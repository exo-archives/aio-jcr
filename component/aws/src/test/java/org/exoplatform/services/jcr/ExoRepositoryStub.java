/*
 * Copyright 2004-2008 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr;

import java.util.Properties;

import javax.jcr.Repository;

import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Implements the <code>RepositoryStub</code> for the JCR Reference Implementation.
 */
public class ExoRepositoryStub extends RepositoryStub {

  /**
   * Property for the repository name (used for jndi lookup)
   */

  public static final String   PROP_REPOSITORY_NAME   = "org.apache.jackrabbit.repository.name";

  /**
   * Property for the repository configuration file (used for repository instantiation)
   */
  public static final String   PROP_REPOSITORY_CONFIG = "org.apache.jackrabbit.repository.config";

  /**
   * Property for the repository home directory (used for repository instantiation)
   */
  public static final String   PROP_REPOSITORY_HOME   = "org.apache.jackrabbit.repository.home";

  /**
   * Property for the jaas config path. If the system property
   * <code>java.security.auth.login.config</code> is not set this repository stub will try to read
   * this property from the environment and use the value retrieved as the value for the system
   * property.
   */
  public static final String   PROP_JAAS_CONFIG       = "org.apache.jackrabbit.repository.jaas.config";

  /**
   * The name of the jaas config system property.
   */
  private static final String  SYS_JAAS_CONFIG        = "java.security.auth.login.config";

  /**
   * The repository instance
   */
  private ManageableRepository repository;

  /**
   * Constructor as required by the JCR TCK.
   * 
   * @param env
   *          environment properties.
   */
  public ExoRepositoryStub(Properties env) {
    super(env);
    // set some attributes on the sessions
    superuser.setAttribute("exo", "exo");
    readwrite.setAttribute("exo", "exo");
    readonly.setAttribute("exo", "exo");
  }

  public synchronized Repository getRepository() throws RepositoryStubException {

    // System.out.println("GET REP >>>>>>>>>>>>>>>>>> >>> "+repository);
    if (repository == null) {
      try {

        String containerConf = ExoRepositoryStub.class.getResource(System.getProperty("jcr.test.configuration.file"))
                                                      .toString();
        String loginConf = ExoRepositoryStub.class.getResource("/login.conf").toString();

        StandaloneContainer.addConfigurationURL(containerConf);
        StandaloneContainer servicesManager = StandaloneContainer.getInstance();

        if (System.getProperty("java.security.auth.login.config") == null)
          System.setProperty("java.security.auth.login.config", loginConf);

        RepositoryService repositoryService = (RepositoryService) servicesManager.getComponentInstanceOfType(RepositoryService.class);

        repository = repositoryService.getRepository("db1");
        /*
         * try { repository.getNamespaceRegistry().getURI("test"); } catch (NamespaceException e) {
         * repository.getNamespaceRegistry().registerNamespace("test",
         * "http://www.apache.org/jackrabbit/test"); } ExtendedNodeTypeManager ntManager =
         * repository.getNodeTypeManager(); InputStream inXml = ExoRepositoryStub.class
         * .getResourceAsStream("/test/nodetypes-tck.xml"); ntManager.registerAllNodeTypes(inXml,
         * ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
         */

      } catch (Exception ex) {
        ex.printStackTrace();
        throw new RepositoryStubException(ex.getMessage());
      }
    }

    return repository;
  }
}
