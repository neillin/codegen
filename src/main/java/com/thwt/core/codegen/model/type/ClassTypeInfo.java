package com.thwt.core.codegen.model.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.thwt.core.codegen.Case;
import com.thwt.core.codegen.model.ImportManager;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ClassTypeInfo extends TypeInfo {

  public static final HashMap<String, ClassTypeInfo> PRIMITIVES = new HashMap<>();
  
  public static final Class<?>[] boxes = {Boolean.class, Byte.class, Short.class, Integer.class, Long.class,
	        Float.class, Double.class, Character.class};
  public static final String[] primitives = {"boolean", "byte", "short", "int", "long",
	        "float", "double", "char" };
  public static final String[] tsNames = {"boolean", "number", "number", "number", "number",
          "number", "number", "string"};
  
  public static final String[] dartNames = {"bool", "int", "int", "int", "int",
          "double", "double", "int"};

  static {
    for (int i=0 ; i< boxes.length; i++) {
    	  Class<?> boxe = boxes[i];
      String name = boxe.getName();
      PRIMITIVES.put(name, new ClassTypeInfo(ClassKind.BOXED_PRIMITIVE, name, tsNames[i], dartNames[i], Collections.<TypeParamInfo.Class>emptyList())/*new ClassTypeInfo(ClassKind.BOXED_PRIMITIVE, name, null, false, Collections.<TypeParamInfo.Class>emptyList())*/);
    }
  }

  public static String getBoxedType(String name) {
	  if("void".equals(name)) {
		  return Void.class.getName();
	  }
	  for (int i=0 ; i< boxes.length; i++) {
		  String p = primitives[i];
		  if(p.equals(name)) {
			  return boxes[i].getName();
		  }
	  }
	  return name;
  }
  
  public static String getTsName(String javaClassName) {
	  String name = TypeMirrorFactory.getTypescriptName(javaClassName);
	  if(name == null) {
		  name = Helper.getSimpleName(javaClassName);
	  }
	  return name;
  }
  
  public static String getDartName(String javaClassName) {
	  String name = TypeMirrorFactory.getDartName(javaClassName);
	  if(name == null) {
		  name = Helper.getSimpleName(javaClassName);
	  }
	  return name;
  }

  
  final ClassKind kind;
  final String name;
  final String simpleName;
  final String packageName;
//  final ModuleInfo module;
//  final boolean nullable;
  final List<TypeParamInfo.Class> params;
  
  final String tsName;
  final String dartName;

//  public ClassTypeInfo(ClassKind kind, String name, ModuleInfo module, boolean nullable, List<TypeParamInfo.Class> params) {
  public ClassTypeInfo(ClassKind kind,  String name, List<TypeParamInfo.Class> params) {
    this(kind, name, getTsName(name), getDartName(name), params);
  }
  
  private ClassTypeInfo(ClassKind kind,  String name, String ts, String dart, List<TypeParamInfo.Class> params) {
	    this.kind = kind;
	    this.name = name;
	    this.simpleName = Helper.getSimpleName(name);
	    this.packageName = Helper.getPackageName(name);
//	    this.module = module;
//	    this.nullable = nullable;
	    this.params = params;
	    this.tsName = ts;
	    this.dartName = dart;
	  }

  public List<TypeParamInfo.Class> getParams() {
    return params;
  }

//  /**
//   * @return the optional module name only present for {@link io.vertx.codegen.annotations.VertxGen} annotated types.
//   */
//  public String getModuleName() {
//    return module != null ? module.getName() : null;
//  }
//
//  /**
//   * @return the optional module name only present for {@link io.vertx.codegen.annotations.VertxGen} annotated types.
//   */
//  public ModuleInfo getModule() {
//    return module;
//  }
//
//  public boolean isNullable() {
//    return nullable;
//  }

  public ClassKind getKind() {
    return kind;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getSimpleName(Case _case) {
    return _case.format(Case.CAMEL.parse(simpleName));
  }

  @Override
  public ClassTypeInfo getRaw() {
    return this;
  }

  @Override
  public void collectImports(ImportManager mgr) {
    mgr.importClass(getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassTypeInfo) {
      return name.equals(((ClassTypeInfo) obj).name);
    }
    return false;
  }

  @Override
  public String format(boolean qualified) {
    return qualified ? name : simpleName;
  }

/* 
 * @see com.thwt.core.codegen.model.type.TypeInfo#getTypescriptName()
 */
@Override
public String getTypescriptName() {
	switch(this.kind) {
	case ARRAY:
	case JSON_ARRAY:
	case LIST:
	case MAP:
	case SET:
		return "Array";
	default:
		return this.tsName;
	}
}

/* 
 * @see com.thwt.core.codegen.model.type.TypeInfo#getTypescriptName()
 */
@Override
public String getDartName() {
	switch(this.kind) {
	case ARRAY:
	case JSON_ARRAY:
	case LIST:
		return "List";
	case MAP:
		return "Map";
	case SET:
		return "Set";
	case DATE_TYPE:
		return "DateTime";
	case ENUM:
		return "String";
	default:
		return this.dartName;
	}
}


//  public String translateName(String id) {
//    return module == null ? name : module.translateQualifiedName(name, id);
//  }
//
//  public String translatePackageName(String id) {
//    return module == null ? packageName : module.translateQualifiedName(packageName, id);
//  }
//
//  @Override
//  public String translateName(TypeNameTranslator translator) {
//    return module == null ? name : translator.translate(module, name);
//  }
//
//  public String translatePackageName(TypeNameTranslator translator) {
//    return module == null ? packageName : translator.translate(module, packageName);
//  }
}
