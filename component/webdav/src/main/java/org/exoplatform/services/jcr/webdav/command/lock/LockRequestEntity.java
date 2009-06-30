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

package org.exoplatform.services.jcr.webdav.command.lock;

import javax.xml.namespace.QName;

import org.exoplatform.services.log.Log;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class LockRequestEntity {

  /**
   * logger.
   */
  private Log log = ExoLogger.getLogger("jcr.LockRequestEntity");

  /**
   * Lock scope.
   */
  private QName            lockScope;

  /**
   * Lock type.
   */
  private QName            lockType;

  /**
   * Owner.
   */
  private String           owner;

  /**
   * Constructor.
   * 
   * @param input request body.
   */
  public LockRequestEntity(HierarchicalProperty input) {
    if (input == null) {
      lockScope = PropertyConstants.EXCLUSIVE;
      lockType = PropertyConstants.WRITE;
      return;
    }

    for (HierarchicalProperty prop : input.getChildren()) {
      if (prop.getName().equals(PropertyConstants.LOCKSCOPE)) {
        QName scope = prop.getChild(0).getName();
        if (!scope.equals(PropertyConstants.EXCLUSIVE)) {
          // should we throw PreconditionException here?
          log.warn("Lock is converted to exclusive scope, requested " + scope.getLocalPart());
        }
        lockScope = PropertyConstants.EXCLUSIVE;
      } else if (prop.getName().equals(PropertyConstants.LOCKTYPE)) {
        QName type = prop.getChild(0).getName();
        if (!type.equals(PropertyConstants.WRITE)) {
          // should we throw PreconditionException here?
          log.warn("Lock is converted to exclusive scope, requested " + type.getLocalPart());
        }
        lockScope = PropertyConstants.WRITE;
      } else if (prop.getName().equals(PropertyConstants.OWNER)) {
        // <D:href>value</D:href>
        if (prop.getChildren().size() > 0) {
          owner = prop.getChild(0).getValue();
        }
      }
    }
  }

  /**
   * lockScope getter.
   * 
   * @return lockScope
   */
  public QName getLockScope() {
    return lockScope;
  }

  /**
   * owner getter.
   * 
   * @return owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * lockType getter.
   * 
   * @return lockType
   */
  public QName getLockType() {
    return lockType;
  }
}
