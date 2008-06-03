@set MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS% -Dexo.test.skip=true -Dexo.devtest.skip=true -Dexo.tck.skip=false -Dexo.test.forkMode=never

@start mvn clean test