package com.jme3x.jfx.util;

import java.nio.ByteBuffer;

public class FormatUtils {

	//TODO benchmark
	public static Void reorder_ARGB82ABGR8(ByteBuffer data){
		int limit = data.limit() - 3;
		byte v;
		for (int i = 0; i < limit; i += 4) {
			v = data.get(i+1);
			data.put(i + 1, data.get(i+3) );
			data.put(i + 3, v );
		}
		return null;
	}

	//TODO benchmark
	public static Void reorder_BGRA82ABGR8(ByteBuffer data) {
		int limit = data.limit() - 3;
		byte v0, v1, v2, v3;
		for (int i = 0; i < limit; i += 4) {
			v0 = data.get(i + 0);
			v1 = data.get(i + 1);
			v2 = data.get(i + 2);
			v3 = data.get(i + 3);
			data.put(i + 0, v3);
			data.put(i + 1, v0);
			data.put(i + 2, v1);
			data.put(i + 3, v2);
		}
		return null;
	}

}
