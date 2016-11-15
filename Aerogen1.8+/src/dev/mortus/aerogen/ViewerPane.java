package dev.mortus.aerogen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ViewerPane extends JPanel implements Runnable {

	public String recordingFilePrefix = "recording/frame.";
	public int recordingFrame = 0;
	public int recordingMaxFrames = 9999;
	public boolean recording = false;
	public double recordingFPS = 24.0;
	
	public boolean showFPS = false;
	
	private static final long serialVersionUID = 5091243043686433403L;
	private ComponentListener componentListener
    = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			ViewerPane.this.onResize(e.getComponent().getSize());
		}
	};
	private MouseAdapter mouseListener
	= new MouseAdapter() {
		int click = 0;
		private AffineTransform panTransform = new AffineTransform();
		public void mousePressed(MouseEvent e) {
			calculateViewTransform();
			panTransform.setTransform(deviewTransform);
			Point2D.Double pt = multiply(panTransform, e.getX(), e.getY());
			click |= (1 << e.getButton());
			view.mousePressed(click >> 1, pt.getX(), pt.getY());
		}
		public void mouseDragged(MouseEvent e) {
			calculateViewTransform();
			Point2D.Double pt = multiply(panTransform, e.getX(), e.getY());
			view.mouseDragged(click >> 1, pt.getX(), pt.getY());
		}
		public void mouseReleased(MouseEvent e) {
			calculateViewTransform();
			Point2D.Double pt = multiply(panTransform, e.getX(), e.getY());
			view.mouseReleased(click >> 1, pt.getX(), pt.getY());
			click &= ~(1 << e.getButton());
		}
		public void mouseMoved(MouseEvent e) {
			calculateViewTransform();
			Point2D.Double pt = deproject(e.getX(), e.getY());
			view.mouseMoved(pt.getX(), pt.getY());
		}
	};
	private MouseWheelListener mouseWheelListener
	= new MouseWheelListener() {
		public void mouseWheelMoved(MouseWheelEvent e) {
			view.scroll(e.getPreciseWheelRotation());			
		}
	};
	private KeyAdapter keyListener
	= new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			view.keyPressed(e);
		}
		public void keyReleased(KeyEvent e) {
			view.keyReleased(e);
		}
		public void keyTyped(KeyEvent e) {
			view.keyTyped(e);
		}
	};

	private AffineTransform freshTransform = new AffineTransform();
	private AffineTransform viewTransform = new AffineTransform();
	private AffineTransform deviewTransform = new AffineTransform();
	private BufferedImage buffer, bufferLast;
	private Graphics2D bufferG2D, bufferLastG2D;
	private boolean rebuildBuffers = true;
	private boolean running = false;
	private View view;
	private Thread thread;
	
	public ViewerPane(View view) {
		this.view = view;
		addComponentListener(componentListener);
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseWheelListener);
		addKeyListener(keyListener);
		onResize(new Dimension(100, 100));
		this.setDoubleBuffered(true);
	}

	private void onResize(Dimension size) {
		// We need a new buffers to write to
		buffer = new BufferedImage(
				(int) size.getWidth(), (int) size.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		bufferLast = new BufferedImage(
				(int) size.getWidth(), (int) size.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		rebuildBuffers = true;
		view.setAspect((double) size.width/size.height);
	}
	
	private void dispose() {
		if (bufferG2D != null) bufferG2D.dispose();
		if (bufferLastG2D != null) bufferLastG2D.dispose();
	}
	
	private void renderSettings(Graphics2D g2d) {
		// Anti-aliasing on
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Background Black
		g2d.setBackground(new Color(0,0,0,255));
	}
	
	private void rebuildBuffers() {
		// Dispose if needed
		dispose();
		
		// Create new
		bufferG2D = buffer.createGraphics();
		bufferLastG2D = bufferLast.createGraphics();
		freshTransform.setTransform(bufferG2D.getTransform());
		
		// Set settings
		renderSettings(bufferG2D);
		renderSettings(bufferLastG2D);
		
		// Mark built
		rebuildBuffers = false;
	}

	private void swap() {
		BufferedImage swap = bufferLast;
		bufferLast = buffer;
		buffer = swap;
		Graphics2D swapG2D = bufferLastG2D;
		bufferLastG2D = bufferG2D;
		bufferG2D = swapG2D;
	}
	
	@Override
	public void paint(Graphics g) {
		if (rebuildBuffers) rebuildBuffers();
		
		// Swap buffers
		swap();
		
		// Show old buffer
		g.drawImage(bufferLast, 0, 0, this);
		
		// Recording?
		if (recording) {
			try {
				ImageIO.write(bufferLast, "png", new File(recordingFilePrefix+getFrameNumber()+".png"));
			} catch (IOException e) {
				e.printStackTrace();
				recording = false;
			}
		}
		
		// Render new buffer
		drawFrame(bufferG2D);
	}
	
	private String getFrameNumber() {
		this.recordingFrame++;
		if (recordingFrame > recordingMaxFrames) {
			throw new RuntimeException("recorded too many frames");
		}
		String frame = String.valueOf(this.recordingFrame);
		String max = String.valueOf(recordingMaxFrames);
		while (frame.length() < max.length()) frame = "0"+frame;
		return frame;
	}

	public void start() {
		running = true;
		view.start();
		thread = new Thread(this);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	public void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		view.stop();
		dispose();
	}

	long lastLoop = System.nanoTime();
	long currentLoop = System.nanoTime();
	long nanosPerMilli = 1000000;
	long nanosPerSecond = 1000000000;
	long delta = 1;
	long sleepThreashold = 100 * nanosPerMilli;  // 6 ms
	long yieldThreashold = 100 * nanosPerMilli;  // 2 ms
	
	private void sync(int fps) {
		long nanos = nanosPerSecond/fps;
		do {
			long timeLeft = lastLoop + nanos - currentLoop;
			if (timeLeft > sleepThreashold) {
				long sleepTime = timeLeft-sleepThreashold;
				sleepTime /= nanosPerMilli;
				try {
					Thread.sleep(sleepTime);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			} else if (timeLeft > yieldThreashold) {
				Thread.yield();
			}
			currentLoop = System.nanoTime();
		} while (currentLoop - lastLoop < nanos);
		delta = currentLoop-lastLoop;
		if (delta == 0) delta++;
		lastLoop = currentLoop;
	}
	
	public void run() {
		int printmod = 0;
		while (running) {
			printmod++;
			if (printmod % 60 == 0) {
				double FPS = (double) nanosPerSecond/delta;
				if (showFPS) System.out.println("FPS = "+FPS);
			}
			
			double updateDelta = (double) delta/nanosPerSecond;
			if (recording) updateDelta = 1.0/recordingFPS;
			view.update(updateDelta);
			
			this.repaint();
			
			// Wait for loop
			sync(60);
		}
	}
	
	private void resetTransform(Graphics2D g2d) {
		g2d.setTransform(freshTransform);
	}
	
	private void calculateViewTransform() {
		viewTransform.setTransform(freshTransform);
		viewTransform.scale(getWidth()/(view.halfWidth*2), 
							getHeight()/(view.halfHeight*2));
		viewTransform.translate(-view.x, -view.y);
		viewTransform.translate(view.halfWidth, view.halfHeight);
		
		deviewTransform.setTransform(freshTransform);
		deviewTransform.translate(-view.halfWidth, -view.halfHeight);
		deviewTransform.translate(view.x, view.y);
		deviewTransform.scale((view.halfWidth*2)/getWidth(),
							(view.halfHeight*2)/getHeight());
	}
	
	private void drawFrame(Graphics2D g2d) {
		// Clear frame
		resetTransform(g2d);
		g2d.clearRect(0, 0, getWidth(), getHeight());

		calculateViewTransform();
		
		view.drawFrame(g2d, viewTransform);
	}

	public Point2D.Double project(double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		return (Double) viewTransform.transform(pt, pt);
	}
	
	public Point2D.Double deproject(double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		return (Double) deviewTransform.transform(pt, pt);
	}
	
	public Point2D.Double multiply(AffineTransform xform, double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		return (Double) xform.transform(pt, pt);
	}
	
	public Point2D.Double project(Point2D.Double pt) {
		return (Double) viewTransform.transform(pt, pt);
	}
	
	public Point2D.Double deproject(Point2D.Double pt) {
		return (Double) deviewTransform.transform(pt, pt);
	}

	public boolean isRecording() {
		return recording;
	}

	public void startRecording() {
		this.recording = true;
		this.recordingFrame = 0;
	}

	public void stopRecording() {
		this.recording = false;
	}
}
