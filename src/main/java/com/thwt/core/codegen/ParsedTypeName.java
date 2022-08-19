package com.thwt.core.codegen;

import java.util.List;

import com.google.common.base.Strings;


public class ParsedTypeName {

	private final String packageName;
	private final String className;
	private final List<ParsedTypeNameParam> params;
	private final int arrayDimensions;

	public ParsedTypeName(String packageName, String className,
			List<ParsedTypeNameParam> generics, int arrayDimensions) {
		this.packageName = packageName;
		this.className = className;
		this.params = generics;
		this.arrayDimensions = arrayDimensions;
	}

	public String getPackageName() {
		return packageName;
	}

	public int getArrayDimensions() {
		return arrayDimensions;
	}

	public String getClassName() {
		return className;
	}

	public List<ParsedTypeNameParam> getParams() {
		return params;
	}

	@Override
	public String toString() {
		return getGenericType() + getArrayPart() + "<" + params + ">";
	}

	public String getArrayPart() {
		return Strings.repeat("[]", arrayDimensions);
	}

	public String getGenericType() {
		return packageName + "." + className;
	}

	public boolean isSimple() {
		return packageName == null && params.isEmpty();
	}
}