/*
 * @(#)ModelHelper.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.util;

import static com.thwt.core.codegen.util.FileUtils.createJavaFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.model.type.ArrayType;
import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.service.model.CommandClassModel;
import com.thwt.core.codegen.service.model.JsonizableConverterModel;
import com.thwt.core.util.Utils;

/**
 * @author Neil Lin
 *
 */
public abstract class ModelHelper {

	private static final ModelHelper instance = new ModelHelper() {
	};

	public static final ModelHelper getInstance() {
		return instance;
	}

	public String joinClasses(List<String> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}
		return Joiner.on(',').join(FluentIterable.from(list).transform(new Function<String, String>() {

			@Override
			public String apply(String clazz) {
				return clazz + ".class";
			}
		}).toList());
	}

	public String capitalize(String s) {
		return Utils.capitalize(s);
	}

	public String uncapitalize(String s) {
		return Utils.uncapitalize(s);
	}

	public String getPrimitiveArrayFromJsonMethod(ArrayType arrayType) {
		TypeInfo type = arrayType.getComponentType();
		if ("int".equals(type.getName())) {
			return "intFromJsonArray";
		} else if ("boolean".equals(type.getName())) {
			return "booleanFromJsonArray";
		} else if ("char".equals(type.getName())) {
			return "charFromJsonArray";
		} else if ("short".equals(type.getName())) {
			return "shortFromJsonArray";
		} else if ("long".equals(type.getName())) {
			return "longFromJsonArray";
		} else if ("float".equals(type.getName())) {
			return "floatFromJsonArray";
		} else if ("double".equals(type.getName())) {
			return "doubleFromJsonArray";
		}
		return null;

	}

	public String getPrimitiveArrayToJsonMethod(ArrayType arrayType) {
		TypeInfo type = arrayType.getComponentType();
		if ("int".equals(type.getName())) {
			return "intToJsonArray";
		} else if ("boolean".equals(type.getName())) {
			return "booleanToJsonArray";
		} else if ("char".equals(type.getName())) {
			return "charToJsonArray";
		} else if ("short".equals(type.getName())) {
			return "shortToJsonArray";
		} else if ("long".equals(type.getName())) {
			return "longToJsonArray";
		} else if ("float".equals(type.getName())) {
			return "floatToJsonArray";
		} else if ("double".equals(type.getName())) {
			return "doubleToJsonArray";
		}
		return null;
	}

	public static void generateJsonConverterJavaFile(final ICodeGenerationContext context,
			JsonizableConverterModel model, String template) throws Exception {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("model", model);
		attributes.put("helper", ModelHelper.getInstance());
		String text = context.getTemplateRenderer().renderFromFile(template, attributes);
		createJavaFile(context, model.getPkgName(), model.getName(), text, model.getFileLocation());
	}

	
	public static String generateFromJsonDartStatement(TypeInfo returnTypeInfo, String propertyName) {
		String jsonObjectName = propertyName != null ? "json['"+propertyName+"']" : "json";
		if (returnTypeInfo == null) {
			return "null";
		}
		switch (returnTypeInfo.getKind()) {
		case API:
			break;
		case ARRAY:
			ArrayType arrayType = (ArrayType)returnTypeInfo;
			if (arrayType.getComponentType().getKind() != ClassKind.DATA_OBJECT) {
				return jsonObjectName+" != null ? (" + jsonObjectName + " as List).map((val) => val as "+arrayType.getComponentType().getDartName()+").toList(growable: false) : <"+arrayType.getComponentType().getDartName()+">[]";
			} else {
				return jsonObjectName+" != null ? (" + jsonObjectName + " as List).map((val) => "+arrayType.getComponentType().getDartName()+"().fromJson(val)).toList(growable: false) : <"+arrayType.getComponentType().getDartName()+">[]";
			}
		case ASYNC_RESULT:
			break;
		case BOXED_PRIMITIVE:
			return jsonObjectName;
		case CLASS_TYPE:
			break;
		case DATA_OBJECT:
			return jsonObjectName+" != null ? "+returnTypeInfo.getDartName()+"().fromJson("+jsonObjectName+") : null";
		case DATE_TYPE:
			return jsonObjectName+" != null ? DateTime.fromMillisecondsSinceEpoch("+jsonObjectName+") : null";
		case ENUM:
			return jsonObjectName;
		case FUNCTION:
			break;
		case HANDLER:
			break;
		case JSON_ARRAY:
		case JSON_OBJECT:
			return jsonObjectName;
		case LIST: {
			ParameterizedTypeInfo listType = (ParameterizedTypeInfo)returnTypeInfo;
			if (listType.getArg(0).getKind() != ClassKind.DATA_OBJECT) {
				return jsonObjectName+" != null ? (" + jsonObjectName + " as List).map((val) => val as "+listType.getArg(0).getDartName()+").toList(growable: false) : <"+listType.getArg(0).getDartName()+">[]";
			} else {
				return jsonObjectName+" != null ? (" + jsonObjectName + " as List).map((val) => "+listType.getArg(0).getDartName()+"().fromJson(val)).toList(growable: false) : <"+listType.getArg(0).getDartName()+">[]";
			}
		}
		case MAP:
			return jsonObjectName;
		case OBJECT:
			return jsonObjectName+" != null ? "+returnTypeInfo.getDartName()+"().fromJson("+jsonObjectName+") : null";
		case OTHER:
			break;
		case PRIMITIVE:
			return jsonObjectName;
		case SET: {
			ParameterizedTypeInfo setType = (ParameterizedTypeInfo)returnTypeInfo;
			if (setType.getArg(0).getKind() != ClassKind.DATA_OBJECT) {
				return jsonObjectName+" != null ? Set.from((" + jsonObjectName + " as List).map((val) => val as "+setType.getArg(0).getDartName()+")) : <"+setType.getArg(0).getDartName()+">{}";
			} else {
				return jsonObjectName+" != null ? Set.from((" + jsonObjectName + " as List).map((val) => "+setType.getArg(0).getDartName()+"().fromJson(val))) : <"+setType.getArg(0).getDartName()+">{}";
			}
		}
		case STRING:
			return jsonObjectName;
		case THROWABLE:
			break;
		case VOID:
			return "null";
		default:
			break;
		}
		return "null";
	}
	
	public static String generateToJsonDartStatement(TypeInfo valueTypeInfo, String property) {
		if (valueTypeInfo == null) {
			return property;
		}
		switch (valueTypeInfo.getKind()) {
		case API:
			break;
		case ARRAY:
			ArrayType arrayType = (ArrayType)valueTypeInfo;
			if (arrayType.getComponentType().getKind() == ClassKind.DATA_OBJECT) {
				return property+" != null ? "+property+".map(( "+ arrayType.getComponentType().getDartName()+" val) => val?.toJson()).toList(growable: false) : null";
			} else {
				return property;
			}
		case ASYNC_RESULT:
			break;
		case BOXED_PRIMITIVE:
			return property;
		case CLASS_TYPE:
			break;
		case DATA_OBJECT:
			return property+"?.toJson()";
		case DATE_TYPE:
			return property+"?.millisecondsSinceEpoch";
		case ENUM:
			return property;
		case FUNCTION:
			break;
		case HANDLER:
			break;
		case JSON_ARRAY:
		case JSON_OBJECT:
			return property;
		case SET:
		case LIST:
			ParameterizedTypeInfo listType = (ParameterizedTypeInfo)valueTypeInfo;
			if (listType.getArg(0).getKind() != ClassKind.DATA_OBJECT) {
				return property;
			} else {
				return property+" != null ? " + property + ".map(( "+listType.getArg(0).getDartName()+" val) => val?.toJson()).toList(growable: false) : null";
			}
		case MAP:
			return property;
		case OBJECT:
			return property+"?.toJson()";
		case OTHER:
			break;
		case PRIMITIVE:
			return property;
		case STRING:
			return property;
		case THROWABLE:
			break;
		case VOID:
			return "null";
		default:
			break;
		}
		return "null";
	}

}
