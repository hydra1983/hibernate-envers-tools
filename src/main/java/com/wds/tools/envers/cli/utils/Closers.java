package com.wds.tools.envers.cli.utils;

import com.google.common.io.Closer;

public class Closers {
	public static void close(Void voidCallback) {
		Closer closer = Closer.create();
		try {
			try {
				voidCallback.call(closer);
			} catch (Throwable e) {
				throw closer.rethrow(e);
			} finally {
				closer.close();
			}
		} catch (Exception e) {
			throw Exceptions.runtime(e);
		}
	}
	
	public static void close(Return returnCallback) {
		Closer closer = Closer.create();
		try {
			try {
				returnCallback.call(closer);
			} catch (Throwable e) {
				throw closer.rethrow(e);
			} finally {
				closer.close();
			}
		} catch (Exception e) {
			throw Exceptions.runtime(e);
		}
	}

	public static interface Void {
		void call(Closer closer) throws Throwable;
	}

	public static interface Return {
		Object call(Closer closer) throws Throwable;
	}
}
