/*
 * @(#)EntityProviderGenerator.java	 2017-02-13
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
import javax.persistence.Entity;

import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.service.model.EntityClassModel;
import com.thwt.core.codegen.service.model.EntityProviderModel;
import com.thwt.core.codegen.service.model.JsonizableConverterModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.FileUtils;
import com.thwt.core.jpa.api.EntityProvider;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class EntityProviderGenerator extends AbstractCodeGenerator {
  
  private static final Logger log = Logger.getLogger(EntityProviderGenerator.class);
  private List<ClassModel> entities = new ArrayList<>();
  
  /* (non-Javadoc)
   * @see com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
   */
  @Override
  protected void doCodeGeneration(Set<? extends Element> elements,
      final ICodeGenerationContext context) {
    for (Element element : elements) {
      Entity ann = element.getAnnotation(Entity.class);
      if((ann != null)&&(element.getKind() == ElementKind.CLASS)){
        log.debug("Found entity classes for class : {0}",elements);
        EntityClassModel model = new EntityClassModel((TypeElement)element);
        entities.add(model);
        try {
        		ModelHelper.generateJsonConverterJavaFile(context, new JsonizableConverterModel(model), "/META-INF/templates/converter.vm");
          } catch (Exception e) {
            log.error("Failed to generate json converter classe for entity bean :["+model.getClassName()+"]", e);
            context.reportException(e, null);
          }
      }
    }
  }
  
  private void generateJavaFile(final ICodeGenerationContext context, ClassModel model, String template) throws Exception {
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
      EntityProviderModel pmodel = new EntityProviderModel(entities);
      generateJavaFile(context, pmodel, "/META-INF/templates/entityProvider.vm");
      FileUtils.generateServiceFile(context, EntityProvider.class.getCanonicalName(), pmodel.getClassName());
    } catch (Exception e) {
      log.error("Failed to generate converter provider classes ", e);
      context.reportException(e, null);
    }
  }
}
