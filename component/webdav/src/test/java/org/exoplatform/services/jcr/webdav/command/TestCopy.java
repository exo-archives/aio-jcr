/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.webdav.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.Range;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestCopy extends BaseStandaloneTest {
  
  
  public void testeCopyForNonCollectionSingleWorkSpace () throws Exception {
    String content = TestUtils.getFileContent();
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    MimeTypeResolver resolver = new MimeTypeResolver();
    String filename = TestUtils.getFileName();
    String destFilename = "copy_"+filename;
    Response response = new PutCommand(new NullResourceLocksHolder()).put(session,
                                                                              "/" + filename,
                                                                              inputStream,
                                                                              "nt:file",
                                                                              resolver.getMimeType(destFilename),
                                                                              null,
                                                                              null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    Response copyResponse = new CopyCommand().copy(session, "/" + filename, "/" + destFilename);
    assertEquals(HTTPStatus.CREATED, copyResponse.getStatus());
    Response getResponse = new GetCommand().get(session, "/" + destFilename, null, null, new ArrayList<Range>());
    ByteArrayInputStream getContent = (ByteArrayInputStream) getResponse.getEntity();
    Reader r = new InputStreamReader(getContent);  
    StringWriter sw = new StringWriter();  
    char[] buffer = new char[1024];  
    for (int n; (n = r.read(buffer)) != -1; )  
        sw.write(buffer, 0, n);  
    String str = sw.toString(); 
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    assertEquals(content, str);
  }
  
  public void testeCopyForNonCollectionDiferentWorkSpaces() throws Exception {
    assertNotSame(session.getWorkspace().getName(), destSession.getWorkspace().getName());
    String content = TestUtils.getFileContent();
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    MimeTypeResolver resolver = new MimeTypeResolver();
    String filename = TestUtils.getFileName();
    String destFilename = "copy_" + filename;
    Response response = new PutCommand(new NullResourceLocksHolder()).put(session,
                                                                              "/" + filename,
                                                                              inputStream,
                                                                              "nt:file",
                                                                              resolver.getMimeType(destFilename),
                                                                              null,
                                                                              null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    Response copyResponse = new CopyCommand().copy(destSession,"ws", "/" + filename, "/" + destFilename);
    assertEquals(HTTPStatus.CREATED, copyResponse.getStatus());
    Response getResponse = new GetCommand().get(destSession, "/" + destFilename, null, null, new ArrayList<Range>());
    ByteArrayInputStream getContent = (ByteArrayInputStream) getResponse.getEntity();
    Reader r = new InputStreamReader(getContent);  
    StringWriter sw = new StringWriter();  
    char[] buffer = new char[1024];  
    for (int n; (n = r.read(buffer)) != -1; )  
        sw.write(buffer, 0, n);  
    String str = sw.toString(); 
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    assertEquals(content, str);
  }


    @Override
    protected String getRepositoryName() {
      return null;
    }


}
