package com.thwt.core.codegen;

import com.google.common.base.Throwables;
import com.thwt.core.codegen.util.StringEscapeUtils;

class JavaRepresenterImpl implements JavaRepresenter {

	@Override
	public String getJavaRepresentation(Object value) {
		if (value instanceof String) {
			return getStringRepresentation((String) value);
		} else if (value instanceof Class<?>) {
			return getClassRepresentation((Class<?>) value);
		} else {
			return String.valueOf(value);
		}
	}

	private String getClassRepresentation(Class<?> clazz) {
		return clazz.getSimpleName() + ".class";
	}

	private String getStringRepresentation(String value) {
		try {
      return '"' + StringEscapeUtils.escapeJava(value) + '"';
    } catch (Throwable e) {
      throw Throwables.propagate(e);
    }
	}

}
