/*
 * @(#)ServiceOptionsModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.google.common.base.Preconditions;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.model.FieldModel;

/**
 * @author Neil Lin
 *
 */
public class ServiceOptionsModel extends ClassModelImpl {
	private final ServiceMBeanModel targetObject;

	public ServiceOptionsModel(ServiceMBeanModel target) {
		super(Preconditions.checkNotNull(target).generateOptionsClassName());
		this.targetObject = target;
		this.targetObject.getTypeInfo().collectImports(getImportManager());
	}

	/**
	 * @return the targetObjectClass
	 */
	public String getTargetObjectClass() {
		return targetObject.getName();
	}

	/**
	 * @return the targetObject
	 */
	public ServiceMBeanModel getTargetObject() {
		return targetObject;
	}

	/* 
	 * @see com.thwt.core.codegen.model.ClassModelImpl#addField(com.thwt.core.codegen.model.FieldModel)
	 */
	@Override
	public void addField(FieldModel field) {
		super.addField(field);
		importClass(field.getType());
	}


}
