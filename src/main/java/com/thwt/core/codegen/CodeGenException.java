/*
 * @(#)CodeGenException.java	 2017-02-13
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen;

/**
 * @author Neil Lin
 *
 */
@SuppressWarnings("serial")
public class CodeGenException extends RuntimeException {

  /**
   * 
   */
  public CodeGenException() {
  }

  /**
   * @param message
   */
  public CodeGenException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public CodeGenException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public CodeGenException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public CodeGenException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
