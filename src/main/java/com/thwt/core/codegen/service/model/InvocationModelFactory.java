/*
 * @(#)InvocationModelFactory.java	 2017-02-17
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.MemberModel;
import com.thwt.core.codegen.service.DefaultInvocationModelFactory;

import io.vertx.core.ServiceHelper;

/**
 * @author Neil Lin
 *
 */
public interface InvocationModelFactory {
  public class Factory {
    public static InvocationModelFactory getFactory() {
      InvocationModelFactory gen = ServiceHelper.loadFactoryOrNull(InvocationModelFactory.class);
      if(gen == null) {
        gen = new DefaultInvocationModelFactory();
      }
      return gen;
    }
  }
  TargetInvocationModel createInvocationModel(ICodeGenerationContext ctx, ServiceVerticleModel classModel, MemberModel target);
}
