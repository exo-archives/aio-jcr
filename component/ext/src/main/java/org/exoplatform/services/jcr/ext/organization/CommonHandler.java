/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.organization;

import java.util.Date;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 7 10 2008
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ReadHandler.java 111 2008-11-11 11:11:11Z $
 */
public abstract class CommonHandler {

  /**
   * Checking if one of mandatory properties of the object is null.
   * 
   * @param obj
   *          The object to check
   * @throws Exception
   *           An exception is thrown if some of mandatory properties is null or the method cannot
   *           access the database
   */
  abstract void checkMandatoryProperties(Object obj) throws Exception;

  /**
   * This method read property data from node in the storage.
   * 
   * @param node
   *          The node to read from
   * @param prop
   *          The property name which need to read
   * @return The property data as date if exist and null if not
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  abstract Date readDateProperty(Node node, String prop) throws Exception;

  /**
   * Get object from node with all properties.
   * 
   * @param node
   *          The node to get data from
   * @return
   * @throws Exception
   *           An exception is thrown the method cannot access the database
   */
  abstract Object readObjectFromNode(Node node) throws Exception;

  /**
   * This method read property data.
   * 
   * @param node
   *          The node to read from
   * @param prop
   *          The property name which need to read
   * @return The property data as string if exist and null if not
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  abstract String readStringProperty(Node node, String prop) throws Exception;

  /**
   * This method write object's properties to the node in the storage.
   * 
   * @param obj
   *          The object which properties need to write
   * @param node
   *          The node to write properties in
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  abstract void writeObjectToNode(Object obj, Node node) throws Exception;
}
