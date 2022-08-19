/*
 * @(#)CommandTSModel.java	2018-01-31
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.thwt.core.annotation.DartLang;
import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.model.type.ArrayType;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.service.util.ModelHelper;

/**
 * @author Neil Lin
 *
 */
public class CommandDartModel {
	
	public static final int DataFlow_To_Server =1, DataFlow_From_Server = 2;
	
	private Map<String, CommandClassModel> commands = new HashMap<>();
	private Map<String, JavaBeanModel> models = new HashMap<>();
	private Map<String, Set<String>> externalDartModels = new HashMap<>();

	

	public void addCommand(CommandClassModel cmd) {
		this.commands.put(cmd.getName(), cmd);
		addDartModel(cmd.getCommandReturnTypeInfo(), DataFlow_From_Server);
		for(PropertyInfo p : cmd.getTsClientProperties()) {
			addDartModel(p.getType(), DataFlow_To_Server);
		}
		cmd.processDartInterfaces(this);
	}
	

	
	protected void addDartModel(TypeInfo type, int dataFlow) {
		switch(type.getKind()) {
		case API:
			break;
		case ARRAY:
			addDartModel(((ArrayType)type).getComponentType(), dataFlow);
			break;
		case ASYNC_RESULT:
			break;
		case BOXED_PRIMITIVE:
			break;
		case CLASS_TYPE:
			break;
		case DATA_OBJECT:
			addJsonizableModel(type.getName(), dataFlow);
			break;
		case DATE_TYPE:
			break;
		case ENUM:
			break;
		case FUNCTION:
			break;
		case HANDLER:
			break;
		case JSON_ARRAY:
			break;
		case JSON_OBJECT:
			break;
		case LIST:
			addDartModel(((ParameterizedTypeInfo)type).getArg(0), dataFlow);
			break;
		case MAP:
			addDartModel(((ParameterizedTypeInfo)type).getArg(1), dataFlow);
			break;
		case OBJECT:
			break;
		case OTHER:
			break;
		case PRIMITIVE:
			break;
		case SET:
			addDartModel(((ParameterizedTypeInfo)type).getArg(0), dataFlow);
			break;
		case STRING:
			break;
		case THROWABLE:
			break;
		case VOID:
			break;
		default:
			break;
		}
	}
	
	public void addJsonizableModel(String returnType, int dataFlow) {
		if(this.models.containsKey(returnType)) {
			return;
		}
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		TypeElement elem = elems.getTypeElement(returnType);
		addJsonizableModel(elem, dataFlow, false);
	}

	public void addJsonizableModel(TypeElement elem, int dataFlow, boolean isInterface ) {
		String returnType = elem.getQualifiedName().toString();
		if(this.models.containsKey(returnType)) {
			return;
		}
		Jsonizable ann = elem != null ? elem.getAnnotation(Jsonizable.class) : null;
		DartLang dart = elem != null ? elem.getAnnotation(DartLang.class) : null;
		if(isInterface || ann != null) {
			JavaBeanModel model = isInterface ? new JavaBeanModel(elem) : new JsonizableModel(elem);
			String externalModuleId = dart != null ? dart.externalPackage().trim() : null;
			if (externalModuleId != null && externalModuleId.length() > 0) {			
				this.addExternalDartModel(model.getTypeInfo().getDartName(), externalModuleId);
			} else {
				this.models.put(returnType, model);
				model.processDartInterfaces(this);
				if(dataFlow == DataFlow_From_Server) {
					for(PropertyInfo p : model.getToProperties()) {
						addDartModel(p.getType(), DataFlow_From_Server);
					}
				}else {
					for(PropertyInfo p : model.getFromProperties()) {
						addDartModel(p.getType(), DataFlow_To_Server);
					}
				}
			}
		}
	}
	
	public void addExternalDartModel(String modelName, String packageName) {
		Set<String> set = this.externalDartModels.computeIfAbsent(packageName, id -> new HashSet<>());
		set.add(modelName);
	}
	
	public CommandClassModel[] getCommands() {
		return this.commands.values().toArray(new CommandClassModel[0]);
	}
	
	
	public JsonizableModel[] getJsonizableModels() {
		return this.models.values().stream().filter((val) -> val instanceof JsonizableModel).collect(Collectors.toList()).toArray(new JsonizableModel[0]);
	}
	
	public JavaBeanModel[] getInterfaceModels() {
		return this.models.values().stream().filter((val) -> ! (val instanceof JsonizableModel)).collect(Collectors.toList()).toArray(new JavaBeanModel[0]);
	}
	
	public String[] getDartExternalImports() {
		if (this.externalDartModels.isEmpty()) {
			return new String[0];
		}
		return this.externalDartModels.entrySet().stream().
				filter((entry) -> ! entry.getKey().equals("thwt_core/core.dart")).
				map((entry) -> "import 'package:"+entry.getKey()+"'").
				collect(Collectors.toList()).toArray(new String[0]);
	}
	
	public String getDartImportClasses() {
		return this.models.values().stream().map((model) -> model.getTypeInfo().getTypescriptName()).filter(name -> !this.externalDartModels.containsKey(name)).reduce((result, elem) -> result+","+elem).orElse(null);
	}
	
	public String generatePropertyToJsonStatement(PropertyInfo property) {
		return ModelHelper.generateToJsonDartStatement(property.getType(), property.getName());
	}
	
	public String generatePropertyFromJsonStatement(PropertyInfo property) {
		return ModelHelper.generateFromJsonDartStatement(property.getType(), property.getName());
	}

}
