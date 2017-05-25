package dev.mortus.util.data;

import java.util.Optional;
import java.util.function.Function;

public class Int2D {
	
	public int x, y;
	public Int2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static class WithIndex extends Int2D {
		public final Int2DRange range;
		public int index;
		public WithIndex(Int2DRange range, int x, int y, int index) {
			super(x, y);
			this.range = range;
			this.index = index;
		}
	}
	
	public static class StoredFloat extends WithIndex {
		Int2DRange.Floats floats;
		public StoredFloat(Int2DRange.Floats floats, int x, int y, int index) {
			super(floats, x, y, index);
			this.floats = floats;
		}
		public float getValue() {
			return floats.get(index);
		}
		public void setValue(float value) {
			floats.set(index, value);
		}
		public void setValue(Function<Float, Float> operation) {
			floats.set(index, operation.apply(floats.get(index)));
		}
		public Optional<StoredFloat> getNeighbor(int i, int j) {
			if (!floats.contains(x+i, y+j)) return Optional.empty();
			return Optional.of(new StoredFloat(this.floats, x+i, y+j, index+j*floats.width+i));
		}
	}
	
}
