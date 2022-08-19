/*
 * @(#)DefaultInjectionPointFactory.java	 2017-02-17
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import com.google.common.base.Supplier;
import com.thwt.core.annotation.service.LocalService;
import com.thwt.core.annotation.service.Optional;
import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.MemberModel;
import com.thwt.core.codegen.service.model.InjectionFieldInitializerGenerator;
import com.thwt.core.codegen.service.model.InjectionPointModel;
import com.thwt.core.codegen.service.model.InjectionPointModelFactory;
import com.thwt.core.codegen.service.model.InvocationMode;
import com.thwt.core.codegen.service.model.InvocationModelFactory;
import com.thwt.core.codegen.service.model.ServiceVerticleModel;
import com.thwt.core.codegen.service.model.TargetInvocationModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.service.AbstractServiceInjectionPoint;
import com.thwt.core.service.spi.InjectionPoint;

import io.vertx.core.ServiceHelper;

/**
 * @author Neil Lin
 *
 */
public class DefaultInjectionPointFactory implements InjectionPointModelFactory {

  public static final String TARGET = "default";
  
  private List<InjectionFieldInitializerGenerator> targetGenerators = new ArrayList<InjectionFieldInitializerGenerator>();
  {
    for(InjectionFieldInitializerGenerator gen : ServiceHelper.loadFactories(InjectionFieldInitializerGenerator.class, this.getClass().getClassLoader())) {
      if(gen.applyableOnTarget(TARGET)) {
        targetGenerators.add(gen);
      }
    }
  }

  public InjectionPointModel createInjectionPoint(ICodeGenerationContext context, ServiceVerticleModel verticle,
      MemberModel targetModel) {
    Element element = targetModel.getElement();
    TargetInvocationModel invocation = InvocationModelFactory.Factory.getFactory().createInvocationModel(context, verticle, targetModel);
    invocation.validateTarget(context, InvocationMode.Injection);
    InjectionPointModel field = new InjectionPointModel(verticle);
    field.setServiceInjection(element.getAnnotation(LocalService.class) != null);
    field.setOptional(element.getAnnotation(Optional.class) != null);
    field.setName(targetModel.getName()+"Ijp");
    field.setModifiers(Collections.singleton(Modifier.PRIVATE));
    field.setType(InjectionPoint.class.getName());
    field.setTarget(invocation);
    if(field.isServiceInjection()) {
      verticle.addField(field);
      invocation.collectImports(verticle.getImportManager());
      generateServiceInjectionFieldInitializer(context,verticle,field,element);
    }else{
      InjectionFieldInitializerGenerator gen = getInitializerGenerator(field.getInjectedValueType(), element);
      if(gen == null) {
        context.reportException(new CodeGenException("Cannot find injection code generator for inject point :["+targetModel+"]"), element);
        field = null;
      }else{
        verticle.addField(field);
        invocation.collectImports(verticle.getImportManager());
        gen.generateInjectionFieldInitializer(context, verticle, field, element);
      }
    }
    return field;
  }
  
  public void generateServiceInjectionFieldInitializer(final ICodeGenerationContext context, final ClassModel classModel, final InjectionPointModel field, final Element element) {
    classModel.importClass(AbstractServiceInjectionPoint.class.getName());
    classModel.importClass(InjectionPoint.class.getName());
    field.setInitializer(new Supplier<String>() {
      String text = null;
      
      @Override
      public String get() {
        if(this.text == null) {
          Map<String, Object> attributes = new HashMap<String, Object>();
          attributes.put("model", field);
          attributes.put("helper", ModelHelper.getInstance());
          this.text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/serviceInjectionInitializer.vm", attributes);
        }
        return this.text;
      }
    });
  }

  public InjectionFieldInitializerGenerator getInitializerGenerator(String clazz, Element elem) {
    for(InjectionFieldInitializerGenerator gen : this.targetGenerators) {
      if(gen.applyableOnType(clazz, elem)) {
        return gen;
      }
    }
    return null;
  }
}
