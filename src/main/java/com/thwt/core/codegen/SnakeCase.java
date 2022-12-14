package com.thwt.core.codegen;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SnakeCase extends Case {

  @Override
  public String name() {
    return "SNAKE";
  }

  @Override
  public String format(Iterable<String> atoms) {
    StringBuilder sb = new StringBuilder();
    for (String atom : atoms) {
      if (atom.length() > 0) {
        if (sb.length() > 0) {
          sb.append('_');
        }
        sb.append(atom.toLowerCase());
      }
    }
    return sb.toString();
  }
  private final Pattern validator = Pattern.compile("(?:\\p{Alnum}|(?:(?<=\\p{Alnum})_(?=\\p{Alnum})))*");
  @Override
  public List<String> parse(String name) {
    if (!validator.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid snake case:" + name);
    }
    return split(name, "_");
  }
}
