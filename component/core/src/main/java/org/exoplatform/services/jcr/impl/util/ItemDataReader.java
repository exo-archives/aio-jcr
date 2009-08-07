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
package org.exoplatform.services.jcr.impl.util;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;

/**
 * Created by The eXo Platform SAS 15.05.2006
 * 
 * ItemData bulk reader (base class).
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public abstract class ItemDataReader {

  protected DataManager      dataManager;

  protected ValueFactoryImpl valueFactory = null;

  protected NodeData         parent;

  public ItemDataReader(NodeData parent, DataManager dataManager, ValueFactoryImpl valueFactory) {
    this.dataManager = dataManager;
    this.parent = parent;
    this.valueFactory = valueFactory;
  }

  public NodeData getParentNode() {
    return parent;
  }
}
