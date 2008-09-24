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
package org.exoplatform.services.jcr.impl.xml.importing.dataflow;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: ImportPropertyData.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class ImportPropertyData extends TransientPropertyData implements ImportItemData {
  /**
   * Logger.
   */
  private static Log log = ExoLogger.getLogger("jcr.ImportedPropertyData");

  /**
   * Default constructor.
   */
  public ImportPropertyData() {

  }

  /**
   * Full constructor.
   * 
   * @param path
   *          - property path.
   * @param identifier
   *          - identifier
   * @param version
   *          - version
   * @param type
   *          - property type
   * @param parentIdentifier
   *          - parent node identifier
   * @param multiValued
   *          - is property multivalued
   */
  public ImportPropertyData(QPath path,
                            String identifier,
                            int version,
                            int type,
                            String parentIdentifier,
                            boolean multiValued) {
    super(path, identifier, version, type, parentIdentifier, multiValued);
  }

  /**
   * Set parent identifier.
   * 
   * @param identifer
   *          - identifier
   */
  public void setParentIdentifer(String identifer) {
    this.parentIdentifier = identifer;
  }

  /**
   * Set path of item.
   * 
   * @param path
   *          - property path.
   */
  public void setQPath(QPath path) {
    this.qpath = path;
  }

}
