/*
 * @(#)JavaBeanModel.java	2017-12-10
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import static com.thwt.core.codegen.model.type.Helper.getElementUtils;
import static com.thwt.core.codegen.model.type.Helper.getTypeMirrorFactory;
import static com.thwt.core.codegen.model.type.Helper.getTypeUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Entity;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.thwt.core.annotation.DartLang;
import com.thwt.core.annotation.JsonMapping;
import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.GenException;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.model.JavaBeanInfo;
import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.Doc;
import com.thwt.core.codegen.model.type.Helper;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.PropertyInfo;
import com.thwt.core.codegen.model.type.PropertyKind;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeParamInfo.Class;

/**
 * @author Neil Lin
 *
 */
public class JavaBeanModel extends ClassModelImpl implements JavaBeanInfo {

	protected int constructors;
	protected Map<String, PropertyInfo> propertyMap;
	protected Doc doc;
	private List<String> dartInterfaces = new ArrayList<String>();

	/**
	 * @param qualifiedName
	 */
	public JavaBeanModel(String qualifiedName) {
		super(qualifiedName);
	}

	/**
	 * @param qualifiedName
	 * @param params
	 */
	public JavaBeanModel(String qualifiedName, List<Class> params) {
		super(qualifiedName, params);
	}

	/**
	 * @param elem
	 */
	public JavaBeanModel(TypeElement elem) {
		super(elem);
	}

	public Doc getDoc() {
	    return doc;
	  }

	public Map<String, PropertyInfo> getPropertyMap() {
	    return propertyMap;
	  }

	public boolean hasEmptyConstructor() {
	    return (constructors & 1) == 1;
	  }

	@Override
	protected void doProcess() {
	    doc = getDocFactory().createDoc(getElement());
	    propertyMap = new LinkedHashMap<>();
	
	    List<ExecutableElement> methodsElt = new ArrayList<>();
//	    TypeElement elem = getElement();
//	    while(elem != null) {
	    		collectionBeanMethods(methodsElt, getElement());
//	    		elem = getSuperClassElement();
//	    }
	    
	    processMethods(methodsElt);
	
	    // Sort the properties so we do have a consistent order
	    ArrayList<PropertyInfo> props = new ArrayList<>(propertyMap.values());
	    Collections.sort(props, new Comparator<PropertyInfo>() {
	
	      @Override
	      public int compare(PropertyInfo p1, PropertyInfo p2) {
	        return p1.getName().compareTo(p2.getName());
	      }
	    });
	    propertyMap.clear();
	    for (PropertyInfo prop : props) {
	      addProperty(prop);
	      prop.getType().collectImports(getImportManager());
	    }
	  }

	/**
	 * @param methodsElt
	 */
	private void collectionBeanMethods(List<ExecutableElement> methodsElt, TypeElement elem) {
		for (Element enclosedElt : getElementUtils().getAllMembers(elem)) {
	      switch (enclosedElt.getKind()) {
	        case CONSTRUCTOR:
	          ExecutableElement constrElt = (ExecutableElement) enclosedElt;
	          processConstructor(constrElt);
	          break;
	        case METHOD: {
	          ExecutableElement methodElt = (ExecutableElement) enclosedElt;
	          methodsElt.add(methodElt);
	          break;
	        }
	        default:
	          break;
	      }
	    }
	}


	private void processConstructor(ExecutableElement constrElt) {
	    if (constrElt.getModifiers().contains(Modifier.PUBLIC)) {
	      Element ownerElt = constrElt.getEnclosingElement();
	      if (ownerElt.equals(getElement())) {
	        List<? extends VariableElement> parameters = constrElt.getParameters();
	        int size = parameters.size();
	        if (size == 1) {
	          TypeInfo ti = getTypeMirrorFactory().create(parameters.get(0).asType());
	          if (ti instanceof ClassTypeInfo) {
	            ClassTypeInfo cl = (ClassTypeInfo) ti;
	            if (cl.getKind() == ClassKind.JSON_OBJECT) {
	              constructors |= 2;
	            }
	          }
	        } else if (size == 0) {
	          constructors |= 1;
	        }
	      }
	    }
	  }

	protected List<ExecutableElement> filterNoPropertyMethods(List<ExecutableElement> methodsElt) {
		  return methodsElt.stream().filter(m -> {
			  if(m.getModifiers().contains(Modifier.STATIC)) {
		    	  	return false;
		      }
		      if (((TypeElement)m.getEnclosingElement()).getQualifiedName().toString().equals("java.lang.Object")) {
		        return false;
		      }
		      return true;
		  }).collect(Collectors.toList());
	  }

	protected void processMethods(List<ExecutableElement> methodsElt) {
	
	    Map<String, ExecutableElement> getters = new HashMap<>();
	    Map<String, ExecutableElement> setters = new HashMap<>();
	    Map<String, ExecutableElement> adders = new HashMap<>();
	
	    methodsElt = filterNoPropertyMethods(methodsElt);
	    while (methodsElt.size() > 0) {
	      ExecutableElement methodElt = methodsElt.remove(0);
	      String methodName = methodElt.getSimpleName().toString();
	      if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3)) && methodElt.getParameters().isEmpty() && methodElt.getReturnType().getKind() != TypeKind.VOID) {
	        String name = Helper.normalizePropertyName(methodName.substring(3));
	        getters.put(name, methodElt);
	      } else if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2)) && methodElt.getParameters().isEmpty() && methodElt.getReturnType().getKind() != TypeKind.VOID) {
	        String name = Helper.normalizePropertyName(methodName.substring(2));
	        getters.put(name, methodElt);
	      } else if ((methodName.startsWith("set") || methodName.startsWith("add")) && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
	        String prefix = methodName.substring(0, 3);
	        String name = Helper.normalizePropertyName(methodName.substring(3));
	        int numParams = methodElt.getParameters().size();
	        if ("add".equals(prefix)) {
	          if (name.endsWith("s")) {
	            throw new GenException(methodElt, "Option adder name must not terminate with 's' char");
	          } else {
	            name += "s";
	          }
	          TypeMirror t= methodElt.getParameters().get(0).asType();
	          if (numParams == 1 || (numParams == 2 && t.getKind() == TypeKind.DECLARED &&
	              ((TypeElement)((DeclaredType)t).asElement()).getQualifiedName().toString().equals("java.lang.String"))) {
	            adders.put(name, methodElt);
	          }
	        } else {
	          if (numParams == 1) {
	            setters.put(name, methodElt);
	          }
	        }
	      }
	    }
	
	    Set<String> names = new HashSet<>();
	    names.addAll(getters.keySet());
	    names.addAll(setters.keySet());
	    names.addAll(adders.keySet());
	
	    for (String name : names) {
	      processMethod(name, getters.get(name), setters.get(name), adders.get(name));
	    }
	  }

	@SuppressWarnings("incomplete-switch")
	protected void processMethod(String name, ExecutableElement getterElt, ExecutableElement setterElt, ExecutableElement adderElt) {
	
	    PropertyKind propKind = null;
	    TypeInfo propType = null;
	    TypeInfo valueType = null;
	    TypeMirror propTypeMirror = null;
	
	    //
	    if (setterElt != null) {
	      VariableElement paramElt = setterElt.getParameters().get(0);
	      propTypeMirror = paramElt.asType();
	      propType = getTypeMirrorFactory().create(propTypeMirror);
	      propKind = PropertyKind.forType(propType.getKind());
	      valueType = propType;
	      switch (propKind) {
	        case LIST:
	        case SET:
	          propType = ((ParameterizedTypeInfo) propType).getArgs().get(0);
	          propTypeMirror = ((DeclaredType)propTypeMirror).getTypeArguments().get(0);
	          break;
	        case MAP:
	          propType = ((ParameterizedTypeInfo) propType).getArgs().get(1);
	          propTypeMirror = ((DeclaredType)propTypeMirror).getTypeArguments().get(1);
	          break;
	      }
	    }
	
	    //
	    if (getterElt != null) {
	      TypeMirror getterTypeMirror = getterElt.getReturnType();
	      TypeInfo getterType = getTypeMirrorFactory().create(getterTypeMirror);
	      PropertyKind getterKind = PropertyKind.forType(getterType.getKind());
	      switch (getterKind) {
	        case LIST:
	        case SET:
	          getterType = ((ParameterizedTypeInfo) getterType).getArgs().get(0);
	          getterTypeMirror = ((DeclaredType)getterTypeMirror).getTypeArguments().get(0);
	          break;
	        case MAP:
	          getterType = ((ParameterizedTypeInfo) getterType).getArgs().get(1);
	          getterTypeMirror = ((DeclaredType)getterTypeMirror).getTypeArguments().get(1);
	          break;
	      }
	      if (propType != null) {
	        if (propKind != getterKind) {
	          throw new GenException(getterElt, name + " getter " + getterKind + " does not match the setter " + propKind);
	        }
	        if (!getterType.equals(propType)) {
	          throw new GenException(getterElt, name + " getter type " + getterType + " does not match the setter type " + propType);
	        }
	      } else {
	        propTypeMirror = getterTypeMirror;
	        propType = getterType;
	        propKind = getterKind;
	      }
	    }
	
	    //
	    if (adderElt != null) {
	      switch (adderElt.getParameters().size()) {
	        case 1: {
	          VariableElement paramElt = adderElt.getParameters().get(0);
	          TypeMirror adderTypeMirror = paramElt.asType();
	          TypeInfo adderType = getTypeMirrorFactory().create(adderTypeMirror);
	          if (propTypeMirror != null) {
	            if (propKind != PropertyKind.LIST && propKind != PropertyKind.SET) {
	              throw new GenException(adderElt, name + "adder does not correspond to non list/set");
	            }
	            if (!adderType.equals(propType)) {
	              throw new GenException(adderElt, name + " adder type " + adderType + "  does not match the property type " + propType);
	            }
	          } else {
	            propTypeMirror = adderTypeMirror;
	            propType = adderType;
	            propKind = PropertyKind.LIST;
	          }
	          break;
	        }
	        case 2: {
	          VariableElement paramElt = adderElt.getParameters().get(1);
	          TypeMirror adderTypeMirror = paramElt.asType();
	          TypeInfo adderType = getTypeMirrorFactory().create(adderTypeMirror);
	          if (propTypeMirror != null) {
	            if (propKind != PropertyKind.MAP) {
	              throw new GenException(adderElt, name + "adder does not correspond to non map");
	            }
	            if (!adderType.equals(propType)) {
	              throw new GenException(adderElt, name + " adder type " + adderType + "  does not match the property type " + propType);
	            }
	          } else {
	            propTypeMirror = adderTypeMirror;
	            propType = adderType;
	            propKind = PropertyKind.MAP;
	          }
	          break;
	        }
	      }
	    }
	
	    //
	    boolean jsonifiable;
	    switch (propType.getKind()) {
	      case OBJECT:
	        if (propKind == PropertyKind.VALUE) {
	          return;
	        }
	      case PRIMITIVE:
	      case BOXED_PRIMITIVE:
	      case STRING:
	      case API:
	      case JSON_OBJECT:
	      case JSON_ARRAY:
	      case ENUM:
	      case ARRAY:
	      case DATE_TYPE:
	        jsonifiable = false;
	        break;
	      case DATA_OBJECT: 
	        Element propTypeElt = getTypeUtils().asElement(propTypeMirror);
	        jsonifiable = propTypeElt.getAnnotation(Jsonizable.class) != null || propTypeElt.getAnnotation(Entity.class) != null ||
	            Helper.isJsonifiable(getElementUtils(), getTypeUtils(), (TypeElement)propTypeElt);
	        break;
	      default: {
		        JsonMapping mapAnn = getPropertyAnnotation(getterElt, setterElt, JsonMapping.class);
		        jsonifiable = false;
		        if(mapAnn == null) {
		        		return;
		        }
		        break;
	        }	
	    }
	
	    boolean declared = false;
	    Doc doc = null;
	    for (final ExecutableElement methodElt : Arrays.asList(setterElt, adderElt, getterElt)) {
	      if (methodElt != null) {
	
	        // A stream that list all overriden methods from super types
	        // the boolean control whether or not we want to filter only annotated
	        // data objects
	        Function<Boolean, Iterable<ExecutableElement>> overridenMeths = new Function<Boolean, Iterable<ExecutableElement>>() {
	          
	          @Override
	          public Iterable<ExecutableElement> apply(final Boolean annotated) {
	            final Set<DeclaredType> ancestorTypes = Helper.resolveAncestorTypes(getElement(), true, true);
	            return FluentIterable.from(ancestorTypes).
	                transform(new Function<DeclaredType, Element>() {
	
	                  @Override
	                  public Element apply(DeclaredType input) {
	                    return input.asElement();
	                  }
	                }).
	                filter(new Predicate<Element>() {
	
	                  @Override
	                  public boolean apply(Element elt) {
	                    return !annotated || elt.getAnnotation(Jsonizable.class) != null;
	                  }
	                }).
	                transformAndConcat(Helper.cast(TypeElement.class)).
	                transformAndConcat(new Function<TypeElement, Iterable<Element>>() {
	                  
	                  @Override
	                  public Iterable<Element> apply(TypeElement elt) {
	                    return (Iterable<Element>)getElementUtils().getAllMembers(elt);
	                  }
	                }).
	                transformAndConcat(Helper.instanceOf(ExecutableElement.class)).
	                filter(new Predicate<ExecutableElement>() {
	
	                  @Override
	                  public boolean apply(ExecutableElement executableElt) {
	                    return executableElt.getKind() == ElementKind.METHOD && getElementUtils().overrides(methodElt, executableElt, getElement());
	                  }
	                });
	          }
	        };
	
	        //
	        if (doc == null) {
	          doc = getDocFactory().createDoc(methodElt);
	          if (doc == null) {
	            Optional<Doc> first = FluentIterable.from(overridenMeths.apply(false)).
	                transform(new Function<ExecutableElement, Doc>() {
	
	                  @Override
	                  public Doc apply(ExecutableElement elt) {
	                    return getDocFactory().createDoc(elt);
	                  }
	                }).
	                filter(new Predicate<Doc>() {
	
	                  @Override
	                  public boolean apply(Doc d) {
	                    return  d != null;
	                  }
	                }).
	                first();
	            doc = first.orNull();
	          }
	        }
	
	        //
	        if (!declared) {
	          Element ownerElt = methodElt.getEnclosingElement();
	          if (ownerElt.equals(getElement())) {
	            Object[] arr = FluentIterable.from(overridenMeths.apply(true)).limit(1).filter(new Predicate<ExecutableElement>() {
	
	              @Override
	              public boolean apply(ExecutableElement elt) {
	                return !elt.getModifiers().contains(Modifier.ABSTRACT);
	              }
	            }).toArray(ExecutableElement.class);
	            // Handle the case where this methods overrides from another data object
	            declared = arr.length == 0;
	          } else {
	            declared = ownerElt.getAnnotation(Jsonizable.class) == null;
	          }
	        }
	      }
	    }
	
	    PropertyInfo property = new PropertyInfo(declared, name, doc, propType,
	        setterElt != null ? setterElt.getSimpleName().toString() : null,
	        adderElt != null ? adderElt.getSimpleName().toString() : null,
	        getterElt != null ? getterElt.getSimpleName().toString() : null,
	        propKind, jsonifiable);
	    addProperty(property);
	  }
	
	

	/**
	 * @param property
	 */
	public void addProperty(PropertyInfo property) {
		propertyMap.put(property.getName(), property);
	}
	
	
	public PropertyInfo getProperty(String name) {
		return this.propertyMap.get(name);
	}

	/**
	 * @param getterElt
	 * @param setterElt
	 * @param adderElt
	 * @return
	 */
	protected static <T extends Annotation> T  getPropertyAnnotation(ExecutableElement getterElt, ExecutableElement setterElt, java.lang.Class<T> annotationType) {
		T ann = getterElt != null ? getterElt.getAnnotation(annotationType) : null;
		if(ann == null) {
			ann = setterElt != null ? setterElt.getAnnotation(annotationType) : null;
		}
		return ann;
	}

	  /**
	   * return properties which has setter/adder method
	   * @return
	   */
	  public List<PropertyInfo> getFromProperties() {
	    return FluentIterable.from(this.getPropertyMap().values()).filter(new Predicate<PropertyInfo>() {

	      @Override
	      public boolean apply(PropertyInfo p) {
	        return p.isSetter()||p.isAdder();
	      }
	    }).toList();
	  }
	  
	  /**
	   * return properties which has getter method
	   * @return
	   */
	  public List<PropertyInfo> getToProperties() {
	    return FluentIterable.from(this.getPropertyMap().values()).filter(new Predicate<PropertyInfo>() {

	      @Override
	      public boolean apply(PropertyInfo p) {
	        return p.getGetterMethod() != null;
	      }
	    }).toList();
	  }

	  /**
	   * return properties which has getter method
	   * @return
	   */
	  public List<DartProperty> getToDartProperties() {
	    return this.getToProperties().stream().filter(p -> p.getKind() != PropertyKind.NULL).
	    		map(p -> new DartProperty(p.getName(), p.getDartName(), p.getType(), p.getKind())).collect(Collectors.toList());
	  }

	  public List<DartProperty> getToDartNullProperties() {
	    return this.getToProperties().stream().filter(p -> p.getKind() == PropertyKind.NULL).
	    		map(p -> new DartProperty(p.getName(), p.getDartName(), p.getType(), p.getKind())).collect(Collectors.toList());
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