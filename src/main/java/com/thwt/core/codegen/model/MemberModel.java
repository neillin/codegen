/**
 * 
 */
package com.thwt.core.codegen.model;

import javax.lang.model.element.Element;

/**
 * @author neillin
 *
 */
public interface MemberModel extends JavaModel {
	String getJavaStatement();
	String getModifiers();
	String getType();
  boolean isPublic();
  boolean isStatic();
  boolean isPrivate();
  Element getElement();
  
}
