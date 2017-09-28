package com.gpergrossi.viewframe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.generator.WorldPrimerChunk;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.river.River;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.util.geom.shapes.Convex;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AeroGeneratorView extends View {

	List<Region> regions;
	
	public AeroGeneratorView() {
		super(0, 0, 800, 600);
	}

	@Override
	public void init() {
		regions = new ArrayList<>();
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
		
		AeroGenerator dim = null;
		for (Map.Entry<World, AeroGenerator> gen : AeroGenerator.getGenerators()) {
			dim = gen.getValue();
			break;
		}
		if (dim == null) return;
		
		BlockPos spawn = dim.getWorld().getSpawnPoint();
		g2d.drawLine(spawn.getX()-5, spawn.getZ()-5, spawn.getX()+5, spawn.getZ()+5);
		g2d.drawLine(spawn.getX()-5, spawn.getZ()+5, spawn.getX()+5, spawn.getZ()-5);
		
		regions.clear();
		dim.getRegionManager().getLoadedRegions(regions);
		for (Region region : regions) {
			
			for (Island island : region.getIslands()) {
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
			for (River river : region.getRivers()) {
				for (RiverWaterfall waterfall : river.getWaterfalls()) {
					g2d.draw(waterfall.getEdge().asAWTShape());
					g2d.draw(waterfall.getLocation().toSegment(6).asAWTShape());
				}
			}

			g2d.setColor(Color.GRAY);
			g2d.draw(region.getRegionPolygon().asAWTShape());
		}
		
		Iterator<WorldPrimerChunk> chunkIter = dim.getWorldPrimer().chunks.iterator();
		
		try {
			while (chunkIter.hasNext()) {
				WorldPrimerChunk chunk = chunkIter.next();
				int chunkMinX = chunk.chunkX << 4;
				int chunkMinZ = chunk.chunkZ << 4;
				
				int red = 64, green = 64, blue = 64;
				if (!chunk.isCompleted()) {
					if (chunk.hasBiomes()) red = 255;
					if (chunk.isGenerated()) green = 255;
					if (chunk.isPopulated()) blue = 255;
				} else { red = green = blue = 0; }
				g2d.setColor(new Color(red, green, blue));
				
				if (!chunk.isNeighborSameStatus(-1, 0)) g2d.drawLine(chunkMinX, chunkMinZ, chunkMinX, chunkMinZ+15);
				if (!chunk.isNeighborSameStatus(1, 0))  g2d.drawLine(chunkMinX+15, chunkMinZ, chunkMinX+15, chunkMinZ+15);
				if (!chunk.isNeighborSameStatus(0, -1)) g2d.drawLine(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ);
				if (!chunk.isNeighborSameStatus(0, 1))  g2d.drawLine(chunkMinX, chunkMinZ+15, chunkMinX+15, chunkMinZ+15);
			}
		} catch (ConcurrentModificationException e) {}

		g2d.setColor(Color.WHITE);
		for (EntityPlayerMP player : dim.getWorld().getPlayers(EntityPlayerMP.class, p -> true)) {
			Vec3d pos = player.getPositionVector();
			Vec3d dir = player.getLookVec();
			g2d.drawOval((int) pos.x-2, (int) pos.z-2, 4, 4);
			g2d.drawLine((int) pos.x, (int) pos.z, (int) (pos.x+dir.x*8), (int) (pos.z+dir.z*8));
		}
	}

	@Override
	public void drawOverlayUI(Graphics2D g2d) {
		// TODO Auto-generated method stub

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
