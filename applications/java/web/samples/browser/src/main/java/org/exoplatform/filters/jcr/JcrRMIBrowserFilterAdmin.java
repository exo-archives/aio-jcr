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
 * Created by The eXo Platform SAS .
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
