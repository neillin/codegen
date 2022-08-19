/*
 * @(#)ClassModelImpl.java 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import static com.thwt.core.codegen.util.Utils.trimToNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.model.type.ClassKind;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.Doc;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.model.type.TypeParamInfo;
import com.thwt.core.codegen.util.ChecksumBuilder;
import com.thwt.core.codegen.util.ModelUtils;
import com.thwt.core.codegen.util.ModelUtils.FieldHandler;
import com.thwt.core.codegen.util.ModelUtils.MethodHandler;


/**
 * @author Neil Lin
 *
 */
public class ClassModelImpl implements ClassModel {
	private final ClassTypeInfo typeInfo;

	private ClassTypeInfo superClass;
	private final Set<ClassTypeInfo> interfaces = new LinkedHashSet<>();
	private boolean traceRequired;
	protected Map<String, FieldModel> fields;
	private Map<String, MethodModel> methods;
	private FileLocation fileLocation = FileLocation.OUTPUT;
	protected ImportManagerImpl importMgr = new ImportManagerImpl();
	private TypeElement element;
	private Set<Modifier> modifiers;
	private Doc.Factory docFactory;
	private Doc comment;

	public ClassModelImpl(String qualifiedName) {
		this.typeInfo = new ClassTypeInfo(ClassKind.OTHER, qualifiedName, Collections.<TypeParamInfo.Class>emptyList());
	}

	public ClassModelImpl(String qualifiedName, List<TypeParamInfo.Class> params) {
		this.typeInfo = new ClassTypeInfo(ClassKind.OTHER, qualifiedName, params);
	}

	public ClassModelImpl(TypeElement elem) {
		this.element = Preconditions.checkNotNull(elem);
		this.typeInfo = (ClassTypeInfo) TypeMirrorFactory.getInstance().create(this.element.asType());
		this.docFactory = new Doc.Factory(this.element);
		this.comment = this.docFactory.createDoc(element);
		for (ClassTypeInfo info : FluentIterable.from(this.element.getInterfaces()).filter(new Predicate<TypeMirror>() {

			@Override
			public boolean apply(TypeMirror superTM) {
				return superTM instanceof DeclaredType
						&& ((DeclaredType) superTM).asElement().getAnnotation(DataObject.class) != null;
			}
		}).transform(new Function<TypeMirror, ClassTypeInfo>() {

			@Override
			public ClassTypeInfo apply(TypeMirror e) {
				return (ClassTypeInfo) TypeMirrorFactory.getInstance().create(e);
			}
		})) {
			this.interfaces.add(info);
			info.collectImports(getImportManager());
		}
		this.superClass = (ClassTypeInfo) TypeMirrorFactory.getInstance().create(this.element.getSuperclass());
		if (this.superClass != null) {
			this.superClass.collectImports(getImportManager());
		}
		this.modifiers = this.element.getModifiers();
		doProcess();
	}

	protected void doProcess() {
		process(null, null);
	}

	public void process(FieldHandler fHandler, MethodHandler mHandler) {
		if (this.element == null) {
			throw new IllegalStateException("Cannot process class model without type element !");
		}
		ModelUtils.collectionBasicClassInfo(MainAnnotationProcessor.getCurrentContext(), this.element, this, fHandler,
				mHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getName()
	 */
	public String getName() {
		return this.typeInfo.getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getPkgName()
	 */
	public String getPkgName() {
		return this.typeInfo.getPackageName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getImports()
	 */
	public List<String> getImports() {
		HashSet<String> imports = new HashSet<String>();
		imports.addAll(this.importMgr.getImports());
		FluentIterable.from(this.importMgr.getPackageImports()).filter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return "java.lang".equals(input) == false;
			}
		}).transform(new Function<String, String>() {
			@Override
			public String apply(String pkg) {
				return pkg + ".*";
			}
		}).copyInto(imports);
		return new ArrayList<>(imports);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getClassName()
	 */
	public String getClassName() {
		return this.typeInfo.getName();
	}

	// /**
	// * @param name the name to set
	// */
	// public void setName(String name) {
	// this.name = name;
	// }
	// /**
	// * @param pkgName the pkgName to set
	// */
	// public void setPkgName(String pkgName) {
	// this.pkgName = pkgName;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#addImport(java.lang.String)
	 */
	public String importClass(String stmt) {
		return this.importMgr.importClass(stmt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getSuperClass()
	 */
	public String getSuperClass() {
		return superClass.getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getInterfaces()
	 */
	public List<String> getInterfaces() {
		return FluentIterable.from(this.interfaces).transform(new Function<ClassTypeInfo, String>() {

			@Override
			public String apply(ClassTypeInfo info) {
				return info.getSimpleName();
			}
		}).toList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getJoinInterfaces()
	 */
	public String getJoinInterfaces() {
		return this.interfaces != null && this.interfaces.size() > 0 ? Joiner.on(',').join(this.interfaces.iterator())
				: null;
	}

	/**
	 * @param superClass
	 *            the superClass to set
	 */
	@Override
	public void setSuperClass(String superClass) {
		superClass = trimToNull(superClass);
		if ((superClass != null) && (Object.class.getCanonicalName().equals(superClass) == false)) {
			this.superClass = (ClassTypeInfo) TypeMirrorFactory.getInstance()
					.create(TypeModel.getTypeMirror(new TypeModel(superClass)));
		}
	}

	/**
	 * @param interfaces
	 *            the interfaces to set
	 */
	@Override
	public void addInterface(String clazz) {
		ClassTypeInfo info = (ClassTypeInfo) TypeMirrorFactory.getInstance()
				.create(TypeModel.getTypeMirror(new TypeModel(clazz)));
		if (!this.interfaces.contains(info)) {
			this.interfaces.add(info);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getFields()
	 */
	public List<FieldModel> getFields() {
		return fields == null ? null : new ArrayList<FieldModel>(fields.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getMethods()
	 */
	public List<MethodModel> getMethods() {
		return methods == null ? null : new ArrayList<MethodModel>(methods.values());
	}

	@Override
	public void addField(String name, String className) {
		name = trimToNull(name);
		className = trimToNull(className);
		if ((name == null) || (className == null)) {
			throw new IllegalArgumentException("name and className cannot be NULL !");
		}
		FieldModel fld = new FieldModel(this);
		fld.setName(name);
		fld.setType(className);
		addField(fld);
	}

	/**
	 * 
	 */
	@Override
	public void addField(FieldModel field) {
		if (field.getName().equals("log")) {
			this.traceRequired = true;
			return;
		}
		if (this.fields == null) {
			this.fields = new HashMap<String, FieldModel>();
		}
		this.fields.put(field.getName(), field);
		// field.setClassModel(this);
	}

	@Override
	public void addMethod(MethodModel method) {
		if (this.methods == null) {
			this.methods = new HashMap<String, MethodModel>();
		}
		if (method.getClassModel() != this) {
			throw new IllegalArgumentException(
					"the adding method is not declared by this class, method :[" + method + "]");
		}
		this.methods.put(method.getMethodKey(), method);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thwt.core.codegen.model.ClassModel#hasMethod(com.thwt.core.codegen.model.
	 * MethodModel)
	 */
	public boolean hasMethod(MethodModel method) {
		return method != null && this.methods != null && this.methods.containsKey(method.getMethodKey());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getMethod(java.lang.String)
	 */
	public MethodModel getMethod(String methodKey) {
		return this.methods.get(methodKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#isTraceRequired()
	 */
	public boolean isTraceRequired() {
		return traceRequired;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getFullQualifiedName()
	 */
	public String getFullQualifiedName() {
		return getPkgName() + "." + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getFileLocation()
	 */
	public FileLocation getFileLocation() {
		return fileLocation;
	}

	/**
	 * @param fileLocation
	 *            the fileLocation to set
	 */
	public void setFileLocation(FileLocation fileLocation) {
		this.fileLocation = fileLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.thwt.core.codegen.model.ClassModel#checksum(com.thwt.core.codegen.util.
	 * ChecksumBuilder)
	 */
	public void checksum(ChecksumBuilder builder) {
		builder.putString(getClassName());
		if (this.superClass != null) {
			builder.putString(this.superClass.getName());
		}
		builder.put(this.traceRequired ? (byte) 1 : (byte) 0);
		if ((this.fields != null) && (this.fields.size() > 0)) {
			String[] keys = this.fields.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			for (String key : keys) {
				FieldModel p = this.fields.get(key);
				if (p != null) {
					p.checksum(builder);
				}
			}
		}
		if ((this.methods != null) && (this.methods.size() > 0)) {
			String[] keys = this.methods.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			for (String key : keys) {
				MethodModel p = this.methods.get(key);
				if (p != null) {
					p.checksum(builder);
				}
			}
		}
		if ((this.interfaces != null) && (this.interfaces.size() > 0)) {
			String[] keys = this.interfaces.toArray(new String[0]);
			Arrays.sort(keys);
			for (String key : keys) {
				builder.putString(key);
			}
		}
		List<String> imports = getImports();
		if ((imports != null) && (imports.size() > 0)) {
			String[] keys = imports.toArray(new String[0]);
			Arrays.sort(keys);
			for (String key : keys) {
				builder.putString(key);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getImportManager()
	 */
	@Override
	public ImportManager getImportManager() {
		return this.importMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getElement()
	 */
	@Override
	public TypeElement getElement() {
		return this.element;
	}

	/**
	 * @return the typeInfo
	 */
	public ClassTypeInfo getTypeInfo() {
		return typeInfo;
	}

	public ClassTypeInfo getSuperType() {
		return this.superClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#isConcrete()
	 */
	@Override
	public boolean isAbstract() {
		return (this.modifiers != null && this.modifiers.contains(Modifier.ABSTRACT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thwt.core.codegen.model.ClassModel#getInterfaceTypes()
	 */
	@Override
	public Set<ClassTypeInfo> getInterfaceTypes() {
		return this.interfaces;
	}

	/**
	 * @return the comment
	 */
	public Doc getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(Doc comment) {
		this.comment = comment;
	}

	/**
	 * @return the docFactory
	 */
	public Doc.Factory getDocFactory() {
		return docFactory;
	}

	/**
	 * @param docFactory
	 *            the docFactory to set
	 */
	public void setDocFactory(Doc.Factory docFactory) {
		this.docFactory = docFactory;
	}

	/* 
	 * @see com.thwt.core.codegen.model.ClassModel#getSuperClassElement()
	 */
	@Override
	public TypeElement getSuperClassElement() {
		return ModelUtils.getSuperClassElement(getElement());
	}

}
