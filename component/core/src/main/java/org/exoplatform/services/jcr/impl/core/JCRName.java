/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JCRName.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class JCRName {
	
	protected final String prefix;
	protected final String name;
  protected final String namespace;
  
  protected final String stringName;
  protected final int hashCode;

  // [PN] 05.02.07 use of canonical representation for the string values
  // see: http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html#intern()
	JCRName(String namespace, String name, String prefix) {
		this.name = name.intern();
		this.namespace = namespace.intern();
		this.prefix = prefix.intern();
    
    this.stringName = ((this.prefix.length() == 0 ? "" : this.prefix + ":") + this.name).intern();
    this.hashCode = 31 * this.stringName.hashCode();
	}
	
	/**
	 * @return Returns the internalName.
	 */
	public String getNamespace() {
		return namespace;
	}
  
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
  
	/**
	 * @return Returns the namespace.
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * @return Returns the internalName.
	 */
	public InternalQName getInternalName() {
		return new InternalQName(namespace, name);
	}
	
	/**
	 * Stringify this name
	 * @param showIndex if index should be included to the string
	 * @return
	 */
	public String getAsString() {
    return stringName; 
	}

	public boolean equals(Object obj) {
  	if (this == obj)
  	    return true;
    
    if (obj == null)
      return false;
    
  	if (obj instanceof JCRName) {
        return hashCode == obj.hashCode();
  	}
  	return false;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    return super.toString() + " (" + getAsString() + ")";
  }
}

