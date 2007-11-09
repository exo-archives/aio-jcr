/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.exporting;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.NamespaceContext;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class JcrNamespaceContext implements NamespaceContext {
  private final Session session;
  public JcrNamespaceContext(Session session) {
    super();
    this.session = session;
  }

  public String getNamespaceURI(String prefix) {
    
    try {
      return session.getNamespaceURI(prefix);
    } catch (NamespaceException e) {
    } catch (RepositoryException e) {
    }
    return null;
  }

  public String getPrefix(String namespaceURI) {
    try {
      return session.getNamespacePrefix(namespaceURI);
    } catch (NamespaceException e) {
    } catch (RepositoryException e) {
    }
    return null;
  }

  public Iterator getPrefixes(String namespaceURI) {
    return Arrays.asList(getPrefix(namespaceURI)).iterator();
  }

}
