/*
 * @(#)InjectionPointModelFactory.java	 2017-02-17
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
import com.thwt.core.codegen.service.DefaultInjectionPointFactory;

import io.vertx.core.ServiceHelper;

/**
 * @author Neil Lin
 *
 */
public interface InjectionPointModelFactory {
  public class Factory {
    public static InjectionPointModelFactory getGenerator() {
      InjectionPointModelFactory gen = ServiceHelper.loadFactoryOrNull(InjectionPointModelFactory.class);
      if(gen == null) {
        gen = new DefaultInjectionPointFactory();
      }
      return gen;
    }
  }
  InjectionPointModel createInjectionPoint(ICodeGenerationContext ctx, ServiceVerticleModel classModel, MemberModel element);
}
