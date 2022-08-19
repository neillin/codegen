package com.thwt.core.codegen.model.vertx;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

public interface AnnotatedType extends AnnotatedElement {

    /**
     * Returns the underlying type that this annotated type represents.
     *
     * @return the type this annotated type represents
     */
    public Type getType();
}
