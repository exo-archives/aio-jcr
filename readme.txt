JCR 1.10.3 release notes
========================

This is a Enterprise Edition version of 1.10 JCR. 
Version 1.10.3 includes all 1.10.2 features plus management extesions 
and XA transactions support (both based on Kernel 2.0.7 features).

Changes log of v.1.10.3:

Bug fixes:

JCR-807 - Getting workspace container from repository container
JCR-841 - ManagementContext is null on Replication service start

Task
JCR-798 - Merge EE features to 1.10.3
JCR-811 - Rename WebDAV-EJB connectors manner as it done in jcr-1.10.1.1



Previous JCR 1.10.2 features was:

- Replication bugfixes (serialization, priority handling)
- WebDAV resources visualization in Dreamweaver
- SessionRegistry concurrency improvements
- FTP service correctly integrated with eXo container (similar to HTTP), which allows JCR actions successful execution.

JCR 1.10.2 changes log:

Bug fixes

JCR-744 - Problem connecting to webdav server using Adobe Dreamweaver.
JCR-724 - TESTING: Functional testing (hgsql, priorities dynamic 3 nodes) problems
JCR-685 - TESTING: Functional testing (hgsql, priorities static 3 nodes) problems
JCR-681 - NullPointerException in TransientItemData.writeExternal()

Improvement

JCR-747 - FTP server should set current container the same as it is done for HTTP
JCR-638 - Change ObjectInput.read() to ObjectInput.readFully for all Externalizable classes.
JCR-617 - SessionRegistry improvements

Task

JCR-612 - Default ValueFactory in export operations
JCR-574 - Wrong console output in NodeType creation



Previous JCR 1.10.1 features was:

- JCR Organization Service refactoring
- Single-db Repository container dialects improved on delete operations for IBM DB2 (db2, db2v8) and PostgreSQL (pgsql).
- NodeTypeImpl definitions access optimization
- Contention improvements in Transactionable resource manager
- New feature: JCR Ext services configuration in Exo Registry


JCR 1.10.1 changes log:

Bug fixes

JCR-581 - Search does not work if the file contains a russian words
JCR-582 - Replication: Invalid string in exception
JCR-586 - Invalid String "ANONIMOUS" in authenticator
JCR-589 - The org.exoplatform.services.security.web.PortalContainerInitializedFilter doesn't release properly the resources stored into the ThreadLocal
JCR-590 - The org.exoplatform.frameworks.jcr.web.ThreadLocalSessionProviderInitializedFilter doesn't release properly the resources stored into the ThreadLocal
JCR-591 - The org.exoplatform.jcr.webdav.ejbconnector30.WebDAVEJBConnector doesn't release properly the resources stored into the ThreadLocal
JCR-597 - TransactionableResourceManager not thread safe and have high contention
JCR-599 - WebDAV in MAC always check-in node automatically
JCR-611 - The class org.exoplatform.services.jcr.ext.resource.jcr.Handler doesn't release properly the resources stored into the ThreadLocal
JCR-616 - Problem with encoding/decoding jcr:path from Row
JCR-621 - <sv:value/> problem in RestoreWorkspaceInitializer
JCR-622 - Import user generates NPE when creating a user that which point to a group that does not exist
JCR-629 - Duplicated group when finding groups by user

Improvement

JCR-596 - JCR Organization Service refactoring
JCR-618 - Delete operation on single-db container
JCR-620 - NodeTypeImpl definitions access optimization
JCR-628 - should not throws exception when the username and password is not matchs

New Feature

JCR-521 - JDBC storage reconnection to database
JCR-573 - JCR Ext services configuration in Exo Registry
JCR-594 - JCR session tracker

Task

JCR-600 - Check JcrURLConection for leak JCR sessions
JCR-603 - Monitoring of methods calls
JCR-604 - Use maven-antrun-plugin v.1.3
JCR-612 - Default ValueFactory in export operations
JCR-624 - CreateNewUser usecase benchmark


Main version JCR 1.10 features:

- JCR based Organization Service and Amazon SimpleDB storage support for Workspace. 
- Repositoy management extended with states ONLINE, READ-ONLY and OFFLINE. 
- Content Addressable External Values Storage
- New authentication policy mechanism 
- FCKeditor uses REST based access to JCR resources

Main improvements are
- Cache optimization for Items access and modification
- Replication members connection recovery and priority mechanism
- Replication BLOBs processing mechanism
- SessionProvider multithreading usage
- Item Move and Remove operations optimization
- ACL and Permissions logic upgraded

Changes log

- JCR-458 - JCR implementation of Organization Service
- JCR-460 - SimpleDB workspace container
- JCR-514 - Expose JCR state management
- JCR-410 - Improve backup configuration
- JCR-520 - Replication bandwidth allocation
- JCR-525 - Backup strategy for JRC under Proxy Replication

- JCR-566 - SearchManager listener registeres before RepositoryService start
- JCR-527 - NewUserListener causes a not required calls to the database
- JCR-542 - JCR Checkstyle
- JCR-538 - lower startup log statements
- JCR-539 - remove FTPConfig startup log statements
- JCR-555 - Do not bind jdbcjcr datasource
- JCR-534 - CASable tests fails sometimes on builder
- JCR-526 - Principles of Workspace storage sizing
- JCR-500 - Check and remove deprecated classes JCRAuthenticationListener, PortalAuthenticationPolicy, RemoteAuthenticationPolicy
- JCR-490 - Use new http client for functional web-dav tests
- JCR-491 - Use new http client for OO plugin
- JCR-455 - Checkstyle of JCR projects
- JCR-398 - Remove EDU.oswego.cs.dl.util.concurrent classes
- JCR-346 - Ms Office plugin: Add the possibility to create a directory

- JCR-531 - QPath.isDescendantOf optimization
- JCR-532 - Workspace cache optimization for JCR operations
- JCR-456 - Create Content Addressable value Storage
- JCR-473 - Value Storage plugin inherited from container parameters
- JCR-486 - Number and string with whitespaces in XPATH query.
- JCR-495 - FCKeditor resource REST path
- JCR-519 - System Node visibility causes storage additional persistent I/O
- JCR-529 - Concurrent modification of binary value on cluster (proxy mode).
- JCR-530 - Slow Move operation on large nodes count
- JCR-535 - Slow remove operation on large nodes amount
- JCR-541 - ACL identity and/or permissions cannot be null


Bug fixes

- JCR-433 - Can't get remove node event from ObservationManager.
- JCR-436 - Multithread add node cause "node ACL is null" Repository Exception
- JCR-450 - Missing jcr-mjdbc.ora.sql and jcr-mjdbc.ora-analyze.sql in JCR core
- JCR-477 - TESTING: Performance testing problems. Import operations
- JCR-533 - Unparseable date on Arabic Vista (error in startup, so JCR,ECM do not work correctly)
- JCR-540 - 'Null Stream data' exception on replication
- JCR-546 - Uploading a file of a large size problem
- JCR-550 - TESTING: Functional testing (hsql, api, usecases, impl, multi, cache is turned off) problems
- JCR-551 - '_x' auto escaped to '_x005F_x' when store xml by RegistryService
- JCR-553 - Problems with Unlock child node
- JCR-554 - Close of SessionProvider throws ConcurrentModificationException
- JCR-556 - java.util.ConcurrentModificationException in SessionProvider
- JCR-559 - Can not specific synonym, indexing config in repository configuration
- JCR-561 - Add call addAutoCreatedItems after autoctreating child node
- JCR-562 - Properties are not updated using webdav PROPPATCH method
- JCR-563 - Permission error and javax.jcr.AccessDeniedException when save node
- JCR-567 - Remove version history after remove mix:versionable node
- JCR-571 - Amazon S3 Value storage lost data large of max-buffer-size
- JCR-572 - Node permissions cannot be empty

- JCR-437 - Remove version history after removing non versioned parent node of versioned node.
- JCR-461 - The TransactionChangesLog have serialization problem when systemId == null in BackupManager
- JCR-465 - TESTING: Functional testing (pgsql, api, usecases, impl, single) problems
- JCR-466 - TESTING: Functional testing (pgsql, TCK, impl, single) problems
- JCR-467 - TESTING: Functional testing (oracle, TCK, single) problems
- JCR-493 - Encoding special characters in DefaultHighlighter
- JCR-507 - JCRPath validation
- JCR-516 - The middle priority member does not set state the read-only.
- JCR-518 - Sometimes the node with the middle/min priority does not pass in a condition read-only. Static priority.



JCR Samples
===========

1. Start Up (Tomcat)
   Tomcat 6 bundled can be started by executing the following commands:

      $CATALINA_HOME\bin\eXo.bat run          (Windows)

      $CATALINA_HOME/bin/eXo.sh run           (Unix)

2. After startup, the sample applications will be available by visiting:

    http://localhost:8080/browser - Simple JCR browser
        Browse the JCR repository that was started with Tomcat
    http://localhost:8080/fckeditor - FCK editor sample
        Edits the sample node using FCKEditor and browse it JCR browser
    http://localhost:8080/rest/jcr/repository/production - WebDAV service,
        Open in Microsoft Explorer, File-Open-OpenAsWebFolder with url http://localhost:8080/rest/jcr/repository/production
        Add/read/remove files there and browse it in the JCR browser or FTP.
        User name/password: admin/admin
    ftp://localhost:2121 - FTP server
        Open the repository in FTP client and browse the JCR repository started with Tomcat as FTP content,
        add/read/remove files there and browse it in the JCR browser or WebDAV.
    \\UniqCIFS - CIFS server (beta3)
        Open beta3 available CIFS service for browse the JCR repository as UNC path resource (see details in CIFS nodes below).

EAR deploy
==========

eXo JCR was tested under JBoss-4.2.2.GA and JOnAS-4.8.5 application servers

  Before use of eXo EAR you need to configure eXo JRC modifying exo-configuration.xml file, first of all you should configure
  ListenerService component which is configured for JBossAuthenticationListener by default
  (just replace it with JonasAuthenticationListener).
  Then put the configuration file to the root directory of an application server (same files as exo-configuration.xml can be found
  in any war file located in the EAR, e.g. fckeditor.war path /WEB-INF/classes/conf).

JBoss-4.2.2.GA

  1. Configuration

    * Copy <jcr.ear> into <%jboss_home%/server/default/deploy>
    * Put exo-configuration.xml to the root <%jboss_home%/exo-configuration.xml
    * Configure JAAS by inserting XML fragment shown below into <%jboss_home%/server/default/conf/login-config.xml>

---------
<application-policy name="exo-domain">
 <authentication>
      <login-module code="org.exoplatform.services.security.j2ee.JbossLoginModule" flag="required"></login-module>
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
    // Use the eXo Platform JAAS login module
  org.exoplatform.services.security.j2ee.JonasLoginModule  required;    
};
exo-domain {
    // Use the eXo Platform JAAS login module
    // The same as shown above, just another name
  org.exoplatform.services.security.j2ee.JonasLoginModule  required;     
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
           userClassNames="org.exoplatform.services.security.jaas.UserPrincipal"
           roleClassNames="org.exoplatform.services.security.jaas.RolePrincipal"
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


Migration from a pevious releases (pre 1.7)
===========================================
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
In JCR post-1.8 version auto-update option is disabled by default (update-storage='false' in workspace configuration).
Storage changes which should be updated to 1.8 it's a relations between database and external values storage(s). 
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


Resources
=========

 Company site        http://www.exoplatform.com
 Community JIRA      http://jira.exoplatform.org
 Comminity site      http://www.exoplatform.org
 Developers wiki     http://wiki.exoplatform.org
 Documentation       http://docs.exoplatform.org 