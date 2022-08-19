/*
 * @(#)ModuleProviderModel.java	 2017-02-14
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;

/**
 * @author Neil Lin
 *
 */
public class ModuleProviderModel extends ClassModelImpl {
  private static SecureRandom rand = new SecureRandom();
  static {
	  rand.setSeed(System.currentTimeMillis());
  }
  private Map<String, ClassModel> modules = new HashMap<String, ClassModel>();
  
  public ModuleProviderModel(List<ClassModel> mods) {
    super(generatePackageName(mods)+".ServiceModuleProviderImpl"+Math.abs(rand.nextInt()));
    for (ClassModel m : Preconditions.checkNotNull(mods)) {
      addModule(m);
    }
  }
  
  public ModuleProviderModel addModule(ClassModel model) {
    model = Preconditions.checkNotNull(model);
    model.getTypeInfo().collectImports(getImportManager());
    modules.put(model.getClassName(), model);
    return this;
  }
  
  public Collection<ClassModel> getModules() {
    return this.modules.values();
  }
    
  private static String getCommonParts(String pkg1, String pkg2) {
    List<String> list1 = Splitter.on('.').splitToList(pkg1);
    List<String> list2 = Splitter.on('.').splitToList(pkg2);
    int size1 = list1.size();
    int size2 = list2.size();
    int size = size1 >= size2 ? size2 : size1;
    List<String> commons = new ArrayList<String>();
    for (int i = 0; i < size; i++) {
      if(!list1.get(i).equals(list2.get(i))){
        break;
      }
      commons.add(list1.get(i));
    }
    if(commons.size() <= 3) {
      if(size > 3) {
        return size1 > size2 ? Joiner.on('.').join(list2) : Joiner.on('.').join(list1);
      }else{
        return size1 > size2 ? Joiner.on('.').join(list1) : Joiner.on('.').join(list2);
      }
    }else{
      return Joiner.on('.').join(commons);
    }
  }
  
  private static String generatePackageName(List<ClassModel> mods) {
    HashSet<String> pkgs = new HashSet<String>();
    for (ClassModel m : mods) {
      pkgs.add(m.getPkgName());
    }
    if(pkgs.size() == 1) {
      return pkgs.iterator().next();
    }
    String name = null;
    for (String pkg : pkgs) {
      if(name == null) {
        name = pkg;
      } else {
        if(name.startsWith(pkg)) {
          name = pkg;
        }else if(!pkg.startsWith(name)) {
          name = getCommonParts(name, pkg);
        }
      }
    }
    return name;
  }
}
