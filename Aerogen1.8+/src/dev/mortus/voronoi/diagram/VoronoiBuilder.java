package dev.mortus.voronoi.diagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.data.StableArrayList;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.internal.Worker;
import dev.mortus.voronoi.internal.WorkerDebug;

public class VoronoiBuilder {

	public static class InitialState {
		public Voronoi voronoi;
		public Rect bounds;
		public Vec2[] sites;
		
		private InitialState(Voronoi voronoi, Rect bounds, Vec2[] sites) {
			this.voronoi = voronoi;
			this.bounds = bounds;
			this.sites = sites;
		}
	}

	private StableArrayList<Vec2> sites;
	private double padding = 5.0;

	public VoronoiBuilder() {
		sites = new StableArrayList<>(Vec2.class, 20);
	}

	public int addSite(Vec2 point) {
		int index = sites.put(point);
		return index;
	}

	public void savePoints() throws IOException {
		FileOutputStream fos = new FileOutputStream("saved");
		DataOutputStream dos = new DataOutputStream(fos);
		
		List<Vec2> packed = new ArrayList<>();
		sites.iterator().forEachRemaining(e -> packed.add(e));
		
		dos.writeInt(packed.size());
		for (Vec2 site : packed) {
			dos.writeDouble(site.x);
			dos.writeDouble(site.y);
		}

		System.out.println("Saved "+packed.size()+" sites");
		dos.close();
	}
	
	public void loadPoints() throws IOException {		
		FileInputStream fis = new FileInputStream("saved");
		DataInputStream dis = new DataInputStream(fis);
		
		int numSites = dis.readInt();
		for (int i = 0; i < numSites; i++) {
			double x = dis.readDouble();
			double y = dis.readDouble();
			
			addSite(new Vec2(x,y));
		}
		
		dis.close();
		System.out.println("Loaded "+numSites+" sites");
	}

	public void removeSite(Vec2 site) {
		sites.remove(site);
	}
	
	public void removeSite(int siteID) {
		sites.remove(siteID);
	}

	public void clearSites() {
		sites.clear();
	}
	
	public void shrink() {
		sites.shrink();
	}
	
	public Worker getBuildWorker() {
		Voronoi voronoi = new Voronoi();
		InitialState init = new InitialState(voronoi, getBounds(), getSiteArray());
		return new Worker(init);
	}
	
	public WorkerDebug getBuildWorkerDebug() {
		Voronoi voronoi = new Voronoi();
		InitialState init = new InitialState(voronoi, getBounds(), getSiteArray());
		return new WorkerDebug(init);
	}
	
	private Vec2[] getSiteArray() {
		Vec2[] siteArray = new Vec2[sites.size()];
		siteArray = sites.toArray(siteArray);
		return siteArray;
	}
	
	private Rect getBounds() {
		Rect bounds = null;
		for (Vec2 point : sites) {
			Rect pointRect = new Rect(point, Vec2.ZERO);
			if (bounds == null) {
				bounds = pointRect;
			} else {
				bounds = bounds.union(pointRect);
			}
		}
		return bounds.expand(padding);
	}

	public List<Vec2> getSites() {
		return sites;
	}

	public Voronoi build() {
		Worker w = this.getBuildWorker();
		while (!w.isDone()) {
			w.doWork();
		}
		return w.getResult();
	}
	
}
