/*
 * @(#)ServiceMBeanModeler.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.google.common.base.Preconditions;
import com.thwt.core.annotation.Inject;
import com.thwt.core.annotation.jmx.ManagedAttribute;
import com.thwt.core.annotation.service.LocalService;
import com.thwt.core.annotation.service.OnServerReady;
import com.thwt.core.annotation.service.OnStart;
import com.thwt.core.annotation.service.OnStop;
import com.thwt.core.annotation.service.ServiceHandler;
import com.thwt.core.codegen.AnnotationAdaptor;
import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.MethodModel;
import com.thwt.core.codegen.model.type.ParamInfo;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.service.annotation.ServiceHandlerAnn;
import com.thwt.core.codegen.service.annotation.ServiceMBeanAnn;
import com.thwt.core.codegen.service.model.CommandHandler;
import com.thwt.core.codegen.service.model.EventMessageHandler;
import com.thwt.core.codegen.service.model.InjectionPointModel;
import com.thwt.core.codegen.service.model.InjectionPointModelFactory;
import com.thwt.core.codegen.service.model.InvocationMode;
import com.thwt.core.codegen.service.model.InvocationModelFactory;
import com.thwt.core.codegen.service.model.KernelModuleModel;
import com.thwt.core.codegen.service.model.OptionFieldModel;
import com.thwt.core.codegen.service.model.ServiceHandlerProvider;
import com.thwt.core.codegen.service.model.ServiceMBeanModel;
import com.thwt.core.codegen.service.model.ServiceOptionsModel;
import com.thwt.core.codegen.service.model.ServiceVerticleModel;
import com.thwt.core.codegen.service.model.TargetInvocationModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.ModelUtils;
import com.thwt.core.codegen.util.ModelUtils.FieldHandler;
import com.thwt.core.codegen.util.ModelUtils.MethodHandler;
import com.thwt.core.command.api.CMDHandler;
import com.thwt.core.command.api.CommandRegistry;
import com.thwt.core.event.MessageAddress;
import com.thwt.core.event.MessageHandler;
import com.thwt.core.logging.Logger;
import com.thwt.core.util.Utils;

import io.reactivex.Single;

/**
 * @author Neil Lin
 *
 */
public class ServiceMBeanModeler {
  
  private static final Logger log = Logger.getLogger(ServiceMBeanModeler.class);
  
  private static class PropertyDescriptor {
    private String name, defaultValue;
    private MethodModel getter, setter;
  }

  private final ICodeGenerationContext context;
  private final TypeElement element;
  private final ServiceMBeanAnn serviceAnn;
  private final ServiceMBeanModel model; 
  private final KernelModuleModel module;
  private final ServiceOptionsModel options;
  private final ServiceVerticleModel verticle;
//  private final OptionsConverterModel converter;
  private final Set<MethodModel> managedAttrs;

  /**
   * 
   */
  public ServiceMBeanModeler(ICodeGenerationContext ctx, TypeElement elem, ServiceMBeanAnn ann) {
    this.context = Preconditions.checkNotNull(ctx);
    this.element = Preconditions.checkNotNull(elem);
    this.serviceAnn = Preconditions.checkNotNull(ann);
    this.model = new ServiceMBeanModel(this.element);
    this.module = new KernelModuleModel(this.model);
    this.options = new ServiceOptionsModel(this.model);
    this.verticle = new ServiceVerticleModel(this.model);
//    this.converter = new OptionsConverterModel(this.options);
    this.managedAttrs = new HashSet<MethodModel>();
    this.module.setAnn(serviceAnn);
    this.module.setTargetVerticle(verticle);
    this.verticle.setTargetOptions(options);

    doProcess();
    
    if(this.verticle.getCommandHandlers() != null) {
    	this.module.addDependency(CommandRegistry.class.getName());
    }
  }
  
//  private void setupPackageName(ClassModel model) {
//    if(pkgset) {
//      return;
//    }
//    String mbeanClass = getServiceName(model.getName());
//    String pkgName = model.getPkgName();
//    options.setPkgName(pkgName);
//    options.setName(mbeanClass+"Options");
//    options.setTargetObject(model);
//    verticle.setPkgName(pkgName);
//    verticle.setName(mbeanClass+"Verticle");
//    verticle.setTargetObject(model);
//    verticle.setTargetOptions(options);
//    converter.setPkgName(pkgName);
//    converter.setName(mbeanClass+"OptsConverter");
//    module.setPkgName(pkgName);
//    module.setName(mbeanClass+"Module");
//    module.setAnn(serviceAnn);
//    module.setTargetObject(model);
//    module.setTargetVerticle(verticle);
//    pkgset = true;
//  }

//  private String getServiceName(String name) {
//    if(name.endsWith("Impl")) {
//      name = name.substring(0,name.length()-4);
//    }
//    return name;
//  }

  protected void doProcess() {
    ModelParser parser = new ModelParser();
    ModelUtils.collectionBasicClassInfo(context, (TypeElement)element, model, parser, parser);
    List<String> selfDeps = this.module.checkSelfDependencies();
    if(!selfDeps.isEmpty()) {
      throw new CodeGenException("Found service depending on server providing but itself, which is not allowed ! class :["+model.getClassName()+"]");
    }
    if(!managedAttrs.isEmpty()) {
      Map<String, PropertyDescriptor> descs = new HashMap<String, PropertyDescriptor>();
      for(MethodModel m : managedAttrs) {
        ManagedAttribute ann = m.getElement().getAnnotation(ManagedAttribute.class);
        String name = m.getName();
        if(name.startsWith("set")&&(m.getParameters().length == 1)) {
          String property = Utils.uncapitalize(name.substring(3));
          PropertyDescriptor desc = descs.get(property);
          if(desc == null) {
            desc = new PropertyDescriptor();
            desc.name = property;
            descs.put(property, desc);
          }
          desc.setter = m;
          if(!Utils.isBlank(ann.defaultValue())) {
            desc.defaultValue = ann.defaultValue();
          }
        }else if(name.startsWith("get")) {
          String property = Utils.uncapitalize(name.substring(3));
          PropertyDescriptor desc = descs.get(property);
          if(desc == null) {
            desc = new PropertyDescriptor();
            desc.name = property;
            descs.put(property, desc);
          }
          desc.getter = m;
          if(!Utils.isBlank(ann.defaultValue())) {
            desc.defaultValue = ann.defaultValue();
          }
        } else if(name.startsWith("is")) {
          String property = Utils.uncapitalize(name.substring(2));
          PropertyDescriptor desc = descs.get(property);
          if(desc == null) {
            desc = new PropertyDescriptor();
            desc.name = property;
            descs.put(property, desc);
          }
          desc.getter = m;
          if(!Utils.isBlank(ann.defaultValue())) {
            desc.defaultValue = ann.defaultValue();
          }
        }
      }
      for(PropertyDescriptor desc : descs.values()) {
        if(desc.getter != null && desc.setter != null && desc.getter.getReturnTypeInfo().equals(desc.setter.getParameters()[0].getType())){
          if(desc.getter.isStatic() || desc.getter.isPrivate() || desc.setter.isPrivate() || desc.setter.isStatic()) {
            context.reportException(new IllegalArgumentException("Neither Getter nor Setter method of managed property:["+desc.name+"] could be private or static !"), element);
            continue;
          }
          OptionFieldModel field = new OptionFieldModel(options);
          field.setGetter(desc.getter);
          field.setSetter(desc.setter);
          field.setType(desc.getter.getReturnTypeInfo().getName());
          field.setName(desc.name);
          field.setDefaultValue(desc.defaultValue);
          options.addField(field);
          ModelUtils.generateGetterMethod(context, field);
          ModelUtils.generateSetterMethod(context, field);
        }
      }
    }
//    converter.setOptionsName(this.module.getModuleId());
//    converter.setOptions(this.options);
    model.setModuleId(this.module.getModuleId());
  }

  private class ModelParser implements MethodHandler, FieldHandler {
    
    
    @Override
    public boolean processField(ClassModel model, Element element) {
//      setupPackageName(model);
      LocalService serviceAnn = element.getAnnotation(LocalService.class);
      ServiceHandlerAnn handlerAnn = AnnotationAdaptor.<ServiceHandlerAnn>getAnnotationAdaptor(context, element, ServiceHandler.class, new AnnotationAdaptor.AnnotationBuilder<ServiceHandlerAnn>() {

        @Override
        public ServiceHandlerAnn buildAnnotation(ICodeGenerationContext ctx,
            AnnotationMirror mirror) {
          return new ServiceHandlerAnn(ctx, mirror);
        }
      });
      Inject injectAnn = element.getAnnotation(Inject.class);
      FieldModel field = ModelUtils.createSimpleFieldModel(context,model,element);
      model.addField(field);
      if(serviceAnn != null || injectAnn != null) {
        InjectionPointModel injectField = InjectionPointModelFactory.Factory.getGenerator().createInjectionPoint(context, verticle, field);
        if(injectField != null && injectField.isServiceInjection()) {
          if(injectField.isOptional()) {
	            module.addOptionalDependency(injectField.getInjectedValueType());		        	    	
	  	    }else {
	  	    		module.addDependency(injectField.getInjectedValueType());
	  	    }
        }
      }else if(handlerAnn != null){
        TargetInvocationModel invocation = InvocationModelFactory.Factory.getFactory().createInvocationModel(context, verticle, field);
        invocation.validateTarget(context, InvocationMode.ServiceHandler);
        invocation.collectImports(verticle.getImportManager());
        for(String clazz : handlerAnn.value()) {
          ServiceHandlerProvider prov = new ServiceHandlerProvider();
          prov.setClazz(verticle.importClass(clazz));
          prov.setTarget(invocation);
          verticle.addHandler(prov);
        }
      }
      return true;
    }
    
    @Override
    public boolean processMethod(ClassModel model, ExecutableElement element) {
	    	try {
	    		return doProcessMethod(model, element);
	    	} catch(MustFailedCodeGenException e) {
	    		throw e;
	    	}catch(Throwable t) {
	    		log.warn("Caught throwable when processing method :["+element+"], method would not be further processed !", t);
	    		return false;
	    	}
    }

	private boolean doProcessMethod(ClassModel model, ExecutableElement element) {
		//      setupPackageName(model);
		      OnStart onStart = element.getAnnotation(OnStart.class);
		      OnStop onStop = element.getAnnotation(OnStop.class);
		      OnServerReady onServerReady = element.getAnnotation(OnServerReady.class);
		      LocalService service = element.getAnnotation(LocalService.class);
		      Inject inject = element.getAnnotation(Inject.class);
		      ServiceHandlerAnn handlerAnn = AnnotationAdaptor.<ServiceHandlerAnn>getAnnotationAdaptor(context, element, ServiceHandler.class, new AnnotationAdaptor.AnnotationBuilder<ServiceHandlerAnn>() {
		
		        @Override
		        public ServiceHandlerAnn buildAnnotation(ICodeGenerationContext ctx,
		            AnnotationMirror mirror) {
		          return new ServiceHandlerAnn(ctx, mirror);
		        }
		      });
		      ManagedAttribute attr = element.getAnnotation(ManagedAttribute.class);
		      MessageHandler msgHandler = element.getAnnotation(MessageHandler.class);
		      CMDHandler cmdHandler = element.getAnnotation(CMDHandler.class);
		      MethodModel method = ModelUtils.createSimpleMethod(context,model, element);
		      model.addMethod(method);
		      TargetInvocationModel invocation = InvocationModelFactory.Factory.getFactory().createInvocationModel(context, verticle, method);
		      log.debug("Found method :[{}], method key :[{}]", method.generateMethodSignature(), method.getMethodKey());
		      if(service != null || inject != null) {
		          InjectionPointModel injectField = InjectionPointModelFactory.Factory.getGenerator().createInjectionPoint(context, verticle, method);
		          if(injectField != null && injectField.isServiceInjection()) {
		        	    if(injectField.isOptional()) {
				            module.addOptionalDependency(injectField.getInjectedValueType());		        	    	
		        	    }else {
		        	    		module.addDependency(injectField.getInjectedValueType());
		        	    }
		          }
			  }else if(onStart != null) {
		        invocation.validateTarget(context, InvocationMode.OnStart);
		        verticle.setOnStart(invocation);
		        invocation.collectImports(verticle.getImportManager());
		      }else if(onStop != null) {
		        invocation.validateTarget(context, InvocationMode.OnStop);
		        verticle.setOnStop(invocation);
		        invocation.collectImports(verticle.getImportManager());
		      }else if(onServerReady != null) {
			    	invocation.validateTarget(context, InvocationMode.OnServerReady);
			    	verticle.setOnServerReady(invocation);
			    	invocation.collectImports(verticle.getImportManager());
		      }else if(handlerAnn != null) {
		        invocation.validateTarget(context, InvocationMode.ServiceHandler);
		        for(String clazz : handlerAnn.value()) {
		          ServiceHandlerProvider prov = new ServiceHandlerProvider();
		          String clsName = verticle.importClass(clazz);
		          prov.setClazz(clsName);
		          prov.setTarget(invocation);
		          verticle.addHandler(prov);
		        }
		        invocation.collectImports(verticle.getImportManager());
		      }else if(attr != null) {
		        managedAttrs.add(method);
		      }else if(msgHandler != null) {
		        invocation.validateTarget(context, InvocationMode.MessageHandler);
		        EventMessageHandler evtMsgHandler = new EventMessageHandler();
		        evtMsgHandler.setTarget(invocation);
		        ParameterizedTypeInfo pType = (ParameterizedTypeInfo)method.getParameters()[0].getType();
		        evtMsgHandler.setClazz(pType.getArg(0));
		        MessageAddress channel = element.getAnnotation(MessageAddress.class);
		        if(channel != null) {
		          evtMsgHandler.setChannel(channel.value());
		        }
		        verticle.addEventMessageHandler(evtMsgHandler);
		        evtMsgHandler.collectImports(verticle.getImportManager());
		      }else if(cmdHandler != null) {
			        invocation.validateTarget(context, InvocationMode.CommandHandler);
			        CommandHandler commandHandler = new CommandHandler();
			        commandHandler.setTarget(invocation);
			        ParamInfo[] params = method.getParameters();
		        	commandHandler.setCmdClass(params.length == 2 ? params[1].getType() : params[0].getType());
		        	commandHandler.setHasContext(params.length == 2 );
		        	commandHandler.setAsync(ModelUtils.isReactiveValueType(method.getReturnTypeInfo()));
		        	commandHandler.setCmdName(ModelUtils.getCommandName(commandHandler.getCmdClass().getName()));
		        	commandHandler.setVoidReturn("java.lang.Void".equals(ModelUtils.getMethodReturnBoxedType(method.getReturnTypeInfo())));
		        	commandHandler.setReturnType(method.getReturnTypeInfo());
			        verticle.addCommandHandler(commandHandler);
			        commandHandler.collectImports(verticle.getImportManager());
			  }
		      return true;
	}
  }

  
  
  /**
   * @return the model
   */
  public ClassModel getModel() {
    return model;
  }

  /**
   * @return the module
   */
  public KernelModuleModel getModule() {
    return module;
  }

  /**
   * @return the options
   */
  public ServiceOptionsModel getOptions() {
    return options;
  }

  /**
   * @return the verticle
   */
  public ServiceVerticleModel getVerticle() {
    return verticle;
  }

//  /**
//   * @return the converter
//   */
//  public OptionsConverterModel getConverter() {
//    return converter;
//  }
}
