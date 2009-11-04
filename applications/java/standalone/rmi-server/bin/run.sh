export dbin=`pwd`
export codebase=$dbin/../codebase
export confpath=$dbin/../config
java -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog  -Djava.security.policy=$confpath/java.policy \
-Djava.security.auth.login.config=$confpath/jaas.conf \
-Djava.rmi.server.codebase=file://$codebase/ -jar ../lib/exo.jcr.applications.rmi-server-1.10.5.1.jar bind $confpath/rmi-server-configuration.xml
