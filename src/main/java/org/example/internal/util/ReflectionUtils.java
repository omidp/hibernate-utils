package org.example.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ReflectionUtils {
	private ReflectionUtils() {
		throw new UnsupportedOperationException("The " + getClass() + " is not instantiable!");
	}

	public static <T> Class<T> getClass(String className) {
		try {
			return (Class<T>) Class.forName(className, false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Set<Class> getGenericTypes(ParameterizedType parameterizedType) {
		Set<Class> genericTypes = new LinkedHashSet<>();
		for (Type genericType : parameterizedType.getActualTypeArguments()) {
			if (genericType instanceof Class) {
				genericTypes.add((Class) genericType);
			}
		}
		return genericTypes;
	}

	public static Method getMethodOrNull(Object target, String methodName, Class... parameterTypes) {
		try {
			return getMethod(target.getClass(), methodName, parameterTypes);
		} catch (RuntimeException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static Method getMethod(Class targetClass, String methodName, Class... parameterTypes) {
		try {
			return targetClass.getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			try {
				return targetClass.getMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException ignore) {
			}

			if (!targetClass.getSuperclass().equals(Object.class)) {
				return getMethod(targetClass.getSuperclass(), methodName, parameterTypes);
			} else {
				throw new RuntimeException(e);
			}
		}
	}

}