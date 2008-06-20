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

package org.exoplatform.services.jcr.webdav.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * Created by The eXo Platform SAS .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class XMLOutputTransformer extends OutputEntityTransformer {

  @Override
  public void writeTo(Object obj, OutputStream out) throws IOException {
    if(!(obj instanceof HierarchicalProperty)) {
      throw new ClassCastException ("HierarchicalProperty object expected, found "
          +obj.getClass().getName());
    }
    
    HierarchicalProperty prop = (HierarchicalProperty) obj;
    
    // TODO write the property here

    
  }

}
