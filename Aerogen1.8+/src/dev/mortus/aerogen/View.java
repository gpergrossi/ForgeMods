package dev.mortus.aerogen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import dev.mortus.chunks.ChunkLoader;
import dev.mortus.chunks.ChunkManager;
import dev.mortus.util.data.LinkedBinaryNode;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.VoronoiBuilder;
import dev.mortus.voronoi.internal.WorkerDebug;

public class View {
	
	public static final int LEFT_CLICK = 1;
	public static final int MIDDLE_CLICK = 2;
	public static final int RIGHT_CLICK = 4;
	
	ViewerPane pane; // access the recording feature
	
	double viewX, viewY;
	double velocityX, velocityY;
	double halfWidth, halfHeight;
	double printTime;
	
	double slowZoom = 1.0;
	
	boolean useChunkLoader = false;
	
	VoronoiBuilder voronoiBuilder;
	WorkerDebug voronoiWorker;
	
	ChunkLoader<SimulationChunk> chunkLoader;
	ChunkManager<SimulationChunk> chunkManager;

	public View (double x, double y, double width, double height) {
		this.viewX = x;
		this.viewY = y;
		this.halfWidth = width/2.0;
		this.halfHeight = height/2.0;
		
		voronoiBuilder = new VoronoiBuilder();
		
		if (useChunkLoader) {
			chunkLoader = new SimulationChunkLoader(100);
			chunkManager = new ChunkManager<SimulationChunk>(chunkLoader, 100, SimulationFrame.NUM_WORKER_THREADS);
		}
	}

	double seconds;
	double radiansPerDegree = (Math.PI/180.0);
	
	public void update(double secondsPassed) {
		seconds += secondsPassed;
		this.viewX += this.velocityX*secondsPassed;
		this.viewY += this.velocityY*secondsPassed;
		
		double decay = Math.pow(0.5, secondsPassed);
		this.velocityX *= decay;
		this.velocityY *= decay;
		
	    decay = Math.pow(0.5, secondsPassed*60.0);
		this.mVelX *= decay;
		this.mVelY *= decay;
		
		halfWidth *= Math.pow(slowZoom, secondsPassed);
		halfHeight *= Math.pow(slowZoom, secondsPassed);
		
		if (useChunkLoader) {
			printTime += secondsPassed;
			if (printTime > 1) {
				printTime -= 1;
				if (useChunkLoader) System.out.println("Loaded chunks = "+chunkManager.getNumLoaded());
			}
		}
	}
	
	AffineTransform identity = new AffineTransform();
	
	public void drawFrame(Graphics2D g2d, AffineTransform viewTransform) {		
		AffineTransform before = g2d.getTransform();
		
		g2d.setTransform(viewTransform);

		// Clock dots
		g2d.setColor(Color.WHITE);
		for (int i = 0; i < 60; i++) {
			double an = i * (Math.PI / 30.0);
			double ew = 5, eh = 5;
			double ex = Math.cos(an)*100-ew/2;
			double ey = Math.sin(an)*100-eh/2;
			Ellipse2D ellipse = new Ellipse2D.Double(ex, ey, ew, eh);

			if (ellipse.contains(mX, mY)) g2d.setColor(Color.YELLOW);
			g2d.fill(ellipse);
			g2d.setColor(Color.WHITE);
		}
		
		// Clock center
		Ellipse2D.Double ellipse2 = new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0);
		if (ellipse2.contains(mX, mY)) g2d.setColor(Color.YELLOW);
		g2d.fill(ellipse2);
		g2d.setColor(Color.WHITE);
		
		// Clock hand
		Path2D.Double path = new Path2D.Double();
		path.moveTo(100, 0);
		path.lineTo(0, 2);
		path.lineTo(0, -2);
		path.closePath();
		g2d.rotate(seconds*(Math.PI / 30.0));
		g2d.fill(path);
		g2d.setTransform(viewTransform);
		
		if (useChunkLoader) { 
			Rectangle2D.Double bounds = new Rectangle2D.Double(viewX-halfWidth, viewY-halfHeight, halfWidth*2, halfHeight*2);
			chunkManager.update(bounds);
			chunkManager.draw(g2d);
		}
		
		if (voronoiWorker != null) voronoiWorker.debugDraw(g2d);
		else {
			for (Vec2 site : voronoiBuilder.getSites()) {
				Ellipse2D ellipse = new Ellipse2D.Double(site.x-1, site.y-1, 2, 2);
				g2d.fill(ellipse);
			}
		}
		
//		// Mouse velocity trail
//		g2d.setColor(Color.WHITE);
//		Line2D mVel = new Line2D.Double(mX, mY, mX+mVelX, mY+mVelY);
//		g2d.draw(mVel);
		
		g2d.setTransform(before);
		
		if (useChunkLoader) {
			g2d.clearRect(0, 0, 200, 30);
			g2d.drawString("Chunks loaded: "+chunkManager.getNumLoaded(), 10, 20);
		}
	}

	double startPX, startPY;
	double startViewX, startViewY;
	boolean panning = false;
	
	double mX, mY;
	double mDX, mDY;
	double mVelX, mVelY;
	
	public void mousePressed(int click, double px, double py) {
		if (click == View.LEFT_CLICK) {
			mDX = 0; mVelX = 0; mX = px; velocityX = 0;
			mDY = 0; mVelY = 0; mY = py; velocityY = 0;
			startPX = px; startPY = py;
			startViewX = viewX; startViewY = viewY;
			panning = true;
		}
		//System.out.println("Click pressed: "+click);
	}

	public void mouseDragged(int click, double px, double py) {
		if (click == View.LEFT_CLICK) {
			mDX = (mX - px); 
			mDY = (mY - py); 
			mVelX += mDX;
			mVelY += mDY; 
			mX = px;
			mY = py;
			this.velocityX = 0;
			this.velocityY = 0;
			
			this.viewX = startViewX + startPX - px;
			this.viewY = startViewY + startPY - py;
		}
		//System.out.println("Click dragged: "+click);
	}

	public void mouseReleased(int click, double px, double py) {
		if (click == View.LEFT_CLICK) {
			mouseDragged(View.LEFT_CLICK, px, py);
			boolean doThrow = (mVelX * mVelX + mVelX * mVelX > 3.0*540.0/halfWidth);
			if (doThrow) {
				this.velocityX = mVelX*60;
				this.velocityY = mVelY*60;
			} else {
				this.velocityX = 0;
				this.velocityY = 0;
			}
			panning = false;
		} else if (click == View.RIGHT_CLICK) {
			Point2D clickP = new Point2D.Double(px, py);
			int x = (int) Math.floor((clickP.getX()+4) / 8);
			int y = (int) Math.floor((clickP.getY()+4) / 8);
			clickP = new Point2D.Double(x*8, y*8);
			
			boolean removed = false;
			for (Vec2 site : voronoiBuilder.getSites()) {
				Point2D point = site.toPoint2D();
				if (clickP.distance(point) < 8) {
					voronoiBuilder.removeSite(site);
					removed = true;
					break;
				}
			}
			if (!removed) {
				voronoiBuilder.addSite(new Vec2(clickP));
			}
			voronoiWorker = null;
		}
		//System.out.println("Click released: "+click);
	}
	
	public void mouseMoved(double px, double py) {
		mDX = (mX - px); 
		mDY = (mY - py); 
		mVelX += mDX; 
		mVelY += mDY; 
		mX = px;
		mY = py;
	}

	public void scroll(double clicks) {
		if (panning) return;
		double multiply = Math.pow(1.2, clicks);
		halfWidth *= multiply;
		halfHeight *= multiply;
	}

	public void setAspect(double aspect) {
		this.halfHeight = this.halfWidth / aspect;
	}

	public void start() {
		if (useChunkLoader) chunkManager.start();
	}

	public void stop() {
		if (useChunkLoader) chunkManager.stop();
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			if (voronoiWorker == null) voronoiWorker = voronoiBuilder.getBuildWorkerDebug();
			else voronoiWorker.doWork();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (voronoiWorker == null) voronoiWorker = voronoiBuilder.getBuildWorkerDebug();
			while (!voronoiWorker.isDone()) {
				voronoiWorker.doWork();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (voronoiWorker != null) { 
				voronoiWorker.debugAdvanceSweepline(-1);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (voronoiWorker != null) { 
				voronoiWorker.debugAdvanceSweepline(+1);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (voronoiWorker != null) { 
				voronoiWorker.stepBack();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			System.out.println("Saving");
			try {
				voronoiBuilder.savePoints();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_L) {
			System.out.println("Loading");
			try {
				voronoiBuilder.loadPoints();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_C) {
			System.out.println("Clearing");
			LinkedBinaryNode.IDCounter = 0;
			voronoiBuilder.clearSites();
		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			slowZoom = 9.0/14.0;
		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			slowZoom = 14.0/9.0;
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			if (pane != null) {
				if (!pane.isRecording()) pane.startRecording();
				else pane.stopRecording();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_SLASH) {
			for (double d = Math.PI/2.0; d < Math.PI*1.0; d += Math.PI/17.3) {
				voronoiBuilder.addSite(new Vec2(Math.cos(d)*d*100, Math.sin(d)*d*100));
				voronoiBuilder.addSite(new Vec2(Math.cos(d+Math.PI*2.0/3.0)*d*100, Math.sin(d+Math.PI*2.0/3.0)*d*100));
				voronoiBuilder.addSite(new Vec2(Math.cos(d-Math.PI*2.0/3.0)*d*100, Math.sin(d-Math.PI*2.0/3.0)*d*100));
			}
		}
	}
	
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			slowZoom = 1.00;
		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			slowZoom = 1.00;
		}
	}
	
	public void keyTyped(KeyEvent e) {}
	
}
