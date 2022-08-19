package com.thwt.core.codegen.util;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Compiler {

  private Processor processor;
  private DiagnosticListener<JavaFileObject> diagnosticListener;
  private List<String> options = new ArrayList<>();
  private File classOutput,sourceOutput;
  private boolean removeDuplcates = false;

  public Compiler(Processor processor) {
    this(processor, new DiagnosticCollector<JavaFileObject>());
  }

  public Compiler(Processor processor, DiagnosticListener<JavaFileObject> diagnosticListener) {
    this.processor = processor;
    this.diagnosticListener = diagnosticListener;
  }

  public Compiler() {
    this(null);
  }

  public Processor getProcessor() {
    return processor;
  }

  public Compiler addOption(String option) {
    options.add(option);
    return this;
  }
  
  public Compiler addOptions(List<String> opts) {
    options.addAll(opts);
    return this;
  }

  public File getClassOutput() {
    return classOutput;
  }

  public void setClassOutput(File classOutput) {
    this.classOutput = classOutput;
  }

  public boolean compile(Class... types) throws Exception {
    return compile(Arrays.asList(types));
  }

  private File getOutputDir(Class<?> type) throws Exception {
    String className = type.getCanonicalName();
    ClassLoader loader = type.getClassLoader();
    String fileName = className.replace(".", "/") + ".class";
    URL classFile = loader.getResource(fileName);
    if(classFile != null && "file".equalsIgnoreCase(classFile.getProtocol())) {
      File f = new File(classFile.toURI().getPath());
      if(f.exists() && f.isFile()) {
        String path = f.getCanonicalPath();
        path = path.substring(0,path.lastIndexOf(fileName));
        f = new File(path);
        if(f.exists() && f.isDirectory()) {
          return f;
        }
      }
    }
    return null;
  }
  
  private void removeDuplicatedClass(List<Class> types) {
    if(!this.removeDuplcates) {
    		return;
    }
    for(Class<?> clazz : types) {
      String className = clazz.getCanonicalName();
      String fileName = className.replace(".", "/") + ".class";
      File f = new File(this.classOutput, fileName);
      if(f.exists()) {
        f.delete();
      }
    }
  }
  
  public boolean compile(List<Class> types) throws Exception {
    ArrayList<File> tmpFiles = new ArrayList<>();
    for (Class type : types) {
      String className = type.getCanonicalName();
      String javaFile = className.replace(".", "/") + ".java";
      ClassLoader loader = type.getClassLoader();
      InputStream is = loader.getResourceAsStream(javaFile);
      if (is == null) {
        URL url =type.getProtectionDomain().getCodeSource().getLocation();
        if("file".equalsIgnoreCase(url.getProtocol())) {
          File file = new File("src/test/java",javaFile);
          if(file.exists()) {
            is = file.toURI().toURL().openStream();
          }
        }
        if(is == null) {
          throw new IllegalStateException("Can't find source on classpath: " + javaFile);
        }
      }
//      if(classOutput == null) {
//        classOutput = getOutputDir(type);
//      }
      // Load the source
      String source;
      try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
        source = scanner.next();
      }
      // Now copy it to a file (this is clunky but not sure how to get around it)
      String tmpFileName = System.getProperty("java.io.tmpdir") + "/" + javaFile;
      File f = new File(tmpFileName);
      File parent = f.getParentFile();
      parent.mkdirs();
      try (PrintStream out = new PrintStream(new FileOutputStream(tmpFileName))) {
        out.print(source);
      }
      tmpFiles.add(f);
    }
    if(compile(tmpFiles.toArray(new File[tmpFiles.size()]))){
      removeDuplicatedClass(types);
      return true;
    }
    return false;
  }

  public boolean compile(File... sourceFiles) throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fm = compiler.getStandardFileManager(diagnosticListener, null, null);
    File tmp = null;
    if (classOutput == null) {
      tmp = Files.createTempDirectory("generated").toFile();
      tmp.deleteOnExit();
      classOutput = new File(tmp, "output");
      classOutput.mkdirs();
    }
    fm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(classOutput));
    if(this.sourceOutput == null) {
      if(tmp == null) {
        tmp = Files.createTempDirectory("generated").toFile();
        tmp.deleteOnExit();
      }
      sourceOutput = new File(tmp, "java");
      sourceOutput.mkdirs();
    }
    fm.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singletonList(this.sourceOutput));
    Iterable<? extends JavaFileObject> fileObjects = fm.getJavaFileObjects(sourceFiles);
    Writer out = new NullWriter();
    JavaCompiler.CompilationTask task = compiler.getTask(out, fm, diagnosticListener, options, null, fileObjects);
    List<Processor> processors = Collections.<Processor>singletonList(processor);
    task.setProcessors(processors);
    try {
      return task.call();
    } catch (RuntimeException e) {
      if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
        throw (RuntimeException)e.getCause();
      } else {
        throw e;
      }
    }
  }

  private static class NullWriter extends Writer {
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      for(int i=0 ; i< len; i++) {
        System.out.write(cbuf[off+i]);
      }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
  }

  /**
   * @return the sourceOutput
   */
  public File getSourceOutput() {
    return sourceOutput;
  }

  /**
   * @param sourceOutput the sourceOutput to set
   */
  public void setSourceOutput(File sourceOutput) {
    this.sourceOutput = sourceOutput;
  }

	/**
	 * @return the removeDuplcates
	 */
	public boolean isRemoveDuplcates() {
		return removeDuplcates;
	}
	
	/**
	 * @param removeDuplcates the removeDuplcates to set
	 */
	public Compiler setRemoveDuplcates(boolean removeDuplcates) {
		this.removeDuplcates = removeDuplcates;
		return this;
	}

}
