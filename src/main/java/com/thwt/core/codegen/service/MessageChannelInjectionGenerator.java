/*
 * @(#)MessageChannelInjectionGenerator.java	 2017-02-17
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

import com.google.common.base.Supplier;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.service.model.InjectionFieldInitializerGenerator;
import com.thwt.core.codegen.service.model.InjectionPointModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.event.MessageAddress;
import com.thwt.core.event.MessageHeader;
import com.thwt.core.event.MessageHeaders;
import com.thwt.core.event.MessageChannel;
import com.thwt.core.service.AbstractInjectionPoint;
import com.thwt.core.service.MessageChannelImpl;
import com.thwt.core.service.spi.InjectionContext;
import com.thwt.core.service.spi.InjectionPoint;
import com.thwt.core.vertx.VertxKernelContext;

import io.vertx.core.eventbus.EventBus;

/**
 * @author Neil Lin
 *
 */
public class MessageChannelInjectionGenerator implements InjectionFieldInitializerGenerator {

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InjectionFieldInitializerGenerator#generateInjectionFieldInitializer(com.thwt.core.codegen.ICodeGenerationContext, com.thwt.core.codegen.model.ClassModel, com.thwt.core.codegen.service.model.InjectionPointModel, javax.lang.model.element.Element)
   */
  @Override
  public void generateInjectionFieldInitializer(ICodeGenerationContext context,
      ClassModel classModel, InjectionPointModel field, Element element) {
    classModel.importClass(AbstractInjectionPoint.class.getName());
    classModel.importClass(InjectionPoint.class.getName());
    classModel.importClass(InjectionContext.class.getName());
    classModel.importClass(EventBus.class.getName());
    classModel.importClass(VertxKernelContext.class.getName());
    classModel.importClass(MessageChannel.class.getName());
    classModel.importClass(MessageChannelImpl.class.getName());
    classModel.importClass(Supplier.class.getName());
    String posterType = element.asType().toString();
    String messageType = posterType.substring(MessageChannel.class.getName().length()+1,posterType.length()-1);
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("model", field);
    attributes.put("clazz", classModel.importClass(messageType));
    MessageAddress channel = element.getAnnotation(MessageAddress.class);
    if(channel != null) {
      attributes.put("channel", channel);
    }
    ArrayList<MessageHeader> headers = new ArrayList<MessageHeader>();
    MessageHeaders headersAnn = element.getAnnotation(MessageHeaders.class);
    if(headersAnn != null) {
      for (MessageHeader h : headersAnn.value()) {
        headers.add(h);
      }
    }
    MessageHeader headerAnn = element.getAnnotation(MessageHeader.class);
    if(headerAnn != null) {
      headers.add(headerAnn);
    }
    attributes.put("headers", headers);
    attributes.put("helper", ModelHelper.getInstance());
    String text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/messagePosterInjectionInitializer.vm", attributes);
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
    return valueType.startsWith(MessageChannel.class.getCanonicalName()+"<");
  }

}
