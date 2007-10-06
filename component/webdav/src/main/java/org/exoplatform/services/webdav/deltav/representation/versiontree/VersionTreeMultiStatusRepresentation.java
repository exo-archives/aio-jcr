/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.read.AbstractItemVisitor;
import org.exoplatform.services.webdav.common.representation.read.MultiPropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.read.PropListItemVisitor;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeMultiStatusRepresentation extends MultiPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionTreeMultiStatusRepresentation");
  
  public VersionTreeMultiStatusRepresentation(WebDavService webDavService, HashMap<String, ArrayList<String>> properties, int selectMode) {
    super(webDavService, properties, selectMode);
    log.info("construct..");   
  }

  @Override
  public void init(String prefix, Node node, int maxLevel) throws RepositoryException {
    this.prefix = prefix;
    
    this.node = node;
    
    this.maxLevel = maxLevel;
    
    
    VersionHistory vHistory = node.getVersionHistory();
    VersionIterator vIter = vHistory.getAllVersions();
    
    while (vIter.hasNext()) {
      Version version = vIter.nextVersion();
      
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(version.getName())) {
        continue;
      }
      
      log.info("Version: " + version);            
    }    
    
  }

}
