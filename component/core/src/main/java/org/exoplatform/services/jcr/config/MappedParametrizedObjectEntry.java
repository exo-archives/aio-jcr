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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: MappedParametrizedObjectEntry.java 1807 2005-08-28 13:34:58Z
 *          geaz $
 */

public abstract class MappedParametrizedObjectEntry {

  protected String type;

  protected List   parameters;

  public MappedParametrizedObjectEntry() {
    parameters = new ArrayList();
  }

  public MappedParametrizedObjectEntry(String type, List params) {
    this.type = type;
    this.parameters = params;
  }

  public List getParameters() {
    return parameters;
  }

  /**
   * @param name
   * @return
   * @throws RepositoryConfigurationException
   */
  public String getParameterValue(String name) throws RepositoryConfigurationException {
    String value = getParameterValue(name, null);
    if (value == null)
      throw new RepositoryConfigurationException("Parameter " + name + " not found ");
    return value;
  }

  /**
   * @param name
   * @param defaultValue
   * @return
   */
  public String getParameterValue(String name, String defaultValue) {
    String value = defaultValue;
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = (SimpleParameterEntry) parameters.get(i);
      if (p.getName().equals(name)) {
        value = p.getValue();
        break;
      }
    }
    return value;
  }

  public String getType() {
    return type;
  }

  public void setParameters(List parameters) {
    this.parameters = parameters;
  }

  public void setType(String type) {
    this.type = type;
  }

}