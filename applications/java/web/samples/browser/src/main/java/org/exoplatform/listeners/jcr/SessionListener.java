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
package org.exoplatform.listeners.jcr;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: SessionListener.java 7175 2006-07-19 07:57:44Z peterit $
 */

public class SessionListener implements HttpSessionListener {
  
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession() ;
    session.setAttribute("rep", "repository");
    session.setAttribute("way", "jndi");
    session.setAttribute("ws", "production");
  }

  public void sessionDestroyed(HttpSessionEvent event) {
  }  
  
}
