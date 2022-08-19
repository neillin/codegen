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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;

/**
 * @author Neil Lin
 *
 */
public class CommandHandlerFactoryModel extends ClassModelImpl {
  private List<CommandHandlerModel> models = new ArrayList<>();
  
  public CommandHandlerFactoryModel(String pakName) {
    super(Preconditions.checkNotNull(pakName)+".CommandHandlerFactoryImpl");
  }

  public CommandHandlerFactoryModel addHandlerClass(CommandHandlerModel model) {
    model = Preconditions.checkNotNull(model);
    importClass(model.getClassName());
    models.add(model);
    return this;
  }
  
  public List<CommandHandlerModel> getHandlers() {
    return this.models;
  }
    
}
