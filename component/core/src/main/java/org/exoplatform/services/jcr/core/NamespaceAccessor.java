/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.core;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL        .<br/>
 * 
 * Interface for namespaces holder objects: Session and NamespaceRegistry
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: NamespaceAccessor.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface NamespaceAccessor {
  
    /**
     * @param prefix
     * @return URI by mapped prefix
     * @throws NamespaceException
     * @throws RepositoryException
     */
    String getNamespaceURIByPrefix(String prefix) throws NamespaceException, RepositoryException;
    
    /**
     * @param uri
     * @return prefix by mapped URI
     * @throws NamespaceException
     * @throws RepositoryException
     */
    String getNamespacePrefixByURI(String uri) throws NamespaceException, RepositoryException;
    
    /**
     * @return all prefixes registered
     * @throws RepositoryException
     */
    String[] getAllNamespacePrefixes() throws RepositoryException;
}
