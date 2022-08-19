/*
 * @(#)GenericDAOInjectionGenerator.java	 2017-02-17
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
import com.thwt.core.jpa.api.GenericDAO;
import com.thwt.core.service.AbstractInjectionPoint;
import com.thwt.core.service.spi.InjectionContext;
import com.thwt.core.service.spi.InjectionPoint;

/**
 * @author Neil Lin
 *
 */
public class GenericDAOInjectionGenerator implements InjectionFieldInitializerGenerator {

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InjectionFieldInitializerGenerator#generateInjectionFieldInitializer(com.thwt.core.codegen.ICodeGenerationContext, com.thwt.core.codegen.model.ClassModel, com.thwt.core.codegen.service.model.InjectionPointModel, javax.lang.model.element.Element)
   */
  @Override
  public void generateInjectionFieldInitializer(ICodeGenerationContext context,
      ClassModel classModel, InjectionPointModel field, Element element) {
    classModel.importClass(AbstractInjectionPoint.class.getName());
    classModel.importClass(InjectionPoint.class.getName());
    classModel.importClass(InjectionContext.class.getName());
    classModel.importClass(GenericDAO.class.getName());
    classModel.importClass("com.thwt.core.jpa.DAOFactory");
    String posterType = element.asType().toString();
    String messageType = posterType.substring(GenericDAO.class.getName().length()+1,posterType.length()-1);
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("model", field);
    attributes.put("clazz", classModel.importClass(messageType));
    attributes.put("helper", ModelHelper.getInstance());
    String text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/genericDAOInjectionInitializer.vm", attributes);
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
    return valueType.startsWith(GenericDAO.class.getCanonicalName()+"<");
  }

}
