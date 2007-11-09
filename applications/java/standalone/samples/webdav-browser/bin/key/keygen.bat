call "%JAVA_HOME%\bin\keytool" -genkey -keystore Key_for_eXoDavBrowser -alias eXo

call "%JAVA_HOME%\bin\keytool" -selfcert -alias eXo -keystore Key_for_eXoDavBrowser

call "%JAVA_HOME%\bin\keytool" -list -keystore Key_for_eXoDavBrowser