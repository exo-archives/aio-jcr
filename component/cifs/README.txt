

CIFS(SMB) Server Configuration 
------------------------------

Server configures at <project path>/src/main/java/conf/standalone/test/test-configuration.xml
<component><key>org.exoplatform.services.cifs.CIFSServiceImpl</key>...</component>
Check that server name ("netbiosname" or if not specified "server_name" )is unique in network 
neighbourhood, it may cause problems.

CIFSService is designed as multiplatform application, but at that moment we support only 
Windows systems and only using NetBIOS transport protocol. So if you use (for example) Linux
server will not work.


Build and run 
---------------
For run CIFS(SMB) Server - it's standalone JCR CIFS service

make next steps:
 - configure the server
  -- check the srever name!!

 - prepare system (It's beta version, in future its gona be fixed)
  -- put Win32NetBIOS.dll to "C:\WINDOWS\system32\"

 - compile project into jar 
      >>mvn clean install

 - load all depended jar into target directory 
      >>mvn rar:rar

 - go to crated directory
      >>cd target\exo.jcr.component.cifs-1.6

 - run server
      >>java -cp exo.jcr.component.cifs-1.6.jar org.exoplatform.services.cifs.CIFSServerRun


Connect to server
-----------------
There are different ways:
 First is looking for server whith name and in workgroup which you specified in configuration.
 Second, if can't find your server (it's possible in some cases with Master Browser),
it's write in explorer 
      >>\\<your server name>

In same way you can get any available resource on server
      >>\\<your server name>\<device\service name>\<file or folder> 





