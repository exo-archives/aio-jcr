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

package org.exoplatform.connectors.jcr.ejb21;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.exoplatform.common.transport.SerialRequest;
import org.exoplatform.common.transport.SerialResponse;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.ws.rest.ejbconnector21.RestEJBConnectorLocalHome;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JcrRestEJBConnectorBean implements SessionBean {

  /**
   * Session context.
   */
  private SessionContext    context;

  /**
   * Generated by Eclipse.
   */
  private static final long serialVersionUID = -1942791580384021057L;

  /**
   * Logger.
   */
  private static final Log  LOG              = ExoLogger.getLogger(JcrRestEJBConnectorBean.class.getName()); 

  /**
   * @param request wrapper for REST request that gives possibility transfer
   *          request via RMI
   * @return wrapper around REST response that gives possibility transfer
   *         request via RMI
   * @throws IOException if any i/o errors occurs
   */
  public final SerialResponse service(final SerialRequest request) throws IOException {
    
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
      
      InitialContext initialContext = new InitialContext();
      Object obj = initialContext.lookup("RestEJBConnectorLocal");
      RestEJBConnectorLocalHome bean = (RestEJBConnectorLocalHome) PortableRemoteObject.narrow(obj,
          RestEJBConnectorLocalHome.class);
      
      return bean.create().service(request);
    } catch (NamingException e) {
      throw new EJBException("RestEJBConnectorLocal not found in jndi", e);
    } catch (CreateException e) {
      throw new EJBException("Can't create RestEJBConnectorLocal", e);
    } catch (Exception e) {
      throw new EJBException("Unexpected error ", e);
    } finally {
      try {
        sessionProviderService.removeSessionProvider(null);
        ConversationState.setCurrent(null);
      } catch (Exception e) {
        LOG.warn("Failed reset ThreadLocal variables", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void ejbActivate() throws EJBException, RemoteException {
    // nothing to do here
  }

  /**
   * {@inheritDoc}
   */
  public void ejbPassivate() throws EJBException, RemoteException {
    // nothing to do here
  }

  /**
   * {@inheritDoc}
   */
  public void ejbRemove() throws EJBException, RemoteException {
    // nothing to do here
  }

  public void ejbCreate() {
    // nothing to do here
  }
  
  /**
   * {@inheritDoc}
   */
  public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
    this.context = context;
  }

  /**
   * @return actual exo container
   */
  protected ExoContainer getContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer)
      return RootContainer.getInstance().getPortalContainer("portal");

    return container;
  }

}
