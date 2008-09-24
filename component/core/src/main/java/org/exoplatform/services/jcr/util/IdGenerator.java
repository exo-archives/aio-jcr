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
package org.exoplatform.services.jcr.util;

import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.datamodel.Identifier;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov </a>
 * @version $Id: IdGenerator.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class IdGenerator {
  public static final int           IDENTIFIER_LENGTH = IDGeneratorService.ID_LENGTH;

  private static IDGeneratorService idGenerator;

  public IdGenerator(IDGeneratorService idGenerator) {
    IdGenerator.idGenerator = idGenerator;
  }

  public Identifier generateId(String path) {
    return new Identifier(idGenerator.generateStringID(path));
  }

  public String generateStringId(String path) {
    return idGenerator.generateStringID(path);
  }

  public static String generate() {
    return idGenerator.generateStringID("" + System.currentTimeMillis());
  }
}
