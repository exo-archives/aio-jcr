/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.lock.Lock;

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
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.DefaultDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.FakeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.response.Property;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.property.LockDiscoveryProp;
import org.exoplatform.services.webdav.lock.request.LockInfoDocument;
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
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getProperties(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);

      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, lockTokens, serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);

      RequestDocument lockDocument = documentDispatcher.getRequestDocument();
      
      if ((lockDocument == null) || (lockDocument instanceof DefaultDocument)) {
        lockDocument = new LockInfoDocument(); 
      }
      
      if (!(lockDocument instanceof LockInfoDocument)) {
        throw new BadRequestException();
      }
      
      WebDavResource resource = resourceLocator.getSrcResource(true);
      
      if (resource instanceof FakeResource) {
        return doFakeLock(resource.getHref(), (LockInfoDocument)lockDocument);
      }

      if (!(resource instanceof NodeResource) &&
          !(resource instanceof DeltaVResource)) {      
        throw new AccessDeniedException();
      }
      
      Node node = ((AbstractNodeResource)resource).getNode();

      if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
        node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);        
        node.getSession().save();
      } 
      
      Lock lockResult = node.lock(true, false);      
      node.getSession().save();

      PropertyFactory factory = webDavService.getConfig().getPropertyFactory();    
      PropertyDefine define = factory.getDefine(DavConst.DAV_NAMESPACE, DavProperty.LOCKDISCOVERY);
      
      LockDiscoveryProp lockDiscovery = (LockDiscoveryProp)define.getProperty();
      
      lockDiscovery.refresh(resource, null);
      
      lockDiscovery.setLockToken(lockResult.getLockToken());
      
      Response response = xmlResponse(new Property(lockDiscovery), WebDavStatus.OK);
      response.getResponseHeaders().putSingle(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">");
      return response;
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }  
  
  private Response doFakeLock(String resourceHref, LockInfoDocument lockDoc) throws Exception {
    FakeLockTable lockTable = webDavService.getLockTable();
    
    String lockToken = lockTable.lockResource(resourceHref);
    
    LockDiscoveryProp lockDiscovery = new LockDiscoveryProp();
    lockDiscovery.setLocked(true);
    lockDiscovery.setOwner(lockDoc.getLockOwner());
    lockDiscovery.setLockToken(lockToken);
    lockDiscovery.setStatus(WebDavStatus.OK);
    
    Response response = xmlResponse(new Property(lockDiscovery), WebDavStatus.OK);
    response.getResponseHeaders().putSingle(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">");
    return response;
  }  
  
}
