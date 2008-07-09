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

package org.exoplatform.jcr.webdav.ejbconnector30;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exoplatform.common.transport.SerialRequest;
import org.exoplatform.common.transport.SerialResponse;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.ws.rest.ejbconnector30.RestEJBConnectorLocal;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Stateless
// (mappedName="WebDAVEJBConnector")
@DeclareRoles({ "admin", "users" })
@TransactionManagement(TransactionManagementType.BEAN)
public class WebDAVEJBConnector implements WebDAVEJBConnectorRemote, WebDAVEJBConnectorLocal {

  private static final String JNDI_NAME = "org.exoplatform.ws.rest.ejbconnector30.RestEJBConnector" +
      "_" + RestEJBConnectorLocal.class.getName() + "@Local";

  @Resource
  private SessionContext context;

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.jcr.webdav.ejbconnector21.WebDAVEJBConnector#service(org
   * .exoplatform.common.transport.SerialRequest)
   */
  @RolesAllowed({ "admin", "users" })
  public final SerialResponse service(final SerialRequest request) throws RemoteException,
      IOException {

    String userId = context.getCallerPrincipal().getName();

    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer()
        .getComponentInstanceOfType(IdentityRegistry.class);

    Identity identity = identityRegistry.getIdentity(userId);

    ConversationState conversationState = new ConversationState(identity);
    ConversationState.setCurrent(conversationState);

    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) getContainer()
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);

    sessionProviderService.setSessionProvider(null, new SessionProvider(conversationState));
    try {
      InitialContext initialContext = new InitialContext();
      RestEJBConnectorLocal bean = (RestEJBConnectorLocal) initialContext.lookup(JNDI_NAME);
      return bean.service(request);
    } catch (NamingException e) {
      e.printStackTrace();
      throw new EJBException("RestEJBConnectorLocal not found in jndi!");
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

  protected ExoContainer getContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer)
      return RootContainer.getInstance().getPortalContainer("portal");

    return container;
  }

}
