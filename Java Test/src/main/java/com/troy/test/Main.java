package com.troy.test;

import java.io.*;

import com.troy.serialization.*;
import com.troy.serialization.io.*;
import com.troy.serialization.util.*;
import com.troy.testframework.*;

import sun.misc.*;

public class Main {

	public static void main(String[] args) throws Throwable {

		Library.doTest(new File("./.dat"), Constants.BIG_HARRY_POTTER);
		System.exit(0);

		Unsafe unsafe = MiscUtil.getUnsafe();

		NativeFileOutput out = new NativeFileOutput(new File("./test.dat"));
		NativeMemoryBlock block = out.map(100);
		unsafe.putLong(block.address(), 0xAABBCCDDEEFF0102L);
		block.setPosition(8);
		out.unmap(block);
		// out.writeInt(0xAABBCCDD);
		System.out.println(block);
		out.close();

		System.exit(0);
		OutputSerializationStream stream = new OutputSerializationStream(new File("./test.dat"));
		stream.writeString(Constants.HARRY_POTTER);
		stream.close();

		// me - 51,550
		// utf8 - 70,262

	}

}