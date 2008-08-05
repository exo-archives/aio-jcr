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

namespace exo_jcr.msofficeplugin.common
{
    public partial class NOpen : Form, DialogInterface
    {
        private ApplicationInterface application;

        public String versionHref = null;

        public Boolean isNeedCompare = false;

        public NOpen(ApplicationInterface application)
        {
            this.application = application;
            InitializeComponent();
            mainControl1.initApps(application, this);
        }

        private void btn_Cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_open_Click(object sender, EventArgs e)
        {
            mainControl1.openClick();
        }

        public void enableVersions(Boolean enableVersions)
        {
            btn_versions.Enabled = enableVersions;
        }

        private void btn_versions_Click(object sender, EventArgs e)
        {
            versionHref = null;
            isNeedCompare = false;
            Versions dialog_versions = new Versions(application, this, mainControl1.selectedHref);
            dialog_versions.ShowDialog(mainControl1.selectedHref);
            if (versionHref != null) {
                Utils.doGetFile(application, versionHref);
                
                application.needsCompare(isNeedCompare);
                
                Close();
            }
        }

    }
}
