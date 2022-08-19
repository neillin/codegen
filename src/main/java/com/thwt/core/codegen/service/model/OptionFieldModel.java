/*
 * @(#)OptionFieldModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.MethodModel;

/**
 * @author Neil Lin
 *
 */
public class OptionFieldModel extends FieldModel {

  
  private MethodModel getter, setter;
  private String defaultValue;
  
  /**
   * @param model
   */
  public OptionFieldModel(ClassModel model) {
    super(model);
  }

  /**
   * @return the getter
   */
  public MethodModel getGetter() {
    return getter;
  }
  /**
   * @param getter the getter to set
   */
  public void setGetter(MethodModel getter) {
    this.getter = getter;
  }
  /**
   * @return the setter
   */
  public MethodModel getSetter() {
    return setter;
  }
  /**
   * @param setter the setter to set
   */
  public void setSetter(MethodModel setter) {
    this.setter = setter;
  }

  /**
   * @return the defaultValue
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param defaultValue the defaultValue to set
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.FieldModel#getInitializer()
   */
  @Override
  public String getInitializer() {
    if(this.defaultValue == null) {
      return null;
    }
    if("java.lang.String".equals(getType())) {
      return new StringBuilder().append('"').append(this.defaultValue).append('"').toString();
    }
    return this.defaultValue;
  }
  
}
