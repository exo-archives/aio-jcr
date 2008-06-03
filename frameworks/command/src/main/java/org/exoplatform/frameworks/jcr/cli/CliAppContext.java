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
package org.exoplatform.frameworks.jcr.cli;

import java.util.List;

import javax.jcr.Item;
import javax.naming.NamingException;
import javax.jcr.Credentials;

import org.exoplatform.frameworks.jcr.command.BasicAppContext;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class CliAppContext extends BasicAppContext {

  protected final String currentItemKey = "CURRENT_ITEM";
  protected final String parametersKey;
  protected final String outputKey = "OUTPUT";

  public CliAppContext(ManageableRepository rep, String parametersKey)
      throws NamingException {
    super(rep, null);
    this.parametersKey = parametersKey;
    // put(currentItemKey, getSession().getRootNode());
  }

  public CliAppContext(ManageableRepository rep, String parametersKey,
      Credentials cred) throws NamingException {
    super(rep, cred);
    this.parametersKey = parametersKey;
    // put(currentItemKey, getSession().getRootNode());
  }

  public String getUserName() {
    try {
      return getSession().getUserID();
    } catch (Exception e) {
      log.error("GetUserName error: " + e);
      return "Undefined";
    }
  }

  public String getCurrentWorkspace() {
    return currentWorkspace;
  }

  public List<String> getParameters() {
    return (List<String>) get(parametersKey);
  }

  public String getParameter(int index) throws ParameterNotFoundException {
    List<String> params = getParameters();
    if (params.size() <= index)
      throw new ParameterNotFoundException(
          "Not enough number of parameters expected at least: " + (index + 1)
              + " found: " + params.size());
    return params.get(index);
  }

  public void setCurrentItem(Item item) {
    put(currentItemKey, item);
  }

  public Item getCurrentItem() {
    return (Item) get(currentItemKey);
  }

  public String getOutput() {
    return (String) get(outputKey);
  }

  public void setOutput(String output) {
    put(outputKey, output);
  }

  public void clearOutput() {
    put(outputKey, "");
  }

}
