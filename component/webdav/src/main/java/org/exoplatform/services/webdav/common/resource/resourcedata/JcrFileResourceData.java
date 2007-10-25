/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.resourcedata;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class JcrFileResourceData extends AbstractResourceData {

  public JcrFileResourceData(Node resourceNode) throws RepositoryException {
    iscollection = false;
    
    name = resourceNode.getName();
    
    Node contentNode = resourceNode.getNode(DavConst.NodeTypes.JCR_CONTENT);

    if (contentNode.hasProperty(DavConst.NodeTypes.JCR_LASTMODIFIED)) {
      lastModified = contentNode.getProperty(DavConst.NodeTypes.JCR_LASTMODIFIED).getString();
    }
    
    contentType = contentNode.getProperty(DavConst.NodeTypes.JCR_MIMETYPE).getString();
    Property dataProperty = contentNode.getProperty(DavConst.NodeTypes.JCR_DATA);
    
    resourceInputStream = dataProperty.getStream();
    resourceLenght = dataProperty.getLength();
  }
  
}
