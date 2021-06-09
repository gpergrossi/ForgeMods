package com.gpergrossi.aerogen.generator.primer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ChunkPrimerExt extends ChunkPrimer {
	
	protected static final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
	
	@SuppressWarnings("deprecation")
	protected static final char DEFAULT_STATE_CHAR = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
	
	protected final char[] data;
	protected int lowestY = 255;
	protected int highestY = 0;
	
	public ChunkPrimerExt() {
		super();
		this.data = ReflectionHelper.getPrivateValue(ChunkPrimer.class, this, 1);
	}

	@Override
    @SuppressWarnings("deprecation")
	public IBlockState getBlockState(int x, int y, int z) {
    	final int index = x << 12 | z << 8 | y;
        final IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(this.data[index]);
        return iblockstate == null ? DEFAULT_STATE : iblockstate;
    }

	@Override
    @SuppressWarnings("deprecation")
	public void setBlockState(int x, int y, int z, IBlockState state) {
    	final int index = x << 12 | z << 8 | y;
        this.data[index] = (char) Block.BLOCK_STATE_IDS.get(state);
        
        if (state != DEFAULT_STATE) {
        	lowestY = Math.min(lowestY, y);
        	highestY = Math.max(highestY, y);
        }
    }

	@Override
	public int findGroundBlockIdx(int x, int z) {
        return findFirstBlockBelow(x, 255, z);
	}
	
	public int findFirstBlockBelow(int x, int y, int z) {
		if (y < lowestY) return 0;
		if (y > highestY) y = highestY;
		
        int xz = (x << 12 | z << 8);
        for (int yi = y; yi >= lowestY; yi--) {
            char block = this.data[xz | yi];
            if (block != 0 && block != DEFAULT_STATE_CHAR) {
                return yi;
            }
        }
        return 0;
	}
	
	public int findFirstBlockAbove(int x, int y, int z) {
		if (y > highestY) return 255;
		if (y < lowestY) y = lowestY;
		
        int xz = (x << 12 | z << 8);
        for (int yi = y; yi <= highestY; yi++) {
            char block = this.data[xz | yi];
            if (block != 0 && block != DEFAULT_STATE_CHAR) {
                return yi;
            }
        }
        return 255;
	}
	
	public static enum DataResult {
		NONE, NORMAL, HIGH_IDS;
		public boolean hasBlocks() {
			return this != NONE;
		}
		public boolean hasHighIDs() {
			return this == HIGH_IDS;
		}
	}
	
	public DataResult getSectionData(int sectionIndexY, byte[] blockIDs, NibbleArray blockIDsHigh, NibbleArray blockData) {
		final int minY = sectionIndexY << 4;
		final int maxY = minY + 16;
		if (maxY < lowestY || minY > highestY) return DataResult.NONE;
		
		boolean anyBlocks = false;
		boolean anyHighIDs = false;
		
		for (int x = 0; x < 16; x++) {
			final int xi = (x << 12);
			
			for (int z = 0; z < 16; z++) {
				final int zi = xi | (z << 8) | minY;
				
				for (int y = 0; y < 16; y++) {
					final char block = this.data[zi + y];
		            if (block == 0 || block == DEFAULT_STATE_CHAR) continue;

					anyBlocks = true;
					
		            final int index = ((x << 8) | (z << 4) | y); 
		            blockIDs[index] = (byte) ((block >> 4) & 0xFF);
		            blockData.setIndex(index, block & 0x0F);
		            
		            final byte blockIDHigh = (byte) ((block >> 12) & 0x0F);
		            if (blockIDHigh != 0) {
			            anyHighIDs = true;
			            blockIDsHigh.setIndex(index, blockIDHigh);
		            }
				}
			}
		}
		
		if (anyHighIDs) return DataResult.HIGH_IDS;
		if (anyBlocks) return DataResult.NORMAL;
		return DataResult.NONE;		
	}

	private void putSection(int sectionY, byte[] idsLow, NibbleArray data, NibbleArray idsHigh) {
		final int minY = sectionY << 4; // * 16
		int sectionIndex = 0;
		
		for (int x = 0; x < 16; x++) {
			final int xi = (x << 12);
			
			for (int z = 0; z < 16; z++) {
				final int zi = xi | (z << 8) | minY;
				
				for (int y = 0; y < 16; y++) {
		            final int blockID = idsLow[sectionIndex];
		            final int dataNibble = data.getFromIndex(sectionIndex);
		            final int blockIDExt = (idsHigh == null ? 0 : idsHigh.getFromIndex(sectionIndex));
		            final char block = (char) (blockIDExt << 12 | (blockID & 255) << 4 | dataNibble);
					
					this.data[zi + y] = block;
					sectionIndex++;
				}
			}
		}
	}
	
	// Save block data in up to 16 sections of 16x16x16 blocks
	public NBTTagList getSectionsNBT() {
		NBTTagList sectionListTag = new NBTTagList();
		
		byte[] idsLow = new byte[4096];
		NibbleArray idsHigh = new NibbleArray();
		NibbleArray dataValues = new NibbleArray();
		
		for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
			DataResult result = this.getSectionData(sectionIndex, idsLow, idsHigh, dataValues);
			if (result.hasBlocks()) {
				NBTTagCompound section = new NBTTagCompound();
				sectionListTag.appendTag(section);
				
				section.setInteger("Y", sectionIndex);
				section.setByteArray("Blocks", idsLow);
				section.setByteArray("Data", dataValues.getData());
				idsLow = new byte[4096];
				dataValues = new NibbleArray();
				
				if (result.hasHighIDs()) {
					section.setByteArray("Add", idsHigh.getData());
					idsHigh = new NibbleArray();
				}
			}
		}
		
		return sectionListTag;
	}

	public static ChunkPrimerExt fromNBT(final NBTTagList tagList) {
		final ChunkPrimerExt result = new ChunkPrimerExt();
		
		for (int i = 0; i < tagList.tagCount(); i++) {
			final NBTTagCompound sectionNBT = tagList.getCompoundTagAt(i);
			
			final int sectionY = sectionNBT.getInteger("Y");
			final byte[] idsLow = sectionNBT.getByteArray("Blocks");
			final NibbleArray data = new NibbleArray(sectionNBT.getByteArray("Data"));
			
			final NibbleArray idsHigh;
			final byte[] idsHighBytes = sectionNBT.getByteArray("Add");
			if (idsHighBytes.length == 2048) {
				idsHigh = new NibbleArray(idsHighBytes);
			} else {
				idsHigh = null;
			}
			
			result.putSection(sectionY, idsLow, data, idsHigh);
		}
		
		return result;
	}
	
}
