/*
 * @(#)DefaultProcessorConfigure.java  2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import javax.annotation.processing.SupportedOptions;
import javax.persistence.Entity;

import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.annotation.ServiceProvider;
import com.thwt.core.annotation.jmx.ServiceMBean;
import com.thwt.core.annotation.service.Converter;
import com.thwt.core.annotation.service.Module;
import com.thwt.core.codegen.IProcessorConfigure;
import com.thwt.core.codegen.annotation.DefaultTemplates;
import com.thwt.core.codegen.annotation.Generator;
import com.thwt.core.command.api.CMD;
import com.thwt.core.command.api.CMDHandler;

/**
 * @author neillin
 *
 */
@DefaultTemplates("META-INF/templates/service_macros.vm")
@SupportedOptions({
	"codegen.javasource.raw",
	"generateTS.target",		// folder that generated typescript files should be saved to
	"generateTS"		// generate Typescript code, by default processor will not generate typescript code
})
public class DefaultProcessorConfigure implements IProcessorConfigure {
  
  @Generator(forAnnotation=ServiceMBean.class)
  public KernelServiceGenerator getServiceModuleGenerator(Generator annotation){
    return new KernelServiceGenerator();
  }	

  @Generator(forAnnotation=Converter.class)
  public ConverterProviderGenerator getConverterProviderGenerator(Generator annotation){
    return new ConverterProviderGenerator();
  } 

  @Generator(forAnnotation=Module.class)
  public ModuleProviderGenerator getModuleProviderGenerator(Generator annotation){
    return new ModuleProviderGenerator();
  } 

  @Generator(forAnnotation=Jsonizable.class)
  public JsonizableConverterGenerator getJsonizableConverterGenerator(Generator annotation){
    return new JsonizableConverterGenerator();
  } 

  @Generator(forAnnotation=ServiceProvider.class)
  public ServiceProviderGenerator getServiceProviderGenerator(Generator annotation){
    return new ServiceProviderGenerator();
  }
  
  @Generator(forAnnotation=Entity.class)
  public EntityProviderGenerator getEntityProviderGenerator(Generator annotation){
    return new EntityProviderGenerator();
  }
  
  @Generator(forAnnotation=CMD.class)
  public CommandAnnotationProcessor getCommandAnnationProcessor(Generator annotation){
    return new CommandAnnotationProcessor();
  }
  
  @Generator(forAnnotation=CMDHandler.class)
  public CMDHanlderAnnotationProcessor getCmdHandlerAnnationProcessor(Generator annotation){
    return new CMDHanlderAnnotationProcessor();
  }


}
