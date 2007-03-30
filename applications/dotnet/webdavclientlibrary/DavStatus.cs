/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient
{

    public class DavStatus {

        public static int CONTINUE                        = 100;
        public static int SWITCHING_PROTOCOLS             = 101;
        public static int OK                              = 200;
        public static int CREATED                         = 201;
        public static int ACCEPTED                        = 202;
        public static int NON_AUTHORITATIVE_INFORMATION   = 203;
        public static int NO_CONTENT                      = 204;
        public static int RESET_CONTENT                   = 205;
        public static int PARTIAL_CONTENT                 = 206;
        public static int MULTISTATUS                     = 207;  
        public static int MULTIPLE_CHOICES                = 300;
        public static int MOVED_PERMANENTLY               = 301;
        public static int FOUND                           = 302;
        public static int SEE_OTHER                       = 303;
        public static int NOT_MODIFIED                    = 304;
        public static int USE_PROXY                       = 305;
        public static int TEMPORARY_REDIRECT              = 307;  
        public static int BAD_REQUEST                     = 400;
        public static int UNAUTHORIZED                    = 401;
        public static int PAYMENT_REQUIRED                = 402;  
        public static int FORBIDDEN                       = 403;  
        public static int NOT_FOUND                       = 404;
        public static int METHOD_NOT_ALLOWED              = 405;  
        public static int NOT_ACCEPTABLE                  = 406;  
        public static int PROXY_AUTHENTICATION_REQUIRED   = 407;
        public static int REQUEST_TIMEOUT                 = 408;
        public static int CONFLICT                        = 409;
        public static int GONE                            = 410;
        public static int LENGTH_REQUIRED                 = 411;
        public static int PRECONDITION_FAILED             = 412;
        public static int REQUEST_ENTITY_TOO_LARGE        = 413;
        public static int REQUEST_URI_TOO_LONG            = 414;
        public static int UNSUPPORTED_MEDIA_TYPE          = 415;
        public static int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        public static int EXPECTATION_FAILED              = 417;  
        public static int INTERNAL_SERVER_ERROR           = 500;
        public static int NOT_IMPLEMENTED                 = 501;
        public static int BAD_GATEWAY                     = 502;
        public static int SERVICE_UNAVAILABLE             = 503;
        public static int GATEWAY_TIMEOUT                 = 504;
        public static int HTTP_VERSION_NOT_SUPPORTED      = 505; 

    }

}
