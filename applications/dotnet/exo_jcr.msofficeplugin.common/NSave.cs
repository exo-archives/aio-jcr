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
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Collections;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.DavProperties;
using exo_jcr.webdav.csclient;

namespace exo_jcr.msofficeplugin.common
{
    public partial class NSave : Form, DialogInterface
    {

        public static String WORDDOCUMENT = "Word Document (.doc)";

        public static String WORDTEMPLATE = "Word Template (.dot)";

        public static String TEXTFILE = "Text File (.txt)";

        public static String HTMLFILE = "HTML File (.html)";

        public static String EXCELFILE = "Excel document (.xls)";

        public static String EXCELTEMPLATE = "Excel Template (.xlt)";

        public static String XMLTABLE = "XML Table (.xml)";

        public static String PRESENTATIONFILE = "Presentation (.ppt)";

        private ApplicationInterface application;

        public NSave(ApplicationInterface application)
        {
            InitializeComponent();

            this.application = application;

            mainControl1.initApps(application, this);

            String fileName = application.getActiveDocumentName();

            box_filename.Text = fileName;
        }

        public void setFileTypes(ArrayList fileTypes)
        {
            if (fileTypes.Count == 0)
            {
                return;
            }
            for (int i = 0; i < fileTypes.Count; i++)
            {
                box_filetype.Items.Add((String)fileTypes[i]);
            }
            this.box_filetype.SelectedIndex = 0;
        }

        public void enableVersions(Boolean enableVersions)
        {
        }

        private void btn_cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_save_Click(object sender, EventArgs e)
        {
            String edtFileName = box_filename.Text;

            char[] chars = new char[8];
            chars[0] = '\\';
            chars[1] = '/';
            chars[2] = '[';
            chars[3] = ']';
            chars[4] = '*';
            chars[5] = '?';
            chars[6] = ':';
            chars[7] = '*';

            while (true) {
                String trimed = edtFileName.Trim(chars);
                trimed = trimed.Trim();
                if (edtFileName == trimed) {
                    break;
                }
                edtFileName = trimed;
            }

            if (edtFileName == "") {
                MessageBox.Show("Please enter valid file name!");
                return;
            }

            box_filename.Text = edtFileName;

            String fileName = application.getActiveDocumentName();

            if (fileName == "") {
                return;
            }

            String contentType = MimeTypes.MIMETYPE_DOC;
            String selectedType = box_filetype.Text;
            
            if (selectedType.IndexOf('.') > 0) {
                if (edtFileName.IndexOf('.') > 0) {
                    String selectedExtension = selectedType.Substring(selectedType.IndexOf('.'));
                    selectedExtension = selectedExtension.Substring(0, selectedExtension.IndexOf(')'));
                    
                    String fileExtension = edtFileName.Substring(edtFileName.LastIndexOf('.'));

                    if (selectedExtension != fileExtension) {
                        String newFileName = edtFileName.Substring(0, edtFileName.IndexOf('.'));
                        newFileName += selectedExtension;

                        Boolean finded = false;
                        ArrayList responses = mainControl1.getFilteredResponses();
                        for (int i = 0; i < responses.Count; i++)
                        {
                            DavResponse response = (DavResponse)responses[i];
                            DisplayNameProperty displayName = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                            if (displayName.getDisplayName().Equals(newFileName) &&
                                !displayName.getDisplayName().Equals(fileName)) {
                                    DialogResult result = MessageBox.Show("Do you want to rewrite file " + newFileName + " ?",
                                        "eXo WebDav Client", MessageBoxButtons.YesNoCancel);
                                
                                    if (result == DialogResult.Cancel) {
                                        this.Close();
                                        return;
                                    }

                                    if (result == DialogResult.No) {
                                        return;
                                    }
                            }
                        }

                        box_filename.Text = newFileName;
                    }
                }
            }

            if (selectedType == WORDDOCUMENT) {
                contentType = MimeTypes.MIMETYPE_DOC;
            } 
            else if (selectedType == WORDTEMPLATE)
            {
                contentType = MimeTypes.MIMETYPE_DOT;
            }
            else if (selectedType == TEXTFILE)
            {
                contentType = MimeTypes.MIMETYPE_TXT;
            } 
            else if (selectedType == HTMLFILE) {
                contentType = MimeTypes.MIMETYPE_HTML;
            } 
            else if (selectedType == EXCELFILE) {
                contentType = MimeTypes.MIMETYPE_XLS;
            }
            else if (selectedType == EXCELTEMPLATE) {
                contentType = MimeTypes.MIMETYPE_XLT;
            }
            else if (selectedType == XMLTABLE) {
                contentType = MimeTypes.MIMETYPE_XML;
            }
            else if (selectedType == PRESENTATIONFILE) {
                contentType = MimeTypes.MIMETYPE_PPT;
            }

            mainControl1.saveClick(box_filename.Text, contentType);
        }

        private void btnFolderCreate_Click(object sender, EventArgs e)
        {
            NCreate NodeCreator = new NCreate();
            NodeCreator.ShowDialog();

            if (Utils.checkNodeNameValid(NodeCreator.folderName))
            {
                mainControl1.createFolder(NodeCreator.folderName);
            }        
        }

    }
}