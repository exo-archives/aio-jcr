/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.config;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.Log;

import org.exoplatform.services.jcr.util.StringNumberParser;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: MappedParametrizedObjectEntry.java 1807 2005-08-28 13:34:58Z geaz $
 */

public abstract class MappedParametrizedObjectEntry {

  protected static final Log LOG = ExoLogger.getLogger("jcr.MappedParametrizedObjectEntry");

  protected String           type;

  protected List<SimpleParameterEntry> parameters;

  public MappedParametrizedObjectEntry() {
    parameters = new ArrayList<SimpleParameterEntry>();
  }

  public MappedParametrizedObjectEntry(String type, List params) {
    this.type = type;
    this.parameters = params;
  }

  public List<SimpleParameterEntry> getParameters() {
    return parameters;
  }

  /**
   * Parse named parameter.
   * 
   * @param name
   *          parameter name
   * @return String
   * @throws RepositoryConfigurationException
   */
  public String getParameterValue(String name) throws RepositoryConfigurationException {
    String value = getParameterValue(name, null);
    if (value == null)
      throw new RepositoryConfigurationException("Parameter " + name + " not found ");
    return value;
  }

  /**
   * Parse named parameter.
   * 
   * @param name
   *          parameter name
   * @param defaultValue
   *          default value
   * @return String
   */
  public String getParameterValue(String name, String defaultValue) {
    String value = defaultValue;
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = parameters.get(i);
      if (p.getName().equals(name)) {
        value = p.getValue();
        break;
      }
    }
    return value;
  }

  /**
   * Parse named parameter as Integer.
   * 
   * @param name
   *          parameter name
   * @param defaultValue
   *          default Integer value
   * @return Integer value
   */
  public Integer getParameterInteger(String name, Integer defaultValue) {
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = parameters.get(i);
      if (p.getName().equals(name)) {
        try {
          return StringNumberParser.parseInt(p.getValue());
        } catch (NumberFormatException e) {
          LOG.warn(name + ": unparseable Integer. " + e);
        }
      }
    }
    return defaultValue;
  }

  /**
   * Parse named parameter as Integer.
   * 
   * @param name
   *          parameter name
   * @return Integer value
   * @throws RepositoryConfigurationException
   */
  public Integer getParameterInteger(String name) throws RepositoryConfigurationException {
    try {
      return StringNumberParser.parseInt(getParameterValue(name));
    } catch (NumberFormatException e) {
      throw new RepositoryConfigurationException(name + ": unparseable Integer. " + e, e);
    }
  }

  /**
   * Parse named parameter as Long.
   * 
   * @param name
   *          parameter name
   * @param defaultValue
   *          default Long value
   * @return Long value
   */
  public Long getParameterLong(String name, Long defaultValue) {
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = parameters.get(i);
      if (p.getName().equals(name)) {
        try {
          return StringNumberParser.parseLong(p.getValue());
        } catch (NumberFormatException e) {
          LOG.warn(name + ": unparseable Long. " + e);
        }
      }
    }
    return defaultValue;
  }

  /**
   * Parse named parameter as Long.
   * 
   * @param name
   *          parameter name
   * @return Long value
   * @throws RepositoryConfigurationException
   */
  public Long getParameterLong(String name) throws RepositoryConfigurationException {
    try {
      return StringNumberParser.parseLong(getParameterValue(name));
    } catch (NumberFormatException e) {
      throw new RepositoryConfigurationException(name + ": unparseable Long. " + e, e);
    }
  }

  /**
   * Parse named parameter using {@link StringNumberParser.parseTime} and return time in
   * milliseconds (Long value).
   * 
   * @param name
   *          parameter name
   * @param defaultValue
   *          default time value
   * @return
   */
  public Long getParameterTime(String name, Long defaultValue) {
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = parameters.get(i);
      if (p.getName().equals(name)) {
        try {
          return StringNumberParser.parseTime(p.getValue());
        } catch (NumberFormatException e) {
          LOG.warn(name + ": unparseable time (as Long). " + e);
        }
      }
    }
    return defaultValue;
  }

  /**
   * Parse named parameter using {@link StringNumberParser.parseTime} and return time in
   * milliseconds (Long value).
   * 
   * @param name
   *          parameter name
   * @return Long value
   * @throws RepositoryConfigurationException
   */
  public Long getParameterTime(String name) throws RepositoryConfigurationException {
    try {
      return StringNumberParser.parseTime(getParameterValue(name));
    } catch (NumberFormatException e) {
      throw new RepositoryConfigurationException(name + ": unparseable time (as Long). " + e, e);
    }
  }

  /**
   * Parse named parameter as Boolean.
   * 
   * @param name
   *          parameter name
   * @param defaultValue
   *          default value
   * @return boolean value
   */
  public Boolean getParameterBoolean(String name, Boolean defaultValue) {
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = parameters.get(i);
      if (p.getName().equals(name)) {
        return new Boolean(p.getValue());
      }
    }
    return defaultValue;
  }

  /**
   * Parse named parameter as Boolean.
   * 
   * @param name
   * @return Boolean value
   * @throws RepositoryConfigurationException
   */
  public Boolean getParameterBoolean(String name) throws RepositoryConfigurationException {
    return new Boolean(getParameterValue(name));
  }

  public String getType() {
    return type;
  }

  public void setParameters(List<SimpleParameterEntry> parameters) {
    this.parameters = parameters;
  }
  
  public void setType(String type) {
    this.type = type;
  }

}
