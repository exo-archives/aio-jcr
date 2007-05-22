/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.web.fckeditor;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.frameworks.jcr.JCRAppSessionFactory;
import org.exoplatform.frameworks.jcr.SingleRepositorySessionFactory;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JCRContentFCKeditor.java 6944 2006-07-11 08:06:04Z peterit $
 */

public class JCRContentFCKeditor extends FCKeditor {
  private Node file;
  
  public JCRContentFCKeditor(HttpServletRequest req, String parInstanceName, String workspaceName, String filePath, String newNodeType) 
   throws RepositoryException {
    super(req, parInstanceName);
    JCRAppSessionFactory factory = (JCRAppSessionFactory) req.getSession()
      .getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY);
    Session session = factory.getSession(workspaceName);
    try {
      this.file = (Node)session.getItem(filePath);
    } catch (PathNotFoundException e1) {
      this.file = session.getRootNode()
      .addNode(filePath.substring(1), newNodeType);
      
    }
    if(!file.isNodeType("nt:file"))
      throw new RepositoryException("The Node should be nt:file type");
    try {
      Property content = (Property)session.getItem(filePath+"/jcr:content/jcr:data");
      this.setValue(content.getString());
    } catch (RepositoryException e) {
      //e.printStackTrace();
    }
  }

  public void saveValue(String value) throws RepositoryException {
    //file.setProperty("jcr:content/jcr:data", value);
    //[VO] "jcr:content/jcr:data" - impossible according to spec
    file.getNode("jcr:content").setProperty("jcr:data", value);
    setValue(value);
    file.getSession().save();
  }
}
