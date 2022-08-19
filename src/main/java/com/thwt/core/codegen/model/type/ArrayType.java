/*
 * @(#)ArrayType.java	 2017-03-21
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model.type;

import java.util.Collections;

import com.google.common.base.Preconditions;
import com.thwt.core.codegen.model.ImportManager;

/**
 * @author Neil Lin
 *
 */
public class ArrayType extends ClassTypeInfo {

  private final TypeInfo componentType;
  
  public ArrayType(TypeInfo componentType) {
    super(ClassKind.ARRAY,Preconditions.checkNotNull(componentType).getName()+"[]",Collections.<TypeParamInfo.Class>emptyList());
    this.componentType = componentType;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.type.TypeInfo#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if(! (obj instanceof ArrayType)){
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    return this.componentType.equals(((ArrayType)obj).componentType);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.type.TypeInfo#format(boolean)
   */
  @Override
  public String format(boolean qualified) {
    return this.componentType.format(qualified)+"[]";
  }
  
  public TypeInfo getComponentType() {
    return this.componentType;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.type.ClassTypeInfo#getRaw()
   */
  @Override
  public ClassTypeInfo getRaw() {
    return this;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.type.ClassTypeInfo#collectImports(com.thwt.core.codegen.model.ImportManager)
   */
  @Override
  public void collectImports(ImportManager mgr) {
    this.componentType.collectImports(mgr);
  }

/* 
 * @see com.thwt.core.codegen.model.type.ClassTypeInfo#getTypescriptName()
 */
@Override
public String getTypescriptName() {
	if("byte".equals(this.componentType.getName())) {
		return "string";
	}
	return "Array<"+this.componentType.getTypescriptName()+">";
}

/* 
 * @see com.thwt.core.codegen.model.type.ClassTypeInfo#getDartName()
 */
@Override
public String getDartName() {
	return "List<"+this.componentType.getDartName()+">";
}
  
}
