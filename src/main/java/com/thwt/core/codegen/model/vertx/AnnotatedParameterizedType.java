package com.thwt.core.codegen.model.vertx;
public interface AnnotatedParameterizedType extends AnnotatedType {

    /**
     * Returns the potentially annotated actual type arguments of this parameterized type.
     *
     * @return the potentially annotated actual type arguments of this parameterized type
     */
    AnnotatedType[] getAnnotatedActualTypeArguments();
}
