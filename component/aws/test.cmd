@set MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS% -Dexo.test.skip=false -Dexo.tck.skip=true -Dexo.test.forkMode=never  -Dexo.aws.access.key=ACCESS_KEY -Dexo.aws.secret.key=SECRET_KEY %MAVEN_OPTS%  

@start mvn clean test