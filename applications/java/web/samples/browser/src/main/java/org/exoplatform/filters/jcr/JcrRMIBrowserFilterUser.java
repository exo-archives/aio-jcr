/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.filters.jcr;

import java.io.IOException;
import java.util.*;

import javax.jcr.*;
import javax.naming.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.services.jcr.rmi.api.client.*;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: JcrRMIBrowserFilterUser.java 11142 2006-12-13 16:32:21Z ksm $
 */

public class JcrRMIBrowserFilterUser implements Filter {

  public void init(FilterConfig filterConfig) {
  }

  public void doFilter(ServletRequest servletRequest,
      ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    httpRequest.setCharacterEncoding("UTF-8");
    
    HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

    Helper.tuneRequest(httpRequest);

    Session ses = (Session) httpRequest.getSession().getAttribute("ses");

    if (ses == null) {
      try {
        Repository repository = Helper.requestRepository(httpRequest, (String) httpRequest.getSession().getAttribute("rep"),
          ((String) httpRequest.getSession().getAttribute("way")).equalsIgnoreCase("rmi"));
        String s = (String) httpRequest.getSession().getAttribute("ws");
        if (s == null)
          s = "";
        ses = repository.login(s);
        httpRequest.getSession().setAttribute("ses", ses);
      } catch (Exception e) {
        System.err.println("doFilter, ses = null: " + e);
        e.printStackTrace();
        httpRequest.getSession().setAttribute("msg", e.toString());
        httpResponse.sendRedirect("reperr.jsp");
        return;
      }
    }

    if (ses != null) {
      String s = httpRequest.getParameter("node");
      Node node = null;
      try {
        if (s == null) {
          node = ses.getRootNode();
        } else
        if (s.equals("/")) {
          node = ses.getRootNode();
        } else {
          node = ses.getRootNode().getNode(s.substring(1));
        }

        if (node == null)
          throw new Exception();
        httpRequest.getSession().setAttribute("node", node);
      } catch (Exception e) {
        System.err.println("doFilter, ses != null: " + e);
        e.printStackTrace();
        httpRequest.getSession().setAttribute("msg", e.toString());
        httpResponse.sendRedirect("reperr.jsp");
        return;
      }
    } else {
      System.out.println("JcrRMIBrowserFilter2: can't get session");
      httpRequest.getSession().setAttribute("msg", "Session expired");
      httpResponse.sendRedirect("reperr.jsp");
      return;
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }

}
