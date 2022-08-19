/**
 * 
 */
package com.thwt.core.codegen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import com.sun.source.util.Trees;
import com.sun.tools.javac.util.Context;

/**
 * @author neillin
 *
 */
public interface ICodeGenerationContext {
	ProcessingEnvironment getProcessingEnvironment();
	RoundEnvironment getRoundEnvironment();
	ITemplateRenderer getTemplateRenderer();
	Trees getTrees();
	Context getJavacContext();
	void addGeneratedSource(JavaSource source);
	Object getParameter(String name);
	JavaFileObject getSourceFile(TypeElement typeElement);
	void reportException(Exception e, Element elt);
	Object getAttribute(String name);
	ICodeGenerationContext setAttribute(String name, Object value);
	String getOption(String name);
}
