/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.util;

import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.datamodel.Identifier;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
 *         </a>
 * @version $Id: UUIDGenerator.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class UUIDGenerator {
  public static final int UUID_LENGTH = IDGeneratorService.ID_LENGTH;
  
  private static IDGeneratorService idGenerator;

  public UUIDGenerator(IDGeneratorService idGenerator) {
    UUIDGenerator.idGenerator = idGenerator;
  }

  public Identifier generateUUID(String path) {
    return new Identifier(idGenerator.generateStringID(path));
  }

  public String generateStringUUID(String path) {
    return idGenerator.generateStringID(path);
  }

  public static String generate() {
    return idGenerator.generateStringID(""+System.currentTimeMillis());
  }
}