/*
 * @(#)ServiceHandlerAnn.java   2017-02-17
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
public class ServiceHandlerAnn extends AnnotationAdaptor {

	/**
	 * @param ctx
	 * @param ann
	 */
	public ServiceHandlerAnn(ICodeGenerationContext ctx, AnnotationMirror ann) {
		super(ctx, ann);
	}

	public String[] value() {
		return getArray("value", new ValueBuilder<String>() {

			@Override
			public String buildAnnotation(Object val) {
			  String s = val.toString();
			  if(s.endsWith(".class")) {
			    s = s.substring(0, s.length()-6);
			  }
				return s;
			}
		}, new String[0]);

	};

}
