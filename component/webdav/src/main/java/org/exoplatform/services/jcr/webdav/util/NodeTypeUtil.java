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

package org.exoplatform.services.jcr.webdav.util;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by The eXo Platform SARL Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class NodeTypeUtil {

  public static String getNodeType(String nodeTypeHeader) {
    if (nodeTypeHeader != null)
      return new String(Base64.decodeBase64(nodeTypeHeader.getBytes()));
    else
      return null;
  }

  public static ArrayList<String> getMixinTypes(String mixinTypeHeader) {
    ArrayList<String> mixins = new ArrayList<String>();
    if (mixinTypeHeader == null) {
      return mixins;
    }

    String mixTypes = new String(Base64.decodeBase64(mixinTypeHeader.getBytes()));

    String[] mixType = mixTypes.split(";");

    for (int i = 0; i < mixType.length; i++) {
      String curMixType = mixType[i];
      if ("".equals(curMixType)) {
        continue;
      }
      mixins.add(curMixType);
    }

    return mixins;
  }

}
