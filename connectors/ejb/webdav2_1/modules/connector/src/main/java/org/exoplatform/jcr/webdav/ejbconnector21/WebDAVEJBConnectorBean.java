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

package org.exoplatform.jcr.webdav.ejbconnector21;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

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
import org.exoplatform.ws.rest.ejbconnector21.RestEJBConnectorLocalHome;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WebDAVEJBConnectorBean implements SessionBean {

  private SessionContext context;

  private static final long serialVersionUID = -1942791580384021057L;

  public final SerialResponse service(final SerialRequest request) throws IOException {
    String userId = context.getCallerPrincipal().getName();
    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer().getComponentInstanceOfType(
        IdentityRegistry.class);

    Identity identity = identityRegistry.getIdentity(userId);

    ConversationState conversationState = new ConversationState(identity);
    ConversationState.setCurrent(conversationState);
    
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) getContainer()
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);

    sessionProviderService.setSessionProvider(null, new SessionProvider(conversationState));
    try {
      InitialContext initialContext = new InitialContext();
      Object obj = initialContext.lookup("RestEJBConnectorLocal");
      RestEJBConnectorLocalHome bean = (RestEJBConnectorLocalHome) PortableRemoteObject.narrow(obj,
          RestEJBConnectorLocalHome.class);
      
      return bean.create().service(request);
    } catch (NamingException e) {
      e.printStackTrace();
      throw new EJBException("RestEJBConnectorLocal not found in jndi!");
    } catch (CreateException e) {
      e.printStackTrace();
      throw new EJBException("Can't create RestEJBConnectorLocal!");
    } catch (Exception e) {
      e.printStackTrace();
      throw new EJBException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see javax.ejb.SessionBean#ejbActivate()
   */
  public void ejbActivate() throws EJBException, RemoteException {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * @see javax.ejb.SessionBean#ejbPassivate()
   */
  public void ejbPassivate() throws EJBException, RemoteException {
    // nothing to do here
  }

  /*
   * (non-Javadoc)
   * @see javax.ejb.SessionBean#ejbRemove()
   */
  public void ejbRemove() throws EJBException, RemoteException {
    // nothing to do here
  }

  public void ejbCreate() {
    // nothing to do here
  }
  
  /*
   * (non-Javadoc)
   * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
   */
  public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
    this.context = context;
  }

  protected ExoContainer getContainer() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer)
      return RootContainer.getInstance().getPortalContainer("portal");

    return container;
  }

}
