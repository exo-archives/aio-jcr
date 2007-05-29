/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.RepositoryServiceImpl;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestSaveConfiguration extends TestCase {
  public String               XML_CONFIG;

  public String               JCR_CONFIG;

  public File                 FILE_XML_CONFIG = null;

  public File                 FILE_JCR_CONFIG = null;

  private boolean             isInitialized   = false;

  private StandaloneContainer container;

  @Override
  protected void setUp() throws Exception {
    if (!isInitialized) {
      FILE_XML_CONFIG = File.createTempFile("xml-config", ".xml");
      FILE_XML_CONFIG.deleteOnExit();
      FILE_JCR_CONFIG = File.createTempFile("jcr-config", ".xml");
      FILE_JCR_CONFIG.deleteOnExit();

      XML_CONFIG = "<?xml version='1.0' encoding='ISO-8859-1'?>"
          + "<configuration>"
          + "  <component>"
          + "    <key>org.exoplatform.services.log.LogConfigurationInitializer</key>"
          + "    <type>org.exoplatform.services.log.LogConfigurationInitializer</type>"
          + "    <init-params>"
          + "      <value-param>"
          + "        <name>logger</name>"
          + "        <value>org.exoplatform.services.log.impl.BufferedLog4JLogger</value>"
          + "      </value-param>"
          + "      <value-param>"
          + "        <name>configurator</name>"
          + "        <value>org.exoplatform.services.log.impl.Log4JConfigurator</value>"
          + "      </value-param>"
          + "      <properties-param>"
          + "        <name>properties</name>"
          + "        <description>Log4J properties</description>"
          + "        <property name='log4j.rootLogger' value='INFO, stdout, file'/>"
          + "        <property name='log4j.category.jcr.Test' value='DEBUG, stdout, file'/>"
          + "        <property name='log4j.appender.stdout' value='org.apache.log4j.ConsoleAppender'/>"
          + "        <property name='log4j.appender.stdout.threshold' value='DEBUG'/>"
          + "        <property name='log4j.appender.stdout.layout' value='org.apache.log4j.PatternLayout'/>"
          + "        <property name='log4j.appender.stdout.layout.ConversionPattern' value='%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n'/>"
          + "        <property name='log4j.appender.file' value='org.apache.log4j.FileAppender'/>"
          + "        <property name='log4j.appender.file.File' value='target/jcr.log'/>"
          + "        <property name='log4j.appender.file.layout' value='org.apache.log4j.PatternLayout'/>"
          + "        <property name='log4j.appender.file.layout.ConversionPattern' value='%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n'/>"
          + "      </properties-param>"
          + "    </init-params>"
          + "  </component>"
          + "  <component>"
          + "      <key>org.exoplatform.services.cache.CacheService</key>"
          + "      <type>org.exoplatform.services.cache.impl.CacheServiceImpl</type>"
          + "      <init-params>"
          + "      <object-param>"
          + "        <name>cache.config.default</name>"
          + "        <description>The default cache configuration</description>"
          + "        <object type='org.exoplatform.services.cache.ExoCacheConfig'>"
          + "          <field  name='name'><string>default</string></field>"
          + "          <field  name='maxSize'><int>100</int></field>"
          + "          <field  name='liveTime'><long>300</long></field>"
          + "          <field  name='distributed'><boolean>false</boolean></field>"
          + "          <field  name='implementation'><string>org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataCache</string></field>"
          + "        </object>"
          + "      </object-param>"
          + "    </init-params>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.log.LogService</key>"
          + "    <type>org.exoplatform.services.log.impl.LogServiceImpl</type>"
          + "    <!--Valid value: FATAL, ERROR, WARN, INFO, DEBUG, TRACE-->"
          + "      <init-params>"
          + "                   <properties-param>"
          + "                     <name>log.level.config</name> "
          + "                    <property name='org.exoplatform.services.log' value='INFO'/>"
          + "                   </properties-param>"
          + "      </init-params>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.idgenerator.IDGeneratorService</key>"
          + "    <type>org.exoplatform.services.idgenerator.impl.IDGeneratorServiceImpl</type>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.transaction.TransactionService</key>"
          + "    <type>org.exoplatform.services.transaction.impl.jotm.TransactionServiceJotmImpl</type>"
          + "    <init-params>"
          + "      <value-param>"
          + "        <name>timeout</name>"
          + "        <value>5</value>"
          + "      </value-param>"
          + "    </init-params>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.naming.InitialContextInitializer</key>"
          + "    <type>org.exoplatform.services.naming.InitialContextInitializer</type>"
          + "    <component-plugins>"
          + "      <component-plugin>"
          + "        <name>bind.datasource</name>"
          + "        <set-method>addPlugin</set-method>"
          + "        <type>org.exoplatform.services.naming.BindReferencePlugin</type>"
          + "        <init-params>"
          + "          <value-param>"
          + "            <name>bind-name</name>"
          + "            <value>jdbcjcr</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>class-name</name>"
          + "            <value>javax.sql.DataSource</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>factory</name>"
          + "            <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>"
          + "          </value-param>"
          + "          <properties-param>"
          + "            <name>ref-addresses</name>"
          + "            <description>ref-addresses</description>"
          + "            <property name='driverClassName' value='org.hsqldb.jdbcDriver'/>"
          + "            <property name='url' value='jdbc:hsqldb:file:target/temp/data/portal'/>"
          + "            <property name='username' value='sa'/>"
          + "            <property name='password' value=''/>"
          + "          </properties-param>"
          + "        </init-params>"
          + "      </component-plugin>"
          + "    <!-- Resource configuration for UserTransaction       use JOTM    -->"
          + "      <component-plugin>"
          + "        <name>jotm.tx</name>"
          + "        <set-method>addPlugin</set-method>"
          + "        <type>org.exoplatform.services.naming.BindReferencePlugin</type>"
          + "        <init-params>"
          + "          <value-param>"
          + "            <name>bind-name</name>"
          + "            <value>UserTransaction</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>class-name</name>"
          + "            <value>javax.transaction.UserTransaction</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>factory</name>"
          + "            <value>org.objectweb.jotm.UserTransactionFactory</value>"
          + "          </value-param>"
          + "          <properties-param>"
          + "            <name>ref-addresses</name>"
          + "            <description>ref-addresses</description>"
          + "            <property name='jotm.timeout' value='60'/>"
          + "          </properties-param>"
          + "        </init-params>"
          + "     </component-plugin>"
          + "     <component-plugin>"
          + "        <name>bind.jcr</name>"
          + "        <set-method>addPlugin</set-method>"
          + "        <type>org.exoplatform.services.naming.BindReferencePlugin</type>"
          + "        <init-params>"
          + "          <value-param>"
          + "            <name>bind-name</name>"
          + "            <value>repo</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>class-name</name>"
          + "            <value>javax.jcr.Repository</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>factory</name>"
          + "            <value>org.exoplatform.services.jcr.impl.jndi.BindableRepositoryFactory</value>"
          + "          </value-param>"
          + "          <properties-param>"
          + "            <name>ref-addresses</name>"
          + "            <description>ref-addresses</description>"
          + "            <property name='repositoryName' value='db1'/>"
          + "            <!-- property name='containerConfig' value='exo-configuration.xml'/ -->"
          + "          </properties-param>"
          + "        </init-params>"
          + "       </component-plugin>"
          + "       <component-plugin>"
          + "        <name>rmi.jcr</name>"
          + "        <set-method>addPlugin</set-method>"
          + "        <type>org.exoplatform.services.naming.BindReferencePlugin</type>"
          + "        <init-params>"
          + "          <value-param>"
          + "            <name>bind-name</name>"
          + "            <value>rmirepository</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>class-name</name>"
          + "            <value>javax.jcr.Repository</value>"
          + "          </value-param>"
          + "          <value-param>"
          + "            <name>factory</name>"
          + "            <value>org.exoplatform.services.jcr.rmi.RepositoryFactory</value>"
          + "          </value-param>"
          + "          <properties-param>"
          + "            <name>ref-addresses</name>"
          + "            <description>ref-addresses</description>"
          + "            <property name='url' value='//localhost:9999/repository'/>"
          + "          </properties-param>"
          + "        </init-params>"
          + "       </component-plugin>"
          + "    </component-plugins>"
          + "    <init-params>"
          + "      <properties-param>"
          + "        <name>default-properties</name>"
          + "        <description>Default initial context properties</description>"
          + "        <property name='java.naming.factory.initial' value='org.exoplatform.services.naming.SimpleContextFactory'/>"
          + "      </properties-param>"
          + "    </init-params>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.jcr.RepositoryService</key>"
          + "    <type>org.exoplatform.services.jcr.impl.RepositoryServiceImpl</type>"
          + "    <component-plugins>"
          + "      <component-plugin>"
          + "          <name>add.namespaces</name>"
          + "          <set-method>addPlugin</set-method>"
          + "          <type>org.exoplatform.services.jcr.impl.AddNamespacesPlugin</type>"
          + "          <init-params>"
          + "            <properties-param>"
          + "              <name>namespaces</name>"
          + "              <property name='test' value='http://www.apache.org/jackrabbit/test'/>"
          + "              <property name='exojcrtest' value='http://www.exoplatform.org/jcr/exojcrtest'/>"
          + "            </properties-param>"
          + "          </init-params>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>add.nodeType</name>"
          + "        <set-method>addPlugin</set-method>"
          + "        <type>org.exoplatform.services.jcr.impl.AddNodeTypePlugin</type>"
          + "        <init-params>"
          + "          <values-param>"
          + "            <name>nodeTypesFiles</name>"
          + "            <description>Node types configuration file</description>"
          + "            <value>jar:/conf/test/nodetypes-tck.xml</value>"
          + "            <value>jar:/conf/test/nodetypes-impl.xml</value>"
          + "          </values-param>"
          + "        </init-params>"
          + "      </component-plugin>"
          + "    </component-plugins>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.jcr.config.RepositoryServiceConfiguration</key>"
          + "    <type>org.exoplatform.services.jcr.impl.config.RepositoryServiceConfigurationImpl</type>"
          + "    <init-params>" + "      <value-param>" + "        <name>conf-path</name>"
          + "        <description>JCR configuration file</description>" +
          // /
          "        <value>"
          + "file:"+FILE_JCR_CONFIG.getAbsolutePath()
          + "        </value>"
          +
          // /
          "        </value-param>"
          + "    </init-params>"
          + "  </component>"
          + "  <component>"
          + "    <type>org.exoplatform.services.organization.impl.mock.DummyOrganizationService</type>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.listener.ListenerService</key>"
          + "    <type>org.exoplatform.services.listener.ListenerService</type>"
          + "    <component-plugins>"
          + "      <component-plugin>"
          + "        <name>exo.service.authentication.login</name>"
          + "        <set-method>addListener</set-method>"
          + "        <type>org.exoplatform.services.jcr.impl.core.access.JCRAuthenticationListener</type>"
          + "      </component-plugin>"
          + "    </component-plugins>"
          + "      </component>"
          + "  <component>"
          + "     <type>org.exoplatform.services.organization.auth.AuthenticationService</type>"
          + "  </component>"
          + "  <component>"
          + "    <key>org.exoplatform.services.document.DocumentReaderService</key>"
          + "    <type>org.exoplatform.services.document.impl.DocumentReaderServiceImpl</type>"
          + "    <component-plugins>"
          + "      <component-plugin>"
          + "        <name>pdf.document.reader</name>"
          + "        <set-method>addDocumentReader</set-method>"
          + "        <type>org.exoplatform.services.document.impl.PDFDocumentReader</type>"
          + "        <description>to read  the pdf inputstream</description>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>document.readerMSWord</name>"
          + "        <set-method>addDocumentReader</set-method>"
          + "        <type>org.exoplatform.services.document.impl.MSWordDocumentReader</type>"
          + "        <description>to read  the ms word inputstream</description>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>document.readerMSExcel</name>"
          + "        <set-method>addDocumentReader</set-method> "
          + "       <type>org.exoplatform.services.document.impl.MSExcelDocumentReader</type> "
          + "       <description>to read  the ms excel inputstream</description>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>PPTdocument.reader</name>"
          + "        <set-method>addDocumentReader</set-method>"
          + "        <type>org.exoplatform.services.document.impl.PPTDocumentReader</type>"
          + "        <description>to read  the ms ppt inputstream</description>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>TPdocument.reader</name>"
          + "        <set-method>addDocumentReader</set-method>"
          + "        <type>org.exoplatform.services.document.impl.TextPlainDocumentReader</type>"
          + "        <description>to read  the plain text inputstream</description>"
          + "      </component-plugin>"
          + "      <component-plugin>"
          + "        <name>document.readerOO</name>"
          + "        <set-method>addDocumentReader</set-method>"
          + "        <type>org.exoplatform.services.document.impl.OpenOfficeDocumentReader</type>"
          + "        <description>to read  the OO inputstream</description>"
          + "      </component-plugin>"
          + "    </component-plugins>"
          + "  </component>"
          + "  <external-component-plugins>"
          + "    <target-component>org.exoplatform.services.remote.group.CommunicationService</target-component>"
          + "    <component-plugin>"
          + "      <name>synchronize.cache.handler</name>"
          + "      <set-method>addPlugin</set-method>"
          + "      <type>org.exoplatform.services.cache.impl.SynchronizeCacheMessageHandler</type>"
          + "      <description>synchronize the cache</description>"
          + "    </component-plugin>"
          + "  </external-component-plugins>" + "</configuration>";

      JCR_CONFIG = "<repository-service default-repository='db1'>"
          + "  <repositories>"
          + "    <repository name='db1' system-workspace='ws' default-workspace='ws'>"
          + "     <security-domain>exo-domain</security-domain>"
          + "     <access-control>optional</access-control>"
          + "     <!--replication"
          + "        enabled='true'"
          + "        channel-config='TCP_NIO(use_send_queues=false;bind_addr=/LocalAddress/;enable_bundling=true;discard_incompatible_packets=true;max_bundle_size=640000;down_thread=false;use_outgoing_packet_handler=false;processor_queueSize=100;max_bundle_timeout=30;reader_threads=8;sock_conn_timeout=300;up_thread=false;loopback=false;skip_suspected_members=true;processor_minThreads=8;enable_diagnostics=false;processor_keepAliveTime=-1;writer_threads=8;processor_maxThreads=8;recv_buf_size=20000000;start_port=7800;use_incoming_packet_handler=true;send_buf_size=640000;processor_threads=8):MPING(bind_addr=/LocalAddress/;num_initial_members=2;up_thread=false;down_thread=false;mcast_addr=229.6.7.8;timeout=2000):FD_SOCK(up_thread=false;down_thread=false):pbcast.NAKACK(gc_lag=10;use_mcast_xmit=false;up_thread=false;retransmit_timeout=100,200,300,600,1200,2400,4800;down_thread=false;discard_delivered_msgs=true;max_xmit_size=600000):pbcast.STABLE(desired_avg_gossip=50000;max_bytes=400000;up_thread=false;down_thread=false;stability_delay=1000):VIEW_SYNC(avg_send_interval=60000;up_thread=false;down_thread=false):pbcast.GMS(print_local_addr=true;up_thread=false;join_timeout=3000;down_thread=false;join_retry_timeout=2000;shun=true):FC(max_block_time=1000;max_credits=2000000;up_thread=false;min_threshold=0.10;down_thread=false)'>"
          + "     </replication-->"
          + "      <authentication-policy>org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy</authentication-policy>"
          + "      <workspaces>"
          + "        <workspace name='ws' auto-init-root-nodetype='nt:unstructured' auto-init-permissions='any read;*:/admin read;*:/admin add_node;*:/admin set_property;*:/admin remove'>"
          + "      <container class='org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer'>"
          + "            <properties>"
          + "              <property name='sourceName' value='jdbcjcr'/>"
          + "              <property name='db-type' value='generic'/>"
          + "              <!-- property name='db-driver' value='org.hsqldb.jdbcDriver'/>"
          + "              <property name='db-url' value='jdbc:hsqldb:file:target/temp/data/portal'/>"
          + "              <property name='db-username' value='su'/>"
          + "              <property name='db-password' value=''/ -->"
          + "              <property name='multi-db' value='false'/>"
          + "              <property name='update-storage' value='true'/>"
          + "              <property name='max-buffer-size' value='204800'/>"
          + "              <property name='swap-directory' value='target/temp/swap/ws'/>"
          + "            </properties>"
          + "            <!-- value-storages>"
          + "              <value-storage class='org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage'>"
          + "                <properties>"
          + "                  <property name='path' value='target/temp/values/ws' />"
          + "                </properties>"
          + "                <filters>"
          + "                  <filter property-type='Binary' min-value-size='100000'/>"
          + "                </filters>"
          + "              </value-storage>              "
          + "            </value-storages -->"
          + "          </container>"
          + "          <cache enabled='true'>"
          + "            <properties>"
          + "              <property name='maxSize' value='10000'/>"
          + "              <property name='liveTime' value='1800'/><!-- 30 min -->"
          + "            </properties>"
          + "          </cache>"
          + "          <query-handler class='org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex'>"
          + "            <properties>"
          + "              <property name='indexDir' value='target/temp/index'/>"
          + "            </properties>"
          + "          </query-handler>"
          + "        </workspace>"
          + "        <workspace name='ws1' auto-init-root-nodetype='nt:unstructured'>"
          + "          <container class='org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer'>"
          + "            <properties>"
          + "              <property name='sourceName' value='jdbcjcr'/>"
          + "              <property name='db-type' value='generic'/>"
          + "              <property name='multi-db' value='false'/>"
          + "              <property name='update-storage' value='true'/>"
          + "              <property name='max-buffer-size' value='204800'/>"
          + "              <property name='swap-directory' value='target/temp/swap/ws1'/>"
          + "            </properties>"
          + "            <!-- value-storages>"
          + "              <value-storage class='org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage'>"
          + "                <properties>"
          + "                  <property name='path' value='target/temp/values/ws1'/>"
          + "                </properties>"
          + "                <filters>"
          + "                  <filter property-type='Binary'/>"
          + "                </filters>"
          + "              </value-storage>"
          + "            </value-storages -->"
          + "          </container>"
          + "          <cache enabled='true'>"
          + "            <properties>"
          + "              <property name='maxSize' value='10000'/>"
          + "              <property name='liveTime' value='1800'/><!-- 30 min -->"
          + "            </properties>"
          + "          </cache>"
          + "          <query-handler class='org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex'>"
          + "            <properties>"
          + "              <property name='indexDir' value='target/temp/index'/>"
          + "            </properties>"
          + "          </query-handler>"
          + "        </workspace>"
          + "        <workspace name='ws2' auto-init-root-nodetype='nt:unstructured'>"
          + "          <container class='org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer'>"
          + "            <properties>"
          + "              <property name='sourceName' value='jdbcjcr'/>"
          + "              <property name='db-type' value='generic'/>"
          + "              <property name='multi-db' value='false'/>"
          + "              <property name='update-storage' value='true'/>"
          + "              <property name='max-buffer-size' value='204800'/>"
          + "              <property name='swap-directory' value='target/temp/swap/ws2'/>"
          + "            </properties>"
          + "            <!-- value-storages>"
          + "              <value-storage class='org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage'>"
          + "                <properties>"
          + "                  <property name='path' value='target/temp/values/ws2'/>"
          + "                </properties>"
          + "                <filters>"
          + "                  <filter property-type='Binary'/>"
          + "                </filters>"
          + "              </value-storage>"
          + "            </value-storages -->"
          + "          </container>"
          + "          <cache enabled='true'>"
          + "            <properties>"
          + "              <property name='maxSize' value='2500'/>"
          + "              <property name='liveTime' value='1800'/>"
          + "            </properties>"
          + "          </cache>"
          + "          <query-handler class='org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex'>"
          + "            <properties>"
          + "              <property name='indexDir' value='target/temp/index'/>"
          + "            </properties>" + "          </query-handler>" + "        </workspace>"
          + "      </workspaces>" + "    </repository>" + "  </repositories>"
          + "</repository-service>";

      FileWriter fw = new FileWriter(FILE_XML_CONFIG);
      fw.write(XML_CONFIG);
      fw.flush();
      fw.close();

      FileWriter fw2 = new FileWriter(FILE_JCR_CONFIG);
      fw2.write(JCR_CONFIG);
      fw2.flush();
      fw2.close();

      StandaloneContainer.setConfigurationPath(FILE_XML_CONFIG.getAbsolutePath());
      // .setConfigurationPath("target/classes/conf/standalone/test/test-configuration-sjdbc.xml");

      container = StandaloneContainer.getInstance();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");

    }
  }

  public void testSaveConfiguration() throws Exception {
    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
    File conf = new File(FILE_JCR_CONFIG.getAbsoluteFile()+ "_" + format.format(new Date()));
    ((RepositoryServiceImpl) service).saveConfiguration();
    
    assertTrue(FILE_JCR_CONFIG.exists());
    assertTrue(conf.exists());
    conf.deleteOnExit();

  }

}
