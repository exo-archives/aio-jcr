@set MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS%

@start mvn clean test -Prun-tck -Drun-default=true