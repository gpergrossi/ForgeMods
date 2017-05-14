package dev.mortus.chunks;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkManager<T extends Chunk> {

	private final Comparator<T> OLDEST_CHUNK_FIRST = new Comparator<T>() {
		public int compare(T o1, T o2) {
			return (int) (o1.lastSeen - o2.lastSeen);
		}
	};
	
	private final Comparator<T> CLOSEST_CHUNK_FIRST = new Comparator<T>() {
		public int compare(T o1, T o2) {
			Point pt1 = new Point(o1.chunkX, o1.chunkY);
			Point pt2 = new Point(o2.chunkX, o2.chunkY);
			double dist1 = pt1.distanceSq(center);
			double dist2 = pt2.distanceSq(center);
			return (int) (dist1 - dist2);
		}
	};
	
	private final Comparator<T> FARTHEST_CHUNK_FIRST = new Comparator<T>() {
		public int compare(T o1, T o2) {
			Point pt1 = new Point(o1.chunkX, o1.chunkY);
			Point pt2 = new Point(o2.chunkX, o2.chunkY);
			double dist1 = pt1.distanceSq(center);
			double dist2 = pt2.distanceSq(center);
			return (int) (dist2 - dist1);
		}
	};
	
	ChunkLoader<T> loader;
	double chunkSize;
	
	// Queue of chunks to be loaded (Use lock)
	Queue<T> loadingQueue;		
	Lock loadingQueueLock = new ReentrantLock(true);
	
	// Queue of chunks to be unloaded (Use lock)
	Queue<T> unloadingQueue;	
	Lock unloadingQueueLock = new ReentrantLock(true);
	
	// List of chunk sorted by age
	Queue<T> loadedChunks;
	Lock loadedChunksLock = new ReentrantLock(true);

	Thread[] workers;
	Object workAvailable = new Object();	// Notification object to tell worker threads of work
	boolean workersRunning = false;			// Running condition for quick kill of worker threads
	long currentViewIteration;				// Used to keep track of how long a chunk has been out of view
	Point center = new Point(0,0);
	
	public ChunkManager(ChunkLoader<T> loader) {
		this(loader, 4, 40);
	}
	
	public ChunkManager(ChunkLoader<T> loader, int numWorkers) {
		this(loader, numWorkers, 40);
	}
	
	public ChunkManager(ChunkLoader<T> loader, int numWorkers, int initialQueueSize) {
		this.loader = loader;
		this.chunkSize = loader.getChunkSize();
		loadingQueue = new StochasticPriorityQueue<T>(initialQueueSize, CLOSEST_CHUNK_FIRST);
		unloadingQueue = new StochasticPriorityQueue<T>(initialQueueSize, FARTHEST_CHUNK_FIRST);
		loadedChunks = new StochasticPriorityQueue<T>(initialQueueSize, OLDEST_CHUNK_FIRST);
		workers = new Thread[numWorkers];
		for (int i = 0; i < numWorkers; i++) {
			workers[i] = new Thread(new WorkerTask<T>(this));
			workers[i].setName("ChunkManager worker thread #"+(i+1)+"/"+numWorkers+"");
			workers[i].setPriority(Thread.MIN_PRIORITY);
		}
		currentViewIteration = 0;
	}
	
	public void start() {
		startWorkers();
	}
	
	public void stop() {
		stopWorkers();
	}
	
	public int getNumLoaded() {
		acquire("loadedChunks", loadedChunksLock);
		int num = loadedChunks.size();
		release("loadedChunks", loadedChunksLock);
		return num;
	}
	
	public T getChunk(double x, double y) {
		Point p = getChunkCoordinate(x, y);
		return loader.getChunk(p.x, p.y);
	}
	
	public void update(Rectangle2D viewBounds) {
		Point upperLeft = getChunkCoordinate(viewBounds.getMinX(), viewBounds.getMinY());
		Point lowerRight = getChunkCoordinate(viewBounds.getMaxX(), viewBounds.getMaxY());
		int minX = upperLeft.x, minY = upperLeft.y;
		int maxX = lowerRight.x, maxY = lowerRight.y;
		
		// For priority evaluation
		int centerX = (minX + maxX) / 2;
		int centerY = (minY + maxY) / 2;
		center = new Point(centerX, centerY);
		
		// For unloading
		currentViewIteration++;
		
		// Load new chunks / update lastSeen
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				T chunk = loader.getChunk(x, y);
				chunk.lastSeen = currentViewIteration;
				if (!chunk.isLoaded()) queueLoad(chunk);
			}
		}
		
		// Unload old chunks
		acquire("loadedChunks", loadedChunksLock);
		long maxAge = loader.getMaxChunkAge();
		T chunk = loadedChunks.peek();
		while (chunk != null && (currentViewIteration - chunk.lastSeen) > maxAge) {
			queueUnload(chunk);
			loadedChunks.remove(chunk);
			chunk = loadedChunks.peek();
		}
		release("loadedChunks", loadedChunksLock);
		
		// Stop loading off-screen chunks
		acquire("loadingQueue", loadingQueueLock);
		Iterator<T> iterator = loadingQueue.iterator();
		while (iterator.hasNext()) {
			chunk = iterator.next();
			if ((currentViewIteration - chunk.lastSeen) > 0) {
				chunk.loading = false;
				iterator.remove();
			}
		}
		release("loadingQueue", loadingQueueLock);
	}
	
	public void draw(Graphics2D g) {
		acquire("loadedChunks", loadedChunksLock);
		Iterator<T> iterator = loadedChunks.iterator();
		while (iterator.hasNext()) {
			iterator.next().draw(g);
		}
		release("loadedChunks", loadedChunksLock);
	}

	private Point getChunkCoordinate(double x, double y) {
		Point coord = new Point();
		coord.x = (int) Math.floor(x / chunkSize);
		coord.y = (int) Math.floor(y / chunkSize);
		return coord;
	}
	
	private void queueLoad(T chunk) {
		if (!chunk.canLoad()) return;
		
		acquire("unloadingQueue/"+chunk, unloadingQueueLock, chunk.lock);
		
		if (unloadingQueue.remove(chunk)) {
			chunk.unloading = false;
		}
		
		release("unloadingQueue/"+chunk, unloadingQueueLock, chunk.lock);
		acquire("loadingQueue/"+chunk, loadingQueueLock, chunk.lock);
		
		if (!loadingQueue.contains(chunk) && chunk.canLoad()) {
			chunk.loading = true;
			loadingQueue.offer(chunk);
			debug("Added "+chunk+" to load queue");
			synchronized (workAvailable) {
				workAvailable.notify();
			}
		}

		release("loadingQueue/"+chunk, loadingQueueLock, chunk.lock);
	}
	
	private void queueUnload(T chunk) {
		if (!chunk.canUnload()) return;
		
		acquire("loadingQueue/"+chunk, loadingQueueLock, chunk.lock);
		
		if (loadingQueue.remove(chunk)) {
			chunk.loading = false;
		}

		release("loadingQueue/"+chunk, loadingQueueLock, chunk.lock);
		acquire("unloadingQueue/"+chunk, unloadingQueueLock, chunk.lock);
		
		if (!unloadingQueue.contains(chunk) && chunk.canUnload()) {
			chunk.unloading = true;
			unloadingQueue.offer(chunk);
			debug("Added "+chunk+" to unload queue");
			synchronized (workAvailable) {
				workAvailable.notify();
			}
		}

		release("unloadingQueue/"+chunk, unloadingQueueLock, chunk.lock);
	}
	
	private void addLoaded(T chunk) {
		acquire("loadedChunks", loadedChunksLock);
		loadedChunks.offer(chunk);
		release("loadedChunks", loadedChunksLock);
	}
	
	private static void debug(String string) {
		//System.out.println("[ChunkManager] "+string);
	}
	
	static void acquire(String resource, Lock... locks) {
		boolean allAcquired = false;
		String msg = Thread.currentThread().getName()+" acquiring "+resource+" ("+locks.length+" locks)";
		debug(msg);
		while (!allAcquired) {
			int i;
			allAcquired = true;
			for (i = 0; i < locks.length; i++) {
				try {
					boolean acquired = locks[i].tryLock(50, TimeUnit.MILLISECONDS);
					if (!acquired) {
						allAcquired = false;
						break;
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!allAcquired) {
				Lock failed = locks[i];
				for (i--; i >= 0; i--) {
					locks[i].unlock();
				}
				debug(Thread.currentThread().getName()+" acquire failed. ("+failed+") Yielding...");
				try {
					synchronized (failed) {
						failed.wait();
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static void release(String resource, Lock... locks) {
		String msg = Thread.currentThread().getName()+" releasing "+resource+" ("+locks.length+" locks)";
		debug(msg);
		for (Lock lock : locks) {
			lock.unlock();
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}
	
	private void startWorkers() {
		debug("Starting worker threads...");
		workersRunning = true;
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
			debug(workers[i].getName()+" has been started.");
		}
		debug("Started.");
	}
	
	private void stopWorkers() {
		debug("Stopping worker threads...");
		workersRunning = false;
		synchronized (workAvailable) {
			workAvailable.notifyAll();
		}
		for (int i = 0; i < workers.length; i++) {
			boolean joined = false;
			while (!joined) {
				try {
					workers[i].join();
					debug(workers[i].getName()+" has been stopped.");
					joined = true;
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		debug("Stopped.");
	}
	
	private static class WorkerTask<T extends Chunk> implements Runnable {
		
		ChunkManager<T> manager;
		Job<T> myJob;
		
		public WorkerTask(ChunkManager<T> manager) {
			this.manager = manager;
			myJob = new Job<T>(manager);
		}
		
		public void run() {
			while (manager.workersRunning) {
			    doWork();
			}
		}
		
		private void debug(String msg) {
			ChunkManager.debug(msg);
		}
		
		private void doWork() {
			while (!getJob(myJob).isAssigned()) {
				try {
					debug(Thread.currentThread().getName()+" is waiting for work.");
					synchronized (manager.workAvailable) {
						manager.workAvailable.wait();
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				if (!manager.workersRunning) return;
			}
			myJob.complete();
		}

		private Job<T> getJob(Job<T> job) {			
			// Look for unloading jobs first
			debug(Thread.currentThread().getName()+" locking unloading queue");
			acquire("unloadingQueue", manager.unloadingQueueLock);
			job.chunk = manager.unloadingQueue.poll();
			if (job.chunk != null) {
				acquire("chunk.lock", job.chunk.lock);
				job.type = Job.Type.UNLOAD;
				debug(Thread.currentThread().getName()+" locking "+job.chunk);
				release("unloadingQueue", manager.unloadingQueueLock);
				debug(Thread.currentThread().getName()+" unlocked unloading queue");
				return job;
			}
			release("unloadingQueue", manager.unloadingQueueLock);
			debug(Thread.currentThread().getName()+" unlocked unloading queue");
			
			// Look for loading jobs
			debug(Thread.currentThread().getName()+" locking loading queue");
			acquire("loadingQueue", manager.loadingQueueLock);
			job.chunk = manager.loadingQueue.poll();
			if (job.chunk != null) {
				debug(Thread.currentThread().getName()+" locking "+job.chunk);
				acquire("chunk.lock", job.chunk.lock);
				job.type = Job.Type.LOAD;
				release("loadingQueue", manager.loadingQueueLock);
				debug(Thread.currentThread().getName()+" unlocked loading queue");
				return job;
			}
			release("loadingQueue", manager.loadingQueueLock);
			debug(Thread.currentThread().getName()+" unlocked loading queue");

			job.type = Job.Type.UNASSIGNED;
			
			return job;
		}
	}
	
	/**
	 * Job class that represents a chunk related job returned 
	 * by the WorkerTask getJob method and used by Tasks.
	 */
	private static class Job<T extends Chunk> {
		ChunkManager<T> manager;
		
		enum Type { UNASSIGNED, LOAD, UNLOAD };
		Type type = Type.UNASSIGNED;
		T chunk = null;
		
		public Job(ChunkManager<T> manager) {
			this.manager = manager;
		}
		
		public boolean isAssigned() {
			return type != Type.UNASSIGNED;
		}
		
		private void debug(String msg) {
			ChunkManager.debug(msg);
		}
		
		public void complete() {
			if (this.chunk == null) {
				debug("Error: Job does not refer to a chunk");
				return;
			}
			
			// Do job
			switch (type) {
				case LOAD:
					chunk.internalLoad();
					manager.addLoaded(chunk);
					debug(chunk+" loaded.");
					break;
				case UNLOAD:
					chunk.internalUnload();
					debug(chunk+" unloaded.");
					break;
				default: 
					debug("Error: Job type is UNASSIGNED");
					return;
			}

			debug(Thread.currentThread().getName()+" unlocking "+chunk);
			release(chunk.toString(), chunk.lock);
			
			// Clear job info
			this.type = Type.UNASSIGNED;
			this.chunk = null;
		}
	}
	
}
