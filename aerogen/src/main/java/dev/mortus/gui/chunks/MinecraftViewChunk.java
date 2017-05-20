package dev.mortus.gui.chunks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Random;

import dev.mortus.aerogen.islands.Island;
import dev.mortus.aerogen.regions.Region;
import dev.mortus.util.math.geom.Polygon;
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
					
					double maxEdge = island.getMaxEdgeDistance();
					for (int x = 0; x < island.width(); x++) {
						for (int z = 0; z < island.depth(); z++) {
							if (!island.isIsland(x+island.minX(), z+island.minZ())) continue;
							Color color = new Color(Color.HSBtoRGB(hue, 1, bri * (1.0f - (float)(island.getEdgeDistance(x+island.minX(), z+island.minZ())/maxEdge)) ));
							g.setColor(color);
							g.drawLine(x+island.minX(), z+island.minZ(), x+island.minX(), z+island.minZ());
						}
						Thread.yield();
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
		
		for (Island island : region.getIslands()) {
			if (!island.isComplete()) continue;
			Rect bb = island.getBoundingBox();
			g.draw(bb.getShape2D());
		}
		
		g.setColor(Color.WHITE);
		g.drawString("("+region.getCoord().x+", "+region.getCoord().y+")", (int) centerX, (int) centerY);
		
		g.draw(shape);
		//g.drawRect(minX, minY, (int) loader.getChunkSize(), (int) loader.getChunkSize());
	}

}

