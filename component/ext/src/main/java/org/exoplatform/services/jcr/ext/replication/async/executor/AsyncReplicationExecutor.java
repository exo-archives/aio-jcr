/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.executor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.common.http.client.ParseException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication;
import org.exoplatform.services.jcr.ext.replication.test.ReplicationTestService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 28.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationExecutorService.java 111 2008-11-11 11:11:11Z rainf0x $
 */

@Path("/async-replication-executor/")
@Produces("text/plain")
public class AsyncReplicationExecutor implements ResourceContainer {

  /**
   * Definition the constants to ReplicationTestService.
   */
  public static final class Constants {

    /**
     * The start timeout.
     */
    public static final int    START_TIMEOUT = 2000;

    /**
     * The base path to this service.
     */
    public static final String BASE_URL      = "/rest/async-replication-executor";

    /**
     * Ok result.
     */
    public static final String OK_RESULT     = "ok";

    /**
     * Fail result.
     */
    public static final String FAIL_RESULT   = "fail";

    /**
     * Definition the operation types.
     */
    public final class OperationType {
      /**
       * Start synchronization.
       */
      public static final String START_SYNCHRONIZATION               = "start";

      /**
       * Start synchronization on repository.
       */
      public static final String START_SYNCHRONIZATION_ON_REPOSITORY = "startSynchronizationOnRepository";

      /**
       * Start synchronization on workspace.
       */
      public static final String START_SYNCHRONIZATION_ON_WORKSPACE  = "startSynchronizationOnWorkspace";
    }
  }

  /**
   * The apache logger.
   */
  private static Log              log = ExoLogger.getLogger(ReplicationTestService.class);

  /**
   * The repository service.
   */
  private final RepositoryService repositoryService;

  /**
   * The AsyncReplication service.
   */
  private final AsyncReplication  asyncReplication;

  /**
   * The list of members.
   */
  private final List<Member>      members;

  public static class ExecutorConf {

    /**
     * List members of cluster.
     */
    private List<Member> members = new ArrayList<Member>();

    public List<Member> getMembers() {
      return members;
    }

    public void setMembers(List<Member> members) {
      this.members = members;
    }

  }

  /**
   * AsyncReplicationExecutorService constructor.
   * 
   * @param repoService
   *          the RepositoryService
   * @param replicationService
   *          the ReplicationService
   * @param backupManager
   *          the BackupManager
   * @param params
   *          the configuration parameters
   */
  public AsyncReplicationExecutor(AsyncReplication asyncReplication,
                                  RepositoryService repoService,
                                  InitParams params) {
    this.repositoryService = repoService;
    this.asyncReplication = asyncReplication;

    // initialize members

    ObjectParameter parameter = params.getObjectParam("async-replication-executor-configuration");

    if (parameter == null)
      throw new RuntimeException("async-replication-executor-configuration not specified");

    ExecutorConf conf = (ExecutorConf) parameter.getObject();

    if (conf == null)
      throw new RuntimeException("ExecutorConf not specified");

    if (conf.getMembers().size() == 0)
      throw new RuntimeException("members not specified");

    members = conf.getMembers();

    log.info("AsyncReplicationExecutorService inited.");
  }

  /**
   * Execute synchronization.
   * 
   */
  public boolean synchronize() throws AsyncReplicationExecutorException {

    // local start.
    try {
      if (asyncReplication.synchronize()) {

        // remote start
        for (Member member : members) {
          String sUrl = member.getUrl() + Constants.BASE_URL + "/"
              + Constants.OperationType.START_SYNCHRONIZATION;

          try {
            Thread.sleep(Constants.START_TIMEOUT);
            remoteStart(member, sUrl);
          } catch (ModuleException e) {
            throw new AsyncReplicationExecutorException("Can't execute remote synchronization. Member : "
                                                            + member,
                                                        e);
          } catch (ParseException e) {
            throw new AsyncReplicationExecutorException("Can't execute remote synchronization. Member : "
                                                            + member,
                                                        e);
          } catch (InterruptedException e) {
            throw new AsyncReplicationExecutorException("Can't execute remote synchronization. Member : "
                                                            + member,
                                                        e);
          }

        }

        return true;
      } else
        return false;
    } catch (IOException e) {
      throw new AsyncReplicationExecutorException("Can't execute local synchronization.", e);
    } catch (RepositoryException e) {
      throw new AsyncReplicationExecutorException("Can't execute local synchronization.", e);
    } catch (RepositoryConfigurationException e) {
      throw new AsyncReplicationExecutorException("Can't execute local synchronization.", e);
    }
  }

  /**
   * remoteStart.
   * 
   * @param member
   *          Mrmber
   * @param sUrl
   *          String, url
   * @throws IOException
   *           Will be generate IOException.
   * @throws ModuleException
   *           Will be generate ModuleException.
   * @throws ParseException
   *           Will be generate ModuleException.
   * @throws RuntimeException
   *           Will be generate ModuleException.
   */
  private void remoteStart(Member member, String sUrl) throws IOException,
                                                      ModuleException,
                                                      ParseException,
                                                      AsyncReplicationExecutorException {
    URL url = new URL(sUrl);

    String userInfo = url.getUserInfo();

    if (userInfo == null || userInfo.split(":").length != 2)
      throw new AsyncReplicationExecutorException("Fail remote start synchronization : the user name or password not not specified : "
          + member);

    String userName = userInfo.split(":")[0];
    String password = userInfo.split(":")[1];

    HTTPConnection connection = new HTTPConnection(url);
    connection.removeModule(CookieModule.class);

    connection.addBasicAuthorization(member.getRealmName(), userName, password);

    HTTPResponse resp = connection.Get(url.getFile());

    if (resp.getStatusCode() != 200 || !Constants.OK_RESULT.equals(resp.getText()))
      throw new AsyncReplicationExecutorException("Fail remote start synchronization : " + member
          + "\n" + resp.getText());
  }

  /**
   * startSynchronization.
   * 
   * @return Response return the response
   */
  @GET
  @Path("/start")
  public Response startSynchronization() {
    String result = Constants.OK_RESULT;

    try {
      asyncReplication.synchronize();
    } catch (Exception e) {
      result = Constants.FAIL_RESULT;
      log.error("Can't start synchronization", e);
    }

    return Response.ok(result).build();
  }
}
