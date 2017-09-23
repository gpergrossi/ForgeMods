package com.gpergrossi.viewframe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.generator.data.IslandCell;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;

import net.minecraft.entity.player.EntityPlayerMP;
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
		this.setFPS(30);
	}
	
	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		AeroGenerator.setGUIEnabled(false);
	}

	@Override
	public void update(double secondsPassed) {
		
	}

	@Override
	public void drawWorld(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.drawLine(-5, -5, 5, 5);
		g2d.drawLine(-5, 5, 5, -5);
		
		AeroGenerator dim = null;
		for (Map.Entry<World, AeroGenerator> gen : AeroGenerator.getGenerators()) {
			dim = gen.getValue();
			break;
		}
		if (dim == null) return;

		g2d.setColor(Color.DARK_GRAY);
		for (int chunkX = -20; chunkX <= 20; chunkX++) {
			for (int chunkZ = -20; chunkZ <= 20; chunkZ++) {
				if (dim.getWorld().isChunkGeneratedAt(chunkX, chunkZ)) {
					g2d.fillRect(chunkX*16, chunkZ*16, 16, 16);
				}
			}	
		}
		
		regions.clear();
		dim.getRegionManager().getLoadedRegions(regions);
		for (Region region : regions) {
			g2d.setColor(Color.GRAY);
			for (Island island : region.getIslands()) {
				for (IslandCell cell : island.getShape().cells) {
					g2d.draw(cell.getPolygon().asAWTShape());
				}
			}
			g2d.setColor(Color.WHITE);
			g2d.draw(region.getRegionPolygon().asAWTShape());
		}
		
		for (EntityPlayerMP player : dim.getWorld().getPlayers(EntityPlayerMP.class, p -> true)) {
			Vec3d pos = player.getPositionVector();
			Vec3d dir = player.getLookVec();
			g2d.drawOval((int) pos.x-1, (int) pos.z-1, 2, 2);
			g2d.drawLine((int) pos.x, (int) pos.z, (int) (pos.x+dir.x*5), (int) (pos.z+dir.z*5));
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
