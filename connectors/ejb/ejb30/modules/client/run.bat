@if "%JONAS_ROOT%"=="" goto error
@goto ok

:error
@echo ERROR: set JONAS_ROOT first
@goto end

:ok
@set CLASSPATH=..\connector\target\exo.jcr.connectors.ejb30.bean-1.11.3-SNAPSHOT.jar
@set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.core.component.security.core-2.2.1.jar
@set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.ws.rest.core-2.0.1.jar
@set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.ws.rest.ext-2.0.1.jar
@set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\client.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-component-smartclient-client-1.0.1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-component-smartclient-api-1.0.1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-component-smartclient-1.0.1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-core-1.0.1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-api-1.0.1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\ow2-ejb-3.0-spec-1.0-M1.jar
@set CLASSPATH=%CLASSPATH%;target\rar\util-log-1.0.6.jar
@set CLASSPATH=%CLASSPATH%;target\rar\util-i18n-1.0.6.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-asm-3.0.jar
@set CLASSPATH=%CLASSPATH%;target\rar\easybeans-util-1.0.1.jar

java -Djava.security.manager=default -Djava.security.policy=%JONAS_ROOT%\conf\java.policy org.objectweb.jonas.client.ClientContainer target\exo.jcr.connectors.ejb30.client-1.11.3-SNAPSHOT.jar

@goto end

:end
