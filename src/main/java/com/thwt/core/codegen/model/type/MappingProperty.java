/*
 * @(#)MappingProperty.java	2017-12-11
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * @author Neil Lin
 *
 */
public class MappingProperty extends PropertyInfo {

	private Map<String, PropertyInfo> mappingProperties = new HashMap<>();
	private PropertyInfo keyProperty;
	
	/**
	 * @param declared
	 * @param name
	 * @param doc
	 * @param type
	 * @param setterMethod
	 * @param adderMethod
	 * @param getterMethod
	 * @param kind
	 * @param jsonifiable
	 */
	public MappingProperty(boolean declared, String name, Doc doc, TypeInfo type, String setterMethod,
			String adderMethod, String getterMethod, PropertyKind kind, boolean jsonifiable) {
		super(declared, name, doc, type, setterMethod, adderMethod, getterMethod, kind, jsonifiable);
	}
	
	public MappingProperty(PropertyInfo info, boolean readonly) {
		super(info.declared, info.name, info.doc, info.type, readonly ? null : info.setterMethod, readonly ? null : info.adderMethod, info.getterMethod, info.kind, info.jsonifiable);
	}

	/**
	 * @return the keyProperty
	 */
	public PropertyInfo getKeyProperty() {
		return keyProperty;
	}

	/**
	 * @param keyProperty the keyProperty to set
	 */
	public void setKeyProperty(PropertyInfo keyProperty) {
		this.keyProperty = keyProperty;
	}
	
	public MappingProperty addMappingProperty(PropertyInfo prop) {
		this.mappingProperties.put(prop.getName(), prop);
		return this;
	}
	
	public MappingProperty removeMappingProperty(String prop) {
		this.mappingProperties.remove(prop);
		return this;
	}
	
	public List<PropertyInfo> getMappingProperties() {
		return Lists.newArrayList(this.mappingProperties.values());
	}
	
	public PropertyInfo getMappingProperty(String prop) {
		return this.mappingProperties.get(prop);
	}

}
