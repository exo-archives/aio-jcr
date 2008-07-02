JCR 1.9 release notes
=====================

JCR version 1.9 includes a lot of new features and improvements such as: 

- Lucene 2.2 based Search engine with advanced indexing configuration, synonym and highlights support, 

  improved fuzzy search (http://lucene.apache.org/java/docs/queryparsersyntax.html),
  pluggable spell checker mechanism (http://wiki.apache.org/jakarta-lucene/SpellChecker),

- Query result now supports two-way NodeIterator.

  More details about Indexer and Serach configuration described here:
  http://wiki.exoplatform.com/xwiki/bin/view/JCR/Searching+Repository+Content

  http://wiki.exoplatform.com/xwiki/bin/view/JCR/Search+Configuration

- Advanced replication service with new synchronization subsytem which allows implementing scalable cluster


- JCR Resource service providing flexible way for node represntation as well as using jcr:// URL scheme
 
- Support for REST services on Groovy script.

- Indexer has new mechanism for I/O errors catching and index storage restoring after system failures.


- XML Import/Export improvements including dedicated logic for mix:versionable nodes and collisions logic refactoring.

- Used new security layer (eXo Core 2.1). New LoginModules implementaions including J2EE servers (Tomcat, JBoss, JOnAS).


- JCR comes with new cache implementation, the cache has lightweight removal logic, supports blocked and unblocked read modes.

- Samples now use JCR Sessions provider mechanism  

- Audit management supports versionable nodes records, new audit storage improved.


- JCR Sessions provider mechanism is refactored 

 

Changes & improvements
======================

Improvement  	JCR-235  	JCR query code reorganization according to the Lucene 2.0 indexer

Improvement 	JCR-339 	Improve import of mix:versionable nodes. 	Sergey Kabashnyuk
Task 		JCR-359 	Add new parameter in DisplayJCRContentServlet to show that which repository is is chosen when use this servlet 
Improvement 	JCR-373 	Ancestor to save after replacing existed node on import

Task	 	JCR-379 	Test the replication service with persistent mode
Task 		JCR-382 	Replace Credentials with identity in SessionImpl
Task	 	JCR-383 	Remove deprecated code which is not used 
Improvement 	JCR-385 	Check IMPORT_UUID_COLLISION_REMOVE_EXISTING and IMPORT_UUID_COLLISION_REPLACE_EXISTING usecases during the import of xml document

Improvement 	JCR-386 	Check correct calculating of same name siblings during operation workspace.copy(), workspace.clone(), node.update()
Improvement 	JCR-391 	Anonymous logic
Sub-task 	JCR-392 	JCR-235 Describe SearchManager component life cycle

Sub-task 	JCR-402 	JCR-235 Improve documentation
Task 		JCR-409 	Workspace cache optimization
Improvement 	JCR-412 	Make post-persistent operations within a storage transaction 

Task 		JCR-416 	Workspace importer independent on session
Improvement 	JCR-417 	Workspace restore from initial data snapshot
Improvement 	JCR-418 	JCR exporter independent on session 	
New Feature 	JCR-419 	Auditable and versionable node support 	

Improvement 	JCR-422 	Demo web applications redesign 
Task 		JCR-423 	Audit introduction and documentation
Improvement 	JCR-424 	Checking size of AccessControlList
Improvement 	JCR-425		Mechanism for accessing JCR resources by URL, for example jcr://root:exo@repository/collaboration#/root/test

Sub-task 	JCR-426 	JCR-412 Indexer on-error log and restore
New Feature 	JCR-429 	Search keyword suggestion with new Lucence
New Feature 	JCR-430 (WS-36) Loader for groovy scripts for REST
Task 		JCR-431 	Configuration fields binding 	Peter Nedonosko 

Task 		JCR-432 	Do we need in MultiIndex.checkFlush() read QueryHandlerEntry configuration each check 
Improvement 	JCR-438 	JCR configuration timing formats 
Improvement 	JCR-439 	Cluster synchronization 	
Task 		JCR-440 	Cluster setup and maintenance tests and guide

Task 		JCR-441 	NodeIndexer should not read non indexable values
Improvement 	JCR-442 	Create TwoWayScoreNodeIterator 
Improvement 	JCR-444 	Remove traces in AddAuditableAction 	
Task 		JCR-445 	QueryHandlerEntry configuration parameters XML names

Task 		JCR-446 	ConfigurationPersister parameters XML names
Task 		JCR-447 	RepositoryConfiguration XML names


Bug fixes
=========

Bug 	JCR-352 	Problem with davfs2
Bug 	JCR-355 	database error with Workspace.clone()

Bug 	JCR-380 	error when importing portlets or starting the ecm 
Bug 	JCR-399 	Changed propery was not reflected in search index, so content of this property can't be finded 
Bug 	JCR-403 	Access manager vulnerability

Bug 	JCR-406 	TCK query tests fails if QueryManager was called before in other JVM (by starting Exo tests)
Bug 	JCR-413 	ConstraintViolationException after use AddAuditableAction
Bug 	JCR-414 	AccessDeniedException on AuditAction 

Bug 	JCR-434 	ConstraintViolationException: Can not define node-type for node :CategoryData 
Bug 	JCR-435 	SystemSearchManager indexer locked on stop
Bug 	JCR-437 	Remove version history after removing non versioned parent node of versioned node.

Bug 	JCR-443 	ConcurrentModificationException on LockManagerImpl.removeExpired


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