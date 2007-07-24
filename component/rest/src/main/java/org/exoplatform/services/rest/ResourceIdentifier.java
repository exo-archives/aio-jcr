/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.rest;

import java.util.Map;

/**
 * Created by The eXo Platform SARL .<br/>
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ResourceIdentifier {

  private Map < String, String > parameters = null;
  private String uri;
  private String baseURI;

  /**
   * @param baseURI the base URI
   * @param relURI the relative URI
   */
  public ResourceIdentifier(String baseURI, String relURI) {
    this.uri = (relURI.endsWith("/")) ? relURI : (relURI + "/");
    this.baseURI = baseURI;
  }

  /**
   * @param relURI the relative URI Relative URI used for identification
   *          ResourceContainer with can serv the request
   */
  public ResourceIdentifier(String relURI) {
    this("", relURI);
  }

  /**
   * @return the relative URI
   */
  public String getURI() {
    return uri;
  }

  /**
   * @return the base URI
   */
  public String getBaseURI() {
    return baseURI;
  }

  /**
   * Initialize the URI parameters.
   * @param pattern the URIPattern
   * @see org.exoplatform.services.rest.URIPattern
   */
  public void initParameters(URIPattern pattern) {
    this.parameters = pattern.parse(uri);
  }

  /**
   * @return the key-value pairs of URi parameters
   * @throws IllegalStateException URI parameters not initializaed yet
   */
  public Map < String, String > getParameters() throws IllegalStateException {
    if (parameters == null) {
      throw new IllegalStateException(
          "Prarameters are not initialized. Call initParameters(pattern) first");
    }
    return parameters;
  }

}
