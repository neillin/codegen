/*
 * @(#)ClassModel.java 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.Doc;
import com.thwt.core.codegen.util.ChecksumBuilder;

/**
 * @author Neil Lin
 *
 */
public interface ClassModel {

  /**
   * @return the name
   */
  String getName();

  /**
   * @return the pkgName
   */
  String getPkgName();

  /**
   * @return the imports
   */
  List<String> getImports();

  String getClassName();

  String importClass(String stmt);

  /**
   * @return the superClass
   */
  String getSuperClass();

  TypeElement getSuperClassElement();
  
  /**
   * @return the interfaces
   */
  List<String> getInterfaces();

  String getJoinInterfaces();

  /**
   * @return the fields
   */
  List<FieldModel> getFields();

  List<MethodModel> getMethods();

  boolean hasMethod(MethodModel method);

  MethodModel getMethod(String methodKey);

  /**
   * @return the traceRequired
   */
  boolean isTraceRequired();

  String getFullQualifiedName();

  Doc getComment();
  /**
   * @return the fileLocation
   */
  FileLocation getFileLocation();

  void checksum(ChecksumBuilder builder);

  public ImportManager getImportManager();

  void addMethod(MethodModel method);

  void addField(FieldModel field);

  void addField(String name, String className);

  void addInterface(String clazz);

  void setSuperClass(String superClass);
  
  ClassTypeInfo getTypeInfo();
  
  ClassTypeInfo getSuperType();
  
  Set<ClassTypeInfo> getInterfaceTypes();
  
  TypeElement getElement();
  
  boolean isAbstract();
}