/*
 * @(#)DAObjectInjectionGenerator.java	 2017-02-17
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.service.model.InjectionFieldInitializerGenerator;
import com.thwt.core.codegen.service.model.InjectionPointModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.jpa.annotation.DAObject;
import com.thwt.core.jpa.api.GenericDAO;
import com.thwt.core.service.AbstractInjectionPoint;
import com.thwt.core.service.spi.InjectionContext;
import com.thwt.core.service.spi.InjectionPoint;

/**
 * @author Neil Lin
 *
 */
public class DAObjectInjectionGenerator implements InjectionFieldInitializerGenerator {

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InjectionFieldInitializerGenerator#generateInjectionFieldInitializer(com.thwt.core.codegen.ICodeGenerationContext, com.thwt.core.codegen.model.ClassModel, com.thwt.core.codegen.service.model.InjectionPointModel, javax.lang.model.element.Element)
   */
  @Override
  public void generateInjectionFieldInitializer(ICodeGenerationContext context,
      ClassModel classModel, InjectionPointModel field, Element element) {
    classModel.importClass(AbstractInjectionPoint.class.getName());
    classModel.importClass(InjectionPoint.class.getName());
    classModel.importClass(InjectionContext.class.getName());
    classModel.importClass("com.thwt.core.jpa.DAOFactory");
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("model", field);
    attributes.put("helper", ModelHelper.getInstance());
    String text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/daobjectInjectionInitializer.vm", attributes);
    field.setInitializer(text);

  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InjectionFieldInitializerGenerator#applyableOnTarget(java.lang.String)
   */
  @Override
  public boolean applyableOnTarget(String target) {
    return DefaultInjectionPointFactory.TARGET.equals(target);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InjectionFieldInitializerGenerator#applyableOnType(java.lang.String)
   */
  @Override
  public boolean applyableOnType(String valueType, Element elem) {
    return valueType.startsWith(GenericDAO.class.getCanonicalName()) == false && elem.getAnnotation(DAObject.class) != null;
  }

}
