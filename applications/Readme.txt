====== Deployment procedure to an application server ======

1. Make sure you have correct: 
1.1. settings.xml. There should be the correct application server version (e.g <exo.projects.app.tomcat.version>) etc.
1.2. exo directory structure.
1.3. Maven version 2.2.1 (or higher).
1.4. Make sure you have run mvn install within ./config folder.  
2. Go to folder "exo.jcr.applications.tomcat" and run "mvn clean install -P deploy" command.
2.1 If you want to deploy JBoss or Jonas use exo.jcr.applications.jboss or exo.jcr.applications.jonas respectively.
3. If the command has executed successfully, go to exo-tomcat and run "eXo run" command.

Application server's specific configs is placed in product-patches/as/ folder.
Win32NetBIOS.dll - will be placed in root directory of an application server, this dll will be use by CIFS (Windows OS).
exo-configuration.xml - the main config file for the samles.
For now you may automatically deploy our samples (including ear) for tomcat, jboss and jonas application servers.  
Deployment procedure has been tested with tomcat6.x, jboss-4.2.3.GA and JONAS_4_8_5.


======= Manual EAR deployment procedure to JBoss AS =======

  1. Configuration
    * Copy exo-jcr-ear.ear into %jboss_home%/server/default/deploy
    * Put exo-configuration.xml to the root %jboss_home%/exo-configuration.xml
    * Configure JAAS by inserting XML fragment shown below into %jboss_home%/server/default/conf/login-config.xml

---------

<application-policy name="exo-domain">
 <authentication>
      <login-module code="org.exoplatform.services.security.j2ee.JbossLoginModule" flag="required"></login-module>
  </authentication>
 </application-policy>

---------

    * Configure character encoding by modifying HTTP connector into %jboss_home%/server/default/deploy/server/default/deploy/jboss-web.deployer/server.xml

---------

<Connector port="8080" address="${jboss.bind.address}".... 
         maxThreads="250" maxHttpHeaderSize="8192" 
         emptySessionPath="true" protocol="HTTP/1.1" 
         enableLookups="false" redirectPort="8443" acceptCount="100" 
         connectionTimeout="20000" disableUploadTimeout="true" URIEncoding="UTF-8" />

---------

  2. Replace %jboss_home%/server/default/lib/hsqldb.jar with newest one from distribution (hsqldb-1.8.0.7.jar).
  3. Start Up
     Execute
       * bin/run.bat on Windows
     or
       * bin/run.sh  on Unix


