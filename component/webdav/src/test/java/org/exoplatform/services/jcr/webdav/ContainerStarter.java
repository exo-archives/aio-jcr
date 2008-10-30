package org.exoplatform.services.jcr.webdav;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
//import org.codehaus.cargo.container.ContainerType;
//import org.codehaus.cargo.container.InstalledLocalContainer;
//import org.codehaus.cargo.container.configuration.ConfigurationType;
//import org.codehaus.cargo.container.configuration.LocalConfiguration;
//import org.codehaus.cargo.container.deployable.WAR;
//import org.codehaus.cargo.container.installer.Installer;
//import org.codehaus.cargo.container.installer.ZipURLInstaller;
//import org.codehaus.cargo.container.property.ServletPropertySet;
//import org.codehaus.cargo.generic.DefaultContainerFactory;
//import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
//import org.codehaus.cargo.util.log.LogLevel;
//import org.codehaus.cargo.util.log.SimpleLogger;
import org.exoplatform.services.log.ExoLogger;

public class ContainerStarter {

  private final Log             log            = ExoLogger.getLogger("ws.CargoContainer");

  protected static final String TEST_PATH      = (System.getProperty("testPath") == null
                                                   ? "."
                                                   : System.getProperty("testPath"));

  protected static final String CONTAINER_PATH = "http://www.apache.org/dist/tomcat/tomcat-6/v6.0.16/bin/apache-tomcat-6.0.16.zip";

  protected static final String LIBS_PATH      = "./target/libs";

//  public static InstalledLocalContainer cargoContainerStart(String port, String home) {
//    try {
//
//      if (port == null || port == "") {
//        // Default port
//        port = "8080";
//      }
//      if (home == null || home == "") {
//        // Default home
//        home = System.getProperty("java.io.tmpdir");
//        home = home + "/" + port;
//      }
//
//      Installer installer = new ZipURLInstaller(new java.net.URL(CONTAINER_PATH), home);
//      installer.install();
//
//      LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory().createConfiguration("tomcat5x",
//                                                                                                                    ContainerType.INSTALLED,
//                                                                                                                    ConfigurationType.STANDALONE);
//
//      configuration.setProperty(ServletPropertySet.PORT, port);
//
//      configuration.addDeployable(new WAR(TEST_PATH + "/test/war/browser.war"));
//      configuration.addDeployable(new WAR(TEST_PATH + "/test/war/fckeditor.war"));
//      configuration.addDeployable(new WAR(TEST_PATH + "/test/war/rest.war"));
//
//      InstalledLocalContainer container = (InstalledLocalContainer) new DefaultContainerFactory().createContainer("tomcat5x",
//                                                                                                                  ContainerType.INSTALLED,
//                                                                                                                  configuration);
//
//      container.setHome(installer.getHome());
//
//      String[] arr;
//      List<String> lst = new ArrayList<String>();
//      File dir = new File(LIBS_PATH);
//      arr = dir.list(new FilenameFilter() {
//        public boolean accept(File dir, String name) {
//          return name.endsWith(".jar");
//        }
//      });
//      for (String name : arr) {
//        lst.add(LIBS_PATH + "/" + name);
//      }
//      String[] arr2 = new String[lst.size()];
//      lst.toArray(arr2);
//
//      container.setExtraClasspath(arr2);
//      container.setOutput("tomcat.log");
//
//      SimpleLogger logger = new SimpleLogger();
//      logger.setLevel(LogLevel.WARN);
//
//      container.setLogger(logger);
//      container.start();
//      System.out.println("CargoContainer.containerStart() : " + container.getState().isStarted());
//
//      return container;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//
//  public static void cargoContainerStop(InstalledLocalContainer container) {
//    container.stop();
//    System.out.println("CargoContainer.containerStop() : " + container.getState().isStopped());
//  }
}
