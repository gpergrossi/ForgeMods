package dev.mortus.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.VoronoiBuilder;
import dev.mortus.voronoi.VoronoiWorker;

public class VoronoiTest {

	private static Random r = new Random();
	
	public static void main(String[] args) {
		
		Scanner s = new Scanner(System.in);
		String line = s.nextLine();
		s.close();
		
		// command line letters:
		// v - verbose
		// d - draw an output image
		// g - constrain points to a grid (useful with lots of points)
		// s - small (20 points), 100x100 image
		// m - medium (10,000 points), 5,000x5,000 image
		// l - large (1,000,000 points), 10,000x10,000 image
		// r - relax, multiple r's means multiple relax iterations
		// o - also runs humphrey's voronoi generator for a timing comparison 
		// (Humphrey's will be faster because it is an approximate diagram and only returns an edge list instead of a traversable diagram)
		
		boolean verbose = line.contains("v");
		boolean draw = line.contains("d");
		boolean grid = line.contains("g");
		
		int relax = 0, i = 0;
		while ((i = line.indexOf('r', i)+1) > 0) relax++;
		
		int num = -1;
		double canvasSize = 10000;
		if (line.contains("s")) { num = 20;		 canvasSize = 100;	 }
		if (line.contains("m")) { num = 10000;	 canvasSize = 5000;	 }
		if (line.contains("l")) { num = 1000000; canvasSize = 10000; }
		if (num != -1) {
			test(num, canvasSize, verbose, draw, grid, relax);
			if (line.contains("o")) test2(num, verbose);
			System.exit(0);
		}
		
		if (line.contains("o")) {
			for (num = 1050000; num > 0;  num -= 10000) test2(num, verbose);
			System.exit(0);
		}
		
		for (num = 1050000; num > 0;  num -= 10000) test(num, canvasSize, verbose, false, grid, 0);
		
	}
	
	private static void test(int num, double canvasSize, boolean verbose, boolean draw, boolean useGrid, int relax) {		
		long start = 0, end = 0, duration = 0;
		long update = 0;

		Voronoi voronoi = null;
		Voronoi.DEBUG_FINISH = verbose;
		VoronoiBuilder vb = new VoronoiBuilder(num);
		VoronoiWorker w = null;
				
		int grid = (int) Math.ceil(Math.sqrt(num));
		double gridSize = canvasSize / grid;
		
		if (useGrid && verbose) System.out.println("Points constrained to "+grid+"x"+grid+" grid with tile size "+gridSize);
		
		if (relax < 0) relax = 0;
		for (int re = 0; re <= relax; re++) {
			
			int numAttempts = 0;
			boolean success = false;
			
			while (success == false && numAttempts < 10) {
				numAttempts++;
				if (verbose) System.out.println("GC...");
				System.gc();
				
				if (re == 0) {
					if (verbose) System.out.println("Generating "+num+" points...");
					vb.setBounds(new Rect(0, 0, canvasSize, canvasSize));
					
					if (useGrid) {
						int i = 0;
						createPoints:
						for (int x = 0; x < grid; x++) {
							for (int y = 0; y < grid; y++) {
								double px = x * gridSize + r.nextDouble()*(gridSize-Vec2.EPSILON*8) + Vec2.EPSILON*4;
								double py = y * gridSize + r.nextDouble()*(gridSize-Vec2.EPSILON*8) + Vec2.EPSILON*4;
								vb.addSite(new Vec2(px, py));
								i++;
								if (i >= num) break createPoints;
							}
						}
					} else {
						for (int i = 0; i < num; i++) {
							double px = r.nextDouble()*canvasSize;
							double py = r.nextDouble()*canvasSize;
							vb.addSite(new Vec2(px, py));
						}
					}
				}
				
				try {
					
					if (verbose) System.out.println("Constructing diagram...");
					update = System.currentTimeMillis();
					start = System.nanoTime();
					
					int numResponses = 0;
					int numEventsProcessed = 0;
					w = vb.getBuildWorker();
					while (!w.isDone()) {
						numEventsProcessed += w.doWork(1000);
						if (verbose) {
							numResponses++;
							if (System.currentTimeMillis() - update > 500) {
								System.out.println("Progress: "+w.getProgressEstimate()+" ("+numResponses+" returns, "+numEventsProcessed+" events)");
								update = System.currentTimeMillis();
								numResponses = 0;
								numEventsProcessed = 0;
							}
						}
					}
					
					success = true;			
					
				} catch (RuntimeException rx) {
					System.out.println("FAIL");
					rx.printStackTrace();
				}
			}
			
			voronoi = w.getResult();
			
			if (re+1 <= relax) {
				System.out.println("Relaxing "+(re+1));
				vb = new VoronoiBuilder(num);
				vb.setBounds(new Rect(0, 0, canvasSize, canvasSize));
				for (Site s : voronoi.getSites().values()) {
					vb.addSite(s.getPolygon().getCentroid());
				}
			}
			
		}
		Voronoi.DEBUG_FINISH = false;
		end = System.nanoTime();
		duration = end-start;

		printStats(num, verbose, duration, voronoi);
		
		if (draw) drawDiagram(num, canvasSize, voronoi);
	}

	private static void printStats(int num, boolean verbose, long dur, Voronoi voronoi) {
		double time = dur*0.000000001;
		
		if (!verbose) {
			System.out.println(num+", "+time);
			return;
		}
		
		System.out.println(num+", "+time+"     [sites="+voronoi.getSites().size()+", verts="+voronoi.getVertices().size()+", edges="+voronoi.getEdges().size()+"]");
		System.out.println(Vec2.ALLOCATION_COUNT+" Vec2's allocated");
		
		double edgesPerSite = 0;
		int maxEdges = 0;
		int[] numSitesPerEdgeCount = new int[20];
		for (Site s : voronoi.getSites().values()) {
			int numEdges = s.numEdges();
			edgesPerSite += numEdges;
			if (numEdges > maxEdges) maxEdges = numEdges;
			if (numEdges >= numSitesPerEdgeCount.length) numSitesPerEdgeCount = Arrays.copyOf(numSitesPerEdgeCount, numEdges+1);
			numSitesPerEdgeCount[numEdges]++;
		}
		edgesPerSite /= voronoi.getSites().size();
		
		System.out.println("Average edges per site: "+edgesPerSite);
		System.out.println("Most edges on any site: "+maxEdges);
		for (int i = 0; i < 20; i++) {
			if (numSitesPerEdgeCount[i] == 0) continue;
			System.out.println(i+", "+numSitesPerEdgeCount[i]);
		}
	}

	private static void drawDiagram(int num, double canvasSize, Voronoi v) {
		double maxArea = (canvasSize*canvasSize / num) * 6;
		int padding = (int) Math.ceil(canvasSize*0.05);
		BufferedImage image = new BufferedImage((int)canvasSize+padding*2, (int)canvasSize+padding*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.translate(padding, padding);
		
		for (Site site : v.getSites().values()) {

			Polygon poly = site.getPolygon();
			double area = poly.getArea();
			if (area <= 0 || area > maxArea || Double.isNaN(area)) {
				System.err.println("Skipping site: "+site.getID()+" due to likely error: "+area);
				continue;
			}
			
			// Draw shape
			g2d.setColor(Color.getHSBColor(r.nextFloat(), 1.0f, 0.5f + r.nextFloat()*0.5f));
			Shape polyShape = poly.getShape2D();
			if (polyShape != null) g2d.fill(polyShape);
			
			// Draw centroid
			g2d.setColor(Color.BLACK);
			Vec2 centroid = poly.getCentroid();
			if (centroid != null) {
				Ellipse2D siteCentroid = new Ellipse2D.Double(centroid.x()-1, centroid.y()-1, 2, 2);
				g2d.fill(siteCentroid);
				g2d.setColor(Color.WHITE);
			} else {
				g2d.setColor(Color.BLACK);
			}
			
			// Draw original point
			Ellipse2D sitePt = new Ellipse2D.Double(site.getX()-1, site.getY()-1, 2, 2);
			g2d.fill(sitePt);
		}
		
		try {
			ImageIO.write(image, "PNG", new File("output.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test2(int num, boolean verbose) {
		be.humphreys.simplevoronoi.Voronoi vor = new be.humphreys.simplevoronoi.Voronoi(0.0001);
		boolean success = false;
		long start = 0, end = 0;
		
		while (success == false) {
			System.gc();
			try {
				if (verbose) System.out.println("Generating "+num+" points...");
				double[] xValuesIn = new double[num];
				double[] yValuesIn = new double[num];
				for (int i = 0; i < num; i++) {
					xValuesIn[i] = r.nextDouble()*10000.0;
					yValuesIn[i] = r.nextDouble()*10000.0;
				}

				if (verbose) System.out.println("Constructing diagram...");
				start = System.nanoTime();
				vor.generateVoronoi(xValuesIn, yValuesIn, 0, 10000.0, 0, 10000.0);
				end = System.nanoTime();
				success = true;
			} catch (RuntimeException re) {
				System.out.println("FAIL");
			}
		}

		long dur = end-start;
		double time = dur*0.000000001;
		System.out.println(num+", "+time);
	}
	
}
