/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.filters.jcr;

import java.io.IOException;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: JcrRMIBrowserFilterAdmin.java 7176 2006-07-19 07:59:47Z peterit $
 */

public class JcrRMIBrowserFilterAdmin implements Filter {

  public void init(FilterConfig filterConfig) {
  }

  public void doFilter(ServletRequest servletRequest,
      ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

    Helper.tuneRequest(httpRequest);

    try {
      Session ses = (Session) httpRequest.getSession().getAttribute("ses");
      if (httpRequest.getParameter("rep") != null) {
        Repository repository = Helper.requestRepository(httpRequest, httpRequest.getParameter("rep"),
          httpRequest.getParameter("way").equalsIgnoreCase("rmi"));

        if (ses != null)
          ses.logout();
        httpRequest.getSession().removeAttribute("ses");
        httpRequest.getSession().setAttribute("rep", httpRequest.getParameter("rep"));
        httpRequest.getSession().setAttribute("way", httpRequest.getParameter("way"));
        httpRequest.getSession().setAttribute("ws", "");
      } else
      if (httpRequest.getParameter("ws") != null) {
        if (ses != null)
          ses.logout();
        httpRequest.getSession().removeAttribute("ses");
        httpRequest.getSession().setAttribute("ws", httpRequest.getParameter("ws"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      String repe = "";
      if (httpRequest.getParameter("rep") != null && httpRequest.getParameter("way") != null)
        repe = httpRequest.getParameter("rep") + "/" + httpRequest.getParameter("way").toUpperCase();
      else
        repe = (String) httpRequest.getSession().getAttribute("rep") + "/" +
          ((String) httpRequest.getSession().getAttribute("way")).toUpperCase();
      httpRequest.getSession().setAttribute("repe", repe);
      httpResponse.sendRedirect("notfound.jsp");
      return;
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }

}
