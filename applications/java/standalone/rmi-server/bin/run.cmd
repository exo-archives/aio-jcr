set dbin=%~dp0
set codebase=%dbin%\..\codebase\
set confpath=%dbin%\..\config\
java -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog  -Djava.security.policy=%confpath%java.policy -Djava.security.auth.login.config=%confpath%jaas.conf -Djava.rmi.server.codebase=file:/%codebase% -jar ../lib/exo.jcr.applications.rmi-server-1.8.3.1.jar bind %confpath%rmi-server-configuration.xml
