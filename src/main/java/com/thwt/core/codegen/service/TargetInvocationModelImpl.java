/*
 * @(#)TargetInvocationModelImpl.java	 2017-02-18
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.ImportManager;
import com.thwt.core.codegen.model.MemberModel;
import com.thwt.core.codegen.model.MethodModel;
import com.thwt.core.codegen.model.MethodSignature;
import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.codegen.service.model.InvocationMode;
import com.thwt.core.codegen.service.model.TargetInvocationModel;
import com.thwt.core.codegen.util.ModelUtils;
import com.thwt.core.service.ReflectionUtils;

import io.reactivex.Completable;

/**
 * @author Neil Lin
 *
 */
public class TargetInvocationModelImpl implements TargetInvocationModel {
  private MemberModel targetObject;
  private ClassModel thisClass; 
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#getTargetObject()
   */
  public MemberModel getTargetObject() {
    return targetObject;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#setTargetObject(com.thwt.core.codegen.model.MemberModel)
   */
  public TargetInvocationModelImpl setTargetObject(MemberModel targetObject) {
    this.targetObject = targetObject;
    return this;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#getThisClass()
   */
  public ClassModel getThisClass() {
    return thisClass;
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#setThisClass(com.thwt.core.codegen.model.ClassModel)
   */
  public TargetInvocationModelImpl setThisClass(ClassModel thisClass) {
    this.thisClass = thisClass;
    return this;
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#isFieldTarget()
   */
  public boolean isFieldTarget() {
    return this.targetObject instanceof FieldModel;
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#isMethodTarget()
   */
  public boolean isMethodTarget() {
    return this.targetObject instanceof MethodModel;
  }

  public void collectImports(ImportManager importMgr) {
    if(this.targetObject instanceof FieldModel) {
      FieldModel field = (FieldModel)this.targetObject;
      if(field.isPrivate()) {
        importMgr.importClass(ReflectionUtils.class.getName());
        importMgr.importClass(field.getClassModel().getClassName());
      }
    }else{
      MethodModel method = ((MethodModel)this.targetObject);
      if(method.isPrivate()) {
        importMgr.importClass(ReflectionUtils.class.getName());
        importMgr.importClass(method.getClassModel().getClassName());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#generateGetStatement(java.lang.String)
   */
  public String generateGetStatement(String objectName) {
    if(this.targetObject instanceof FieldModel) {
      FieldModel field = (FieldModel)this.targetObject;
      if(field.isPrivate()) {
        String utilsName = thisClass.getImportManager().importClass(ReflectionUtils.class.getName());
        return utilsName+".getFieldValue("+this.thisClass.getImportManager().importClass(field.getClassModel().getClassName())+".class, \""+targetObject.getName()+"\", "+objectName+")";
      }else{
        return objectName+"."+targetObject.getName();
      }
    }else{
      throw new MustFailedCodeGenException("Target Object is not a field :["+this.targetObject+"]");
    }
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#generateSetStatement(java.lang.String, java.lang.String)
   */
  public String generateSetStatement(String objectName, String valueName) {
    if(this.targetObject instanceof FieldModel) {
      FieldModel field = (FieldModel)this.targetObject;
      if(field.isPrivate()) {
        String utilsName = thisClass.getImportManager().importClass(ReflectionUtils.class.getName());
        return utilsName+".setFieldValue("+this.thisClass.getImportManager().importClass(field.getClassModel().getClassName())+".class, \""+targetObject.getName()+"\", "+objectName+", "+valueName+")";
      }else{
        return objectName+"."+targetObject.getName()+" = "+valueName;
      }
    }else{
      throw new MustFailedCodeGenException("Target Object is not a field :["+this.targetObject+"]");
    }
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#generateCallStatement(java.lang.String, java.lang.String)
   */
  public String generateCallStatement(String objectName, String... argNames) {
    if(this.targetObject instanceof MethodModel) {
      MethodModel method = ((MethodModel)this.targetObject);
      boolean hasPrev = false;
      final StringBuilder sb;
      if(method.isPrivate()) {
        String utilsName = thisClass.getImportManager().importClass(ReflectionUtils.class.getName());
        try {
          sb = new StringBuilder(utilsName).append(".callMethod(").
              append(this.thisClass.getImportManager().importClass(method.getClassModel().getClassName())).append(".class, ").   // target class name
              append(ReflectionUtils.calculateHash(MethodSignature.Factory.createSignature(method.getName(), method.getParameters(), method.isVarArgs()).getSignature())).append("L, ").   // method hash
              append(objectName);   // target object
          hasPrev = true;
        } catch (Exception e) {
          throw new MustFailedCodeGenException("Failed to generate Reflection method call of :["+this.targetObject+"]", e);
        }
      }else {
        sb = new StringBuilder(objectName).append('.').append(this.targetObject.getName()).append('(');
      }
      ParamInfo[] pTypes = method.getParameters();
      int argLen = argNames != null ? argNames.length : 0;
      if(pTypes != null && pTypes.length > 0) {
        for(int i=0 ; i < pTypes.length ; i++) {
          if(hasPrev) {
            sb.append(", ");
          }
          if(i < argLen) {
            sb.append(argNames[i]);
          }else{
            sb.append("null");
          }
          hasPrev = true;
        }
      }
      return sb.append(')').toString();      
    }else{
      throw new MustFailedCodeGenException("Target Object is not a method :["+this.targetObject+"]");
    }
  }
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.model.TargetInvocationModel#validateTarget(com.thwt.core.codegen.ICodeGenerationContext, com.thwt.core.codegen.service.model.InvocationMode)
   */
  @Override
  public void validateTarget(ICodeGenerationContext ctx, InvocationMode mode)
      throws CodeGenException {
    boolean relectionAllowed = "true".equalsIgnoreCase(ctx.getProcessingEnvironment().getOptions().get(OPTION_REFLECTION_ENABLED));
    switch(mode) {
      case ServiceHandler: {
        if(isMethodTarget()) {
          MethodModel method = (MethodModel)this.targetObject;
          ParamInfo[] pTypes = method.getParameters();
          if(pTypes != null && (pTypes.length > 1||(pTypes.length == 1 && (pTypes[0].equals("java.lang.Class")==false) && (pTypes[0].getType().getName().startsWith("java.lang.Class<")==false)))) {
            throw new MustFailedCodeGenException("Service Handler method cannot take more than one parameter and parameter type must be java.lang.Class if have, method without parameter is allowed !");
          }
          if(!relectionAllowed && method.isPrivate()) {
            throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid service handler method in class: ["+method.getClassModel().getClassName()+"], Private method is not allowed when java reflection is not enabled");
          }
        }else{
          FieldModel field = (FieldModel)this.targetObject;
          if(!relectionAllowed && field.isPrivate()) {
            throw new MustFailedCodeGenException("Field :["+field.generateFieldSignature()+"] is not a valid service handler field in class: ["+field.getClassModel().getClassName()+"], Private field is not allowed when java reflection is not enabled");
          }
        }
        break;
      }
      case Injection: {
        if(isFieldTarget()) {
          FieldModel field = (FieldModel)this.targetObject;
          if(!relectionAllowed && field.isPrivate()) {
            throw new MustFailedCodeGenException("Field :["+field.generateFieldSignature()+"] is not a valid injectable field in class: ["+field.getClassModel().getClassName()+"], Private field is not allowed when java reflection is not enabled");
          }
        }else{
          MethodModel method = (MethodModel)this.targetObject;
          ParamInfo[] pTypes = method.getParameters();
          if(pTypes == null || pTypes.length != 1 || method.isAbstract()) {
            throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid injectable method in class: ["+method.getClassModel().getClassName()+"], MethoInjection method take only take one parameter");
          }
          if(!relectionAllowed && method.isPrivate()) {
            throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid injectable method in class: ["+method.getClassModel().getClassName()+"], Private method is not allowed when java reflection is not enabled");
          }
        }
        break;
      }
      case OnStart:
      case OnStop: {
        MethodModel method = (MethodModel)this.targetObject;
        if(!relectionAllowed && method.isPrivate()) {
          throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Start or Stop method in class: ["+method.getClassModel().getClassName()+"], Private method is not allowed when java reflection is not enabled");
        }
        ParamInfo[] pTypes = method.getParameters();
        if(method.isStatic()||(pTypes != null && pTypes.length > 1)||
            ((pTypes != null && pTypes.length == 1 && !"com.thwt.core.async.AsyncFuture<java.lang.Void>".equals(pTypes[0].getType().getName())))) {
          throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Start or Stop method in class: ["+method.getClassModel().getClassName()+"], start method cannot be static and should not take more than one parameter!");
        }
        break;
      }
      case OnServerReady: {
    	    MethodModel method = (MethodModel)this.targetObject;
    	    if(!relectionAllowed && method.isPrivate()) {
              throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid OnServerReady method in class: ["+method.getClassModel().getClassName()+"], Private method is not allowed when java reflection is not enabled");
        }
    	    ParamInfo[] pTypes = method.getParameters();
        if(method.isStatic()||(pTypes != null && pTypes.length > 0)) {
          throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid OnServerReady method in class: ["+method.getClassModel().getClassName()+"], OnServerReady method cannot be static and should not take any parameter!");
        }
        break;
      }
      case CommandHandler: {
	    	  MethodModel method = (MethodModel)this.targetObject;
	    	  if(!relectionAllowed && method.isPrivate()) {
	    		  throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Command handler method in class: ["+method.getClassModel().getClassName()+"], Private method is not allowed when java reflection is not enabled");
	    	  }
	    	  ParamInfo[] pTypes = method.getParameters();
	    	  if(method.isStatic()||pTypes == null || pTypes.length > 2 || pTypes.length == 0) {
	    		  throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Command handler  method in class: ["+method.getClassModel().getClassName()+"], Command handler method cannot be static and should take 1 or 2 parameters!");
	    	  }
	    	  ParamInfo cmdParam = pTypes.length == 2 ? pTypes[1] : pTypes[0];
	    	  ParamInfo ctxParam = pTypes.length == 2 ? pTypes[0] : null;
	    	  if(ctxParam != null && "com.thwt.core.command.api.CmdHandlerContext".equals(ctxParam.getType().getName()) == false) {
	    		  throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Command handler  method in class: ["+method.getClassModel().getClassName()+"], 1st parameter of command handler method must be [com.thwt.core.command.api.CmdHandlerContext]");
	    	  }
	    	  String cmdReturnType = ModelUtils.getCommandReturnValueType(cmdParam.getType());
	    	  if(cmdReturnType == null) {
	    		  throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Command handler  method in class: ["+method.getClassModel().getClassName()+"], ["+cmdParam.getType().getName()+"] is not a type command !");	    		  
	    	  }
	    	  String returnType = ModelUtils.getMethodReturnBoxedType(method.getReturnTypeInfo());
	    	  if(!cmdReturnType.equals(returnType)) {
		    	  boolean isCompletable = cmdReturnType.equals(Void.class.getName()) && returnType.equals(Completable.class.getName());
		    	  if(!isCompletable) {
		    		  throw new MustFailedCodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid Command handler  method in class: ["+method.getClassModel().getClassName()+"], method return type :["+returnType+"] is not compatiable with command return type :["+cmdReturnType+"]");	    		  
		    	  }
		      }
	    	  break;
      }
    }
  }
}
