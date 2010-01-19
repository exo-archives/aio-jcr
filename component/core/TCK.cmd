@set MAVEN_OPTS=-Duser.language=en -Duser.region=us -Dorg.exoplatform.jcr.monitor.jdbcMonitor %MAVEN_OPTS% 

@start mvn clean test -Prun-tck,-run-default-tests