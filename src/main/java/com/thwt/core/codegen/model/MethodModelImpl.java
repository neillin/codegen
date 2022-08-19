/**
 * 
 */
package com.thwt.core.codegen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.model.type.VoidTypeInfo;
import com.thwt.core.codegen.util.ChecksumBuilder;
import com.thwt.core.codegen.util.MethodElementParser;
import com.thwt.core.codegen.util.ModelUtils;

/**
 * @author neillin
 *
 */
public class MethodModelImpl implements MemberModel, MethodModel {
  private final ClassModel classModel;
  private String methodName;
  private TypeInfo returnType;
  private List<ParamInfo> parameters;
  private List<TypeInfo> thrownTypes;
  private List<TypeInfo> typeVariables;
  private String methodBody;
  private Set<Modifier> modifiers;
  private boolean varArgs;
  private String javaStatement;
  private boolean generated;
  private boolean constructor;
  private String modString;
  private ExecutableElement element;
  private Set<ClassTypeInfo> ownerTypes; // class that declares this method and classes which has method overridden by this method
  private String returnDescription;
  
  public MethodModelImpl(ClassModel declareType) {
    this.classModel = Preconditions.checkNotNull(declareType);
  }
  
  public MethodModelImpl(ClassModel declareType, ExecutableElement exec) {
    this.classModel = Preconditions.checkNotNull(declareType);
    this.element = Preconditions.checkNotNull(exec);
    MethodElementParser parser = MethodElementParser.getParser(exec);
    this.returnDescription = null; //parser.getReturnDesc() != null ? parser.getReturnDesc().getValue() : null;
    this.methodName = parser.getMethodName();
    this.modifiers = parser.getModifiers();
    this.ownerTypes = parser.getOwnerTypes();
    this.parameters = parser.getParameters();
    this.returnType = parser.getReturnType();
    this.thrownTypes = parser.getThrownTypes();
    this.constructor = parser.isConstructor();
    this.typeVariables = parser.getTypeVariables();
    this.varArgs = parser.isVarArgs();
    collectImports(this.classModel.getImportManager());
  }
  
  protected void collectImports(ImportManager mgr) {
    if(this.returnType != null) {
      this.returnType.collectImports(mgr);
    }
    if(this.parameters != null) {
      for (ParamInfo pInfo : this.parameters) {
        pInfo.getType().collectImports(mgr);
      }
    }
    if(this.typeVariables != null) {
      for (TypeInfo info : this.typeVariables) {
        info.collectImports(mgr);
      }
    }
    if(this.thrownTypes != null) {
      for (TypeInfo info : this.thrownTypes) {
        info.collectImports(mgr);
      }
    }
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getName()
   */
  public String getName() {
    return methodName;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getReturnType()
   */
  public String getReturnType() {
    return this.returnType != null ? this.returnType.getSimpleName() : "";
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getParameterTypes()
   */
  public String[] getParameterTypes() {
    if(this.parameters != null && this.parameters.size() > 0) {
      String[] params = new String[this.parameters.size()];
      for (ParamInfo info : this.parameters) {
        params[info.getIndex()] = info.getType().getSimpleName();
      }
      return params;
    }
    return new String[0];
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getParameterTypes()
   */
  public TypeInfo[] getParameterTypeInfos() {
    if(this.parameters != null && this.parameters.size() > 0) {
    	TypeInfo[] params = new TypeInfo[this.parameters.size()];
      for (ParamInfo info : this.parameters) {
        params[info.getIndex()] = info.getType();
      }
      return params;
    }
    return new TypeInfo[0];
  }

  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getParameterNames()
   */
  public String[] getParameterNames() {
    if(this.parameters != null && this.parameters.size() > 0) {
      String[] params = new String[this.parameters.size()];
      for (ParamInfo info : this.parameters) {
        params[info.getIndex()] = info.getName();
      }
      return params;
    }
    return new String[0];
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getThrownTypes()
   */
  public String[] getThrownTypes() {
    if(this.thrownTypes != null && this.thrownTypes.size() > 0) {
      return FluentIterable.from(this.thrownTypes).transform(new Function<TypeInfo, String>() {

        @Override
        public String apply(TypeInfo type) {
          return type.getSimpleName();
        }
      }).toArray(String.class);
    }
    return new String[0];
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getMethodBody()
   */
  public String getMethodBody() {
    return methodBody;
  }
  /**
   * @param methodName the methodName to set
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  /**
   * @param returnType the returnType to set
   */
  public void setReturnType(String returnType) {
    if("void".equals(returnType)) {
      this.returnType = VoidTypeInfo.INSTANCE;
      return;
    }
    TypeMirror type = TypeModel.getTypeMirror(new TypeModel(Preconditions.checkNotNull(returnType)));
    if(type == null) {
      throw new IllegalArgumentException("Invalid return type :["+returnType+"]");
    }
    this.returnType = TypeMirrorFactory.getInstance().create(type);
  }
  /**
   * @param parameterTypes the parameterTypes to set
   */
  public void setParameters(String[] parameterTypes, String[] parameterNames) {
    if((parameterTypes == null || parameterTypes.length == 0)&&(parameterNames == null || parameterNames.length == 0)){
      this.parameters = null;
      return;
    }
    if((parameterTypes == null || parameterTypes.length == 0)||(parameterNames == null || parameterNames.length == 0)||(parameterTypes.length != parameterNames.length)){
      throw new IllegalArgumentException("Method parameter types and names must be in same number !");
    }
    int len = parameterNames.length;
    this.parameters = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      ParamInfo info = new ParamInfo(i, parameterNames[i], null, TypeMirrorFactory.getInstance().create(TypeModel.getTypeMirror(new TypeModel(parameterTypes[i]))), true);
      this.parameters.add(info);
    }
  }
  /**
   * @param thrownTypes the thrownTypes to set
   */
  public void setThrownTypes(String[] thrownTypes) {
    if(thrownTypes == null || thrownTypes.length == 0) {
      this.thrownTypes = null;
    }else{
      this.thrownTypes = FluentIterable.from(thrownTypes).transform(new Function<String, TypeInfo>() {

        @Override
        public TypeInfo apply(String input) {
          return TypeMirrorFactory.getInstance().create(TypeModel.getTypeMirror(new TypeModel(input)));
        }
      }).toList();
    }
  }
  /**
   * @param methodBody the methodBody to set
   */
  public void setMethodBody(String methodBody) {
    this.methodBody = methodBody;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isVarArgs()
   */
  public boolean isVarArgs() {
    return varArgs;
  }
  /**
   * @param varArgs the varArgs to set
   */
  public void setVarArgs(boolean varArgs) {
    this.varArgs = varArgs;
  }

  public String toString() {
    return getJavaStatement();
  }
  /**
   * @param buf
   */
  protected void generateMethodKey(StringBuffer buf) {
    String[] parameterTypes = getParameterTypes();
    buf.append(getName()).append('(');
    int size = parameterTypes != null ? parameterTypes.length : 0;
    if(size > 0){
      for(int i=0 ; i < size ; i++){
        if(i > 0){
          buf.append(',');
        }
        buf.append(this.classModel.importClass(parameterTypes[i]));
        if(i == (size -1)){
          if(this.varArgs){
            buf.append("...");
          }
        }
      }
    }
    buf.append(')');
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getMethodKey()
   */
  public String getMethodKey() {
    return MethodSignature.Factory.createSignature(getName(), getParameters(), isVarArgs()).getSignature();
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getModifiers()
   */
  public String getModifiers() {
    if(this.modString == null) {
      StringBuffer buf = new StringBuffer();
      if(this.modifiers != null) {
        int cnt = 0;
        for (Modifier mod : this.modifiers) {
          if(cnt > 0){
            buf.append(' ');
          }
          buf.append(mod.toString());
          cnt++;
        }
      }
      this.modString = buf.toString();
    }
    return this.modString;
  }
  /**
   * @param modifiers the modifiers to set
   */
  public void setModifiers(Set<Modifier> mods) {
    this.modifiers = mods;
    this.modString = null;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getClassModel()
   */
  public ClassModel getClassModel() {
    return classModel;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#generateMethodSignature()
   */
  public String generateMethodSignature() {
    return ModelUtils.generateMethodSignature(this,false);
  }


  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getJavaStatement()
   */
  public String getJavaStatement() {
    if(javaStatement == null){
      javaStatement = new StringBuffer(ModelUtils.generateMethodSignature(this,true,true)).append('\n').append(getMethodBody()).toString();
    }
    return javaStatement;
  }

  /**
   * @param javaStatement the javaStatement to set
   */
  public void setJavaStatement(String javaStatement) {
    this.javaStatement = javaStatement;
  }

  @Override
  public void checksum(ChecksumBuilder builder) {
    if(this.methodBody != null){
      builder.putString(this.methodBody);
    }
    if(this.methodName != null){
      builder.putString(this.methodName);
    }
    if(this.modifiers != null){
      builder.putString(getModifiers());
    }
    if(this.parameters != null){
      for (ParamInfo val : this.parameters) {
        builder.putString(val.getName()).putString(val.getType().getName());
      }
    }
    if((this.thrownTypes != null)&&(this.thrownTypes.size() > 0)){
      Collections.sort(this.thrownTypes, new Comparator<TypeInfo>() {

        @Override
        public int compare(TypeInfo o1, TypeInfo o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      for (TypeInfo val : thrownTypes) {
        builder.putString(val.getName());
      }
    }
    builder.put(varArgs ? (byte)1 : (byte)0);
    if(this.returnType != null){
      builder.putString(this.returnType.getName());
    }
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isGenerated()
   */
  public boolean isGenerated() {
    return generated;
  }
  /**
   * @param generated the generated to set
   */
  public void setGenerated(boolean generated) {
    this.generated = generated;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isConstructor()
   */
  public boolean isConstructor() {
    return constructor;
  }
  /**
   * @param constructor the constructor to set
   */
  public void setConstructor(boolean constructor) {
    this.constructor = constructor;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isPrivate()
   */
  public boolean isPrivate() {
    return this.modifiers != null && this.modifiers.contains(Modifier.PRIVATE);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isStatic()
   */
  public boolean isStatic() {
    return this.modifiers != null && this.modifiers.contains(Modifier.STATIC);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isPublic()
   */
  public boolean isPublic() {
    return this.modifiers != null && this.modifiers.contains(Modifier.PUBLIC);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#isAbstract()
   */
  public boolean isAbstract() {
    return this.modifiers != null && this.modifiers.contains(Modifier.ABSTRACT);
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MemberModel#getType()
   */
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getType()
   */
  @Override
  public String getType() {
    return getReturnType();
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getTypeVariables()
   */
  @Override
  public String[] getTypeVariables() {
    if(this.typeVariables != null && this.typeVariables.size() > 0) {
      return FluentIterable.from(this.typeVariables).transform(new Function<TypeInfo, String>() {

        @Override
        public String apply(TypeInfo input) {
          return input.getSimpleName();
        }
      }).toArray(String.class);
    }
    return new String[0];
  }
  /**
   * @param typeVariables the typeVariables to set
   */
  public void setTypeVariables(String[] typeVariables) {
    if(typeVariables == null || typeVariables.length == 0) {
      this.typeVariables = null;
    }else{
      this.typeVariables = FluentIterable.from(typeVariables).transform(new Function<String, TypeInfo>() {

        @Override
        public TypeInfo apply(String input) {
          return TypeMirrorFactory.getInstance().create(TypeModel.getTypeMirror(new TypeModel(input)));
        }
      }).toList();
    }
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MemberModel#getJavaElement()
   */
  @Override
  public Element getElement() {
    return this.element;
  }

  /**
   * @return the returnDescription
   */
  public String getReturnDescription() {
    return returnDescription;
  }

  /**
   * @param returnDescription the returnDescription to set
   */
  public void setReturnDescription(String returnDescription) {
    this.returnDescription = returnDescription;
  }

  /**
   * @return the ownerTypes
   */
  public Set<ClassTypeInfo> getOwnerTypes() {
    return ownerTypes;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getReturnTypeInfo()
   */
  @Override
  public TypeInfo getReturnTypeInfo() {
    return this.returnType;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getParameters()
   */
  @Override
  public ParamInfo[] getParameters() {
    if(this.parameters != null && this.parameters.size() > 0) {
      ParamInfo[] params = new ParamInfo[this.parameters.size()];
      for (ParamInfo info : this.parameters) {
        params[info.getIndex()] = info;
      }
      return params;
    }
    return new ParamInfo[0];
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getThrownTypeInfos()
   */
  @Override
  public List<TypeInfo> getThrownTypeInfos() {
    return this.thrownTypes;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#getTypeVariableinfos()
   */
  @Override
  public List<TypeInfo> getTypeVariableinfos() {
    return this.typeVariables;
  }

}
