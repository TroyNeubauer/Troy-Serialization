package com.troy.serialization.io;

import com.troy.serialization.exception.AlreadyClosedException;
import com.troy.serialization.exception.NoBufferException;
import com.troy.serialization.util.MiscUtil;
import com.troy.serialization.util.NativeUtils;
import com.troy.serialization.util.SerializationUtils;

import sun.misc.Unsafe;

public class NativeOutput extends AbstractOutput {

	static {
		SerializationUtils.init();
	}

	private static final Unsafe unsafe = MiscUtil.getUnsafe();
	private static final long DEFAULT_CAPACITY = 128;

	private long address, position;
	private long capacity, size, requested;

	public NativeOutput() {
		this(DEFAULT_CAPACITY);
	}

	public NativeOutput(long capacity) {
		this.address = unsafe.allocateMemory(capacity);
		this.capacity = capacity;
		this.size = 0;
	}

	@Override
	public void writeByteImpl(byte b) {
		unsafe.putByte(address + size++, b);
	}

	@Override
	public void flush() {
		// Nop
	}

	@Override
	public boolean hasBuffer() {
		return false;
	}

	@Override
	public int getBufferPosition() {
		throw new NoBufferException();
	}

	@Override
	public byte[] getBuffer() {
		throw new NoBufferException();
	}

	public byte[] toByteArray() {
		return ngetBuffer(address, (int) capacity);
	}

	public static native byte[] ngetBuffer(long address, int capacity);

	@Override
	public void require(long bytes) {
		if (bytes + size > capacity) {
			long newSize = (bytes / 2 + 1) / 2;// Round up to next power of two
			address = unsafe.reallocateMemory(address, newSize);
		}
		requested = bytes;
	}

	@Override
	public void close() {
		if (address == -1)
			throw new AlreadyClosedException();
		unsafe.freeMemory(address);
		address = -1;
		capacity = 0;
		size = 0;
	}

	@Override
	public void resetMappedOutputImpl(AbstractMappedIO out, long minSize) {
	}

	@Override
	public AbstractMappedIO newMappedOutput(long minSize) {
		return new MappedOutput(address, size, capacity);
	}

	@Override
	public void unmapOutputImpl(AbstractMappedIO out, long numBytesWritten) {
		require(numBytesWritten);
		// FIXME actually copy bytes
		// nmemcpy(address, position, out.address, out.offset, numBytesWritten);
		size += numBytesWritten;
	}

	@Override
	public void writeBytes(byte[] src, int offset, int bytes) {
		require(bytes);
		// FIXME switch to more efficient native approach later
		final int end = offset + bytes;
		for (int i = offset; i < end; i++) {
			writeByteImpl(src[i]);
		}
		addRequested();
	}

	@Override
	public void writeShorts(short[] src, int offset, int elements) {
		NativeUtils.shortsToNative(address + position, src, offset, elements, swapEndianess);
		addRequested();
	}

	@Override
	public void writeInts(int[] src, int offset, int elements) {
		require(elements * Integer.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.intsToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
		}
	}

	@Override
	public void writeLongs(long[] src, int offset, int elements) {
		require(elements * Long.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.longsToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
		}
		addRequested();
	}

	@Override
	public void writeFloats(float[] src, int offset, int elements) {
		require(elements * Float.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.floatsToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
		}
		addRequested();
	}

	@Override
	public void writeDoubles(double[] src, int offset, int elements) {
		require(elements * Double.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.doublesToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
		}
		addRequested();
	}

	@Override
	public void writeChars(char[] src, int offset, int elements) {
		require(elements * Character.BYTES);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.charsToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
		}
		addRequested();
	}

	@Override
	public void writeBooleans(boolean[] src, int offset, int elements) {
		require(elements * 1);
		if (NativeUtils.NATIVES_ENABLED) {
			NativeUtils.booleansToNative(address + position, src, offset, elements, swapEndianess);
		} else {
			
			
		}
		addRequested();
	}

	@Override
	public void writeBooleansCompact(boolean[] src, int offset, int elements) {
	}
	
	private void addRequested() {
		position += requested;
	}
}