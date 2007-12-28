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

package org.exoplatform.services.webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.services.rest.transformer.InputEntityTransformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavXmlInputTransformer extends InputEntityTransformer {
  
  @Override
  public Document readFrom(InputStream entityDataStream) throws IOException {
    try {      
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      
      byte []buffer = new byte[2048];
      while (true) {
        int readed = entityDataStream.read(buffer);
        if (readed < 0) {
          break;
        }
        outStream.write(buffer, 0, readed);
      }
      
      byte []bytes = outStream.toByteArray();
      
      if (bytes.length == 0) {
        return null;
      }
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    } catch (SAXException saxe) {
      return null;
    } catch (ParserConfigurationException pce) {
      throw new IOException("Can't read from input stream " + pce);
    }
  }

}
