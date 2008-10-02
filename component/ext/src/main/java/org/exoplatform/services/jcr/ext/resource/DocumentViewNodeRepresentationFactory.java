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

package org.exoplatform.services.jcr.ext.resource;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class DocumentViewNodeRepresentationFactory implements NodeRepresentationFactory {

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentationFactory#createNodeRepresentation(javax.jcr.Node, java.lang.String)
   */
  public NodeRepresentation createNodeRepresentation(Node node,
      String mediaTypeHint) {
    return new DocumentViewNodeRepresentation(node);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentationFactory#getNodeType()
   */
  public String getNodeType() {
    return "nt:base";
  }

}

