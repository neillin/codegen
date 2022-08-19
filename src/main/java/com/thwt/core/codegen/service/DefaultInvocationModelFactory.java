/*
 * @(#)DefaultInvocationModelFactory.java	 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;

import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.MemberModel;
import com.thwt.core.codegen.service.model.InvocationModelFactory;
import com.thwt.core.codegen.service.model.ServiceVerticleModel;
import com.thwt.core.codegen.service.model.TargetInvocationModel;

/**
 * @author Neil Lin
 *
 */
public class DefaultInvocationModelFactory implements InvocationModelFactory {

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.service.InvocationModelGenerator#createInvocationModel(com.thwt.core.codegen.ICodeGenerationContext, com.thwt.core.codegen.service.model.ServiceVerticleModel, javax.lang.model.element.Element)
   */
  @Override
  public TargetInvocationModel createInvocationModel(ICodeGenerationContext ctx,
      ServiceVerticleModel classModel, MemberModel target) {
    TargetInvocationModel model = new TargetInvocationModelImpl();
    model.setThisClass(classModel);
    model.setTargetObject(target);
    return model;
  }

}
