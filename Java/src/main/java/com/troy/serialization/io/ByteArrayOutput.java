package com.troy.serialization.io;

import java.util.*;

import com.troy.serialization.*;
import com.troy.serialization.exception.*;
import com.troy.serialization.util.*;

import sun.misc.*;

public class ByteArrayOutput extends AbstractOutput {
	private static final Unsafe unsafe = MiscUtil.getUnsafe();

	private static final int NATIVE_ARRAY_COPY_THRESH_HOLD = 16;
	private static final int DEFAULT_NATIVE_SIZE = 128;

	private byte[] buffer;
	private int position;
	private int requested = 0;

	public ByteArrayOutput() {
		this(20);// A Hotspot array header is 12 bytes so 20 + 12 = 32 therefore no bytes are
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
			mapped.close();
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
			NativeUtils.shortsToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {
			// FIXME provide non native alternative
		}
		addRequired();
	}

	@Override
	public void writeInts(int[] src, int offset, int elements) {
		require(elements * Integer.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.intsToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {

		}
		addRequired();
	}

	@Override
	public void writeLongs(long[] src, int offset, int elements) {
		require(elements * Long.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.longsToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {

		}
		addRequired();
	}

	@Override
	public void writeFloats(float[] src, int offset, int elements) {
		require(elements * Float.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.floatsToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {
			for (int i = 0; i < src.length; i++) {
				writeFloat(src[i]);
			}
		}
		addRequired();
	}

	@Override
	public void writeDoubles(double[] src, int offset, int elements) {
		require(elements * Double.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.doublesToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {

		}
		addRequired();
	}

	@Override
	public void writeChars(char[] src, int offset, int elements) {
		require(elements * Character.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.charsToBytes(buffer, src, offset, position, elements, bigEndian);
		} else {

		}
		addRequired();
	}

	@Override
	public void writeBooleans(boolean[] src, int offset, int elements) {
		require(elements * 1);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.booleansToBytes(buffer, src, offset, position, elements, bigEndian);
			position += elements;
		} else {

		}
		addRequired();
	}

	@Override
	public void writeBooleansCompact(boolean[] src, int offset, int elements) {
		require((elements + 7) / 8);
		if (NativeUtils.NATIVES_ENABLED) {
			int result = NativeUtils.booleansToBytesCompact(buffer, src, offset, position, elements);
			if (result < 0) {// Error happened

			} else {
				position += result;// If result is positive, it holds the number of bytes written
			}
		} else {
			addRequired();
		}

	}

	public void addRequired() {
		position += requested;
	}

	@Override
	public NativeMemoryBlock map(long bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unmap(NativeMemoryBlock block) {
		// TODO Auto-generated method stub
		
	}
}
