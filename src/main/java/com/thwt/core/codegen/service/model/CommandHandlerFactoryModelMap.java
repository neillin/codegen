/*
 * @(#)ConverterProviderModel.java	 2017-02-14
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * @author Neil Lin
 *
 */
public class CommandHandlerFactoryModelMap {
  private Map<String, CommandHandlerFactoryModel> models = new HashMap<>();
  
  public CommandHandlerFactoryModelMap addHandlerClass(CommandHandlerModel model) {
    model = Preconditions.checkNotNull(model);
    String pkgName = model.getPkgName();
    CommandHandlerFactoryModel handlerFactory = this.models.get(pkgName);
    if(handlerFactory == null) {
    		handlerFactory = new CommandHandlerFactoryModel(pkgName);
    		models.put(pkgName, handlerFactory);
    }
	handlerFactory.addHandlerClass(model);
    return this;
  }
  
  public Collection<CommandHandlerFactoryModel> getFactories() {
    return this.models.values();
  }
    
  public boolean isEmpty() {
	  return this.models.isEmpty();
  }
}
