/**
 * 
 */
package com.thwt.core.codegen.service;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import com.google.common.base.Strings;
import com.thwt.core.codegen.CodeGenException;
import com.thwt.core.codegen.ICodeGenerationContext;
import com.thwt.core.codegen.MainAnnotationProcessor;
import com.thwt.core.codegen.MustFailedCodeGenException;
import com.thwt.core.codegen.model.type.ClassTypeInfo;
import com.thwt.core.codegen.model.type.ParameterizedTypeInfo;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.command.api.AbstractCommand;
import com.thwt.core.command.api.AbstractCommandHandler;
import com.thwt.core.command.api.CMD;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * @author neillin
 *
 */
public abstract class ModelUtils {
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
