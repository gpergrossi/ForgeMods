package dev.mortus.aerogen;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import dev.mortus.chunks.Chunk;

public class SimulationChunk extends Chunk {

	Rectangle2D.Double bounds;
	Ellipse2D.Double dot;
	Point2D.Double center;
	double chunkSize;
	
	public SimulationChunk(int chunkX, int chunkY, double chunkSize) {
		super(chunkX, chunkY);
		this.chunkSize = chunkSize;
	}

	@Override
	public void load() {
		bounds = new Rectangle2D.Double(chunkX*chunkSize, chunkY*chunkSize, chunkSize, chunkSize);
		double x = Math.random()*(chunkSize-10) + chunkX*chunkSize+5;
		double y = Math.random()*(chunkSize-10) + chunkY*chunkSize+5;
		dot = new Ellipse2D.Double(x-5, y-5, 10, 10);
		center = new Point2D.Double(chunkX*chunkSize+chunkSize/2, chunkY*chunkSize+chunkSize/2);
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
		g.draw(bounds);
		g.draw(dot);
		String name = this.toString();
		g.drawString(name, (int) (center.x-name.length()*3), (int) (center.y+5));
	}

}
