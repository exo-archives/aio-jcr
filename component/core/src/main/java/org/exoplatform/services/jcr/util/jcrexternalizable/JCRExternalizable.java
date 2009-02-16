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
package org.exoplatform.services.jcr.util.jcrexternalizable;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRExternalizable.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public interface JCRExternalizable {

  public void readExternal(JCRObjectInput in) throws UnknownClassIdException, IOException;

  public void writeExternal(JCRObjectOutput out) throws UnknownClassIdException, IOException;
}
