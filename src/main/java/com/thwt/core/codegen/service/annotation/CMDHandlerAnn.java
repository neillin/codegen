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
public class CMDHandlerAnn extends AnnotationAdaptor {

  public CMDHandlerAnn(ICodeGenerationContext ctx, AnnotationMirror ann) {
    super(ctx, ann);
  }

  public String[] value() {
	  return getArray("value", (val) -> {
			  String s = val.toString();
			  if(s.endsWith(".class")) {
				  s = s.substring(0, s.length()-6);
			  }
			  return s;
	  }, new String[0]);
  };
}
