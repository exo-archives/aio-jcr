package org.exoplatform.services.jcr.ext.mvnadmin.service;
/*23.08.2007-12:30:49 Volodymyr*/

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

public class BrowseArtifact {
  private Session session;
  private Node rootNode;
  
  private static Log log = ExoLogger.getLogger(BrowseArtifact.class);
  
  public BrowseArtifact(Session session) throws RepositoryException{
    this.session = session;
    
    rootNode = session.getRootNode(); // root Node is already been initialized! 
    
  }

}
 