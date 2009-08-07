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

package org.exoplatform.services.jcr.ext.resource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.StringTokenizer;

import org.exoplatform.services.jcr.datamodel.Identifier;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */
public class UnifiedNodeReference {

  /**
   * Scheme name.
   */
  public static final String      JCR_SCHEME = "jcr";

  /**
   * Repository name.
   */
  private String                  repository;

  /**
   * Workspace name.
   */
  private String                  workspace;

  /**
   * Node identifier.
   */
  private Identifier              id;

  /**
   * Node path.
   */
  private String                  path;

  /**
   * URLStreamHandler for protocol jcr://.
   */
  private static URLStreamHandler handler;

  /**
   * @param spec
   *          string for parsing as URL
   * @throws URISyntaxException
   *           when string could not be parsed as URI reference
   * @throws MalformedURLException
   *           if malformed URL occurs
   */
  public UnifiedNodeReference(final String spec) throws URISyntaxException, MalformedURLException {
    this(new URL(null, spec, getURLStreamHandler()));
  }

  /**
   * @param url
   *          URL
   * @throws URISyntaxException
   *           if URL is not formated well to convert to URI
   */
  public UnifiedNodeReference(final URL url) throws URISyntaxException {
    this(url.toURI());
  }

  /**
   * @param uri
   *          URI
   * @throws URISyntaxException
   *           if URI does not contains required parts, e.g. scheme, path, fragment, etc
   */
  public UnifiedNodeReference(final URI uri) throws URISyntaxException {

    String scheme = uri.getScheme();
    if (uri.getScheme() == null)
      scheme = JCR_SCHEME;
    if (!scheme.equals(JCR_SCHEME))
      throw new URISyntaxException(scheme, "Only 'jcr' scheme is acceptable!");

    repository = uri.getHost();

    workspace = parseWorkpace(uri);

    String fragment = uri.getFragment();
    if (fragment != null) {
      if (fragment.startsWith("/"))
        this.path = fragment;
      else
        this.id = new Identifier(uri.getFragment());
    } else
      throw new URISyntaxException(fragment, "Neither Path nor Identifier defined!");

  }

  /**
   * @param uri
   *          URI
   * @param defaultRepository
   *          use this repository if it could not be parsed from URI
   * @param defaultWorkspace
   *          use this workspace if it could not be parsed from URI
   * @throws URISyntaxException
   *           if URI does not contains required parts, e.g. scheme, path, fragment, etc
   */
  public UnifiedNodeReference(final URI uri,
                              final String defaultRepository,
                              final String defaultWorkspace) throws URISyntaxException {

    String scheme = uri.getScheme();
    if (uri.getScheme() == null)
      scheme = JCR_SCHEME;
    if (!scheme.equals(JCR_SCHEME))
      throw new URISyntaxException(scheme, "Only 'jcr' scheme is acceptable!");

    repository = uri.getHost();
    if (repository == null)
      repository = defaultRepository;

    workspace = parseWorkpace(uri);
    if (workspace == null || workspace.length() == 0)
      workspace = defaultWorkspace;

    String fragment = uri.getFragment();
    if (fragment != null) {
      if (fragment.startsWith("/"))
        this.path = fragment;
      else
        this.id = new Identifier(uri.getFragment());
    } else
      throw new URISyntaxException(fragment, "Neither Path nor Identifier defined!");

  }

  /**
   * @param repository
   *          repository name
   * @param workspace
   *          workspace name
   * @param identifier
   *          node identifier
   */
  public UnifiedNodeReference(final String repository,
                              final String workspace,
                              final Identifier identifier) {
    this.repository = repository;
    this.workspace = workspace;
    this.id = identifier;
  }

  /**
   * @param repository
   *          repository name
   * @param workspace
   *          workspace name
   * @param identifier
   *          node path
   */
  public UnifiedNodeReference(final String repository, final String workspace, final String path) {
    this.repository = repository;
    this.workspace = workspace;
    this.path = path;
  }

  /**
   * @return the repository name.
   */
  public String getRepository() {
    return repository;
  }

  /**
   * @return the workspace name.
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * @return the node identifier.
   */
  public Identifier getIdentitifier() {
    return id;
  }

  /**
   * @return true if UUID used as node identifier.
   */
  public boolean isIdentitifier() {
    return id != null;
  }

  /**
   * @return the node path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @return true if full path used as node identifier.
   */
  public boolean isPath() {
    return path != null;
  }

  /**
   * @return the URI of node.
   * @throws URISyntaxException
   */
  public URI getURI() throws URISyntaxException {
    if (id != null)
      return new URI(JCR_SCHEME, null, repository, -1, '/' + workspace, null, id.getString());
    else if (path != null)
      return new URI(JCR_SCHEME, null, repository, -1, '/' + workspace, null, path);
    throw new URISyntaxException("", "Path or Idenfifier is not defined!");
  }

  /**
   * @return the URL of node.
   * @throws MalformedURLException
   */
  public URL getURL() throws MalformedURLException {
    URI uri;
    try {
      uri = getURI();
    } catch (URISyntaxException e) {
      throw new MalformedURLException();
    }

    try {
      return new URL(uri.toString());
    } catch (MalformedURLException e) {
      // If handler can't be found by java.net.URL#getStreamHandler()
      return new URL(null, uri.toString(), getURLStreamHandler());
    }
  }

  /**
   * @return the handler for protocol <code>jcr</code>.
   * 
   * @see java.net.URLStreamHandler.
   */
  public static URLStreamHandler getURLStreamHandler() {

    if (handler != null)
      return handler;

    /*
     * use Class#forName(), instead created by 'new' to be sure handler was
     * started and set required system property. Usually this job must be done
     * by java.net.URL, but it does not work in web container. See details in
     * org.exoplatform.services.jcr.ext.resource.jcr.Handler
     */
    String packagePrefixList = System.getProperty("java.protocol.handler.pkgs");

    if (packagePrefixList == null)
      return null;

    StringTokenizer packagePrefixIter = new StringTokenizer(packagePrefixList, "|");

    while (handler == null && packagePrefixIter.hasMoreTokens()) {
      String packagePrefix = packagePrefixIter.nextToken().trim();
      try {
        String clsName = packagePrefix + "." + JCR_SCHEME + ".Handler";
        Class<?> cls = null;
        try {
          cls = Class.forName(clsName);
        } catch (ClassNotFoundException e1) {
          try {
            // try do it with context ClassLoader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            cls = cl.loadClass(clsName);
          } catch (ClassNotFoundException e2) {
            // last chance, try use system ClasLoader
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            if (cl != null) {
              cls = cl.loadClass(clsName);
            }
          }
        }
        if (cls != null) {
          handler = (URLStreamHandler) cls.newInstance();
        }
      } catch (Exception e) {
        // exceptions can get thrown here if class not be loaded y system ClassLoader
        // or if class can't be instantiated.
      }
    }
    return handler;
  }

  private static String parseWorkpace(URI uri) {
    String path = uri.getPath();
    int sl = path.indexOf('/', 1);
    if (sl <= 0)
      return path.substring(1);
    return path.substring(1, sl);
  }

}
