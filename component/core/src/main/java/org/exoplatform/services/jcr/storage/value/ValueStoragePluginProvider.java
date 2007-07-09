/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.storage.value;

import java.io.IOException;
import java.util.Iterator;

import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataNotFoundException;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

/**
 * Created by The eXo Platform SARL 04.09.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ValueStoragePluginProvider.java 12843 2007-02-16 09:11:18Z
 *          peterit $
 */
public interface ValueStoragePluginProvider {

  /**
   * @param property
   * @return ValueIOChannel appropriate for this property (by path, id etc) or
   *         null if no such channel found
   * @throws IOException
   */
  ValueIOChannel getApplicableChannel(PropertyData property, int valueOrderNumer) throws IOException;

  /**
   * @param vdDesc
   * @return ValueIOChannela ppropriate for this value data descriptor
   * @throws IOException
   * @throws ValueDataNotFoundException
   */
  ValueIOChannel getChannel(String valueDataDescriptor, PropertyData prop, int valueOrderNumer) throws IOException,
      ValueDataNotFoundException;

  /**
   * @return an iterator through all registered plugins
   */
  Iterator<ValueStoragePlugin> plugins();

  /**
   * Run consistency check operation on each plugin registered.
   * 
   * @param dataConnection
   */
  void checkConsistency(WorkspaceStorageConnection dataConnection);

}
