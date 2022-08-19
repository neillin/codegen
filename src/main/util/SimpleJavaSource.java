/**
 * 
 */
package com.thwt.core.codegen.util;

import com.thwt.core.codegen.JavaSource;
import com.thwt.core.codegen.model.FileLocation;

/**
 * @author Neil Lin
 *
 */
public class SimpleJavaSource implements JavaSource {


	private String sourceFQN;
	private Object contentObject;
	private FileLocation fileLocation;
	private boolean resource;
	
	public SimpleJavaSource(String sourceFQN, FileLocation location,
			Object contentObject) {
		this.sourceFQN = sourceFQN;
		this.fileLocation = location;
		this.contentObject = contentObject;
	}


	public SimpleJavaSource() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.wxxr.mobile.core.tools.JavaSource#getSourceFQN()
	 */
	@Override
	public String getSourceFQN() {
		return this.sourceFQN;
	}

	/* (non-Javadoc)
	 * @see com.wxxr.mobile.core.tools.JavaSource#getCompilationUnit()
	 */
	@Override
	public Object getContentObject() {
		return this.contentObject;
	}


	public void setSourceFQN(String sourceFQN) {
		this.sourceFQN = sourceFQN;
	}


	public void setContentObject(Object contentObject) {
		this.contentObject = contentObject;
	}


	/**
	 * @return the fileLocation
	 */
	public FileLocation getFileLocation() {
		return fileLocation;
	}


	/**
	 * @param fileLocation the fileLocation to set
	 */
	public void setFileLocation(FileLocation fileLocation) {
		this.fileLocation = fileLocation;
	}


	/**
	 * @return the resource
	 */
	public boolean isResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public void setResource(boolean resource) {
		this.resource = resource;
	}

}
