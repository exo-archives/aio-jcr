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

import javax.jcr.Node;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: BaseStandaloneTest.java 20657 2007-10-11 07:05:27Z vetal_ok $
 */
public abstract class BaseStandaloneWebDavTest extends TestCase {

  protected static Log log = ExoLogger.getLogger("jcr.WebDavTest");

  protected SessionImpl session;

  protected RepositoryImpl repository;

  protected CredentialsImpl adminCredentials;

  protected RepositoryService repositoryService;

  protected StandaloneContainer container;
  
  protected WebDavService webdavService;
  
  protected Node readNode;
  
  protected Node writeNode;
  
  protected Node webdavNode;

  public void setUp() throws Exception {
  	
  	
  	StandaloneContainer.addConfigurationPath("src/test/resources/configuration.xml");
    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/test/resources/login.conf");

    adminCredentials = new CredentialsImpl("admin", "admin".toCharArray());

    webdavService = (WebDavService)container.getComponentInstanceOfType(WebDavService.class); 
    
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();
    
    session = (SessionImpl) repository.login(adminCredentials);
    
    SessionProviderService spService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    spService.setSessionProvider(null, new SessionProvider(adminCredentials));
    
    addNodes();

  }
  
  private void addNodes() throws Exception {
    Node root = session.getRootNode();

    if (!root.hasNode("webdav-test")) {
			webdavNode = root.addNode("webdav-test", "nt:unstructured");
			webdavNode.addNode("write", "nt:unstructured");
			readNode = webdavNode.addNode("read", "nt:unstructured");
			writeNode = webdavNode.addNode("write", "nt:unstructured");

			session.save();
		} else {    
		  webdavNode = root.getNode("webdav-test");
		  readNode = webdavNode.getNode("read");
		  writeNode = webdavNode.getNode("write");
		}

  }

  protected void tearDown() throws Exception {
//
//    if (session != null) {
//      try {
//        session.refresh(false);
//        Node rootNode = session.getRootNode();
//        if (rootNode.hasNodes()) {
//          // clean test root
//          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
//            Node node = children.nextNode();
//            if (!node.getPath().startsWith("/jcr:system")) {
//              //log.info("DELETing ------------- "+node.getPath());
//              node.remove();
//            }
//          }
//          session.save();
//        }
//      } catch (Exception e) {
//        log.error("tearDown() ERROR " + getClass().getName() + "." + getName() + " " + e, e);
//      } finally {
//        session.logout();
//      }
//    }
//    super.tearDown();

    //log.info("tearDown() END " + getClass().getName() + "." + getName());
  }

  protected final Session webdavSession() throws Exception {
  	WebDavServiceImpl serv = (WebDavServiceImpl)webdavService;
  	return serv.session(repository.getConfiguration().getName(),
 			 session.getWorkspace().getName(), serv.lockTokens(null, null));
  }
}
