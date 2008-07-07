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
using System.Windows.Forms;
using System.IO;
using System.Threading;
using System.Text.RegularExpressions;

using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.DavProperties;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.msofficeplugin.common
{
    public class Utils
    {

        public const String CAPTION = "eXo-Platform MSO Plugin";

        public static Boolean doGetFile(ApplicationInterface appInterface, String href)
        {
            String contexthref = appInterface.getContext().getContextHref();
            href = href.Substring(contexthref.Length);

            String s_p = appInterface.getCacheFolder();;

            int index1 = href.IndexOf(appInterface.getWorkspaceName());

            int index2 = href.LastIndexOf("/");
            String folder = s_p + href.Substring(index1, index2 - index1);
            folder = folder.Replace("/", "\\");

            if (!Directory.Exists(folder))
            {
                try
                {
                    DirectoryInfo dirinfo = Directory.CreateDirectory(folder);
                }
                catch (Exception ee)
                {
                    MessageBox.Show("Can't create temporary directory! Please check file system permissions!");
                    return false;
                }
            }                 

            String f_name = href.Substring(href.LastIndexOf("/") + 1);
            String FILE_NAME = folder + "\\" + f_name;

            FILE_NAME = FILE_NAME.Replace("?", "%3F");

            DavContext context = appInterface.getContext();
            try
            {
                GetCommand get = new GetCommand(context);

                get.setResourcePath(href);

                int status = get.execute();
                if (status == DavStatus.OK)
                {
                    byte[] resp = get.getResponseBody();

                    if (File.Exists(FILE_NAME))
                    {
                        File.Delete(FILE_NAME);
                    }
                    Thread.Sleep(200);

                    FileStream fs = new FileStream(FILE_NAME, FileMode.Create, FileAccess.ReadWrite, FileShare.ReadWrite);
                    BinaryWriter w = new BinaryWriter(fs);
                    for (long i = 0; i < resp.Length; i++)
                    {
                        w.Write(resp[i]);
                    }
                    w.Close();
                    fs.Close();
                }
                appInterface.setFileNameForOpen(FILE_NAME);
            }
            catch (IOException rr)
            {
                MessageBox.Show("The file seemed to be already opened", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Stop);
                return false;
            }
            catch (Exception ed)
            {
                MessageBox.Show("Can't create temporary directory! Please check file system permissions!");
                return false;
            }
            return true;
        }

        public static String getValidServletPath(String servlet, String repository, String workspace)
        {
            String servletPath = "/" + servlet + "/" + repository + "/" + workspace;
            while (true)
            {
                String normalized = servletPath.Replace("\\", "/");
                if (normalized.Equals(servletPath))
                {
                    break;
                }
                servletPath = normalized;
            }

            while (true)
            {
                String normalized = servletPath.Replace("//", "/");
                if (normalized.Equals(servletPath))
                {
                    break;
                }
                servletPath = normalized;
            }

            if (servletPath.EndsWith("/"))
            {
                servletPath = servletPath.Substring(0, servletPath.Length - 1);
            }

            return servletPath;
        }

        public static String getValidName(String name)
        {

            while (true) {
                String normalized = name.Replace("\\", "/");
                if (normalized.Equals(name)) {
                    break;
                }
                name = normalized;
            }

            while (name.StartsWith("/")) {
                name = name.Substring(1);
            }

            while (name.EndsWith("/")) {
                name = name.Substring(0, name.Length - 1);
            }

            return name;
        }

        private static String regexpPathName = "^[A-Za-z0-9. %]{1,}$";

        private static String regexpSimpleName = "^[-A-Za-z0-9.]{1,}$";

        private static String regexpNumber = "^[0-9]{1,}$";

        private static String regexpValidNodeName = "^[-\\d\\w ]{1,}$|^[-\\d\\w]{1,}:\\S[-\\d\\w ]{1,}\\S$";


        public static Boolean checkPathValid(String path)
        {
            while (true)
            {
                String[] pathes = path.Split('/');

                if (!"".Equals(pathes[0]))
                {
                    return false;
                }

                path = path.Substring(1);
                pathes = path.Split('/');

                if (!Regex.IsMatch(pathes[0], regexpPathName))
                {
                    return false;
                }

                if (path.IndexOf("/") < 0)
                {
                    return true;
                }

                path = path.Substring(path.IndexOf("/"));
            }
        }


        public static Boolean checkNameValid(String name)
        {
            return Regex.IsMatch(name, regexpPathName);
        }

        public static Boolean checkSimpleNameValid(String simpleName)
        {
            return Regex.IsMatch(simpleName, regexpSimpleName);
        }

        public static Boolean checkPortValid(String portValue)
        {
            return Regex.IsMatch(portValue, regexpNumber);
        }

        public static Boolean checkNodeNameValid(String nodeName)
		{
            return Regex.IsMatch(nodeName, regexpValidNodeName);
        }

        public static void showMessageStatus(int status)
        {
            if (status == -1)
            {
                MessageBox.Show("Error! Can't connect to the server!", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else if (status == DavStatus.NOT_FOUND)
            {
                MessageBox.Show("Error! Resource not found!", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else if (status == DavStatus.UNAUTHORIZED)
            {
                MessageBox.Show("Error! Not authorized!.", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else if (status == DavStatus.BAD_REQUEST) {
                MessageBox.Show("Error! Bad request!.", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else
            {
                MessageBox.Show("Internal server error! Status: " + status.ToString(), Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
