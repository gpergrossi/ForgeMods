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
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.tree.TreeNode;

public class View {
	
	public static final int LEFT_CLICK = 1;
	public static final int MIDDLE_CLICK = 2;
	public static final int RIGHT_CLICK = 4;
	
	double x, y;
	double velocityX, velocityY;
	double halfWidth, halfHeight;
	double printTime;
	
	boolean useChunkLoader = false;
	
	Voronoi voronoi;
	
	ChunkLoader<SimulationChunk> chunkLoader;
	ChunkManager<SimulationChunk> chunkManager;

	public View (double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.halfWidth = width/2.0;
		this.halfHeight = height/2.0;
		
		voronoi = new Voronoi();
		
		if (useChunkLoader) {
			chunkLoader = new SimulationChunkLoader(100);
			chunkManager = new ChunkManager<SimulationChunk>(chunkLoader, 100, SimulationFrame.NUM_WORKER_THREADS);
		}
	}

	double seconds;
	double radiansPerDegree = (Math.PI/180.0);
	
	public void update(double secondsPassed) {
		seconds += secondsPassed;
		this.x += this.velocityX*secondsPassed;
		this.y += this.velocityY*secondsPassed;
		double decay = Math.pow(0.5, secondsPassed);
		this.velocityX *= decay;
		this.velocityY *= decay;

		if (useChunkLoader) {
			printTime += secondsPassed;
			if (printTime > 1) {
				printTime -= 1;
				if (useChunkLoader) System.out.println("Loaded chunks = "+chunkManager.getNumLoaded());
			}
		}
	}
	
	public void drawFrame(Graphics2D g2d, AffineTransform viewTransform) {		
		AffineTransform before = g2d.getTransform();
		
		g2d.setTransform(viewTransform);
		
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
		

		Ellipse2D.Double ellipse2 = new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0);
		if (ellipse2.contains(mX, mY)) g2d.setColor(Color.YELLOW);
		g2d.fill(ellipse2);
		g2d.setColor(Color.WHITE);
		
		if (useChunkLoader) { 
			Rectangle2D.Double bounds = new Rectangle2D.Double(x-halfWidth, y-halfHeight, halfWidth*2, halfHeight*2);
			chunkManager.update(bounds);
			chunkManager.draw(g2d);
		}
		
		voronoi.draw(g2d);
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo(100, 0);
		path.lineTo(0, 2);
		path.lineTo(0, -2);
		path.closePath();
		
		g2d.rotate(seconds*(Math.PI / 30.0));
		g2d.fill(path);
		
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
			mDX = 0; mVelX = 0; mX = px;
			mDY = 0; mVelY = 0; mY = py;
			startPX = px; startPY = py;
			startViewX = x; startViewY = y;
			panning = true;
		}
		//System.out.println("Click pressed: "+click);
	}

	public void mouseDragged(int click, double px, double py) {
		if (click == View.LEFT_CLICK) {
			mDX = (mX - px); mVelX = (mVelX*4 + mDX) / 5.0; mX = px;
			mDY = (mY - py); mVelY = (mVelY*4 + mDY) / 5.0; mY = py;
			this.velocityX = 0;
			this.velocityY = 0;
			
			this.x = startViewX + startPX - px;
			this.y = startViewY + startPY - py;
			//System.out.println((px-startPX)+", "+(py-startPY));
		}
		//System.out.println("Click dragged: "+click);
	}

	public void mouseReleased(int click, double px, double py) {
		if (click == View.LEFT_CLICK) {
			boolean doThrow = (mDX * mDX + mDY * mDY > 5); 
			mVelX = (mVelX*4 + mDX) / 5.0; mX = px;
			mVelY = (mVelY*4 + mDY) / 5.0; mY = py;
			this.x = startViewX + startPX - px;
			this.y = startViewY + startPY - py;
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
			for (Site site : voronoi.getSites()) {
				Point2D point = site.getPos();
				if (clickP.distance(point) < 2) {
					voronoi.removeSite(site);
					removed = true;
					break;
				}
			}
			if (!removed) {
				voronoi.addSite(clickP);
			}
		}
		//System.out.println("Click released: "+click);
	}
	
	public void mouseMoved(double px, double py) {
		mDX = (mX - py); mVelX = (mVelX*4 + mDX) / 5.0; mX = px;
		mDY = (mY - py); mVelY = (mVelY*4 + mDY) / 5.0; mY = py;
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
			voronoi.buildStep();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			voronoi.build();
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			voronoi.debugAdvanceSweepline(-1);
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			voronoi.debugAdvanceSweepline(+1);
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			voronoi.stepBack();
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			System.out.println("Saving");
			try {
				voronoi.savePoints();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_L) {
			System.out.println("Loading");
			try {
				voronoi.loadPoints();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_C) {
			System.out.println("Clearing");
			TreeNode.IDCounter = 0;
			Site.IDCounter = 0;
			voronoi.clearSites();
		}
	}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
	
}
