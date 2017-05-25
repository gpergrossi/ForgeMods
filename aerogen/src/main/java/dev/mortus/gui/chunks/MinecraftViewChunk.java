package dev.mortus.gui.chunks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.islands.IslandShape;
import dev.mortus.aerogen.world.islands.River;
import dev.mortus.aerogen.world.islands.RiverCell;
import dev.mortus.aerogen.world.islands.RiverWaterfall;
import dev.mortus.aerogen.world.regions.Region;
import dev.mortus.util.data.Int2D;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;

public class MinecraftViewChunk extends View2DChunk<MinecraftViewChunk> {
	
	Region region;
	Rect bounds;
	
	Color color;
	Shape shape;
	
	Thread drawThread;
	boolean alive;
	int imageX, imageY;
	BufferedImage islands;
	
	double centerX, centerY;
	
	public MinecraftViewChunk(ChunkManager<MinecraftViewChunk> manager, int chunkX, int chunkY) {
		super(manager, chunkX, chunkY);
	}
	
	@Override
	public MinecraftViewChunkLoader getChunkLoader() {
		return (MinecraftViewChunkLoader) loader;
	}

	@Override
	public void load() {
		region = getChunkLoader().regionManager.getRegion(chunkX, chunkY);
		Vec2 center = region.getRegionPolygon().getCentroid();
		centerX = center.x();
		centerY = center.y();
		
		int chunkSize = (int) loader.getChunkSize();
		bounds = new Rect(chunkX * chunkSize, chunkY * chunkSize, chunkSize, chunkSize);
		
		Polygon poly = region.getRegionPolygon();
		shape = poly.getShape2D();

		Random r = new Random(region.getSeed());
		color = new Color(Color.HSBtoRGB(r.nextFloat(), 1, 1));
		
		Rect bounds = region.getRegionPolygon().getBounds();
		imageX = (int) bounds.minX();
		imageY = (int) bounds.minY();
		
		drawThread = new Thread(new Runnable() {
			public void run() {
				islands = new BufferedImage((int) bounds.width(), (int) bounds.height(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = islands.createGraphics();
				g.translate(-bounds.minX(), -bounds.minY());
				for (Island island : region.getIslands()) {					
					island.build();
					
					r.setSeed(island.getSeed());
					float hue = r.nextFloat();
					float bri = 1.0f;
					
					IslandShape shape = island.getShape();

					g.setColor(new Color(Color.HSBtoRGB(hue, 1, 1)));
					g.drawString(""+island.getAltitudeLayer(), (shape.minX()+shape.maxX())/2, (shape.minZ()+shape.maxZ())/2);
					
					float maxEdge = shape.getMaxEdgeDistance();
					for (Int2D.WithIndex tile : shape.range.getAllMutable()) {
						if (tile.index % 100 == 0) Thread.yield();
						if (!shape.contains(tile.x, tile.y)) continue;
						
						float edge = shape.getEdgeDistance(tile.x, tile.y);
						
						// Outlines guaranteed, interior speckled
						if (edge > 1 && Math.random() < 0.9) continue;
						
						g.setColor(new Color(Color.HSBtoRGB(hue, 1, bri * (1.0f - edge/maxEdge))));
						g.drawLine(tile.x, tile.y, tile.x, tile.y);
					}
					if (!alive) break;
				}
				g.dispose();
			}
		});
		alive = true;
		drawThread.start();
	}

	@Override
	public void unload() {
		alive = false;
		try {
			drawThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		islands = null;
		region.release();
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
				
//		int minX = (int) bounds.minX();
//		int minY = (int) bounds.minY();
//		int maxX = (int) bounds.maxX();
//		int maxY = (int) bounds.maxY();
//
//		g.setColor(new Color(255, 255, 255, 32));
//		for (int i = 0; i < getChunkManager().chunkSize/16; i++) {
//			g.drawLine(minX + i*16, minY, minX + i*16, maxY);
//			g.drawLine(minX, minY + i*16, maxX, minY + i*16);
//		}

		if (islands != null)
			g.drawImage(islands, imageX, imageY, null);
		
//		for (Island island : region.getIslands()) {
//			if (!island.isComplete()) continue;
//			Rect bb = island.getShape().getBoundingBox();
//			g.draw(bb.getShape2D());
//		}
		
		float hue = 0;
		g.setColor(Color.WHITE);
		for (River river : region.getRivers()) {
			for (RiverCell irc : river.getCells()) {
				for (LineSeg seg : irc.getRiverCurve()) {
					hue += 0.166f;
					g.setColor(new Color(Color.HSBtoRGB(hue, 1, 1)));
					g.drawLine((int) seg.getStartX(), (int) seg.getStartY(), (int) seg.getEndX(), (int) seg.getEndY());
					
					double ax = seg.getStartY() - seg.getEndY();
					double ay = seg.getEndX() - seg.getStartX();
					double l = Math.sqrt(ax*ax+ay*ay);
					ax /= (l/2.0); ay /= (l/2.0);
					
					g.drawLine((int) seg.getEndX(), (int) seg.getEndY(), (int) (seg.getEndX()+ax), (int) (seg.getEndY()+ay));
				}
			}
			g.setColor(Color.WHITE);
			for (RiverWaterfall waterfall : river.getWaterfalls()) {
				LineSeg seg = waterfall.getEdge();
				g.drawLine((int) seg.getStartX(), (int) seg.getStartY(), (int) seg.getEndX(), (int) seg.getEndY());
				
				Ray ray = waterfall.getLocation();
				seg = ray.toSegment(0.0, 5.0);
				g.drawLine((int) seg.getStartX(), (int) seg.getStartY(), (int) seg.getEndX(), (int) seg.getEndY());
			}
		}
		
		g.setColor(Color.WHITE);
		g.drawString("("+region.getCoord().x+", "+region.getCoord().y+")", (int) centerX, (int) centerY);
		
		g.draw(shape);
		//g.drawRect(minX, minY, (int) loader.getChunkSize(), (int) loader.getChunkSize());
	}

}

