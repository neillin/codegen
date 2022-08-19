/**
 * 
 */
package com.thwt.core.codegen.model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;



/**
 * @author neillin
 *
 */
public class TypeModel {
	
	public static enum JsonTypeName {
		BOOLEAN,
		INT,
		LONG,
		DOUBLE,
		STRING,
		DATE,
		ARRAY,
		LIST,
		MAP,
		OBJECT
	}

	private static Map<String, String> primitiveTypes = new HashMap<String, String>();
	static {
		primitiveTypes.put("int", Integer.class.getCanonicalName());
		primitiveTypes.put("byte", Byte.class.getCanonicalName());
		primitiveTypes.put("char", Character.class.getCanonicalName());
		primitiveTypes.put("long", Long.class.getCanonicalName());
		primitiveTypes.put("float", Float.class.getCanonicalName());
		primitiveTypes.put("double", Double.class.getCanonicalName());
		primitiveTypes.put("boolean", Boolean.class.getCanonicalName());
		primitiveTypes.put("short", Short.class.getCanonicalName());
	}
	
	private static Map<String, TypeKind> primitiveTypeKinds = new HashMap<String, TypeKind>();
	static {
		primitiveTypeKinds.put("int", TypeKind.INT);
		primitiveTypeKinds.put("byte", TypeKind.BYTE);
		primitiveTypeKinds.put("char", TypeKind.CHAR);
		primitiveTypeKinds.put("long", TypeKind.LONG);
		primitiveTypeKinds.put("float", TypeKind.FLOAT);
		primitiveTypeKinds.put("double", TypeKind.DOUBLE);
		primitiveTypeKinds.put("boolean", TypeKind.BOOLEAN);
		primitiveTypeKinds.put("short", TypeKind.SHORT);
	}
	
	public static boolean isPrimitiveType(String name){
		return primitiveTypeKinds.containsKey(name);
	}
	
	public static TypeKind getPrimitiveKind(String name){
		return primitiveTypeKinds.get(name);
	}

	
	public static boolean isPrimitiveBoxedType(String name){
		return primitiveTypes.values().contains(name);
	}
	
	public static TypeMirror getTypeMirror(TypeModel model) {
	  ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
	  if(ctx == null) {
	    throw new IllegalStateException("This method could be called under annotation processing environment!");
	  }
	  Elements elemUtils = ctx.getProcessingEnvironment().getElementUtils();
	  Types typeUtils = ctx.getProcessingEnvironment().getTypeUtils();
	  TypeMirror type;
	  TypeElement rawTypeElement;
	  String typeName = model.isArray() ? model.getCompnentType() : model.getType();
	  if(isPrimitiveType(typeName)) {
	    type = typeUtils.getPrimitiveType(getPrimitiveKind(typeName));
	  }else if("?".equals(typeName)) {
	    type = typeUtils.getDeclaredType(elemUtils.getTypeElement(Object.class.getName()));
	  }else {
	    rawTypeElement = elemUtils.getTypeElement(typeName);
	    if(rawTypeElement == null) {
	      throw new IllegalArgumentException("Cannot find type element for type :["+typeName+"]");
	    }
	    TypeModel[] tParams = model.getParameterTypes();
	    if(tParams != null && tParams.length > 0) {
	      type = typeUtils.getDeclaredType(rawTypeElement, FluentIterable.from(tParams).transform(new Function<TypeModel, TypeMirror>() {

	        @Override
	        public TypeMirror apply(TypeModel input) {
	          return getTypeMirror(input);
	        }
	      }).toArray(TypeMirror.class));
	    }else{
	      type = typeUtils.getDeclaredType(rawTypeElement);
	    }
	  }
	  if(model.isArray()) {
	    type = typeUtils.getArrayType(type);
	  }
	  return type;
	}
	
	
	public static boolean isAbstractClass(Element elem) {
		return elem.getKind() == ElementKind.CLASS && ((TypeElement)elem).getModifiers().contains(Modifier.ABSTRACT);
	}
	
	public static String getArrayMemberType(String name){
		TypeModel typeModel = new TypeModel(name);
		if(typeModel.isArray()){
			return typeModel.getCompnentType();
		}
		throw new IllegalArgumentException("type :["+name+"] is not a array type !");
	}
	
	public static String getListMemberType(String name){
		TypeModel model = new TypeModel(name);
		TypeModel[] params = model.getParameterTypes();
		return params[0].getType();
	}

	public static String getMapValueType(String name){
		TypeModel model = new TypeModel(name);
		TypeModel[] params = model.getParameterTypes();
		return params[1].getType();
	}
	
	public static String getMapKeyType(String name){
		TypeModel model = new TypeModel(name);
		TypeModel[] params = model.getParameterTypes();
		return params[0].getType();
	}
	
	public static String getUnboxedType(String name){
		if(!isPrimitiveBoxedType(name)){
			return name;
		}
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		TypeElement elem = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(name);
		TypeMirror type = elem.asType();
		type = ctx.getProcessingEnvironment().getTypeUtils().unboxedType(type);
		return type.toString();
	}
	
	public static String getBoxedType(String name){
		if(!isPrimitiveType(name)){
			return name;
		}
		return primitiveTypes.get(name);
	}



	public static String  getJsonTypeName(String name) {
		return getJsonType(name).name();
	}
	
	public static JsonTypeName getJsonType(String name) {
		TypeKind kind = getPrimitiveKind(name);
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		TypeMirror type = null;
		if(kind == null){
			TypeModel typeModel = new TypeModel(name);
			if(typeModel.isArray()){
				return JsonTypeName.ARRAY; 
			}
			TypeElement elem = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(typeModel.getType());
			type = elem.asType();
			if(isPrimitiveBoxedType(name)){
				type = ctx.getProcessingEnvironment().getTypeUtils().unboxedType(type);
			}
			kind = type.getKind();
		}
		switch (kind) {
		case BOOLEAN:	
			return JsonTypeName.BOOLEAN;
		case BYTE:			
		case CHAR:			
		case SHORT:			
		case INT:			
			return JsonTypeName.INT;
		case FLOAT:			
		case DOUBLE:			
			return JsonTypeName.DOUBLE;
		case LONG:			
			return JsonTypeName.LONG;
		case ARRAY:			
			return JsonTypeName.ARRAY;
		case DECLARED:
			TypeMirror listType = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(List.class.getCanonicalName()).asType();
			TypeMirror mapType = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(Map.class.getCanonicalName()).asType();
			TypeMirror dateType = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(Date.class.getCanonicalName()).asType();
			TypeMirror stringType = ctx.getProcessingEnvironment().getElementUtils().getTypeElement(String.class.getCanonicalName()).asType();
			Types typeUtil = ctx.getProcessingEnvironment().getTypeUtils();
			if(typeUtil.isSameType(stringType, type)){
				return JsonTypeName.STRING;
			}
			if(typeUtil.isSameType(type, dateType)){
				return JsonTypeName.DATE;
			}
			if(typeUtil.isAssignable(type, listType)){
				return JsonTypeName.LIST;
			}
			if(typeUtil.isAssignable(type, mapType)){
				return JsonTypeName.MAP;
			}
		default:
			return JsonTypeName.OBJECT;
		}
	}
	
	
	private String type;
	private TypeModel[] parameterTypes;
	private boolean array;
	
	
	public TypeModel(String type, TypeModel[] parameterTypes) {
		super();
		this.type = type;
		this.parameterTypes = parameterTypes;
	}

	public TypeModel(String typeString){
		if(typeString.endsWith("[]")){
			this.array = true;
			typeString = typeString.substring(0,typeString.length()-2);
		}
		if(typeString.endsWith("[ ]")){
			this.array = true;
			typeString = typeString.substring(0,typeString.length()-3);
		}
		int idx = typeString.indexOf('<');
		if(idx > 0){
			this.type = typeString.substring(0,idx);
			String[] pTypes = parseParamterTypes(typeString.substring(idx));
			if((pTypes != null)&&(pTypes.length > 0)){
				parameterTypes = new TypeModel[pTypes.length];
				for (int i=0 ; i < pTypes.length ; i++) {
					parameterTypes[i] = new TypeModel(pTypes[i]);
				}
			}
		}else{
			this.type = typeString;
		}
	}
	
	String[] parseParamterTypes(String s){
		s = StringUtils.trimToNull(s);
		s = s.substring(1,s.length()-1);	// remove < and >
		LinkedList<String> result = null;
		int len = s.length();
		int pos = 0;
		int depth = 0;
		for(int i=0 ; i < len ; i++){
			char ch = s.charAt(i);
			switch(ch){
			case '<':
				depth++;
				break;
			case '>':
				depth--;
				break;
			case ',':
				if(depth == 0){
					String val = StringUtils.trimToNull(s.substring(pos,i));
					if(val != null){
						if(result == null){
							result = new LinkedList<String>();
						}
						result.add(val);
					}
					pos = i+1;
				}
				break;
			}
		}
		String val = StringUtils.trimToNull(s.substring(pos,s.length()));
		if(val != null){
			if(result == null){
				result = new LinkedList<String>();
			}
			result.add(val);
		}
		return result != null ? result.toArray(new String[0]) : null;		
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return this.array ? type+"[]" : type;
	}
	/**
	 * @return the parameterTypes
	 */
	public TypeModel[] getParameterTypes() {
		return parameterTypes;
	}
	
	public String getSimpleTypeName(ClassModel model) {
		return this.array ? model.importClass(this.type)+"[]" : model.importClass(this.type);
	}
	
	public String toSimpleName(ClassModel model){
		StringBuffer buf = new StringBuffer();
		buf.append(model.importClass(this.type));
		if((this.parameterTypes != null)&&(this.parameterTypes.length > 0)){
			buf.append('<');
			int cnt = 0 ;
			for (int i = 0; i < this.parameterTypes.length; i++) {
				TypeModel p = this.parameterTypes[i];
				if(cnt > 0){
					buf.append(',');
				}
				buf.append(p.toSimpleName(model));
				cnt++;
			}
			buf.append('>');
		}
		if(this.array){
			buf.append("[]");
		}

		return buf.toString();
	}
	
	public TypeMirror toTypeMirror(ICodeGenerationContext ctx){
		Elements elemUtil = ctx.getProcessingEnvironment().getElementUtils();
		Types typeUtil = ctx.getProcessingEnvironment().getTypeUtils();
		TypeMirror[] pTypes = null;
		if((this.parameterTypes != null)&&(this.parameterTypes.length > 0)){
			pTypes = new TypeMirror[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				TypeModel tModel = this.parameterTypes[i];
				pTypes[i] = tModel.toTypeMirror(ctx);
			}
		}
		TypeMirror resultType = null;
		if(pTypes != null){
			resultType = typeUtil.getDeclaredType(elemUtil.getTypeElement(getType()), pTypes);
		}else{
			String stype = getType();
			if(primitiveTypeKinds.containsKey(stype)){
				resultType = typeUtil.getPrimitiveType(primitiveTypeKinds.get(stype));
			}else{
				resultType = elemUtil.getTypeElement(stype).asType();
			}
		}
		if(isArray()){
			resultType = typeUtil.getArrayType(resultType);
		}
		return resultType;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the array
	 */
	public boolean isArray() {
		return array;
	}

	/**
	 * @param array the array to set
	 */
	public void setArray(boolean array) {
		this.array = array;
	}
	
	public String getCompnentType() {
		return this.array ? this.type : null;
	}
}
