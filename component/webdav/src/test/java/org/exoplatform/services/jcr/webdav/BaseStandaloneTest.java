package org.exoplatform.services.jcr.webdav;

import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.ProviderBinder;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public abstract class BaseStandaloneTest extends TestCase {

  protected static Log          log                   = ExoLogger.getLogger(BaseStandaloneTest.class);

  protected static String       TEMP_PATH             = "./temp/fsroot";

  protected static String       WORKSPACE             = "ws";

  public static final String    DEST_WORKSPACE        = "ws1";

  protected SessionImpl         session;

  protected SessionImpl         destSession;

  protected RepositoryImpl      repository;

  protected CredentialsImpl     credentials;

  protected Workspace           workspace;

  protected RepositoryService   repositoryService;

  protected Node                root;

  protected StandaloneContainer container;

  protected ProviderBinder      providers;

  protected ResourceBinder      resourceBinder;

  protected RequestHandlerImpl  requestHandler;

  public String                 defaultFileNodeType   = "nt:file";

  public String                 defaultFolderNodeType = "nt:folder";

  public String                 repoName;
  

  public void setUp() throws Exception {
    
    String containerConf = getClass().getResource("/conf/standalone/test-configuration.xml")
                                     .toString();

    String loginConf = BaseStandaloneTest.class.getResource("/login.conf").toString();

    StandaloneContainer.addConfigurationURL(containerConf);

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    credentials = new CredentialsImpl("admin", "admin".toCharArray());

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();
    repoName = repository.getName();
    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    destSession = (SessionImpl) repository.login(credentials, DEST_WORKSPACE);
    workspace = session.getWorkspace();
    //root = session.getRootNode();
    initRepository();
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    assertNotNull(sessionProviderService);
    sessionProviderService.setSessionProvider(null,
                                              new SessionProvider(new ConversationState(new Identity("admin"))));
    WebDavServiceImpl webDavServiceImpl = (WebDavServiceImpl) container.getComponentInstanceOfType(WebDavServiceImpl.class);
    assertNotNull(webDavServiceImpl);
    resourceBinder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(resourceBinder);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    assertNotNull(requestHandler);
    providers = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    
    
    root = session.getRootNode().addNode("webdav-test", "nt:folder");
    
    //session.save();
    
    
  }

  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data,
                                   ContainerResponseWriter writer) throws Exception {

    if (headers == null)
      headers = new MultivaluedMapImpl();

    ByteArrayInputStream in = null;
    if (data != null)
      in = new ByteArrayInputStream(data);

    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest(in,
                                                                in != null ? in.available() : 0,
                                                                method,
                                                                new InputHeadersMap(headers));
    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
                                                    new URI(requestURI),
                                                    new URI(baseURI),
                                                    in,
                                                    new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);
    requestHandler.handleRequest(request, response);
    return response;
  }

  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data) throws Exception {
    return service(method, requestURI, baseURI, headers, data, new DummyContainerResponseWriter());

  }

  protected void tearDown() throws Exception {
    
    root.remove();

    if (session != null) {
      try {
        session.refresh(false);
        Node rootNode = session.getRootNode();
        if (rootNode.hasNodes()) {
          // clean test root
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            Node node = children.nextNode();
            if (!node.getPath().startsWith("/jcr:system")) {
              // log.info("DELETing ------------- "+node.getPath());
              node.remove();
            }
          }
          session.save();
        }
      } catch (Exception e) {
        log.error("tearDown() ERROR " + getClass().getName() + "." + getName() + " " + e, e);
      } finally {
        session.logout();
      }
    }
    super.tearDown();

    // log.info("tearDown() END " + getClass().getName() + "." + getName());
  }

  protected abstract String getRepositoryName();

  public void initRepository() throws RepositoryException {
  }

  
  public String getPathWS() {
    return "/jcr/" + repoName + "/" + WORKSPACE;
  }

  public String getPathDestWS() {
    return "/jcr/" + repoName + "/" + DEST_WORKSPACE;
  }
}
