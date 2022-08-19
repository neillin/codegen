/*
 * @(#)JsonizableConverterModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Preconditions;

import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.json.api.IJsonArray;
import com.thwt.core.json.api.JsonHelper;
import com.thwt.core.util.BiConsumer;
import com.thwt.core.util.Consumer;

/**
 * @author Neil Lin
 *
 */
public class JsonizableConverterModel extends ClassModelImpl {
	private final JsonizableModel target;

	public JsonizableConverterModel(JsonizableModel target) {
		super(Preconditions.checkNotNull(target).getClassName()+"JsonConverter");
		this.target = target;
		this.target.getTypeInfo().collectImports(getImportManager());
		boolean requiredArrayConverting = false;
		boolean requireSet = false;
		boolean requireList = false;
		boolean requireMap = false;
		boolean requireConsumer = false;
		boolean requireBiConsumer = false;
		boolean jsonizable = false;
		for(PropertyInfo pInfo : this.target.getPropertyMap().values()) {
			pInfo.collectImports(getImportManager());
			if(pInfo.isArray()) {
				requiredArrayConverting = true;
			}else if(pInfo.isList()) {
				requiredArrayConverting = true;
				if(pInfo.getAdderMethod() != null) {
					requireConsumer = true;
				}else{
					requireList = true;
				}
			}else if(pInfo.isSet()) {
				requiredArrayConverting = true;
				if(pInfo.getAdderMethod() != null) {
					requireConsumer = true;
				}else{
					requireSet = true;
				}
			}else if(pInfo.isMap()) {
				requiredArrayConverting = true;
				if(pInfo.getAdderMethod() != null) {
					requireBiConsumer = true;
				}else{
					requireMap = true;
				}
			}else if(pInfo.isJsonifiable()) {
				jsonizable = true;
			}
		}
		getImportManager().importClass(JsonHelper.class.getName());
		if(requiredArrayConverting) {
			getImportManager().importClass(IJsonArray.class.getName());
		}
		if(requireList) {
			getImportManager().importClass(ArrayList.class.getName());
		}
		if(requireSet) {
			getImportManager().importClass(HashSet.class.getName());
		}
		if(requireMap) {
			getImportManager().importClass(HashMap.class.getName());
		}
		if(requireConsumer) {
			getImportManager().importClass(Consumer.class.getName());
		}
		if(requireBiConsumer) {
			getImportManager().importClass(BiConsumer.class.getName());
		}
		if(jsonizable) {
			getImportManager().importClass(JsonHelper.class.getName());
		}
	}

	/**
	 * @return the optionsName
	 */
	public String getJsonName() {
		return this.target.getJsonName();
	}


	public String getCategory() {
		return this.target.getCategory();
	}

	/**
	 * @return the optionsClass
	 */
	public String getTargetClass() {
		return this.target.getTypeInfo().getSimpleName();
	}

	/**
	 * @return the options
	 */
	public JsonizableModel getTarget() {
		return this.target;
	}

	/**
	 * return properties which has setter/adder method
	 * @return
	 */
	public List<PropertyInfo> getFromProperties() {
		return this.target.getFromProperties();
	}

	/**
	 * return properties which has setter/adder method
	 * @return
	 */
	public List<PropertyInfo> getToProperties() {
		return this.target.getToProperties();
	}

}
