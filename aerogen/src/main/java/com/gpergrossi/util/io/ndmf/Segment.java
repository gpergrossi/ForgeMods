package com.gpergrossi.util.io.ndmf;

import java.io.IOException;

public class Segment<Name, Data> {

	protected final NamedDataMapFile<Name, Data> ndmFile;
	protected int blockIDStart;
	protected int size;
	protected boolean inUse;
	
	public Segment(NamedDataMapFile<Name, Data> ndmFile, int blockIDStart) {
		this.ndmFile = ndmFile;
		this.blockIDStart = blockIDStart;
		this.size = 4;
	}
	
	public void read() {
		try {
			ndmFile.seekBlock(blockIDStart, 0);
			this.size = ndmFile.readSegmentHeader();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write() {
		try {
			ndmFile.seekBlock(blockIDStart, 0);
			ndmFile.writeSegmentHeader(size);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Resizes this segment to the provided {@link newSize} in bytes. The new size is immediately written 
	 * to the disk. If this segment no longer fits in its current blocks and {@link reallocate} is true,
	 * the segment will allocate new block(s) and may be copied to a new location. If {@link reallocate} 
	 * is false and the new size cannot fit, the operation fails, no changes are made, and false is returned.
	 * @param newSize - the size in bytes this segment should change to
	 * @param reallocate - whether or not the operation should allow the allocation of new
	 *  blocks and a potential relocation of this segment's data.
	 * @return true if successful, false otherwise.
	 * @throws IOException 
	 */
	public boolean resize(int newSize, boolean reallocate) throws IOException {
		if (newSize < size) {
			shrink(newSize);
			return true;
		} else if (newSize > size) {
			return grow(newSize, reallocate);
		}
		// newSize == size
		return true;
	}
	
	/**
	 * Shrinks this segment to the provided {@link newSize} in bytes. 
	 * The new size is immediately written to the disk.
	 * @param newSize - the size in bytes this segment should shrink to.
	 * @throws IOException 
	 */
	private void shrink(int newSize) throws IOException {
		if (size < ndmFile.SIZE_SEGMENT_HEADER) throw new RuntimeException("Cannot shrink to a size smaller than SIZE_SEGMENT_HEADER!");
		
		int numBlocks = ndmFile.numBlocks(size);
		int newNumBlocks = ndmFile.numBlocks(newSize);
		
		if (newNumBlocks == numBlocks) {
			this.size = newSize;
			write();
			return;
		}
		
		final int numBlocksFreed = numBlocks - newNumBlocks;
		final int startBlocksFreed = this.blockIDStart + numBlocks;
		final int endBlocksFreed = startBlocksFreed + numBlocksFreed - 1;
		ndmFile.markBlocksFree(startBlocksFreed, endBlocksFreed);
	}

	private boolean grow(int newSize, boolean allowReallocate) throws IOException {
		int numBlocks = ndmFile.numBlocks(size);
		int newNumBlocks = ndmFile.numBlocks(newSize);

		System.out.println("Resize grow "+size+" ("+numBlocks+") --> "+newSize+" ("+newNumBlocks+")");
		
		if (newNumBlocks == numBlocks) {
			this.size = newSize;
			write();
			return true;
		}
		
		if (!allowReallocate) return false;
		
		// Try to grow in place
		final int numBlocksClaimed = (newNumBlocks - numBlocks);
		final int startBlocksClaimed = this.blockIDStart + numBlocks;
		final int endBlocksClaimed = startBlocksClaimed + numBlocksClaimed - 1;
		boolean success = ndmFile.tryClaim(startBlocksClaimed, endBlocksClaimed);
		if (success) {
			this.size = newSize;
			write();
			return true;
		}
		
		// Relocate
		int newClaim = ndmFile.getClaim(newNumBlocks);
		ndmFile.copyData(this.blockIDStart, newClaim, this.size);
		ndmFile.markBlocksFree(this.blockIDStart, this.blockIDStart+numBlocks-1);
		this.blockIDStart = newClaim;
		this.size = newSize;
		write();		
		return true;
	}
	
	protected int getAllocateBlock(int size) throws IOException {
		// Need to at least have space for the size header
		if (size < ndmFile.SIZE_SEGMENT_HEADER) throw new IllegalArgumentException("Cannot allocate a size smaller than SIZE_SEGMENT_HEADER!");
		final int numBlocks = ndmFile.numBlocks(size);
		return ndmFile.getClaim(numBlocks);
	}

}
