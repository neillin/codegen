/*
 * @(#)MethodSignature.java	 2017-02-26
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.model;

import java.lang.reflect.Type;

import com.thwt.core.codegen.model.type.ParamInfo;

/**
 * @author Neil Lin
 *
 */
public interface MethodSignature {
  
  public static class Factory {
    public static MethodSignature createSignature(final String name, final ParamInfo[] params, final boolean isVarArgs) {
      return new MethodSignature() {
        
        @Override
        public String getSignature() {
          StringBuilder buf = new StringBuilder();
          buf.append(name).append('(');
          int size = params != null ? params.length : 0;
          if(size > 0){
            for(int i=0 ; i < size ; i++){
              if(i > 0){
                buf.append(',');
              }
              buf.append(params[i].getType().getName());
              if(i == (size -1)){
                if(isVarArgs){
                  buf.append("...");
                }
              }
            }
          }
          buf.append(')');
          return buf.toString();
        }
        
        public boolean equals(Object o) {
          if (o == this) {
            return true;
          }
          if (o instanceof MethodSignature) {
            return getSignature().equals(((MethodSignature)o).getSignature());
          }
          return false;
        }

        @Override
        public int hashCode() {
          return name.hashCode() ^ params.hashCode();
        }

        @Override
        public String toString() {
          return getSignature();
        }

      };
    }
    
    public static MethodSignature createSignature(final String name, final Type[] params, final boolean isVarArgs) {
      return new MethodSignature() {
        
        @Override
        public String getSignature() {
          StringBuilder buf = new StringBuilder();
          buf.append(name).append('(');
          int size = params != null ? params.length : 0;
          if(size > 0){
            for(int i=0 ; i < size ; i++){
              if(i > 0){
                buf.append(',');
              }
              buf.append(params[i]);
              if(i == (size -1)){
                if(isVarArgs){
                  buf.append("...");
                }
              }
            }
          }
          buf.append(')');
          return buf.toString();
        }
        
        public boolean equals(Object o) {
          if (o == this) {
            return true;
          }
          if (o instanceof MethodSignature) {
            return getSignature().equals(((MethodSignature)o).getSignature());
          }
          return false;
        }

        @Override
        public int hashCode() {
          return name.hashCode() ^ params.hashCode();
        }

        @Override
        public String toString() {
          return getSignature();
        }

      };
    }

  }

  String getSignature();
}
