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
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.DavProperties;

using System.Security.Permissions;
using Microsoft.Win32;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

[assembly: RegistryPermissionAttribute(SecurityAction.RequestMinimum,
    ViewAndModify = "HKEY_CURRENT_USER")]



namespace exo_jcr.msofficeplugin.common
{
    public partial class Settings : Form
    {

        public static String host = "localhost";
        
        public static int port = 8080;
        
        public static String servlet = "/rest/jcr";
        
        public static String repository = "repository";
        
        public static String workspace = "production";

        public static String userId = "admin";

        public static String userPass = "admin";

        public exo_jcr.webdav.csclient.Request.DavContext context;

        private ArrayList path = new ArrayList();
        
        public Settings(object app)
        {
            InitializeComponent();
        }

        private void Settings_Load(object sender, EventArgs e)
        {

            RegistryKey soft_key = Registry.CurrentUser.OpenSubKey(RegKeys.SOFTWARE_KEY,true);
            RegistryKey exo_key = soft_key.OpenSubKey(RegKeys.EXO_KEY, true);
            if (exo_key == null)
            {
                soft_key.CreateSubKey(RegKeys.EXO_KEY);
                exo_key = soft_key.OpenSubKey(RegKeys.EXO_KEY, true);
            }
            

            RegistryKey client_key = exo_key.OpenSubKey(RegKeys.CLIENT_KEY);
            if (client_key == null) {
                exo_key.CreateSubKey(RegKeys.CLIENT_KEY);
                client_key = exo_key.OpenSubKey(RegKeys.CLIENT_KEY);
            }

                box_Server.Text = client_key.GetValue(RegKeys.S_ADDDR_KEY, "").ToString();
                if (box_Server.Text.Equals("")) box_Server.Text = host;

                box_Port.Text = client_key.GetValue(RegKeys.S_PORT_KEY, "").ToString();
                if (box_Port.Text.Equals("")) box_Port.Text = port.ToString();

                box_Servlet.Text = client_key.GetValue(RegKeys.S_SERVLET_KEY, "").ToString();
                if (box_Servlet.Text.Equals("")) box_Servlet.Text = servlet;

                box_repository.Text = client_key.GetValue(RegKeys.REPO_KEY, "").ToString();
                if (box_repository.Text.Equals("")) box_repository.Text = repository;

                box_workspace.Text = client_key.GetValue(RegKeys.WS_KEY, "").ToString();
                if (box_workspace.Text.Equals("")) box_workspace.Text = workspace;

                box_Username.Text = client_key.GetValue(RegKeys.USER_KEY, "").ToString();
                if (box_Username.Text.Equals("")) box_Username.Text = userId;

                String svalue = client_key.GetValue(RegKeys.PASS_KEY, "").ToString();
                byte[] bs_pass = System.Convert.FromBase64String(svalue);
                string spass = Encoding.UTF8.GetString(bs_pass);
                if (spass.Equals("")) spass = userPass;
                box_Password.Text = spass;
            }

        

        private void btn_Cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_TestConn_Click(object sender, EventArgs e)
        {
            if (!checkValidParams())
            {
                return;
            }

            try
            {
                String curPath = getPath();
                int port = Convert.ToInt32(box_Port.Text);

                String servletPath = Utils.getValidServletPath(box_Servlet.Text, box_repository.Text, box_workspace.Text);

                this.context = new exo_jcr.webdav.csclient.Request.DavContext(box_Server.Text, port, servletPath, box_Username.Text, box_Password.Text);
                HeadCommand head = new HeadCommand(context);
                head.setResourcePath("/");
                int status = head.execute();
                if (status == DavStatus.OK)
                {
                    MessageBox.Show("Testing connection succesfully!", Utils.CAPTION,
                        MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
                else
                {
                    Utils.showMessageStatus(status);
                }
            }
            catch (Exception tryexc)
            {
                MessageBox.Show("Error! Can't connect to the server!", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

        }


        private String getPath()
        {
            String curPath = "/";
            for (int i = 0; i < path.Count; i++)
            {
                curPath += path[i];
                if (i < (path.Count - 1))
                {
                    curPath += "/";
                }
            }
            return curPath;
        }

        private Boolean checkValidParams()
        {
            if (!Utils.checkSimpleNameValid(box_Server.Text))
            {
                MessageBox.Show("Server name is invalid!", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            if (!Utils.checkPortValid(box_Port.Text))
            {
                MessageBox.Show("Port is invalid!", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            if (!Utils.checkPathValid(box_Servlet.Text))
            {
                MessageBox.Show("Servlet path is invalid!\r\nIt should confirm the following pattern '/name1/nameN'", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            if (!Utils.checkNameValid(box_repository.Text))
            {
                MessageBox.Show("Repository name is invalid!", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            if (!Utils.checkNameValid(box_workspace.Text))
            {
                MessageBox.Show("Workspace name is invalid!", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            return true;
        }

        private void btn_Save_Click(object sender, EventArgs e)
        {
            if (!checkValidParams()) {
                return;
            }

            try
            {
                RegistryKey soft_key = Registry.CurrentUser.OpenSubKey(RegKeys.SOFTWARE_KEY, true);
                RegistryKey exo_key = soft_key.OpenSubKey(RegKeys.EXO_KEY, true);
                if (exo_key == null)
                {
                    soft_key.CreateSubKey(RegKeys.EXO_KEY);

                }
                RegistryKey client_key = exo_key.OpenSubKey(RegKeys.CLIENT_KEY, true);
                if (client_key == null)
                {
                    exo_key.CreateSubKey(RegKeys.CLIENT_KEY);
                }

                byte[] bpass = getBytes(box_Password.Text);

                client_key.SetValue(RegKeys.S_ADDDR_KEY, box_Server.Text);

                if (!box_Port.Text.Equals(""))
                client_key.SetValue(RegKeys.S_PORT_KEY, Convert.ToInt32(box_Port.Text));
                

                client_key.SetValue(RegKeys.S_SERVLET_KEY, box_Servlet.Text);

                client_key.SetValue(RegKeys.USER_KEY, box_Username.Text);
                client_key.SetValue(RegKeys.PASS_KEY, System.Convert.ToBase64String(bpass));

                client_key.SetValue(RegKeys.WS_KEY, box_workspace.Text);
                client_key.SetValue(RegKeys.REPO_KEY, box_repository.Text);
            }
            catch (Exception ee)
            {
                MessageBox.Show("Cannot save paramethers", Utils.CAPTION,
                MessageBoxButtons.OK, MessageBoxIcon.Error);
                MessageBox.Show(ee.StackTrace + ee.Message);
            }
            this.Close();
        }

        private byte[] getBytes(String value)
        {
            char[] data1 = value.ToCharArray();
            byte[] data2 = new byte[data1.Length];
            for (int i = 0; i < data1.Length; i++)
            {
                data2[i] = (byte)data1[i];
            }
            return data2;
        }
        
    } 

    
}