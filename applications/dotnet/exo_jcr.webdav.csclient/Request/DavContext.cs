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

namespace exo_jcr.webdav.csclient.Request
{
    public class DavContext {

        private String host;
        private int port;
        private String servletPath;
        private String user;
        private String pass;

        public DavContext(String host, int port, String servletPath)
        {
            this.host = host;
            this.port = port;
            this.servletPath = servletPath;
        }

        public DavContext(String host, int port, String servletPath, String user, String pass)
        {
            this.host = host;
            this.port = port;
            this.servletPath = servletPath;
            this.user = user;
            this.pass = pass;
        }

        public String getContextHref()
        {
            return "http://" + host + ":" + port.ToString() + servletPath;
        }

        public String Host 
        {
            get 
            { 
                return host;
            }
            set 
            {
                host = value; 
            }
        }

       public int Port
        {
            get
            {
                return port;
            }
            set
            {
               port = value;
            }
        }

        public String ServletPath
        {
            get
            {
                return servletPath;
            }
            set
            {
                servletPath = value;
            }
        }


        public String User
        {
            get
            {
                return user;
            }
            set
            {
                user = value;
            }
        }


        public String Pass
        {
            get
            {
                return pass;
            }
            set
            {
                pass = value;
            }
        }


    }
}
