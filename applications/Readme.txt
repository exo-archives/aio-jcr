====== Deployment procedure to an application server ======

1. Make sure you have correct settings.xml (there should be the correct application server version etc.) and exo directory structure, Maven version 2.0.4 (or higher).
2. Run "mvn -f product-exo-jcr-as.xml clean install antrun:run" command.
3. If the command has executed successfully, go to exo-tomcat and run "eXo run" command.

For now you may automatically deploy our samples for tomcat 5.x application server only.  