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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.AnnotationAdaptor;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.service.annotation.CMDHandlerAnn;
import com.thwt.core.codegen.service.model.CommandClassModel;
import com.thwt.core.codegen.service.model.CommandHandlerFactoryModel;
import com.thwt.core.codegen.service.model.CommandHandlerFactoryModelMap;
import com.thwt.core.codegen.service.model.CommandHandlerModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.FileUtils;
import com.thwt.core.codegen.util.ModelUtils;
import com.thwt.core.command.api.CMDHandler;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class CMDHanlderAnnotationProcessor extends AbstractCodeGenerator {

	private static final Logger log = Logger.getLogger(CMDHanlderAnnotationProcessor.class);
	private CommandHandlerFactoryModelMap map = new CommandHandlerFactoryModelMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wxxr.mobile.core.tools.AbstractCodeGenerator#doCodeGeneration(java.util.
	 * Set, com.wxxr.mobile.core.tools.ICodeGenerationContext)
	 */
	@Override
	protected void doCodeGeneration(Set<? extends Element> elements, final ICodeGenerationContext context) {
		log.debug("Process command annotated classes : {}", elements);
		for (Element element : elements) {
			CMDHandlerAnn cmdAnn = AnnotationAdaptor.<CMDHandlerAnn>getAnnotationAdaptor(context, element, CMDHandler.class,
					new AnnotationAdaptor.AnnotationBuilder<CMDHandlerAnn>() {

						@Override
						public CMDHandlerAnn buildAnnotation(ICodeGenerationContext ctx, AnnotationMirror mirror) {
							return new CMDHandlerAnn(ctx, mirror);
						}
					});

			if ((cmdAnn != null) && (element.getKind() == ElementKind.CLASS)) {
				CommandClassModel model = new CommandClassModel((TypeElement) element);
				String[] cmdClasses = cmdAnn.value();
				if(cmdClasses != null && cmdClasses.length > 0) {
					for(String clazz : cmdClasses) {
						addCommandHandler(((TypeElement) element).getQualifiedName().toString(), clazz);
					}
				} else {
					String cmdClass = ModelUtils.getCommandClassFromHandler(model.getTypeInfo());
					if(cmdClass == null) {
						throw new MustFailedCodeGenException("Class :["+model.getFullQualifiedName()+"] is not a compatiable command handler !");
					}
					addCommandHandler(((TypeElement) element).getQualifiedName().toString(), cmdClass);					
				}
			}
		}
		this.generateHandlerFactory(context);
	}

	/**
	 * @param element
	 * @param clazz
	 */
	private void addCommandHandler(String handlerClass, String clazz) {
		String cmdName = ModelUtils.getCommandName(clazz);
		CommandHandlerModel handlerModel = new CommandHandlerModel(handlerClass);
		handlerModel.setCommandName(cmdName);
		this.map.addHandlerClass(handlerModel);
	}

	public void generateHandlerFactory(ICodeGenerationContext context) {
		if (this.map.isEmpty()) {
			return;
		}
		try {
			for (CommandHandlerFactoryModel pmodel : this.map.getFactories()) {
				generateJavaFile(context, pmodel, "/META-INF/templates/commandHandlerFactory.vm");
//				FileUtils.generateServiceFile(context, CommandHandlerFactory.class.getCanonicalName(), pmodel.getClassName());
			}
		} catch (Exception e) {
			log.error("Failed to generate converter provider classes ", e);
			context.reportException(e, null);
		}
	}

	private void generateJavaFile(final ICodeGenerationContext context, ClassModel model, String template)
			throws Exception {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("model", model);
		attributes.put("helper", ModelHelper.getInstance());
		String text = context.getTemplateRenderer().renderFromFile(template, attributes);
		FileUtils.writeJavaFile(context, model.getTypeInfo().getName(), text);
	}
}
