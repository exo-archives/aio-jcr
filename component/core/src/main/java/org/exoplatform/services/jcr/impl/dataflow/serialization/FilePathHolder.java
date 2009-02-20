/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 19.02.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: FilePathHolder.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class FilePathHolder {
  
  private FileCleaner cleaner;
  
  private  ConcurrentHashMap<String, String> filePathMap = new ConcurrentHashMap<String, String>();
  
  public String getPath(String parentPropertyDataId) {
    if (filePathMap.containsKey(parentPropertyDataId)) {
      return filePathMap.get(parentPropertyDataId);
    } else 
      return null;
  }
  
  public void putPath(String parentPropertyDataId, String filePath) {
    filePathMap.put(parentPropertyDataId, filePath);
  }
  
  public void clean() {
    List<String> ls  = new ArrayList<String>(filePathMap.values());
    
    for (String fPath : ls) 
      cleaner.addFile(new File(fPath));
  } 

}
