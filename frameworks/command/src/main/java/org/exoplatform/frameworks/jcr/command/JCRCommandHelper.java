/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL.
 * @author Gennady Azarenkov
 * @version $Id: JCRCommandHelper.java 9857 2006-10-28 20:49:03Z geaz $
 */

public class JCRCommandHelper {

  /**
   * creates nt:file node and fills it with incoming data
   * @param parentNode
   * @param relPath
   * @param data
   * @param mimeType
   * @return
   * @throws Exception
   */
  public static Node createResourceFile(Node parentNode, String relPath, 
      Object data, String mimeType) throws Exception {
    
    Node file = parentNode.addNode(relPath, "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    
    if(data instanceof InputStream)
      contentNode.setProperty("jcr:data", (InputStream)data);
    else if(data instanceof String) 
      contentNode.setProperty("jcr:data", (String)data);
//    else if(data instanceof BinaryValue) 
//      contentNode.setProperty("jcr:data", (BinaryValue)data);
    else
      throw new Exception("Invalid object for jcr:data "+data);
    
    contentNode.setProperty("jcr:mimeType", mimeType);
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    return file;

  }
  
  /**
   * traverses incoming node trying to find primary nt:resource node  
   * @param node
   * @return nt:resource node
   * @throws ItemNotFoundException if no such node found
   * @throws RepositoryException
   */
  public static Node getNtResourceRecursively(Node node) throws 
  ItemNotFoundException, RepositoryException {

    if(node.isNodeType("nt:resource"))
      return node;

    Item pi = node.getPrimaryItem();
    if(pi.isNode()) {
      return getNtResourceRecursively((Node)pi);
    }
    throw new ItemNotFoundException("No nt:resource node found for "+node.getPath());
  }
}
