package com.thwt.core.codegen.model.vertx;

import java.util.Map;

import javax.lang.model.element.Element;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:zwlinca@gmail.com">Neil Lin</a>
 */
public interface Model {

  String getKind();

  Element getElement();

  String getFqn();
  
  Map<String, Object> getVars();

//  default Map<String, Object> getVars() {
//    HashMap<String, Object> vars = new HashMap<>();
//    vars.put("helper", new Helper());
//    return vars;
//  }

  ModuleInfo getModule();

}
