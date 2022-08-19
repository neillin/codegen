package com.thwt.core.codegen.model.type;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EnumTypeInfo extends ClassTypeInfo {

  final List<String> values;
  final boolean gen;

  public EnumTypeInfo(String fqcn, boolean gen, List<String> values, boolean proxyGen) {
    super(ClassKind.ENUM, fqcn, Collections.<TypeParamInfo.Class>emptyList());

    this.gen = gen;
    this.values = values;
  }

  /**
   * @return true if the type is a generated type
   */
  public boolean isGen() {
    return gen;
  }

  /**
   * @return the enum possible values
   */
  public List<String> getValues() {
    return values;
  }
  
  @Override
  public String getTypescriptName() {
  	return "string";
  }
  
  @Override
  public String getDartName() {
  	return "String";
  }
}
