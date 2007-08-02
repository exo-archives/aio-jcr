package org.exoplatform.services.rest;

/**
 * An utility class for manipulating "parametrized" strings. 
 * A parametrized is a string constructed by substituting placeholders
 * in the pattern string where pattern string is any string containing 
 * placeholders in the form of "{placeholderName}". 
 * @author Alexander Tereshkin
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIPattern {

  private static Pattern paramPattern = Pattern.compile("\\{[^\\}^\\{]*\\}");
  private String[]       tokens;
  private String[]       paramNames;
  private String         pattern;
  private Set < String > params;

  /**
   * Creates a new instance of ParametrizedStringParser.
   */
  public URIPattern(String patternString) {
    tokens = paramPattern.split(patternString, -1);
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].length() == 0) {
        throw new IllegalArgumentException("Invalid pattern:" + patternString);
      }
    }
    int numParams = tokens.length - 1;
    paramNames = new String[numParams];
    Matcher matcher = paramPattern.matcher(patternString);
    for (int i = 0; matcher.find(); i++) {
      paramNames[i] = patternString.substring(matcher.start() + 1, matcher.end() - 1);
    }
    if (numParams > 0) {
      assert paramNames[numParams - 1] != null;
    }
    this.pattern = patternString;
    params = Collections.unmodifiableSet(new HashSet(Arrays.asList(paramNames)));
  }

  /**
   * Returns a <code>Set</code> of names of parameters (placeholders)
   * participating in the underlying pattern string.
   * @return <code>Set</code> of string parameters
   */
  public Set < String > getParamNames() {
    return params;
  }

  /**
   * Parses the given string against the underlying pattern and returns a
   * <code>Map</code> of parameter names to values.
   * @param string string to parse
   * @return <code>Map</code> of parameter names to values
   * @throws IllegalArgumentException if the string doesn't match the pattern
   */
  public Map < String, String > parse(String string) {
    if (string == null) {
      throw new NullPointerException();
    }
    Map < String, String > ret = new HashMap < String, String >();
    if (paramNames.length == 0) {
      if (string.equals(pattern)) {
        return ret;
      }
      throw new IllegalArgumentException("Pattern not matched: " + pattern + "," + string);
    }
    if (!matches(string)) {
      throw new IllegalArgumentException("Pattern not matched: " + pattern + "," + string);
    }
    int pos = tokens[0].length();
    for (int i = 0; i < paramNames.length; i++) {
      int nextPos = tokens[i + 1].length() > 0 ? string.indexOf(tokens[i + 1], pos) : string.length();
      if (nextPos < 0) {
        throw new IllegalArgumentException("Pattern not matched: " + pattern + "," + string);
      }
      if (i == paramNames.length - 1
          && tokens[i + 1].equals("/")) {
        ret.put(paramNames[i], string.substring(pos, string.length() - 1));
      } else {
        ret.put(paramNames[i], string.substring(pos, nextPos));
      }
      pos = nextPos + tokens[i + 1].length();
    }
    return ret;
  }

  // /**
  // * Returns the result of substituting the placeholders in the
  // * pattern with given parameter values.
  // * @param paramValues map of parameter names to values
  // * @return substituted string
  // */
  // public String substitute(Map<String, String> paramValues) {
  // StringBuffer sb = new StringBuffer();
  // for (int i = 0; i < tokens.length; i++) {
  // sb.append(tokens[i]);
  // if (i < tokens.length - 1) {
  // sb.append(paramValues.get(paramNames[i]));
  // }
  // }
  // return sb.toString();
  // }

  /**
   * Checks wheter the given string matches the underlying pattern.
   * @param string string to test
   * @return true if the string matches the pattern false otherwise
   */
  public boolean matches(String string) {
    if (string.indexOf(tokens[0], 0) < 0) {
      return false;
    }
    int pos = tokens[0].length();
    for (int i = 0; i < paramNames.length; i++) {
      int nextPos = (tokens[i + 1].length() > 0) ? string.indexOf(tokens[i + 1], pos) : string.length();
      if (nextPos < 0) {
        return false;
      }
      pos = nextPos + tokens[i + 1].length();
    }
    if (string.lastIndexOf(tokens[tokens.length - 1], string.length()) < 0) {
      return false;
    }
    return true;
  }

  /**
   * check is two URIPattern matches.
   * @param another the another URIPattern
   * @return the result of comparison
   */
  public boolean matches(URIPattern another) {
    int minSize = (tokens.length <= another.getTokens().length) ? tokens.length : another
        .getTokens().length;
    for (int i = 0; i < minSize; i++) {
      if (!tokens[i].equals(another.getTokens()[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the underlying pattern string.
   * @return the underlying pattern string
   */
  public String getString() {
    return pattern;
  }

  private String[] getTokens() {
    return tokens;
  }
}
