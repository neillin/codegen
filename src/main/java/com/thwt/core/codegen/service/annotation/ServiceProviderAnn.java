/*
 * @(#)ServiceProviderAnn.java	 2017-03-26
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.annotation;

import javax.lang.model.element.AnnotationMirror;

import com.thwt.core.codegen.AnnotationAdaptor;
import com.thwt.core.codegen.ICodeGenerationContext;

/**
 * @author Neil Lin
 *
 */
public class ServiceProviderAnn extends AnnotationAdaptor {

  public ServiceProviderAnn(ICodeGenerationContext ctx, AnnotationMirror ann) {
    super(ctx, ann);
  }

  public String value() {
    String val = getString("value");
    if(val.endsWith(".class")) {
      val = val.substring(0, val.length()-6);
    }
    return val;
  }
}
