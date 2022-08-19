/*
 * @(#)CommandHandlerModel.java	2017-12-18
 *
 * Copyright ThoughtWare Technology Corp. 
 * All rights reserved.
 * 
 * PROPRIETARY/CONFIDENTIAL.
 *
 */
package com.thwt.core.codegen.service.model;

import com.thwt.core.codegen.model.ClassModelImpl;

/**
 * @author Neil Lin
 *
 */
public class CommandHandlerModel extends ClassModelImpl {

	private String commandName;
	
	public CommandHandlerModel(String qualifiedName) {
		super(qualifiedName);
	}

	/**
	 * @return the commandName
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * @param commandName the commandName to set
	 */
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	public String getSimpleName() {
		return this.getTypeInfo().format(false);
	}
}
