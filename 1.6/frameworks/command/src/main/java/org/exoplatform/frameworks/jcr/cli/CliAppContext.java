/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import java.util.List;

import javax.jcr.Item;
import javax.naming.NamingException;
import javax.jcr.Credentials;

import org.exoplatform.frameworks.jcr.command.BasicAppContext;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL        .
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
    //put(currentItemKey, getSession().getRootNode());
  }
  
  public CliAppContext(ManageableRepository rep, String parametersKey,Credentials cred) 
  throws NamingException {
    super(rep, cred);
    this.parametersKey = parametersKey;
    //put(currentItemKey, getSession().getRootNode());
  }
  
  public String getUserName() {
    try {
      return getSession().getUserID();
    } catch (Exception e) {
      log.error("GetUserName error: "+e);
      return "Undefined";
    } 
  }
  
  public String getCurrentWorkspace() {
    return currentWorkspace;
  }
  
  public List <String> getParameters() {
    return (List <String>)get(parametersKey);
  }
  
  public String getParameter(int index) throws ParameterNotFoundException {
    List <String> params = getParameters();
    if(params.size() <= index)
      throw new ParameterNotFoundException("Not enough number of parameters expected at least: "+(index+1)+" found: "+params.size());
    return params.get(index);
  }
  
  public void setCurrentItem(Item item) {
    put(currentItemKey, item);
  }
  
  public Item getCurrentItem() {
    return (Item)get(currentItemKey);
  }

  public String getOutput() {
    return (String)get(outputKey);
  }

  public void setOutput(String output) {
    put(outputKey, output);
  }
  
  public void clearOutput() {
    put(outputKey,"");
  }

}
