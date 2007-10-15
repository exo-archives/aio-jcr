/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

using System.Threading;
using System.Reflection;

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


namespace exo_jcr.msofficeplugin.ppoint
{
    public partial class NOpen : Form
    {

        Microsoft.Office.Interop.PowerPoint._Application app;
        Connect connect;

        public String versionHref = null;

        public NOpen(object app, Connect connect)
        {
            this.app = (Microsoft.Office.Interop.PowerPoint._Application)app;
            this.connect = connect;
            InitializeComponent();
            mainControl1.SetConnect(connect);
        }

        private void btn_Cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_open_Click(object sender, EventArgs e)
        {
            mainControl1.openClick();
        }

        private void btn_versions_Click(object sender, EventArgs e)
        {
            versionHref = null;
            Versions dialog_versions = new Versions(connect, this);
            dialog_versions.ShowDialog(mainControl1.selectedHref);
            if (versionHref != null) {
                Utils.doGetFile(connect, versionHref);
                Close();
            }
        }

        public void activateVersionButton(bool enabled) {
            btn_versions.Enabled = enabled;
        }

    }
}