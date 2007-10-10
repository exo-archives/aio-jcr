/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.representation.SinglePropertyRepresentation;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.representation.LockInfoRepresentation;
import org.exoplatform.services.webdav.lock.representation.property.LockDiscoveryRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class LockCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.LockCommand");
  
  public LockCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  
  
  @HTTPMethod(WebDavMethod.LOCK)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getProperties(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader      
      ) {
    
    try {      
//      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
//      
//      SessionProvider sessionProvider = getSessionProvider(authorization);
//      
//      RequestRepresentation requestRepresentation = webDavService.getRequestDispatcher().getRequestRepresentation(document);
//      
//      if (requestRepresentation == null ||
//          (requestRepresentation instanceof SimpleRequestRepresentation)) {
//        requestRepresentation = new LockInfoRepresentation();
//        
//        ((LockInfoRepresentation)requestRepresentation).setLockOwner("gavrikvetal");
//      }
//      
//      ManageableRepository repository = webDavService.getRepository(repoName);
//      
//      try {
//        Item item = new JCRResourceDispatcher(sessionProvider, repository).getItem(repoPath);
//        
//        if ("/".equals(item.getPath())) {
//          throw new AccessDeniedException();
//        }
//        
//        tuneSession(item.getSession(), lockTokens);
//        
//        Node node = (Node)item;        
//        if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
//          node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);        
//          node.getSession().save();
//        } 
//    
//        Lock lockResult = node.lock(true, false);      
//        node.getSession().save();
//        
//        LockDiscoveryRepresentation lockDiscovery = new LockDiscoveryRepresentation();
//        lockDiscovery.read(node);        
//        lockDiscovery.setLockToken(lockResult.getLockToken());
//        
//        return createLockResponse(repository, lockDiscovery);        
//      } catch (PathNotFoundException pexc) {
//        String fullPath = repoName + "/" + repoPath;
//        return doFakeLock(repository, fullPath, (LockInfoRepresentation)requestRepresentation);
//      }
      
      return null;
      
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }
  
  private Response createLockResponse(ManageableRepository repository, LockDiscoveryRepresentation lockDiscovery) {
    SinglePropertyRepresentation propertyRepresentation = new SinglePropertyRepresentation(lockDiscovery); 
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    //XmlResponseWriter writer = new XmlResponseWriter(repository, propertyRepresentation);
    
    //writer.write(outStream);
    
    byte []bytes = outStream.toByteArray();
    
    InputStream inputStream = new ByteArrayInputStream(bytes);
    
    return Response.Builder.withStatus(WebDavStatus.OK).
      header(DavConst.Headers.CONTENTLENGTH, "" + bytes.length).
      header(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">").
      entity(inputStream, "text/xml").build();      
   
  }
  
  private Response doFakeLock(ManageableRepository repository, String resourceHref, LockInfoRepresentation lockInfoRepresentation) throws Exception {
    FakeLockTable lockTable = webDavService.getLockTable();
    
    String lockToken = lockTable.lockResource(resourceHref);
    
//    LockDiscoveryRepresentation lockDiscovery = new LockDiscoveryRepresentation();
//    lockDiscovery.setLocked(true);
//    lockDiscovery.setLockOwner(lockInfoRepresentation.getLockOwner());
//    lockDiscovery.setLockToken(lockToken);
//    lockDiscovery.setStatus(WebDavStatus.OK);
//    
//    return createLockResponse(repository, lockDiscovery);
    return null;
  }  
  
}
