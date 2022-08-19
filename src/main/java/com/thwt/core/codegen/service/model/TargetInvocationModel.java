/*
 * @(#)TargetInvocationModel.java 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ImportManager;
import com.thwt.core.codegen.model.MemberModel;

/**
 * @author Neil Lin
 *
 */
public interface TargetInvocationModel {

  String OPTION_REFLECTION_ENABLED = "codegen.reflection.enabled";
  
  /**
   * @return the targetObject
   */
  MemberModel getTargetObject();

  /**
   * @param targetObject the targetObject to set
   */
  TargetInvocationModel setTargetObject(MemberModel targetObject);

  /**
   * @return the thisClass
   */
  ClassModel getThisClass();

  /**
   * @param thisClass the thisClass to set
   */
  TargetInvocationModel setThisClass(ClassModel thisClass);
  

  boolean isFieldTarget();
  

  boolean isMethodTarget();
  
  void collectImports(ImportManager mgr);

  String generateGetStatement(String objectName);

  String generateSetStatement(String objectName, String valueName);

  String generateCallStatement(String objectName, String... argNames);
  
  void validateTarget(ICodeGenerationContext ctx, InvocationMode mode) throws CodeGenException;

}