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

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavReport extends MultistatusCommand {
  
  private int depth = 0;

  public DavReport(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.REPORT;
    xmlName = Const.StreamDocs.VERSION_TREE;
  }
  
  public void setDepth(int depth) {
    this.depth = depth;
  }
  
  @Override
  public int execute() throws Exception {
    client.setRequestHeader(HttpHeader.DEPTH, "" + depth);    
    return super.execute();
  }  
  
}
