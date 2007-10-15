/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.listeners.jcr;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by The eXo Platform SARL .
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
