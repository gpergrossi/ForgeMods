package com.gpergrossi.test.experiment;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.gpergrossi.tasks.Task;
import com.gpergrossi.tasks.TaskManager;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.spacial.Int2DSet;
import com.gpergrossi.viewframe.View;
import com.gpergrossi.viewframe.ViewerFrame;

public class HugeConway extends View {

	public static void main(String[] args) {
		JFrame settings = new JFrame();
		settings.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		settings.setLayout(new FlowLayout());
		
		JLabel label = new JLabel("Num threads: ");
		settings.add(label);
		
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 0, 512, 1));
		settings.add(spinner);
		
		JButton button = new JButton("Run");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ViewerFrame(new HugeConway((Integer) spinner.getValue()));
				settings.dispose();
			}
		});
		settings.add(button);
		
		settings.pack();
		settings.setVisible(true);
	}
	
	private Lock swapLock = new ReentrantLock();
	private TaskManager taskManager;
	private int numThreads;
	
	private int iterNum = 0;
	
	private final int TILE_SIZE = 32;
	private final Font FONT = new Font("Courier New", Font.PLAIN, 12);
	private final Color bgColor = new Color(0, 0, 0, 32);
	
	private boolean drawGrid = true;
	private boolean dotDisplay = true;
	private boolean displayUI = true;
	private boolean oneLoop = false;
	private boolean keepGoing = false;
	private boolean phosphor = false;
	private boolean antialias = true;
	
	private Int2D mouseTile;
	private Int2D mouseTilePrev;
	
	private Int2DSet currentFrame;
	private Int2DSet nextFrame;
	
	public HugeConway(int numThreads) {
		super(0, 0, 800, 600);
		this.numThreads = numThreads;
	}

	@Override
	public void init() {
		this.currentFrame = new Int2DSet(numThreads);
		this.nextFrame = new Int2DSet(numThreads);
		this.taskManager = TaskManager.create("ConwayTaskManager", numThreads);
	}
	
	@Override
	protected double getMaxZoom() {
		return 4;
	}
	
	@Override
	protected double getMinZoom() {
		return 1.0/32.0;
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		taskManager.shutdown();
		boolean terminated = false;
		try {
			terminated = taskManager.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!terminated) taskManager.shutdownNow();
		taskManager.setTaskMonitorVisible(false);
	}

	@Override
	public void update(double secondsPassed) {
		if (!oneLoop && !keepGoing) return;
		oneLoop = false;
		
		if (taskManager.getTaskCount() == 0) {
			taskManager.submit(new ConwayIterationTask("Conway Iteration "+iterNum));
			iterNum++;
		}
		
	}
	
	@Override
	public Color getBackgroundColor() {
		if (phosphor) return bgColor;
		return Color.BLACK;
	}
	
	@Override
	public void drawWorld(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		
		Rectangle2D bounds = getViewWorldBounds();
		int minX = (int) Math.floor(bounds.getMinX() / TILE_SIZE);
		int minY = (int) Math.floor(bounds.getMinY() / TILE_SIZE);
		int maxX = (int) Math.ceil(bounds.getMaxX() / TILE_SIZE);
		int maxY = (int) Math.ceil(bounds.getMaxY() / TILE_SIZE);

		g2d.setColor(Color.WHITE);
		
		if (drawGrid && getViewZoom() >= 0.25) {
			Int2D.Mutable coord = new Int2D.Mutable();
			for (int x = minX; x <= maxX; x++) {
				coord.x(x);
				for (int y = minY; y <= maxY; y++) {
					coord.y(y);
					
					if (dotDisplay) {
						if (currentFrame.contains(coord)) {
							g2d.fillOval(x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE);
						} else {
							final int i = x*TILE_SIZE + TILE_SIZE/2;
							final int j = y*TILE_SIZE + TILE_SIZE/2;
							g2d.drawLine(i, j, i, j);
						}
					} else {
						if (currentFrame.contains(coord)) {
							g2d.fillRect(x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE);
						} else {
							final int i = x*TILE_SIZE + TILE_SIZE/2;
							final int j = y*TILE_SIZE + TILE_SIZE/2;
							g2d.drawLine(i, j, i, j);
						}
					}
				}
			}
		} else {
			swapLock.lock();
			try {
				for (Int2D coord : currentFrame) {
					if (coord.x() < minX || coord.x() > maxX) continue;
					if (coord.y() < minY || coord.y() > maxY) continue;
					
					if (dotDisplay) {
						g2d.fillOval(coord.x()*TILE_SIZE, coord.y()*TILE_SIZE, TILE_SIZE, TILE_SIZE);
					} else {
						g2d.fillRect(coord.x()*TILE_SIZE, coord.y()*TILE_SIZE, TILE_SIZE, TILE_SIZE);
					}
				}
			} finally {
				swapLock.unlock();
			}
		}
	}

	private Int2D toTile(double x, double y) {
		final int xi = (int) Math.floor(x / TILE_SIZE);
		final int yi = (int) Math.floor(y / TILE_SIZE);
		return new Int2D(xi, yi);
	}
	
	@Override
	public void drawOverlayUI(Graphics2D g2d) {
		if (displayUI) {
			g2d.setFont(FONT);
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, 250, 170);
			
			int y = 10;
			g2d.setColor(Color.WHITE);
			g2d.drawString("(C) Clear all points", 10, y += 10);
			g2d.drawString("(I) One iteration", 10, y += 10);
			g2d.drawString("(L) Begin looping (currently "+(keepGoing ? "on" : "off")+")", 10, y += 10);
			g2d.drawString("(D) Dot display (currently "+(dotDisplay ? "on" : "off")+")", 10, y += 10);
			g2d.drawString("(G) Draw grid (currently "+(drawGrid ? "on" : "off")+")", 10, y += 10);
			g2d.drawString("(P) Phosphor (currently "+(phosphor ? "on" : "off")+")", 10, y += 10);
			g2d.drawString("(A) Antialiasing (currently "+(antialias ? "on" : "off")+")", 10, y += 10);
			y += 10;
			g2d.drawString("Left click to turn tiles on", 10, y += 10);
			g2d.drawString("Right click to turn tiles off", 10, y += 10);
			g2d.drawString("Pan with middle click", 10, y += 10);
			y += 10;
			g2d.drawString(currentFrame.size()+" cells alive ("+currentFrame.numChunks()+" chunks)", 10, y += 10);
			g2d.drawString("(H) Hide this information", 10, y += 10);
		}
	}

	@Override
	public void mousePressed() {
		double mx = getMouseWorldX();
		double my = getMouseWorldY();
		mouseTile = toTile(mx, my);
		
		swapLock.lock();
		try {
			if (getMouseClick() == LEFT_CLICK) currentFrame.add(mouseTile);
			else if (getMouseClick() == RIGHT_CLICK) currentFrame.remove(mouseTile);
		} finally {
			swapLock.unlock();
		}
		
		mouseTilePrev = mouseTile;
	}

	@Override
	public void mouseDragged() {
		double mx = getMouseWorldX();
		double my = getMouseWorldY();
		mouseTile = toTile(mx, my);
		
		if (mouseTilePrev != null) {
			swapLock.lock();
			try {
				if (getMouseClick() == LEFT_CLICK) {
					forEachLine(mouseTilePrev, mouseTile, t -> currentFrame.add(t));
				} else if (getMouseClick() == RIGHT_CLICK) {
					forEachLine(mouseTilePrev, mouseTile, t -> currentFrame.remove(t));
				}		
			} finally {
				swapLock.unlock();
			}
		}
		
		mouseTilePrev = mouseTile;
	}

	private static void forEachLine(Int2D prev, Int2D tile, Consumer<Int2D> consumer) {
		int dx = (int) Math.floor(tile.x() - prev.x());
		int dy = (int) Math.floor(tile.y() - prev.y());
		
		if (dx == 0 && dy == 0) return;
		
		if (Math.abs(dx) > Math.abs(dy)) {
			int numSteps = Math.abs(dx);
			double x = prev.x();
			double y = prev.y();
			double stepX = Math.signum(dx);
			double stepY = (double) dy / (double) numSteps;
			Int2D.Mutable coord = new Int2D.Mutable();

			consumer.accept(prev);
			for (int i = 1; i < numSteps; i++) {
				x += stepX;	y += stepY;
				coord.x((int) Math.floor(x));
				coord.y((int) Math.floor(y));
				consumer.accept(coord);
			}
			consumer.accept(tile);
		} else {
			int numSteps = Math.abs(dy);
			double x = prev.x();
			double y = prev.y();
			double stepX = (double) dx / (double) numSteps;
			double stepY = Math.signum(dy);
			Int2D.Mutable coord = new Int2D.Mutable();

			consumer.accept(prev);
			for (int i = 1; i < numSteps; i++) {
				x += stepX;	y += stepY;
				coord.x((int) Math.floor(x));
				coord.y((int) Math.floor(y));
				consumer.accept(coord);
			}
			consumer.accept(tile);
		}
		
	}

	@Override
	public void mouseReleased() {}

	@Override
	public void mouseMoved() {}

	@Override
	public void mouseScrolled() {}

	@Override
	public void keyPressed() {}
	
	@Override
	public void keyReleased() {
		final int keycode = getKeyEvent().getKeyCode();
		
		switch (keycode) {
			case KeyEvent.VK_G:
				drawGrid = !drawGrid;
				System.out.println("Draw grid: "+(drawGrid ? "on" : "off"));
			break;
			case KeyEvent.VK_C:
				currentFrame.clear();
				nextFrame.clear();
				System.out.println("All cells cleared");
			break;
			case KeyEvent.VK_D:
				dotDisplay = !dotDisplay;
				System.out.println("Dot display: "+(dotDisplay ? "on" : "off"));
			break;
			case KeyEvent.VK_L:
				keepGoing = !keepGoing;
				System.out.println("Looping: "+(keepGoing ? "on" : "off"));
			break;
			case KeyEvent.VK_I:
				if (!oneLoop) System.out.println("One iteration queued");
				oneLoop = true;
			break;
			case KeyEvent.VK_H:
				displayUI = !displayUI;
				System.out.println("UI display: "+(displayUI ? "on" : "off"));
			break;
			case KeyEvent.VK_P:
				phosphor = !phosphor;
				System.out.println("Phosphor: "+(phosphor ? "on" : "off"));
			break;
			case KeyEvent.VK_A:
				antialias = !antialias;
				System.out.println("Antialias: "+(antialias ? "on" : "off"));
			break;
		}
	}

	@Override
	public void keyTyped() {}
	
	private class ConwayIterationTask extends Task {

		public ConwayIterationTask(String name) {
			super(name);
		}

		@Override
		public void work() {
			
			List<ConwayChunkTask> tasks = new ArrayList<>();
			Iterator<Int2D> chunkIter = currentFrame.chunkIterator();
			while (chunkIter.hasNext()) {
				Int2D chunkCoord = chunkIter.next();
				ConwayChunkTask task = new ConwayChunkTask(name, chunkCoord);
				taskManager.submit(task);
				tasks.add(task);
			}
			
			this.block(tasks, this::finish);
		}

		public void finish() {
			
			swapLock.lock();
			try {
				// Swap frames
				Int2DSet swap = currentFrame;
				currentFrame = nextFrame;
				nextFrame = swap;
				
				nextFrame.clear();
			} finally {
				swapLock.unlock();
			}
			
			set(null);
		}
		
	}
	
	private class ConwayChunkTask extends Task {

		private Int2D chunkCoord;
		private int chunkValue;
		
		public ConwayChunkTask(String name, Int2D chunkCoord) {
			super(name+" ("+chunkCoord+")");
			this.chunkCoord = chunkCoord;
		}

		@Override
		public void work() {
			
			chunkValue = currentFrame.getChunk(chunkCoord);
			Int2DRange range = new Int2DRange(chunkCoord.x(), chunkCoord.y(), chunkCoord.x()+7, chunkCoord.y()+3).grow(2);
			
			for (Int2D coord : range.getAllMutable()) {
				if (range.onBorder(coord, 1)) continue;
				
				boolean self = currentFrame.contains(coord.x(), coord.y());
				
				int neighbors = 0;
				if (range.onBorder(coord, 3)) {
					if (currentFrame.contains(coord.x()-1, coord.y()-1)) neighbors++;
					if (currentFrame.contains(coord.x()+0, coord.y()-1)) neighbors++;
					if (currentFrame.contains(coord.x()+1, coord.y()-1)) neighbors++;
					if (currentFrame.contains(coord.x()-1, coord.y()+0)) neighbors++;
					if (currentFrame.contains(coord.x()+1, coord.y()+0)) neighbors++;
					if (currentFrame.contains(coord.x()-1, coord.y()+1)) neighbors++;
					if (currentFrame.contains(coord.x()+0, coord.y()+1)) neighbors++;
					if (currentFrame.contains(coord.x()+1, coord.y()+1)) neighbors++;
				} else {
					if (fastCheck(coord.x()-1, coord.y()-1)) neighbors++;
					if (fastCheck(coord.x()+0, coord.y()-1)) neighbors++;
					if (fastCheck(coord.x()+1, coord.y()-1)) neighbors++;
					if (fastCheck(coord.x()-1, coord.y()+0)) neighbors++;
					if (fastCheck(coord.x()+1, coord.y()+0)) neighbors++;
					if (fastCheck(coord.x()-1, coord.y()+1)) neighbors++;
					if (fastCheck(coord.x()+0, coord.y()+1)) neighbors++;
					if (fastCheck(coord.x()+1, coord.y()+1)) neighbors++;
				}
				
				if (self && neighbors >= 2 && neighbors <= 3) nextFrame.add(coord);
				else if (!self && neighbors == 3) nextFrame.add(coord);
			}
			
			set(null);
		}

		public boolean fastCheck(int x, int y) {			
			final int index = ((y & 3) << 3) | (x & 7);
			final int bitOfInterest = (1 << index);
			return (chunkValue & bitOfInterest) != 0;
		}
		
	}
	
}
