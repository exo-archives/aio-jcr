#!/bin/sh

if ( test -z "$JONAS_ROOT" ); then
  echo "ERROR: set \$JONAS_ROOT first"
  exit 1
fi

$JAVA_HOME/bin/java -Djava.security.manager=default  \
-Djava.security.policy=$JONAS_ROOT/conf/java.policy \
-cp $JONAS_ROOT/lib/apps/exo.core.component.security.core-2.1.8.jar:\
$JONAS_ROOT/lib/apps/exo.ws.commons-1.3.6.jar:\
$JONAS_ROOT/lib/client.jar:\
exo.jcr.connectors.ejb21.bean-1.10.7-SNAPSHOT.jar \
org.objectweb.jonas.client.ClientContainer exo.jcr.connectors.ejb21.client-1.10.7.jar $1 $2
