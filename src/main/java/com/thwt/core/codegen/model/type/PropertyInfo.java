package com.thwt.core.codegen.model.type;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thwt.core.codegen.model.ImportManager;

/**
 * Describes a property of a {@link io.vertx.codegen.DataObjectModel data object model}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PropertyInfo {

	final PropertyKind kind;
	final boolean declared;
	final String name;
	final Doc doc;
	final TypeInfo type;
	final String setterMethod;
	final String adderMethod;
	final String getterMethod;
	final boolean jsonifiable;
	private String tsName;
	private String mappingName;

	public PropertyInfo(boolean declared, String name, Doc doc, TypeInfo type, String setterMethod, String adderMethod, String getterMethod,
			PropertyKind kind, boolean jsonifiable) {
		this.kind = kind;
		this.declared = declared;
		this.name = name;
		this.doc = doc;
		this.type = type;
		this.adderMethod = adderMethod;
		this.setterMethod = setterMethod;
		this.getterMethod = getterMethod;
		this.jsonifiable = jsonifiable;
		this.tsName = this.name;
	}

	/**
	 * @return true if the property is declared by the its data object, that means it does not override the same property
	 *   from other data object ancestors
	 */
	public boolean isDeclared() {
		return declared;
	}

	/**
	 * @return the resolved documentation of this property
	 */
	public Doc getDoc() {
		return doc;
	}

	/**
	 * @return the property kind
	 */
	public PropertyKind getKind() {
		return kind;
	}

	/**
	 * @return the property name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the property type
	 */
	public TypeInfo getType() {
		return type;
	}

	public TypeInfo getBoxedType() {
		if(isPrimitive()) {
			return ((PrimitiveTypeInfo)getType()).getBoxed();
		}else{
			return this.getType();
		}
	}

	/**
	 * @return the name of the Java method that can read the state of this property on the data object.
	 */
	public String getGetterMethod() {
		return getterMethod;
	}

	public String getGetter() {
		return this.getterMethod != null ? this.getterMethod : (isBooleanValue() ? "is" : "get")+Helper.capitaliseFirstLetter(this.name);
	}

	/**
	 * @return the name of the Java method that will update the state of this property on the data object, the nature of the method
	 * depends on the {@link #isAdder()} and {@link #isList()} values.
	 */
	public String getSetterMethod() {
		return setterMethod;
	}

	public String getSetter() {
		return this.setterMethod != null ? this.setterMethod : "set"+Helper.capitaliseFirstLetter(this.name);
	}

	public String getAdderMethod() {
		return adderMethod;
	}

	public boolean isBooleanValue() {
		return "Boolean".equals(getBoxedType().getName());
	}
	/**
	 * @return true if the property is managed as a single value
	 */
	public boolean isValue() {
		return kind == PropertyKind.VALUE;
	}

	public boolean isEnum() {
		return getType().getKind() == ClassKind.ENUM;
	}

	/**
	 * @return true if the property is managed by a {@code java.util.List<T>}
	 */
	public boolean isList() {
		return kind == PropertyKind.LIST;
	}

	/**
	 * @return true if the property is managed by a {@code java.util.Set<T>}
	 */
	public boolean isSet() {
		return kind == PropertyKind.SET;
	}

	/**
	 * @return true if the property is managed by a {@code java.util.Map<String, T>}
	 */
	public boolean isMap() {
		return kind == PropertyKind.MAP;
	}

	public boolean isPrimitive() {
		return getType().getKind() == ClassKind.PRIMITIVE;
	}


	public boolean isArray() {
		return getType().getKind() == ClassKind.ARRAY;
	}

	public boolean isByteArray() {
		return getType().getKind() == ClassKind.ARRAY && "byte".equals(((ArrayType)getType()).getComponentType().getName());
	}

	public boolean isPrimitiveArray() {
		return getType().getKind() == ClassKind.ARRAY && ((ArrayType)getType()).getComponentType().getKind() == ClassKind.PRIMITIVE;
	}

	public boolean isBoxedPrimitiveArray() {
		return getType().getKind() == ClassKind.ARRAY && ((ArrayType)getType()).getComponentType().getKind() == ClassKind.BOXED_PRIMITIVE;
	}

	public boolean isDate() {
		return getType().getKind() == ClassKind.DATE_TYPE;
	}

	/**
	 * @return true if the property has a setter method
	 */
	public boolean isSetter() {
		return setterMethod != null;
	}

	/**
	 * @return true if the property has an adder method
	 */
	public boolean isAdder() {
		return adderMethod != null;
	}

	/**
	 * @return true if the property type can be converted to a Json type
	 */
	public boolean isJsonifiable() {
		return jsonifiable;
	}

	public void collectImports(ImportManager mgr) {
		if(isList()) {
			mgr.importClass(List.class.getName());
		}else if(isMap()) {
			mgr.importClass(Map.class.getName());
		}else if(isSet()) {
			mgr.importClass(Set.class.getName());
		}
		getType().collectImports(mgr);
	}

	/**
	 * @return the mappingName
	 */
	public String getMappingName() {
		return this.mappingName != null ? this.mappingName: this.name;
	}

	/**
	 * @param mappingName the mappingName to set
	 */
	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}
	
	public String getTypescriptName() {
		switch (this.kind) {
		case SET:
		case LIST:
			return "Array<"+this.type.getTypescriptName()+">";
		case MAP:
			return "{ [key: string]: "+this.type.getTypescriptName()+" }";
		default:
			return this.type.getTypescriptName();
		}
	}

	public String getDartName() {
		switch (this.kind) {
		case SET:
			return "Set<"+this.type.getDartName()+">";
		case LIST:
			return "List<"+this.type.getDartName()+">";
		case MAP:
			return "Map<String, "+this.type.getDartName()+">";
		default:
			return this.type.getDartName();
		}
	}

	/**
	 * @return the tsName
	 */
	public String getTsName() {
		return tsName;
	}

	/**
	 * @param tsName the tsName to set
	 */
	public void setTsName(String tsName) {
		this.tsName = tsName;
	}
}
