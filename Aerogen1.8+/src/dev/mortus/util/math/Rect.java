package dev.mortus.util.math;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.data.Pair;

public class Rect {

	Vec2 pos;
	Vec2 size;
	Vec2 extent = null;
	
	public Rect(Rectangle2D rect2d) {
		this.pos = new Vec2(rect2d.getX(), rect2d.getY());
		this.size = new Vec2(rect2d.getWidth(), rect2d.getHeight());
	}
	
	public Pair<Vec2> intersect(Line line) {
		Vec2 first = null;
		Vec2 second = null;
		
		List<LineSeg> sides = getSides();
		for (LineSeg seg : sides) {
			Vec2 intersect = seg.intersect(line);
			if (intersect != null) {
				if (first == null) {
					first = intersect;
				} else if (second == null) {
					if (!first.equals(intersect))
					second = intersect;
				} else {
					if (!first.equals(intersect) && !second.equals(intersect))
					throw new RuntimeException("More than two intersects");
				}
			}
		}
		
		// Correct ordering first is first along line direction
		if (first == null) {
			first = second; second = null;
		} else if (second != null) {
			if (line.dir.x != 0) {
				if ((second.x - first.x) / line.dir.x < 0) {
					Vec2 swap = first; first = second; second = swap;
				}
			} else if (line.dir.y != 0) {
				if ((second.y - first.y) / line.dir.y < 0) {
					Vec2 swap = first; first = second; second = swap;
				}
			}
		}
		
		return new Pair<Vec2>(first, second);
	}
	
	/**
	 * returns the max X, max Y position of this rectangle
	 */
	public Vec2 extent() {
		if (extent == null) extent = pos.add(size);
		return extent;
	}
	
	public boolean contains(Vec2 p) {
		if (p.x < pos.x || p.y < pos.y) return false;
		Vec2 ext = extent();
		if (p.x > ext.x || p.y > ext.y) return false;
		return true;
	}
	
	public LineSeg clip(Line line) {
		if (line.length() == 0) {
			if (this.contains(line.pos)) return (LineSeg) line.redefine(0, 0);
			return null;
		}
		
		List<LineSeg> sides = getSides();
		for (LineSeg seg : sides) {
			// See: sides winding order + slice() doc comment
			// first = left side of segment = interior of rectangle
			line = seg.toLine().slice(line).first;
			if (line == null) break;
		}
		return (LineSeg) line;
	}
	
	public List<LineSeg> getSides() {
		List<LineSeg> sides = new ArrayList<LineSeg>();
		
		Vec2 x0y0 = pos;
		Vec2 x1y0 = new Vec2(pos.x+size.x, pos.y);
		Vec2 x0y1 = new Vec2(pos.x, pos.y+size.y);
		Vec2 x1y1 = pos.add(size);
		
		// interior is LEFT of all line segments (counter clockwise winding order in right handed system)
		sides.add(new LineSeg(x0y0, x1y0));
		sides.add(new LineSeg(x1y0, x1y1));
		sides.add(new LineSeg(x1y1, x0y1));
		sides.add(new LineSeg(x0y1, x0y0));
		
		return sides;
	}
	
	@Override
	public String toString() {
		return "Rect[x0="+pos.x+", y0="+pos.y+", x1="+extent().x+", y1="+extent().y+"]";
	}
	
}
