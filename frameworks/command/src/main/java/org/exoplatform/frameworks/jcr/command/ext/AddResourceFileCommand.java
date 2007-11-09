/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.ext;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;
import org.exoplatform.frameworks.jcr.command.JCRCommandHelper;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: AddResourceFileCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class AddResourceFileCommand implements Command {

  private String pathKey = DefaultKeys.PATH;
  private String currentNodeKey = DefaultKeys.CURRENT_NODE;
  private String resultKey = DefaultKeys.RESULT;
  private String dataKey = "data";
  private String mimeTypeKey = "mimeType";

  public boolean execute(Context context) throws Exception {
    
    Session session = ((JCRAppContext)context).getSession();
  
    Node parentNode = (Node)session.getItem((String)context.get(currentNodeKey));
    String relPath = (String)context.get(pathKey);
    Object data = context.get(dataKey);
    String mimeType = (String)context.get(mimeTypeKey);
    
    Node file = JCRCommandHelper.createResourceFile(parentNode, relPath, data, mimeType);
    
//    Node file = parentNode.addNode(relPath, "nt:file");
//    Node contentNode = file.addNode("jcr:content", "nt:resource");
//    if(data instanceof InputStream)
//      contentNode.setProperty("jcr:data", (InputStream)data);
//    else
//      contentNode.setProperty("jcr:data", (String)data);
//    contentNode.setProperty("jcr:mimeType", (String)context.get(mimeTypeKey));
//    contentNode.setProperty("jcr:lastModified", session
//        .getValueFactory().createValue(Calendar.getInstance()));

    context.put(resultKey, file);
    return true; 
  }

  public String getResultKey() {
    return resultKey;
  }

  public String getCurrentNodeKey() {
    return currentNodeKey;
  }

  public String getPathKey() {
    return pathKey;
  }
}
