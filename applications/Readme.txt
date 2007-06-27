====== Deployment procedure to an application server ======

1. Make sure you have correct: 
1.1. settings.xml. There should be the correct application server version (e.g <exo.projects.app.tomcat.version>) etc.
1.2. exo directory structure.
1.3. Maven version 2.0.4 (or higher).
2. Run "mvn -f product-exo-jcr-as-tomcat6.xml clean install antrun:run" command.
3. If the command has executed successfully, go to exo-tomcat and run "eXo run" command.
4. You may use other product-exo-jcr-as* and an application server if you need.

Application server's specific configs is placed in product-patches/as/ folder.
Win32NetBIOS.dll - will be placed in root directory of an application server, this dll will be use by CIFS (Windows OS).
exo-configuration.xml - the main config file for the samles.
For now you may automatically deploy our samples (including ear) for tomcat, jboss and jonas application servers.  
Deployment procedure has been tested with tomcat5.x, jboss-4.0.3SP1 and JONAS_4_7_4.