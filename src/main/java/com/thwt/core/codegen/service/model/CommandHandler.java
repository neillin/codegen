/*
 * @(#)CommandHandler.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ImportManager;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.command.api.CommandRegistry;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * @author Neil Lin
 *
 */
public class CommandHandler {
	private String cmdName;
	private TargetInvocationModel target;
	private TypeInfo cmdClass,returnType;
	private boolean async, hasContext, voidReturn;



	/**
	 * @return the target
	 */
	public TargetInvocationModel getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(TargetInvocationModel target) {
		this.target = target;
	}

	public String generateGetStatement(String targetObjectName, String className) {
		if (this.target.isFieldTarget()) {
			return this.target.generateGetStatement(targetObjectName);
		} else {
			return this.target.generateCallStatement(targetObjectName, className);
		}
	}

	public void collectImports(ImportManager mgr) {
		this.target.collectImports(mgr);
		//		mgr.importClass(AbstractMessageHandler.class.getName());
		mgr.importClass(CommandRegistry.class.getName());
		this.cmdClass.collectImports(mgr);
		this.returnType.collectImports(mgr);
	}

	/**
	 * @return the async
	 */
	public boolean isAsync() {
		return async;
	}

	/**
	 * @param async
	 *            the async to set
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}

	/**
	 * @return the cmdName
	 */
	public String getCmdName() {
		return cmdName;
	}

	/**
	 * @param cmdName
	 *            the cmdName to set
	 */
	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	/**
	 * @return the cmdClass
	 */
	public TypeInfo getCmdClass() {
		return cmdClass;
	}

	/**
	 * @param cmdClass the cmdClass to set
	 */
	public void setCmdClass(TypeInfo cmdClass) {
		this.cmdClass = cmdClass;
	}

	/**
	 * @return the hasContext
	 */
	public boolean isHasContext() {
		return hasContext;
	}

	/**
	 * @param hasContext the hasContext to set
	 */
	public void setHasContext(boolean hasContext) {
		this.hasContext = hasContext;
	}



	/**
	 * @return the voidReturn
	 */
	public boolean isVoidReturn() {
		return voidReturn;
	}

	/**
	 * @param voidReturn the voidReturn to set
	 */
	public void setVoidReturn(boolean voidReturn) {
		this.voidReturn = voidReturn;
	}

	/**
	 * @return the returnType
	 */
	public TypeInfo getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(TypeInfo returnType) {
		this.returnType = returnType;
	}

	public String getToObserverMethod() {
		String returnTypeString = this.returnType.getName();
		if(returnTypeString.startsWith(Single.class.getName())) {
			return "toSingleObserver";
		}
		if(returnTypeString.startsWith(Maybe.class.getName())) {
			return "toMaybeObserver";
		}
		if(returnTypeString.startsWith(Completable.class.getName())) {
			return "toCompletableObserver";
		}
		throw new MustFailedCodeGenException("Cannot generate observer for :"+returnTypeString);
	}

}
