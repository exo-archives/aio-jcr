
set JAVA_MEM_OPTS=-Xshare:auto -Xms128m -Xmx512m
set JAVA_CLASSPATH=-classpath target\exo.jcr.applications.backupconsole-1.11.1-SNAPSHOT.jar
 
java %JAVA_CLASSPATH% org.exoplatform.jcr.backupconsole.BackupConsole %*
