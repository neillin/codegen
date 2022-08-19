/**
 * 
 */
package com.thwt.core.codegen.model;

import java.util.List;

/**
 * @author Neil Lin
 *
 */
public interface ImportManager {
	/**
	 * Import a static field or method.
	 * @param name The static member name, including the full class name,
	 *     to be imported
	 */
  String importStatic(String name);

	/**
	 * Import a class.
	 * @param name The full class name of the class to be imported
	 */
  String importClass(String name);

	/**
	 * Import all the classes in a package.
	 * @param packageName The package name to be imported
	 */
  ImportManager importPackage(String packageName);

	/**
	 * Resolve a class name.
	 *
	 * @param name The name of the class (without package name) to be resolved.
	 * @return  If the class has been imported previously, with
	 *     {@link #importClass} or {@link #importPackage}, then its
	 *     Class instance. Otherwise <code>null</code>.
	 * @throws ELException if the class is abstract or is an interface, or
	 *     not public.
	 */
	String resolveClass(String name);

	/**
	 * Resolve a static field or method name.
	 *
	 * @param name The name of the member(without package and class name)
	 *    to be resolved.
	 * @return  If the field or method  has been imported previously, with
	 *     {@link #importStatic}, then the class object representing the class that
	 *     declares the static field or method.
	 *     Otherwise <code>null</code>.
	 * @throws ELException if the class is not public, or is abstract or
	 *     is an interface.
	 */
	String resolveStatic(String name);
	
	List<String> getImports();
	
	List<String> getPackageImports();

}
