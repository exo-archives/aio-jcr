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
package org.exoplatform.services.jcr.core.nodetype;

import org.exoplatform.commons.utils.QName;

/**
 * Created by The eXo Platform SAS.
 * 
 * Define base abstraction for NodeType data used in core.
 * 
 * <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class NodeTypeData {

  protected QName   name;

  protected QName   primaryItemName;

  protected QName[] declaredChildNodeDefinitions;

  protected QName[] declaredPropertyDefinitions;

  protected QName[] declaredSupertypeNames;

  protected boolean hasOrderableChildNodes;

  protected boolean mixin;

  public NodeTypeData(QName name,
                      QName primaryItemName,
                      boolean mixin,
                      boolean hasOrderableChildNodes,
                      QName[] declaredSupertypeNames,
                      QName[] declaredChildNodeDefinitions,
                      QName[] declaredPropertyDefinitions) {

    this.name = name;
    this.primaryItemName = primaryItemName;
    this.mixin = mixin;
    this.hasOrderableChildNodes = hasOrderableChildNodes;
    this.declaredSupertypeNames = declaredSupertypeNames;
    this.declaredPropertyDefinitions = declaredPropertyDefinitions;
    this.declaredChildNodeDefinitions = declaredChildNodeDefinitions;
  }

  public QName[] getDeclaredChildNodeDefinitions() {
    return declaredChildNodeDefinitions;
  }

  public QName[] getDeclaredPropertyDefinitions() {
    return declaredPropertyDefinitions;
  }

  public QName[] getDeclaredSupertypeNames() {
    return declaredSupertypeNames;
  }

  public QName getPrimaryItemName() {
    return primaryItemName;
  }

  public QName getName() {
    return name;
  }

  public boolean hasOrderableChildNodes() {
    return hasOrderableChildNodes;
  }

  public boolean isMixin() {
    return mixin;
  }

}
