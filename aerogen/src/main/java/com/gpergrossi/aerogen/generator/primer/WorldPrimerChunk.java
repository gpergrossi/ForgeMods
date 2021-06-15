package com.gpergrossi.aerogen.generator.primer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.tasks.Task;
import com.gpergrossi.tasks.Task.Priority;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class WorldPrimerChunk {
	
	private static class ChunkLock extends ReentrantLock {
		private static final long serialVersionUID = 2438038171384929688L;
		
		private final WorldPrimerChunk chunk;
		private Thread owner;
		private String ownerReason;
		
		public ChunkLock(WorldPrimerChunk chunk) {
			super();
			this.chunk = chunk;
		}
		
		public void lock(String reason) {
			boolean success = super.tryLock();
			if (success) return;
			
			if (owner != null) {
				if (reason == null) reason = "null";
				System.err.println(Thread.currentThread().getName()+": Locking for \""+reason+"\" had to wait on lock for "
						+ "chunk["+chunk.getChunkX()+", "+chunk.getChunkZ()+"]. "
						+ "Owned by "+owner.getName()+" for \""+ownerReason+"\"");
			}
			super.lock();
			
			owner = Thread.currentThread();
			ownerReason = reason;
		}
	}
	
	private static final Task FINISHED_TASK = Task.createFinished("<COMPLETE>");
	
	public static final int NOT_LOADED = 0;
	public static final int LOAD_STATUS_GENERATED = 1;
	public static final int LOAD_STATUS_POPULATED = 2;
	public static final int LOAD_STATUS_BIOMES = 4;
	public static final int LOAD_STATUS_COMPLETED = 8;
	
	private int loadedStatus = NOT_LOADED;
	
	private volatile boolean needsSave = false;
	boolean inSaveQueue = false;
	long modifyTimestamp;

	private volatile boolean needsLoad;
	private volatile boolean needsRegions;
	private volatile boolean needsIslands;
	private volatile boolean hasBiomes;
	private volatile boolean isGenerated; 
	private volatile boolean isPopulated;
	private volatile boolean isCompleted;
	private volatile boolean isDumped;
	private final boolean isProxy;

	private Task loadTask;
	private Task biomesTask;
	private Task generateTask;
	private Task populateTask;
	private Task completeTask;
	
	public final WorldPrimer world;
	public final Int2D chunkCoord;
	
	private final ChunkLock taskLock;
	private final ChunkLock modifyLock;
	
	private List<Region> regions;
	private List<Island> islands;

	private byte[] biomes;
	private ChunkPrimerExt blocks;
	
	private int[] heightmap;
	
	public WorldPrimerChunk(WorldPrimer world, int chunkX, int chunkZ) {
		this(world, chunkX, chunkZ, false, false);
	}
	
	private WorldPrimerChunk(WorldPrimer world, int chunkX, int chunkZ, boolean isProxy, boolean needsLoad) {
		this.world = world;
		this.chunkCoord = new Int2D(chunkX, chunkZ);
		this.taskLock = new ChunkLock(this);
		this.modifyLock = new ChunkLock(this);
		
		this.isProxy = isProxy;
		
		this.needsLoad = needsLoad;
		if (isProxy) {
			hasBiomes = true;
			isGenerated = true;
			isPopulated = true;
			isCompleted = true;
		}
		
		this.debugPrintChunkLog("constructed");
	}
	

	public static WorldPrimerChunk createNeedsLoad(WorldPrimer world, int chunkX, int chunkZ) {
		return new WorldPrimerChunk(world, chunkX, chunkZ, false, true);
	}
	
	public static WorldPrimerChunk createProxy(WorldPrimer world, int chunkX, int chunkZ) {
		return new WorldPrimerChunk(world, chunkX, chunkZ, true, false);
	}
	


	public byte getBiome(int i, int j) {
		return getBiomes()[j << 4 | i];
	}
	
	public byte[] getBiomes() {
		if (this.isCompleted) {
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkCoord.x(), chunkCoord.y());
			return mcChunk.getBiomeArray();
		}
		if (!this.hasBiomes) throw new RuntimeException("Cannot getBiomes on a Chunk("+chunkCoord+"). Has not generated biomes yet!");

		checkModifyLock("getBiomes");
		return biomes;
	}

	public int getHeight(int x, int z) {
		if (isCompleted) {
			warnWithStack("getHeight on primer chunk that is already complete!");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkCoord.x(), chunkCoord.y());
			return mcChunk.getHeightValue(x, z);
		}
		if (!this.isGenerated) throw new RuntimeException("Cannot getHeight on a chunk("+chunkCoord+"). Has not generated yet!");
		
		checkModifyLock("getHeight");
		return heightmap[z << 4 | x] + 1;
	}

	public IBlockState getBlockState(int x, int y, int z) {
		if (isCompleted) {
			warnWithStack("getBlockState on primer chunk that is already complete!");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkCoord.x(), chunkCoord.y());
			return mcChunk.getBlockState(x, y, z);
		}
		
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return Blocks.AIR.getDefaultState();

		if (!this.isGenerated) throw new RuntimeException("Cannot getBlockState on a chunk("+chunkCoord+"). Has not generated yet!");

		checkModifyLock("getBlockState");
		return blocks.getBlockState(x, y, z);
	}
	
	public void setBlockState(int x, int y, int z, IBlockState state) {		
		if (isCompleted) {
			warnWithStack("setBlockState on primer chunk that is already complete!");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkCoord.x(), chunkCoord.y());
			mcChunk.setBlockState(new BlockPos(x, y, z), state);
			return;
		}
		
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return;

		if (!this.isGenerated) throw new RuntimeException("Cannot getBlockState on a chunk("+chunkCoord+"). Has not generated yet!");

		checkModifyLock("setBlockState");
		blocks.setBlockState(x, y, z, state);
		
		// Update height map
		int index = z << 4 | x;
		if (y >= heightmap[index]) {
			if (state.getMaterial() != Material.AIR) {
				heightmap[index] = y;
			} else if (y == heightmap[index]) {
				heightmap[index] = blocks.findFirstBlockBelow(x, y, z);
			}
		}
		setNeedsSave(true);
	}

	/**
	 * Completes this chunk and provides it to the MinecraftWorld. This method should only ever be called once.
	 * Since it is called only by the Minecraft thread dealing with world generation.
	 */
	public Tuple2<ChunkPrimer, byte[]> getCompleted() {
		debugPrintChunkLog("getCompleted");

		Task task = this.getCompleteTask();
		try {
			task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		
		this.isDumped = true;
		this.save();
		
		return new Tuple2<ChunkPrimer, byte[]>(this.blocks, this.biomes);
	}

	public void save() {
		if (this.isProxy) return;
		this.needsSave = true;
		getSaveTask();
	}
	
	public void setNeedsSave(boolean dirty) {
		if (this.isProxy) return;
		if (!dirty) {
			this.needsSave = false;
		} else {
			if (!needsSave) {
				needsSave = true;
				modifyTimestamp = System.currentTimeMillis();
				if (!inSaveQueue) {
					world.saveQueue.offer(new Tuple2<>(modifyTimestamp, this));
					inSaveQueue = true;
				}
			}
		}
	}
	
	public boolean needsSave() {
		return needsSave;
	}
	
	
	
	public Task getSaveTask() {
		if (this.isProxy) return FINISHED_TASK;
		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (!this.needsSave) return FINISHED_TASK;
			this.needsSave = false;
		} finally {
			taskLock.unlock();
		}
		
		Task saveTask = new SaveTask(this, Priority.NORMAL);
		world.generator.getTaskManager().submit(saveTask);
		return saveTask;
	}

	public Task getLoadTask() {
		if (this.isProxy) return FINISHED_TASK;
		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (this.loadTask != null) return this.loadTask;
			if (!this.needsLoad) return (this.loadTask = FINISHED_TASK);
			
			this.loadTask = new LoadTask(this, Priority.NORMAL);
		} finally {
			taskLock.unlock();
		}
		
		world.generator.getTaskManager().submit(loadTask);
		return loadTask;
	}
	
	public Task getBiomesTask() {		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (this.biomesTask != null) return this.biomesTask;
			if (this.hasBiomes) return (this.biomesTask = FINISHED_TASK);
			
			this.biomesTask = new BiomesTask(this, Priority.NORMAL);
		} finally {
			taskLock.unlock();
		}
		
		world.generator.getTaskManager().submit(biomesTask);
		return biomesTask;
	}
	
	public Task getGenerateTask() {		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (this.generateTask != null) return this.generateTask;
			if (this.isGenerated) return (this.generateTask = FINISHED_TASK);
			
			this.generateTask = new GenerateTask(this, Priority.NORMAL);
		} finally {
			taskLock.unlock();
		}
		
		world.generator.getTaskManager().submit(generateTask);
		return generateTask;
	}
	
	public Task getPopulateTask() {		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (this.populateTask != null) return this.populateTask;
			if (this.isPopulated) return (this.populateTask = FINISHED_TASK);
			
			this.populateTask = new PopulateTask(this, Priority.NORMAL);
		} finally {
			taskLock.unlock();
		}
		
		world.generator.getTaskManager().submit(populateTask);
		return populateTask;
	}
	
	public Task getCompleteTask() {		
		Lock taskLock = this.getTaskLock();
		taskLock.lock();
		try {
			if (this.completeTask != null) return this.completeTask;
			if (this.isCompleted) return (this.completeTask = FINISHED_TASK);
			
			this.completeTask = new CompleteTask(this, Priority.NORMAL);
		} finally {
			taskLock.unlock();
		}
		
		world.generator.getTaskManager().submit(completeTask);
		return completeTask;
	}
	
	
	
	public ChunkLock getTaskLock() {
		return this.taskLock;
	}
	
	public ChunkLock getModifyLock() {
		return this.modifyLock;
	}

	private void checkModifyLock(String method) {
		ReentrantLock lock = getModifyLock();
		if (!lock.isHeldByCurrentThread()) {
			warnWithStack("Failed checkModifyLock on call to "+method);
		}
	}
	
	void debugPrintChunkLog(String string) {
		if (!WorldPrimer.DEBUG_PRINT_CHUNK_LOG) return;
		
		String id = this.toString();
		id = id.substring(id.indexOf("@"));
		
		String status = (this.hasBiomes ? "B" : "-") + (this.isGenerated ? "G" : "-") + (this.isPopulated ? "P" : "-") + (this.isCompleted ? "C" : "-");
		
		AeroGenMod.log.info("Chunk ["+chunkCoord+"] ("+id+") "+status+":"+string);
	}
	
	void warnWithStack(String message) {
		StringBuilder warning = new StringBuilder();
		warning.append("WARNING! ").append(message).append("\n");
		warning.append("This is typically caused by a feature populating blocks outside the allowed bounds.\n");
		warning.append("Stack trace:\n");
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (int i = 2; i < trace.length; i++) {
			warning.append("   ").append(trace[i]).append("\n");
			if (trace[i].getClassName().equals(AeroGenerator.class.getName())) {
				final int remaining = trace.length-1 - i;
				warning.append("   ... ").append(remaining).append(" more");
				break;
			}
		}
		AeroGenMod.log.warn(warning.toString());
	}
	
	public boolean isNeighborSameStatus(int offsetX, int offsetZ) {
		WorldPrimerChunk neighbor = peakNeighbor(offsetX, offsetZ);
		if (neighbor == null) return false;
		if (this.hasBiomes != neighbor.hasBiomes) return false;
		if (this.isGenerated != neighbor.isGenerated) return false;
		if (this.isPopulated != neighbor.isPopulated) return false;
		if (this.isCompleted != neighbor.isCompleted) return false;
		return true;
	}
	
	public WorldPrimerChunk peakNeighbor(int offsetX, int offsetZ) {
		return world.peakPrimerChunk(chunkCoord.x()+offsetX, chunkCoord.y()+offsetZ);
	}
	
	public WorldPrimerChunk getNeighbor(int offsetX, int offsetZ) {
		return world.getOrCreatePrimerChunk(chunkCoord.x()+offsetX, chunkCoord.y()+offsetZ);
	}
	
	public boolean hasBiomes() {
		return hasBiomes;
	}

	public boolean isGenerated() {
		return isGenerated;
	}

	public boolean isPopulated() {
		return isPopulated;
	}	

	public boolean isCompleted() {
		return isCompleted;
	}
	
	public boolean isDumped() {
		return isDumped;
	}

	public boolean wasGeneratedOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_GENERATED) != 0;
	}
	
	public boolean wasPopulatedOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_POPULATED) != 0;
	}
	
	public boolean hadBiomesOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_BIOMES) != 0;
	}

	public int getLoadedStatus() {
		return this.loadedStatus;
	}
	
	
	
	
	private static final int NBT_TAG_BYTE_ARRAY = 7;
	private static final int NBT_TAG_LIST = 9;
	private static final int NBT_TAG_COMPOUND = 10;
	
	private NBTTagCompound writeToNBT() {
		if (this.isDumped()) return new NBTTagCompound();
		
		final NBTTagCompound chunkCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setInteger("DataVersion", 1);
		
		final NBTTagCompound levelCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setTag("Level", levelCompoundTag);
		
		levelCompoundTag.setInteger("xPos", chunkCoord.x());
		levelCompoundTag.setInteger("zPos", chunkCoord.y());
		levelCompoundTag.setBoolean("TerrainPopulated", this.isPopulated);
		if (this.hasBiomes) levelCompoundTag.setByteArray("Biomes", this.biomes);
		levelCompoundTag.setBoolean("Completed", this.isCompleted);
		
		if (this.isGenerated) {
			levelCompoundTag.setIntArray("HeightMap", this.heightmap);
			levelCompoundTag.setTag("Sections", this.blocks.getSectionsNBT());
		}
		
		return chunkCompoundTag;
	}
	
	private void readFromNBT(NBTTagCompound nbt) {
		if (nbt == null) return;
				
		final NBTTagCompound levelNBT = nbt.getCompoundTag("Level");
		final int chunkX = levelNBT.getInteger("xPos");
		final int chunkZ = levelNBT.getInteger("zPos");
		
		if (chunkX != chunkCoord.x() || chunkZ != chunkCoord.y()) {
			throw new RuntimeException("Chunk coordinates do not match saved chunk data!");
		}
				
		if (levelNBT.hasKey("Biomes", NBT_TAG_BYTE_ARRAY)) {
			this.biomes = levelNBT.getByteArray("Biomes");
			this.hasBiomes = true;
			this.loadedStatus |= LOAD_STATUS_BIOMES;
		}
		
		if (levelNBT.hasKey("Sections", NBT_TAG_LIST)) {
			this.blocks = ChunkPrimerExt.fromNBT(levelNBT.getTagList("Sections", NBT_TAG_COMPOUND));
			this.heightmap = levelNBT.getIntArray("HeightMap");
			this.isGenerated = true;
			this.loadedStatus |= LOAD_STATUS_GENERATED;
		} else {
			AeroGenMod.log.warn("Chunk "+chunkX+", "+chunkZ+" had no Sections NBT");
		}

		if (levelNBT.getBoolean("TerrainPopulated")) {
			this.isPopulated = true;
			this.loadedStatus |= LOAD_STATUS_POPULATED;
		}
		
		if (levelNBT.getBoolean("Completed")) {
			this.isCompleted = true;
			this.loadedStatus |= LOAD_STATUS_COMPLETED;
		}
	}

	public int getChunkX() {
		return chunkCoord.x();
	}
	
	public int getChunkZ() {
		return chunkCoord.y();
	}
	
	
	
	/**
	 * The Save task saves the chunk.
	 */
	private static final class SaveTask extends Task {		
		private final WorldPrimerChunk self;
		private NBTTagCompound nbt;
		private boolean shouldDump;
		
		public SaveTask(WorldPrimerChunk chunk, Priority priority) {
			super("Save Chunk["+chunk.getChunkX()+", "+chunk.getChunkZ()+"]", priority);
			self = chunk;
		}

		@Override
		public void work() {
			shouldDump = self.isDumped;
			
			ChunkLock lock = self.getModifyLock();
			final String reason = "Save ["+self.getChunkX()+", "+self.getChunkZ()+"]";
			lock.lock(reason);
			try {
				nbt = self.writeToNBT();
			} finally {
				lock.unlock();
			}
			
			this.blockIO(this::save, this::complete);
		}
		
		public void save() {
			self.world.chunkLoader.writeChunkData(self.chunkCoord, nbt);
			nbt = null;
		}
		
		public void complete() {			
			// Mark complete
			this.setFinished();
			
			if (shouldDump) self.world.dump(self);
			
			self.debugPrintChunkLog("save");
		}
	}
	
	/**
	 * The Load task loads the chunk.
	 */
	private static final class LoadTask extends Task {		
		private final WorldPrimerChunk self;
		private NBTTagCompound nbt;
		
		public LoadTask(WorldPrimerChunk chunk, Priority priority) {
			super("Load Chunk["+chunk.getChunkX()+", "+chunk.getChunkZ()+"]", priority);
			self = chunk;
		}

		@Override
		public void work() {
			this.blockIO(this::load, this::complete);
		}
		
		public void load() {
			nbt = self.world.chunkLoader.readChunkData(self.chunkCoord);
		}
		
		public void complete() {
			ChunkLock lock = self.getModifyLock();
			final String reason = "Load ["+self.getChunkX()+", "+self.getChunkZ()+"]";
			lock.lock(reason);
			try {
				self.readFromNBT(nbt);
				nbt = null;
			} finally {
				lock.unlock();
			}
			
			// Mark complete
			self.needsLoad = false;
			this.setFinished();
			
			self.debugPrintChunkLog("load");
		}
	}
	
	/**
	 * The Biomes task loads the chunk if necessary, then calls AeroGenerator.generateBiomes().
	 */
	private static final class BiomesTask extends ChunkTask {
		
		public BiomesTask(WorldPrimerChunk chunk, Priority priority) {
			super(chunk, "Biomes", priority);
		}

		@Override
		public void begin() {
			if (self.hasBiomes) {
				this.setFinished();
				return;
			}
			
			ChunkLock lock = self.getModifyLock();
			final String reason = "Biomes ["+self.getChunkX()+", "+self.getChunkZ()+"]";
			lock.lock(reason);
			try {				
				self.biomes = new byte[256];
				self.world.getGenerator().generateBiomes(self.biomes, self.getChunkX(), self.getChunkZ());
			} finally {
				lock.unlock();
			}
			
			// Mark complete
			self.hasBiomes = true;
			self.setNeedsSave(true);
			this.setFinished();
			
			self.debugPrintChunkLog("biomes");
		}
	}
	
	/**
	 * The Generate task loads the chunk if necessary, then calls AeroGenerator.generateTerrain().
	 */
	private static final class GenerateTask extends ChunkTask {
		
		public GenerateTask(WorldPrimerChunk chunk, Priority priority) {
			super(chunk, "Generate", priority);
		}

		@Override
		public void begin() {
			if (self.isGenerated) {
				this.setFinished();
				return;
			}

			ChunkLock lock = self.getModifyLock();
			final String reason = "Generate ["+self.getChunkX()+", "+self.getChunkZ()+"]";
			lock.lock(reason);
			try {
				// Generate terrain blocks
				self.blocks = new ChunkPrimerExt();
				self.world.getGenerator().generateTerrain(self.blocks, self.getChunkX(), self.getChunkZ());
				
				// Create the initial heightmap for the terrain tiles
				self.heightmap = new int[256];
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						self.heightmap[z << 4 | x] = self.blocks.findFirstBlockBelow(x, 255, z);
					}	
				}
			} finally {
				lock.unlock();
			}
			
			// Mark complete
			self.isGenerated = true;
			self.setNeedsSave(true);
			this.setFinished();
			
			self.debugPrintChunkLog("generate blocks");
		}
	}
	
	/**
	 * The populate task will create and wait for the Generate tasks for itself and its positive neighbors.
	 * Then it will wait for the locks on all four chunks and call AeroGenerator.prePopulate().
	 * 
	 * Populates this chunk's populate region (+8, +8, +24, +24) if it has not been populated already.
	 * Population of this chunks populate region requires generation of this chunk's terrain blocks and those of
	 * it positive 3-neighbors (+0, +1), (+1, +0), and (+1, +1).
	 */
	private static final class PopulateTask extends ChunkTask {
		
		public PopulateTask(WorldPrimerChunk chunk, Priority priority) {
			super(chunk, "Populate", priority);
		}

		@Override
		public void begin() {
			if (self.isPopulated) {
				this.setFinished();
				return;
			}
			
			final List<Task> waitFor = new ArrayList<>();
			
			waitFor.add(self.getGenerateTask());
			waitFor.add(self.getNeighbor(0, 1).getGenerateTask());
			waitFor.add(self.getNeighbor(1, 0).getGenerateTask());
			waitFor.add(self.getNeighbor(1, 1).getGenerateTask());
			
			// Block until other tasks are done
			this.block(waitFor, this::finish);
		}

		private void finish() {			
			List<ChunkLock> locks = new ArrayList<>();
			locks.add(self.getModifyLock());
			locks.add(self.getNeighbor(0, 1).getModifyLock());
			locks.add(self.getNeighbor(1, 0).getModifyLock());
			locks.add(self.getNeighbor(1, 1).getModifyLock());
			
			final String reason = "Populate ["+self.getChunkX()+", "+self.getChunkZ()+"]";
			locks.forEach(lock -> lock.lock(reason));
			try {
				// The populate region is offset by (+8, +8). This means features will be able to
				// place blocks overhanging their spawn location by up to (+7, +7) and down to (-8, -8)
				// This is an idea borrowed from Minecraft's populate functionality.
				self.world.getGenerator().prePopulate(self.world, self.getChunkX(), self.getChunkZ());
			} finally {
				locks.forEach(Lock::unlock);
			}

			// Mark complete
			self.isPopulated = true;
			self.setNeedsSave(true);
			this.setFinished();
			
			self.debugPrintChunkLog("populate");
		}
	}
	
	/**
	 * The Complete task will create and wait for the Populate tasks for itself and its negative neighbors.
	 */
	private static final class CompleteTask extends ChunkTask {
		
		public CompleteTask(WorldPrimerChunk chunk, Priority priority) {
			super(chunk, "Complete", priority);
		}

		@Override
		public void begin() {
			if (self.isCompleted) {
				this.setFinished();
				return;
			}
			
			final List<Task> waitFor = new ArrayList<>();
			
			waitFor.add(self.getBiomesTask());
			waitFor.add(self.getPopulateTask());
			waitFor.add(self.getNeighbor(0, -1).getPopulateTask());
			waitFor.add(self.getNeighbor(-1, 0).getPopulateTask());
			waitFor.add(self.getNeighbor(-1, -1).getPopulateTask());
			
			// Block until other tasks are done
			this.block(waitFor, this::finish);
		}

		private void finish() {
			// Mark complete
			self.isCompleted = true;
			self.setNeedsSave(true);
			this.setFinished();
			
			self.debugPrintChunkLog("complete");
		}
	}
	
	private static abstract class ChunkTask extends Task {
		protected final WorldPrimerChunk self;
		
		public ChunkTask(WorldPrimerChunk chunk, String operation, Priority priority) {
			super(operation+" Chunk["+chunk.getChunkX()+", "+chunk.getChunkZ()+"]", priority);
			self = chunk;
		}
		
		@Override
		public final void work() {
			if (self.needsLoad) {
				load();
				return;
			}
			if (self.needsRegions) {
				getRegions();
				return;
			}
			begin();
		}

		private final void load() {
			final List<Task> waitFor = new ArrayList<>();
			waitFor.add(self.getLoadTask());
			this.block(waitFor, this::work);
		}
		
		private final void getRegions() {
	        int chunkMinX = (self.getChunkX() << 4);
	        int chunkMinZ = (self.getChunkZ() << 4);
	        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
			
			RegionManager regionManager = self.world.generator.getRegionManager();
			final List<Region> regions = new ArrayList<>();
			regionManager.getRegions(regions, chunkBounds);
			
			ChunkLock lock = self.getModifyLock();
			lock.lock("getRegions");
			try {
				self.regions = regions;
			} finally {
				lock.unlock();
			}
			
			final List<Task> waitFor = new ArrayList<>();
			for (Region region : regions) {
				waitFor.add(region.getGenerateTask());
			}
			this.block(waitFor, this::getRegionsReturn);
		}
		
		private final void getRegionsReturn() {
			self.needsRegions = false;
			work();
		}
		
		private final void getIslands() {
	        int chunkMinX = (self.getChunkX() << 4);
	        int chunkMinZ = (self.getChunkZ() << 4);
	        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
			
			final List<Island> islands = new ArrayList<>();
			
			ChunkLock lock = self.getModifyLock();
			lock.lock("getIslands");
			try {
				for (Region region : self.regions) {
					region.getIslands(islands, chunkBounds);
				}
				self.islands = islands;
			} finally {
				lock.unlock();
			}
	        
			final List<Task> waitFor = new ArrayList<>();
			for (Island island : islands) {
				waitFor.add(island.getGenerateTask());
			}
			this.block(waitFor, this::getIslandsReturn);
		}
		
		private final void getIslandsReturn() {
			self.needsIslands = false;
			work();
		}
		
		public abstract void begin();
	}
	
}
