package com.thwt.core.codegen.model.vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thwt.core.codegen.Case;

/**
 * Translate a type name into a name used in some shims. Name conversion can be sometimes tedious and repetitive, the
 * goal of this is to encapsulate it in a single class and make it easy to use and reuse.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface TypeNameTranslator {
  public static class Helper {
    public static Map<String, Object> vars(String lang) {
      Map<String, Object> vars = new HashMap<>();
      vars.put("TRANSLATOR_HIERARCHICAL", hierarchical(lang));
      vars.put("TRANSLATOR_COMPOSITE", composite(lang));
      return vars;
    }

    static TypeNameTranslator hierarchical(final String lang) {
      return new TypeNameTranslator() {
        
        @Override
        public String translate(ModuleInfo module, String qualifiedName) {
          if (qualifiedName.startsWith(module.getGroupPackage())) {
            return module.getGroupPackage() + "." + lang + qualifiedName.substring(module.getGroupPackage().length(), qualifiedName.length());
          }
          return qualifiedName;
        }
      };
    }

    static TypeNameTranslator composite(final String lang) {
      return new TypeNameTranslator() {

        @Override
        public String translate(ModuleInfo module, String qualifiedName) {
          List<String> def = new ArrayList<>(Case.QUALIFIED.parse(module.getGroupPackage()));
          def.add(lang);
          List<String> abc = Case.KEBAB.parse(module.getName());
          if (abc.get(0).equals("vertx")) {
            // Special case
            if (abc.size() == 1) {
              def.add("core");
            } else {
              for (int i = 1;i < abc.size();i++) {
                def.add(abc.get(i));
              }
            }
          } else {
            def.addAll(abc);
          }
          if (qualifiedName.startsWith(module.getPackageName())) {
            if (qualifiedName.equals(module.getPackageName())) {
            } else {
              String nameInPackage = qualifiedName.substring(module.getPackageName().length() + 1);
              def.addAll(Case.QUALIFIED.parse(nameInPackage));
            }

            return Case.QUALIFIED.format(def);
          }
          return qualifiedName;
        }
      };
    }
  }
  String translate(ModuleInfo module, String qualifiedName);

}
