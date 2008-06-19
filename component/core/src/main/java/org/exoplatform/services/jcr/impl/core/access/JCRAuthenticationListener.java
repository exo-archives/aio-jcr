/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.core.access;

import org.apache.commons.logging.Log;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Identity;


/**
 * Created by The eXo Platform SAS
 * 
 * @deprecated for back compatibility
 * 
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * May 17, 2007  
 * 
 * @version $Id: JCRAuthenticationListener.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */
public class JCRAuthenticationListener extends Listener<ConversationRegistry, Identity> {
  
  private Log      log = ExoLogger.getLogger("kernel.JCRAuthenticationListener");
  
  public void onEvent(Event<ConversationRegistry, Identity> event)  {
//    Identity identity = event.getData() ;
//    String username = identity.getUserId();
//    Subject subject = identity.getSubject() ;
//    Set<String> temp = subject.getPrivateCredentials(String.class);
//    if (temp.size() == 1)
//      subject.getPublicCredentials().add(new CredentialsImpl(username,
//          temp.iterator().next().toCharArray()));
//    else
//      log.warn("Could not obtain the password credentials from AuthenticationService."
//          + "JCR Credentials will not be created.");
//    
    if (log.isDebugEnabled())
      log.debug("Call JCRAuthenticationListener");
  }
}
