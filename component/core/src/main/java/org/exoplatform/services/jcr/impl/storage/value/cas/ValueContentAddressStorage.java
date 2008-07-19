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

package org.exoplatform.services.jcr.impl.storage.value.cas;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;

/**
 * Created by The eXo Platform SAS        .
 * 
 * Some concept description from Wikipedia:
 * CAS system records a content address, which is an identifier uniquely and permanently 
 * linked to the information content itself. A request to retrieve information from a 
 * CAS system must provide the content identifier, from which the system can determine the 
 * physical location of the data and retrieve it. Because the identifiers are based on content, 
 * any change to a data element will necessarily change its content address. 
 * In nearly all cases, a CAS device will not permit editing information once it has been 
 * stored. Whether it can be deleted is often controlled by a policy.
 * 
 * Storage for content addresses.
 * Physically content address is presented as ValueData content checksum, assuming that here 
 * are no collisions in checksum calculation (i.e. an address is unique for given content) 
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface ValueContentAddressStorage {
  
  /**
   * deletes the address for given property value
   * @param propertyId
   * @param orderNum
   * @throws RecordNotFoundException
   */
  void delete(String propertyId) throws RecordNotFoundException, VCASException;
    

  /**
   * @param propertyId
   * @param orderNum
   * @param identifier
   * @throws RecordAlreadyExistsException if such propertyId/orderNumber already exists in storage
   */
  void add(String propertyId, int orderNum, String identifier) throws RecordAlreadyExistsException, VCASException;

  /**
   * 
   * @param propertyId
   * @param orderNum
   * @return identifier
   * @throws RecordNotFoundException
   */
  String getIdentifier(String propertyId, int orderNum) throws RecordNotFoundException, VCASException;
  
  
  /**
   * @param propertyId
   * @return identifier
   */
  List <String> getIdentifiers(String propertyId) throws RecordNotFoundException, VCASException;
  
  /**
   * initializes VCAS
   * @param props
   * @throws IOException
   */
  void init(Properties props) throws RepositoryConfigurationException, VCASException; 
}

