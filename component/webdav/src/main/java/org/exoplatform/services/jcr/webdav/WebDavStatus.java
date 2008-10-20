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

package org.exoplatform.services.jcr.webdav;

import java.util.Hashtable;

/**
 * Created by The eXo Platform SARL Author : <a
 * href="mailto:gavrik-vetal@gmail.com">Vitaly Guly</a>.
 * 
 * @version $Id: $
 */
public class WebDavStatus {

  /**
   * HTTP/1.1 Status 100 "Continue" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.1 for more information.
   */
  public static final int CONTINUE = 100;

  /**
   * HTTP/1.1 Status 101 "Switching Protocols" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.1 for more information.
   */
  public static final int SWITCHING_PROTOCOLS = 101;

  /**
   * HTTP/1.1 Status 200 "Ok" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int OK = 200;

  /**
   * HTTP/1.1 Status 201 "Created" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int CREATED = 201;

  /**
   * HTTP/1.1 Status 202 "Accepted" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int ACCEPTED = 202;

  /**
   * HTTP/1.1 Status 203 "Non-Authoritative Information" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int NON_AUTHORITATIVE_INFORMATION = 203;

  /**
   * HTTP/1.1 Status 204 "No Content" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int NO_CONTENT = 204;

  /**
   * HTTP/1.1 Status 205 "Reset Content" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int RESET_CONTENT = 205;

  /**
   * HTTP/1.1 Status 206 "Partial Content" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP
   * Extensions for Distributed Authoring - WEBDAV </a> section 10.2 for more
   * information.
   */
  public static final int PARTIAL_CONTENT = 206;

  /**
   * HTTP/1.1 Status 207 "Multistatus" code extensions. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.2 for more information.
   */
  public static final int MULTISTATUS = 207;

  /**
   * HTTP/1.1 Status 300 "Multiple Choices" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int MULTIPLE_CHOICES = 300;

  /**
   * HTTP/1.1 Status 301 "Moved Permanently" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int MOVED_PERMANENTLY = 301;

  /**
   * HTTP/1.1 Status 302 "Found" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int FOUND = 302;

  /**
   * HTTP/1.1 Status 303 "See Other" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int SEE_OTHER = 303;

  /**
   * HTTP/1.1 Status 304 "Not Modified" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int NOT_MODIFIED = 304;

  /**
   * HTTP/1.1 Status 305 "Use Proxy" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int USE_PROXY = 305;

  /**
   * HTTP/1.1 Status 307 "Temporary Redirect" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.3 for more information.
   */
  public static final int TEMPORARY_REDIRECT = 307;

  /**
   * HTTP/1.1 Status 400 "Bad Request" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int BAD_REQUEST = 400;

  /**
   * HTTP/1.1 Status 401 "Unauthorized" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int UNAUTHORIZED = 401;

  /**
   * HTTP/1.1 Status 402 "Payment Required" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int PAYMENT_REQUIRED = 402;

  /**
   * HTTP/1.1 Status 403 "Forbidden" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int FORBIDDEN = 403;

  /**
   * HTTP/1.1 Status 404 "Not Found" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int NOT_FOUND = 404;

  /**
   * HTTP/1.1 Status 405 "Method Not Allowed" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int METHOD_NOT_ALLOWED = 405;

  /**
   * HTTP/1.1 Status "" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int NOT_ACCEPTABLE = 406;

  /**
   * HTTP/1.1 Status 406 "Not Acceptable" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10. for more information.
   */
  public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

  /**
   * HTTP/1.1 Status 408 "Request Timeout" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int REQUEST_TIMEOUT = 408;

  /**
   * HTTP/1.1 Status 409 "Conflict" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int CONFLICT = 409;

  /**
   * HTTP/1.1 Status 410 "Gone" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int GONE = 410;

  /**
   * HTTP/1.1 Status 411 "Length Required" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int LENGTH_REQUIRED = 411;

  /**
   * HTTP/1.1 Status 412 "Precondition Failed" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int PRECONDITION_FAILED = 412;

  /**
   * HTTP/1.1 Status Request Entity Too Large "413" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int REQUEST_ENTITY_TOO_LARGE = 413;

  /**
   * HTTP/1.1 Status 414 "Request-URI Too Long" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int REQUEST_URI_TOO_LONG = 414;

  /**
   * HTTP/1.1 Status 415 "Unsupported Media Type" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int UNSUPPORTED_MEDIA_TYPE = 415;

  /**
   * HTTP/1.1 Status 416 "Requested Range Not Satisfiable" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

  /**
   * HTTP/1.1 Status 417 "Expectation Failed" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int EXPECTATION_FAILED = 417;

  /**
   * HTTP/1.1 Status 423 "Locked" code extensions. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.4 for more information.
   */
  public static final int LOCKED = 423;

  /**
   * HTTP/1.1 Status 500 "Internal Server Error" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int INTERNAL_SERVER_ERROR = 500;

  /**
   * HTTP/1.1 Status 501 "Not Implemented" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int NOT_IMPLEMENTED = 501;

  /**
   * HTTP/1.1 Status 502 "Bad Gateway" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int BAD_GATEWAY = 502;

  /**
   * HTTP/1.1 Status 503 "Service Unavailable" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int SERVICE_UNAVAILABLE = 503;

  /**
   * HTTP/1.1 Status 504 "Gateway Timeout" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int GATEWAY_TIMEOUT = 504;

  /**
   * HTTP/1.1 Status 505 "HTTP Version Not Supported" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'> HTTP/1.1
   * Status Code Definitions </a> section 10.5 for more information.
   */
  public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

  /**
   * Contains HTTP/1.1 status description.
   */
  private static Hashtable<Integer, String> statusDescriptions = new Hashtable<Integer, String>();

  /**
   * Registers Status code and it's description.
   * @param status Status code
   * @param descr Description
   */
  private static void registerDescr(int status, String descr) {
    statusDescriptions.put(new Integer(status), descr);
  }

  static {
    registerDescr(CONTINUE, "Continue");
    registerDescr(SWITCHING_PROTOCOLS, "Switching Protocols");
    registerDescr(OK, "OK");
    registerDescr(CREATED, "Created");
    registerDescr(ACCEPTED, "Accepted");
    registerDescr(NON_AUTHORITATIVE_INFORMATION, "Non-Authoritative Information");
    registerDescr(NO_CONTENT, "No Content");
    registerDescr(RESET_CONTENT, "Reset Content");
    registerDescr(PARTIAL_CONTENT, "Partial Content");
    registerDescr(MULTISTATUS, "Multi Status");
    registerDescr(MULTIPLE_CHOICES, "Multiple Choices");
    registerDescr(MOVED_PERMANENTLY, "Moved Permanently");
    registerDescr(FOUND, "Found");
    registerDescr(SEE_OTHER, "See Other");
    registerDescr(NOT_MODIFIED, "Not Modified");
    registerDescr(USE_PROXY, "Use Proxy");
    registerDescr(TEMPORARY_REDIRECT, "Temporary Redirect");
    registerDescr(BAD_REQUEST, "Bad Request");
    registerDescr(UNAUTHORIZED, "Unauthorized");
    registerDescr(PAYMENT_REQUIRED, "Payment Required");
    registerDescr(FORBIDDEN, "Forbidden");
    registerDescr(NOT_FOUND, "Not Found");
    registerDescr(METHOD_NOT_ALLOWED, "Method Not Allowed");
    registerDescr(NOT_ACCEPTABLE, "Not Acceptable");
    registerDescr(PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required");
    registerDescr(REQUEST_TIMEOUT, "Request Timeout");
    registerDescr(CONFLICT, "Conflict");
    registerDescr(GONE, "Gone");
    registerDescr(LENGTH_REQUIRED, "Length Required");
    registerDescr(PRECONDITION_FAILED, "Precondition Failed");
    registerDescr(REQUEST_ENTITY_TOO_LARGE, "Request Entity Too Large");
    registerDescr(REQUEST_URI_TOO_LONG, "Request-URI Too Long");
    registerDescr(UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
    registerDescr(REQUESTED_RANGE_NOT_SATISFIABLE, "Requested Range Not Satisfiable");
    registerDescr(EXPECTATION_FAILED, "Expectation Failed");
    registerDescr(INTERNAL_SERVER_ERROR, "Internal Server Error");
    registerDescr(NOT_IMPLEMENTED, "Not Implemented");
    registerDescr(BAD_GATEWAY, "Bad Gateway");
    registerDescr(SERVICE_UNAVAILABLE, "Service Unavailable");
    registerDescr(GATEWAY_TIMEOUT, "Gateway Timeout");
    registerDescr(HTTP_VERSION_NOT_SUPPORTED, "HTTP Version Not Supported");
  }

  /**
   * Returns status description by it's code.
   * @param status Status code
   * @return Status Description
   */
  public static final String getStatusDescription(int status) {
    String description = "";

    Integer statusKey = new Integer(status);
    if (statusDescriptions.containsKey(statusKey)) {
      description = statusDescriptions.get(statusKey);
    }

    return String.format("%s %d %s", WebDavConst.HTTPVER, status, description);
  }

}
