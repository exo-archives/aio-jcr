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

namespace exo_jcr.msofficeplugin
{
    public partial class Versions : Form
    {
        private Connect connect;
        private String href;
        private ArrayList responses = new ArrayList();
        private Form parentForm;

        public Versions(Connect connect, Form parentForm)
        {
            this.connect = connect;
            this.parentForm = parentForm;
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
                DavContext context = connect.getContext();
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
                    
                    for (int i = 0; i < responses.Count; i++)
                    {
                        DavResponse response = (DavResponse)responses[i];

                        DisplayNameProperty displayNameProp = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                        CreationDateProperty createdProp = (CreationDateProperty)response.getProperty(DavProperty.CREATIONDATE);                        
                        ContentLenghtProperty contentLengthProp = (ContentLenghtProperty)response.getProperty(DavProperty.GETCONTENTLENGTH);

                        //WebDavProperty creatorDisplayNameProp = (WebDavProperty)response.getProperty("D:" + DavProperty.CREATORDISPLAYNAME);

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

                        //if (creatorDisplayNameProp != null) {
                        //    creator = creatorDisplayNameProp.getTextContent();
                        //}

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
                    MessageBox.Show("Cannot receive versions list!", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                    this.Close();
                }

            } catch (Exception etr) {
                MessageBox.Show("at fillVersionList"+etr.Message+etr.StackTrace);
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
            ((NOpen)parentForm).versionHref = href;
            return true;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (openVersionFile())
            {
                ((NOpen)parentForm).isNeedCompare = true;
                Close();
            }
        }

    }
}