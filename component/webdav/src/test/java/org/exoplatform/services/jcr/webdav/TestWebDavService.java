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

import javax.jcr.Session;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestWebDavService extends BaseStandaloneWebDavTest {

  public void testService() throws Exception {
  	assertNotNull(webdavService);
  }

  public void testJCR() throws Exception {
  	WebDavServiceImpl serv = (WebDavServiceImpl)webdavService;
  	//System.out.println("------------->"
  	assertEquals("/file1/file2", serv.path("ws/file1/file2"));
  	assertEquals("ws", serv.workspaceName("ws/file1/file2"));
  	assertEquals(0, serv.lockTokens(null, null).size());
  	
  	Session internalSession = serv.session(repository.getConfiguration().getName(),
  			 session.getWorkspace().getName(), serv.lockTokens(null, null));
  	assertNotNull(internalSession);
  	assertNotNull(internalSession.getRootNode());
  }
	
  public void testQName() throws Exception {
  	QName name = new QName("DAV:", "test");
  	assertEquals(XMLConstants.DEFAULT_NS_PREFIX, name.getPrefix());
  }
}
