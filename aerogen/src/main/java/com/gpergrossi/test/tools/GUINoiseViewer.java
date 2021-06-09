package com.gpergrossi.test.tools;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import com.gpergrossi.util.math.func2d.CombineOperation;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.IFunction2D;
import com.gpergrossi.util.math.func2d.RemapOperation;
import com.gpergrossi.viewframe.View;
import com.gpergrossi.viewframe.ViewerFrame;
import com.gpergrossi.viewframe.chunks.NoiseViewerChunk;
import com.gpergrossi.viewframe.chunks.View2DChunkLoader;
import com.gpergrossi.viewframe.chunks.View2DChunkManager;

public class GUINoiseViewer extends View {
	
	public static ViewerFrame frame;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
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
	protected double getZoomPerClick() {
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
		DRAG_MOUSE_BUTTON = LEFT_CLICK;
	}	
	
	public static IFunction2D noise;
	
	static {
		Random random = new Random();

//		IFunction2D surfaceNoise = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(16).withOctaves(4).withRange(-15.0, 15.0).build();
//		IFunction2D brycePillarNoise = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(4).withOctaves(4).withRange(-15.0, 15.0).build();
//		brycePillarNoise = new CombineOperation(surfaceNoise, brycePillarNoise, (a,b) -> Math.min(Math.abs(a), b));
//
//		IFunction2D bryceRoofNoise = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(512).build();
//		bryceRoofNoise = new RemapOperation(bryceRoofNoise, v -> Math.ceil(Math.abs(v) * 50.0D) + 14.0D);
//
//		brycePillarNoise = new CombineOperation(brycePillarNoise, bryceRoofNoise, (a,b) -> ((a <= 0) ? 0 : Math.min(a*a*2.5, b)+64.0));
//		
//		noise = new RemapOperation(brycePillarNoise, t ->  ((t < 64) ? 0 : (t-64.0)/64.0));

		final double breakpoint = 0.7;
		final double erodeRange = 0.05;
		IFunction2D surface = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(64).withOctaves(4, 0.3).withRange(0, 1).build();
		IFunction2D erosion = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(2).withOctaves(2, 0.5).withRange(0, 1).build();
		erosion = new CombineOperation(surface, erosion, (a, b) -> {
			if (a < breakpoint) return 0;
			if (a > breakpoint + erodeRange) return 0;
			return (1 - Math.abs(a - (breakpoint+erodeRange/2)) / (erodeRange/2)) * b;
		});
		surface = new CombineOperation(surface, erosion, (a,b) -> a * (1-b*0.02));
		surface = new RemapOperation(surface, v -> {
			if (v < 0) return 0;
			if (v < breakpoint) return v * (0.3/breakpoint);
			int xShift = (int) (40 * breakpoint) - 1;
			return (40*v-xShift) / ((40*v-xShift) + 1);
		});
		noise = surface;
	}
	
//	Function2D noise = new SandDunes(9809414L); 
//	Function2D noise = new RemapOperation(new FractalNoise2D(1.0/256.0, 4), t->t*0.5+0.5);
	
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
	public void mousePressed() {}

	@Override
	public void mouseDragged() {}

	@Override
	public void mouseReleased() {}

	@Override
	public void mouseMoved() {}

	@Override
	public void mouseScrolled() {}

	@Override
	public void keyPressed() {}

	@Override
	public void keyReleased() {}

	@Override
	public void keyTyped() {}
	
}
