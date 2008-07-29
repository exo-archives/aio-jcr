#!/bin/sh

VERSION=trunk

if ( test -z "$JONAS_ROOT" ); then
  echo "ERROR: set \$JONAS_ROOT first"
  exit 1
fi

$JAVA_HOME/bin/java -Djava.security.manager=default  \
-Djava.security.policy=$JONAS_ROOT/conf/java.policy \
-cp \
../connector/target/exo.jcr.webdav.ejbconnector3_0.bean-trunk.jar:\
$JONAS_ROOT/lib/apps/exo.core.component.security.core-$VERSION.jar:\
$JONAS_ROOT/lib/apps/exo.ws.commons-$VERSION.jar:\
$JONAS_ROOT/lib/client.jar:\
target/rar/easybeans-component-smartclient-client-1.0.1.jar:\
target/rar/easybeans-component-smartclient-api-1.0.1.jar:\
target/rar/easybeans-component-smartclient-1.0.1.jar:\
target/rar/easybeans-core-1.0.1.jar:\
target/rar/easybeans-api-1.0.1.jar:\
target/rar/ow2-ejb-3.0-spec-1.0-M1.jar:\
target/rar/util-log-1.0.6.jar:\
target/rar/util-i18n-1.0.6.jar:\
target/rar/easybeans-asm-3.0.jar:\
target/rar/easybeans-util-1.0.1.jar \
org.objectweb.jonas.client.ClientContainer target/exo.jcr.webdav.ejbconnector3_0.client-trunk.jar




