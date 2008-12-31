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
using System.IO;
using System.Collections;
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
using exo_jcr.webdav.csclient.Search;

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
    public partial class Search : Form
    {
        private Multistatus multistatus;

        private DavContext context;
        
        private ApplicationInterface application;
        
        public Search(ApplicationInterface application)
        {
            this.application = application;
            InitializeComponent();
        }

        private void btn_cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_search_Click(object sender, EventArgs e)
        {
            this.context = application.getContext();

            if (context == null) {
                MessageBox.Show("Cannot load paramethers,\n please run Settings first.", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                this.Close();
            }


            try
            {
                SearchCommand searchcomm = new SearchCommand(context);
                SQLQuery query = new SQLQuery();

                searchcomm.setResourcePath("/" + application.getWorkspaceName());
                String query_string = "select * from nt:base where contains(*, '" + box_search.Text + "')";
                //String query_string = box_search.Text;
                query.setQuery(query_string);
                searchcomm.setQuery(query);

                int status = searchcomm.execute();
                if (status == DavStatus.MULTISTATUS)
                {
                    this.multistatus = searchcomm.getMultistatus();
                    DrawFileList();
                } else {
                    Utils.showMessageStatus(status);
                }
            }
            catch (Exception exc)
            {
                MessageBox.Show("Error! Can't connect to the server!", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

        }

        private void Search_Load(object sender, EventArgs e)
        {
        }


        private void DrawFileList()
        {   
            this.file_list.Items.Clear();
            ArrayList resplist = multistatus.getResponses();
            if (resplist.Count == 0)
            {
                MessageBox.Show("No results found", Utils.CAPTION, MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }

            btn_open.Enabled = true;
            for (int i = 0; i < resplist.Count; i++)
            {
                int image = 2;

                exo_jcr.webdav.csclient.Response.DavResponse response = (exo_jcr.webdav.csclient.Response.DavResponse)resplist[i];

                String href = response.getHref().getHref();
                if (href == null)
                {
                    href = "";
                }
                String displayName = "";
                WebDavProperty displayNameProp = response.getProperty(DavProperty.DISPLAYNAME);
                if (displayNameProp != null)
                {
                    displayName = ((DisplayNameProperty)displayNameProp).getDisplayName();
                }

                href = href.Substring(0, href.LastIndexOf("/")) + "/" + displayName;

                ResourceTypeProperty resourceTypeProp = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                if (resourceTypeProp.getResourceType() == ResourceTypeProperty.COLLECTION)
                    image = 2;
                else
                    image = 3;

                String cdate = "";
                String size = "";
                CreationDateProperty cdpropProp = (CreationDateProperty)response.getProperty(DavProperty.CREATIONDATE);
                if (cdpropProp != null)
                {
                    cdate = ((CreationDateProperty)cdpropProp).getCreationDate();
                }


                String lastmodified = "";
                LastModifiedProperty lastModifiedProp = (LastModifiedProperty)response.getProperty(DavProperty.GETLASTMODIFIED);
                if (lastModifiedProp != null)
                {
                    lastmodified = ((LastModifiedProperty)lastModifiedProp).getLastModified();
                }


                WebDavProperty getContentLengthProp = response.getProperty(DavProperty.GETCONTENTLENGTH);
                if (getContentLengthProp != null)
                {
                    size = getContentLengthProp.getTextContent();
                }

                System.Windows.Forms.ListViewItem Line = new System.Windows.Forms.ListViewItem(new string[] {
                    displayName,
                    href,
                    cdate,   
                    size,
                    }, image);

                this.file_list.Items.Add(Line);
            }

        }


        private void file_list_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            int item_index = ((ListView)sender).FocusedItem.Index;

            ArrayList resplist = multistatus.getResponses();
            exo_jcr.webdav.csclient.Response.DavResponse response = (exo_jcr.webdav.csclient.Response.DavResponse)resplist[item_index];

            ResourceTypeProperty rtproperty = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
            if (rtproperty.getResourceType() != ResourceTypeProperty.RESOURCE)
                return;
            String _path = response.getHref().getHref();

            WebDavProperty displayNameProperty = response.getProperty(DavProperty.DISPLAYNAME);
            _path = _path.Substring(0, _path.LastIndexOf("/")) + "/" + displayNameProperty.getTextContent();

            Utils.doGetFile(application,_path );

            this.Close();
        }

        private void btn_cancel_Click_1(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_open_Click(object sender, EventArgs e)
        {
            if (file_list.SelectedItems.Count == 0) return;
            ListViewItem curItem = file_list.SelectedItems[0];
            if (!Utils.doGetFile(application, curItem.SubItems[1].Text)) {
                return;
            }
            this.Close();
        }


        private void textEntered(object sender, EventArgs e)
        {
            this.btn_search.Enabled = true;
        }
        
    }
}