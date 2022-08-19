/*
 * @(#)FileUtils.java	 2017-02-13
 *
 * Copyright 2004-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.JavaSourceCodeFormatter;
import com.thwt.core.codegen.model.FileLocation;

/**
 * @author Neil Lin
 *
 */
public abstract class FileUtils {
  
  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
  
  public static void generateServiceFile(final ICodeGenerationContext context, String name, String content) throws Exception {
    FileObject file = context.getProcessingEnvironment().getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/"+name);
    log.debug("Generate services file : {}", file.toUri());
    Writer w = file.openWriter();
    w.write(content);
    w.close();
  }

  /**
   * @param context
   * @param filer
   * @param model
   * @param content
   * @throws IOException
   */
  public static void writeJavaFile(ICodeGenerationContext context,String fqn, Object content) throws IOException {
    try {
      JavaFileObject file = context.getProcessingEnvironment().getFiler().createSourceFile(fqn);
      log.debug("Generate java source file : {}", file.toUri());
      Writer w = file.openWriter();
      if(content instanceof String){
        w.write((String)content);
      }else if(content instanceof JCCompilationUnit){
        JCCompilationUnit unit = (JCCompilationUnit)content;
        unit.accept(new JavaSourceCodeFormatter(w,true));
      }else{
        throw new IOException("Unknown content :["+content+"]");
      }
      w.close();
//      log.info("Source code generated :["+file.getName()+"]");
    } catch (IOException e) {
      log.error("Failed to generate java file :" +fqn);
      throw e;
    }
  }

  public static void createJavaFile(ICodeGenerationContext context,String pkgname, String filename, String content, FileLocation location) throws IOException {
    createJavaFile(context, pkgname, filename, content, location, !context.getProcessingEnvironment().getOptions().containsKey("codegen.javasource.raw"));
  }
  /**
   * @param context
   * @param filer
   * @param model
   * @param content
   * @throws IOException
   */
  public static void createJavaFile(ICodeGenerationContext context,String pkgname, String filename, String content, FileLocation location, boolean format) throws IOException {
    final String fqn = pkgname+"."+filename;
    if(log.isDebugEnabled())
      log.info("Generate java class file :["+fqn+"]");
    if(format){
      try {
        content = formatJavaSource(context,content);
      }catch(Throwable t) {
        log.warn("Failed to format java source file :["+fqn+"]",t);
      }
    }
    try{
      context.addGeneratedSource(new SimpleJavaSource(fqn, location, content));
    }catch(Throwable t){
      log.error("Failed to create java source file :["+fqn+"]",t);
      //      context.addGeneratedSource(new SimpleJavaSource(fqn, location, content));
    }
  }
  
  /**
   * @param context
   * @param filer
   * @param model
   * @param content
   * @throws IOException
   */
  public static void createResourceFile(ICodeGenerationContext context,File targetDir, String filename, String content) throws IOException {
    final String fqn = targetDir.getCanonicalPath()+"/"+filename;
    if(log.isDebugEnabled())
      log.debug("Generate resource file : ["+fqn+"]");
    File file = new File(fqn);
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(content.getBytes(Charsets.UTF_8));
    fos.close();
  }

  
  /**
   * @param baseMap
   * @param javaSource
   * @param unit
   * @throws IOException
   */
  protected static String formatJavaSource(ICodeGenerationContext context,String content) throws IOException {
    JCCompilationUnit unit = ParserFactory.instance(context.getJavacContext()).newParser(content, true, true, true).parseCompilationUnit();
    final JCErroneous[] errors = new JCErroneous[] { null };
    Writer writer = new StringWriter();
    try {
      unit.accept(new JavaSourceCodeFormatter(writer,true){

        /* (non-Javadoc)
         * @see com.wxxr.mobile.core.tools.JavaSourceCodeFormatter#visitErroneous(com.sun.tools.javac.tree.JCTree.JCErroneous)
         */
        @Override
        public void visitErroneous(JCErroneous tree) {
          super.visitErroneous(tree);
          errors[0] = tree;
        }
        
      });
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
    if(errors[0] != null){
      log.warn("Generated source has syntax errors :["+errors[0]+"] ! \n "+content);
      return content;
    }
    return writer.toString();
  }

}
