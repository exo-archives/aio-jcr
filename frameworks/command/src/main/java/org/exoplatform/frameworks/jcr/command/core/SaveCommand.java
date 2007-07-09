/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.core;

import javax.jcr.Item;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SaveCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class SaveCommand implements Command {

  private String pathKey = DefaultKeys.PATH;

  public boolean execute(Context context) throws Exception {
    
    Session session = ((JCRAppContext)context).getSession();
    String relPath = (String)context.get(pathKey);
    if(relPath == null)
      session.save();
    else
      ((Item)session.getItem(relPath)).save();

    return true; 
  }

  public String getPathKey() {
    return pathKey;
  }

}
