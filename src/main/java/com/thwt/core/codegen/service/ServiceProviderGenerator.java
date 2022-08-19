/*
 * @(#)ServiceProviderGenerator.java   2017-02-13
 *
 * Copyright 2004-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;


import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thwt.core.annotation.ServiceProvider;
import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.AnnotationAdaptor;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.service.annotation.ServiceProviderAnn;
import com.thwt.core.codegen.util.FileUtils;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class ServiceProviderGenerator extends AbstractCodeGenerator {
  
  private static final Logger log = Logger.getLogger(ConverterProviderGenerator.class);
  private Multimap<String, String> services = ArrayListMultimap.<String, String>create();
  
  /* (non-Javadoc)
   * @see com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
   */
  @Override
  protected void doCodeGeneration(Set<? extends Element> elements,
      final ICodeGenerationContext context) {
    log.debug("Found service provider class : {0}",elements);
    for (Element element : elements) {
      ServiceProviderAnn ann = AnnotationAdaptor.<ServiceProviderAnn>getAnnotationAdaptor(context, element, ServiceProvider.class, new AnnotationAdaptor.AnnotationBuilder<ServiceProviderAnn>() {

        @Override
        public ServiceProviderAnn buildAnnotation(ICodeGenerationContext ctx,
            AnnotationMirror mirror) {
          return new ServiceProviderAnn(ctx, mirror);
        }
      });
      if((ann != null)&&(element.getKind() == ElementKind.CLASS)){
//        ClassModelImpl model = new ClassModelImpl((TypeElement)element);
        services.put(ann.value(), element.toString());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.AbstractCodeGenerator#finishProcessing(com.thwt.core.codegen.ICodeGenerationContext)
   */
  @Override
  public void finishProcessing(ICodeGenerationContext context) {
    for(String className : services.keySet()) {
      String content = Joiner.on('\n').join(services.get(className));
      try {
        FileUtils.generateServiceFile(context, className, content);
      } catch (Exception e) {
        log.error("Failed to generate converter provider classes ", e);
        context.reportException(e, null);
      }
    }
  }
}
