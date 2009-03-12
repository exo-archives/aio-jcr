@if "%JONAS_ROOT%"=="" goto error
@goto ok

:error
@echo ERROR: set JONAS_ROOT first
@goto end

:ok
set CLASSPATH=%JONAS_ROOT%\lib\apps\exo.core.component.security.core-2.1.5-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.ws.rest.core-2.0.1-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.ws.rest.ext-2.0.1-SNAPSHOT
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\jsr311-api-1.0.jar.jar
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\client.jar

java -Djava.security.manager=default -Djava.security.policy=%JONAS_ROOT%\conf\java.policy org.objectweb.jonas.client.ClientContainer jcr-rest-ejb-connector-21.ear

@goto end

:end
