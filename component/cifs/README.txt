For run CIFS(SMB) Server - it's standalone JCR CIFS service
make next steps:
 - prepare system (It's beta version, in future its gona be fixed)
  -- put Win32NetBIOS.dll to "C:\WINDOWS\system32\"
  -- check that configuration files is exist by path 
	"D:/exo/projects/jcr/trunk/component/cifs/src/main/java/conf/standalone/"	
         (its s temporary decision)

 - compile project into jar 
      >>mvn clean install

 - load all depended jar into target directory 
      >>mvn rar:rar

 - go to crated directory
      >>cd target\exo.jcr.component.cifs-1.6

 - run server
      >>java -cp exo.jcr.component.cifs-1.6.jar org.exoplatform.services.cifs.CIFSServerRun