package com.gpergrossi.viewframe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandShape;
import com.gpergrossi.util.geom.shapes.Rect;
import com.gpergrossi.util.geom.vectors.Int2D;

public class IslandDebugRender {

	private Island island;
	private BufferedImage image;
	private int imageX, imageY;
	
	private boolean renderBegan;
	private boolean renderStopped;
	
	public IslandDebugRender(Island island) {
		if (!island.isInitialized()) throw new RuntimeException("Island not yet initialized");
		
		this.island = island;
		island.debugRender = this;
		renderBegan = false;
		renderStopped = false;
	}
	
	public int getX() {
		return imageX;
	}
	
	public int getY() {
		return imageY;
	}
	
	public synchronized BufferedImage getImage() {
		if (!renderBegan) {
			renderBegan = true;
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					IslandDebugRender.this.render();
				}
			});
			thread.start();
		}
		return image;
	}
	
	private void render() {
		
		IslandShape shape = island.getShape();
		Rect boundingBox = shape.getBoundingBox();
		
		int paddingForHeightOffset = 10;
		
		this.imageX = (int) boundingBox.minX();
		this.imageY = (int) boundingBox.minY()-paddingForHeightOffset;
		int width = (int) boundingBox.width();
		int height = (int) boundingBox.height()+paddingForHeightOffset;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();
		g.translate(-boundingBox.minX(), -boundingBox.minY()+paddingForHeightOffset);
			
		Random rand = new Random(island.getSeed());
		float hue = rand.nextFloat();
		float sat = 0.5f;
		float bri = 0.5f;
			
		float maxEdge = shape.getMaxEdgeDistance();

		for (Int2D.WithIndex tile : shape.range.getAllMutable()) {
			if (renderStopped) break;
			if (tile.index % 100 == 0) Thread.yield();
			if (!shape.contains(tile.x(), tile.y())) continue;
				
			float edge = shape.getEdgeDistance(tile.x(), tile.y());

			// Outlines guaranteed, interior uses dot grid
			if (edge > 1 && ((tile.x()*4 + tile.y()) % 8 != 0)) continue;
			
			float briScale = (1.0f - edge/maxEdge)*0.8f + 0.2f;
			g.setColor(new Color(Color.HSBtoRGB(hue, sat, bri * briScale)));
			
			int terrain = island.getHeightmap().getTop(tile) - island.getAltitude();
			terrain /= 1.5;
			
			g.drawLine(tile.x(), tile.y()-terrain, tile.x(), tile.y()-terrain);
		}
		
		if (!renderStopped) {
		}
		g.dispose();
	}

	
}
