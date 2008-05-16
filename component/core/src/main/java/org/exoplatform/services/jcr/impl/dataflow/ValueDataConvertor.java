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
package org.exoplatform.services.jcr.impl.dataflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;

/**
 * Created by The eXo Platform SAS. <br/>
 *
 * Helper to make ValueData conversion in one place.
 * 
 * Convert bytes to types <ul> 
 * <li>String</li>
 * <li>Long</li>
 * <li>Double</li>
 * <li>Calendar</li>
 * <li>Boolean</li>
 * </ul>
 * 
 * To make conversion to Name or Path use ValueFactory which covers conversion using LocationFactory. 
 * 
 * Candidate to ValueDataFactory.
 * 
 * Date: 13.05.2008 <br/>
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: ValueDataConvertor.java 14164 2008-05-13 10:45:27Z pnedonosko $
 */
public class ValueDataConvertor {

  public static String readString(ValueData value) throws UnsupportedEncodingException, IllegalStateException, IOException {
    return new String(value.getAsByteArray(), Constants.DEFAULT_ENCODING);
  }
  
  public static Calendar readDate(ValueData value) throws UnsupportedEncodingException, IllegalStateException, IOException, ValueFormatException {
    return new JCRDateFormat().deserialize(new String(value.getAsByteArray(), Constants.DEFAULT_ENCODING));
  }
  
  public static long readLong(ValueData value) throws NumberFormatException, UnsupportedEncodingException, IllegalStateException, IOException {
    return Long.valueOf(new String(value.getAsByteArray(), Constants.DEFAULT_ENCODING)).longValue();
  }
  
  public static double readDouble(ValueData value) throws NumberFormatException, UnsupportedEncodingException, IllegalStateException, IOException {
    return Double.valueOf(new String(value.getAsByteArray(), Constants.DEFAULT_ENCODING)).doubleValue();
  }
  
  public static boolean readBoolean(ValueData value) throws UnsupportedEncodingException, IllegalStateException, IOException {
    return Boolean.valueOf(new String(value.getAsByteArray(), Constants.DEFAULT_ENCODING)).booleanValue();
  }
}
