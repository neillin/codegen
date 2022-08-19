/*
 * @(#)KernelServiceGenerator.java	 2017-02-13
 *
 * Copyright 2004-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;


import static com.thwt.core.codegen.util.FileUtils.createJavaFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.thwt.core.annotation.jmx.ServiceMBean;
import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.AnnotationAdaptor;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.service.annotation.ServiceMBeanAnn;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class KernelServiceGenerator extends AbstractCodeGenerator {
  
  private static final Logger log = Logger.getLogger(KernelServiceGenerator.class);
  /* (non-Javadoc)
   * @see com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
   */
  @Override
  protected void doCodeGeneration(Set<? extends Element> elements,
      final ICodeGenerationContext context) {
    log.debug("Generate service classes for class : {0}",elements);
    for (Element element : elements) {
      ServiceMBeanAnn ann = AnnotationAdaptor.<ServiceMBeanAnn>getAnnotationAdaptor(context, element, ServiceMBean.class, new AnnotationAdaptor.AnnotationBuilder<ServiceMBeanAnn>() {

        @Override
        public ServiceMBeanAnn buildAnnotation(ICodeGenerationContext ctx,
            AnnotationMirror mirror) {
          return new ServiceMBeanAnn(ctx, mirror);
        }
      });
      if((ann != null)&&(element.getKind() == ElementKind.CLASS)){
        try {
          ServiceMBeanModeler modeler = new ServiceMBeanModeler(context, (TypeElement)element, ann);
          generateJavaFile(context, modeler.getModule(), "/META-INF/templates/module.vm");
          generateJavaFile(context, modeler.getVerticle(), "/META-INF/templates/verticle.vm");
          generateJavaFile(context, modeler.getOptions(), "/META-INF/templates/options.vm");
//          generateJavaFile(context, modeler.getConverter(), "/META-INF/templates/converter.vm");
        } catch (Exception e) {
          log.error("Failed to generate service mbean classes for class:"+element, e);
          context.reportException(e, element);
        }
      }
    }

  }
  
  private void generateJavaFile(final ICodeGenerationContext context, ClassModel model, String template) throws Exception {
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("model", model);
    attributes.put("helper", ModelHelper.getInstance());
    String text = context.getTemplateRenderer().renderFromFile(template, attributes);
    createJavaFile(context, model.getPkgName(), model.getName(), text, model.getFileLocation());
  }
}
