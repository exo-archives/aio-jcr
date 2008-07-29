@set MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS% -Dexo.test.skip=false  -Dexo.devtest.skip=true -Dexo.tck.skip=false -Dexo.test.forkMode=once

@start mvn clean test