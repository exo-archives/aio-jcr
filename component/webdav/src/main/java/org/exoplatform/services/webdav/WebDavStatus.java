/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.util.Hashtable;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class WebDavStatus {
  
  public static final int CONTINUE                        = 100;
  public static final int SWITCHING_PROTOCOLS             = 101;
  
  public static final int OK                              = 200;
  public static final int CREATED                         = 201;
  public static final int ACCEPTED                        = 202;
  public static final int NON_AUTHORITATIVE_INFORMATION   = 203;
  public static final int NO_CONTENT                      = 204;
  public static final int RESET_CONTENT                   = 205;
  public static final int PARTIAL_CONTENT                 = 206;
  public static final int MULTISTATUS                     = 207;
  
  public static final int MULTIPLE_CHOICES                = 300;
  public static final int MOVED_PERMANENTLY               = 301;
  public static final int FOUND                           = 302;
  public static final int SEE_OTHER                       = 303;
  public static final int NOT_MODIFIED                    = 304;
  public static final int USE_PROXY                       = 305;
  public static final int TEMPORARY_REDIRECT              = 307;
  
  public static final int BAD_REQUEST                     = 400;
  public static final int UNAUTHORIZED                    = 401;
  public static final int PAYMENT_REQUIRED                = 402;  
  public static final int FORBIDDEN                       = 403;  
  public static final int NOT_FOUND                       = 404;
  public static final int METHOD_NOT_ALLOWED              = 405;  
  public static final int NOT_ACCEPTABLE                  = 406;  
  public static final int PROXY_AUTHENTICATION_REQUIRED   = 407;
  public static final int REQUEST_TIMEOUT                 = 408;
  public static final int CONFLICT                        = 409;
  public static final int GONE                            = 410;
  public static final int LENGTH_REQUIRED                 = 411;
  public static final int PRECONDITION_FAILED             = 412;
  public static final int REQUEST_ENTITY_TOO_LARGE        = 413;
  public static final int REQUEST_URI_TOO_LONG            = 414;
  public static final int UNSUPPORTED_MEDIA_TYPE          = 415;
  public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
  public static final int EXPECTATION_FAILED              = 417;
  
  public static final int INTERNAL_SERVER_ERROR           = 500;
  public static final int NOT_IMPLEMENTED                 = 501;
  public static final int BAD_GATEWAY                     = 502;
  public static final int SERVICE_UNAVAILABLE             = 503;
  public static final int GATEWAY_TIMEOUT                 = 504;
  public static final int HTTP_VERSION_NOT_SUPPORTED      = 505;
  
  private static Hashtable<Integer, String> statusDescriptions = new Hashtable<Integer, String>();
  
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

  public static final String getStatusDescription(int status) {
    String description = "";
    
    Integer statusKey = new Integer(status);
    if (statusDescriptions.containsKey(statusKey)) {
       description = statusDescriptions.get(statusKey);
    }  
    
    return String.format("%s %d %s", DavConst.HTTPVER, status, description);
  }
  
}
