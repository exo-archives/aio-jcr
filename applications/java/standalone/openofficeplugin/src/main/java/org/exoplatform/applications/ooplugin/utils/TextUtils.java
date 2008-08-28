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
package org.exoplatform.applications.ooplugin.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * Author : Dmytro Katayev
 *          work.visor.ck@gmail.com
 * Aug 19, 2008  
 */
public class TextUtils {
  
  public static Document getXmlFromBytes(byte[] xmlBytes) throws Exception{
    
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    InputStream inputStream = new ByteArrayInputStream(xmlBytes);
    return builderFactory.newDocumentBuilder().parse(inputStream);
    
  }
  
  public static String UnEscape(String string, char escape) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(string.length());
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (c == escape) {
        try {
          out.write(Integer.parseInt(string.substring(i + 1, i + 3), 16));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException();
        }
        i += 2;
      } else {
        out.write(c);
      }
    }

    try {
      return new String(out.toByteArray(), "utf-8");
    } catch (Exception exc) {
      throw new InternalError(exc.toString());
    }
  }

}
