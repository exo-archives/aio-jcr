/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.web;

import java.net.MalformedURLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.naming.InitialContextInitializer;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Servlet context initializer that initializes standalone container at the context startup time.
 * To activate this your web.xml have to be configured like:
 * <listener>
 * <listener-class>org.exoplatform.frameworks.web.common.StandaloneContainerInitializedListener</listener-class>
 * </listener>
 * You may also specify an URL to the configuration.xml stored the configuration for StandaloneContainer
 * as servlet's init parameter called 'org.exoplatform.container.standalone.config'
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: StandaloneContainerInitializedListener.java 6739 2006-07-04 14:34:49Z gavrikvetal $
 */

public class StandaloneContainerInitializedListener implements
    ServletContextListener {

  /**
   * org.exoplatform.container.standalone.config
   */
  private static final String CONF_URL_PARAMETER = "org.exoplatform.container.standalone.config";
  
  //private final static String CONTAINER_CONFIG = "conf/standalone/exo-configuration.xml";
  
  //private final static String CONTAINER_CONFIG = "conf/exo-configuration.xml";

  private StandaloneContainer container;

  /* (non-Javadoc)
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */

  public void contextInitialized(ServletContextEvent event) {
    String configurationURL = event.getServletContext().
    getInitParameter(CONF_URL_PARAMETER);
    /*if(configurationURL == null) {
      configurationURL = Thread.currentThread().getContextClassLoader().getResource(
          CONTAINER_CONFIG).toString();
      //configurationURL = "conf/exo-configuration.xml";
    }*/
    try {
      //StandaloneContainer.setConfigurationURL(configurationURL);
      StandaloneContainer.addConfigurationURL(configurationURL);
      //if configurationURL is still == null StandaloneContainer will search
      //"exo-configuration.xml" in root of AS, then "conf/exo-configuration.xml"
      //in current classpath, then "conf/standalone/configuration.xml" in current classpath 
    } catch (MalformedURLException e1) {
    }

//    if (container == null) {
    try {
      container = StandaloneContainer.getInstance(Thread.currentThread()
          .getContextClassLoader());


      // Patch for tomcat InitialContext
      InitialContextInitializer ic = (InitialContextInitializer) container
          .getComponentInstanceOfType(InitialContextInitializer.class);
      if (ic != null)
        ic.recall();
      // ////////////////////////////////

      event.getServletContext().setAttribute(WebConstants.EXO_CONTAINER,
          container);
    } catch (Exception e) {
      e.printStackTrace();
    }
// }

  }

  /*
   * (non-Javadoc)
   *
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent event) {
    //container.stop();
  }
}
