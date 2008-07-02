eXo JCR core 1.7 relese notes
===============================

eXo JCR core 1.7 release contains improvements
- XML export/import, StAX based, BLOB files support. This feature may be used as main tool for custom backup/restore, migration.
- External values storage processing (including large files number support and Amazon S3 support), BLOB values processing. 
- The core indexer/serarch engine was optimized to use in concurrent environment, tuned default document order logic of query result.
- Lock persistence was refactored to be not-locked after the system restart.
- Core supports date formats 8601:2000-compliant, including short formats (like YYYY-MM, YYYY-MM-DD or YYYY-MM-DDThh:mmTZD) as well as RFC822 timezone formats.
- IPv6 suppored for replication, bind-ip-address and mcast_addr like "fe80::215:f2ff:febe:3343%4". It's possible to select dedicated IP-address for JGroups binding (bind-ip-address="192.168.0.15").
- WebDAV new implementaion based on eXo REST
- CIFS beta 2 implementaion, now NT LN 0.12 protocol supported.
- Mechanism of benchmarking of JCR has been improved. The implementation has been tested with up to 1000 threads that were emulating real users.
There was a lot of usecases such as concurrent adding folders, concurrent reading, concurrent traversing and some others.

Samples
=======

1. Start Up (Tomcat)
   Tomcat 6 bundled can be started by executing the following commands:

      $CATALINA_HOME\bin\eXo.bat run          (Windows)

      $CATALINA_HOME/bin/eXo.sh run           (Unix)

2. After startup, the sample applications will be available by visiting:

    http://localhost:8080/browser - Simple JCR browser
        Browse the JCR repository that was started with Tomcat
    http://localhost:8080/fckeditor - FCK editor sample
        Edits the sample node using FCKEditor and browse it JCR browser
    http://localhost:8080/rest/jcr/repository/portal - WebDAV service,
        Open it as webpage and run JavaWebStart WebDAV browser (eXo WebDAV client sample),
        or open in Microsoft Explorer, File-Open-OpenAsWebFolder with url http://localhost:8080/webdav/repository
        Add/read/remove files there and browse it in the JCR browser or FTP.
        User name/password: admin/admin
    ftp://localhost:2121 - FTP server
        Open the repository in FTP client and browse the JCR repository started with Tomcat as FTP content,
        add/read/remove files there and browse it in the JCR browser or WebDAV.
    \\UniqCIFS - CIFS server (beta2)
        Open beta2 available CIFS service for browse the JCR repository as UNC path resource (see details in CIFS nodes below).

EAR deploy
==========

eXo JCR was tested under JBoss-4.0.4GA,4.2.1GA and JOnAS-4.8.5 application servers

  Before use of eXo EAR you need to configure eXo JRC modifying exo-configuration.xml file, first of all you should configure
  ListenerService component which is configured for JBossAuthenticationListener by default
  (just replace it with JonasAuthenticationListener).
  Then put the configuration file to the root directory of an application server (same files as exo-configuration.xml can be found
  in any war file located in the EAR, e.g. fckeditor.war path /WEB-INF/classes/conf).

JBoss-4.2.0GA

  1. Configuration

    * Copy <jcr.ear> into <%jboss_home%/server/default/deploy>
    * Put exo-configuration.xml to the root <%jboss_home%/exo-configuration.xml
    * Configure JAAS by inserting XML fragment shown below into <%jboss_home%/server/default/conf/login-config.xml>

---------
<application-policy name="exo-domain">
 <authentication>
      <login-module code="org.exoplatform.services.jcr.impl.core.access.StandaloneJAASLoginModule" flag="required">
      </login-module>
      <login-module code="org.exoplatform.services.jcr.impl.core.access.StandaloneBroadcastJAASLoginModule" flag="required">
      </login-module>
  </authentication>
 </application-policy>
---------

  2. Replace <%jboss_home%/server/default/lib/hsqldb.jar> with newest one from this distribution (hsqldb-1.8.0.7.jar).
  3. Put Win32NetBIOS.dll (CIFS support) to <%jboss_home%/bin> directory.
  4. Start Up

     Execute
       * bin/run.bat on Windows
     or
       * bin/run.sh  on Unix

JOnAS-4.8.5

  1. Configuration

    * Copy <jcr.ear> into <%jonas_home%/apps/autoload>
    * Configure JAAS inserting XML fragment below into <%jonas_home%/conf/jaas.config>
    (if you have already configured login lodule for web container tomcat, delete it)

---------
tomcat {
   org.exoplatform.services.jcr.impl.core.access.StandaloneJAASLoginModule required;
   org.exoplatform.services.jcr.impl.core.access.StandaloneBroadcastJAASLoginModule required;
};
exo-domain {
    org.exoplatform.services.jcr.impl.core.access.StandaloneJAASLoginModule required;
    org.exoplatform.services.jcr.impl.core.access.StandaloneBroadcastJAASLoginModule required;
};
---------

    * Edit <%jonas_home%/conf/context.xml>, enable crossContext

---------

    <Context crossContext="true">

---------

    * Edit <%jonas_home%/conf/server.xml>, configure engine

---------
    <Engine name="jonas" defaultHost="localhost">
    .....

    <Realm className="org.objectweb.jonas.security.realm.web.catalina55.JAAS"
           userClassNames="org.exoplatform.services.security.impl.UserPrincipalImpl"
           roleClassNames="org.exoplatform.services.security.impl.RolePrincipalImpl"
           debug="99"/>

---------

    This Realm uses the JAAS model to authenticate users.
    User and Role Principals are eXo Platform specific.

    Make JOnAS Admin use the default database to identify users, go to the end of
    <%jonas_home%/conf/server.xml> file (the end <Host name="localhost" debug="0" appBase="webapps"...> tag), add this fragment:

---------
    <Context className="org.objectweb.jonas.web.catalina55.JOnASStandardContext" path="/jonasAdmin">
      <Realm className="org.objectweb.jonas.security.realm.web.catalina55.JACC" resourceName="memrlm_1"/>
    </Context>
---------


  2. Replace <%jonas_home%/lib/commons/jonas/hsqldb.jar> with newest one from this distribution (hsqldb-1.8.0.7.jar).
  3. Put Win32NetBIOS.dll (CIFS support) to <%jonas_home%/bin/nt> directory (Microsoft Windows(tm) option only).
  4. Copy log4j-1.2.8.jar into <%jonas_home%/lib/apps> directory
  5. Go to jonas.bat (the place where JONAS_OPTS are setting) and append the following:
  set JONAS_OPTS=%JONAS_OPTS% -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Djava.awt.headless=true
  set JAVA_OPTS=%JAVA_OPTS% -Xmx512M

  6. Start Up

     Execute
       * bin/nt/jonas.bat start <<(Windows)>>
     or
       * bin/unix/jonas start   <<(Unix)>>

  7. JOnAS is started on http://localhost:9000 address


Migration from a pevious releases
=================================
The Migration is possible via export/import to/from XML files.

For exporting some node (tree) to the XML file (system view) apply following to your eXo JCR 1.5.x repository
(where destPath - it is a path to the creating file)

 File destFile = new File(destPath);

 session.exportSystemView(source.getPath(), new FileOutputStream(destFile), false, false);
 //Do not forget close stream
 outStream.close();

For importing some node (tree) from the XML file (system view) apply following to your eXo JCR 1.6.1+ repository
(where pathToFile - it is a path to the file containing JCR data)

 File srcFile  = new File(pathToFile);
 //import data
 session.importXML (target.getPath(),
 new FileInputStream(destFile), ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
 // save in to persistent storage
 session.save();

NOTE: such an import procedure wont work for root node, so prepare several files for root's children and then import them one by one

More details are available on wiki http://wiki.exoplatform.org/xwiki/bin/view/JCR/How+to+JCR+import+export


Manual database migration 
=========================
As a custom solution it's possible to update database manually with SQL-script. 
In version 1.7 auto-update option is disabled by default (update-storage='false' in workspace configuration).
Storage changes which should be updated to 1.7 it's a relations between database and external values storage(s). 
'id' attribute in value-storage configuration was added. This attribute used instead absolute file path in values storage. 
The 'id' is stores in database table JCR_VALUES (JCR_MVALUES, JCR_SVALUES) column STORAGE_DESC. Before 1.7 it contains 
absolute path of value storage file. From 1.7 this field contains storage id.
So, for manual update you have to update your JCR_VALUES table column STORAGE_DESC with valid storage id, i.e. with value 
described in configuration. For example the workspace configuration has value-storage with id 'Storage #1'. 

    <value-storage id="Storage #1" class="org.exoplatform.services.jcr.impl.storage.value.fs.TreeFileValueStorage">
      <properties>
        <property name="path" value="/path/to/values/storage/workspace_name"/>
      </properties>
    .................

Then your script for single-db case will be next.

  UPDATE jcr_svalue SET storage_desc='Storage #1' WHERE storage_desc LIKE '/path/to/values/storage/workspace_name/%'

for multi-db 

  UPDATE jcr_mvalue SET storage_desc='Storage #1' WHERE storage_desc LIKE '/path/to/values/storage/workspace_name/%'

Notice, if you have more one storage per workspace the script should be more detailed in like-clause to touch only values located in targeted workspace.

After the successful update you have to update the JCR_SCONTAINER table VERSION column.

  UPDATE jcr_scontainer SET version='1.7' 

for multi-db 

  UPDATE jcr_mcontainer SET version='1.7' 

Migration done.

Bug fixes
=========
JCR-334 	Make dialogs of plugin in a separate library, from which they will be available for all applications of office.
JCR-333  	OpenOffice plugin can't open remote file from Search window
JCR-325 	Repository name seems to be ignored in WebDAV URL
JCR-323 	Remove dependency on Servlet API in WebDav service
JCR-309 	Remove version after restore 	Peter Nedonosko 
JCR-303 	JDBC configuration persister on Postgres 
JCR-296 	Issue while renaming a file using WebDAV 
JCR-293 	Escape ":" character when setting the JGroups channel and IPv6 is used 	
JCR-288 	String Value.getDate() format exception but content is 8601:2000-compliant
JCR-326 	NullPointerException on system view import
JCR-313 	Namespace mapping in export 

Changes and improvements
========================

JCR-341		Indexer/Search concurrent usage
JCR-308 	S3 stream data optimization
JCR-283 	Additional Search testing, refactoring
JCR-243 	Improve of exportDocumentView method (of JCR-220 )
JCR-220 	Change export to xml file encoding to UTF-8.
JCR-173 	Make a value storage plugin that stores content in Amazon S3 
JCR-339 	Improve import of mix:versionable nodes. 	
JCR-332 	Romove orderable states for ItemState class
JCR-329 	Swapped ValueData new lifecycle 	
JCR-328 	Be more tolerant with data inconsistancies at import
JCR-322 	Use only target item for actions launch in SessionActionInterceptor
JCR-320 	ItemState MOVE mockup (JCR-319)
JCR-317 	Value storage ID
JCR-312 	Same-Name Siblings for jcr:xmltext nodes 
JCR-307 	add jcr sql creation script for oracle  (multidb)	
JCR-305 	Create RemoveAuditableAction 
JCR-304 	Query default order optimization 
JCR-297 	Remove exception stacks when using webDAV 
JCR-295 	Remove lock after JCR restart. 
JCR-294 	Copy of locked node. 
JCR-292 	Hierarchical organized BLOB value storage 
JCR-291 	NT LM 0.12 SMB dialect implementation 
JCR-287 	Import/Export with StAX impl (BLOB operation optimized)


WebDav Service
==============
   Now you can access any workspace of your repository using following URL:
   Standalone mode:
      http://host:port/rest/jcr/{RepositoryName}/{WorkspaceName}/{Path}
   Portal mode:
      http://host:port/portal/rest/jcr/{RepositoryName}/{WorkspaceName}/{Path}

    Configuration
      [default-identity]
indicates the defaults login and password values for using as credentials for accessing the repository
[auth-header]
value of WWW-Authenticate header
[def-folder-node-type]
default node type which used for creation of collections
[def-file-node-type]
- default node type which used for creation of files
[def-file-mimetype]
used as default mime type
[update-policy]
- This parameter indicates one of the three cases when updating content of the resource by PUT command.

    Updates

    * simplified configuration;
    * using of the resources were removed;
    * client library contains 99 tests for testing the most used cases;
    * locking mechanism was updated. Now it contains valid timeout value;
    * implemented chunked transfer-encoding;
    * refactored properties working in PROPFIND, REPORT, PROPPATCH commands;
    * PROPNAMES type of the PROPFIND included


Microsoft Office Plugin Release notes
===================================== 

Allows :

    * Load files from JCR repository into MS Word;
    * Edit and Save files in repository;
    * Making full-text repository search;
    * View and Edit some version of the file and compare it with the base version (current state). 

    Updates

    * exo_jcr.msofficeplugin.common library created;
    * proxy settings was removed from Settings dialog;
    * small redesigning of the dialogs windows;
    * now size value shows in the friendly format;
    * the Sessings dialog checks on the correct input parameters;
    * the repository name configuration included in the Settings dialog;
    * versions of the file sorted by Creation Date;
    * new icons and mime-types included;


Open Office Add-On release notes
================================

    Allows

   1. Loading all known types of the files (.doc, .xls, .odt, .ods, .odp, .odj, ect..) from JCR repository in Open Office;
   2. Editing and then Saving files to the repository;
   3. Making full-text repository searching;
   4. Viewing and Editing any version of the file, and then saving it as BaseVersion of this file.

    Updates

    * small redesigning of the dialogs windows;
    * size value shows in the friendly format;
    * the repository name configuration included in the Settings dialog;
    * versions of the file sorted by Creation Date;
    * search error fixed: the property jcr:data always sends to client during searching;
    * fixrd the error while saving a non-existent file: now displays a message "Can't save the file";
    * now the query displays when store file with a another name (Save As), and a file of that name is exists.

Link Producer Service Release notes
=================================== 

   Now Link Producer Service is an extension of the REST Framework library and includes in the  WebDav service.

Rest servlet is available by href like:
  standalone moode:
http://host:port/rest/lnkproducer/filename.lnk?path=/reponame/workspace/[path]
  
  portal mode:
http://host:port/portal/rest/lnkproducer/filename.lnk?path=/reponame/workspace/[path]
    
    Updates

    * Lnk Producer is an extension of the REST framework library;
    * URL changed from http://host:port/lnkgenerator/.. to the http://host:port/rest/lnkproducer/...


CIFS Service
============

CIFS Server new version changes:
- NTLM 0.12 protocol dialect impementation added.NTLM 0.12 dialect is the newest of standart dialects.
- VirtualCircuits added - client-server session stability increased.
- java-implemented transport layer (NetBIOS) added. Server runs on nonWindoes systems now.
- user authentication implemented. Plain text and challange/response mechanism.
- file random write ability added. Just a small files (size <50Mb). File size limitation depends from common server and network productivity.
- server configuration updated.

Restrictions, defects, detected faults:
- file write restriction - server can't write large files. Server haven't time to finish file save. File size limitation depends from common server and network productivity.
- when server runs over nonWindows systems it's invisible in Microsoft Windows Neighbourhood.
- representation of and oparations over samename files are uncleare.
- if negotiated dialect is LanMan, there are faults with enters on server (enters from 2-3 attempt).
- if you have SVN installed on client, there may by shorttime hangs of clients whan you performs any operation in root folders.

Notes:
- NTFS streams is not supported;
- share level security is not supported;
- server support only newest dialects: LanMan and NTLM ;
- there are two features remain for complete server implementation. Its Lock support and Notify Change Handler implementation. First grant correct use of resources by concurent users. Second lets have see changes for user, without refrash resource content.

on-Windows server run notes:
- accessing server from Windows client, is same as for another windows smb-servers.
  Or run "\\<server name>", e.g. by default \\UniqCIFS.
- accessing server from Linux. Use   "smbclient -U <username> //<server ip or name>", e.g. "smbclient -U admin //192.168.0.1".

on-Linux server run notes:
- accessing server from Windows client is possible just if server binded with standart smb-transports ports (its 139, 138, 137).
- if server use non-standart ports (assigned by user in configuration.xml), accessing possible just from clients, who gives feature to explain which port client must use for connection. For example "smbclient -p 60139 //127.0.0.1/ws".
- server invisible in Network Neighbourhood for default, but if your compuer registered (have netbios name) and server binded with standart smb ports, windows-client can use server resources in usual case.


Resources
=========

 Company site        http://www.exoplatform.com
 Community JIRA      http://jira.exoplatform.org
 Comminity site      http://www.exoplatform.org
 Developers wiki     http://wiki.exoplatform.org
 Documentation       http://docs.exoplatform.org 