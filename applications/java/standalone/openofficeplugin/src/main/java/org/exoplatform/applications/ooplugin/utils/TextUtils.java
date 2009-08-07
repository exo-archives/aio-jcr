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
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.applications.ooplugin.OOConstants.MimeTypes;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 19, 2008
 */
public class TextUtils {

  public static Document getXmlFromBytes(byte[] xmlBytes) throws Exception {

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

  public static String getMimeType(String type) {

    if (type.equalsIgnoreCase(".odt")) {
      return MimeTypes.ODT;
    } else if (type.equalsIgnoreCase(".odp")) {
      return MimeTypes.ODP;
    } else if (type.equalsIgnoreCase(".ods")) {
      return MimeTypes.ODS;
    } else if (type.equalsIgnoreCase(".doc")) {
      return MimeTypes.DOC;
    } else if (type.equalsIgnoreCase(".ppt")) {
      return MimeTypes.PPT;
    } else if (type.equalsIgnoreCase(".xls")) {
      return MimeTypes.XLS;
    } else if (type.equalsIgnoreCase(".rtf")) {
      return MimeTypes.RTF;
    } else
      return "";

  }

  public static String EncodePath(String path) throws Exception {

    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String[] pathElements = path.split("/");

    StringBuffer sb = new StringBuffer();

    for (String string : pathElements) {
      sb.append("/").append(URLEncoder.encode(string, "UTF-8"));
    }
    return sb.toString();
  }

  public static String DecodePath(String path) throws Exception {

    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String[] pathElements = path.split("/");

    StringBuffer sb = new StringBuffer();

    for (String string : pathElements) {
      sb.append("/").append(URLDecoder.decode(string, "UTF-8"));
    }
    return sb.toString();
  }

  public static String FilterFileName(String filename) {

    while (filename.startsWith(" ")) {
      filename = filename.substring(1);
    }

    filename = filename.substring(0, filename.indexOf("."));

    return filename;
  }

}
