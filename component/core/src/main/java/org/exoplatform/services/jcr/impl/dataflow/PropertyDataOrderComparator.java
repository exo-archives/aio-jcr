/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.Comparator;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class PropertyDataOrderComparator implements Comparator<PropertyData> {

  public int compare(PropertyData p1, PropertyData p2) {
    int r = 0;
    try {
      InternalQName qname1 = p1.getQPath().getName();
      InternalQName qname2 = p2.getQPath().getName();
      if (qname1.equals(Constants.JCR_PRIMARYTYPE)) {
        r = Integer.MIN_VALUE;
      } else if (qname2.equals(Constants.JCR_PRIMARYTYPE)) {
        r = Integer.MAX_VALUE;
      } else if (qname1.equals(Constants.JCR_MIXINTYPES)) {
        r = Integer.MIN_VALUE + 1;
      } else if (qname2.equals(Constants.JCR_MIXINTYPES)) {
        r = Integer.MAX_VALUE - 1;
      } else if (qname1.equals(Constants.JCR_UUID)) {
        r = Integer.MIN_VALUE + 2;
      } else if (qname2.equals(Constants.JCR_UUID)) {
        r = Integer.MAX_VALUE - 2;
      } else {
        r = qname1.getAsString().compareTo(qname2.getAsString());
      }
    } catch (Exception e) {
      System.err.println("PropertiesOrderComparator error: " + e);
    }
    return r;
  }

}
