package com.thwt.core.codegen.model.vertx;

import java.util.List;

import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.TypeParamInfo;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DataObjectTypeInfo extends ClassTypeInfo {

  final boolean _abstract;

  public DataObjectTypeInfo(ClassKind kind, String name, /*ModuleInfo module, */boolean _abstract,/* boolean nullable, */ boolean proxyGen, List<TypeParamInfo.Class> params) {
    super(kind, name, /*module, nullable,*/ params);

    this._abstract = _abstract;
  }

  public boolean isAbstract() {
    return _abstract;
  }
}
