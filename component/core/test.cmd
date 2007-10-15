
@REM mvn -Dtest.repository=db1 <tag>  <-- for all tests
@REM mvn -Dtest.repository=db1 -Dtest=TestName|Test*Name <tag>
@REM mvn -Dtest.repository=db1 -Dtest=TestName|Test*Name|package/Test* <tag>
@REM where <tag> == clean install | install | test

@ECHO ========================== RUN ===========================

@REM -cp "D:/Projects/eXo/dev/maven2/repository/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2.jar"
@REM -Dclassworlds.conf="D:/Projects/eXo/dev/maven2/bin/m2.conf"
@REM -Dmaven.home="D:/Projects/eXo/dev/maven2"
@REM -Dtest.repository=db1
@REM -Duser.language=en
@REM -Duser.region=us

@REM mvn -Dtest.repository=db1 -Dtest="org/apache/jackrabbit/test/api/SerializationTest.java" test

@REM mvn -Dtest.repository=db1 -Dtest=SerializationTest test

@REM @set JAVA_HOME=%JDK5_HOME%

@SET MAVEN_OPTS=-Duser.language=en -Duser.region=us %MAVEN_OPTS%

@start mvn clean test