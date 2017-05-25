package dev.mortus.util.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;

import dev.mortus.util.data.Int2D.StoredFloat;
import dev.mortus.util.math.geom.Rect;

public class Int2DRange {
	
	public final int minX, maxX;
	public final int minY, maxY;
	public final int width, height;
	
	public static Int2DRange fromRect(Rect rect) {
		int minX = (int) Math.floor(rect.minX());
		int minY = (int) Math.floor(rect.minY());
		int maxX = (int) Math.ceil(rect.maxX());
		int maxY = (int) Math.ceil(rect.maxY());
		return new Int2DRange(minX, minY, maxX, maxY);
	}

	public Int2DRange(Int2DRange range) {
		this(range.minX, range.minY, range.maxX, range.maxY);
	}
	
	public Int2DRange(Int2D start, Int2D end) {
		this(start.x, start.y, end.x, end.y);
	}
	
	public Int2DRange(int minX, int minY, int maxX, int maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.width = Math.max(maxX - minX + 1, 0);
		this.height = Math.max(maxY - minY + 1, 0);
	}

	protected Int2DRange resize(int minX, int minY, int maxX, int maxY) {
		return new Int2DRange(minX, minY, maxX, maxY);
	}
	
	public int size() {
		return width*height;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean contains(int x, int y) {
		return (x >= minX && y >= minY && x <= maxX && y <= maxY);
	}
	
	public boolean contains(Int2D pt) {
		return contains(pt.x, pt.y);
	}
	
	public boolean onBorder(int x, int y, int borderPadding) {
		return contains(x, y) && (x < minX+borderPadding && y < minY + borderPadding && x > maxX-borderPadding && y > maxY-borderPadding);
	}
	
	public boolean onBorder(Int2D pt, int borderPadding) {
		return onBorder(pt.x, pt.y, borderPadding);
	}
	
	public Int2DRange grow(int padX, int padY) {
		return resize(minX-padX, minY-padY, maxX+padX, maxY+padY);
	}

	public Int2DRange grow(int pad) {
		return grow(pad, pad);
	}
	
	public Int2DRange shrink(int insetX, int insetY) {
		return grow(-insetX, -insetY);
	}
	
	public Int2DRange shrink(int inset) {
		return grow(-inset, -inset);
	}
	
	public Int2DRange intersect(Int2DRange other) {
		int minX = Math.max(this.minX, other.minX);
		int minY = Math.max(this.minY, other.minY);
		int maxX = Math.min(this.maxX, other.maxX);
		int maxY = Math.min(this.maxY, other.maxY);
		return resize(minX, minY, maxX, maxY);
	}

	public Int2DRange offset(int offsetX, int offsetY) {
		return resize(minX+offsetX, minY+offsetY, maxX+offsetX, maxY+offsetY);
	}
	
	public int indexFor(Int2D pt) {
		return indexFor(pt.x, pt.y);
	}
	
	private int indexFor(int x, int y) {
		return (y-minY)*width+(x-minX);
	}

	public Int2D randomTile(Random random) {
		return new Int2D(random.nextInt(width)+minX, random.nextInt(height)+minY);
	}
	
	public Iterable<Int2D.WithIndex> getAllMutable() {
		return new Iterable<Int2D.WithIndex>() {
			@Override
			public Iterator<Int2D.WithIndex> iterator() {
				return new Iterator<Int2D.WithIndex>() {
					private Int2D.WithIndex mutable = new Int2D.WithIndex(Int2DRange.this, 0, 0, 0);
					private int x, y, index;
					
					@Override
					public boolean hasNext() {
						return index < size();
					}

					@Override
					public Int2D.WithIndex next() {
						if (!hasNext()) throw new NoSuchElementException();
						mutable.x = x+minX;
						mutable.y = y+minY;
						mutable.index = index;
						index++;
						x++;
						if (x == width) {
							x = 0;
							y++;
						}
						return mutable;
					}
				};	
			}
		};
	}
	
	public Iterable<Int2D.WithIndex> getAll() {
		return new Iterable<Int2D.WithIndex>() {
			@Override
			public Iterator<Int2D.WithIndex> iterator() {
				return new Iterator<Int2D.WithIndex>() {
					private int x, y, index;
					
					@Override
					public boolean hasNext() {
						return index < size();
					}

					@Override
					public Int2D.WithIndex next() {
						if (!hasNext()) throw new NoSuchElementException();
						Int2D.WithIndex result = new Int2D.WithIndex(Int2DRange.this, x+minX, y+minY, index);
						index++;
						x++;
						if (x == width) {
							x = 0;
							y++;
						}
						return result;
					}
				};	
			}
		};
	}

	protected <S, T> Iterable<T> castIterable(Iterable<S> iterable, Function<S, T> caster) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					Iterator<S> iter = iterable.iterator();
					public boolean hasNext() { return iter.hasNext(); }
					public T next() { return caster.apply(iter.next()); }
				};
			}
		};
	}


	public Floats createFloats() {
		return new Floats(this);
	}
	
	public Floats copyFloats(Floats floats, int dstStartX, int dstStartY) {
		Floats result = new Floats(this);
		Floats.copyRange(floats, result, dstStartX, dstStartY);
		return result;
	}
	
	
	
	
	public static class Floats extends Int2DRange {
		
		public static void copyRange(Floats src, Floats dest, int dstStartX, int dstStartY) {
			int minX = Math.max(src.minX, dest.minX-dstStartX);
			int minY = Math.max(src.minY, dest.minY-dstStartY);
			int maxX = Math.min(src.maxX, dest.maxX-dstStartX);
			int maxY = Math.min(src.maxY, dest.maxY-dstStartY);
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					dest.set(x+dstStartX, y+dstStartY, src.get(x, y));
				}	
			}
		}
		
		public final float[] data;		
		
		public Floats(Int2DRange range) {
			this(range.minX, range.minY, range.maxX, range.maxY);
		}
		
		public Floats(Int2D start, Int2D end) {
			this(start.x, start.y, end.x, end.y);
		}
		
		public Floats(int minX, int minY, int maxX, int maxY) {
			super(minX, minY, maxX, maxY);
			this.data = new float[size()];
		}
		
		@Override
		protected Floats resize(int minX, int minY, int maxX, int maxY) {
			Floats result = new Floats(minX, minY, maxX, maxY);
			copyRange(this, result, 0, 0);
			return result;
		}

		@Override
		public Floats grow(int padX, int padY) {
			return resize(minX-padX, minY-padY, maxX+padX, maxY+padY);
		}

		@Override
		public Floats grow(int pad) {
			return grow(pad, pad);
		}

		@Override
		public Floats shrink(int insetX, int insetY) {
			return grow(-insetX, -insetY);
		}

		@Override
		public Floats shrink(int inset) {
			return grow(-inset, -inset);
		}

		@Override
		public Floats intersect(Int2DRange other) {
			int minX = Math.max(this.minX, other.minX);
			int minY = Math.max(this.minY, other.minY);
			int maxX = Math.min(this.maxX, other.maxX);
			int maxY = Math.min(this.maxY, other.maxY);
			return resize(minX, minY, maxX, maxY);
		}
		
		protected float get(int index) {
			return data[index];
		}
		
		protected void set(int index, float value) {
			data[index] = value;
		}
		
		public float get(int x, int y) {
			return data[(y-minY)*width+(x-minX)];
		}
		
		public void set(int x, int y, float value) {
			data[(y-minY)*width+(x-minX)] = value;
		}
		
		public Iterable<StoredFloat> getAllFloats() {
			return castIterable(Floats.super.getAll(), t -> new Int2D.StoredFloat(Floats.this, t.x, t.y, t.index));
		}
		
		public Iterable<StoredFloat> getAllFloatsMutable() {
			return castIterable(Floats.super.getAllMutable(), t -> new Int2D.StoredFloat(Floats.this, t.x, t.y, t.index));
		}

		public Int2DRange asRange() {
			return new Int2DRange(this);
		}

		public float getSafe(int x, int y, float defaultValue) {
			if (!this.contains(x, y)) return defaultValue;
			return this.get(x, y);
		}
		
	}

	
}
