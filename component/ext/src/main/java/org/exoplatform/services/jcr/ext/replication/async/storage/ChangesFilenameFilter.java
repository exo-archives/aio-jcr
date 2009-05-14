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

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesFileNameFilter.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesFilenameFilter implements FilenameFilter {

  private final boolean       skipInternal;

  private final static String FILENAME_REGEX               = "[0-9]+";

  private final static String FILENAME_REGEX_WITH_INTERNAL = "[0-9]"
                                                               + LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG
                                                               + "?";

  private final Pattern       PATTERN                      = Pattern.compile(FILENAME_REGEX);

  private final Pattern       PATTERN_WITH_INTERNAL        = Pattern.compile(FILENAME_REGEX_WITH_INTERNAL);

  ChangesFilenameFilter(boolean skipInternal) {
    this.skipInternal = skipInternal;
  }

  public boolean accept(File dir, String name) {
    Matcher m = skipInternal ? PATTERN.matcher(name) : PATTERN_WITH_INTERNAL.matcher(name);
    if (!m.matches())
      return false;
    File file = new File(dir, name);
    return !file.isDirectory();
  }
}
