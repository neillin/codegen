/*
 * @(#)JsonizableConverterGenerator.java	 2017-02-13
 *
 * Copyright 2004-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;


import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.service.model.JsonizableConverterModel;
import com.thwt.core.codegen.service.model.JsonizableModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class JsonizableConverterGenerator extends AbstractCodeGenerator {
  
  private static final Logger log = Logger.getLogger(JsonizableConverterGenerator.class);
  
  /* (non-Javadoc)
   * @see com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
   */
  @Override
  protected void doCodeGeneration(Set<? extends Element> elements,
      final ICodeGenerationContext context) {
    log.debug("Generate service classes for class : {0}",elements);
    for (Element element : elements) {
      Jsonizable ann = element.getAnnotation(Jsonizable.class);
      if((ann != null)&&(element.getKind() == ElementKind.CLASS)){
        JsonizableModel model = new JsonizableModel((TypeElement)element);
        JsonizableConverterModel converter = new JsonizableConverterModel(model);
        try {
          ModelHelper.generateJsonConverterJavaFile(context, converter, "/META-INF/templates/converter.vm");
        } catch (Exception e) {
          log.error("Failed to generate converter provider classes ", e);
          context.reportException(e, null);
        }
      }
    }
  }
}
