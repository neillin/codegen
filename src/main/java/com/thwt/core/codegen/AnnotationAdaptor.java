package com.thwt.core.codegen;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class AnnotationAdaptor {
	
	public static interface ValueBuilder<T> {
		T buildAnnotation(Object val);
	}
	
	public static interface AnnotationBuilder<T extends AnnotationAdaptor> {
    T buildAnnotation(ICodeGenerationContext ctx, AnnotationMirror mirrot);
  }
	

	protected AnnotationMirror mirror;
	protected ICodeGenerationContext context;
	protected Map<String, AnnotationValue> values = new HashMap<String, AnnotationValue>();

	public AnnotationAdaptor(ICodeGenerationContext ctx,AnnotationMirror ann) {
		this.mirror = ann;
		this.context = ctx;
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : this.mirror.getElementValues().entrySet()) {
			ExecutableElement key = entry.getKey();
			AnnotationValue value = entry.getValue();
			this.values.put(key.getSimpleName().toString(), value);
		}
		for(Element elem : ((TypeElement)this.mirror.getAnnotationType().asElement()).getEnclosedElements()){
			if(elem instanceof ExecutableElement){
				ExecutableElement meth = (ExecutableElement)elem;
				String valKey = meth.getSimpleName().toString();
				if(!this.values.containsKey(valKey)){
					AnnotationValue value = meth.getDefaultValue();
					if(value != null){
						this.values.put(valKey, value);
					}
				}
			}
		}
	}
	
	protected String getString(String name){
		AnnotationValue val = this.values.get(name);
		return val != null ? val.getValue().toString() : null;
	}
	
	protected boolean getBoolean(String name){
		return Boolean.parseBoolean(getString(name));
	}
	
	protected int getInteger(String name){
		return Integer.parseInt(getString(name));
	}

	protected long getLong(String name){
		return Long.parseLong(getString(name));
	}

	
	protected <A> A[] getArray(String name, ValueBuilder<A> builder, A[] array) {
		List<A> result = null;
		AnnotationValue val = this.values.get(name);
		if(val != null){
			List<Object> list = (List<Object>)val.getValue();
			if((list != null)&&(list.size() > 0)){
				for (Object ann : list) {
					if(result == null){
						result = new ArrayList<A>();
					}
					result.add(builder.buildAnnotation(ann));
				}
			}
		}
		return result != null ? result.toArray(array) : null;
	}

	 public static <T extends AnnotationAdaptor> T getAnnotationAdaptor(ICodeGenerationContext ctx,Element elem, Class<? extends Annotation> clazz, AnnotationBuilder<T> builder){
	   AnnotationMirror ann = getAnnotation(ctx, elem, clazz);
	   return ann != null ? builder.buildAnnotation(ctx,ann) : null;
	 }
	 
	public static AnnotationMirror getAnnotation(ICodeGenerationContext ctx,Element elem, Class<? extends Annotation> clazz){
		List<? extends AnnotationMirror> anns = ctx.getProcessingEnvironment().getElementUtils().getAllAnnotationMirrors(elem);
		String className = clazz.getCanonicalName();
		if((anns != null)&&(anns.size() > 0)){
			for (AnnotationMirror ann : anns) {
				if(className.equals(ann.getAnnotationType().toString())){
					return ann;
				}
			}
		}
		return null;
	}
	
	protected String trimQuoteMark(String val) {
		if(val == null){
			return null;
		}
		if(val.startsWith("\"")&&val.endsWith("\"")){
			val = val.substring(1,val.length()-1);
		}
		return val;
	}

}