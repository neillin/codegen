package com.thwt.core.codegen.model.vertx;

import java.util.List;

import com.thwt.core.codegen.model.type.ParamInfo;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Signature {

  final String name;
  final List<ParamInfo> params;

  public Signature(String name, List<ParamInfo> params) {
    this.name = name;
    this.params = params;
  }

  public String getName() {
    return name;
  }

  public List<ParamInfo> getParams() {
    return params;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Signature) {
      Signature that = (Signature) o;
      return name.equals(that.name) && params.equals(that.params);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ params.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append('(');
    if (params.size() > 0) {
      for (int i = 0;i < params.size();i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(params.get(i).getType().getName()).append(" ").append(params.get(i).getName());
      }
    }
    sb.append(')');
    return sb.toString();
  }
}
