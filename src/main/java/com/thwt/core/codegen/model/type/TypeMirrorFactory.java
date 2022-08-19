package com.thwt.core.codegen.model.type;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.persistence.Entity;

import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.annotation.DartLang;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.util.Utils;

import io.vertx.codegen.annotations.DataObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Type info factory based on <i>javax.lang.model</i> and type mirrors.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TypeMirrorFactory {

//  final Elements elementUtils;
//  final Types typeUtils;
//
//  public TypeMirrorFactory(Elements elementUtils, Types typeUtils) {
//    this.elementUtils = elementUtils;
//    this.typeUtils = typeUtils;
//  }

//  public TypeInfo create(TypeMirror type) {
//    return create(null, type);
//  }
//
  private static final TypeMirrorFactory instance = new TypeMirrorFactory();
  
  public static TypeMirrorFactory getInstance() {
    return instance;
  }
  
  public static String getTypescriptName(String javaClassName) {
	  ClassTypeInfo type = ClassTypeInfo.PRIMITIVES.get(javaClassName);
	  if(type != null) {
		  return type.getTypescriptName();
	  }
	  if(Void.class.getName().equals(javaClassName)) {
		  return "any";
	  }
	  if(String.class.getName().equals(javaClassName)) {
		  return "string";
	  }
	  if(List.class.getName().equals(javaClassName)) {
		  return "Array";
	  }
	  if(Set.class.getName().equals(javaClassName)) {
		  return "Array";
	  }
	  ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
	  Elements elems = ctx.getProcessingEnvironment().getElementUtils();
	  TypeElement elem = elems.getTypeElement(javaClassName);
	  Jsonizable ann = elem != null ? elem.getAnnotation(Jsonizable.class) : null;
	  if(ann != null && (Utils.isBlank(ann.tsName()) == false)) {
		  return ann.tsName().trim();
	  }
	  ServiceLoader<TypescriptNameProvider> loader = ServiceLoader.load(TypescriptNameProvider.class);
	  for(TypescriptNameProvider p: loader) {
		  String name = p.getTypescriptName(javaClassName);
		  if(name != null) {
			  return name;
		  }
	  }
	  return null;
  }
  
  public static String getDartName(String javaClassName) {
	  ClassTypeInfo type = ClassTypeInfo.PRIMITIVES.get(javaClassName);
	  if(type != null) {
		  return type.getDartName();
	  }
	  if(Void.class.getName().equals(javaClassName)) {
		  return "void";
	  }
	  if(String.class.getName().equals(javaClassName)) {
		  return "String";
	  }
	  if(List.class.getName().equals(javaClassName)) {
		  return "List";
	  }
	  if(Set.class.getName().equals(javaClassName)) {
		  return "Set";
	  }
	  if(Map.class.getName().equals(javaClassName)) {
		  return "Map";
	  }
	  if(Date.class.getName().equals(javaClassName)||Timestamp.class.getName().equals(javaClassName)||java.sql.Date.class.getName().equals(javaClassName)) {
		  return "DateTime";
	  }
	  ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
	  Elements elems = ctx.getProcessingEnvironment().getElementUtils();
	  TypeElement elem = elems.getTypeElement(javaClassName);
	  DartLang ann = elem != null ? elem.getAnnotation(DartLang.class) : null;
	  if(ann != null && (Utils.isBlank(ann.dartName()) == false)) {
		  return ann.dartName().trim();
	  }
	  ServiceLoader<DartTypeNameProvider> loader = ServiceLoader.load(DartTypeNameProvider.class);
	  for(DartTypeNameProvider p: loader) {
		  String name = p.getDartName(javaClassName);
		  if(name != null) {
			  return name;
		  }
	  }
	  return null;
  }

  
  private TypeMirrorFactory(){}
  
  public TypeInfo create(String javaClassName) {
	  ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
	  Elements elems = ctx.getProcessingEnvironment().getElementUtils();
	  TypeElement elem = elems.getTypeElement(javaClassName);
	  return elem != null ? this.create(elem.asType()) : null;
  }
  
  
  public TypeInfo create(/*TypeUse use,*/ TypeMirror type) {
    switch (type.getKind()) {
      case VOID:
        return VoidTypeInfo.INSTANCE;
      case ERROR:
      case DECLARED:
        return create(/*use, */(DeclaredType) type);
      case DOUBLE:
      case LONG:
      case FLOAT:
      case CHAR:
      case BYTE:
      case SHORT:
      case BOOLEAN:
      case INT:
//        if (use != null && use.isNullable()) {
//          throw new IllegalArgumentException("Primitive types cannot be annotated with @Nullable");
//        }
        return PrimitiveTypeInfo.PRIMITIVES.get(type.getKind().name().toLowerCase());
      case TYPEVAR:
        return create(/*use,*/ (TypeVariable) type);
      case ARRAY:
        return new ArrayType(create(((javax.lang.model.type.ArrayType)type).getComponentType()));
      case WILDCARD:
    	  	WildcardType wType = (WildcardType)type;
    	  	if(wType.getExtendsBound() != null) {
    	  		return create(wType.getExtendsBound());
    	  	}else {
    	  		return ObjectTypeInfo.INSTANCE;
    	  	}
      case NONE:
    	  return null;
      default:
        throw new IllegalArgumentException("Illegal type " + type + " of kind " + type.getKind());
    }
  }

//  public TypeInfo create(DeclaredType type) {
//    return create(null, type);
//  }

  public TypeInfo create(final DeclaredType type) {
//    boolean nullable = true; //use != null && use.isNullable();
//	if("java.lang.Void".equals(type.toString())) {
//		return VoidTypeInfo.INSTANCE;
//	}
    TypeElement elt = (TypeElement) type.asElement();
//    PackageElement pkgElt = elementUtils.getPackageOf(elt);
//    ModuleInfo module = ModuleInfo.resolve(elementUtils, pkgElt);
    String fqcn = elt.getQualifiedName().toString();
    boolean proxyGen = false; //elt.getAnnotation(ProxyGen.class) != null;
    if (elt.getKind() == ElementKind.ENUM) {
      ArrayList<String> values = new ArrayList<>();
      for (Element enclosedElt : elt.getEnclosedElements()) {
        if (enclosedElt.getKind() == ElementKind.ENUM_CONSTANT) {
          values.add(enclosedElt.getSimpleName().toString());
        }
      }
      boolean gen = false; //elt.getAnnotation(VertxGen.class) != null;
      return new EnumTypeInfo(fqcn, gen, values, proxyGen);
    } else {
      ClassKind kind = ClassKind.getKind(fqcn, 
    		    elt.getAnnotation(Jsonizable.class) != null||
    		  	elt.getAnnotation(DataObject.class) != null||
    		  	elt.getAnnotation(Entity.class) != null, 
    		  	false); //elt.getAnnotation(VertxGen.class) != null);
      ClassTypeInfo raw;
      if (kind == ClassKind.BOXED_PRIMITIVE) {
        raw = ClassTypeInfo.PRIMITIVES.get(fqcn);
//        if (nullable) {
//          raw = new ClassTypeInfo(raw.kind, raw.name, raw.module, true, raw.params);
//        }
      } else {
        List<TypeParamInfo.Class> typeParams = createTypeParams(type);
//        if (kind == ClassKind.API) {
//          VertxGen genAnn = elt.getAnnotation(VertxGen.class);
//          TypeInfo[] args = FluentIterable.of( new String[] {
//              ClassModel.VERTX_READ_STREAM,
//              ClassModel.VERTX_WRITE_STREAM,
//              ClassModel.VERTX_HANDLER
//          }
//          ).transform(new Function<String, TypeInfo>() {
//
//            @Override
//            public TypeInfo apply(String s) {
//              TypeElement parameterizedElt = elementUtils.getTypeElement(s);
//              TypeMirror parameterizedType = parameterizedElt.asType();
//              TypeMirror rawType = typeUtils.erasure(parameterizedType);
//              if (typeUtils.isSubtype(type, rawType)) {
//                TypeMirror resolved = Helper.resolveTypeParameter(typeUtils, type, parameterizedElt.getTypeParameters().get(0));
//                return create(resolved);
//              }
//              return null;
//            }
//          }).toArray(TypeInfo.class);
//          raw = new ApiTypeInfo(fqcn, genAnn.concrete(), typeParams, args[0], args[1], args[2], module, nullable, proxyGen);
//        } else if (kind == ClassKind.DATA_OBJECT) {
//          boolean _abstract = elt.getModifiers().contains(Modifier.ABSTRACT);
//          raw = new DataObjectTypeInfo(kind, fqcn, module, _abstract, nullable, proxyGen, typeParams);
//        } else {
        	  	raw = new ClassTypeInfo(kind, fqcn, typeParams);
//        }
          List<? extends TypeMirror> typeArgs = type.getTypeArguments();
          if (typeArgs.size() > 0) {
            List<TypeInfo> typeArguments;
            typeArguments = new ArrayList<>(typeArgs.size());
            for (int i = 0; i < typeArgs.size(); i++) {
//              TypeUse argUse = use != null ? use.getArg(i) : null;
              TypeInfo typeArgDesc = create(/*argUse, */typeArgs.get(i));
              // Need to check it is an interface type
              typeArguments.add(typeArgDesc);
            }
            raw = new ParameterizedTypeInfo(kind, fqcn, typeParams, typeArguments);
          }
      }
      return raw;
    }
  }

  public TypeVariableInfo create(/*TypeUse use,*/ TypeVariable type) {
    TypeParameterElement elt = (TypeParameterElement) type.asElement();
    TypeParamInfo param = TypeParamInfo.create(elt);
    return new TypeVariableInfo(param, /*use != null && use.isNullable()true,*/ type.toString());
  }

  private List<TypeParamInfo.Class> createTypeParams(DeclaredType type) {
    List<TypeParamInfo.Class> typeParams = new ArrayList<>();
    TypeElement elt = (TypeElement) type.asElement();
    List<? extends TypeParameterElement> typeParamElts = elt.getTypeParameters();
    for (int index = 0; index < typeParamElts.size(); index++) {
      TypeParameterElement typeParamElt = typeParamElts.get(index);
      typeParams.add(new TypeParamInfo.Class(elt.getQualifiedName().toString(), index, typeParamElt.getSimpleName().toString()));
    }
    return typeParams;
  }
}
