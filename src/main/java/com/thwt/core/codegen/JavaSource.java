/**
 * 
 */
package com.thwt.core.codegen;

import com.thwt.core.codegen.model.FileLocation;

/**
 * @author Neil Lin
 *
 */
public interface JavaSource {
	String getSourceFQN();		// full qualified name
	Object getContentObject();
	FileLocation getFileLocation();
	boolean isResource();
}
