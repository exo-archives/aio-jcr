JCR 1.11.2 release notes
========================

eXoPlatform Java Content Repository (JSR-170) implementation and Extension services.

Main changes of version 1.11.2:


Changes log
===========

Bug

    * [JCR-863] - WIKI page How to+Persistent+JCR+Cluster seems to be outdated.
    * [JCR-874] - TESTING:Perfomance testing.Lock operations.Problems
    * [JCR-875] - ConcurrentModificationException on org.exoplatform.services.jcr.impl.dataflow.session.TransactionableResourceManager
    * [JCR-882] - Problem with restoring node on high priority after it was deleted on low priority
    * [JCR-895] - PUT with NodeType header allows to create a node with any known nodetype.
    * [JCR-897] - Removing not inconsistent items
    * [JCR-898] - Start listener may throw OutOfMemory Exception
    * [JCR-902] - Stop Backup chain when have no more operations.
    * [JCR-917] - Rare error in the tests: FileNotFoundException
    * [JCR-920] - Method PROPPATCH does not work for remove property
    * [JCR-921] - Method DELETE does not work correct with locked items
    * [JCR-922] - Method PROPPATCH does not work correct with locked items
    * [JCR-926] - LocalStorage changesLogs file may have not md5hash file after close nad restart AsyncReplication service.
    * [JCR-927] - Error after repeated restore
    * [JCR-932] - Chnange method 'restore' in BackupManager
    * [JCR-935] - Merge error after node restarted
    * [JCR-936] - Start changes listener does not clear storage.
    * [JCR-939] - In testunits use webdav-nodetypes.xml from test/ instead of jar. Jar should not contain any test data
    * [JCR-941] - NtFileNodeRepresentation returns null for multivalued property
    * [JCR-943] - SessionImpl.exportWorkspaceSystemView problem
    * [JCR-944] - NullPointerException in BackupChainImpl.startBackup
    * [JCR-945] - Asynchronous replication Subscriber FirtsChangesWaiter cancels receive without reason
    * [JCR-948] - WebDAV does not work correctly on MacOS X (10.5/Leopard)
    * [JCR-949] - Asynchronous replication rewrites not sent changes on restart
    * [JCR-953] - Node.getProperties() returns an iterator which contains duplicate properties.
    * [JCR-957] - Asyn replication Publisher sends files after CANCEL
    * [JCR-963] - BackupConsole can't apply user name and password when I try to use list command
    * [JCR-964] - External Value Storages doesn't support Values large of 2GiB
    * [JCR-966] - TESTING : ExportWorkspaceSystemViewTest fail
    * [JCR-968] - ItemDefKey equals and hashCode methods implementations
    * [JCR-969] - Problems with restored the workspace with complex structure
    * [JCR-972] - NPE when I try to get the content of a directory
    * [JCR-973] - The AR doesn't work anymore after getting a message of type "No members found or channel closed"
    * [JCR-974] - WorkspaceRestoreExeption throws when trying to restore workspace from backup
    * [JCR-975] - Correct node ACL initialization after import
    * [JCR-981] - ReplicationVersionRestoreTest fail
    * [JCR-986] - AsyncStartChangesListener does not return last changes
    * [JCR-994] - NodeType.getSupertypes should returns all supertypes of this node type
    * [JCR-999] - Server starts new backup when entered negative parameters ( Incremental job period, Incremental repetition number )
    * [JCR-1000] - AsyncReplication test fails
    * [JCR-1020] - TESTING: external weekly test - replication
    * [JCR-1027] - LocalStorageTest.testBrokenChangesLog test failed
    * [JCR-1028] - TESTING: Daily Testing (PropertyGetStream)
    * [JCR-1029] - TESTING: replication mode persistent and mode proxy
    * [JCR-1033] - Adopt new Logger to JCR components
    * [JCR-1036] - NoSuchMethodError Exception in Async tests
    * [JCR-1040] - java.lang.IndexOutOfBoundsException in BackupManagerImpl
    * [JCR-1043] - Failure after restore from backup
    * [JCR-1044] - TESTING: Functional testing (hgsql, backup, multi ) TestBackupManager problems
    * [JCR-1047] - Lose data when import version history
    * [JCR-1049] - Little issue in the Lock Manager configuration about the timeout
    * [JCR-1050] - The problem of working together BackupWorkspaceInitializer and AsyncReplication
    * [JCR-1069] - Problems with starting exo-jboss

Doc

    * [JCR-784] - Create an "How to set up and launch an Asynchronous Replication?" on the wiki
    * [JCR-814] - Wiki Article about JCR/Backup+service not complete
    * [JCR-907] - Workspace Data Container contract document
    * [JCR-979] - Database configuration how-to for multi-language support

Improvement

    * [JCR-838] - Asynchronous Replication : Should be possible with a Point-to-Point Communication between servers
    * [JCR-891] - Make WebDAV posibilty operate with node type expanding nt:resources.
    * [JCR-892] - Use NIO for JCR serialization
    * [JCR-893] - Use byte for read and write synchronization flags.
    * [JCR-894] - Node merge performance investigation
    * [JCR-900] - Finish time for Backup operations
    * [JCR-901] - PageList sanitization improvement
    * [JCR-903] - Asynchronous Backup restore support.
    * [JCR-906] - Replication transport Packet Handler
    * [JCR-916] - Remove method getNodeTypesHolder from ManageableRepository class
    * [JCR-930] - Asynchronous replication startable support
    * [JCR-933] - Bidirectional movement in ItemState iterator.
    * [JCR-934] - Add parameter to collection in MappedParametrizedObjectEntry and ContainerEntry
    * [JCR-938] - Check current backups in method BackupManagerImpl.getBackupsLogs().
    * [JCR-950] - Merge need ignore nodes with fixed identifiers
    * [JCR-951] - [Performance] rollback statement executed twice
    * [JCR-955] - Temporary files are not deleted in some special cases
    * [JCR-956] - Files received during Async replication are temporarily stored in disc several times
    * [JCR-958] - Serialization should use one BLOB file for many ChangesLog files.
    * [JCR-967] - Import binary value perfomance
    * [JCR-971] - Replication Proxy mode simple configuration
    * [JCR-976] - AddMetadataAction doesn't close Value InputStream
    * [JCR-992] - Don't copy bytes on createTransientCopy of ValueData
    * [JCR-1031] - MySQL integrity constraint violation special handling
    * [JCR-1035] - Avoid file duplication in online replication.
    * [JCR-1076] - RMI-server configuration update

New Feature

    * [JCR-923] - Change the method 'restore' in HTTPBackupAgent
    * [JCR-924] - Add the method 'getDefaultWorkspaceConfig' in HTTPBackupAgent
    * [JCR-947] - Add method 'infoRestores()' to HTTPBackupAgent.
    * [JCR-954] - Add restore to BackupManager based on SysViewWorkspaceInitializer
    * [JCR-965] - Needs to retrieve WorkspaceEntry from selected restore in Backup Service
    * [JCR-970] - Async replication Synchronization stop procedure
    * [JCR-987] - Use JSR-250 annotation for role based security in HTTPBackupAgent
    * [JCR-988] - Add parameter 'channel-name' to online replication.

Task

    * [JCR-887] - Research potential problems in merge when used ItemState.isSame() method.
    * [JCR-889] - Refactoring WebDAV service checkstyle, javadoc and etc.
    * [JCR-890] - Refactoring test for WebDAV service.
    * [JCR-896] - Asynchronous replication testing on EC2 infrastructure
    * [JCR-904] - HTTPBackupAgent REST-based testing
    * [JCR-910] - JCR 1.11.2 management
    * [JCR-918] - WorkspaceDataContainer constants are RDB related
    * [JCR-919] - Move Read-Only status to WorkspacePersistentDataManager
    * [JCR-929] - LocalStorage restart testing
    * [JCR-940] - create a REST component to represent a node as sys or doc XML
    * [JCR-946] - Update command restore in backup client
    * [JCR-993] - Remove temp status from FileStreamPersistedValueData
    * [JCR-1001] - JCR 1.11.2 release
    * [JCR-1004] - Give some support to the DMS Team to fix the remaining issues to create a DMS cluster
    * [JCR-1009] - Release JCR 1.11.2 before JBoss migration
    * [JCR-1030] - Remove WorkspaceContainer from repositoryContainer in case of unsuccessful repositoryContainer.registerWorkspace operation
    * [JCR-1037] - Use synchronous save for ItemsPersistenceListener
    * [JCR-1041] - Unable to initialize two eXo database on a single Oracle instance
    * [JCR-1042] - Not use org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference in GroovyScript2RestLoader.
    * [JCR-1048] - Apply Maven dependency management

    * [JCR-960] - BLOBs on-demand spool
    * [JCR-961] - Use File.rename when it's possible for BLOB Values
    * [JCR-978] - Test BLOB processing on distributes file-systems
    * [JCR-980] - QA testing of proxy mode simple configuration
    * [JCR-982] - Test connection failures for Async replication
    * [JCR-983] - Test bandwidth allocation on distributed networks.
    * [JCR-995] - Transient ValueData for persistent Value files
    * [JCR-996] - SpoolFile removed at the time of new TransientValueData creation
    * [JCR-997] - ValueData transient state flag
    * [JCR-998] - stopSynchronization() throw Exception
    * [JCR-1002] - JCR 1.11.2 pre-release testing
    * [JCR-1003] - Don't spool Value already stored in Values Storage
    * [JCR-1005] - Create the JIRAs in the DMS project in order to describe and explain how to reproduce the problems met by the JCR Team
    * [JCR-1006] - Help if needed the DMS Team
    * [JCR-1032] - Adopt new Logger to OpenOffice plugin



JCR 1.11.1 release notes
========================
- New Backup command line tool
- Asynchronous replication service rework around merge and changes transport
- Online replication transport and serialization refactoring
- Replication automatic workspace population from remote Repository.
- External Value Storage Update refactoring (transaction-style improved)
- Version History export/import util	

Changes log
===========

Bugs

    * [JCR-470] - TESTING: Daily performance testing problems. NodeMergeTest - elapsed time has increased
    * [JCR-557] - Impossible to stop server while spooling
    * [JCR-679] - Binary value size change
    * [JCR-708] - jcrvd files not getting cleaned up
    * [JCR-742] - OpenOffice webdav plugin: Problem with search in versionable files
    * [JCR-749] - javax.jcr dependency wrong declaration in RMI module
    * [JCR-765] - Exception is throwed when re-create entry continuous
    * [JCR-767] - Error with getting webdav resource encoded in iso-8859-5 or windows-1251 charsets
    * [JCR-770] - Can not create/edit nt:file in webdav by webdav client on Window SP2(seem same for Mac OS)
    * [JCR-774] - Fix webdav specification issues in 1.11
    * [JCR-778] - JCR Rest Web Browsing should be sort alphabetically by default
    * [JCR-779] - Problem during loading default node types if accessControlPolicy=disabled
    * [JCR-780] - TESTING: Functional testing (hsql, api, usecases, impl, multi, cache is turned off) problems
    * [JCR-781] - TESTING: Functional testing (hgsql, priorities dynamic 3 nodes) problems
    * [JCR-782] - TESTING: Functional testing (hsql, replication mode persistent&proxy) problems
    * [JCR-797] - Some OrgService benchmark tests are failed.
    * [JCR-799] - Problems with Emma Coverage Trend
    * [JCR-801] - Workspace configuranion fail
    * [JCR-805] - Auto-creation can make invalid nodes
    * [JCR-806] - Auto-creation should check if a node with the same name has already been define before performing the creation
    * [JCR-807] - Getting workspace container from repository container
    * [JCR-808] - Include results of makeAutoCreatedItems in NodeTypeDataManagerImpl.makeAutoCreatedNodes method
    * [JCR-813] - jcr webdav service does not provide header for media files
    * [JCR-815] - Problems with restored the workspace
    * [JCR-816] - Java heap space exception when run all async rplication tests.
    * [JCR-835] - Asynchronous Replication : Merge process fails in some cases
    * [JCR-841] - ManagementContext is null on Replication service start

Improvements

    * [JCR-513] - Provide cahce control headers for multimedia files
    * [JCR-558] - Investigate the thread_pool optimization in JGroups for the replication
    * [JCR-607] - Check if the class org.exoplatform.applications.jcr.browser.JCRBrowserFilter can better handle the Portal Container to be compatible with multiple portal instance
    * [JCR-608] - Check if the org.exoplatform.frameworks.jcr.web.CommandControllerServlet can better handle the Portal Container to be compatible with multiple portal instance
    * [JCR-609] - Check if the class org.exoplatform.frameworks.jcr.web.DisplayJCRContentServlet can better handle the Portal Container to be compatible with multiple portal instance
    * [JCR-610] - Check if the class org.exoplatform.frameworks.jcr.web.fckeditor.JCRContentFCKeditor can better handle the Portal Container to be compatible with multiple portal instance
    * [JCR-677] - PageList sanitization
    * [JCR-741] - Simplify cluster nodes initialization
    * [JCR-773] - Create serialization mechanism for JCR.
    * [JCR-775] - Asynchronous replication impovement
    * [JCR-783] - The FTPService should not print the FTP commands into the log file
    * [JCR-829] - Asynchronous Replication can be used for only one or several workspaces
    * [JCR-832] - Several different configurations of the Asynchronous Replication should be possible
    * [JCR-839] - Improve HTTPBackupAgent according to requirements
    * [JCR-849] - Add write and read String methods to serialization mechanism
    * [JCR-850] - The improvement online replication (ReplicationService)
    * [JCR-852] - External Value Storage Update refactoring
    * [JCR-859] - Cannot directly create a recursive nodeType
    * [JCR-867] - Remove depricated clear() method from interface PlainChangesLog

New Features

    * [JCR-675] - MySQL 5.1 support
    * [JCR-777] - Remote Workspace initialization
    * [JCR-786] - Backup console application

Tasks

    * [JCR-384] - Remove using JCRAppSessionFactory use SessionProvider related mechanism instead
    * [JCR-534] - CASable tests fails sometimes on builder
    * [JCR-726] - Test and document replication with all nodes with same priority
    * [JCR-772] - WebDAV OpenOffice plugin uses dependency on webdavclient v.1.9
    * [JCR-798] - Merge EE features to 1.10.3
    * [JCR-811] - Rename WebDAV-EJB connectors manner as it done in jcr-1.10.1.1
    * [JCR-817] - Export/Import version history
    * [JCR-831] - Applications build modules without project version
    * [JCR-833] - File system Value storage concurrent access prototyping.
    * [JCR-837] - Create plugin to ResistryService for initialize entries at start time
    * [JCR-840] - Migrate module configuration to use kernel_1_0.xsd
    * [JCR-843] - Remove unnecessary configuration from all the JARs' configuration.xml
    * [JCR-844] - SystemSearchManager changes buff clean after start
    * [JCR-861] - Use Reader-Writers serialization schema
    * [JCR-876] - WorkspaceStorageCacheImpl deprecated
    * [JCR-888] - Use parent pom 1.1.1 in trunk

Sub-tasks

    * [JCR-776] - The thread_pool optimization in JGroups for AsyncReplication
    * [JCR-787] - Merge refactoring
    * [JCR-788] - Stress testing
    * [JCR-789] - Versionable Nodes merge full support
    * [JCR-790] - Use JCR serialization
    * [JCR-791] - Listen workspace from start
    * [JCR-792] - Manual testing
    * [JCR-794] - Client application
    * [JCR-795] - Server side
    * [JCR-796] - Return backup status to a client
    * [JCR-800] - Uncomment the org.exoplatform.ws.frameworks.servlet.StandaloneContainerInitializedListener in rest.war/web.xml for exo-applications
    * [JCR-818] - Get realm from server
    * [JCR-819] - Use ThreadLocalSessionProviderService for getting the session in BackupServer.
    * [JCR-820] - Remove password and user name from URL in backup client
    * [JCR-821] - Rework of manual testing scenario
    * [JCR-822] - QA manual testing
    * [JCR-824] - Test the BackupServer on single-db workspace.
    * [JCR-825] - Add the parameter force-close-session to BackupServer and backup clinet
    * [JCR-827] - Add method closeSessions on specific workspace for SessionRegistry
    * [JCR-834] - Apply ENCRYPT protocol for transport layer
    * [JCR-836] - Create configuration AsyncReplication per workspace.
    * [JCR-842] - Make correct PairChangesLog cleanup in SystemLocalStorage
    * [JCR-851] - Use JCR serialization in online replication
    * [JCR-853] - Manual testing tool
    * [JCR-856] - Connection errors handling
    * [JCR-857] - Apply thread pool JGroups configuration
    * [JCR-858] - Advanced testing
    * [JCR-860] - Remote Exporter Versionable Nodes Support
    * [JCR-864] - Need export only version history of specified node
    * [JCR-869] - Investigation of slow merge on low priority
    * [JCR-870] - Skip root node changes
    * [JCR-871] - Skip RemoteWorkspaceInitializer changes
    * [JCR-878] - In ConnectionFailDetector the reconnection thread is never stop.
    * [JCR-883] - Problem with Update/Delete/Add sequence in merge
    * [JCR-886] - Let other threads work on packets receive



JCR 1.11 release notes
======================
- Asynchronous Replication service 
- Nodetypes/Namespaces edit feature 
- REST Groovy services management
- WebDAV service and other REST services dependent on WS 2.0 (JSR-311)
- JCR based Organization service performance improvement
- Extension actions on nodetypes hierarchy
- Improving of Session registry reliability in concurrent environment using Thread safe map
- WebDAV service specification bugfixes
- SimpleDB data container (transaction consistency on update rollback)
- External Values storage (transaction fail-over)
- Data container READ-ONLY status, on-save filters and save through READ-ONLY status mechanism
- Data container Ingres database backend (v.9.2) support
- Improved JMX support


Changes log
===========

New Feature

    * [JCR-585] - Ingres database support
    * [JCR-614] - Asynchronous replication
    * [JCR-636] - NodeTypes registration
    * [JCR-637] - Namespaces altering
    * [JCR-668] - Allow to launch action for subtypes of a nodetype that defined in action config
    * [JCR-715] - Create service for administration groovy scripts for REST via HTTP.
    * [JCR-722] - PersistentDataManager listeners filter


Improvement

    * [JCR-524] - Values storage on transaction fail-over
    * [JCR-617] - SessionRegistry improvements
    * [JCR-623] - Replacing org.apache.maven.wagon.observers.ChecksumObserver with some other class that works directly with InputStream, not byte array
    * [JCR-626] - Some HTTPHEaders included in javax.ws.rs.core.HttpHeaders are duplicated in jcr.webdav.WebDavHeaders
    * [JCR-632] - Decrease queries count in Organization service
    * [JCR-635] - NodeTypes refactoring
    * [JCR-638] - Change ObjectInput.read() to ObjectInput.readFully for all Externalizable classes.
    * [JCR-639] - Make all the MBeans be viewable within the JConsole or other JMX Console
    * [JCR-682] - AsyncPacket refactoring
    * [JCR-692] - SimpleDB container improvements
    * [JCR-717] - Add method that returns the list of deployed groovy-scripts to the GroovyScript2RestLoader class


Bugfixes:
    * [JCR-601] - Can not create node same name in the case (1 is lowercase and 1 is uppercase) with MySQL
    * [JCR-602] - Can not unlock node after sign out
    * [JCR-625] - WebDav CHECKIN, CHECKOUT and UNCHECKOUT MUST include a Cache-Control:no-cache header.
    * [JCR-641] - Webdav MOVE method returns wrong response status
    * [JCR-642] - Wrong locks of the collection.
    * [JCR-643] - Webdav PROPPATCH method returns wrong body of response with status 409:Conflict
    * [JCR-644] - Webdav Version-Control error: wrong value of "checked-in" property after setting of version control of resource
    * [JCR-645] - Webdav Version-Control error: wrong value of response status of method CHECKIN to the already checked-out resource
    * [JCR-646] - Webdav REPORT method to the non-version-controlled resource was failed.
    * [JCR-647] - Webdav Version-Control error of creating of first version after the resource was put under version-control
    * [JCR-648] - Webdav REPORT method returns wrong response "version-tree".
    * [JCR-649] - Webdav CHECKOUT method returns wrong status code after the resource was put under version-control.
    * [JCR-650] - Webdav CHECKOUT method coursed wrong name of checked-out property.
    * [JCR-651] - Webdav CHECKOUT returns wrong body of response with status 409:Conflict.
    * [JCR-652] - Webdav CHECKIN method issues.
    * [JCR-653] - Webdav UNCHECKOUT method issue: after UNCHECKOUT the resource property "checked-in" was not set.
    * [JCR-654] - Webdav UNCHECKOUT method does not return error message after the CHECKIN method to the same resource.
    * [JCR-655] - Webdav UNCHECKOUT method issue: repeated UNCHECKOUT method does not return error message.
    * [JCR-656] - Webdav ORDERPATCH method failed.
    * [JCR-657] - Bugs when query with frech character ' in when query
    * [JCR-658] - Webdav SEARCH method with "SQL" search language returns wrong status code 400:Bad Request.
    * [JCR-659] - Webdav SEARCH method with "XPATH" search language returns wrong status code 400:Bad Request.
    * [JCR-660] - Bug when query with special character in nodetype and combination values
    * [JCR-662] - Incorrect TransientValueData creation. Encoding problems
    * [JCR-676] - Webdav: PROPFIND prop method returns all properties values instead of returning the value of requested property
    * [JCR-680] - Reindexing node after property removing
    * [JCR-681] - NullPointerException in TransientItemData.writeExternal()
    * [JCR-685] - TESTING: Functional testing (hgsql, priorities static 3 nodes) problems
    * [JCR-700] - Operators "Greater than" and "Lower than" are misinterpreted in SQL requests
    * [JCR-710] - Same-name sibling Node delete isn't indexed
    * [JCR-711] - Open Office plugin: Unable to open files with names consisting non-latin symbols.
    * [JCR-724] - TESTING: Functional testing (hgsql, priorities dynamic 3 nodes) problems
    * [JCR-744] - Problem connecting to webdav server using Adobe Dreamweaver.
    * [JCR-750] - Transient ItemData non-ASCII path serialization
    * [JCR-762] - Fail TestBackupManager

Doc

    * [JCR-560] - Best practise in jcr doc for replication
    * [JCR-584] - Provide Documentation and Sample Code for "Replication"
    * [JCR-687] - WebDAV testing guide

Task

    * [JCR-69] - NodeTypes/Namespaces altering
    * [JCR-574] - Wrong console output in NodeType creation
    * [JCR-578] - Check the cifs failed tests
    * [JCR-579] - Test the "proxy" replication on JBoss
    * [JCR-612] - Default ValueFactory in export operations
    * [JCR-627] - Index Lucene native query
    * [JCR-640] - ExtendedPropertyType.nameFromValue function
    * [JCR-661] - DummyOrgService replace on JCROrgService
    * [JCR-669] - WorkspaceSynchronizer.getExportChanges(String uuid)
    * [JCR-670] - Change webdav-ejb connector according to new implementation of rest.
    * [JCR-673] - Use UPDATE state for reindex of same-name siblings on Delete and Move
    * [JCR-674] - Write AsyncChannelManager tests
    * [JCR-686] - Office plugins with WebDAV 1.11
    * [JCR-691] - Hide NullPointerException org.exoplatform.frameworks.jcr.web.ThreadLocalSessionProviderInitializedFilter#doFilter that occurs when ThreadLocalSessionProvider already cleaned.
    * [JCR-702] - WebDAV office plugins testing
    * [JCR-720] - NodeTypes registration benchmarking
    * [JCR-738] - Pre-release testing
    * [JCR-745] - Save through read-only status mechanism
    * [JCR-763] - TransientValueData should close source stream


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
        User name/password: root/exo
    ftp://localhost:2121 - FTP server
        Open the repository in FTP client and browse the JCR repository started with Tomcat as FTP content,
        add/read/remove files there and browse it in the JCR browser or WebDAV.
    Configure CIFS server (beta3) according the documentation:
        http://wiki.exoplatform.org/xwiki/bin/view/JCR/Quick+start 
        http://wiki.exoplatform.org/xwiki/bin/view/JCR/CIFS(SMB).
    Try it on Windows \\UniqCIFS, or on Unix smb://UniqCIFS 
        Open beta3 available CIFS service for browse the JCR repository.

EAR deploy
==========

eXo JCR was tested under JBoss-4.2.2.GA and JOnAS-4.9.2 application servers

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

JOnAS-4.9.2

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

 Company site        	http://www.exoplatform.com
 Documentation wiki   	http://wiki.exoplatform.org
 Community JIRA      	http://jira.exoplatform.org
 Comminity site      	http://www.exoplatform.org
 Community forum     	http://www.exoplatform.com/portal/public/en/forum			     
 JavaDoc site        	http://docs.exoplatform.org
 