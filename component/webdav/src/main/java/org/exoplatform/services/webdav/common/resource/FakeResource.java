/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: FakeResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public class FakeResource extends AbstractWebDavResource implements JCRResource {
  
  private static Log log = ExoLogger.getLogger("jcr.FakeResource");
  
  private Session session;
  private String resourcePath;
  
  public FakeResource(
      WebDavService webDavService,
      String rootHref,
      String resourcePath,
      Session session
      ) {    
    super(webDavService, rootHref);
    
    this.session = session;
    this.resourcePath = resourcePath;
  }
  
  public Session getSession() {
    return session;
  }
  
  public String getPath() {
    return resourcePath;    
  }
  
  public Workspace getWorkspace() {
    return session.getWorkspace();
  }
  
  public WebDavResource createAsCollection(String nodeType, ArrayList<String> mixinTypes) throws RepositoryException {
    String workNodeType = (nodeType != null) ? nodeType :
      webDavService.getConfig().getDefFolderNodeType();
    
    String []pathes = resourcePath.split("/");
    Node node = session.getRootNode();
    
    for (int i = 0; i < pathes.length; i++) {
      if ("".equals(pathes[i])) {
        continue;
      }
      
      if (node.hasNode(pathes[i])) {
        node = node.getNode(pathes[i]);
      } else {
        node = node.addNode(pathes[i], workNodeType);
      }
      
    }
    
    session.save();

    if (mixinTypes != null) {
      for (int i = 0; i < mixinTypes.size(); i++) {
        String curMixinType = mixinTypes.get(i);
        try {
          node.addMixin(curMixinType);
          node.getSession().save();
        } catch (Exception exc) {
          log.info("Can't add mixin [" + curMixinType + "]");
        }
        
      }
      
    }
    
    return new NodeResource(webDavService, getHref(), node);
  }  
  
}
