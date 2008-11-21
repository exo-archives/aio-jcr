/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.ext.artifact.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.resource.NodeRepresentation;
import org.exoplatform.services.jcr.ext.resource.NodeRepresentationService;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

/**
 * @author Volodymyr Krasnikov volodymyr.krasnikov@exoplatform.com.ua
 * @version $Id: RESTArtifactLoaderService.java 11:37:47 andrew00x $
 */

@URITemplate("/maven2/")
public class RESTArtifactLoaderService implements ResourceContainer {

  private static final Log                  LOG      = ExoLogger.getLogger("jcr.ext.RESTArtifactLoaderService");

  /**
   * XHTML namespace.
   */
  private static final String               XHTML_NS = "http://www.w3.org/1999/xhtml";

  /**
   * Uses for represent JCR node in comfortable form.
   */
  private NodeRepresentationService         nodeRepresentationService;

  /**
   * Prepared JCR session. Can be null if username or password is not presents in configuration.
   */
  private Session                           session;

  /**
   * Root node for maven repository.
   */
  private String                            mavenRoot;

  /**
   * Repository name.
   */
  private String                            repository;

  /**
   * Workspace name.
   */
  private String                            workspace;

  /**
   * Keeps SessionProvider for user.
   */
  private ThreadLocalSessionProviderService sessionProviderService;

  /**
   * RepositoryService.
   */
  private RepositoryService                 repositoryService;

  /**
   * @param initParams
   *          the initialized parameters. Set repository name, workspace name, root node for Maven
   *          repository, username(optional) and password (optional).
   * @param sessionProviderService
   *          the ThreadLocalSessionProviderService.
   * @param repositoryService
   *          the RepositoryService.
   * @param nodeRepresentationService
   *          the NodeRepresentationService.
   * @param authenticator
   *          the Authenticator.
   * @throws Exception
   *           if any errors occur or not valid configuration.
   */
  public RESTArtifactLoaderService(InitParams initParams,
                                   ThreadLocalSessionProviderService sessionProviderService,
                                   RepositoryService repositoryService,
                                   NodeRepresentationService nodeRepresentationService,
                                   Authenticator authenticator) throws Exception {

    PropertiesParam props = initParams.getPropertiesParam("artifact.workspace");

    if (props == null)
      throw new IllegalArgumentException("Properties-param 'artifact.workspace' expected.");

    this.nodeRepresentationService = nodeRepresentationService;
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;

    this.repository = props.getProperty("repository");
    this.workspace = props.getProperty("workspace");

    this.mavenRoot = props.getProperty("mavenRoot");
    if (mavenRoot.startsWith("/"))
      mavenRoot = mavenRoot.substring(1);
    if (!mavenRoot.endsWith("/"))
      mavenRoot += "/";

    String username = props.getProperty("username");
    String password = props.getProperty("password");
    // prepare session if user will come as anonymous.
    if (username != null && password != null) {
      String userId = authenticator.validateUser(new Credential[] {
          new UsernameCredential(username), new PasswordCredential(password) });

      SessionProvider sessionProvider = new SessionProvider(new ConversationState(authenticator.createIdentity(userId)));
      this.session = sessionProvider.getSession(workspace,
                                                this.repositoryService.getRepository(repository));
    } else {
      LOG.info("Default username and password for access to JCR storage were not specified.");
    }

  }

  /**
   * Return Response with Maven artifact if it is file or HTML page for browsing if requested URL is
   * folder.
   * 
   * @param mavenPath
   *          the relative part of requested URL.
   * @param base
   *          the base URL.
   * @return @see {@link Response}.
   */
  @HTTPMethod("GET")
  @URITemplate("/{path}/")
  // Will be used for if transformer is not initialized by ResourceContainer.
  // Response.Builder.errorMessage set StringOutputTransformer.
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getResource(@URIParam("path") String mavenPath,
                              final @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String base) {

    String resourcePath = mavenRoot + mavenPath; // JCR resource
    mavenPath = base + getClass().getAnnotation(URITemplate.class).value() + mavenPath;

    try {

      Session ses = getSession(sessionProviderService.getSessionProvider(null));
      if (ses == null) {
        throw new RepositoryException("Access to JCR Repository denied. "
            + "SessionProvider is null and prepared session is null.");
      }

      Node node = ses.getRootNode().getNode(resourcePath);

      if (isFile(node))
        return downloadArtifact(node);
      else
        return browseRepository(node, mavenPath);

    } catch (PathNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("File not found " + mavenPath);
      LOG.info("File not found " + mavenPath);

      return Response.Builder.notFound()
                             .errorMessage(e.getMessage())
                             .mediaType("text/plain")
                             .build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.Builder.serverError()
                             .errorMessage(e.getMessage())
                             .mediaType("text/plain")
                             .build();
    }

  }

  /**
   * Browsing of root node of Maven repository.
   * 
   * @param base
   *          the base URL.
   * @return @see {@link Response}.
   */
  @HTTPMethod("GET")
  // Will be used for if transformer is not initialized by ResourceContainer.
  // Response.Builder.errorMessage set StringOutputTransformer.
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getRootNodeList(final @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String base) {
    return getResource("", base);
  }

  /**
   * Create JCR session if SessionProvider is not null, otherwise return default session.
   * 
   * @param sessionProvider
   *          the SessionProvider.
   * @return JCR session.
   * @throws Exception
   *           if any errors occurs.
   */
  private Session getSession(SessionProvider sessionProvider) throws Exception {
    // anonymous, user not authenticated or ThreadLocalSessionProvider was not set
    if (sessionProvider == null)
      return this.session;

    // user was authenticate or maybe has anonymous SessionProvider
    return sessionProvider.getSession(workspace, repositoryService.getRepository(repository));
  }

  /**
   * Check is node represents file.
   * 
   * @param node
   *          the node.
   * @return true if node represents file false otherwise.
   * @throws RepositoryException
   *           in JCR errors occur.
   */
  private static boolean isFile(Node node) throws RepositoryException {
    if (!node.isNodeType("nt:file"))
      return false;
    if (!node.getNode("jcr:content").isNodeType("nt:resource"))
      return false;
    return true;
  }

  /**
   * Create response for browsing Maven repository.
   * 
   * @param node
   *          the root node for browsing.
   * @param mavenPath
   *          the Maven path, used for creating &lt;a&gt; element.
   * @return @see {@link Response}.
   * @throws IOException
   *           if i/o error occurs.
   */
  private Response browseRepository(final Node node, final String mavenPath) throws IOException {

    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread() {
      @Override
      public void run() {
        try {
          XMLOutputFactory factory = XMLOutputFactory.newInstance();
          // name spaces
          factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
          XMLStreamWriter xsw = factory.createXMLStreamWriter(po, Constants.DEFAULT_ENCODING);
          xsw.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
          xsw.writeDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
          xsw.writeCharacters("\n");
          xsw.writeStartElement("html");
          xsw.writeDefaultNamespace(XHTML_NS);
          xsw.writeStartElement("head");
          xsw.writeStartElement("style");
          xsw.writeAttribute("type", "text/css");
          xsw.writeCharacters("a {text-decoration: none; color: #10409C;}"
              + "a:hover {text-decoration: underline;}" + ".centered { text-align: center; }"
              + ".underlined { border-bottom : 1px solid #cccccc; }\n");
          xsw.writeEndElement(); // style
          xsw.writeStartElement("title");
          xsw.writeCharacters("Maven2 Repository Browser");
          xsw.writeEndElement(); // title
          xsw.writeEndElement(); // head

          xsw.writeStartElement("body");
          //
          xsw.writeStartElement("h2");
          xsw.writeAttribute("class", "centered");
          xsw.writeCharacters("Maven2 Repository");
          xsw.writeEndElement();
          //
          xsw.writeStartElement("table");
          xsw.writeAttribute("width", "80%");
          // table header
          xsw.writeStartElement("tr");
          xsw.writeStartElement("th");
          xsw.writeAttribute("class", "underlined");
          xsw.writeCharacters("name");
          xsw.writeEndElement(); // th
          xsw.writeStartElement("th");
          xsw.writeAttribute("class", "underlined");
          xsw.writeCharacters("media-type");
          xsw.writeEndElement(); // th
          xsw.writeStartElement("th");
          xsw.writeAttribute("class", "underlined");
          xsw.writeCharacters("size");
          xsw.writeEndElement(); // th
          xsw.writeStartElement("th");
          xsw.writeAttribute("class", "underlined");
          xsw.writeCharacters("last modified");
          xsw.writeEndElement(); // th
          xsw.writeEndElement(); // tr
          // end table header

          // parent href
          String parent = mavenPath.substring(0, mavenPath.lastIndexOf('/'));
          xsw.writeStartElement("td");
          xsw.writeStartElement("a");
          xsw.writeAttribute("href", parent);
          xsw.writeCharacters("..");
          xsw.writeEndElement(); // a
          xsw.writeEndElement(); // td
          xsw.writeEmptyElement("td");
          xsw.writeEmptyElement("td");
          xsw.writeEmptyElement("td");

          NodeIterator iterator = node.getNodes();
          while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            xsw.writeStartElement("tr");
            if (RESTArtifactLoaderService.isFile(node)) {
              NodeRepresentation nodeRepresentation = nodeRepresentationService.getNodeRepresentation(node,
                                                                                                      null);
              xsw.writeStartElement("td");
              xsw.writeStartElement("a");
              xsw.writeAttribute("href", mavenPath.endsWith("/")
                  ? mavenPath + node.getName()
                  : mavenPath + "/" + node.getName());
              xsw.writeCharacters(node.getName());
              xsw.writeEndElement(); // a
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters(nodeRepresentation.getMediaType());
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters("" + nodeRepresentation.getContentLenght());
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters(new Date(nodeRepresentation.getLastModified()).toString());
              xsw.writeEndElement(); // td
            } else {
              xsw.writeStartElement("td");
              xsw.writeStartElement("a");
              xsw.writeAttribute("href", mavenPath.endsWith("/")
                  ? mavenPath + node.getName()
                  : mavenPath + "/" + node.getName());
              xsw.writeCharacters(node.getName());
              xsw.writeEndElement(); // a
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters("-");
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters("-");
              xsw.writeEndElement(); // td
              xsw.writeStartElement("td");
              xsw.writeAttribute("class", "centered");
              xsw.writeCharacters("-");
              xsw.writeEndElement(); // td
            }
            xsw.writeEndElement(); // tr
          }

          xsw.writeStartElement("tr");
          xsw.writeEndElement();

          xsw.writeEndElement(); // table
          xsw.writeEndElement(); // body
          xsw.writeEndElement(); // html

          xsw.writeEndDocument();
        } catch (XMLStreamException xmle) {
          xmle.printStackTrace();
        } catch (RepositoryException re) {
          re.printStackTrace();
        } finally {
          try {
            po.flush();
            po.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();

    // application/xhtml+xml content type is recommended for XHTML, but IE6
    // does't support this.
    return Response.Builder.ok().entity(pi, /* "application/xhtml+xml" */"text/html").build();

  }

  /**
   * Get content of JCR node.
   * 
   * @param node
   *          the node.
   * @return @see {@link Response}.
   * @throws Exception
   *           if any errors occurs.
   */
  private Response downloadArtifact(Node node) throws Exception {
    NodeRepresentation nodeRepresentation = nodeRepresentationService.getNodeRepresentation(node,
                                                                                            null);
    long lastModified = nodeRepresentation.getLastModified();
    String contentType = nodeRepresentation.getMediaType();
    long contentLength = nodeRepresentation.getContentLenght();
    InputStream entity = nodeRepresentation.getInputStream();
    Response response = Response.Builder.ok()
                                        .contentLenght(contentLength)
                                        .lastModified(new Date(lastModified))
                                        .entity(entity, contentType)
                                        .transformer(new PassthroughOutputTransformer())
                                        .build();

    return response;
  }

}
