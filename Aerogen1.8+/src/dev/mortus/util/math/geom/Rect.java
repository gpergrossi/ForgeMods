package dev.mortus.util.math.geom;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.data.Pair;

public final class Rect {

	public final double x, y;
	public final double width, height;
	private List<LineSeg> sides;
	
	public Rect(Rectangle2D rect2d) {
		this.x = rect2d.getX();
		this.y = rect2d.getY();
		this.width = rect2d.getWidth();
		this.height = rect2d.getHeight();
	}
	
	public Rect(Vec2 pos, Vec2 size) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.width = size.getX();
		this.height = size.getY();
	}
	
	public Rect(double x, double y, Vec2 size) {
		this.x = x;
		this.y = y;
		this.width = size.getX();
		this.height = size.getY();
	}
	
	public Rect(Vec2 pos, double width, double height) {		
		this.x = pos.getX();
		this.y = pos.getY();
		this.width = width;
		this.height = height;
	}
	
	public Rect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rect union(Rect r) {
		double minX = Math.min(minX(), r.minX());
		double minY = Math.min(minY(), r.minY());
		
		double maxX = Math.max(maxX(), r.maxX());
		double maxY = Math.max(maxY(), r.maxY());
		
		return new Rect(minX, minY, maxX-minX, maxY-minY);
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
		
		// Correct ordering: first is first along line direction
		if (first == null) {
			first = second; second = null;
		} else if (second != null) {
			if (line.dir.getX() != 0) {
				if ((second.getX() - first.getX()) / line.dir.getX() < 0) {
					Vec2 swap = first; first = second; second = swap;
				}
			} else if (line.dir.getY() != 0) {
				if ((second.getY() - first.getY()) / line.dir.getY() < 0) {
					Vec2 swap = first; first = second; second = swap;
				}
			}
		}
		
		return new Pair<Vec2>(first, second);
	}
	
	public boolean contains(Vec2 p) {
		if (p.getX() < minX() || p.getY() < minY()) return false;
		if (p.getX() > maxX() || p.getY() > maxY()) return false;
		return true;
	}
	
	public LineSeg clip(Line line) {
		if (line.length() == 0) {
			if (this.contains(line.pos)) return (LineSeg) line.redefine(0, 0);
			return null;
		}
		
		if (line instanceof LineSeg) {
			LineSeg lineseg = (LineSeg) line;
			if (this.contains(lineseg.getStart()) && this.contains(lineseg.getEnd())) return lineseg;
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
		if (sides == null) {
			sides = new ArrayList<LineSeg>();
			
			Vec2 x0y0 = Vec2.create(minX(), minY());
			Vec2 x1y0 = Vec2.create(maxX(), minY());
			Vec2 x0y1 = Vec2.create(minX(), maxY());
			Vec2 x1y1 = Vec2.create(maxX(), maxY());
			
			// interior is LEFT of all line segments (counter clockwise winding order in right handed system)
			sides.add(new LineSeg(x0y0, x1y0));
			sides.add(new LineSeg(x1y0, x1y1));
			sides.add(new LineSeg(x1y1, x0y1));
			sides.add(new LineSeg(x0y1, x0y0));
		}
		
		return sides;
	}
	
	@Override
	public String toString() {
		return "Rect[x0="+minX()+", y0="+minY()+", x1="+maxX()+", y1="+maxY()+"]";
	}

	public Rect expand(double padding) {
		return new Rect(minX() - padding, minY() - padding, width() + padding*2, height() + padding*2);
	}

	public Rectangle2D toRectangle2D() {
		return new Rectangle2D.Double(x, y, width, height);
	}

	public double minX() {
		return x;
	}
	
	public double maxX() {
		return x+width;
	}
	
	public double minY() {
		return y;
	}
	
	public double maxY() {
		return y+height;
	}

	public double width() {
		return width;
	}
	
	public double height() {
		return height;
	}
	
	public double centerX() {
		return x + width/2;
	}
	
	public double centerY() {
		return y + height/2;
	}
	
}
