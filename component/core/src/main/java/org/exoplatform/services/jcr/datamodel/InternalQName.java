/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.datamodel;

import org.exoplatform.commons.utils.QName;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: InternalQName.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class InternalQName extends QName {

  public InternalQName(String namespace, String name) {
    super(namespace, name);
  }

  /**
   * Parse qname in form of eXo-JCR names conversion string, e.g.
   * [name_space]item_name, [http://www.jcp.org/jcr/nt/1.0]:base
   * 
   * @param qName
   * @return
   * @throws IllegalNameException
   */
  public static InternalQName parse(String qName) throws IllegalNameException {

    if (!qName.startsWith("["))
      throw new IllegalNameException("Invalid Internal QName '" + qName
          + "' Should start of '['");
    int uriStart = 0;
    int uriFinish = qName.indexOf("]", uriStart);
    if (uriFinish == -1)
      throw new IllegalNameException("Invalid Internal QName '" + qName
          + "' No closed ']'");
    String uri = qName.substring(uriStart + 1, uriFinish);
    String localName = qName.substring(uriFinish + 1, qName.length());
    return new InternalQName(uri, localName);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    
    if (o == null)
      return false;    
    
    if (!(o instanceof InternalQName))
      return false;
    
    return hashCode == o.hashCode();
  }  

}
