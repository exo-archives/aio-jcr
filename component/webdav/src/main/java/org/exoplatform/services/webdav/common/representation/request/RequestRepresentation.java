/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.request;

import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

/*
 * RequestRepresentation is the representation of XML body of HTTP request.
 * It parse the request and returns the ResponseRepresentation, which will generates the response body.  
 * 
 */

public interface RequestRepresentation {

  /*
   * Returns the name and namespace of the xml - document, which binede with RequestRepresentation
   *  
   */
  String getDocumentName();
  
  String getNamespaceURI();
  
  /*
   * Here is parsing of XML - document and obtaining all the necessary parameters.
   * 
   */
  void parse(Document document);
  
  /*
   * Returns then ResponseRepresentation, which is directly used in generating response.
   * ResponseRepresentation must receive all the necessary parameters from RequestRepresentation and 
   * from command handlers.
   * 
   */
  ResponseRepresentation getResponseRepresentation();

}
