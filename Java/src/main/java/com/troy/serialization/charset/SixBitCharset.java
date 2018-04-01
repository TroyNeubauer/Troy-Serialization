package com.troy.serialization.charset;

import com.troy.serialization.exception.UnsupportedCharacterException;
import com.troy.serialization.io.*;
import com.troy.serialization.util.*;

public class SixBitCharset implements TroyCharset {
	static {
		NativeUtils.init();
	}
	public static final byte CODE = 0b01;
	public static final int MIN_VALUE = 0, MAX_VALUE = 63;
	private static final int MASK = 0b00111111;
	// maps a four bit code -> java char value
	// format:off
	public static final char[] DECODING_CACHE = new char[] { ' ', '.', '!', '?', ',', '\'', '\"', ';', ':', '+', '-', '_', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', };
	// format:on

	// maps a java char -> four bit code
	private static final int[] ENCODING_CACHE = SerializationUtils.constructEncodingFromDecoding(DECODING_CACHE);

	@Override
	public void decode(Input src, char[] dest, int destOffset, int chars) {

	}

	private static native void encode(char[] src, long dest, int srcOffset, int chars, boolean chackForErrors);

	@Override
	public void encode(char[] src, Output dest, int srcOffset, int chars, boolean checkForErrors) {
		if (NativeUtils.NATIVES_ENABLED) {
			encode(src, dest, srcOffset, chars, checkForErrors);
		} else {
			int i = srcOffset;
			int result;
			final int end = (chars / 4) * 4;// round down to next multiple of two
			while (i < end) {
				int c0 = ENCODING_CACHE[src[i++]];
				if (c0 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c1 = ENCODING_CACHE[src[i++]];
				if (c1 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c2 = ENCODING_CACHE[src[i++]];
				if (c2 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c3 = ENCODING_CACHE[src[i++]];
				if (c3 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				result = c3;
				result |= c2 << 6;
				result |= c1 << 12;
				result |= c0 << 18;

				dest.writeByte((byte) ((result >> 16) & 0xFF));
				dest.writeByte((byte) ((result >> 8) & 0xFF));
				dest.writeByte((byte) ((result >> 0) & 0xFF));
			}
			if (chars % 4 == 3) {// Write the remaining byte if there was an odd number
				int c0 = ENCODING_CACHE[src[i++]];
				if (c0 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c1 = ENCODING_CACHE[src[i++]];
				if (c1 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c2 = ENCODING_CACHE[src[i++]];
				if (c2 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				result = 0;
				result |= c2 << 0;
				result |= c1 << 6;
				result |= c0 << 12;

				dest.writeByte((byte) ((result >> 16) & 0xFF));
				dest.writeByte((byte) ((result >> 8) & 0xFF));
				dest.writeByte((byte) ((result >> 0) & 0xFF));
			} else if (chars % 4 == 2) {// Write the remaining byte if there was an odd number
				int c0 = ENCODING_CACHE[src[i++]];
				if (c0 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				int c1 = ENCODING_CACHE[src[i++]];
				if (c1 == -1)
					throw new UnsupportedCharacterException(src[--i], i, this);
				result = c1 << 0;
				result |= c0 << 6;

				dest.writeByte((byte) ((result >> 8) & 0xFF));
				dest.writeByte((byte) ((result >> 0) & 0xFF));
			} else if (chars % 4 == 1) {// Write the remaining byte if there was an odd number
				System.out.println("one char remain " + src[end]);
				dest.writeByte((byte) (ENCODING_CACHE[src[end]]));
			}
		}
	}

	@Override
	public float getMinCharactersPerByte() {
		return 8.0f / (6.0f * 2.0f);// Usually 4 chars per 3 bytes but if we have three capital letters its 4 chars
									// in 6 bytes
	}

	@Override
	public int getCharsetCode() {
		return CODE;
	}

	@Override
	public String name() {

		return "6Bit-Encoding";
	}
	
	@Override
	public byte getOpCode() {
		return 0b01;
	}

	@Override
	public void init() {
	}
	
	@Override
	public char[] getDecodingCache() {
		return DECODING_CACHE;
	}

	@Override
	public int[] getEncodingCache() {
		return ENCODING_CACHE;
	}

}