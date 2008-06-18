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

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 18.06.2008
 * 
 * <br/>
 * For use with JiBX binding in eXo configuration.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class ConfigurationFormat {

  private static final Log LOG = ExoLogger.getLogger("jcr.ConfigurationFormat");
  
  public static int parseInt(String text) {
    try {
      return StringNumberParser.parseInt(text);
    } catch(Throwable e) {
      LOG.warn("Unparseable int '" + text + "'. Check StringNumberParser.parseInt for details.", e);
      return 0;
    }
  }
  
  public static long parseLong(String text) {
    try {
      return StringNumberParser.parseLong(text);
    } catch(Throwable e) {
      LOG.warn("Unparseable long '" + text + "'. Check StringNumberParser.parseLong for details.", e);
      return 0l;
    }
  }
  
  public static long parseTime(String text) {
    try {
      return StringNumberParser.parseTime(text);
    } catch(Throwable e) {
      LOG.warn("Unparseable time (as long) '" + text + "'. Check StringNumberParser.parseTime for details.", e);
      return 0l;
    }
  }
  
}
