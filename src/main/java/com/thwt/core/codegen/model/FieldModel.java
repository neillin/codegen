/**
 * 
 */
package com.thwt.core.codegen.model;

import static com.thwt.core.codegen.util.Utils.*;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.thwt.core.codegen.model.type.TypeInfo;
import com.thwt.core.codegen.model.type.TypeMirrorFactory;
import com.thwt.core.codegen.util.ChecksumBuilder;
import com.thwt.core.codegen.util.ModelUtils;



/**
 * @author neillin
 *
 */
public class FieldModel implements MemberModel{
  private final ClassModel classModel;
  private String name;
  private TypeInfo type;
  private String initializer;
  private Supplier<String> supplier;
  private String modString;
  private Set<Modifier> modifiers;
  private String javaStatement;
  
  private Element element;
  
  public FieldModel(ClassModel model) {
    this.classModel = Preconditions.checkNotNull(model);
  }
  
  public FieldModel(ClassModel model, Element elem) {
    this.classModel = Preconditions.checkNotNull(model);
    this.element = Preconditions.checkNotNull(elem);
    ModelUtils.collectBasicFieldModel(this, this.element);
    if(this.type != null) {
      this.type.collectImports(this.classModel.getImportManager());
    }
  }
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  /**
   * @return the type
   */
  public String getType() {
    return this.type.getName();
  }

  public String getSimpleType() {
    return this.type.getSimpleName();
  }

  public String getSimpleCanonicalType() {
    return this.type.getSimpleName();
  }


  public String getSimpleBoxedType() {
    return getSimpleBoxedType(getType());
  }

  protected String getSimpleBoxedType(String sType) {
    TypeModel tModel = new TypeModel(sType);
    String type = tModel.getType();
    if(type.indexOf('.') > 0){
      return tModel.getSimpleTypeName(getClassModel());
    }
    if("int".equals(type)){
      return "Integer";
    }else if("boolean".equals(type)){
      return "Boolean";
    }else if("char".equals(type)){
      return "Character";
    }else if("byte".equals(type)){
      return "Byte";
    }else if("long".equals(type)){
      return "Long";
    }else if("double".equals(type)){
      return "Double";
    }else if("float".equals(type)){
      return "Float";
    }else if("short".equals(type)){
      return "Short";
    }
    return null;
  }


  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = TypeMirrorFactory.getInstance().create(TypeModel.getTypeMirror(new TypeModel(type)));
  }
  /**
   * @return the classModel
   */
  public ClassModel getClassModel() {
    return classModel;
  }

  /**
   * @return the initializer
   */
  public String getInitializer() {
    return initializer != null ? initializer : (this.supplier != null ? this.initializer = this.supplier.get() : null);
  }
  /**
   * @param initializer the initializer to set
   */
  public void setInitializer(String initializer) {
    this.initializer = initializer;
  }
  
  /**
   * @param initializer the initializer to set
   */
  public void setInitializer(Supplier<String> initializer) {
    this.supplier = initializer;
  }

  /**
   * @return the modifiers
   */
  public String getModifiers() {
    if(this.modString == null) {
      StringBuffer buf = new StringBuffer();
      if(this.modifiers != null) {
        int cnt = 0;
        for (Modifier mod : this.modifiers) {
          if(cnt > 0){
            buf.append(' ');
          }
          buf.append(mod.toString());
          cnt++;
        }
      }
      this.modString = buf.toString();
    }
    return this.modString;
  }
  /**
   * @param modifiers the modifiers to set
   */
  public void setModifiers(Set<Modifier> mods) {
    this.modifiers = mods;
    this.modString = null;
  }

  public String toString() {
    return getJavaStatement();
  }
  
  public String generateFieldSignature() {
    return getModifiers()+" "+getType()+" "+getName();
  }

  @Override
  public String getJavaStatement() {
    if(this.javaStatement == null){
      StringBuffer buf = new StringBuffer();
      buf.append(this.modifiers != null ? getModifiers() : "private").append(' ');
      buf.append(getSimpleType()).append(' ').append(this.name);
      if(getInitializer() != null){
        buf.append(" = ").append(getInitializer());
      }
      this.javaStatement = buf.append(';').toString();
    }
    return this.javaStatement;
  }

  /**
   * @param javaStatement the javaStatement to set
   */
  public void setJavaStatement(String javaStatement) {
    this.javaStatement = trimToNull(javaStatement);
    if((this.javaStatement != null)&&(!this.javaStatement.endsWith(";"))){
      this.javaStatement = this.javaStatement+";";
    }
  }

  public void checksum(ChecksumBuilder builder) {
    if(this.name != null){
      builder.putString(this.name);
    }
    if(this.type != null){
      builder.putString(this.getType());
    }
    if(this.modifiers != null){
      builder.putString(getModifiers());
    }
    if(this.initializer != null){
      builder.putString(this.initializer);
    }
  }

  @Override
  public boolean isPrivate() {
    return this.modifiers != null && this.modifiers.contains(Modifier.PRIVATE);
  }

  @Override
  public boolean isStatic() {
    return this.modifiers != null && this.modifiers.contains(Modifier.STATIC);
  }

  @Override
  public boolean isPublic() {
    return this.modifiers != null && this.modifiers.contains(Modifier.PUBLIC);
  }

  /* (non-Javadoc)
   * @see com.thwt.core.codegen.model.MemberModel#getJavaElement()
   */
  @Override
  public Element getElement() {
    return this.element;
  }

}
