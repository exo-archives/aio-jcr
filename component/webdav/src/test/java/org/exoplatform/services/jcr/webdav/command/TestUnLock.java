/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.webdav.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class TestUnLock extends BaseStandaloneTest {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger(TestUnLock.class);

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getRepositoryName() {
    return null;
  }
  
  private String path = TestUtils.getFileName();

  private String fileContent = TestUtils.getFileContent();


  @Override
  public void setUp() throws Exception {
    super.setUp();
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
  }

  
  public void testUnLock() throws Exception {
    assertTrue(session.getRootNode().hasNode(TextUtil.relativizePath(path)));
    Node lockNode = session.getRootNode().getNode(TextUtil.relativizePath(path));
    String token = TestUtils.lockNode(session, path, true);
    assertTrue(lockNode.isLocked());
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.LOCKTOKEN, token);
    ContainerResponse containerResponse = service(WebDAVMethods.UNLOCK,getPathWS() + path , "", headers, null);
    assertEquals(HTTPStatus.NO_CONTENT, containerResponse.getStatus());
    
    
  }
  
  
  
}
