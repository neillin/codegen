/*
 * @(#)ConverterProviderModel.java	 2017-02-14
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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;

/**
 * @author Neil Lin
 *
 */
public class ConverterProviderModel extends ClassModelImpl {
  private static SecureRandom rand = new SecureRandom();
  static {
	  rand.setSeed(System.currentTimeMillis());
  }
  
  private Multimap<String, ClassModel> converters = ArrayListMultimap.<String, ClassModel>create();
  
  public ConverterProviderModel(Multimap<String, ClassModel> convs) {
    super(generatePackageName(Preconditions.checkNotNull(convs).values())+".JsonConverterProvider"+Math.abs(rand.nextInt()));
    for (Entry<String,ClassModel> m : convs.entries()) {
      addConverter(m.getKey(), m.getValue());
    }
  }

  public ConverterProviderModel addConverter(String category, ClassModel model) {
    model = Preconditions.checkNotNull(model);
    category = Preconditions.checkNotNull(category);
    importClass(model.getClassName());
    converters.put(category, model);
    return this;
  }
  
  public Collection<Entry<String, ClassModel>> getConverters() {
    return this.converters.entries();
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
  
  private static String generatePackageName(Collection<ClassModel> convs) {
    HashSet<String> pkgs = new HashSet<String>();
    for (ClassModel m : convs) {
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
