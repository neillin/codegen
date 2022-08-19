/*
 * @(#)KernelModuleModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.service.annotation.ServiceMBeanAnn;
import com.thwt.core.util.Utils;

/**
 * @author Neil Lin
 *
 */
public class KernelModuleModel extends ClassModelImpl {
  private List<String> dependencies;
  private List<String> optionalDependencies;
  private List<String> serviceClasses;
  private final ServiceMBeanModel targetObject;
  private ServiceVerticleModel targetVerticle;
  private ServiceMBeanAnn ann;

  /**
   * @param qualifiedName
   */
  public KernelModuleModel(ServiceMBeanModel target) {
    super(Preconditions.checkNotNull(target).generateModuleClassName());
    this.targetObject = target;
    this.targetObject.getTypeInfo().collectImports(getImportManager());
  }

  /**
   * @return the dependencies
   */
  public List<String> getDependencies() {
    return dependencies;
  }
  
  public List<String> getOptionalDependencies() {
	  return this.optionalDependencies;
  }
  
  /**
   * @param dependencies the dependencies to set
   */
  public KernelModuleModel addDependency(String className) {
    if(this.dependencies == null) {
      this.dependencies = new ArrayList<String>();
    }
    String dep = importClass(className);
    if(!this.dependencies.contains(dep)) {
      this.dependencies.add(dep);
    }
    return this;
  }
  
  public KernelModuleModel addOptionalDependency(String className) {
	    if(this.optionalDependencies == null) {
	      this.optionalDependencies = new ArrayList<String>();
	    }
	    String dep = importClass(className);
	    if(!this.optionalDependencies.contains(dep)) {
	      this.optionalDependencies.add(dep);
	    }
	    return this;
	  }
  /**
   * @return the targetObjectClass
   */
  public String getTargetObjectClass() {
    return this.targetObject.getName();
  }

  /**
   * @return the verticleClass
   */
  public String getVerticleClass() {
    return this.targetVerticle.getName();
  }
  /**
   * @param verticleClass the verticleClass to set
   */
  public KernelModuleModel setTargetVerticle(ServiceVerticleModel verticle) {
    this.targetVerticle = Preconditions.checkNotNull(verticle);
    importClass(verticle.getClassName());
    return this;
  }
  /**
   * @return the moduleId
   */
  public String getModuleId() {
    String name = Utils.trim2Null(this.ann.moduleName());
    return name != null ? name : this.targetObject.getClassName();
  }
  
  /**
   * @return the ann
   */
  public ServiceMBeanAnn getAnn() {
    return ann;
  }
  
  /**
   * @param ann the ann to set
   */
  public void setAnn(ServiceMBeanAnn ann) {
    this.ann = ann;
    String[] classes = ann.localServices();
    if(classes != null) {
      for (String clazz : classes) {
        addServiceClasses(clazz);
      }
    }
  }
  /**
   * @return the serviceClasses
   */
  public List<String> getServiceClasses() {
    return serviceClasses;
  }
  /**
   * @param serviceClasses the serviceClasses to set
   */
  public KernelModuleModel addServiceClasses(String clazz) {
    clazz = Preconditions.checkNotNull(clazz);
    if(this.serviceClasses == null) {
      this.serviceClasses = new ArrayList<String>();
    }
    clazz = importClass(clazz);
    if(!this.serviceClasses.contains(clazz)) {
      this.serviceClasses.add(clazz);
    }
    return this;
  }
  
  public List<String> checkSelfDependencies() {
    if(this.serviceClasses != null && this.dependencies != null) {
      ArrayList<String> deps = new ArrayList<String>(this.dependencies);
      Iterables.retainAll(deps, this.serviceClasses);
      if(!deps.isEmpty()){
        return deps;
      }
    }
    return Collections.<String>emptyList();
  }
 
}
