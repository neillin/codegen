package com.thwt.core.codegen;

public interface ImportOrganizer {

	String[] getTypeImports(String type);

	String getTypeUsage(String type);

}