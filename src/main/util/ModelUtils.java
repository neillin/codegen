/**
 * 
 */
package com.thwt.core.codegen.util;

import static com.thwt.core.codegen.util.Utils.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.JavaSourceCodeFormatter;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.ClassModel;
import com.thwt.core.codegen.model.ClassModelImpl;
import com.thwt.core.codegen.model.FieldModel;
import com.thwt.core.codegen.model.ImportManager;
import com.thwt.core.codegen.model.MethodModel;
import com.thwt.core.codegen.model.MethodModelImpl;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.TypeInfo;


/**
 * @author neillin
 *
 */
@SuppressWarnings({ "unused"})
public abstract class ModelUtils {
	
	public static interface FieldHandler {
		boolean processField(ClassModel model, Element element);
	}
	
	public static interface MethodHandler {
		boolean processMethod(ClassModel model, ExecutableElement element);
	}

	private static final Logger log = LoggerFactory.getLogger(ModelUtils.class);


	
	public static String calculateClassChecksum(ClassModel model) throws Exception {
		ChecksumBuilder builder = new ChecksumBuilder();
		model.checksum(builder);
		byte[] data = builder.getDigest();
		String newSum = data != null && data.length > 0 ?  BaseEncoding.base64().encode(Hashing.md5().hashBytes(data).asBytes()) : null;
		return newSum;
	}

	public static ClassModelImpl collectionBasicClassInfo(ICodeGenerationContext context, TypeElement typeElem) {
	  ClassModelImpl model = new ClassModelImpl(typeElem);
	  collectionBasicClassInfo(context, typeElem, model, true, true);
	  return model;
	}

	public static void collectionBasicClassInfo(
			ICodeGenerationContext context, TypeElement typeElem, ClassModelImpl model, boolean includeFields, boolean includeMethods) {
		FieldHandler fHandler = includeFields ? new FieldHandler() {
			
			@Override
			public boolean processField(ClassModel model, Element element) {
				return false;
			}
		} : null;
		MethodHandler mHandler = includeMethods ? new MethodHandler() {
			
			@Override
			public boolean processMethod(ClassModel model,
					ExecutableElement element) {
				return false;
			}
		} : null;
		collectionBasicClassInfo(context, typeElem, model, fHandler, mHandler);
	}
	
	public static ICodeGenerationContext getCodeGenerationContext() {
	  return MainAnnotationProcessor.getCurrentContext();
	}
	
	/**
	 * @param context
	 * @param typeElem
	 * @param ann
	 * @param pkg
	 * @param model
	 * @return
	 */
	public static void collectionBasicClassInfo(
			ICodeGenerationContext context, TypeElement typeElem, ClassModelImpl model, FieldHandler fieldHandler, MethodHandler methodHandler) {
		TreePath treePath = context.getTrees().getPath(typeElem);
		List<? extends ImportTree> imports = treePath.getCompilationUnit().getImports();
		if(imports != null){
			for (ImportTree importTree : imports) {
				String importClass = importTree.getQualifiedIdentifier().toString();
				if(importTree.isStatic()) {
				  model.getImportManager().importStatic(importClass);
				}else if(importClass.endsWith(".*")){
				  model.getImportManager().importPackage(importClass.substring(0, importClass.length()-2));
				}else{
				  model.importClass(importClass);
				}
			}
		}
		if(fieldHandler != null||methodHandler != null){
			List<? extends Element> children = context.getProcessingEnvironment().getElementUtils().getAllMembers(typeElem); //typeElem.getEnclosedElements();
			if(children != null){
				for (Element child : children) {
					try {
					  switch(child.getKind()){
					    case FIELD:
					      if(fieldHandler != null){
					        if(!fieldHandler.processField(model, child)){
					          FieldModel field = createSimpleFieldModel(context,model, child);
					          model.addField(field);
					        }
					      }
					      break;
					    case METHOD:
					      if(methodHandler != null) {
					        if(!methodHandler.processMethod(model,(ExecutableElement)child)){
					          addSimpleMethod(context,model, child);
					        }
					      }
					      break;
					    case CONSTRUCTOR:
					      addConstructor(context,model, child);
					      break;
					    default:
					      break;
					  }
					}catch(Exception e) {
					  context.reportException(e, child);
					}
				}
			}

		}
	}

	public static MethodModel createInitMethod(ICodeGenerationContext context,ClassModel model){
		Types typeUtil = context.getProcessingEnvironment().getTypeUtils();
		Elements elemUtil = context.getProcessingEnvironment().getElementUtils();
		MethodModelImpl m = new MethodModelImpl(model);
		m.setMethodName("init");
		m.setModifiers(Sets.newHashSet(Modifier.PRIVATE));
		m.setReturnType("void");
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("model", model);
		String javaStatement = context.getTemplateRenderer().renderMacro("init", attrs, null);
		m.setJavaStatement(javaStatement);
		return m;
	}
	
	
	public static JCMethodDecl parseMethod(ICodeGenerationContext context,String statement) {
		JCCompilationUnit unit = ParserFactory.instance(context.getJavacContext()).newParser(new StringBuffer("public class Temp {").append(statement).append("}"), true, true, true).parseCompilationUnit();
		final JCMethodDecl[] result = new JCMethodDecl[1];
		unit.accept(new com.sun.tools.javac.tree.TreeScanner() {

			/* (non-Javadoc)
			 * @see com.sun.tools.javac.tree.JCTree.Visitor#visitMethodDef(com.sun.tools.javac.tree.JCTree.JCMethodDecl)
			 */
			@Override
			public void visitMethodDef(JCMethodDecl m) {
				result[0] = m;
			}
		});
		return result[0];
	}

	
	public static void createFieldAccessors(ICodeGenerationContext context,ClassModel model){
		Types typeUtil = context.getProcessingEnvironment().getTypeUtils();
		Elements elemUtil = context.getProcessingEnvironment().getElementUtils();
		List<FieldModel> fields = model.getFields();
		if((fields != null)&&(fields.size() > 0)){
			for (FieldModel field : fields) {
				generateSetterMethod(context, field);
				generateGetterMethod(context, field);
			}
		}
	}



	/**
	 * @param context
	 * @param field
	 */
	public static MethodModel generateSetterMethod(ICodeGenerationContext context,
			FieldModel field) {
		MethodModelImpl m = new MethodModelImpl(field.getClassModel());
		m.setMethodName("set"+capitalize(field.getName()));
		m.setModifiers(Sets.newHashSet(Modifier.PUBLIC));
		m.setParameters(new String[]{ field.getType()}, new String[]{ field.getName()});
		m.setReturnType("void");
		m.setGenerated(true);
		if(!field.getClassModel().hasMethod(m)){
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("model", field);
			String javaStatement = context.getTemplateRenderer().renderMacro("setter", attrs, null);
			m.setJavaStatement(javaStatement);
			ClassModel classModel = field.getClassModel();
			if(classModel instanceof ClassModelImpl) {
			  ((ClassModelImpl)classModel).addMethod(m);
			}
		}
		return m;
	}
	
	/**
	 * @param context
	 * @param field
	 */
	public static MethodModel generateGetterMethod(ICodeGenerationContext context,
			FieldModel field) {
		MethodModelImpl m = new MethodModelImpl(field.getClassModel());
		m.setMethodName("get"+capitalize(field.getName()));
		m.setModifiers(Sets.newHashSet(Modifier.PUBLIC));
		m.setReturnType(field.getType());
		m.setGenerated(true);
		if(!field.getClassModel().hasMethod(m)){
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("model", field);
			String javaStatement = context.getTemplateRenderer().renderMacro("getter", attrs, null);
			m.setJavaStatement(javaStatement);
			ClassModel classModel = field.getClassModel();
      if(classModel instanceof ClassModelImpl) {
        ((ClassModelImpl)classModel).addMethod(m);
      }
		}
		return m;
	}


	
	public static boolean isPrimitiveType(TypeMirror type){
		TypeKind kind = type.getKind();
		return (kind == TypeKind.BOOLEAN)||
				(kind == TypeKind.BYTE)||
				(kind == TypeKind.CHAR)||
				(kind == TypeKind.DOUBLE)||
				(kind == TypeKind.FLOAT)||
				(kind == TypeKind.INT)||
				(kind == TypeKind.SHORT)||
				(kind == TypeKind.LONG);
	}

	public static FieldModel collectBasicFieldModel(FieldModel model,Element elem){
	  return collectBasicFieldModel(MainAnnotationProcessor.getCurrentContext(), model, elem);
	}
	
	public static FieldModel collectBasicFieldModel(ICodeGenerationContext context,FieldModel model,Element elem){
		model.setName(elem.getSimpleName().toString());
		model.setType(elem.asType().toString());
		model.setModifiers(elem.getModifiers());
		VariableTree variableTree = (VariableTree)context.getTrees().getTree(elem);
		if(variableTree != null && variableTree.getInitializer() != null){
			model.setInitializer(variableTree.getInitializer().toString());
		}
		return model;
	}

	public static FieldModel createSimpleFieldModel(ICodeGenerationContext context,ClassModel classModel, Element elem){
		return new FieldModel(classModel,elem);
	}
	
	public static FieldModel createSimpleFieldModel(ICodeGenerationContext context,ClassModel classModel,String name, String type, String initializer){
	  FieldModel model = new FieldModel(classModel);
	  model.setName(name);
	  model.setType(type);
	  model.setInitializer(initializer);
	  return model;
	}


	
	
	private static List<? extends TypeMirror> getParameterTypesOfConvertor(ICodeGenerationContext context,DeclaredType type){
//		log.info("Convertor field type :["+type.getClass()+"]/"+type);
//		if(context.getProcessingEnvironment().getTypeUtils().isSubtype(type, interfaceType) == false){
//			return null;
//		}
		List<? extends TypeMirror> types = type.getTypeArguments();
		if((types != null)&&(types.size() == 2)){
			return types;
		}
		List<? extends TypeMirror> superTypes = context.getProcessingEnvironment().getTypeUtils().directSupertypes(type);
		if(superTypes != null){
			for (TypeMirror typeMirror : superTypes) {
				if(typeMirror instanceof DeclaredType){
					types = getParameterTypesOfConvertor(context, (DeclaredType)typeMirror);
					if(types != null){
						return types;
					}
				}
			}
		}
		return null;
	}


	/**
	 * @param context
	 * @param elem
	 */
	protected static boolean isFieldAnnotationApplyable(ICodeGenerationContext context,
			Element elem) {
		return isFieldTypeOfDataField(context, elem) == false;
	}

	/**
	 * @param context
	 * @param elem
	 * @return
	 */
	public static boolean isFieldTypeOfDataField(
			ICodeGenerationContext context, Element elem) {
		Types types = context.getProcessingEnvironment().getTypeUtils();
		Elements elements = context.getProcessingEnvironment().getElementUtils();
		TypeElement dataFieldType = elements.getTypeElement("com.wxxr.mobile.core.ui.api.IDataField");
		if(types.isAssignable(elem.asType(), dataFieldType.asType())){
			return true;
		}
		return false;
	}
	
	
	public static String getELExpression(String expression) {
		if((expression.startsWith("${")||expression.startsWith("#{"))&&expression.endsWith("}")){
			return expression.substring(2, expression.length()-1);
		}
		return null;
	}
	
	public static MethodModel addSimpleMethod(ICodeGenerationContext context,ClassModelImpl model, Element elem){
	  MethodModel m = createSimpleMethod(context, model, elem);
	  model.addMethod(m);
	  log.debug("Found method :[{}], method key :[{}]",m.generateMethodSignature(), m.getMethodKey());
	  return m;
	}

	
	public static MethodModel createSimpleMethod(ICodeGenerationContext context,ClassModel model, Element elem){
		if(elem.getKind() != ElementKind.METHOD){
			return null;
		}
		ExecutableElement exec = (ExecutableElement)elem;
		MethodModelImpl m = new MethodModelImpl(model,exec);
		m.setMethodBody(generateMethodBody(context, exec));
		return m;
	}
	
	public static MethodModel addConstructor(ICodeGenerationContext context,ClassModelImpl model, Element elem){
		if(elem.getKind() != ElementKind.CONSTRUCTOR){
			return null;
		}
		ExecutableElement exec = (ExecutableElement)elem;
		MethodModelImpl m = new MethodModelImpl(model, exec);
		m.setConstructor(true);
		m.setMethodBody(generateMethodBody(context, exec));
		return m;
	}
	
	private static class ThreadAssignHolder {
		private ThreadLocal<Stack<List<String>>> tlocal = new ThreadLocal<Stack<List<String>>>(){

			@Override
			protected Stack<List<String>> initialValue() {
				return new Stack<List<String>>();
			}
			
		};
		
		public void push() {
			tlocal.get().push(new ArrayList<String>());
		}
		
		public List<String> pop() {
			if(tlocal.get().size() > 0){
				return tlocal.get().pop();
			}
			return null;
		}
		
		public List<String> current() {
			if(tlocal.get().size() > 0){
				return tlocal.get().peek();
			}
			return null;
		}

		
		public void addBean(String name){
			List<String> list = current();
			if((list != null)&&(!list.contains(name))){
				list.add(name);
			}
		}
		
	}
	
  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#generateMethodSignature()
   */
  public static String generateMethodSignature(MethodModel model, boolean simpleTypeName) {
    return generateMethodSignature(model, false, false);
  }

  private static  String simplifyTypeName(ImportManager importMgr, String typeName) {
    return importMgr != null ? importMgr.importClass(typeName) : typeName;
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MethodModel#generateMethodSignature(boolean)
   */
  public static String generateMethodSignature(MethodModel model,boolean simpleTypeName, boolean finalFlag) {
    ClassModel classModel = model.getClassModel();
    ImportManager importMgr = simpleTypeName && classModel != null ? classModel.getImportManager() : null;
    StringBuffer buf = new StringBuffer();
    String mods = model.getModifiers();
    if(mods != null){
      buf.append(mods).append(' ');
    }
    if(model.isConstructor()){
      buf.append(model.getClassModel().getName()).append('(');
    }else{
      String[] typeVars = model.getTypeVariables();
      if(typeVars != null && typeVars.length > 0) {
        buf.append('<').append(Joiner.on(',').join(typeVars)).append("> ");
      }
      String returnType = model.getReturnType();
      buf.append(returnType != null ? simplifyTypeName(importMgr,returnType) : "void").append(' ');
      buf.append(model.getName()).append('(');
    }
    String[] parameterTypes = model.getParameterTypes();
    String[] parameterNames = model.getParameterNames();
    int size = parameterTypes != null ? parameterTypes.length : 0;
    if(size > 0){
      for(int i=0 ; i < size ; i++){
        if(i > 0){
          buf.append(',');
        }
        if(finalFlag){
          buf.append("final ");
        }
        buf.append(simplifyTypeName(importMgr,parameterTypes[i]));
        if(i == (size -1)){
          if(model.isVarArgs()){
            buf.append("...");
          }
        }
        buf.append(' ').append(parameterNames[i]);
      }
    }
    buf.append(')');
    String[] thrownTypes = model.getThrownTypes();
    size = thrownTypes != null ? thrownTypes.length : 0;
    if(size > 0){
      buf.append(" throws ");
      int cnt = 0;
      for (int i = 0; i < size; i++) {
        if(cnt > 0){
          buf.append(',');
        }
        String exception = thrownTypes[i];
        if(exception != null){
          buf.append(simplifyTypeName(importMgr, exception));
          cnt++;
        }
      }
    }
    return buf.toString();
  }

		
	/**
	 * @param context
	 * @param exec
	 * @return
	 */
	public static String generateMethodBody(ExecutableElement exec) {
	  return generateMethodBody(MainAnnotationProcessor.getCurrentContext(), exec);
	}
	  /**
	   * @param context
	   * @param exec
	   * @return
	   */
	  public static String generateMethodBody(ICodeGenerationContext context,
	      ExecutableElement exec) {
//		m.setMethodBody(context.getTrees().getTree(exec).getBody().toString());
		StringWriter sw = new StringWriter();
		JCTree block = null;
		try {
			block = (JCTree)context.getTrees().getTree(exec).getBody();
		}catch(NullPointerException e) {
		}
		if(block == null) {
			return null;
		}
		block.accept(new JavaSourceCodeFormatter(sw, true)
  		{
  			private ThreadAssignHolder holder = new ThreadAssignHolder();
  			
  			@Override
  			public void visitExec(JCExpressionStatement tree) {
  				holder.push();
  				try {
  					super.visitExec(tree);
  				}finally{
  					List<String> list = holder.pop();
  					if((list != null)&&(list.size() > 0)){
  						for (String name : list) {
  							try {
  								print("updateBean(\""+name+"\" , "+name+");");
  								println();
  							} catch (IOException e) {
  								e.printStackTrace();
  							}
  						}
  					}
  				}
  			}
  
  			@Override
  			public void visitAssign(JCAssign tree) {
  				
  				super.visitAssign(tree);
  			}
  			
  		}
		);
		try {
			sw.close();
		} catch (IOException e) {
		}
		return sw.toString();
	}


	public static String getElementText(ICodeGenerationContext context, Element elem){
		StringWriter sw = new StringWriter();
		context.getProcessingEnvironment().getElementUtils().printElements(sw, elem);
		try {
			sw.close();
		} catch (IOException e) {
		}
		return sw.toString();
	}
	
	public static boolean isElExpression(String expr) {
		expr = trimToNull(expr);
		 return expr != null && (expr.startsWith("${")||expr.startsWith("#{")) && expr.endsWith("}");
	}
	
	
	public static TypeElement getSuperClassElement(TypeElement elem) {
		if(elem == null) {
			return null;
		}
		TypeMirror type = elem.getSuperclass();
		if(type.getKind() == TypeKind.NONE) {
			return null;
		}
		return (TypeElement)getCodeGenerationContext().getProcessingEnvironment().getTypeUtils().asElement(type);
	}

	
//	public static String getElementSource(ICodeGenerationContext context, Element elem){
//		StringWriter sw = new StringWriter();
//		((JCTree)context.getTrees().getTree(elem)).accept(new JavaSourceCodeFormatter(sw, true){
//
//			@Override
//			public void visitExec(JCExpressionStatement tree) {
//				super.visitExec(tree);
//			}
//
//			@Override
//			public void visitAssign(JCAssign tree) {
//				super.visitAssign(tree);
//			}
//			
//		});
////		context.getProcessingEnvironment().getElementUtils().printElements(sw, elem);
//		try {
//			sw.close();
//		} catch (IOException e) {
//		}
//		return sw.toString();
//	}
//
	
	public static boolean isReactiveValueType(TypeInfo typeInfo) {
		String returnType = typeInfo.getName();
		return returnType.startsWith(Single.class.getName()) || returnType.startsWith(Maybe.class.getName())
				|| returnType.startsWith(Completable.class.getName())
				|| returnType.startsWith(Observable.class.getName()) || returnType.startsWith(Flowable.class.getName());
	}

	public static String getMethodReturnType(TypeInfo returnType) {
		if (isReactiveValueType(returnType)) {
			if(returnType instanceof ParameterizedTypeInfo) {
				ParameterizedTypeInfo pTypeInfo = (ParameterizedTypeInfo) returnType;
				return pTypeInfo.getArg(0).getName();
			}
			if(returnType.getName().startsWith(Completable.class.getName())) {
				return Void.class.getName();
			}
		}
		return returnType.getName();
	}

	public static String getMethodReturnBoxedType(TypeInfo returnType) {
		String type = getMethodReturnType(returnType);
		return ClassTypeInfo.getBoxedType(type);
	}

	public static String getCommandName(String cmdClass) {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		TypeElement elem = elems.getTypeElement(cmdClass);
		CMD ann = elem.getAnnotation(CMD.class);
		if (ann == null) {
			throw new MustFailedCodeGenException("Command was not annotated with @CMD :" + cmdClass);
		}
		String catalog = Strings.emptyToNull(ann.catalog().trim());
		return (ann.secure() ? AbstractCommand.SECURE_COMMAND_CHANNEL_PREFIX
				: AbstractCommand.NO_SECURE_COMMAND_CHANNEL_PREFIX) + (catalog != null ? catalog + "." : "")
				+ elem.getSimpleName();

	}

	public static String getCommandReturnValueType(TypeInfo cmdType) throws CodeGenException {
		TypeMirror mirror = getCommandReturnValueTypeMirroe(cmdType);
		return mirror != null ? mirror.toString() : null;
	}
	
	public static TypeMirror getCommandReturnValueTypeMirroe(TypeInfo cmdType) throws CodeGenException {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		DeclaredType elem = getAbstractCommandElement(elems.getTypeElement(cmdType.getName()));
		return elem != null ? elem.getTypeArguments().get(0) : null;
	}
	
	public static String getCommandClassFromHandler(TypeInfo cmdType) throws CodeGenException {
		TypeMirror type = getCommandClassFromHandlerMirror(cmdType);
		return type != null ? type.toString() : null;
	}
	
	public static TypeMirror getCommandClassFromHandlerMirror(TypeInfo cmdType) throws CodeGenException {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		TypeElement elem = elems.getTypeElement(cmdType.getName());
		TypeMirror type = getAbstractCommandHandlerElement(elems.getTypeElement(cmdType.getName()));
		if(type == null) {
			type = getCommandHandlerElement(elem);
		}
		return type != null ? ((DeclaredType)type).getTypeArguments().get(1) : null;
	}


	public static DeclaredType getAbstractCommandElement(TypeElement elem) {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		DeclaredType superType = (DeclaredType)elem.getSuperclass();
		if(superType == null) {
			return null;
		}
		if(AbstractCommand.class.getName().equals(superType.asElement().toString())) {
			return superType;
		}
		if(Object.class.getName().equals(superType.toString())) {
			return null;
		}
		return getAbstractCommandElement(elems.getTypeElement(superType.toString()));
	}
	
	public static TypeMirror getAbstractCommandHandlerElement(TypeElement elem) {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		DeclaredType superType = (DeclaredType)elem.getSuperclass();
		if(superType == null) {
			return null;
		}
		if(AbstractCommandHandler.class.getName().equals(superType.asElement().toString())) {
			return superType;
		}
		if(Object.class.getName().equals(superType.toString())) {
			return null;
		}
		return getAbstractCommandHandlerElement(elems.getTypeElement(superType.toString()));
	}
	
	public static TypeMirror getCommandHandlerElement(TypeElement elem) {
		ICodeGenerationContext ctx = MainAnnotationProcessor.getCurrentContext();
		Elements elems = ctx.getProcessingEnvironment().getElementUtils();
		TypeMirror result = null;
		for(TypeMirror type : elem.getInterfaces()) {
			if(type.toString().startsWith(com.thwt.core.command.api.CommandHandler.class.getName())) {
				result = type;
				break;
			}else {
				result = getCommandHandlerElement(elems.getTypeElement(type.toString()));
				if(result != null) {
					break;
				}
			}
		}
		return result;
	}

}
