@set MAVEN_OPTS=-Duser.language=en -Duser.region=us -Dexo.test.skip=true -Dexo.devtest.skip=true -Dexo.tck.skip=false -Dexo.test.forkMode=never -Dorg.exoplatform.jcr.monitor.jdbcMonitor %MAVEN_OPTS% 

@start mvn clean install