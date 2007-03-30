/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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



namespace exo_jcr.msofficeplugin.excel
{
    public partial class Settings : Form
    {

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
                if (box_Server.Text.Equals("")) box_Server.Text = "localhost";

                box_Port.Text = client_key.GetValue(RegKeys.S_PORT_KEY, "").ToString();
                if (box_Port.Text.Equals("")) box_Port.Text = "8080";

                box_Servlet.Text = client_key.GetValue(RegKeys.S_SERVLET_KEY, "").ToString();
                if (box_Servlet.Text.Equals("")) box_Servlet.Text = "/jcr-webdav/repository";


                box_Username.Text = client_key.GetValue(RegKeys.USER_KEY, "").ToString();
                if (box_Username.Text.Equals("")) box_Username.Text = "admin";


                box_workspace.Text = client_key.GetValue(RegKeys.WS_KEY, "").ToString();
                if (box_workspace.Text.Equals("")) box_workspace.Text = "production";

                String svalue = client_key.GetValue(RegKeys.PASS_KEY, "").ToString();
                byte[] bs_pass = System.Convert.FromBase64String(svalue);
                string spass = Encoding.UTF8.GetString(bs_pass);
                if (spass.Equals("")) spass = "admin";
                box_Password.Text = spass;

                if ((int)client_key.GetValue(RegKeys.P_ALLOW_KEY, 0) == 1)
                {

                    box_ProxyAddress.Enabled = true;
                    box_ProxyPort.Enabled = true;
                    ProxyEnabled.Checked = true;
                }
                box_ProxyAddress.Text = client_key.GetValue(RegKeys.P_ADDDR_KEY, "").ToString();
                box_ProxyPort.Text = client_key.GetValue(RegKeys.P_PORT_KEY, "").ToString();
            }

        

        private void btn_Cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }
       
        private void ProxyEnabled_CheckedChanged(object sender, EventArgs e)
        {

            RegistryKey soft_key = Registry.CurrentUser.OpenSubKey(RegKeys.SOFTWARE_KEY,true);
            RegistryKey exo_key = soft_key.OpenSubKey(RegKeys.EXO_KEY,true);
            if (exo_key == null)
            {
                soft_key.CreateSubKey(RegKeys.EXO_KEY);

            }
            RegistryKey client_key = exo_key.OpenSubKey(RegKeys.CLIENT_KEY);
            if (client_key == null)
            {
                exo_key.CreateSubKey(RegKeys.CLIENT_KEY);
            }
            
            if (ProxyEnabled.Checked == true)
            {
                this.box_ProxyAddress.Enabled = true;
                this.box_ProxyPort.Enabled = true;
            }
            else {
                this.box_ProxyAddress.Enabled = false;
                this.box_ProxyPort.Enabled = false;
            }

            box_ProxyAddress.Text = client_key.GetValue(RegKeys.P_ADDDR_KEY, "").ToString();
            box_ProxyPort.Text = client_key.GetValue(RegKeys.P_PORT_KEY, "").ToString();


        }

        private void btn_TestConn_Click(object sender, EventArgs e)
        {
            try
            {
                String curPath = getPath();
                int port = Convert.ToInt32(box_Port.Text);
                this.context = new exo_jcr.webdav.csclient.Request.DavContext(box_Server.Text, port, box_Servlet.Text, box_Username.Text, box_Password.Text);
                HeadCommand head = new HeadCommand(context);
                head.setResourcePath(curPath);
                int status = head.execute();

                if (status == DavStatus.OK)
                {
                    MessageBox.Show("Succesfull connection!", "Succesfull",
                        MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
                else
                {
                    MessageBox.Show("Connection fail.", "Failed",
                        MessageBoxButtons.OK, MessageBoxIcon.Error);
                }

            }
            catch (Exception tryexc)
            {
                MessageBox.Show("Connection error..", "Error",
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

        private void btn_Save_Click(object sender, EventArgs e)
        {
            try
            {

                if (box_Server.Text.Equals("") || box_Port.Text.Equals("") || box_Servlet.Text.Equals("") ||
                    box_Username.Text.Equals("") || box_Password.Text.Equals("") || box_workspace.Text.Equals(""))
                {
                    MessageBox.Show("Please, fill all required fields!", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }


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

                client_key.SetValue(RegKeys.P_ADDDR_KEY, box_ProxyAddress.Text);

                if (!box_ProxyPort.Text.Equals(""))
                client_key.SetValue(RegKeys.P_PORT_KEY, Convert.ToInt32(box_ProxyPort.Text));

                if (ProxyEnabled.Checked){
                    client_key.SetValue(RegKeys.P_ALLOW_KEY, 1);
                }else{
                    client_key.SetValue(RegKeys.P_ALLOW_KEY, 0);
                }
                MessageBox.Show("Settings Saved!", "Ok",
                   MessageBoxButtons.OK, MessageBoxIcon.Information);                
            }
            catch (Exception ee)
            {
                MessageBox.Show("Cannot save paramethers", "Error",
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