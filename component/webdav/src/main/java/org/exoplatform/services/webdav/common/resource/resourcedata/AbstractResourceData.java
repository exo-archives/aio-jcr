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

package org.exoplatform.services.webdav.common.resource.resourcedata;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class AbstractResourceData implements ResourceData {

  protected String name;
  protected boolean iscollection;
  protected String contentType;
  protected String lastModified;  
  protected InputStream resourceInputStream;
  protected long resourceLenght;  
  
  public String getName() {
    return name;
  }
  
  public boolean isCollection() {
    return iscollection;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public String getLastModified() {
    return lastModified;
  }
  
  public InputStream getContentStream() {
    return resourceInputStream;
  }
  
  public long getContentLength() {
    return resourceLenght;
  }
  
}

