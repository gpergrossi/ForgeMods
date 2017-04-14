package dev.mortus.aerogen;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import dev.mortus.chunks.Chunk;
import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.func2d.Function2D;
import dev.mortus.util.math.func2d.SimplexNoise2D;

public class SimulationChunk extends Chunk {

	public static long seed = 8964591453215L;
	public static Function2D noise;
	public static Function2D noise2;
	
	static {
		Random r = new Random(seed);
		noise = new FractalNoise2D(r.nextLong(), 1.0/4096.0, 4, 0.5);
		noise2 = new SimplexNoise2D(r.nextLong(), 1.0/512.0);
	}
	
	Rectangle2D.Double bounds;
	Ellipse2D.Double dot;
	Point2D.Double center;
	double chunkSize;
	
	boolean dotBool = false;
	int imgSize;
	BufferedImage img;
	
	public SimulationChunk(int chunkX, int chunkY, double chunkSize) {
		super(chunkX, chunkY);
		this.chunkSize = chunkSize;
		
		this.imgSize = (int) (chunkSize / 64.0);
		this.img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
	}
	
	public long getSeed() {
		Random r = new Random(seed);
		long x = (chunkX * r.nextInt(Integer.MAX_VALUE)) % Integer.MAX_VALUE;
		long y = (chunkY * r.nextInt(Integer.MAX_VALUE)) % Integer.MAX_VALUE;
		
		r.setSeed(x ^ (y << 32) ^ r.nextLong());
		return r.nextLong();			
	}
	
	public double getNoise(double x, double y) {
		double n0 = noise.getValue(x, y);
		n0 = ((n0 + 1.0) / 2.0);
		
		//double n1 = noise2.getValue(x, y);
		
		return Math.pow(n0, 3.0); //Math.abs(n1) * ((n0 + 1.0) / 2.0);
	}

	@Override
	public void load() {
		Random r = new Random(getSeed());
		
		bounds = new Rectangle2D.Double(chunkX*chunkSize, chunkY*chunkSize, chunkSize, chunkSize);
		double x = r.nextDouble()*(chunkSize-10) + chunkX*chunkSize + 5;
		double y = r.nextDouble()*(chunkSize-10) + chunkY*chunkSize + 5;
		dot = new Ellipse2D.Double(x-5, y-5, 10, 10);
		center = new Point2D.Double(chunkX*chunkSize+chunkSize/2, chunkY*chunkSize+chunkSize/2);
		
//		double step = chunkSize / (double) imgSize;
		
//		int[] argb = new int[imgSize * imgSize];
//		for (int yi = 0; yi < imgSize; yi++) {
//			for (int xi = 0; xi < imgSize; xi++) {
//				int ind = yi * imgSize + xi;
//				double val = getNoise(chunkX*chunkSize + step*xi, chunkY*chunkSize + step*yi);
//				int vali = (int) (255*val);
//				argb[ind] = vali | (vali << 8) | (vali << 16) | 0xFF000000;
//			}
//		}
//		img.setRGB(0, 0, imgSize, imgSize, argb, 0, imgSize);
		
		double val = getNoise(dot.getCenterX(), dot.getCenterY());
		if (r.nextDouble() < val) dotBool = true;
		
		try {
			Thread.sleep(SimulationFrame.LOADING_TIME);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unload() {
		try {
			Thread.sleep(SimulationFrame.UNLOADING_TIME);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		
//		g.drawImage(img, (int) (chunkX*chunkSize), (int) (chunkY*chunkSize), (int) (chunkSize), (int) (chunkSize), Color.BLACK, null);
		//g.draw(bounds);
		if (dotBool) g.draw(dot);
		//String name = this.toString();
		//g.drawString(name, (int) (center.x-name.length()*3), (int) (center.y+5));
	}

}
