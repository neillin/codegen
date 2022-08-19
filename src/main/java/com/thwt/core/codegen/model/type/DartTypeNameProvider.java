/*
 * @(#)TyscriptNameProvider.java	2018-02-01
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model.type;

/**
 * @author Neil Lin
 *
 */
public interface DartTypeNameProvider {
	String getDartName(String javaClassName);
}
