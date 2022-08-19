/**
 * 
 */
package com.thwt.core.codegen.model;

import com.thwt.core.codegen.util.ChecksumBuilder;

/**
 * @author neillin
 *
 */
public interface JavaModel {
	String getName();
	void checksum(ChecksumBuilder builder);
}
