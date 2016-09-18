package dev.mortus.util.math;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Rect {

	Vec2 pos;
	Vec2 size;
	
	public Rect(Rectangle2D rect2d) {
		this.pos = new Vec2(rect2d.getX(), rect2d.getY());
		this.size = new Vec2(rect2d.getWidth(), rect2d.getHeight());
	}
	
	public List<Vec2> intersect(Line line) {
		List<Vec2> intersects = new ArrayList<Vec2>(2);
		
		List<LineSeg> sides = getSides();
		for (LineSeg seg : sides) {
			Vec2 intersect = seg.intersect(line);
			if (intersect != null) intersects.add(intersect);
		}
		
		return intersects;
	}
	
	public LineSeg clip(Line line) {
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
	
}
