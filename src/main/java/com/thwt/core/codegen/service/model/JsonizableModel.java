/*
 * @(#)JsonizableModel.java   2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.thwt.core.annotation.JsonField;
import com.thwt.core.annotation.JsonMapping;
import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.Case;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.type.ArrayType;
import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.MappingProperty;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.codegen.model.type.PropertyKind;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.util.Utils;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonizableModel extends JavaBeanModel {
	
	private static Map<String, JsonizableModel> models = new HashMap<String, JsonizableModel>();
	
	public static JsonizableModel getJsonizableModel(String name) {
		return models.get(name);
	}
	
	private static class FieldMappings {
		ExecutableElement getterElt;
		ExecutableElement setterElt;
		JsonMapping mapAnn;
	}
	
	private Map<String, FieldMappings> mappings;


	protected boolean generateConverter;
	protected boolean inheritConverter;
	protected String category;
	protected String jsonName;
	protected Map<String, String> jsonFieldNames;
	
	public JsonizableModel(TypeElement elem) {
		super(Preconditions.checkNotNull(elem));
		models.put(getName(), this);
	}

	public boolean getGenerateConverter() {
		return generateConverter;
	}

	public boolean getInheritConverter() {
		return inheritConverter;
	}

	/*
	 * @see com.thwt.core.codegen.service.model.JavaBeanModel#doProcess()
	 */
	@Override
	protected void doProcess() {
		processJsoniableAnnotation();
		super.doProcess();
	}

	/**
	 * 
	 */
	protected void processJsoniableAnnotation() {
		Jsonizable ann = getElement().getAnnotation(Jsonizable.class);
		//    this.inheritConverter = ann.inheritConverter();
		this.category = ann != null ? ann.category() : null;
		this.jsonName = ann == null || Utils.isBlank(ann.jsonName()) ? getTypeInfo().getSimpleName(Case.LOWER_CAMEL) : ann.jsonName();
		this.generateConverter = ann != null ? ann.generateConverter() : false;
	}


	/**
	 * @return the category
	 */
	public String getCategory() {
		return Utils.isBlank(this.category) ? "Default" : this.category;
	}

	/**
	 * @return the jsonName
	 */
	public String getJsonName() {
		return jsonName;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @param jsonName
	 *            the jsonName to set
	 */
	public void setJsonName(String jsonName) {
		this.jsonName = jsonName;
	}
	

	private void addProperty0(PropertyInfo property) {
		if(this.jsonFieldNames != null && this.jsonFieldNames.containsKey(property.getName())) {
			property.setMappingName(this.jsonFieldNames.get(property.getName()));
		}
		super.addProperty(property);
	}

	protected synchronized void addJsonFieldName(String name, String jsonFieldName) {
		if(this.jsonFieldNames == null) {
			this.jsonFieldNames = new HashMap<>();
		}
		this.jsonFieldNames.put(name, jsonFieldName);
	}
	
	private void processMethod0(String name, ExecutableElement getterElt, ExecutableElement setterElt,
			ExecutableElement adderElt) {
		JsonField ann = getPropertyAnnotation(getterElt, setterElt, JsonField.class);
		if(ann != null) {
			this.addJsonFieldName(name, ann.value().trim());
		}
		super.processMethod(name, getterElt, setterElt, adderElt);
	}
	  
	@Override
	protected void processMethod(String name, ExecutableElement getterElt, ExecutableElement setterElt,
			ExecutableElement adderElt) {
		JsonMapping ann = getPropertyAnnotation(getterElt, setterElt, JsonMapping.class);
		if(ann == null) { 
			this.processMethod0(name, getterElt, setterElt, adderElt);
		} else if(!ann.mapping()){
			return;
		} else {
			if(ann.fields().length > 0 || ann.readonly() || ann.nullValue() || (! Utils.isBlank(ann.tsName()))) {
				FieldMappings fm = new FieldMappings();
				fm.getterElt = getterElt;
				fm.mapAnn = ann;
				fm.setterElt = setterElt;
				if(this.mappings == null) {
					this.mappings = new HashMap<>();
				}
				this.mappings.put(name, fm);
			}
			this.processMethod0(name, getterElt, setterElt, adderElt);
		}
	}

	/* 
	 * @see com.thwt.core.codegen.service.model.JavaBeanModel#addProperty(com.thwt.core.codegen.model.type.PropertyInfo)
	 */
	@Override
	public void addProperty(PropertyInfo property) {
		FieldMappings mapping = this.mappings != null ? this.mappings.get(property.getName()) : null;
		if(mapping != null) {
			if(!Utils.isBlank(mapping.mapAnn.tsName())) {
				property.setTsName(mapping.mapAnn.tsName());
			}
			if(mapping.mapAnn.fields().length > 0) {
				if(!isMappingableType(property.getType())) {
					throw new MustFailedCodeGenException("Property :[ "+property.getName()+"] of class :[ "+ getClassName()+" ] is not mappingable ! ("+property.getType().getName()+")");
				}
				Elements elemUtil = MainAnnotationProcessor.getCurrentContext().getProcessingEnvironment().getElementUtils();
				TypeElement elem = elemUtil.getTypeElement(getMappingableTypeName(property.getType()));
				JavaBeanModel javaBeanModel = new JavaBeanModel(elem);
				String keyField =  Strings.emptyToNull(mapping.mapAnn.key());
				MappingProperty mProperty = new MappingProperty(property, keyField == null|| mapping.mapAnn.readonly());
				for(String name : mapping.mapAnn.fields()) {
					PropertyInfo info = javaBeanModel.getProperty(name);
					if(info == null) {
						throw new MustFailedCodeGenException("Cannot find mapping property :[ "+name+"] of class :[ "+ property.getType().getName()+" ] !");
					}
					mProperty.addMappingProperty(info);
				}
				if(keyField != null && mProperty.getMappingProperty(keyField) == null) {
					PropertyInfo info = javaBeanModel.getProperty(keyField);
					if(info == null) {
						throw new MustFailedCodeGenException("Cannot find mapping property :[ "+keyField+"] of class :[ "+ property.getType().getName()+" ] !");
					}
					mProperty.addMappingProperty(info);
					mProperty.setKeyProperty(info);
				}
				this.addProperty0(mProperty);
			}else if(mapping.mapAnn.nullValue()) {
				this.addProperty0(new PropertyInfo(property.isDeclared(), property.getName(), property.getDoc(), property.getType(), null, null, 
						property.getGetterMethod(), PropertyKind.NULL, property.isJsonifiable()));
			}else if(mapping.mapAnn.readonly()) {
				this.addProperty0(new PropertyInfo(property.isDeclared(), property.getName(), property.getDoc(), property.getType(), null, null, 
						property.getGetterMethod(), property.getKind(), property.isJsonifiable()));
			}
		}else {
			this.addProperty0(property);
		}
	}
	
	private boolean isMappingableType(TypeInfo type) {
		return type.getKind() == ClassKind.DATA_OBJECT || type.getKind() == ClassKind.OTHER || (type.getKind() == ClassKind.ARRAY && isMappingableType(((ArrayType)type).getComponentType()));
	}
	
	private String getMappingableTypeName(TypeInfo type) {
		return type.getKind() == ClassKind.ARRAY ? ((ArrayType)type).getComponentType().getName() : type.getName();
	}

		  

}
