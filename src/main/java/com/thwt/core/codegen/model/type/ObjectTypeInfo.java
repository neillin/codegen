package com.thwt.core.codegen.model.type;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ObjectTypeInfo extends TypeInfo {

  public static TypeInfo INSTANCE = new ObjectTypeInfo() {
  };

  private ObjectTypeInfo() {
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ObjectTypeInfo;
  }

  @Override
  public String format(boolean qualified) {
    return "java.lang.Object";
  }

  @Override
  public boolean isVoid() {
    return false;
  }
  
  public ClassKind getKind() {
	    return ClassKind.OBJECT;
  }

	/* 
	 * @see com.thwt.core.codegen.model.type.TypeInfo#getTypescriptName()
	 */
	@Override
	public String getTypescriptName() {
		return "any";
	}
	
	@Override
	public String getDartName() {
		return "Object";
	}

}
