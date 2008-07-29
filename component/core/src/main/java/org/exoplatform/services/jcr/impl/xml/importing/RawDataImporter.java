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
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.Map;

import javax.jcr.NamespaceRegistry;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: RawDataImporter.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */
public interface RawDataImporter {

  public abstract ContentImporter createContentImporter(NodeData parent,
                                                        int uuidBehavior,
                                                        ItemDataConsumer dataConsumer,
                                                        NodeTypeManagerImpl ntManager,
                                                        LocationFactory locationFactory,
                                                        ValueFactoryImpl valueFactory,
                                                        NamespaceRegistry namespaceRegistry,
                                                        AccessManager accessManager,
                                                        ConversationState userState,
                                                        Map<String, Object> context,
                                                        RepositoryImpl repository,
                                                        String currentWorkspaceName);

}