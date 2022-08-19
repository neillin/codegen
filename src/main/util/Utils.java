/**
 * 
 */
package com.thwt.core.codegen.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.function.Consumer;

import com.google.common.base.Strings;

/**
 * @author Neil Lin
 *
 */
public abstract class Utils {

  public static String trim2Null(String s) {
    return Strings.emptyToNull(Strings.nullToEmpty(s).trim());
  }

  public static String trimToNull(String s) {
    return Strings.emptyToNull(Strings.nullToEmpty(s).trim());
  }

  public static boolean isBlank(String s) {
    return Strings.isNullOrEmpty(Strings.nullToEmpty(s).trim());
  }

  /**
   * <p>Capitalizes a String changing the first letter to title case as
   * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
   *
   * <p>For a word based algorithm, see {@link WordUtils#capitalize(String)}.
   * A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.capitalize(null)  = null
   * StringUtils.capitalize("")    = ""
   * StringUtils.capitalize("cat") = "Cat"
   * StringUtils.capitalize("cAt") = "CAt"
   * </pre>
   *
   * @param str  the String to capitalize, may be null
   * @return the capitalized String, <code>null</code> if null String input
   * @see WordUtils#capitalize(String)
   * @see #uncapitalize(String)
   * @since 2.0
   */
  public static String capitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    return new StringBuffer(strLen)
        .append(Character.toTitleCase(str.charAt(0)))
        .append(str.substring(1))
        .toString();
  }

  /**
   * Utility method to take a string and convert it to normal Java variable
   * name capitalization.  This normally means converting the first
   * character from upper case to lower case, but in the (unusual) special
   * case when there is more than one character and both the first and
   * second characters are upper case, we leave it alone.
   * <p>
   * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays
   * as "URL".
   *
   * @param  name The string to be decapitalized.
   * @return  The decapitalized version of the string.
   */
  public static String decapitalize(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
        Character.isUpperCase(name.charAt(0))){
      return name;
    }
    char chars[] = name.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }

  /**
   * <p>Uncapitalizes a String changing the first letter to title case as
   * per {@link Character#toLowerCase(char)}. No other letters are changed.</p>
   *
   * <p>For a word based algorithm, see {@link WordUtils#uncapitalize(String)}.
   * A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.uncapitalize(null)  = null
   * StringUtils.uncapitalize("")    = ""
   * StringUtils.uncapitalize("Cat") = "cat"
   * StringUtils.uncapitalize("CAT") = "cAT"
   * </pre>
   *
   * @param str  the String to uncapitalize, may be null
   * @return the uncapitalized String, <code>null</code> if null String input
   * @see WordUtils#uncapitalize(String)
   * @see #capitalize(String)
   * @since 2.0
   */
  public static String uncapitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    return new StringBuffer(strLen)
        .append(Character.toLowerCase(str.charAt(0)))
        .append(str.substring(1))
        .toString();
  }


  public static long methodHash(Method method)
  {
    Class<?>[] parameterTypes = method.getParameterTypes();
    StringBuilder methodDesc = new StringBuilder(method.getName()).append("(");
    for (int j = 0; j < parameterTypes.length; j++)
    {
      methodDesc.append(getTypeString(parameterTypes[j]));
    }
    methodDesc.append(")").append(getTypeString(method.getReturnType()));
    try {
      return createHash(methodDesc.toString());
    } catch (Exception e) {
      throw new RuntimeException("Failed to calculate method hash :["+method+"]", e);
    }
  }

  public static long createHash(String methodDesc)
      throws Exception
  {
    long hash = 0;
    ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(512);
    MessageDigest messagedigest = MessageDigest.getInstance("SHA");
    DataOutputStream dataoutputstream = new DataOutputStream(new DigestOutputStream(bytearrayoutputstream, messagedigest));
    dataoutputstream.writeUTF(methodDesc);
    dataoutputstream.flush();
    byte abyte0[] = messagedigest.digest();
    for (int j = 0; j < Math.min(8, abyte0.length); j++)
      hash += (long) (abyte0[j] & 0xff) << j * 8;
    return hash;

  }

  static String getTypeString(Class<?> cl)
  {
    if (cl == Byte.TYPE)
    {
      return "B";
    }
    else if (cl == Character.TYPE)
    {
      return "C";
    }
    else if (cl == Double.TYPE)
    {
      return "D";
    }
    else if (cl == Float.TYPE)
    {
      return "F";
    }
    else if (cl == Integer.TYPE)
    {
      return "I";
    }
    else if (cl == Long.TYPE)
    {
      return "J";
    }
    else if (cl == Short.TYPE)
    {
      return "S";
    }
    else if (cl == Boolean.TYPE)
    {
      return "Z";
    }
    else if (cl == Void.TYPE)
    {
      return "V";
    }
    else if (cl.isArray())
    {
      return "[" + getTypeString(cl.getComponentType());
    }
    else
    {
      return "L" + cl.getName().replace('.', '/') + ";";
    }
  }


  public static boolean isSameObjects(Object object1, Object object2) {
    if (object1 == object2) {
      return true;
    }
    if ((object1 == null) && (object2 == null)) {
      return true;
    }
    if ((object1 == null) || (object2 == null)) {
      return false;
    }
    return object1.equals(object2);
  }

  public static int toUnsignedInt(byte x) {
    return ((int) x) & 0xff;
  }

  public static long toUnsignedLong(byte x) {
    return ((long) x) & 0xffL;
  }

  public static long toUnsignedLong(int x) {
    return ((long) x) & 0xffffffffL;
  }


  public static <T> void foreach(Iterable<T> itr, Consumer<T> consumer) {
    for (T t : itr) {
      consumer.accept(t);
    }
  }

}
