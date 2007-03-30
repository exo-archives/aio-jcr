/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class Const {

  public class Http {
    public static final String CLIENTDESCR = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)";
    public static final String HEADER_DELIM = ": ";
    public static final String VERSION = "HTTP/1.1";    
  }  
 
  public class HttpHeaders {
    public static final String ALLOW = "Allow";
    public static final String CONNECTION = "Connection";
    public static final String CONTENTLENGTH = "Content-Length";
    public static final String DESTINATION = "Destination";
    public static final String HOST = "Host";
    public static final String USERAGENT = "User-Agent";
    public static final String TRANSLATE = "Translate";
    public static final String ACCEPTENCODING = "Accept-Encoding";
    public static final String TE = "TE";
    public static final String DEPTH = "Depth";
    public static final String CONTENTTYPE = "Content-type";
    public static final String TIMEOUT = "Timeout";
    public static final String LOCKTOKEN = "Lock-Token";
    public static final String OVERWRITE = "Overwrite";      
  }  
  
  public class DavCommand {
    public static final String CHECKIN = "CHECKIN";
    public static final String CHECKOUT = "CHECKOUT";
    public static final String COPY = "COPY";
    public static final String DELETE = "DELETE";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String LABEL = "LABEL";
    public static final String LOCK = "LOCK";
    public static final String MERGE = "MERGE";
    public static final String MKCOL = "MKCOL";
    public static final String MKWORKSPACE = "MKWORKSPACE";
    public static final String MOVE = "MOVE";
    public static final String OPTIONS = "OPTIONS";
    public static final String ORDERPATCH = "ORDERPATCH";
    public static final String PROPFIND = "PROPFIND";
    public static final String PROPPATCH = "PROPPATCH";
    public static final String PUT = "PUT";
    public static final String REPORT = "REPORT";
    public static final String UNCHECKOUT = "UNCHECKOUT";
    public static final String UNLOCK = "UNLOCK";
    public static final String UPDATE = "UPDATE";
    public static final String VERSIONCONTROL = "VERSION-CONTROL";
  }
  
  
  
}
