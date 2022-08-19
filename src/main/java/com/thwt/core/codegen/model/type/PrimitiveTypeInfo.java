package com.thwt.core.codegen.model.type;

import java.util.HashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PrimitiveTypeInfo extends TypeInfo {

	static final HashMap<String, PrimitiveTypeInfo> PRIMITIVES = new HashMap<>();

	static {
		java.lang.Class<?>[] primitives = { boolean.class, byte.class, short.class, int.class, long.class, float.class,
				double.class, char.class };
		java.lang.Class<?>[] boxes = { Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
				Double.class, Character.class };
		String[] tsNames = { "boolean", "number", "number", "number", "number", "number", "number", "string" };
		String[] dartNames = { "bool", "int", "int", "int", "int", "double", "double", "String" };
		for (int i = 0; i < primitives.length; i++) {
			java.lang.Class<?> primitive = primitives[i];
			String name = primitive.getName();
			PRIMITIVES.put(name,
					new PrimitiveTypeInfo(primitive.getName(), boxes[i].getName(), tsNames[i], dartNames[i]));
		}
	}

	final String name, tsName, dartName;
	final String boxedName;

	private PrimitiveTypeInfo(String name, String boxedName, String ts, String dart) {
		this.name = name;
		this.boxedName = boxedName;
		this.tsName = ts;
		this.dartName = dart;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PrimitiveTypeInfo) {
			return name.equals(((PrimitiveTypeInfo) obj).name);
		}
		return false;
	}

	/**
	 * @return the boxed equivalent
	 */
	public ClassTypeInfo getBoxed() {
		return ClassTypeInfo.PRIMITIVES.get(boxedName);
	}

	@Override
	public ClassKind getKind() {
		return ClassKind.PRIMITIVE;
	}

	@Override
	public String format(boolean qualified) {
		return name;
	}

	/*
	 * @see com.thwt.core.codegen.model.type.TypeInfo#getTypescriptName()
	 */
	@Override
	public String getTypescriptName() {
		return this.tsName;
	}

	@Override
	public String getDartName() {
		return this.dartName;
	}
}
