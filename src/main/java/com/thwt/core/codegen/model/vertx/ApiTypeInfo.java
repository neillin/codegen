package com.thwt.core.codegen.model.vertx;

import java.util.List;

import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeParamInfo;

/**
 * A special subclass for {@link ClassKind#API} kinds.
 */
public class ApiTypeInfo extends ClassTypeInfo {

  final boolean proxyGen;
  final boolean concrete;
  final TypeInfo readStreamArg;
  final TypeInfo writeStreamArg;
  final TypeInfo handlerArg;

  public ApiTypeInfo(
      String fqcn,
      boolean concrete,
      List<TypeParamInfo.Class> params,
      TypeInfo readStreamArg,
      TypeInfo writeStreamArg,
      TypeInfo handlerArg,
//      ModuleInfo module,
//      boolean nullable,
      boolean proxyGen) {
    super(ClassKind.API, fqcn, /*module, nullable,*/ params);
    this.concrete = concrete;
    this.proxyGen = proxyGen;
    this.readStreamArg = readStreamArg;
    this.writeStreamArg = writeStreamArg;
    this.handlerArg = handlerArg;
  }

  public boolean isProxyGen() {
    return proxyGen;
  }

  public boolean isConcrete() {
    return concrete;
  }

  public boolean isAbstract() {
    return !concrete;
  }

  public TypeInfo getReadStreamArg() {
    return readStreamArg;
  }

  public boolean isReadStream() {
    return readStreamArg != null;
  }

  public TypeInfo getWriteStreamArg() {
    return writeStreamArg;
  }

  public boolean isWriteStream() {
    return writeStreamArg != null;
  }

  public TypeInfo getHandlerArg() {
    return handlerArg;
  }

  public boolean isHandler() {
    return handlerArg != null;
  }
}
