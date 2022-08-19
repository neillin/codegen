/*
 * @(#)ServiceMBeanModel.java	 2017-02-25
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import javax.lang.model.element.TypeElement;

import com.thwt.core.codegen.model.ClassModelImpl;

/**
 * @author Neil Lin
 *
 */
public class ServiceMBeanModel extends ClassModelImpl {
  
  private String moduleId;

  /**
   * @param elem
   */
  public ServiceMBeanModel(TypeElement elem) {
    super(elem);
  }
  
  private String getServiceName(String name) {
    if(name.endsWith("Impl")) {
      name = name.substring(0,name.length()-4);
    }
    return name;
  }

  public String generateVerticleClassName() {
    return getPkgName()+"."+getServiceName(getName())+"Verticle";
  }
  
  public String generateOptionsConverterClassName() {
    return getPkgName()+"."+getServiceName(getName())+"OptsConverter";
  }

  public String generateModuleClassName() {
    return getPkgName()+"."+getServiceName(getName())+"Module";
  }

  public String generateOptionsClassName() {
    return getPkgName()+"."+getServiceName(getName())+"Options";
  }

  /**
   * @return the moduleId
   */
  public String getModuleId() {
    return moduleId;
  }

  /**
   * @param moduleId the moduleId to set
   */
  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

}
