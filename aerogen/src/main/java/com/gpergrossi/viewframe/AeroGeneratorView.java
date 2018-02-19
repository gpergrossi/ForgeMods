package com.gpergrossi.viewframe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.AeroGenerator;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.river.River;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverCell;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverFeature;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.aerogen.primer.WorldPrimerChunk;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.shapes.LineSeg;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AeroGeneratorView extends View {

	List<Region> regions;
	List<Island> islands;
	AeroGenerator generator;
	
	public AeroGeneratorView(AeroGenerator generator) {
		super(0, 0, 800, 600);
		this.generator = generator;
	}

	@Override
	public void init() {
		regions = new ArrayList<>();
		islands = new ArrayList<>();
		this.setFPS(12);
	}
	
	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		AeroGenerator.guiClosed();
	}

	@Override
	public void update(double secondsPassed) {
	}

	@Override
	public void drawWorld(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		
		BlockPos spawn = generator.getWorld().getSpawnPoint();
		g2d.drawLine(spawn.getX()-5, spawn.getZ()-5, spawn.getX()+5, spawn.getZ()+5);
		g2d.drawLine(spawn.getX()-5, spawn.getZ()+5, spawn.getX()+5, spawn.getZ()-5);
		
		regions.clear();
		generator.getIslandProvider().debugGetLoadedRegions(regions);
		for (Region region : regions) {
			islands.clear();
			generator.getIslandProvider().getIslandsInRegion(islands, region);
			for (Island island : islands) {

				Random random = new Random(island.getSeed());
				float hue = random.nextFloat();
				float bri = random.nextFloat()*0.25f + 0.25f;
				float sat = 0.5f;
				g2d.setColor(new Color(Color.HSBtoRGB(hue, sat, bri)));

				// Draw island image
				if (!island.isGenerated()) {
					g2d.draw(island.getShape().getBoundingBox().asAWTShape());
				} else {
					IslandDebugRender idr = island.debugRender;
					if (idr == null) idr = new IslandDebugRender(island);
				
					g2d.drawImage(idr.getImage(), idr.getX(), idr.getY(), null);
				}
				
				// Draw island cells
				for (IslandCell cell : island.getShape().cells) {
					Convex insetPoly = cell.getInsetPolygon();
					if (insetPoly == null) System.err.println("A cell from island "+cell.getIsland().toString()+" was too small");
					else g2d.draw(insetPoly.asAWTShape());
				}

			}

			g2d.setColor(Color.LIGHT_GRAY);
			RiverFeature riverFeature = region.getFeature(RiverFeature.class);
			if (riverFeature != null) {
				for (River river : riverFeature.getRivers()) {
					for (RiverCell cell : river.getCells()) {
						List<LineSeg> segs = cell.getRiverCurve();
						for (LineSeg seg : segs) g2d.draw(seg.asAWTShape());
						g2d.draw(cell.getIslandCell().getInsetPolygon().asAWTShape());
					}
					for (RiverWaterfall waterfall : river.getWaterfalls()) {
						g2d.draw(waterfall.getEdge().asAWTShape());
						g2d.draw(waterfall.getLocation().toSegment(6).asAWTShape());
					}
				}
			}

			// Draw region polygon
			g2d.setColor(Color.GRAY);
			g2d.draw(region.getRegionPolygon().asAWTShape());
		}
		
		Iterator<WorldPrimerChunk> chunkIter = generator.getWorldPrimer().getChunks();
		
		try {
			while (chunkIter.hasNext()) {
				WorldPrimerChunk chunk = chunkIter.next();
				int chunkMinX = chunk.chunkX << 4;
				int chunkMinZ = chunk.chunkZ << 4;
				
				int red = 64, green = 64, blue = 64;
				if (chunk.hasBiomes()) red = 255;
				if (chunk.isGenerated()) green = 255;
				if (chunk.isPopulated()) blue = 255;
				g2d.setColor(new Color(red, green, blue));
				
				if (chunk.needsSave()) {
					g2d.drawLine(chunkMinX+5, chunkMinZ+5, chunkMinX+11, chunkMinZ+11);
					g2d.drawLine(chunkMinX+5, chunkMinZ+11, chunkMinX+11, chunkMinZ+5);
				}
				
				if (chunk.getLoadedStatus() != 0) { 
					red = 64; green = 64; blue = 64;
					if (chunk.hadBiomesOnLoad()) red = 255;
					if (chunk.wasGeneratedOnLoad()) green = 255;
					if (chunk.wasPopulatedOnLoad()) blue = 255;
					g2d.setColor(new Color(red, green, blue));
					g2d.drawLine(chunkMinX+2, chunkMinZ+2, chunkMinX+2, chunkMinZ+6);
					g2d.drawLine(chunkMinX+2, chunkMinZ+6, chunkMinX+4, chunkMinZ+6);
				}
				
				if (!chunk.isNeighborSameStatus(-1, 0)) g2d.drawLine(chunkMinX, chunkMinZ, chunkMinX, chunkMinZ+15);
				if (!chunk.isNeighborSameStatus(1, 0))  g2d.drawLine(chunkMinX+15, chunkMinZ, chunkMinX+15, chunkMinZ+15);
				if (!chunk.isNeighborSameStatus(0, -1)) g2d.drawLine(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ);
				if (!chunk.isNeighborSameStatus(0, 1))  g2d.drawLine(chunkMinX, chunkMinZ+15, chunkMinX+15, chunkMinZ+15);
			}
		} catch (ConcurrentModificationException e) {}

		g2d.setColor(Color.WHITE);
		for (EntityPlayerMP player : generator.getWorld().getPlayers(EntityPlayerMP.class, p -> true)) {
			Vec3d pos = player.getPositionVector();
			Vec3d dir = player.getLookVec();
			g2d.drawOval((int) pos.x-2, (int) pos.z-2, 4, 4);
			g2d.drawLine((int) pos.x, (int) pos.z, (int) (pos.x+dir.x*8), (int) (pos.z+dir.z*8));
		}
	}

	@Override
	public void drawOverlayUI(Graphics2D g2d) {}
	
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
