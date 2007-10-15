/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public enum XmlMapping {
  /**
   * The document view is designed to be more human-readable than the system
   * view, though it achieves this at the expense of completeness. In level 1
   * the document view is used as the format for the virtual XML stream against
   * which an XPath query is run (see 6.6 Searching Repository Content). As
   * well, in level 1, export to document view format is supported (see 6.5
   * Exporting Repository Content). In level 2, document view also allows for
   * the import of arbitrary XML (see 7.3.2 Import from Document View). The
   * document view mapping in fact consists of a family of related mappings
   * whose precise features vary according to the context in which it is used
   * (export, import or XPath query) and which optional features are supported
   * by the particular implementation in question.
   */
  DOCVIEW,
  /**
   * The system view mapping provides a complete serialization of workspace
   * content to XML without loss of information
   */
  SYSVIEW
}
