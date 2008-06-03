/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.ext.resource.jcr;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.resource.JcrURLConnection;
import org.exoplatform.services.jcr.ext.resource.NodeRepresentationService;
import org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Handler extends URLStreamHandler implements Startable {

  /*
   * This is implements as Startable to be independent from other services.
   * It should be guaranty created, and set special system property. 
   */
  
  private static final String protocolPathPkg = "org.exoplatform.services.jcr.ext.resource";

  private static Authenticator authenticator_;
  private static RepositoryService repositoryService_;
  private static NodeRepresentationService nodeRepresentationService_;
  
  private static ThreadLocal<SessionProvider> sessionProviderKeeper =
    new ThreadLocal<SessionProvider>();
  
  public Handler(Authenticator authenticator,
      RepositoryService repositoryService, NodeRepresentationService nodeRepresentationService) {
    authenticator_ = authenticator;
    repositoryService_ = repositoryService;
    nodeRepresentationService_ = nodeRepresentationService;
  }
  
  public Handler() {
    // this constructor will be used by java.net.URL
  }

  /* (non-Javadoc)
   * @see java.net.URLStreamHandler#openConnection(java.net.URL)
   */
  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    try {
      UnifiedNodeReference nodeReference = new UnifiedNodeReference(url);
      
      SessionProvider sp = sessionProviderKeeper.get();
      
      ConversationState conversationState = ConversationState.getCurrent();
      if (sp == null && conversationState != null) {
        sp = (SessionProvider) conversationState.getAttribute(
            SessionProvider.SESSION_PROVIDER);
      }
      
      if (sp == null) {
        Credential[] cred = parseCredentials(nodeReference.getUserInfo());
        if (cred == null) {
          if (conversationState == null)
            sp = SessionProvider.createAnonimProvider();
          else 
            sp = new SessionProvider(conversationState);
        }
        else
          sp = new SessionProvider(new ConversationState(authenticator_.authenticate(cred)));
      }
      
      ManageableRepository repository;
      String repositoryName = nodeReference.getRepository();
      if (repositoryName != null || repositoryName.length() > 0)
        repository = repositoryService_.getRepository(repositoryName);
      else 
        repository = sp.getLastUsedRepository();

      String workspaceName = nodeReference.getWorkspace();
      if (workspaceName == null || workspaceName.length() == 0)
        workspaceName = sp.getLastUsedWorkspace();
      
      Session ses = sp.getSession(workspaceName, repository);
      JcrURLConnection conn = new JcrURLConnection(nodeReference, ses, nodeRepresentationService_);
      return conn;

    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException("Open connection to URL '" + url.toString() + "' failed!");
    }
  }
  
  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    String existingProtocolPathPkgs = System.getProperty("java.protocol.handler.pkgs");
    if (existingProtocolPathPkgs == null)
      System.setProperty("java.protocol.handler.pkgs", protocolPathPkg);
    else if (existingProtocolPathPkgs.indexOf(protocolPathPkg) == -1)
      System.setProperty("java.protocol.handler.pkgs", existingProtocolPathPkgs
          + "|" + protocolPathPkg);
  }
  
  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    // nothing to do!
  }
  
  /**
   * Set session provider, it useful on startup time and for update script, when there is no any users.
   * @param sp the SessionProvider.
   */
  public void setSessionProvider(SessionProvider sessionProvider) {
    sessionProviderKeeper.set(sessionProvider);
  }
  
  /**
   * Close session. 
   */
  public void removeSessionProvider() {
    sessionProviderKeeper.get().close();
    sessionProviderKeeper.remove();
  }
  
  private static Credential[] parseCredentials(String userInfo) {
    if (userInfo == null)
      return null;
    
    int colon = userInfo.indexOf(':');
    
    if (colon > 0)
      return new Credential[] { new UsernameCredential(userInfo.substring(0, colon)),
        new PasswordCredential(userInfo.substring(colon + 1)) };
    if (colon < 0) 
      return new Credential[] { new UsernameCredential(userInfo) }; 
    
    return null;
  }  
  
}

