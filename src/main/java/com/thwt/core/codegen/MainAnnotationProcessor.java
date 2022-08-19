/**
 * 
 */
package com.thwt.core.codegen;

import static com.thwt.core.codegen.util.Utils.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import com.thwt.core.codegen.annotation.DefaultTemplates;
import com.thwt.core.codegen.annotation.Generator;
import com.thwt.core.codegen.model.FileLocation;
import com.thwt.core.codegen.util.FileUtils;


/**
 * @author neillin
 *
 */
@SupportedOptions({"codegen.reflection.enabled"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MainAnnotationProcessor extends AbstractProcessor {
	private static final Logger log = LoggerFactory.getLogger(MainAnnotationProcessor.class);
	private static final ThreadLocal<ICodeGenerationContext> tContext = new ThreadLocal<ICodeGenerationContext>();
	
	public static ICodeGenerationContext getCurrentContext() {
		return tContext.get();
	}
	
	private Map<Class<? extends Annotation>, ICodeGenerator> supportingAnnotations = new HashMap<Class<? extends Annotation>,ICodeGenerator>();
	private Set<String> supportAnnotationTypes;
	private Set<String> options = new HashSet<String>();
	private Properties props = new Properties();
	private VelocityTemplateRenderer renderer;
	private Trees trees;
	private LinkedList<ICodeGenerator> processors = new LinkedList<ICodeGenerator>();
	private ProcessingEnvironment processingEnv;
	private List<JavaSource> generatedSources;
	private Map<String, Object> parameters = new HashMap<String, Object>();
//	private File outputDirectory;
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
	    log.debug("processing annations :{}, processor :{}, is last round :",annotations,this, roundEnv.processingOver());
		GenContext ctx = new GenContext(roundEnv);
		tContext.set(ctx);
		try {
			if(!this.supportingAnnotations.isEmpty()){
				for (Entry<Class<? extends Annotation>, ICodeGenerator> entry : this.supportingAnnotations.entrySet()) {
					Class<? extends Annotation> annClazz = entry.getKey();
					ICodeGenerator gen = entry.getValue();
					Set<? extends Element> elems = roundEnv.getElementsAnnotatedWith(annClazz);
					log.debug("Found annotated elements : {}",elems);
					if((elems != null)&&(elems.size() > 0)){
						gen.process(elems, ctx);
						if(!processors.contains(gen)){
							processors.add(gen);
						}
					}
				}
			}
			if(roundEnv.processingOver()){
				for (ICodeGenerator gen : processors) {
					gen.finishProcessing(ctx);
				}
			}else if(annotations.isEmpty()){
				for (ICodeGenerator gen : processors) {
					gen.process(Collections.<Element>emptySet(),ctx);
				}
			}
			return true;
		}finally {
			tContext.set(null);
		}
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		log.info("Initializing Main Annotation processor from "
				+ getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
		this.processingEnv = processingEnv;
		this.trees = Trees.instance(processingEnv);
		ServiceLoader<IProcessorConfigure> loader = ServiceLoader.load(IProcessorConfigure.class,
				getClass().getClassLoader());
		List<String> templates = new ArrayList<String>();
		for (IProcessorConfigure config : loader) {
			log.info("Found processor configure : " + config);
			DefaultTemplates temps = config.getClass().getAnnotation(DefaultTemplates.class);
			if (temps != null) {
				templates.addAll(Arrays.asList(temps.value()));
			}
			SupportedOptions so = config.getClass().getAnnotation(SupportedOptions.class);
			if (so != null) {
				for (String s : so.value()) {
					this.options.add(s);
					int idx = s.indexOf('=');
					if (idx > 0) {
						String key = trimToNull(s.substring(0, idx));
						String val = trimToNull(s.substring(idx + 1));
						if ((key != null) && (val != null)) {
							this.props.setProperty(key, val);
						}
					}
				}
			}
			Method[] methods = config.getClass().getMethods();
			if (methods != null) {
				for (Method method : methods) {
					if (method.getDeclaringClass() == Object.class) {
						continue;
					}
					Generator ann = method.getAnnotation(Generator.class);
					if (ann != null) {
						log.debug("Found processor method : {}, annotaions : {}", method,
								Joiner.on(',').join(method.getDeclaredAnnotations()));
						Class<? extends Annotation> annClazz = ann.forAnnotation();
						Class<?>[] paramTypes = method.getParameterTypes();
						Class<?> returnType = method.getReturnType();
						if ((annClazz != null) && (paramTypes != null) && (paramTypes.length == 1)
								&& (paramTypes[0] == Generator.class)
								&& (ICodeGenerator.class.isAssignableFrom(returnType))) {
							try {
								ICodeGenerator gen = (ICodeGenerator) method.invoke(config, ann);
								this.supportingAnnotations.put(annClazz, gen);
							} catch (Exception e) {
								log.error("Failed to create code generator for annotation : {}",
										annClazz.getCanonicalName());
							}
						}
					}
				}
			}
		}
		if (this.supportingAnnotations.isEmpty()) {
			this.supportAnnotationTypes = Collections.<String>emptySet();
		} else {
			this.supportAnnotationTypes = new HashSet<String>();
			for (Class<? extends Annotation> clazz : this.supportingAnnotations.keySet()) {
				this.supportAnnotationTypes.add(clazz.getCanonicalName());
			}
		}
		this.renderer = new VelocityTemplateRenderer();
		this.renderer.configure(this.props, templates.toArray(new String[0]));
		super.init(processingEnv);
		log.info("Done Main Annotation processor intialization , support annotations {}",
				getSupportedAnnotationTypes());
	}

	
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#getSupportedAnnotationTypes()
	 */
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return this.supportAnnotationTypes;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#getSupportedOptions()
	 */
	@Override
	public Set<String> getSupportedOptions() {
		return this.options;
	}
	
	protected ITemplateRenderer getVelocityRenderer(){
		return this.renderer;
	}
	
	public List<JavaSource> getGeneratedSources() {
		return this.generatedSources != null && this.generatedSources.size() > 0 ? Collections.unmodifiableList(this.generatedSources) : null;
	}
	
	private class GenContext implements ICodeGenerationContext {
		private final RoundEnvironment env;
		private final Map<String, Object> attrs = new HashMap<String, Object>();
		
		public GenContext(RoundEnvironment env){
			this.env = env;
		}
		@Override
		public ProcessingEnvironment getProcessingEnvironment() {
			return processingEnv;
		}
		@Override
		public RoundEnvironment getRoundEnvironment() {
			return this.env;
		}
		@Override
		public ITemplateRenderer getTemplateRenderer() {
			return getVelocityRenderer();
		}
		@Override
		public Trees getTrees() {
			return trees;
		}
		
		
		@Override
		public Context getJavacContext() {
			return ((JavacProcessingEnvironment)processingEnv).getContext();
		}
		
		@Override
		public void addGeneratedSource(JavaSource source) {
			if((source.getFileLocation() == FileLocation.OUTPUT)&&(env.processingOver()==false)){
				try {
					FileUtils.writeJavaFile(this,source.getSourceFQN(),source.getContentObject());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return;
			}
			if(generatedSources == null){
				generatedSources = new ArrayList<JavaSource>();
			}
			if(!generatedSources.contains(source)){
				generatedSources.add(source);
			}
		}
		@Override
		public Object getParameter(String name) {
			return parameters.get(name);
		}
		
		@Override
		public JavaFileObject getSourceFile(TypeElement typeElement) {
			return ((ClassSymbol)typeElement).sourcefile;
		}
    /* (non-Javadoc)
     * @see com.thwt.core.codegen.ICodeGenerationContext#reportException(java.lang.Exception, javax.lang.model.element.Element)
     */
    @Override
    public void reportException(Exception e, Element elt) {
      String msg = "Could not generate code for " + elt + ": " + Throwables.getStackTraceAsString(e);
      log.error(msg, e);
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, elt);
    }
    /* (non-Javadoc)
     * @see com.thwt.core.codegen.ICodeGenerationContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
      return this.attrs.get(name);
    }
    /* (non-Javadoc)
     * @see com.thwt.core.codegen.ICodeGenerationContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public ICodeGenerationContext setAttribute(String name, Object value) {
      this.attrs.put(name, value);
      return this;
    }
	/* 
	 * @see com.thwt.core.codegen.ICodeGenerationContext#getOption(java.lang.String)
	 */
	@Override
	public String getOption(String name) {
		return this.getProcessingEnvironment().getOptions().get(name);
	}
	}
	
	public void setParameter(String name, Object value) {
		this.parameters.put(name, value);
	}
	
	public Object getParameter(String name){
		return this.parameters.get(name);
	}
	
	public Object removeParameter(String name){
		return this.parameters.remove(name);
	}

}
