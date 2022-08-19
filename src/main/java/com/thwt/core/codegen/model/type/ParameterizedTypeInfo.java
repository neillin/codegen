package com.thwt.core.codegen.model.type;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.model.ImportManager;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ParameterizedTypeInfo extends ClassTypeInfo {

  final List<TypeInfo> args;

  public ParameterizedTypeInfo(ClassKind kind,  String name, List<TypeParamInfo.Class> params,List<TypeInfo> args) {
    super(kind, name, params);
    this.args = args;
  }
  

  @Override
  public TypeInfo getErased() {
    return new ParameterizedTypeInfo(getKind(), getName(), getParams(), FluentIterable.from(args).transform(new Function<TypeInfo, TypeInfo>() {

      @Override
      public TypeInfo apply(TypeInfo info) {
        return info.getErased();
      }
    }).toList());
  }

//  @Override
//  public boolean isNullable() {
//    return nullable;
//  }

  public ClassTypeInfo getRaw() {
    return new ClassTypeInfo(getKind(), getName(), getParams());
  }

  /**
   * @return the type arguments
   */
  public List<TypeInfo> getArgs() {
    return args;
  }

  /**
   * @param index the type argument index
   * @return a specific type argument
   */
  public TypeInfo getArg(int index) {
    return args.get(index);
  }

  @Override
  public void collectImports(ImportManager mgr) {
    super.collectImports(mgr);
    for(TypeInfo a : args) {
      a.collectImports(mgr);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(!super.equals(obj)) {
      return false;
    }
    if (obj instanceof ParameterizedTypeInfo) {
      ParameterizedTypeInfo that = (ParameterizedTypeInfo) obj;
      return args.equals(that.args);
    }
    return false;
  }

  @Override
  public String format(boolean qualified) {
    StringBuilder buf = new StringBuilder(super.format(qualified)).append('<');
    for (int i = 0; i < args.size(); i++) {
      TypeInfo typeArgument = args.get(i);
      if (i > 0) {
        buf.append(',');
      }
      buf.append(typeArgument.format(qualified));
    }
    buf.append('>');
    return buf.toString();
  }

//  @Override
//  public String translateName(TypeNameTranslator translator) {
//    StringBuilder buf = new StringBuilder(raw.translateName(translator)).append('<');
//    for (int i = 0; i < args.size(); i++) {
//      TypeInfo typeArgument = args.get(i);
//      if (i > 0) {
//        buf.append(',');
//      }
//      buf.append(typeArgument.translateName(translator));
//    }
//    buf.append('>');
//    return buf.toString();
//  }

  @Override
  public boolean isParameterized() {
    return true;
  }

/* 
 * @see com.thwt.core.codegen.model.type.ClassTypeInfo#getTypescriptName()
 */
@Override
public String getTypescriptName() {
	StringBuilder buf = new StringBuilder(super.getTypescriptName()).append('<');
    for (int i = 0; i < args.size(); i++) {
      TypeInfo typeArgument = args.get(i);
      if (i > 0) {
        buf.append(',');
      }
      buf.append(typeArgument.getTypescriptName());
    }
    buf.append('>');
    return buf.toString();
}
  
/* 
 * @see com.thwt.core.codegen.model.type.ClassTypeInfo#getTypescriptName()
 */
@Override
public String getDartName() {
	StringBuilder buf = new StringBuilder(super.getDartName()).append('<');
    for (int i = 0; i < args.size(); i++) {
      TypeInfo typeArgument = args.get(i);
      if (i > 0) {
        buf.append(',');
      }
      buf.append(typeArgument.getDartName());
    }
    buf.append('>');
    return buf.toString();
}
  
}
