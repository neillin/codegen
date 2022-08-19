/**
 * 
 */
package com.thwt.core.codegen;

/**
 * @author linzhenwu
 *
 */
public class MustFailedCodeGenException extends CodeGenException {

	private static final long serialVersionUID = 4374526413639096782L;

	public MustFailedCodeGenException() {
		super();
	}

	public MustFailedCodeGenException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MustFailedCodeGenException(String message, Throwable cause) {
		super(message, cause);
	}

	public MustFailedCodeGenException(String message) {
		super(message);
	}

	public MustFailedCodeGenException(Throwable cause) {
		super(cause);
	}

}
