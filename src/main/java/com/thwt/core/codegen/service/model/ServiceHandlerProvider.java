/*
 * @(#)ServiceHandlerProvider.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

/**
 * @author Neil Lin
 *
 */
public class ServiceHandlerProvider {
  private String clazz;
  private TargetInvocationModel target;
  
  /**
   * @return the clazz
   */
  public String getClazz() {
    return clazz;
  }
  /**
   * @param clazz the clazz to set
   */
  public void setClazz(String clazz) {
    this.clazz = clazz;
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
 }
