/**
 * 
 */
package com.thwt.core.codegen;

import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.TypeModel;

/**
 * @author Neil Lin
 *
 */
public class TypeHelper {

	public String getJsonTypeName(String type){
		return TypeModel.getJsonTypeName(type);
	}
	
	public boolean isPrimitiveBoxedType(String type){
		return TypeModel.isPrimitiveBoxedType(type);
	}
	
	public String getUnboxedType(String type){
		return TypeModel.getUnboxedType(type);
	}
	
	public String getBoxedType(String type){
		return TypeModel.getBoxedType(type);
	}

	public String getMapKeyType(String type){
		return TypeModel.getMapKeyType(type);
	}

	
	public String getSimpleTypeName(String type, ClassModel model){
		return new TypeModel(type).toSimpleName(model);
	}
}