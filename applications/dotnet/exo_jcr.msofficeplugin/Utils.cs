/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Threading;

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

namespace exo_jcr.msofficeplugin
{
    public class Utils
    {

        public static void doGetFile(Connect connect, String href)
        {

            String contexthref = connect.getContext().getContextHref();
            href = href.Substring(contexthref.Length);

            String s_p = connect.getCacheFolder();;

            int index1 = href.IndexOf(connect.Workspace);
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
                    MessageBox.Show("At doGetFile():" + ee.Message + ee.StackTrace);
                }
            }

            String f_name = href.Substring(href.LastIndexOf("/") + 1);
            String FILE_NAME = folder + "\\" + f_name;

            FILE_NAME = FILE_NAME.Replace("?", "%3F");

            DavContext context = connect.getContext();
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
                connect.Filename = FILE_NAME;
            }
            catch (IOException rr)
            {
                MessageBox.Show("The file seemed to be already opened", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Stop);
            }
            catch (Exception ed)
            {
                MessageBox.Show("AT doGetFile " + ed.Message + ed.StackTrace);
                return;
            }

        }

    }
}
