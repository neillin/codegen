/*
 * @(#)MethodElementParser.java	 2017-02-26
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.GenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.DataObjectTypeInfo;
import com.thwt.core.codegen.model.type.Helper;
import com.thwt.core.codegen.model.type.MethodKind;
import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.model.type.TypeVariableInfo;
import com.thwt.core.codegen.model.type.VoidTypeInfo;

/**
 * @author Neil Lin
 *
 */
public class MethodElementParser {
  private static final Logger log = LoggerFactory.getLogger(MethodElementParser.class);
  
  public static MethodElementParser getParser(ExecutableElement elem) {
    return new MethodElementParser(Preconditions.checkNotNull(elem));
  }
  
  private final Types typeUtils;
  private final Elements elementUtils;
  private final ICodeGenerationContext ctx;
  private final TypeMirrorFactory typeFactory;
  private final TypeElement modelElt;
  private final ExecutableElement modelMethod;
  private final TypeInfo declaringType;
//  private final Doc.Factory docFactory;
  private final String methodName;
  private final Set<Modifier> modifiers;
  private final boolean constructor, varArgs;
  
//  private Map<String, String> paramDescs;
//  private String comment;
//  private Text returnDesc;
  private Set<ClassTypeInfo> ownerTypes;
  private List<ParamInfo> mParams;
  private TypeInfo returnType;
  private final boolean isFluent;
  private List<TypeInfo> thrownTypes;
  private List<TypeInfo> typeVariables;
  
  private MethodElementParser(ExecutableElement model) {
    this.ctx = MainAnnotationProcessor.getCurrentContext();
    if(ctx == null) {
      throw new IllegalStateException("This method could be called under annotation processing environment!");
    }
    this.typeUtils = ctx.getProcessingEnvironment().getTypeUtils();
    this.elementUtils = ctx.getProcessingEnvironment().getElementUtils();
    this.typeFactory = TypeMirrorFactory.getInstance();
    
    this.modelElt = (TypeElement) model.getEnclosingElement();
    this.declaringType = typeFactory.create(this.modelElt.asType());
    this.modelMethod = model;
//    this.docFactory = new Doc.Factory(this.modelElt);
    this.isFluent = checkFluent();
    this.methodName = this.modelMethod.getSimpleName().toString();
    this.modifiers = this.modelMethod.getModifiers();
    this.constructor = this.modelMethod.getKind() == ElementKind.CONSTRUCTOR;
    this.varArgs = this.modelMethod.isVarArgs();
    process();
  }
  
  protected void process() {
//    parseComment();
    this.ownerTypes = getOwnerTypes(null);
    if(!constructor) {
      this.returnType = parseReturnType();
    }
    this.mParams = getParams();
    this.typeVariables = getTypeParams();
    this.thrownTypes = FluentIterable.from(this.modelMethod.getThrownTypes()).transform(new Function<TypeMirror, TypeInfo>() {

      @Override
      public TypeInfo apply(TypeMirror type) {
        return typeFactory.create(type);
      }
    }).toList();
  }
  
  private boolean isObjectBound(TypeMirror bound) {
    return bound.getKind() == TypeKind.DECLARED && bound.toString().equals(Object.class.getName());
  }

  private List<TypeInfo> getTypeParams() {
    ArrayList<TypeInfo> typeParams = new ArrayList<>();
    for (TypeParameterElement typeParam : modelMethod.getTypeParameters()) {
//      for (TypeMirror bound : typeParam.getBounds()) {
//        if (!isObjectBound(bound)) {
//          throw new GenException(modelMethod, "Type parameter bound not supported " + bound);
//        }
//      }
      typeParams.add(this.typeFactory.create(typeParam.asType()));
    }
    return typeParams;
  }
  
  private TypeInfo parseReturnType() {
    ExecutableType methodType = (ExecutableType) typeUtils.asMemberOf((DeclaredType) modelElt.asType(), modelMethod);
    TypeInfo returnType;
    try {
      returnType = typeFactory.create(/*returnTypeUse, */methodType.getReturnType());
    } catch (Exception e) {
      GenException genEx = new GenException(modelMethod, e.getMessage());
      genEx.initCause(e);
      throw genEx;
    }
    // Only check the return type if not fluent, because generated code won't look it at anyway
//    checkReturnType(modelMethod, returnType, methodType.getReturnType());
    return returnType;
  }
  
  private boolean checkFluent() {
    AnnotationMirror fluentAnnotation = Helper.resolveMethodAnnotation(Fluent.class, elementUtils, typeUtils, this.modelElt, modelMethod);
    boolean isFluent = fluentAnnotation != null;
    if (isFluent) {
      isFluent = true;
      if (!typeUtils.isSameType(this.modelElt.asType(), modelElt.asType())) {
        String msg = "Interface " + modelElt + " does not redeclare the @Fluent return type " +
            " of method " + modelMethod + " declared by " + this.modelElt;
        this.ctx.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.WARNING, msg, modelElt, fluentAnnotation);
        log.warn(msg);
      } else {
        TypeMirror fluentType = modelMethod.getReturnType();
        if (!typeUtils.isAssignable(fluentType, modelElt.asType())) {
          throw new GenException(modelMethod, "Methods marked with @Fluent must have a return type that extends the type");
        }
      }
    }
    return isFluent;
  }

  private Set<ClassTypeInfo> getOwnerTypes(final List<ExecutableElement> modelMethods) {

    Set<ClassTypeInfo> ownerTypes = new HashSet<>();

    ArrayList<DeclaredType> ancestors = new ArrayList<>(Helper.resolveAncestorTypes(modelElt, true, true));

    // Sort to have super types the last, etc..
    // solve some problem with diamond inheritance order that can show up in type use
    Collections.sort(ancestors, new Comparator<DeclaredType>() {

      @Override
      public int compare(DeclaredType o1, DeclaredType o2) {
        if (typeUtils.isSubtype(o1, o2)) {
          return -1;
        } else if (typeUtils.isSubtype(o2, o1)) {
          return 1;
        } else {
          return ((TypeElement) o1.asElement()).getQualifiedName().toString().compareTo(((TypeElement) o2.asElement()).getQualifiedName().toString());
        }
      }
    });

    // Check overrides and merge type use
    for (DeclaredType ancestorType : ancestors) {
      TypeElement ancestorElt = (TypeElement) ancestorType.asElement();
      for(ExecutableElement overridenMethodElt : FluentIterable.from(elementUtils.getAllMembers(ancestorElt)).
          transformAndConcat(Helper.FILTER_METHOD).
          filter(new Predicate<ExecutableElement>() {

            @Override
            public boolean apply(ExecutableElement meth) {
              return elementUtils.overrides(modelMethod, meth, modelElt);
            }
          }).toList()) {
        if(modelMethods != null) {
          modelMethods.add(overridenMethodElt);
        }
        ownerTypes.add(typeFactory.create((DeclaredType) ancestorElt.asType()).getRaw());
      };
    }
    return ownerTypes;
  }

//  private void parseComment() {
//    this.paramDescs = new HashMap<>();
//    this.comment = elementUtils.getDocComment(modelMethod);
//    Doc doc = docFactory.createDoc(modelMethod);
//    if (doc != null) {
//      for(Tag.Param tag : FluentIterable.from(doc.getBlockTags()).
//          filter(new Predicate<Tag>() {
//
//            @Override
//            public boolean apply(Tag tag) {
//              return tag.getName().equals("param");
//            }
//          }).
//          transform(new Function<Tag, Tag.Param>() {
//
//            @Override
//            public Param apply(Tag tag) {
//              return new Tag.Param(tag);
//            }
//          }).toList()) {
//        paramDescs.put(tag.getParamName(), tag.getParamDescription());
//      }
//      Optional<Tag> returnTag = FluentIterable.from(doc.getBlockTags()).
//          filter(new Predicate<Tag>() {
//
//            @Override
//            public boolean apply(Tag tag) {
//              return tag.getName().equals("return");
//            }
//          }).
//          first();
//      if (returnTag.isPresent()) {
//        returnDesc = new Text(Helper.normalizeWhitespaces(returnTag.get().getValue())).map(Token.tagMapper(modelElt));
//      }
//    }
//  }
  
  private List<ParamInfo> getParams() {
    ExecutableType methodType = (ExecutableType) typeUtils.asMemberOf((DeclaredType) modelElt.asType(), this.modelMethod);
    List<? extends VariableElement> params = this.modelMethod.getParameters();
    List<ParamInfo> mParams = new ArrayList<>();
    for (int i = 0; i < params.size();i++) {
      VariableElement param = params.get(i);
      TypeMirror type = methodType.getParameterTypes().get(i);
      TypeInfo typeInfo;
//      final int index = i;
//      TypeUse typeUse;
//      if (reflectMethods != null) {
//        typeUse = TypeUse.createTypeUse(reflectMethods.stream().map(m -> m.getAnnotatedParameterTypes()[index]).toArray(AnnotatedType[]::new));
//      } else {
//        typeUse = TypeUse.createTypeUse(this.typeUtils, FluentIterable.from(modelMethods).transform(new Function<ExecutableElement, TypeMirror>() {
//
//          @Override
//          public TypeMirror apply(ExecutableElement m) {
//            return m.getParameters().get(index).asType();
//          }
//        }).toArray(TypeMirror.class));
//      }
      try {
        typeInfo = typeFactory.create(/*typeUse, */type);
      } catch (Exception e) {
        throw new GenException(param, e.getMessage());
      }
//      checkParamType(this.modelMethod, type, typeInfo, i, params.size());
      String name = param.getSimpleName().toString();
//      String desc = this.paramDescs.get(name);
//      Text text = desc != null ? new Text(desc).map(Token.tagMapper(modelElt)) : null;
      ParamInfo mParam = new ParamInfo(i, name, null, typeInfo, true);
      mParams.add(mParam);
    }
    return mParams;
  }
  
  public MethodKind getMethodKind() {
    MethodKind kind = MethodKind.OTHER;
    int lastParamIndex = mParams.size() - 1;
    if (lastParamIndex >= 0 && (returnType instanceof VoidTypeInfo || isFluent)) {
      TypeInfo lastParamType = mParams.get(lastParamIndex).getType();
      if (lastParamType.getKind() == ClassKind.HANDLER) {
        TypeInfo typeArg = ((ParameterizedTypeInfo) lastParamType).getArgs().get(0);
        if (typeArg.getKind() == ClassKind.ASYNC_RESULT) {
          kind = MethodKind.FUTURE;
        } else {
          kind = MethodKind.HANDLER;
        }
      }
    }
    return kind;
  }

  protected void checkParamType(ExecutableElement elem, TypeMirror type, TypeInfo typeInfo, int pos, int numParams) {
    if (isLegalNonCallableParam(typeInfo)) {
      return;
    }
    if (isLegalClassTypeParam(elem, typeInfo)) {
      return;
    }
    if (isLegalHandlerType(typeInfo)) {
      return;
    }
    if (isLegalHandlerAsyncResultType(typeInfo)) {
      return;
    }
    if (isLegalFunctionType(typeInfo)) {
      return;
    }
    throw new GenException(elem, "type " + typeInfo + " is not legal for use for a parameter in code generation");
  }

  protected void checkReturnType(ExecutableElement elem, TypeInfo type, TypeMirror typeMirror) {
    if (type instanceof VoidTypeInfo) {
      return;
    }
    if (isLegalNonCallableReturnType(type)) {
      return;
    }
    if (isLegalHandlerType(type)) {
      return;
    }
    if (isLegalHandlerAsyncResultType(type)) {
      return;
    }
    throw new GenException(elem, "type " + type + " is not legal for use for a return type in code generation");
  }

  /**
   * The <i>Return</i> set but not `void`.
   */
  private boolean isLegalNonCallableReturnType(TypeInfo type) {
    if (type.getKind().basic) {
      return true;
    }
    if (type.getKind().json) {
      return true;
    }
    if (isLegalDataObjectTypeReturn(type)) {
      return true;
    }
    if (isLegalEnum(type)) {
      return true;
    }
    if (type.getKind() == ClassKind.THROWABLE) {
      return true;
    }
    if (isTypeVariable(type)) {
      return true;
    }
    if (type.getKind() == ClassKind.OBJECT) {
      return true;
    }
    if (isVertxGenInterface(type, true)) {
      return true;
    }
    if (isLegalContainerReturn(type)) {
      return true;
    }
    return false;
  }

  private boolean isLegalEnum(TypeInfo info) {
    return info.getKind() == ClassKind.ENUM;
  }

  /**
   * The set <i>Param</i>
   */
  private boolean isLegalNonCallableParam(TypeInfo typeInfo) {
    if (typeInfo.getKind().basic) {
      return true;
    }
    if (typeInfo.getKind().json) {
      return true;
    }
    if (isLegalDataObjectTypeParam(typeInfo)) {
      return true;
    }
    if (isLegalEnum(typeInfo)) {
      return true;
    }
    if (typeInfo.getKind() == ClassKind.THROWABLE) {
      return true;
    }
    if (isTypeVariable(typeInfo)) {
      return true;
    }
    if (typeInfo.getKind() == ClassKind.OBJECT) {
      return true;
    }
    if (isVertxGenInterface(typeInfo, true)) {
      return true;
    }
    if (isLegalContainerParam(typeInfo)) {
      return true;
    }
    return false;
  }

  private boolean isTypeVariable(TypeInfo type) {
    return type instanceof TypeVariableInfo;
  }

  private boolean isLegalDataObjectTypeParam(TypeInfo type) {
    if (type.getKind() == ClassKind.DATA_OBJECT) {
      DataObjectTypeInfo classType = (DataObjectTypeInfo) type;
      return !classType.isAbstract();
    }
    return false;
  }

  private boolean isLegalClassTypeParam(ExecutableElement elt, TypeInfo type) {
    if (type.getKind() == ClassKind.CLASS_TYPE && type.isParameterized()) {
      ParameterizedTypeInfo parameterized = (ParameterizedTypeInfo) type;
      TypeInfo arg = parameterized.getArg(0);
      if (arg.isVariable()) {
        TypeVariableInfo variable = (TypeVariableInfo) arg;
        for (TypeParameterElement typeParamElt : elt.getTypeParameters()) {
          if (typeParamElt.getSimpleName().toString().equals(variable.getName())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected boolean isLegalDataObjectTypeReturn(TypeInfo type) {
    if (type.getKind() == ClassKind.DATA_OBJECT) {
      TypeElement typeElt = elementUtils.getTypeElement(type.getName());
      if (typeElt != null) {
        Optional<ExecutableElement> opt = 
            FluentIterable.from(elementUtils.getAllMembers(typeElt)).transformAndConcat(Helper.FILTER_METHOD).
            filter(new Predicate<ExecutableElement>() {

              @Override
              public boolean apply(ExecutableElement m) {
                return m.getSimpleName().toString().equals("toJson") &&
                    m.getParameters().isEmpty() &&
                    m.getReturnType().toString().equals(TypeInfo.JSON_OBJECT);
              }
            }).
            first();
        return opt.isPresent();
      }
    }
    return false;
  }
  
  private static boolean rawTypeIs(TypeInfo type, Class<?>... classes) {
    if (type instanceof ParameterizedTypeInfo) {
      String rawClassName = type.getRaw().getName();
      for (Class<?> c : classes) {
        if (rawClassName.equals(c.getName())) {
          return true;
        }
      }
    }

    return false;
  }


  protected boolean isLegalContainerParam(TypeInfo type) {
    // List<T> and Set<T> are also legal for params if T = basic type, json, @VertxGen, @DataObject
    // Map<K,V> is also legal for returns and params if K is a String and V is a basic type, json, or a @VertxGen interface
    if (rawTypeIs(type, List.class, Set.class, Map.class)) {
      TypeInfo argument = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (type.getKind() != ClassKind.MAP) {
        if (argument.getKind().basic || argument.getKind().json || isVertxGenInterface(argument, false) || isLegalDataObjectTypeParam(argument) || argument.getKind() == ClassKind.ENUM) {
          return true;
        }
      } else if (argument.getKind() == ClassKind.STRING) { // Only allow Map's with String's for keys
        argument = ((ParameterizedTypeInfo) type).getArgs().get(1);
        if (argument.getKind().basic || argument.getKind().json || isVertxGenInterface(argument, false)) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isLegalContainerReturn(TypeInfo type) {
    if (rawTypeIs(type, List.class, Set.class, Map.class)) {
      List<TypeInfo> args = ((ParameterizedTypeInfo) type).getArgs();
      if (type.getKind() == ClassKind.MAP) {
        if (args.get(0).getKind() != ClassKind.STRING) {
          return false;
        }
        TypeInfo valueType = args.get(1);
        if (valueType.getKind().basic ||
            valueType.getKind().json) {
          return true;
        }
      } else {
        TypeInfo valueType = args.get(0);
        if (valueType.getKind().basic ||
            valueType.getKind().json ||
            valueType.getKind() == ClassKind.ENUM ||
            isVertxGenInterface(valueType, false) ||
            isLegalDataObjectTypeReturn(valueType)) {
          return true;
        }
      }
    }
    return false;
  }


  private boolean isVertxGenInterface(TypeInfo type, boolean allowParameterized) {
    if (type.getKind() == ClassKind.API) {
      if (type.isParameterized()) {
        if (allowParameterized) {
          ParameterizedTypeInfo parameterized = (ParameterizedTypeInfo) type;
          for (TypeInfo paramType : parameterized.getArgs()) {
            ClassKind kind = paramType.getKind();
            if (!(/*paramType instanceof ApiTypeInfo || */paramType.isVariable() || kind == ClassKind.VOID
              || kind.basic || kind.json || kind == ClassKind.DATA_OBJECT || kind == ClassKind.ENUM )) {
              return false;
            }
//            if (paramType.isNullable()) {
//              return false;
//            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  private boolean isLegalFunctionType(TypeInfo typeInfo) {
    if (typeInfo.getErased().getKind() == ClassKind.FUNCTION) {
      TypeInfo paramType = ((ParameterizedTypeInfo) typeInfo).getArgs().get(0);
      if (isLegalCallbackValueType(paramType) || paramType.getKind() == ClassKind.THROWABLE) {
        TypeInfo returnType = ((ParameterizedTypeInfo) typeInfo).getArgs().get(1);
        return isLegalNonCallableParam(returnType);
      }
    }
    return false;
  }

  private boolean isLegalHandlerType(TypeInfo type) {
    if (type.getErased().getKind() == ClassKind.HANDLER) {
      TypeInfo eventType = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (isLegalCallbackValueType(eventType) || eventType.getKind() == ClassKind.THROWABLE) {
        return true;
      }
    }
    return false;
  }

  private boolean isLegalHandlerAsyncResultType(TypeInfo type) {
    if (type.getErased().getKind() == ClassKind.HANDLER) {
      TypeInfo eventType = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (eventType.getErased().getKind() == ClassKind.ASYNC_RESULT /*&& !eventType.isNullable()*/) {
        TypeInfo resultType = ((ParameterizedTypeInfo) eventType).getArgs().get(0);
        if (isLegalCallbackValueType(resultType)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isLegalCallbackValueType(TypeInfo type) {
    if (type.getKind() == ClassKind.VOID) {
      return true; //!type.isNullable();
    }
    return isLegalNonCallableReturnType(type);
  }

  /**
   * @return the declaringType
   */
  public TypeInfo getDeclaringType() {
    return declaringType;
  }

//  /**
//   * @return the paramDescs
//   */
//  public Map<String, String> getParamDescs() {
//    return paramDescs;
//  }
//
//  /**
//   * @return the comment
//   */
//  public String getComment() {
//    return comment;
//  }
//
//  /**
//   * @return the returnDesc
//   */
//  public Text getReturnDesc() {
//    return returnDesc;
//  }

  /**
   * @return the ownerTypes
   */
  public Set<ClassTypeInfo> getOwnerTypes() {
    return ownerTypes;
  }

  /**
   * @return the mParams
   */
  public List<ParamInfo> getParameters() {
    return mParams;
  }

  /**
   * @return the isFluent
   */
  public boolean isFluent() {
    return isFluent;
  }

  /**
   * @return the returnType
   */
  public TypeInfo getReturnType() {
    return returnType;
  }

  /**
   * @return the thrownTypes
   */
  public List<TypeInfo> getThrownTypes() {
    return thrownTypes;
  }

  /**
   * @return the methodName
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return the modifiers
   */
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  /**
   * @return the constructor
   */
  public boolean isConstructor() {
    return constructor;
  }

  /**
   * @return the typeVariables
   */
  public List<TypeInfo> getTypeVariables() {
    return typeVariables;
  }

  /**
   * @return the varArgs
   */
  public boolean isVarArgs() {
    return varArgs;
  }


}
