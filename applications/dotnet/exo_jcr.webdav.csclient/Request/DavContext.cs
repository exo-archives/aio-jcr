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
            String serverPort = (port == 80) ? "" : ":" + port.ToString();
            return "http://" + host + serverPort + servletPath;
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
