/*
 * @(#)InvocationMode.java	 2017-02-20
 *
 * Copyright 2006-2017 ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

/**
 * @author Neil Lin
 *
 */
public enum InvocationMode {
  Injection,
  OnStart,
  OnStop,
  OnServerReady,
  ServiceHandler,
  MessageHandler,
  CommandHandler
}
