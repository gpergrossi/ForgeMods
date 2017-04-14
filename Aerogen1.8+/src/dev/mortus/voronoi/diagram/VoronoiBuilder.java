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

	private StableArrayList<Vec2> sites;
	private double padding = 5.0;
	private Rect bounds = null;
	private boolean userBounds = false;
	
	public VoronoiBuilder() {
		this(20);
	}
	
	public VoronoiBuilder(int initialCapacity) {
		sites = new StableArrayList<>(t -> new Vec2[t], initialCapacity);
	}

	public int addSite(Vec2 point) {
		if (userBounds && !bounds.contains(point)) {
			return -1;
		}
		int index = sites.put(point);
		boundsAddPoint(point);
		return index;
	}

	public void removeSite(Vec2 site) {
		sites.remove(site);
	}
	
	public void removeSite(int siteID) {
		sites.remove(siteID);
	}

	public void clearSites(boolean keepBounds) {
		sites.clear();
		if (!keepBounds) {
			bounds = null;
			userBounds = false;
		} else {
			userBounds = true;
		}
	}
	
	public void clearSites() {
		clearSites(false);
	}
	
	public void shrink() {
		sites.shrink();
	}
	
	public Worker getBuildWorker() {
		if (sites.size() == 0) throw new RuntimeException("Cannot construct diagram with no sites.");
		return new Worker(getBounds(), getSiteArray());
	}
	
	public WorkerDebug getBuildWorkerDebug() {
		if (sites.size() == 0) throw new RuntimeException("Cannot construct diagram with no sites.");
		return new WorkerDebug(getBounds(), getSiteArray());
	}
	
	private Vec2[] getSiteArray() {
		Vec2[] siteArray = new Vec2[sites.size()];
		siteArray = sites.toArray(siteArray);
		return siteArray;
	}
	
	/**
	 * Sets the bounds of this diagram
	 * All current and future points outside these bounds will be removed.
	 * @param bounds
	 */
	public void setBounds(Rect bounds) {
		this.bounds = bounds;
		this.userBounds = true;
		for (Vec2 v : sites) {
			if (!bounds.contains(v)) {
				removeSite(v);
			}
		}
	}
	
	public Rect getBounds() {
		return bounds;
	}
	
	private void boundsAddPoint(Vec2 point) {
		Rect pointRect = new Rect(point, Vec2.ZERO);
		if (!userBounds) pointRect = pointRect.expand(padding);
		if (bounds == null) {
			bounds = pointRect;
		} else {
			bounds = bounds.union(pointRect);
		}
	}

	public List<Vec2> getSites() {
		return sites;
	}

	public Voronoi build() {
		Worker w = this.getBuildWorker();
		while (!w.isDone()) {
			w.doWork(-1);
		}
		return w.getResult();
	}


	public void savePoints() throws IOException {
		FileOutputStream fos = new FileOutputStream("saved");
		DataOutputStream dos = new DataOutputStream(fos);
		
		List<Vec2> packed = new ArrayList<>();
		sites.iterator().forEachRemaining(e -> packed.add(e));
		
		dos.writeInt(packed.size());
		for (Vec2 site : packed) {
			dos.writeDouble(site.getX());
			dos.writeDouble(site.getY());
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
			
			addSite(Vec2.create(x,y));
		}
		
		dis.close();
		System.out.println("Loaded "+numSites+" sites");
	}
	
}
