/*
 * @(#)ServiceVerticleModel.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.thwt.core.async.AsyncEventMaybe;
import com.thwt.core.async.AsyncFuture;
import com.thwt.core.async.RxJavaHelper;
import com.thwt.core.auth.AuthUser;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.MethodModel;
import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.service.AbstractServiceInjectionPoint;
import com.thwt.core.service.spi.InjectionPoint;

/**
 * @author Neil Lin
 *
 */
public class ServiceVerticleModel extends ClassModelImpl {
	private List<String> injections;
	private final ServiceMBeanModel targetObject;
	private ServiceOptionsModel targetOptions;
	private boolean asyncStart;
	private boolean asyncStop;
	private List<ServiceHandlerProvider> handlers;
	private List<EventMessageHandler> msgHandlers;
	private List<CommandHandler> cmdHandlers;
	private TargetInvocationModel onStart, onStop, onServerReady;

	public ServiceVerticleModel(ServiceMBeanModel target) {
		super(Preconditions.checkNotNull(target).generateVerticleClassName());
		this.targetObject = target;
		this.targetObject.getTypeInfo().collectImports(getImportManager());
	}

	public ServiceVerticleModel addInjection(String fieldName) {
		if(this.injections == null) {
			this.injections = new ArrayList<String>();
		}
		if(!this.injections.contains(fieldName)) {
			this.injections.add(fieldName);
		}
		return this;
	}

	public List<String> getInjections() {
		return this.injections;
	}

	/**
	 * @return the targetObjectClass
	 */
	public String getTargetObjectClass() {
		return targetObject.getName();
	}

	/**
	 * @return the asyncStart
	 */
	public boolean isAsyncStart() {
		return asyncStart;
	}
	/**
	 * @return the asyncStop
	 */
	public boolean isAsyncStop() {
		return asyncStop;
	}
	/**
	 * @return the handlers
	 */
	public List<ServiceHandlerProvider> getHandlers() {
		return handlers;
	}
	/**
	 * @param handlers the handlers to set
	 */
	public ServiceVerticleModel addHandler(ServiceHandlerProvider handler) {
		if(this.handlers == null) {
			this.handlers = new ArrayList<ServiceHandlerProvider>();
		}
		if(!this.handlers.contains(handler)) {
			this.handlers.add(handler);
		}
		return this;
	}

	/**
	 * @return the handlers
	 */
	public List<EventMessageHandler> getEventMessageHandlers() {
		return this.msgHandlers;
	}
	
	/**
	 * @return the command handlers
	 */
	public List<CommandHandler> getCommandHandlers() {
		return this.cmdHandlers;
	}

	/**
	 * @param handlers the handlers to set
	 */
	public ServiceVerticleModel addEventMessageHandler(EventMessageHandler handler) {
		if(this.msgHandlers == null) {
			this.msgHandlers = new ArrayList<EventMessageHandler>();
		}
		if(!this.msgHandlers.contains(handler)) {
			this.msgHandlers.add(handler);
		}
		return this;
	}
	
	
	/**
	 * @param handlers the handlers to set
	 */
	public ServiceVerticleModel addCommandHandler(CommandHandler handler) {
		if(this.cmdHandlers == null) {
			this.cmdHandlers = new ArrayList<CommandHandler>();
			importClass(AbstractServiceInjectionPoint.class.getName());
			importClass(InjectionPoint.class.getName());
		}
		if(!this.cmdHandlers.contains(handler)) {
			this.cmdHandlers.add(handler);
			importClass(AuthUser.class.getName());
			importClass(AsyncEventMaybe.class.getName());
			importClass(RxJavaHelper.class.getName());
		}
		return this;
	}


	/**
	 * @return the onStart
	 */
	public TargetInvocationModel getOnStart() {
		return onStart;
	}
	
	/**
	 * @param method the onStart to set
	 */
	public ServiceVerticleModel setOnStart(TargetInvocationModel invModel) {
		if(this.onStart != null) {
			throw new MustFailedCodeGenException("Only one start method is allowed !");
		}
		MethodModel method = (MethodModel)invModel.getTargetObject();
		ParamInfo[] pTypes = method.getParameters();
		if(pTypes != null && pTypes.length == 1) {
			this.asyncStart = true;
			importClass(AsyncFuture.class.getName());
		}
		this.onStart = invModel;
		return this;
	}

	/**
	 * @return the onStop
	 */
	public TargetInvocationModel getOnStop() {
		return onStop;
	}

	/**
	 * @param method the onStop to set
	 */
	public ServiceVerticleModel setOnStop(TargetInvocationModel invModel) {
		if(this.onStop != null) {
			throw new MustFailedCodeGenException("Only one stop method is allowed !");
		}
		MethodModel method = (MethodModel)invModel.getTargetObject();
		ParamInfo[] pTypes = method.getParameters();
		//    if(method.isPrivate()||method.isStatic()||(method.getParameterTypes() != null && method.getParameterTypes().length > 1)||
		//        ((method.getParameterTypes() != null && method.getParameterTypes().length == 1 && !"io.vertx.core.Future<java.lang.Void>".equals(method.getParameterTypes()[0])))) {
		//      throw new CodeGenException("Method :["+method.generateMethodSignature()+"] is not a valid stop method in class :["+this.targetObject.getClassName()+"]");
		//    }
		if(pTypes != null && pTypes.length == 1) {
			this.asyncStop = true;
			importClass(AsyncFuture.class.getName());
		}
		this.onStop = invModel;
		return this;
	}

	/**
	 * @return the targetOptions
	 */
	public String getTargetOptionsClass() {
		return targetOptions.getName();
	}

	/**
	 * @param targetOptions the targetOptions to set
	 */
	public ServiceVerticleModel setTargetOptions(ServiceOptionsModel model) {
		this.targetOptions = Preconditions.checkNotNull(model);
		importClass(model.getClassName());
		return this;
	}

	/* (non-Javadoc)
	 * @see com.thwt.core.codegen.model.ClassModel#addField(com.thwt.core.codegen.model.FieldModel)
	 */
	@Override
	public void addField(FieldModel field) {
		super.addField(field);
		if(field instanceof InjectionPointModel) {
			addInjection(field.getName());
			importClass(((InjectionPointModel)field).getInjectedValueType());
		}
	}

	/**
	 * @return the onServerReady
	 */
	public TargetInvocationModel getOnServerReady() {
		return onServerReady;
	}

	/**
	 * @param onServerReady the onServerReady to set
	 */
	public ServiceVerticleModel setOnServerReady(TargetInvocationModel invModel) {
		if(this.onServerReady != null) {
			throw new MustFailedCodeGenException("Only one On Server Ready method is allowed !");
		}
		this.onServerReady = invModel;
		return this;
	}
}
