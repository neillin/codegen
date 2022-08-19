/*
 * @(#)EventMessageHandler.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.google.common.base.Preconditions;
import com.thwt.core.codegen.model.ImportManager;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.service.AbstractMessageHandler;

import io.vertx.core.eventbus.Message;

/**
 * @author Neil Lin
 *
 */
public class EventMessageHandler {
  private String channel;
  private TargetInvocationModel target;
  private TypeInfo clazz;
  
  /**
   * @return the clazz
   */
  public TypeInfo getClazz() {
    return clazz;
  }
  /**
   * @param clazz the clazz to set
   */
  public void setClazz(TypeInfo clazz) {
    this.clazz = Preconditions.checkNotNull(clazz);
  }
  
  /**
   * @return the target
   */
  public TargetInvocationModel getTarget() {
    return target;
  }
  
  /**
   * @param target the target to set
   */
  public void setTarget(TargetInvocationModel target) {
    this.target = target;
  }
  
  public String generateGetStatement(String targetObjectName, String className) {
    if(this.target.isFieldTarget()) {
      return this.target.generateGetStatement(targetObjectName);
    }else{
      return this.target.generateCallStatement(targetObjectName, className);
    }
  }
  /**
   * @return the channel
   */
  public String getChannel() {
    return channel;
  }
  /**
   * @param channel the channel to set
   */
  public void setChannel(String channel) {
    this.channel = channel;
  }
  
  public void collectImports(ImportManager mgr) {
    this.target.collectImports(mgr);
    mgr.importClass(AbstractMessageHandler.class.getName());
    mgr.importClass(Message.class.getName());
    this.clazz.collectImports(mgr);
  }

 }
