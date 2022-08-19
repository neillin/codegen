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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.google.common.base.Strings;
import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.AbstractCodeGenerator;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.service.model.CommandClassModel;
import com.thwt.core.codegen.service.model.CommandDartModel;
import com.thwt.core.codegen.service.model.CommandTSModel;
import com.thwt.core.codegen.service.model.JsonizableConverterModel;
import com.thwt.core.codegen.service.model.JsonizableModel;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.FileUtils;
import com.thwt.core.command.api.CMD;
import com.thwt.core.command.api.Command;
import com.thwt.core.logging.Logger;

/**
 * @author Neil Lin
 *
 */
public class CommandAnnotationProcessor extends AbstractCodeGenerator {

	private static final Logger log = Logger.getLogger(CommandAnnotationProcessor.class);

	private CommandTSModel tsModel;
	private CommandDartModel dartModel;
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
		boolean generateTS = "true".equalsIgnoreCase(MainAnnotationProcessor.getCurrentContext().getOption("generateTS"));
		boolean generateDart = "true".equalsIgnoreCase(MainAnnotationProcessor.getCurrentContext().getOption("generateDart"));
		if(generateTS) {
			this.tsModel = new CommandTSModel();
		}
		if(generateDart) {
			this.dartModel = new CommandDartModel();
		}
		for (Element element : elements) {
			CMD ann = element.getAnnotation(CMD.class);

			if ((ann != null) && (element.getKind() == ElementKind.CLASS)) {
				CommandClassModel model = new CommandClassModel((TypeElement) element);
				String cmdName = getCommandName(ann, model.getName());
				model.setJsonName(cmdName);
				model.setCommandName(cmdName);
				JsonizableConverterModel converter = new JsonizableConverterModel(model);
				if(generateTS) {
					String tsModule = Strings.emptyToNull(ann.tsmodule().trim());
					model.setTsModuleName(tsModule);
					tsModel.addCommand(model);
				}
				if(generateDart) {
					dartModel.addCommand(model);
				}
				try {
					ModelHelper.generateJsonConverterJavaFile(context, converter, "/META-INF/templates/converter.vm");
				} catch (Exception e) {
					log.error("Failed to generate converter provider classes ", e);
					context.reportException(e, null);
				}
			}
		}
		
		if(generateTS) {
			try {
				this.generateCommandTypescript(context);
			} catch (Exception e) {
				log.error("Failed to generate typescript classes ", e);
				context.reportException(e, null);
			}
		}
		if(generateDart) {
			try {
				this.generateCommandDart(context);
			} catch (Exception e) {
				log.error("Failed to generate typescript classes ", e);
				context.reportException(e, null);
			}
		}
	}

	public String getCommandName(CMD ann, String classSimpleName) {
		String catalog = Strings.emptyToNull(ann.catalog().trim());
		return (ann.secure() ? Command.SECURE_COMMAND_CHANNEL_PREFIX : Command.NO_SECURE_COMMAND_CHANNEL_PREFIX)
				+ (catalog != null ? catalog + "." : "") + classSimpleName;
	}
	
	
	private void addJsonizableTSModels(Set<? extends Element> elements, final ICodeGenerationContext context) throws IOException {
		for (Element element : elements) {
	      Jsonizable ann = element.getAnnotation(Jsonizable.class);
	      if((ann != null)&&(element.getKind() == ElementKind.CLASS) && ann.tsName() != ""){
	        this.tsModel.addJsonizableModel((TypeElement)element, CommandTSModel.DataFlow_From_Server);
	      }
	    }
	}
	
	private void addJsonizableDartModels(Set<? extends Element> elements, final ICodeGenerationContext context) throws IOException {
		for (Element element : elements) {
	      Jsonizable ann = element.getAnnotation(Jsonizable.class);
	      if((ann != null)&&(element.getKind() == ElementKind.CLASS) && ann.tsName() != ""){
	        this.dartModel.addJsonizableModel((TypeElement)element, CommandDartModel.DataFlow_From_Server, false);
	      }
	    }
	}
	
	
	
	private void generateCommandTypescript(final ICodeGenerationContext context) throws IOException {
		this.addJsonizableTSModels(context.getRoundEnvironment().getElementsAnnotatedWith(Jsonizable.class), context);
		String target = context.getOption("generateTS.target");
		log.info("Typescript generation target folder: " + target);
		File folder = new File(target);
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("model", this.tsModel);
		attributes.put("helper", ModelHelper.getInstance());
		String text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/command.ts.vm", attributes);
		File cmdFolder = new File(folder,"commands");
		if(!cmdFolder.exists()) {
			cmdFolder.mkdirs();
		}
		FileUtils.createResourceFile(context, cmdFolder, "wefixd.command.ts", text);
		
		text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/cmdmodel.ts.vm", attributes);
		File modelFolder = new File(folder,"models");
		if(!modelFolder.exists()) {
			modelFolder.mkdirs();
		}
		FileUtils.createResourceFile(context, modelFolder, "wefixd.model.ts", text);
	}
	
	private void generateCommandDart(final ICodeGenerationContext context) throws IOException {
		this.addJsonizableDartModels(context.getRoundEnvironment().getElementsAnnotatedWith(Jsonizable.class), context);
		String target = context.getOption("generateDart.target");
		log.info("DartLang generation target folder: " + target);
		File folder = new File(target);
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("model", this.dartModel);
		attributes.put("helper", ModelHelper.getInstance());
		
		File libFolder = new File(folder,"lib/src");
		if(!libFolder.exists()) {
			libFolder.mkdirs();
		}
		
		String text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/command.dart.vm", attributes);
		FileUtils.createResourceFile(context, libFolder, "commands.dart", text);
		
		text = context.getTemplateRenderer().renderFromFile("/META-INF/templates/cmdmodel.dart.vm", attributes);
		FileUtils.createResourceFile(context, libFolder, "models.dart", text);
	}
}
