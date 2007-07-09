/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.core;

import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: RemoveItemCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class RemoveItemCommand implements Command {

  private String pathKey = DefaultKeys.PATH;
  private String resultKey = DefaultKeys.RESULT;

  public boolean execute(Context context) throws Exception {
    
    Session session = ((JCRAppContext)context).getSession();
    session.getItem((String)context.get(pathKey)).remove();

    return true; 
  }

  public String getResultKey() {
    return resultKey;
  }

  public String getPathKey() {
    return pathKey;
  }
}
