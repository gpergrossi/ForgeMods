package dev.mortus.util.data;

import java.util.Optional;
import java.util.function.Function;

public class Int2D {
	
	public int x, y;
	
	public Int2D() {
		this(0, 0);
	}
	
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

	public static class StoredByte extends WithIndex {
		Int2DRange.Bytes bytes;
		public StoredByte(Int2DRange.Bytes bytes, int x, int y, int index) {
			super(bytes, x, y, index);
			this.bytes = bytes;
		}
		public float getValue() {
			return bytes.get(index);
		}
		public void setValue(byte value) {
			bytes.set(index, value);
		}
		public void setValue(Function<Byte, Byte> operation) {
			bytes.set(index, operation.apply(bytes.get(index)));
		}
		public Optional<StoredByte> getNeighbor(int i, int j) {
			if (!bytes.contains(x+i, y+j)) return Optional.empty();
			return Optional.of(new StoredByte(this.bytes, x+i, y+j, index+j*bytes.width+i));
		}
	}

	public static class StoredBit extends WithIndex {
		Int2DRange.Bits bits;
		public StoredBit(Int2DRange.Bits bits, int x, int y, int index) {
			super(bits, x, y, index);
			this.bits = bits;
		}
		public boolean getValue() {
			return bits.get(index);
		}
		public void setValue(boolean value) {
			bits.set(index, value);
		}
		public void setValue(Function<Boolean, Boolean> operation) {
			bits.set(index, operation.apply(bits.get(index)));
		}
		public Optional<StoredBit> getNeighbor(int i, int j) {
			if (!bits.contains(x+i, y+j)) return Optional.empty();
			return Optional.of(new StoredBit(this.bits, x+i, y+j, index+j*bits.width+i));
		}
	}
	
}
