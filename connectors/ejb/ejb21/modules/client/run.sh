#!/bin/sh

if ( test -z "$JONAS_ROOT" ); then
  echo "ERROR: set \$JONAS_ROOT first"
  exit 1
fi

$JAVA_HOME/bin/java -Djava.security.manager=default  \
-Djava.security.policy=$JONAS_ROOT/conf/java.policy \
-cp $JONAS_ROOT/lib/apps/exo.core.component.security.core-2.1.3.jar:\
$JONAS_ROOT/lib/apps/exo.ws.commons-1.3.1.jar:\
$JONAS_ROOT/lib/client.jar:\
exo.jcr.webdav.ejbconnector2_1.bean-1.10.1.1.jar:\
exo.ws.rest.ejbconnector2_1.bean-1.3.1.jar \
org.objectweb.jonas.client.ClientContainer exo.jcr.webdav.ejbconnector2_1.client-1.10.1.1.jar $1 $2