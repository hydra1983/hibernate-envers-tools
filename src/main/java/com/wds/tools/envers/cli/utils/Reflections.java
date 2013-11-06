package com.wds.tools.envers.cli.utils;

import java.lang.reflect.Field;

public class Reflections {
	public static Field getField(Class<?> javaType, String fieldName) {
		Field field = null;
		try {
			field = javaType.getDeclaredField(fieldName);
		} catch (Exception e) {
			if (field == null && javaType.getSuperclass() != Object.class) {
				field = getField(javaType.getSuperclass(), fieldName);
			}
		}

		return field;
	}

	public static Object getValue(Object instance, String fieldName) {
		Object value = null;
		if (instance != null) {
			Class<?> javaType = instance.getClass();
			try {
				Field field = getField(javaType, fieldName);
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				value = field.get(instance);
				field.setAccessible(accessible);
			} catch (Exception e) {
				throw Exceptions.runtime(e);
			}
		}
		return value;
	}
}
