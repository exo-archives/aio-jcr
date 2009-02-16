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
using System.Collections;

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
    public partial class Versions : Form
    {
        private String href;
        
        private ArrayList responses = new ArrayList();
        
        private Form parentForm;

        private ApplicationInterface application;

        private String selectedhref;

        public Versions(ApplicationInterface application, Form parentForm, String selectedhref) 
        {
            this.parentForm = parentForm;
            this.application = application;
            this.selectedhref = selectedhref;
            InitializeComponent();
        }

        private void btn_cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        public DialogResult ShowDialog(String href)
        {
            this.href = href;
            
            fillVersionList();
            return base.ShowDialog();
        }

        private void fillVersionList() {
            try
            {
                DavContext context = application.getContext();
                String path = href.Substring(context.getContextHref().Length);

                ReportCommand report = new ReportCommand(context);
                report.setResourcePath(path);

                report.addRequiredProperty(DavProperty.DISPLAYNAME);
                report.addRequiredProperty(DavProperty.CREATIONDATE);
                report.addRequiredProperty(DavProperty.GETCONTENTLENGTH);
                report.addRequiredProperty(DavProperty.CREATORDISPLAYNAME);
                
                report.addRequiredProperty(DavProperty.VERSIONNAME);

                int status = report.execute();

                if (status == DavStatus.MULTISTATUS)
                {
                    responses = report.getMultistatus().getResponses();

                    responses.Sort(new VersionComparer());

                    for (int i = 0; i < responses.Count; i++)
                    {
                        DavResponse response = (DavResponse)responses[i];

                        DisplayNameProperty displayNameProp = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                        CreationDateProperty createdProp = (CreationDateProperty)response.getProperty(DavProperty.CREATIONDATE);                        
                        ContentLenghtProperty contentLengthProp = (ContentLenghtProperty)response.getProperty(DavProperty.GETCONTENTLENGTH);

                        String displayName = "";
                        String created = "";
                        String contentLength = "";
                        String creator = "";

                        if (displayNameProp != null) {
                            displayName = displayNameProp.getDisplayName();
                        }
                        
                        if (createdProp != null) {
                            created = createdProp.getCreationDate();
                        }
                        
                        if (contentLengthProp != null) {
                            contentLength = contentLengthProp.getContentLenght();
                        }

                        ListViewItem viewItem = new ListViewItem(new string[] {
                            displayName,
                            created,
                            contentLength,
                            creator},
                        0);
                        
                        list_versions.Items.Add(viewItem);
                    }

                }
                else {
                    Utils.showMessageStatus(status);
                    this.Close();
                }

            } catch (Exception etr) {
                MessageBox.Show("Error! Can't connect to the server!", Utils.CAPTION,
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                this.Close();
            }

        }

        private void btn_open_Click(object sender, EventArgs e)
        {
            if (openVersionFile()) {
                Close();
            }
        }

        private void list_versions_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if (openVersionFile())
            {
                Close();
            }
        }

        private bool openVersionFile()
        {
            if (list_versions.SelectedItems.Count == 0) return false;
            int item_index = list_versions.FocusedItem.Index;
            DavResponse response = (DavResponse)responses[item_index];
            String href = response.getHref().getHref();
            int versionStart = href.LastIndexOf("?");
            String version = href.Substring(versionStart);
            href = selectedhref + version;

            ((NOpen)parentForm).versionHref = href;
            return true;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            String selectedFileName = selectedhref.Substring(selectedhref.LastIndexOf("/") + 1);
            String currentFileName;
            try
            {
                 currentFileName = this.application.getActiveDocumentName();
            }
            catch (Exception exc) {
                MessageBox.Show("There is no active file to compare.", "Error");
                this.Close();
                return;
            }
            

            if (currentFileName.Contains("%3F")) {
                currentFileName = currentFileName.Substring(0, currentFileName.LastIndexOf("%3F"));
            }


            if (!selectedFileName.Equals(currentFileName))
            {
                MessageBox.Show("You can't compare versions of different files", "Error");
                return;
            }


            if (openVersionFile())
            {
                ((NOpen)parentForm).isNeedCompare = true;
                Close();
            }
        }

    }

    public class VersionComparer : IComparer
    {

        public int Compare(object x, object y)
        {

            DavResponse response1 = (DavResponse)x;
            CreationDateProperty creationDate1 = (CreationDateProperty)response1.getProperty(DavProperty.CREATIONDATE);

            DavResponse response2 = (DavResponse)y;
            CreationDateProperty creationDate2 = (CreationDateProperty)response2.getProperty(DavProperty.CREATIONDATE);

            if (creationDate1.getCreationDate().Equals(creationDate2.getCreationDate())) {

                DisplayNameProperty displayName1 = (DisplayNameProperty)response1.getProperty(DavProperty.DISPLAYNAME);
                DisplayNameProperty displayName2 = (DisplayNameProperty)response2.getProperty(DavProperty.DISPLAYNAME);

                return displayName2.getDisplayName().CompareTo(displayName1.getDisplayName());
            }

            return creationDate2.getCreationDate().CompareTo(creationDate1.getCreationDate());
        }

    }
}
