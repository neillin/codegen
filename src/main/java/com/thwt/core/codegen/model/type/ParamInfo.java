package com.thwt.core.codegen.model.type;

import com.thwt.core.codegen.Case;

/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ParamInfo {

  final int index;
  final String name;
  final Text description;
  final TypeInfo type;
  final boolean nullable;

  public ParamInfo(int index, String name, Text description, TypeInfo type, boolean nullable) {
    this.index = index;
    this.name = name;
    this.description = description;
    this.type = type;
    this.nullable = nullable;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public String getName(Case _case) {
    return _case.format(Case.CAMEL.parse(name));
  }

  public Text getDescription() {
    return description;
  }

  /**
   * @return true when the param is nullable
   */
  public boolean isNullable() {
    return type.getKind() == ClassKind.OBJECT || this.nullable;
  }

//  /**
//   * @return true when the param callback value is nullable: when the parameter type is an
//   *         handler or an async result handler it returns the nullable boolean of the corresponding
//   *         parameter, otherwise it returns null
//   */
//  public Boolean isNullableCallback() {
//    switch (type.getKind()) {
//      case HANDLER:
//        TypeInfo handler = ((ParameterizedTypeInfo)type).getArg(0);
//        switch (handler.getKind()) {
//          case OBJECT:
//            return true;
//          case ASYNC_RESULT:
//            TypeInfo asyncResult = ((ParameterizedTypeInfo)handler).getArg(0);
//            switch (asyncResult.getKind()) {
//              case OBJECT:
//                return true;
//              default:
//                return asyncResult.isNullable();
//            }
//          default:
//            return handler.isNullable();
//        }
//      default:
//        return null;
//    }
//  }

  public TypeInfo getType() {
    return type;
  }

  public boolean isDataObject() {
    return type instanceof ClassTypeInfo && (type).getKind() == ClassKind.DATA_OBJECT;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof  ParamInfo)) {
      return false;
    }
    ParamInfo pother = (ParamInfo)other;
    return this.name.equals(pother.name) && this.type.equals(pother.type);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return type + " " + name;
  }
}
