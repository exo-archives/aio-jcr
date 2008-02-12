@ECHO ========================== RUN ===========================

@REM @set JAVA_HOME=%JDK5_HOME%

@SET MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS% -Djava.io.tmpdir="C:\Tmp"

@start mvn clean test