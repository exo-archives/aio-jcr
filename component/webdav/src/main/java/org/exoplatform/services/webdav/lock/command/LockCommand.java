/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.lock.Lock;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.representation.PropertyResponseRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.representation.property.LockDiscoveryRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class LockCommand extends WebDavCommand {
  
  public LockCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  
  
  @HTTPMethod(WebDavMethod.LOCK)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response getProperties(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader      
      ) {
    
    try {      
      /*
       * SessionProvider uses to identify the user in the system and the receive session
       * 
       */
      
      SessionProvider sessionProvider = getSessionProvider(authorization);

      /*
       * Item is the JCR element of tree, which is used as root element while generating response.
       * There may be an RepositoryException if a user does not have the necessary access rights.
       */
      
      ManageableRepository repository = webDavService.getRepository(repoName);
      
      try {
        Item item = new JCRResourceDispatcher(sessionProvider, repository).getItem(repoPath);

        /*
         * If the selected element not a Node is a error
         */

        if ("/".equals(item.getPath())) {
          throw new AccessDeniedException();
        }
        
        ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
        tuneSession(item.getSession(), lockTokens);

                
        
        if (!(item instanceof Node)) {
          throw new AccessDeniedException();
        }

        Node node = (Node)item;        
        if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
          node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);        
          node.getSession().save();
        } 
    
        Lock lockResult = node.lock(true, false);      
        node.getSession().save();
        
        LockDiscoveryRepresentation lockDiscovery = new LockDiscoveryRepresentation();
        lockDiscovery.read(node);        
        lockDiscovery.setLockToken(lockResult.getLockToken());        

        PropertyResponseRepresentation propertyResponseRepresentation = new PropertyResponseRepresentation(repository, lockDiscovery);
        
        return Response.Builder.withStatus(WebDavStatus.OK).
          header(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">").
          entity(propertyResponseRepresentation, "text/xml").build();
        
      } catch (PathNotFoundException pexc) {
        
        FakeLockTable lockTable = webDavService.getLockTable();
        
        String lockToken = lockTable.lockResource(repoName + "/" + repoPath);
        
        LockDiscoveryRepresentation lockDiscovery = new LockDiscoveryRepresentation();
        lockDiscovery.setLocked(true);
        lockDiscovery.setLockOwner("admin");
        lockDiscovery.setLockToken(lockToken);
        lockDiscovery.setStatus(WebDavStatus.OK);
        
        PropertyResponseRepresentation propertyResponseRepresentation = new PropertyResponseRepresentation(repository, lockDiscovery);

        return Response.Builder.withStatus(WebDavStatus.OK).
          header(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">").
          entity(propertyResponseRepresentation, "text/xml").build();
      }      
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }
  
}
