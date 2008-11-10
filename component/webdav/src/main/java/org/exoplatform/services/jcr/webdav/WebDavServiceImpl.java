/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.webdav;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.webdav.command.CopyCommand;
import org.exoplatform.services.jcr.webdav.command.DeleteCommand;
import org.exoplatform.services.jcr.webdav.command.GetCommand;
import org.exoplatform.services.jcr.webdav.command.HeadCommand;
import org.exoplatform.services.jcr.webdav.command.LockCommand;
import org.exoplatform.services.jcr.webdav.command.MkColCommand;
import org.exoplatform.services.jcr.webdav.command.MoveCommand;
import org.exoplatform.services.jcr.webdav.command.OrderPatchCommand;
import org.exoplatform.services.jcr.webdav.command.PropFindCommand;
import org.exoplatform.services.jcr.webdav.command.PropPatchCommand;
import org.exoplatform.services.jcr.webdav.command.PutCommand;
import org.exoplatform.services.jcr.webdav.command.SearchCommand;
import org.exoplatform.services.jcr.webdav.command.UnLockCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.CheckInCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.CheckOutCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.ReportCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.UnCheckOutCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.VersionControlCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.NodeTypeUtil;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.CHECKIN;
import org.exoplatform.services.rest.CHECKOUT;
import org.exoplatform.services.rest.COPY;
import org.exoplatform.services.rest.LOCK;
import org.exoplatform.services.rest.MKCOL;
import org.exoplatform.services.rest.MOVE;
import org.exoplatform.services.rest.OPTIONS;
import org.exoplatform.services.rest.ORDERPATCH;
import org.exoplatform.services.rest.PROPFIND;
import org.exoplatform.services.rest.PROPPATCH;
import org.exoplatform.services.rest.REPORT;
import org.exoplatform.services.rest.SEARCH;
import org.exoplatform.services.rest.UNCHECKOUT;
import org.exoplatform.services.rest.UNLOCK;
import org.exoplatform.services.rest.VERSIONCONTROL;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

@Path("/jcr/")
public class WebDavServiceImpl implements WebDavService, ResourceContainer {

  /**
   * Default folder initialization node type.
   */
  public static final String                      INIT_PARAM_DEF_FOLDER_NODE_TYPE = "def-folder-node-type";

  /**
   * Default file initialization node type.
   */
  public static final String                      INIT_PARAM_DEF_FILE_NODE_TYPE   = "def-file-node-type";

  /**
   * Default file initialization mime type.
   */
  public static final String                      INIT_PARAM_DEF_FILE_MIME_TYPE   = "def-file-mimetype";

  /**
   * Initialization initialization "update-policy"-parameter value.
   */
  public static final String                      INIT_PARAM_UPDATE_POLICY        = "update-policy";

  /**
   * Logger.
   */
  private static Log                              log                             = ExoLogger.getLogger("jcr.WebDavServiceImpl");

  private final ThreadLocalSessionProviderService sessionProviderService;

  private final RepositoryService                 repositoryService;

  // private final ResourceBinder resourceBinder;

  private final NullResourceLocksHolder           nullResourceLocks;

  /**
   * Default folder node type.
   */
  private String                                  defaultFolderNodeType           = "nt:folder";

  /**
   * Default file node type.
   */
  private String                                  defaultFileNodeType             = "nt:file";

  /**
   * Default file mime type.
   */
  private String                                  defaultFileMimeType             = "application/octet-stream";

  private String                                  updatePolicyType                = "create-version";

  public WebDavServiceImpl(InitParams params,
                           RepositoryService repositoryService,
                           ThreadLocalSessionProviderService sessionProviderService,
                           ResourceBinder resourceBinder) throws Exception {
    this.sessionProviderService = sessionProviderService;
    this.repositoryService = repositoryService;
    // this.resourceBinder = resourceBinder;
    this.nullResourceLocks = new NullResourceLocksHolder();

    ValueParam pDefFolderNodeType = params.getValueParam(INIT_PARAM_DEF_FOLDER_NODE_TYPE);
    if (pDefFolderNodeType != null) {
      defaultFolderNodeType = pDefFolderNodeType.getValue();
      log.info(INIT_PARAM_DEF_FOLDER_NODE_TYPE + " = " + defaultFolderNodeType);
    }

    ValueParam pDefFileNodeType = params.getValueParam(INIT_PARAM_DEF_FILE_NODE_TYPE);
    if (pDefFileNodeType != null) {
      defaultFileNodeType = pDefFileNodeType.getValue();
      log.info(INIT_PARAM_DEF_FILE_NODE_TYPE + " = " + defaultFileNodeType);
    }

    ValueParam pDefFileMimeType = params.getValueParam(INIT_PARAM_DEF_FILE_MIME_TYPE);
    if (pDefFileMimeType != null) {
      defaultFileMimeType = pDefFileMimeType.getValue();
      log.info(INIT_PARAM_DEF_FILE_MIME_TYPE + " = " + defaultFileMimeType);
    }

    ValueParam pUpdatePolicy = params.getValueParam(INIT_PARAM_UPDATE_POLICY);
    if (pUpdatePolicy != null) {
      updatePolicyType = pUpdatePolicy.getValue();
      log.info(INIT_PARAM_UPDATE_POLICY + " = " + updatePolicyType);
    }

  }

  @CHECKIN
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response checkin(@PathParam("repoName") String repoName,
                          @PathParam("repoPath") String repoPath,
                          @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                          @HeaderParam(WebDavHeaders.IF) String ifHeader,
                          HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("CHECKIN " + repoName + "/" + repoPath);
    }

    Session session;
    try {
      session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader, ifHeader));
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
    return new CheckInCommand().checkIn(session, path(repoPath));
  }

  @CHECKOUT
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response checkout(@PathParam("repoName") String repoName,
                           @PathParam("repoPath") String repoPath,
                           @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                           @HeaderParam(WebDavHeaders.IF) String ifHeader,
                           HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("CHECKOUT " + repoName + "/" + repoPath);
    }

    Session session;
    try {
      session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader, ifHeader));
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }

    return new CheckOutCommand().checkout(session, path(repoPath));
  }

  @COPY
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response copy(@PathParam("repoName") String repoName,
                       @PathParam("repoPath") String repoPath,
                       @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
                       @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                       @HeaderParam(WebDavHeaders.IF) String ifHeader,
                       @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                       @HeaderParam(WebDavHeaders.OVERWRITE) String overwriteHeader,
                       @Context UriInfo baseURI,
                       HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("COPY " + repoName + "/" + repoPath);
    }

    try {
      String serverURI = baseURI + "/jcr/" + repoName;

      destinationHeader = TextUtil.unescape(destinationHeader, '%');

      if (!destinationHeader.startsWith(serverURI)) {
        return Response.status(HTTPStatus.BAD_GATEWAY).build();
      }

      String destPath = destinationHeader.substring(serverURI.length() + 1);
      String destWorkspace = workspaceName(destPath);
      String destinationPath = destinationHeader.substring(serverURI.length() + 1);

      List<String> lockTokens = lockTokens(lockTokenHeader, ifHeader);

      Depth depth = new Depth(depthHeader);

      if (overwriteHeader == null) {
        overwriteHeader = "F";
      }

      if (overwriteHeader.equalsIgnoreCase("T")) {
        delete(repoName, destinationPath, lockTokenHeader, ifHeader);
      } else {
        Session session = session(repoName, workspaceName(repoPath), null);
        String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
        Response prpfind = new PropFindCommand().propfind(session,
                                                          path(destinationPath),
                                                          body,
                                                          depth.getIntValue(),
                                                          uri);
        if (prpfind.getStatus() != HTTPStatus.NOT_FOUND) {

          return Response.status(HTTPStatus.PRECON_FAILED).build();
        }
      }

      if (depth.getStringValue().equalsIgnoreCase("infinity")) {
        String srcWorkspace = workspaceName(repoPath);

        if (srcWorkspace.equals(destWorkspace)) {
          Session session = session(repoName, destWorkspace, lockTokens);
          return new CopyCommand().copy(session, path(repoPath), path(destPath));
        }

        Session destSession = session(repoName, destWorkspace, lockTokens);
        return new CopyCommand().copy(destSession, srcWorkspace, path(repoPath), path(destPath));

      } else if (depth.getIntValue() == 0) {

        int nodeNameStart = repoPath.lastIndexOf('/') + 1;
        String nodeName = repoPath.substring(nodeNameStart);

        Session session = session(repoName, destWorkspace, lockTokens);

        return new MkColCommand(nullResourceLocks).mkCol(session,
                                                         path(destPath + "/" + nodeName),
                                                         defaultFolderNodeType,
                                                         null,
                                                         lockTokens);

      } else {
        return Response.status(HTTPStatus.BAD_REQUEST).build();
      }

    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response delete(@PathParam("repoName") String repoName,
                         @PathParam("repoPath") String repoPath,
                         @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                         @HeaderParam(WebDavHeaders.IF) String ifHeader) {

    if (log.isDebugEnabled()) {
      log.debug("DELETE " + repoName + "/" + repoPath);
    }

    try {
      Session session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader,
                                                                              ifHeader));
      return new DeleteCommand().delete(session, path(repoPath));
    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @GET
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response get(@PathParam("repoName") String repoName,
                      @PathParam("repoPath") String repoPath,
                      @HeaderParam(WebDavHeaders.RANGE) String rangeHeader,
                      @QueryParam("version") String version,
                      @Context UriInfo baseURI) {

    if (log.isDebugEnabled()) {
      log.debug("GET " + repoName + "/" + repoPath);
    }
    try {
      Session session = session(repoName, workspaceName(repoPath), null);

      ArrayList<Range> ranges = new ArrayList<Range>();

      if (rangeHeader != null) {

        if (log.isDebugEnabled()) {
          log.debug(rangeHeader);
        }

        if (rangeHeader.startsWith("bytes=")) {
          String rangeString = rangeHeader.substring(rangeHeader.indexOf("=") + 1);

          String[] tokens = rangeString.split(",");
          for (String token : tokens) {
            Range range = new Range();
            token = token.trim();
            int dash = token.indexOf("-");
            if (dash == -1) {
              return Response.status(HTTPStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            } else if (dash == 0) {
              range.setStart(Long.parseLong(token));
              range.setEnd(-1L);
            } else if (dash > 0) {
              range.setStart(Long.parseLong(token.substring(0, dash)));
              if (dash < token.length() - 1)
                range.setEnd(Long.parseLong(token.substring(dash + 1, token.length())));
              else
                range.setEnd(-1L);
            }
            ranges.add(range);
          }
        }
      }
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new GetCommand().get(session, path(repoPath), version, uri, ranges);

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @HEAD
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response head(@PathParam("repoName") String repoName,
                       @PathParam("repoPath") String repoPath,
                       @QueryParam("version") String version,
                       @Context UriInfo baseURI) {

    if (log.isDebugEnabled()) {
      log.debug("HEAD " + repoName + "/" + repoPath);
    }
    try {
      Session session = session(repoName, workspaceName(repoPath), null);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new HeadCommand().head(session, path(repoPath), uri);
    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @LOCK
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response lock(@PathParam("repoName") String repoName,
                       @PathParam("repoPath") String repoPath,
                       @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                       @HeaderParam(WebDavHeaders.IF) String ifHeader,
                       @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                       @HeaderParam(WebDavHeaders.TIMEOUT) String timeout,
                       HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("LOCK " + repoName + "/" + repoPath);
    }

    try {
      Session session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader,
                                                                              ifHeader));
      return new LockCommand(nullResourceLocks).lock(session,
                                                     path(repoPath),
                                                     body,
                                                     new Depth(depthHeader),
                                                     "86400");

    } catch (PreconditionException e) {
      log.error("PreconditionException " + e.getMessage());
      e.printStackTrace();
      return Response.status(HTTPStatus.PRECON_FAILED).build();

    } catch (NoSuchWorkspaceException e) {

      log.error("NoSuchWorkspaceException " + e.getMessage());
      e.printStackTrace();

      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (Exception e) {

      log.error("Unhandled Exception " + e.getMessage());
      e.printStackTrace();

      return Response.serverError().build();
    }
  }

  @UNLOCK
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response unlock(@PathParam("repoName") String repoName,
                         @PathParam("repoPath") String repoPath,
                         @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                         @HeaderParam(WebDavHeaders.IF) String ifHeader,
                         HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("UNLOCK " + repoName + "/" + repoPath);
    }
    Session session;
    List<String> tokens = lockTokens(lockTokenHeader, ifHeader);
    try {
      session = session(repoName, workspaceName(repoPath), tokens);
      return new UnLockCommand(nullResourceLocks).unLock(session, path(repoPath), tokens);

    } catch (NoSuchWorkspaceException e) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  @MKCOL
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response mkcol(@PathParam("repoName") String repoName,
                        @PathParam("repoPath") String repoPath,
                        @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                        @HeaderParam(WebDavHeaders.IF) String ifHeader,
                        @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
                        @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader) {
    if (log.isDebugEnabled()) {
      log.debug("MKCOL " + repoName + "/" + repoPath);
    }
    try {
      List<String> tokens = lockTokens(lockTokenHeader, ifHeader);
      Session session = session(repoName, workspaceName(repoPath), tokens);
      String nodeType = NodeTypeUtil.getNodeType(nodeTypeHeader);
      if (nodeType == null) {
        nodeType = defaultFolderNodeType;
      }

      return new MkColCommand(nullResourceLocks).mkCol(session,
                                                       path(repoPath),
                                                       nodeType,
                                                       NodeTypeUtil.getMixinTypes(mixinTypesHeader),
                                                       tokens);
    } catch (NoSuchWorkspaceException wexc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @MOVE
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response move(@PathParam("repoName") String repoName,
                       @PathParam("repoPath") String repoPath,
                       @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
                       @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                       @HeaderParam(WebDavHeaders.IF) String ifHeader,
                       @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                       @HeaderParam(WebDavHeaders.OVERWRITE) String overwriteHeader,
                       @Context UriInfo baseURI,
                       HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("MOVE " + repoName + "/" + repoPath);
    }

    try {
      String serverURI = baseURI + "/jcr/" + repoName;

      destinationHeader = TextUtil.unescape(destinationHeader, '%');

      if (!destinationHeader.startsWith(serverURI)) {
        return Response.status(HTTPStatus.BAD_GATEWAY).build();
      }

      String destPath = destinationHeader.substring(serverURI.length() + 1);
      String destWorkspace = workspaceName(destPath);
      String destinationPath = destinationHeader.substring(serverURI.length() + 1);

      String srcWorkspace = workspaceName(repoPath);

      List<String> lockTokens = lockTokens(lockTokenHeader, ifHeader);

      Depth depth = new Depth(depthHeader);

      if (overwriteHeader == null) {
        overwriteHeader = "F";
      }

      if (overwriteHeader.equalsIgnoreCase("T")) {
        delete(repoName, destinationPath, lockTokenHeader, ifHeader);
      } else {
        Session session = session(repoName, workspaceName(repoPath), null);
        String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
        Response prpfind = new PropFindCommand().propfind(session,
                                                          path(destinationPath),
                                                          body,
                                                          depth.getIntValue(),
                                                          uri);
        if (prpfind.getStatus() != HTTPStatus.NOT_FOUND) {
          return Response.status(HTTPStatus.PRECON_FAILED).build();
        }
      }

      if (depth.getStringValue().equalsIgnoreCase("Infinity")) {
        if (srcWorkspace.equals(destWorkspace)) {
          Session session = session(repoName, srcWorkspace, lockTokens);
          return new MoveCommand().move(session, path(repoPath), path(destPath));
        }

        Session srcSession = session(repoName, srcWorkspace, lockTokens);
        Session destSession = session(repoName, destWorkspace, lockTokens);
        return new MoveCommand().move(srcSession, destSession, path(repoPath), path(destPath));
      } else {
        return Response.status(HTTPStatus.BAD_REQUEST).build();
      }

    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }

  }

  @OPTIONS
  @Path("/{repoName}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response options(@PathParam("repoName") String repoName, HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("OPTIONS " + repoName);
    }
    ArrayList<String> commands = new ArrayList<String>();

    // List<ResourceDescriptor> descriptors = resourceBinder.getAllDescriptors();
    // for (int i = 0; i < descriptors.size(); i++) {
    // ResourceDescriptor descriptor = descriptors.get(i);
    //
    // String acceptableMethod = descriptor.getAcceptableMethod();
    // String uriPattern = descriptor.getURIPattern().getString();
    //
    // if (uriPattern.startsWith("/jcr/")) {
    // commands.add(acceptableMethod);
    // }
    // }

    String allowCommands = "";

    for (int i = 0; i < commands.size(); i++) {
      String curCommand = commands.get(i);
      allowCommands += curCommand;
      if (i < (commands.size() - 1)) {
        allowCommands += ", ";
      }
    }

    String DASL_VALUE = "<DAV:basicsearch>" + "<exo:sql xmlns:exo=\"http://exoplatform.com/jcr\"/>"
        + "<exo:xpath xmlns:exo=\"http://exoplatform.com/jcr\"/>";

    return Response.ok()
                   .header(WebDavHeaders.ALLOW, allowCommands)
                   .header(WebDavHeaders.DAV, "1, 2, ordered-collections")
                   .header(WebDavHeaders.DASL, DASL_VALUE)
                   .header(WebDavHeaders.MSAUTHORVIA, "DAV")
                   .build();
  }

  @ORDERPATCH
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response order(@PathParam("repoName") String repoName,
                        @PathParam("repoPath") String repoPath,
                        @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                        @HeaderParam(WebDavHeaders.IF) String ifHeader,
                        @Context UriInfo baseURI,
                        HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("ORDERPATCH " + repoName + "/" + repoPath);
    }

    try {
      List<String> lockTokens = lockTokens(lockTokenHeader, ifHeader);
      Session session = session(repoName, workspaceName(repoPath), lockTokens);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new OrderPatchCommand().orderPatch(session, path(repoPath), body, uri);
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PROPFIND
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response propfind(@PathParam("repoName") String repoName,
                           @PathParam("repoPath") String repoPath,
                           @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                           @Context UriInfo baseURI,
                           HierarchicalProperty body) {
    if (log.isDebugEnabled()) {
      log.debug("PROPFIND " + repoName + "/" + repoPath);
    }

    try {
      Session session = session(repoName, workspaceName(repoPath), null);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      Depth depth = new Depth(depthHeader);
      return new PropFindCommand().propfind(session, path(repoPath), body, depth.getIntValue(), uri);
    } catch (NoSuchWorkspaceException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (PreconditionException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.BAD_REQUEST).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PROPPATCH
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response proppatch(@PathParam("repoName") String repoName,
                            @PathParam("repoPath") String repoPath,
                            @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                            @HeaderParam(WebDavHeaders.IF) String ifHeader,
                            @Context UriInfo baseURI,
                            HierarchicalProperty body) {
    if (log.isDebugEnabled()) {
      log.debug("PROPPATCH " + repoName + "/" + repoPath);
    }

    try {
      List<String> lockTokens = lockTokens(lockTokenHeader, ifHeader);
      Session session = session(repoName, workspaceName(repoPath), lockTokens);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new PropPatchCommand(nullResourceLocks).propPatch(session,
                                                               path(repoPath),
                                                               body,
                                                               lockTokens,
                                                               uri);
    } catch (NoSuchWorkspaceException exc) {
      log.error("NoSuchWorkspace. " + exc.getMessage());
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response put(@PathParam("repoName") String repoName,
                      @PathParam("repoPath") String repoPath,
                      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                      @HeaderParam(WebDavHeaders.IF) String ifHeader,
                      @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
                      @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader,
                      @HeaderParam(WebDavHeaders.CONTENTTYPE) String mimeType,
                      InputStream inputStream) {

    if (log.isDebugEnabled()) {
      log.debug("PUT " + repoName + "/" + repoPath);
    }

    try {
      List<String> tokens = lockTokens(lockTokenHeader, ifHeader);
      Session session = session(repoName, workspaceName(repoPath), tokens);

      String fileNodeType = NodeTypeUtil.getNodeType(nodeTypeHeader);
      if (fileNodeType == null) {
        fileNodeType = defaultFileNodeType;
      }

      if (mimeType == null) {
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        mimeTypeResolver.setDefaultMimeType(defaultFileMimeType);
        mimeType = mimeTypeResolver.getMimeType(TextUtil.nameOnly(repoPath));
      }

      return new PutCommand(nullResourceLocks).put(session,
                                                   path(repoPath),
                                                   inputStream,
                                                   fileNodeType,
                                                   mimeType,
                                                   updatePolicyType,
                                                   tokens);

    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  @REPORT
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response report(@PathParam("repoName") String repoName,
                         @PathParam("repoPath") String repoPath,
                         @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
                         @Context UriInfo baseURI,
                         HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("REPORT " + repoName + "/" + repoPath);
    }

    try {
      Depth depth = new Depth(depthHeader);
      Session session = session(repoName, workspaceName(repoPath), null);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new ReportCommand().report(session, path(repoPath), body, depth, uri);
    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @SEARCH
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response search(@PathParam("repoName") String repoName,
                         @PathParam("repoPath") String repoPath,
                         @Context UriInfo baseURI,
                         HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("SEARCH " + repoName + "/" + repoPath);
    }

    try {
      Session session = session(repoName, workspaceName(repoPath), null);
      String uri = baseURI + "/jcr/" + repoName + "/" + workspaceName(repoPath);
      return new SearchCommand().search(session, body, uri);

    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @UNCHECKOUT
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response uncheckout(@PathParam("repoName") String repoName,
                             @PathParam("repoPath") String repoPath,
                             @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                             @HeaderParam(WebDavHeaders.IF) String ifHeader,
                             HierarchicalProperty body) {

    if (log.isDebugEnabled()) {
      log.debug("UNCHECKOUT " + repoName + "/" + repoPath);
    }

    try {
      Session session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader,
                                                                              ifHeader));
      return new UnCheckOutCommand().uncheckout(session, path(repoPath));

    } catch (NoSuchWorkspaceException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
  }

  @VERSIONCONTROL
  @Path("/{repoName}/{repoPath:.*}/")
  @Consumes("text/xml")
  @Produces("text/xml")
  public Response versionControl(@PathParam("repoName") String repoName,
                                 @PathParam("repoPath") String repoPath,
                                 @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
                                 @HeaderParam(WebDavHeaders.IF) String ifHeader) {

    if (log.isDebugEnabled()) {
      log.debug("VERSION-CONTROL " + repoName + "/" + repoPath);
    }

    Session session;
    try {
      session = session(repoName, workspaceName(repoPath), lockTokens(lockTokenHeader, ifHeader));
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }
    return new VersionControlCommand().versionControl(session, path(repoPath));
  }

  protected Session session(String repoName, String wsName, List<String> lockTokens) throws Exception {
    ManageableRepository repo = this.repositoryService.getRepository(repoName);
    SessionProvider sp = sessionProviderService.getSessionProvider(null);
    if (sp == null)
      throw new RepositoryException("SessionProvider is not properly set. Make the application calls"
          + "SessionProviderService.setSessionProvider(..) somewhere before ("
          + "for instance in Servlet Filter for WEB application)");

    Session session = sp.getSession(wsName, repo);
    if (lockTokens != null) {
      String[] presentLockTokens = session.getLockTokens();
      ArrayList<String> presentLockTokensList = new ArrayList<String>();
      for (int i = 0; i < presentLockTokens.length; i++) {
        presentLockTokensList.add(presentLockTokens[i]);
      }

      for (int i = 0; i < lockTokens.size(); i++) {
        String lockToken = lockTokens.get(i);
        if (!presentLockTokensList.contains(lockToken)) {
          session.addLockToken(lockToken);
        }
      }
    }
    return session;
  }

  protected String workspaceName(String repoPath) {
    return repoPath.split("/")[0];
  }

  protected String path(String repoPath) {
    String path = repoPath.substring(workspaceName(repoPath).length(), repoPath.length());

    if (!"".equals(path)) {
      return path;
    }

    return "/";
  }

  protected List<String> lockTokens(String lockTokenHeader, String ifHeader) {
    ArrayList<String> lockTokens = new ArrayList<String>();

    if (lockTokenHeader != null) {
      lockTokenHeader = lockTokenHeader.substring(1, lockTokenHeader.length() - 1);
      lockTokens.add(lockTokenHeader);
    }

    if (ifHeader != null) {
      String headerLockToken = ifHeader.substring(ifHeader.indexOf("("));
      headerLockToken = headerLockToken.substring(2, headerLockToken.length() - 2);
      lockTokens.add(headerLockToken);
    }

    return lockTokens;
  }

}
