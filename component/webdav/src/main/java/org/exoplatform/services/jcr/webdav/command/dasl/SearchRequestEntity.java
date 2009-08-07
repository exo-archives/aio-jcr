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

package org.exoplatform.services.jcr.webdav.command.dasl;

import org.exoplatform.common.util.HierarchicalProperty;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class SearchRequestEntity {

  private HierarchicalProperty body;

  public SearchRequestEntity(HierarchicalProperty body) {
    this.body = body;
  }

  public String getQueryLanguage() throws UnsupportedQueryException {
    if (body.getChild(0).getName().getNamespaceURI().equals("SQL:")
        && body.getChild(0).getName().getLocalPart().equals("sql")) {
      return "sql";
    }

    throw new UnsupportedOperationException();
  }

  public String getQuery() throws UnsupportedQueryException {
    if (body.getChild(0).getName().getNamespaceURI().equals("SQL:")
        && body.getChild(0).getName().getLocalPart().equals("sql")) {
      return body.getChild(0).getValue();
    }

    throw new UnsupportedQueryException();
  }

}
