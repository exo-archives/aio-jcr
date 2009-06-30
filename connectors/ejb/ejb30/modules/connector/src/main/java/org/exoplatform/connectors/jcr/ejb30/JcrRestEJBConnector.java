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

package org.exoplatform.connectors.jcr.ejb30;

import java.io.IOException;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
//import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ext.transport.SerialRequest;
import org.exoplatform.services.rest.ext.transport.SerialResponse;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.ws.rest.ejbconnector30.RestEJBConnectorLocal;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Stateless
//(mappedName="JcrRestEJBConnector")
@DeclareRoles({ "admin", "users" })
@TransactionManagement(TransactionManagementType.BEAN)
public class JcrRestEJBConnector implements JcrRestEJBConnectorRemote, JcrRestEJBConnectorLocal {

  /**
   * Logger.
   */
  private static final Log      LOG       = ExoLogger.getLogger(JcrRestEJBConnector.class.getName());

  /**
   * JNDI name for REST-EJB connector bean.
   */
  private static final String   JNDI_NAME = "org.exoplatform.ws.rest.ejbconnector30.RestEJBConnector"
                                              + "_"
                                              + RestEJBConnectorLocal.class.getName()
                                              + "@Local";

  /**
   * Session context.
   */
  @Resource
  private SessionContext        context;

  // Inject required bean instead lookup bean 'manually'.
  // does not work at easy-beans 1.0.1, why ???
//  @EJB(beanInterface = RestEJBConnectorLocal.class)
//  private RestEJBConnectorLocal bean; 

  /**
   * Portal container name.
   */
  private String                containerName;

  /**
   * @param request wrapper for REST request that gives possibility transfer
   *          request via RMI
   * @return wrapper around REST response that gives possibility transfer
   *         request via RMI
   * @throws IOException if any i/o errors occurs
   */
  @RolesAllowed({ "admin", "users" })
  public final SerialResponse service(final SerialRequest request) throws IOException {
    InitialContext ctx = null;
    try {
      ctx = new InitialContext();
      containerName = (String) ctx.lookup("java:comp/env/exo.container.name");
    } catch (NamingException e1) {
      LOG.error("Can't construct an initial context or get portal container name. ");
    }

    ExoContainer container = getContainer();
    
    IdentityRegistry identityRegistry =
      (IdentityRegistry) container.getComponentInstanceOfType(IdentityRegistry.class);

    ThreadLocalSessionProviderService sessionProviderService =
      (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);

    String userId = context.getCallerPrincipal().getName();
    Identity identity = identityRegistry.getIdentity(userId);

    if (identity == null) {
      // Identity was not initialized yet. This happen when use remote
      // servlet for access to bean, but never happen when use standalone client
      // or servlet that works on the same machine.
      // Trust ejb security so create identity for this user.
      Authenticator authenticator = (Authenticator) container.getComponentInstanceOfType(Authenticator.class);
      try {
        identity = authenticator.createIdentity(userId);
      } catch (Exception e) {
        throw new EJBException("Can't create identity for user " + userId, e);
      }
    }

    try {
      ConversationState state = new ConversationState(identity);
      SessionProvider provider = new SessionProvider(state);
      state.setAttribute(SessionProvider.SESSION_PROVIDER, provider);
      
      ConversationState.setCurrent(state);
      sessionProviderService.setSessionProvider(null, provider);

      if (ctx != null) {
        RestEJBConnectorLocal bean = (RestEJBConnectorLocal) ctx.lookup(JNDI_NAME);
        return bean.service(request);
      } else {
        throw new EJBException("Can't get local interface of RestEJBConnector, InitialContext is null. ");
      }
    } catch (Exception e) {
      throw new EJBException(e);
    } finally {
      try {
        sessionProviderService.removeSessionProvider(null);
        ConversationState.setCurrent(null);
        ExoContainerContext.setCurrentContainer(null);
      } catch (Exception e) {
        LOG.warn("Failed reset ThreadLocal variables", e);
      }
    }
  }

  /**
   * @return actual exo container
   */
  protected ExoContainer getContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer) {
      container = RootContainer.getInstance().getPortalContainer(containerName);
      ExoContainerContext.setCurrentContainer(container);
    }

    return container;
  }

}
