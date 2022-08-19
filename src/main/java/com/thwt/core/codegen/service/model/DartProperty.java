/**
 * 
 */
package com.thwt.core.codegen.service.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.PropertyKind;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.model.type.TypeParamInfo;
import com.thwt.core.codegen.service.util.ModelHelper;

/**
 * @author neillin
 *
 */
public class DartProperty {
	final private  String name, typeName;
	final private TypeInfo typeInfo;
	
	public DartProperty(String name, String typeName, TypeInfo typeInfo, PropertyKind kind) {
		this.name = name;
		this.typeName = typeName;
		switch (kind) {
		case LIST:
			typeInfo = new ParameterizedTypeInfo(ClassKind.LIST, List.class.getName(), Arrays.asList(new TypeParamInfo.Class(List.class.getName(), 0, "E")), Arrays.asList(typeInfo));
			break;
		case MAP:
			typeInfo = new ParameterizedTypeInfo(ClassKind.MAP, Map.class.getName(), 
					Arrays.asList(new TypeParamInfo.Class(Map.class.getName(), 0, "K"), new TypeParamInfo.Class(Map.class.getName(), 1 , "V")), 
					Arrays.asList(TypeMirrorFactory.getInstance().create(String.class.getName()), typeInfo));
			break;
		case SET:
			typeInfo = new ParameterizedTypeInfo(ClassKind.SET, Set.class.getName(), Arrays.asList(new TypeParamInfo.Class(Set.class.getName(), 0, "E")), Arrays.asList(typeInfo));
			break;
		default:
			break;
		
		}
		this.typeInfo = typeInfo;
	}

	public String getToJsonStatement() {
		return ModelHelper.generateToJsonDartStatement(this.typeInfo, name);
	}
	
	public String getFromJsonStatement() {
		return ModelHelper.generateFromJsonDartStatement(this.typeInfo, name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * @return the typeInfo
	 */
	public TypeInfo getTypeInfo() {
		return typeInfo;
	}
}
