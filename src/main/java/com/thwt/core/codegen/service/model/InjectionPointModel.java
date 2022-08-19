/*
 * @(#)InjectionPointModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.google.common.base.Preconditions;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.MethodModel;

/**
 * @author Neil Lin
 *
 */
public class InjectionPointModel extends FieldModel{
  

  private boolean optional;
  private boolean serviceInjection;
  private TargetInvocationModel target;
  
  /**
   * @param model
   */
  public InjectionPointModel(ClassModel model) {
    super(model);
  }

  /**
   * @return the optional
   */
  public boolean isOptional() {
    return optional;
  }
  /**
   * @param optional the optional to set
   */
  public void setOptional(boolean optional) {
    this.optional = optional;
  }
  /**
   * @return the serviceInjection
   */
  public boolean isServiceInjection() {
    return serviceInjection;
  }
  /**
   * @param serviceInjection the serviceInjection to set
   */
  public void setServiceInjection(boolean serviceInjection) {
    this.serviceInjection = serviceInjection;
  }
  
  public String getTargetClassName() {
    return getClassModel() != null ? getClassModel().importClass(this.getInjectedValueType()) : this.getInjectedValueType();
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
    this.target = Preconditions.checkNotNull(target);
    this.target.setThisClass(getClassModel());
  }
//  /* (non-Javadoc)
//   * @see com.thwt.core.codegen.model.FieldModel#setClassModel(com.thwt.core.codegen.model.ClassModel)
//   */
//  @Override
//  public void setClassModel(ClassModel classModel) {
//    super.setClassModel(classModel);
//    if(this.target != null) {
//      this.target.setThisClass(classModel);
//    }
//  }
  
  public String generateInjectStatement(String targetObjectName, String... argNames) {
    if(this.target.isFieldTarget()) {
      return this.target.generateSetStatement(targetObjectName, argNames[0]);
    }else{
      return this.target.generateCallStatement(targetObjectName, argNames);
    }
  }

  public String getInjectedValueType() {
	  if(this.target.isFieldTarget()) {
		  return this.target.getTargetObject().getType();
	  }else {
		  return ((MethodModel)this.target.getTargetObject()).getParameterTypeInfos()[0].getName();
	  }
  }
}
