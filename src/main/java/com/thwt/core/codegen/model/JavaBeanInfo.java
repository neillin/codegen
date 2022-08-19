/*
 * @(#)JavaBeanInfo.java	2017-12-10
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import java.util.Map;

import com.thwt.core.codegen.model.type.PropertyInfo;

/**
 * @author Neil Lin
 *
 */
public interface JavaBeanInfo {
	/**
	 * @return the name
	 */
	String getName();

	/**
	 * @return the pkgName
	 */
	String getPkgName();


	String getClassName();

	Map<String, PropertyInfo> getPropertyMap();
	
	boolean hasEmptyConstructor();
}
