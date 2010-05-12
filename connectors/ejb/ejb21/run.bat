@if "%JONAS_ROOT%"=="" goto error
@goto ok

:error
@echo ERROR: set JONAS_ROOT first
@goto end

:ok
set CLASSPATH=%JONAS_ROOT%\lib\apps\exo.core.component.security.core-2.1.9.jar
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\apps\exo.ws.commons-1.3.7.jar
set CLASSPATH=%CLASSPATH%;%JONAS_ROOT%\lib\client.jar

java -Djava.security.manager=default -Djava.security.policy=%JONAS_ROOT%\conf\java.policy org.objectweb.jonas.client.ClientContainer jcr-rest-ejb-connector-21.ear

@goto end

:end
