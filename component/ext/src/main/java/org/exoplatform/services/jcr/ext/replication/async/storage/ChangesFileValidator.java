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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesFileValidator.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesFileValidator {

  /**
   * Check is ChangesFile's content checksum equals its checksum property.
   * @param file ChangesFile
   * @return <code>true</code> if equals, <code>false</code> in other case.
   * @throws NoSuchAlgorithmException if the <code>MD5</code> algorithm is not available in the caller's environment.
   * @throws IOException if exception occurs on file content read.
   */
  public static boolean validate(ChangesFile file) throws NoSuchAlgorithmException, IOException {

    MessageDigest digest = MessageDigest.getInstance("MD5");
    DigestInputStream in = new DigestInputStream(file.getInputStream(), digest);

    byte[] buf = new byte[1024];
    while (in.read(buf) != -1)
      ;
    return java.util.Arrays.equals(file.getChecksum(), digest.digest());
  }

}
