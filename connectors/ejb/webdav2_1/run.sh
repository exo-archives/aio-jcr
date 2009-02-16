#!/bin/sh

if ( test -z "$JONAS_ROOT" ); then
  echo "ERROR: set \$JONAS_ROOT first"
  exit 1
fi

$JAVA_HOME/bin/java -Djava.security.manager=default  \
-Djava.security.policy=$JONAS_ROOT/conf/java.policy \
-cp $JONAS_ROOT/lib/apps/exo.core.component.security.core-2.1.4-SNAPSHOT.jar:\
$JONAS_ROOT/lib/apps/exo.ws.rest.core-2.0-SNAPSHOT.jar:\
$JONAS_ROOT/lib/apps/exo.ws.rest.ext-2.0-SNAPSHOT.jar:\
$JONAS_ROOT/lib/apps/jsr311-api-1.0.jar:\
$JONAS_ROOT/lib/client.jar \
org.objectweb.jonas.client.ClientContainer webdav-ejb-connector-2_1.ear
