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
package org.exoplatform.services.jcr.impl.core;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JCRName.java 11907 2008-03-13 15:36:21Z ksm $
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
    
    this.stringName = ((this.prefix.length() == 0 ? "" : this.prefix + ":") + this.name);
    
    //this.hashCode = 31 * this.stringName.hashCode();
    
    int hk = 31 + this.namespace.hashCode();
    hk = hk * 31 + this.name.hashCode();
    this.hashCode = hk * 31 + this.prefix.hashCode();
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

