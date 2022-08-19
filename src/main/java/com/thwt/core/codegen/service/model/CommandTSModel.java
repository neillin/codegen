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

import com.google.common.base.Joiner;
import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.model.type.ArrayType;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.codegen.model.type.TypeInfo;

/**
 * @author Neil Lin
 *
 */
public class CommandTSModel {
	
	public static final int DataFlow_To_Server =1, DataFlow_From_Server = 2;
	
	private Map<String, CommandClassModel> commands = new HashMap<>();
	private Map<String, JsonizableModel> models = new HashMap<>();
	private Map<String, Set<String>> externalTsModels = new HashMap<>();
	

	public void addCommand(CommandClassModel cmd) {
		this.commands.put(cmd.getName(), cmd);
		addTsModel(cmd.getCommandReturnTypeInfo(), DataFlow_From_Server);
		for(PropertyInfo p : cmd.getTsClientProperties()) {
			addTsModel(p.getType(), DataFlow_To_Server);
		}
	}
	
	protected void addTsModel(TypeInfo type, int dataFlow) {
		switch(type.getKind()) {
		case API:
			break;
		case ARRAY:
			addTsModel(((ArrayType)type).getComponentType(), dataFlow);
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
			addTsModel(((ParameterizedTypeInfo)type).getArg(0), dataFlow);
			break;
		case MAP:
			addTsModel(((ParameterizedTypeInfo)type).getArg(1), dataFlow);
			break;
		case OBJECT:
			break;
		case OTHER:
			break;
		case PRIMITIVE:
			break;
		case SET:
			addTsModel(((ParameterizedTypeInfo)type).getArg(0), dataFlow);
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
		addJsonizableModel(elem, dataFlow);
	}

	public void addJsonizableModel(TypeElement elem, int dataFlow ) {
		String returnType = elem.getQualifiedName().toString();
		if(this.models.containsKey(returnType)) {
			return;
		}
		Jsonizable ann = elem != null ? elem.getAnnotation(Jsonizable.class) : null;
		if(ann != null) {
			JsonizableModel model = new JsonizableModel(elem);
			String externalModuleId = ann.tsExternalModule().trim();
			if (externalModuleId != null && externalModuleId.length() > 0) {			
				this.addExternalTsModel(model.getTypeInfo().getTypescriptName(), externalModuleId);
			} else {
				this.models.put(returnType, model);
				if(dataFlow == DataFlow_From_Server) {
					for(PropertyInfo p : model.getToProperties()) {
						addTsModel(p.getType(), DataFlow_From_Server);
					}
				}else {
					for(PropertyInfo p : model.getFromProperties()) {
						addTsModel(p.getType(), DataFlow_To_Server);
					}
				}
			}
		}
	}
	
	public void addExternalTsModel(String modelName, String moduleId) {
		Set<String> set = this.externalTsModels.computeIfAbsent(moduleId, id -> new HashSet<>());
		set.add(modelName);
	}
	
	public CommandClassModel[] getCommands() {
		return this.commands.values().toArray(new CommandClassModel[0]);
	}
	
	
	public JsonizableModel[] getModels() {
		return this.models.values().toArray(new JsonizableModel[0]);
	}
	
	public String[] getTsExternalImports() {
		if (this.externalTsModels.isEmpty()) {
			return new String[0];
		}
		return this.externalTsModels.entrySet().stream().
				map((entry) -> "import { "+Joiner.on(',').join(entry.getValue())+" } from '"+entry.getKey()+"'").
				collect(Collectors.toList()).toArray(new String[0]);
	}
	
	public String getTsImportClasses() {
		return this.models.values().stream().map((model) -> model.getTypeInfo().getTypescriptName()).filter(name -> !this.externalTsModels.containsKey(name)).reduce((result, elem) -> result+","+elem).orElse(null);
	}
}
