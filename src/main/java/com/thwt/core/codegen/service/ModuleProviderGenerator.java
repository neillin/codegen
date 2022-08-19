/*
 * @(#)ModuleProviderGenerator.java	 2017-02-13
 *
 * Copyright 2004-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.thwt.core.annotation.service.Module;
import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.service.model.ModuleProviderModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.FileUtils;
import com.thwt.core.kernel.api.ServiceModuleProvider;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class ModuleProviderGenerator extends AbstractCodeGenerator {
  
  private static final Logger log = Logger.getLogger(ModuleProviderGenerator.class);
  private List<ClassModel> modules = new ArrayList<ClassModel>();
  
  /* (non-Javadoc)
   * @see com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
   */
  @Override
  protected void doCodeGeneration(Set<? extends Element> elements,
      final ICodeGenerationContext context) {
    log.debug("Generate service classes for class : {0}",elements);
    for (Element element : elements) {
      Module ann = element.getAnnotation(Module.class);
      if((ann != null)&&(element.getKind() == ElementKind.CLASS)){
        ClassModelImpl model = new ClassModelImpl((TypeElement)element);
        modules.add(model);
      }
    }
  }
  
  public static void generateJavaFile(final ICodeGenerationContext context, ClassModel model, String template) throws Exception {
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("model", model);
    attributes.put("helper", ModelHelper.getInstance());
    String text = context.getTemplateRenderer().renderFromFile(template, attributes);
    FileUtils.writeJavaFile(context, model.getTypeInfo().getName(), text);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.AbstractCodeGenerator#finishProcessing(com.thwt.core.codegen.ICodeGenerationContext)
   */
  @Override
  public void finishProcessing(ICodeGenerationContext context) {
    try {
      ModuleProviderModel pmodel = new ModuleProviderModel(this.modules);
      generateJavaFile(context, pmodel, "/META-INF/templates/moduleProvider.vm");
      FileUtils.generateServiceFile(context, ServiceModuleProvider.class.getCanonicalName(), pmodel.getClassName());
    } catch (Exception e) {
      log.error("Failed to generate converter provider classes ", e);
      context.reportException(e, null);
    }

  }
}
