/*
 * @(#)ImportManagerImpl.java	 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * @author Neil Lin
 *
 */
public class ImportManagerImpl implements ImportManager {
  protected Map<String, String> classNameMap = new HashMap<String, String>();
  protected Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
  protected Map<String, String> staticNameMap = new HashMap<String, String>();
  protected HashSet<String> notAClass = new HashSet<String>();
  private String pkgName;
  private List<String> imports = new ArrayList<String>();
  protected List<String> packages = new ArrayList<String>();

  public ImportManagerImpl() {
    importPackage("java.lang");
  }
  /* (non-Javadoc)
   * @see com.wxxr.el.codegen.ISourceCode#importStatic(java.lang.String)
   */
  @Override
  public String importStatic(String name) {
    int i = name.lastIndexOf('.');
    if (i <= 0) {
      throw new IllegalArgumentException(
          "The name " + name + " is not a full static member name");
    }
    String memberName = name.substring(i+1);
    String className = name.substring(0, i);
    staticNameMap.put(memberName, className);
    return memberName;
  }

  /* (non-Javadoc)
   * @see com.wxxr.el.codegen.ISourceCode#importClass(java.lang.String)
   */
  @Override
  public String importClass(String name) {
    return doImportType(new TypeModel(name));
  }
  
  protected String doImportType(TypeModel typeModel) {
    StringBuilder sb = new StringBuilder();
    if(typeModel.isArray()) {
      sb.append(doImport(typeModel.getCompnentType()));
    }else{
      sb.append(doImport(typeModel.getType()));
    }
    TypeModel[] tParams = typeModel.getParameterTypes();
    if(tParams != null && tParams.length > 0) {
      sb.append('<');
      for (int i=0 ; i < tParams.length ; i++) {
        if(i > 0) {
          sb.append(',');
        }
        sb.append(doImportType(tParams[i]));
      }
      sb.append('>');
    }
    if(typeModel.isArray()) {
      sb.append("[]");
    }
    return sb.toString();

  }
  
//  public String doGenericImport(String stmt) {
//    if(isBlank(stmt)){
//      return null;
//    }
//    String generic = null;
//    int sIdx = stmt.indexOf('<');
//    if(sIdx > 0){
//      generic = trimToNull(stmt.substring(sIdx+1));
//      if(generic.endsWith(">")){
//        generic = generic.substring(0,generic.length()-1);
//      }
//      stmt = stmt.substring(0,sIdx);
//    }
//    int idx = stmt.lastIndexOf('.');
//    if(idx > 0){
//      stmt = doImport(stmt);
//    }
//    if(generic != null){
//      List<String> tokens = new ArrayList<String>();
//      int size = generic.length();
//      StringBuffer sb = new StringBuffer();
//      int nestedCnt = 0;
//      for(int i=0 ; i < size ; i++) {
//        char c = generic.charAt(i);
//        if(c == ',' && nestedCnt == 0) {
//          if(sb.length() > 0) {
//            tokens.add(sb.toString());
//            sb.setLength(0);
//          }
//        }else{
//          sb.append(c);
//          if(c == '<') {
//            nestedCnt++;
//          }else if(c == '>') {
//            if(nestedCnt > 0) {
//              nestedCnt--;
//            }
//          }
//        }
//      }
//      if(sb.length() > 0) {
//        tokens.add(sb.toString());
//      }
//      StringBuffer buf = new StringBuffer(stmt).append('<');
//      int cnt = 0;
//      for (String token : tokens) {
//        token = doGenericImport(token);
//        if(cnt > 0){
//          buf.append(',');
//        }
//        buf.append(token);
//        cnt++;
//      }
//      stmt = buf.append('>').toString();
//    }
//    return stmt;
//
//  }
  /**
   * @param name
   */
  private String doImport(String name) {
    int i = name.lastIndexOf('.');
    if (i <= 0) {
//      throw new IllegalArgumentException(
//          "The name " + name + " is not a full class name");
      return name;
    }
    String className = name.substring(i+1);
    String pkg = name.substring(0,i);
    classNameMap.put(className, name);
    if((!"java.lang".equals(pkg))&&(!pkg.equals(pkgName))){
      if(imports == null){
        imports = new ArrayList<String>();
      }
      if(name.endsWith("[]")){
        name = name.substring(0,name.length()-2);
      }
      if(name.endsWith("[ ]")){
        name = name.substring(0,name.length()-3);
      }
      if(!imports.contains(name)){
        imports.add(name);
      }
    }
    return className;
  }

  /* (non-Javadoc)
   * @see com.wxxr.el.codegen.ISourceCode#importPackage(java.lang.String)
   */
  @Override
  public ImportManagerImpl importPackage(String packageName) {
    packages.add(packageName);
    return this;
  }

  /* (non-Javadoc)
   * @see com.wxxr.el.codegen.ISourceCode#resolveClass(java.lang.String)
   */
  @Override
  public String resolveClass(String name) {

    String className = classNameMap.get(name);
    if (className != null) {
      return className;
    }

    for (String packageName: packages) {
      String fullClassName = packageName + "." + name;
      Class<?>c = resolveClassFor(fullClassName);
      if (c != null) {
        classNameMap.put(name, fullClassName);
        return fullClassName;
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see com.wxxr.el.codegen.ISourceCode#resolveStatic(java.lang.String)
   */
  @Override
  public String resolveStatic(String name) {
    String className = staticNameMap.get(name);
    if (className != null) {
      Class<?> c = resolveClassFor(className);
      if (c != null) {
        return className;
      }
    }
    return null;
  }

  private void checkModifiers(int modifiers) {
    if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)
        || ! Modifier.isPublic((modifiers))) {
      throw new IllegalArgumentException("Imported class must be public, and cannot be abstract or an interface");
    }
  }

  private Class<?> resolveClassFor(String className) {
    Class<?> c = classMap.get(className);
    if (c != null) {
      return c;
    }
    c = getClassFor(className);
    if (c != null) {
      checkModifiers(c.getModifiers());
      classMap.put(className, c);
    }
    return c;
  }


  private Class<?> getClassFor(String className) {
    if (!notAClass.contains(className)) {
      try {
        return Class.forName(className, false, getClass().getClassLoader());
      } catch (ClassNotFoundException ex) {
        notAClass.add(className);
      }
    }
    return null;
  }
  /**
   * @return the pkgName
   */
  public String getPkgName() {
    return pkgName;
  }
  /**
   * @param pkgName the pkgName to set
   */
  public void setPkgName(String pkgName) {
    this.pkgName = pkgName;
  }
  
  /**
   * @return the imports
   */
  public List<String> getImports() {
    return imports;
  }
  
  public List<String> getPackageImports() {
    return packages;
  }
  
  /**
   * @return the imports
   */
  public Set<String> getStaticImports() {
    return FluentIterable.from(staticNameMap.entrySet()).transform(new Function<Entry<String, String>, String>() {

      @Override
      public String apply(Entry<String, String> input) {
        return input.getValue()+"."+input.getKey();
      }
    }).toSet();
  }

}
