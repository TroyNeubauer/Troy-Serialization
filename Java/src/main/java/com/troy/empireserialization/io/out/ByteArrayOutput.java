package com.troy.empireserialization.io.out;

import java.util.Arrays;

import com.troy.empireserialization.exception.AlreadyClosedException;
import com.troy.empireserialization.memory.MasterMemoryBlock;
import com.troy.empireserialization.memory.NativeMemoryBlock;
import com.troy.empireserialization.util.MiscUtil;
import com.troy.empireserialization.util.NativeUtils;

import sun.misc.Unsafe;

public class ByteArrayOutput extends AbstractOutput {
	private static final Unsafe unsafe = MiscUtil.getUnsafe();

	private static final int NATIVE_ARRAY_COPY_THRESH_HOLD = 16;

	private MasterMemoryBlock mapped;

	private byte[] buffer;
	private int position;
	private int requested;

	public ByteArrayOutput() {
		this(116);// A Hotspot array header is 12 bytes so 116 + 12 = 128 therefore no bytes are
					// wasted due to multiple of eight packing
	}

	public ByteArrayOutput(int initalSize) {
		if (initalSize <= 0)
			throw new IllegalArgumentException("Inital size cannot be <= 0!");
		this.buffer = new byte[initalSize];
	}

	@Override
	public void writeByteImpl(byte b) {
		try {
			buffer[position++] = b;
		} catch (NullPointerException e) {
			throw new AlreadyClosedException();
		}
	}

	@Override
	public boolean hasBuffer() {
		return true;
	}

	@Override
	public int getBufferPosition() {
		return position;
	}

	@Override
	public void setBufferPosition(int newPosition) {
		if (newPosition < 0 || newPosition >= buffer.length)
			throw new IllegalArgumentException("New position out of range! " + newPosition);
		this.position = newPosition;
	}

	@Override
	public byte[] getBuffer() {
		return buffer;
	}

	@Override
	public void require(long bytes) {
		try {
			if (position + bytes > buffer.length) {
				buffer = Arrays.copyOf(buffer, (int) (buffer.length * 2.5 + 1));
			}
		} catch (NullPointerException e) {
			throw new AlreadyClosedException();
		}
		if (bytes > (long) Integer.MAX_VALUE)
			throw new IllegalArgumentException();
		requested = (int) bytes;
	}

	@Override
	public void close() {
		buffer = null;// Help GC
		position = -1;
		if (mapped != null)
			mapped.free();
	}

	@Override
	public void flush() {
		// Nop nothing to flush
	}

	@Override
	public void writeBytes(byte[] src, int offset, int elements) {
		require(elements * Byte.BYTES);
		System.arraycopy(src, offset, buffer, position, elements);
		addRequired();
	}

	@Override
	public void writeShorts(short[] src, int offset, int elements) {
		require(elements * Short.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.shortsToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeShorts(src, offset, elements);
		}

	}

	@Override
	public void writeInts(int[] src, int offset, int elements) {
		require(elements * Integer.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.intsToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeInts(src, offset, elements);
		}
	}

	@Override
	public void writeLongs(long[] src, int offset, int elements) {
		require(elements * Long.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.longsToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeLongs(src, offset, elements);
		}
	}

	@Override
	public void writeFloats(float[] src, int offset, int elements) {
		require(elements * Float.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.floatsToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeFloats(src, offset, elements);
		}
	}

	@Override
	public void writeDoubles(double[] src, int offset, int elements) {
		require(elements * Double.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.doublesToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeDoubles(src, offset, elements);
		}
	}

	@Override
	public void writeChars(char[] src, int offset, int elements) {
		require(elements * Character.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.charsToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeChars(src, offset, elements);
		}
	}

	@Override
	public void writeBooleans(boolean[] src, int offset, int elements) {
		require(elements * 1);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.booleansToBytes(buffer, src, offset, position, elements, swapEndinessInNative());
			addRequired();
		} else {
			super.writeBooleans(src, offset, elements);
		}
	}

	@Override
	public void writeBooleansCompact(boolean[] src, int offset, int elements) {
		require((elements + 7) / 8);
		if (NativeUtils.NATIVES_ENABLED) {
			int result = NativeUtils.booleansToBytesCompact(buffer, src, offset, position, elements);
			if (result < 0) {// Error happened

			} else {
				addRequired();
			}
		} else {
			super.writeBooleansCompact(src, offset, elements);
		}

	}

	public void addRequired() {
		position += requested;
	}

	@Override
	public NativeMemoryBlock map(long bytes) {
		if (mapped == null)
			mapped = MasterMemoryBlock.allocate(bytes);
		else {
			mapped.require(bytes);
		}
		return mapped;
	}

	@Override
	public void unmap(NativeMemoryBlock block) {
		if (block != mapped)
			throw new IllegalArgumentException("NativeMemoryBlock block " + block + " is not the block mapped by this buffer!");
		if (block.position() + position > Integer.MAX_VALUE)
			NativeUtils.throwByteIndexOutOfBounds();
		NativeUtils.nativeToBytes(buffer, block.address(), position, (int) block.position());
		position += block.position();
		block.setPosition(0);
	}

	@Override
	public boolean isNative() {
		return false;// We use a Java array. So no
	}
}
