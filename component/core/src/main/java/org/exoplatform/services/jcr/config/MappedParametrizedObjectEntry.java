/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: MappedParametrizedObjectEntry.java 1807 2005-08-28 13:34:58Z
 *          geaz $
 */

public abstract class MappedParametrizedObjectEntry {

  //protected String name;

  protected String type;

  protected ArrayList parameters;

  public MappedParametrizedObjectEntry() {
    parameters = new ArrayList();
  }

  public MappedParametrizedObjectEntry(String type, ArrayList params) {
    //this.name = name;
    this.type = type;
    this.parameters = params;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List getParameters() {
    return parameters;
  }

  public void setParameters(List parameters) {
    this.parameters = (ArrayList) parameters;
  }

  public String getParameterValue(String name)
      throws RepositoryConfigurationException {
    for (int i = 0; i < parameters.size(); i++) {
      SimpleParameterEntry p = (SimpleParameterEntry) parameters.get(i);
      if (p.getName().equals(name))
        return p.getValue();
    }
    throw new RepositoryConfigurationException("Parameter not found " + name);
  }

}