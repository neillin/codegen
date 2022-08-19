/*
 * @(#)CommandClassModel.java	2017-12-18
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;


import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.thwt.core.annotation.DartLang;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.codegen.model.type.PropertyKind;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.service.util.ModelHelper;
import com.thwt.core.codegen.util.ModelUtils;


/**
 * @author Neil Lin
 *
 */
public class CommandClassModel extends JsonizableModel {

	private String tsModuleName;
	private TypeInfo returnType;
	private String commandName;
	private List<String> dartInterfaces = new ArrayList<String>();
	/**
	 * @param elem
	 */
	public CommandClassModel(TypeElement elem) {
		super(elem);
	}
	
	/**
	 * 
	 */
	protected void processJsoniableAnnotation() {
//		CMD ann = getElement().getAnnotation(CMD.class);
		//    this.inheritConverter = ann.inheritConverter();
		this.category = "commands";
//		this.jsonName = Utils.isBlank(ann.jsonName()) ? getTypeInfo().getSimpleName(Case.LOWER_CAMEL) : ann.jsonName();
		this.generateConverter = true;
	}
	

	
	/**
	 * @return the tsModuleName
	 */
	public String getTsModuleName() {
		return tsModuleName;
	}

	/**
	 * @param tsModuleName the tsModuleName to set
	 */
	public void setTsModuleName(String tsModuleName) {
		this.tsModuleName = tsModuleName;
	}


	public String getCommandReturnValueType() {
		return ModelUtils.getCommandReturnValueType(getTypeInfo());
	}
	
	public TypeInfo getCommandReturnTypeInfo() {
		if(this.returnType == null) {
			this.returnType = TypeMirrorFactory.getInstance().create(ModelUtils.getCommandReturnValueTypeMirroe(getTypeInfo()));
		}
		return this.returnType;
	}

	/**
	 * @return the commandName
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * @param commandName the commandName to set
	 */
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	
	  public List<PropertyInfo> getTsClientProperties() {
	    return FluentIterable.from(this.getPropertyMap().values()).filter(new Predicate<PropertyInfo>() {
	
	      @Override
	      public boolean apply(PropertyInfo p) {
	        return (p.isSetter()||p.isAdder()) && ("commandId".equals(p.getName()) == false);
	      }
	    }).toList();
	  }
	  
	  public List<DartProperty> getDartClientProperties() {
	    return FluentIterable.from(this.getPropertyMap().values()).filter(new Predicate<PropertyInfo>() {
	
	      @Override
	      public boolean apply(PropertyInfo p) {
	        return (p.isSetter()||p.isAdder()) && ("commandId".equals(p.getName()) == false) && ("transactionId".equals(p.getName()) == false);
	      }
	    }).transform(p -> new DartProperty(p.getName(), p.getDartName(), p.getType(), p.getKind())).toList();
	  }

	  public List<DartProperty> getDartNullProperties() {
	    return FluentIterable.from(this.getPropertyMap().values()).filter(new Predicate<PropertyInfo>() {
	
	      @Override
	      public boolean apply(PropertyInfo p) {
	        return p.getKind() == PropertyKind.NULL;
	      }
	    }).transform(p -> new DartProperty(p.getName(), p.getDartName(), p.getType(), p.getKind())).toList();
	  }
	  
	  public String getReturnValueFromJsonStatement() {
		  return ModelHelper.generateFromJsonDartStatement(this.returnType, null);
	  }
	  

	  public void processDartInterfaces(CommandDartModel dartModel) {
		  TypeElement elem = getElement();
			if (elem == null) {
				return;
			}
			for (TypeMirror info : FluentIterable.from(elem.getInterfaces()).filter(new Predicate<TypeMirror>() {

				@Override
				public boolean apply(TypeMirror superTM) {
					return superTM instanceof DeclaredType
							&& ((DeclaredType) superTM).asElement().getAnnotation(DartLang.class) != null;
				}
			})) {
				DartLang ann = ((DeclaredType) info).asElement().getAnnotation(DartLang.class);
				TypeElement typeElem = (TypeElement)((DeclaredType) info).asElement();
				String dartName = Strings.isNullOrEmpty(ann.dartName()) ? typeElem.getSimpleName().toString() : ann.dartName().trim() ;
				if (! Strings.isNullOrEmpty(ann.externalPackage())) {
					dartModel.addExternalDartModel(dartName, ann.externalPackage().trim());
				} else {
					dartModel.addJsonizableModel(typeElem, CommandDartModel.DataFlow_To_Server, true);
				}
				if (!this.dartInterfaces.contains(dartName)) {
					this.dartInterfaces.add(dartName);
				}
				
			}
	  }
	  
		
		public String getDartImplements() {
			return this.dartInterfaces.isEmpty() ? "" : "implements "+Joiner.on(',').join(this.dartInterfaces);
		}
		
		
		public String getDartInterfaces() {
			return this.dartInterfaces.isEmpty() ? "" : ", "+Joiner.on(',').join(this.dartInterfaces);
		}
}
