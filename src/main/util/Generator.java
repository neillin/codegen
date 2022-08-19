package com.thwt.core.codegen.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.thwt.core.codegen.MainAnnotationProcessor;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 */
public class Generator {
	private static Logger log = LoggerFactory.getLogger(Generator.class);
	HashMap<String, String> options = new HashMap<>();
	DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
	File sourceOutput;
	File classOutput;
	private boolean removeDuplicates = false;

	public Generator() {
		this.sourceOutput = new File("target/test-generated");
	}

	public Generator(File srcOutput) {
		this.sourceOutput = srcOutput;
	}

	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return collector.getDiagnostics();
	}

	public void setOption(String name, String value) {
		options.put(name, value);
	}

	public void generateClass(Class c, Class... rest) throws Exception {
		ArrayList<Class> types = new ArrayList<>();
		types.add(c);
		Collections.addAll(types, rest);
		MainAnnotationProcessor processor = new MainAnnotationProcessor();
		Compiler compiler = new Compiler(processor, collector).setRemoveDuplcates(this.removeDuplicates);
		if (!this.options.isEmpty()) {
			compiler.addOptions(FluentIterable.from(this.options.entrySet())
					.transform(new Function<Entry<String, String>, String>() {

						@Override
						public String apply(Entry<String, String> entry) {
							return "-A" + entry.getKey() + (entry.getValue() != null ? "=" + entry.getValue() : "");
						}
					}).toList());
		}
		if (this.sourceOutput != null) {
			if (!this.sourceOutput.exists()) {
				this.sourceOutput.mkdirs();
			}else {
				MoreFiles.deleteDirectoryContents(this.sourceOutput.toPath(),RecursiveDeleteOption.ALLOW_INSECURE);
			}
			compiler.setSourceOutput(sourceOutput);
		}
		if (this.classOutput != null) {
			if (!this.classOutput.exists()) {
				this.classOutput.mkdirs();
			}
			compiler.setClassOutput(classOutput);
		}
		compiler.compile(types);
		if (this.sourceOutput == null) {
			this.sourceOutput = compiler.getSourceOutput();
		}
		if (this.classOutput == null) {
			this.classOutput = compiler.getClassOutput();
		}
		log.info("Source output directory :"+this.sourceOutput.getAbsolutePath());
		log.info("Class output directory :"+this.classOutput.getAbsolutePath());
	}

	/**
	 * @return the sourceOuput
	 */
	public File getSourceOutput() {
		return sourceOutput;
	}

	/**
	 * @param sourceOuput
	 *            the sourceOuput to set
	 */
	public void setSourceOutput(File sourceOuput) {
		this.sourceOutput = sourceOuput;
	}

	/**
	 * @return the classOuput
	 */
	public File getClassOutput() {
		return classOutput;
	}

	/**
	 * @param classOuput
	 *            the classOuput to set
	 */
	public void setClassOutput(File classOuput) {
		this.classOutput = classOuput;
	}

	/**
	 * @return the removeDuplicates
	 */
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

	/**
	 * @param removeDuplicates
	 *            the removeDuplicates to set
	 */
	public Generator setRemoveDuplicates(boolean removeDuplicates) {
		this.removeDuplicates = removeDuplicates;
		return this;
	}

}
