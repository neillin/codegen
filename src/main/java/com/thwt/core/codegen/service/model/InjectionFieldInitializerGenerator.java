/*
 * @(#)InjectionFieldInitializerGenerator.java	 2017-02-17
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import javax.lang.model.element.Element;

import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;

/**
 * @author Neil Lin
 *
 */
public interface InjectionFieldInitializerGenerator {
  void generateInjectionFieldInitializer(ICodeGenerationContext context, ClassModel classModel, InjectionPointModel field, Element element);
  boolean applyableOnTarget(String target);
  boolean applyableOnType(String valueType, Element elem);
}
