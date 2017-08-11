package dev.mortus.test;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import dev.mortus.gui.View;
import dev.mortus.gui.ViewerFrame;
import dev.mortus.gui.chunks.NoiseViewerChunk;
import dev.mortus.gui.chunks.View2DChunkLoader;
import dev.mortus.gui.chunks.View2DChunkManager;

public class GUINoiseViewer extends View {
	
	public static ViewerFrame frame;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.setProperty("sun.java2d.opengl", "true");
					frame = new ViewerFrame(new GUINoiseViewer(0, 0, 1024, 768));
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	protected double getMinZoom() {
		return 0.5;
	}

	@Override
	protected double getMaxZoom() {
		return 8.0;
	}
	
	@Override
	protected double getZoomMultiplier() {
		return 2;
	}
	
	@Override
	protected void renderSettings(Graphics2D g2d) {
		// Background Black
		g2d.setBackground(new Color(0,0,0,255));
	}
		
	View2DChunkLoader<NoiseViewerChunk> chunkLoader;
	View2DChunkManager<NoiseViewerChunk> chunkManager;
	
	double seconds;
	double printTime;
	double radiansPerDegree = (Math.PI/180.0);

	public GUINoiseViewer(double x, double y, double width, double height) {
		super (x, y, width, height);
	}	
	
	@Override
	public void init() {
		chunkLoader = new View2DChunkLoader<NoiseViewerChunk>(8964591453215L, 16, NoiseViewerChunk::constructor);
		chunkManager = new View2DChunkManager<NoiseViewerChunk>(chunkLoader, 3);
	}
	
	@Override
	public void start() {
		chunkManager.start();
	}

	@Override
	public void stop() {
		chunkManager.stop();
	}

	@Override
	public void update(double secondsPassed) {
		seconds += secondsPassed;

		printTime += secondsPassed;
		if (printTime > 1) {
			printTime -= 1;
			frame.setTitle("FPS = "+String.format("%.2f", getFPS()));
		}
	}
	
	@Override
	public void drawWorld(Graphics2D g2d) {		
		Rectangle2D bounds = this.getViewWorldBounds();
		chunkManager.setView(bounds);
		chunkManager.update();
		chunkManager.draw(g2d);
		
		// Mouse velocity trail
//		g2d.setColor(Color.WHITE);
//		Line2D mVel = new Line2D.Double(getMouseWorldX(), getMouseWorldY(), getMouseWorldX()-getMouseWorldDX(), getMouseWorldY()-getMouseWorldDY());
//		g2d.draw(mVel);
	}

	@Override
	public void drawOverlayUI(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.clearRect(0, 0, 200, 30);
		g2d.drawString("Chunks loaded: "+chunkManager.getNumLoaded(), 10, 20);
	}

	@Override
	public void mousePressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseScrolled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped() {
		// TODO Auto-generated method stub
		
	}
	
}
