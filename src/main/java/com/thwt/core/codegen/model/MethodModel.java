/*
 * @(#)MethodModel.java 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import java.util.List;

import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.codegen.model.type.TypeInfo;

/**
 * @author Neil Lin
 *
 */
public interface MethodModel extends MemberModel {

  /**
   * @return the methodName
   */
  String getName();

  /**
   * @return the returnType
   */
  String getReturnType();
  
  
  TypeInfo getReturnTypeInfo();
  

  /**
   * @return the parameterTypes
   */
  String[] getParameterTypes();
  
  TypeInfo[] getParameterTypeInfos();

  /**
   * @return the parameterNames
   */
  String[] getParameterNames();
  
  
  ParamInfo[] getParameters();

  /**
   * @return the thrownTypes
   */
  String[] getThrownTypes();
  
  
  String[] getTypeVariables();

  
  List<TypeInfo> getThrownTypeInfos();
  
  
  List<TypeInfo> getTypeVariableinfos();
  /**
   * @return the methodBody
   */
  String getMethodBody();

  /**
   * @return the varArgs
   */
  boolean isVarArgs();

  String getMethodKey();

  /**
   * @return the modifiers
   */
  String getModifiers();

  /**
   * @return the classModel
   */
  ClassModel getClassModel();

  
  String generateMethodSignature();
  
  /**
   * @return the javaStatement
   */
  String getJavaStatement();

  /**
   * @return the generated
   */
  boolean isGenerated();

  /**
   * @return the constructor
   */
  boolean isConstructor();

  boolean isPrivate();

  boolean isStatic();

  boolean isPublic();

  boolean isAbstract();

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MemberModel#getType()
   */
  String getType();

}