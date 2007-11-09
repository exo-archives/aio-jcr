/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using Microsoft.Office.Core;
using System.Reflection;

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
    public partial class MainControl : UserControl
    {
        protected String currentPath;

        private ApplicationInterface application;

        private DialogInterface dialog;

        private Hashtable multistatusCache = new Hashtable();

        private int status = 0;
        
        private ArrayList filteredResponses = new ArrayList();

        public Hashtable getMultistatusCache()
        {
            return multistatusCache;
        }
        
        public MainControl()
        {
            InitializeComponent();
        }

        public ArrayList getFilteredResponses()
        {
            return filteredResponses;
        }

        public void initApps(ApplicationInterface application, DialogInterface dialog)
        {
            this.application = application;
            this.dialog = dialog;
        }

        protected void setCurrentPath(String path)
        {
            this.currentPath = path;
        }

        private void MainControl_Load(object sender, EventArgs e)
        {
            if (application == null) {
                return;
            }
            refresh();
        }

        public void refresh()
        {
            DavContext context = application.getContext();

            if (context == null)
            {
                ParentForm.Close();
                return;
            }

            string treeName = context.getContextHref() + "/" + application.getWorkspaceName();
            TreeNode repositoryNode = NodeTree.Nodes.Add(treeName);
        }

        public void openClick() 
        {
            if (listFiles.SelectedItems.Count == 0) return;
            int item_index = listFiles.FocusedItem.Index;
            doGetFile(item_index);
        }

        public void saveClick(String entered_filename, String contentType)
        {
            String localFilePath = application.getActiveDocumentFullName();// app.ActiveDocument.FullName;

            String path = NodeTree.SelectedNode.FullPath;
            String tmp = path.Substring(application.getContext().getContextHref().Length + 1);
            String localFolder = (application.getCacheFolder() + tmp + "/").Replace('/', '\\');

            if (!Directory.Exists(localFolder))
            {
                Directory.CreateDirectory(localFolder);
            }

            String localNameToSave = (localFolder + entered_filename).Replace('/', '\\');

            try
            {
                application.saveDocumentWithFormat(localNameToSave, contentType);
                localFilePath = application.getActiveDocumentFullName();
                String remoteFileName = localFilePath.Substring(application.getCacheFolder().Length - 1);
                remoteFileName = remoteFileName.Replace("\\", "/");
                doPutFile(localFilePath, remoteFileName, contentType);
            }
            catch (Exception e)
            {
                MessageBox.Show("Error! " + e.Message + " " + e.StackTrace);
            }
        }

        private void doPutFile(String localName, String remoteName, String contentType)
        {
            try
            {
                FileStream stream = new FileStream(localName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);

                long len = stream.Length;
                byte[] filedata = new byte[len];
                int readed = 0;
                while (readed < len)
                {
                    readed += stream.Read(filedata, 0, (int)(len - readed));
                }

                DavContext context = application.getContext();
                PutCommand put = new PutCommand(context);
                put.addRequestHeader(HttpHeaders.CONTENTTYPE, contentType);
                put.setResourcePath(remoteName);
                put.setRequestBody(filedata);
                int status = put.execute();
                if (status != DavStatus.CREATED)
                {
                    Utils.showMessageStatus(status);
                }
                else
                {
                    MessageBox.Show("File saved successfully!", "Info", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    ParentForm.Close();
                }
            }
            catch (FileNotFoundException ee)
            {
                MessageBox.Show("File read error!", "Can't read file", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;

            }
        }

        private void NodeTree_AfterSelect(object sender, TreeViewEventArgs e)
        {
            String serverPrefix = application.getContext().getContextHref();

            String path = e.Node.FullPath;

            path = path.Replace("\\", "/");

            if (path.StartsWith(serverPrefix))
            {
                path = path.Substring(serverPrefix.Length);

                if (path == "")
                {
                    path = "/";
                }

                setCurrentPath(path);
                if (multistatusCache[path] != null)
                {
                    fillFileList(path, (Multistatus)multistatusCache[path]);
                    return;
                }

                if (e.Node.ImageIndex == 2)
                {
                    return;
                }

                int status = getFileList(path);

                if (status == DavStatus.MULTISTATUS)
                {
                    fillTreeList(e.Node);
                    e.Node.Expand();
                    fillFileList(path, (Multistatus)multistatusCache[path]);
                }
                else
                {
                    Utils.showMessageStatus(status);
                }

            }

        }

        private void listFiles_Double_click(object sender, EventArgs e)
        {
            if (ParentForm.Name.Equals("NOpen"))
            {
                int item_index = ((ListView)sender).FocusedItem.Index;
                doGetFile(item_index);
            }
            else
                return;
        }

        public Boolean doGetFile(int ItemId)
        {
            currentPath = currentPath.Replace("\\", "/");
            DavResponse response = (DavResponse)filteredResponses[ItemId];
            String href = response.getHref().getHref();
            if (Utils.doGetFile(application, href)) {
                ParentForm.Close();
                return true;
            }
            return false;
        }

        public int getFileList(String path)
        {
            try
            {
                PropFindCommand propFind = new PropFindCommand(application.getContext());
                propFind.setResourcePath(path);

                propFind.addRequiredProperty(DavProperty.DISPLAYNAME);
                propFind.addRequiredProperty(DavProperty.GETCONTENTTYPE);
                propFind.addRequiredProperty(DavProperty.RESOURCETYPE);
                propFind.addRequiredProperty(DavProperty.GETLASTMODIFIED);

                propFind.addRequiredProperty(DavProperty.GETCONTENTLENGTH);

                propFind.addRequiredProperty(DavProperty.CREATIONDATE);
                propFind.addRequiredProperty(DavProperty.HREF);
                propFind.addRequiredProperty(DavProperty.SUPPORTEDLOCK);
                propFind.addRequiredProperty(DavProperty.VERSIONNAME);

                String jcrPrefix = "jcr";
                String jcrMimeType = "jcr:mimeType";
                String jcrNameSpace = "http://www.jcp.org/jcr/1.0";
                propFind.addRequiredProperty(jcrMimeType, jcrPrefix, jcrNameSpace);

                propFind.setDepth(1);

                status = propFind.execute();

                if (status == DavStatus.MULTISTATUS)
                {
                    if (multistatusCache[path] != null)
                    {
                        multistatusCache.Remove(path);
                    }

                    multistatusCache.Add(path, propFind.getMultistatus());
                }
              
                return status;
            }
            catch (Exception exc)
            {
                return -1;
            }
        }

        public void fillTreeList(TreeNode node)
        {
            try
            {
                String nodePath = getFullPath(node);
                
                Multistatus multistatus = (Multistatus)multistatusCache[nodePath];
                if (multistatus == null) {
                    return;
                }
                ArrayList responses = multistatus.getResponses();
                
                for (int i = 0; i < responses.Count; i++)
                {
                    DavResponse response = (DavResponse)responses[i];

                    String responseHref = response.getHref().getHref();

                    String nodeFullPath = node.FullPath.Replace("\\", "/");

                    if (responseHref.Equals(nodeFullPath))
                    {
                        continue;
                    }

                    if (responseHref.Equals(nodeFullPath + "/"))
                    {
                        continue;
                    }

                    DisplayNameProperty displayName = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                    ResourceTypeProperty resourceType = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                    if (displayName != null)
                    {
                        if (resourceType != null && resourceType.getResourceType() == ResourceTypeProperty.RESOURCE)
                        {
                            continue;
                        }
                        else
                        {
                            TreeNode addedNode = node.Nodes.Add(displayName.getDisplayName());
                            addedNode.ImageIndex = 1;
                            addedNode.SelectedImageIndex = 1;
                        }
                    }
                }

            }
            catch (Exception exc)
            {
                MessageBox.Show("Error at " + exc.StackTrace);
            }

        }

        private String getFullPath(TreeNode node)
        {
            String serverPrefix = application.getContext().getContextHref();
            String fullPath = node.FullPath.Substring(serverPrefix.Length);
            if (fullPath == "")
            {
                fullPath = "/";
            }

            fullPath = fullPath.Replace("\\", "/");
            return fullPath;
        }

        private void fillFilteredResponses(Multistatus multistatus)
        {
            filteredResponses.Clear();
            ArrayList responses = multistatus.getResponses();
            for (int i = 0; i < responses.Count; i++)
            {
                DavResponse response = (DavResponse)responses[i];

                ResourceTypeProperty resourceTypeProp = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                if (resourceTypeProp != null && resourceTypeProp.getResourceType() == ResourceTypeProperty.RESOURCE)
                {
                    filteredResponses.Add(response);
                }                
            }
        }

        private int getImageIdByMimeType(WebDavProperty mimeTypeProperty)
        {
            if (mimeTypeProperty == null) {
                return 9;
            }

            String mimeType = mimeTypeProperty.getTextContent();

            if (mimeType == MimeTypes.MIMETYPE_TXT || mimeType == MimeTypes.MIMETYPE_TXTPLAIN)
            {
                return 9;
            }

            if (mimeType == MimeTypes.MIMETYPE_DOC || mimeType == MimeTypes.MIMETYPE_WORD)
            {
                return 10;
            }

            if (mimeType == MimeTypes.MIMETYPE_DOT)
            {
                return 11;
            }

            if (mimeType == MimeTypes.MIMETYPE_HTML)
            {
                return 12;
            }

            if (mimeType == MimeTypes.MIMETYPE_XLS) {
                return 13;
            }

            if (mimeType == MimeTypes.MIMETYPE_PPT) {
                return 14;
            }

            if (mimeType == MimeTypes.MIMETYPE_XLT) {
                return 15;
            }

            if (mimeType == MimeTypes.MIMETYPE_XML) {
                return 16;
            }

            return 9;
        }

        public void fillFileList(String remotePath, Multistatus multistatus)
        {
            ArrayList responses = multistatus.getResponses();

            fillFilteredResponses(multistatus);

            listFiles.Items.Clear();

            try
            {
                for (int i = 0; i < filteredResponses.Count; i++)
                {
                    DavResponse response = (DavResponse)filteredResponses[i];

                    String displayName = "";
                    String size = "";
                    String mimeType = "";
                    String modified = "";

                    DisplayNameProperty displayNameProp = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                    ResourceTypeProperty resourceTypeProp = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                    LastModifiedProperty lastModifiedProp = (LastModifiedProperty)response.getProperty(DavProperty.GETLASTMODIFIED);
                    ContentLenghtProperty getContentLengthProp = (ContentLenghtProperty)response.getProperty(DavProperty.GETCONTENTLENGTH);

                    WebDavProperty versionNameProp = response.getProperty("D:" + DavProperty.VERSIONNAME);

                    WebDavProperty mimeTypeProperty = response.getProperty("jcr:mimeType");

                    if (displayNameProp != null)                    
                    {
                        displayName = displayNameProp.getDisplayName();

                        if (lastModifiedProp != null)
                        {
                            modified = lastModifiedProp.getLastModified();
                        }

                        if (mimeTypeProperty != null) {
                            mimeType = mimeTypeProperty.getTextContent();
                        }

                        if (getContentLengthProp != null)
                        {
                            //size = getContentLengthProp.getContentLenght();

                            long fileSize = System.Convert.ToInt64(getContentLengthProp.getContentLenght());

                            if (fileSize < 1024)
                            {
                                size = fileSize.ToString();
                            }
                            else if (fileSize < (1024 * 1024))
                            {
                                fileSize = fileSize >> 10;
                                size += fileSize;
                                size += "K";
                            }
                            else
                            {
                                String kb = "" + (fileSize >> 10) % 1024;
                                while (kb.Length < 3)
                                {
                                    kb = "0" + kb;
                                }
                                fileSize = fileSize >> 20;
                                size += fileSize;
                                size += ".";
                                size += kb.ToCharArray()[0];
                                size += "M";
                            }

                        }

                        int imageId = getImageIdByMimeType(mimeTypeProperty);

                        ListViewItem viewItem = new ListViewItem(new string[] {
                            displayName,
                            size,
                            mimeType,
                            modified}, imageId);

                        listFiles.Items.Add(viewItem);
                    }
                }
            }
            catch (Exception exc)
            {
                MessageBox.Show("EXCEPTION " + exc.Message + " : " + exc.StackTrace);
            }

        }

        public String selectedHref;

        private void listFiles_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (dialog == null) {
                return;
            }

            if (!(ParentForm.Name.Equals("NOpen")))
            {
                return;
            }

            int item_index = ((ListView)sender).FocusedItem.Index;

            DavResponse response = (DavResponse)filteredResponses[item_index];
            selectedHref = response.getHref().getHref();

            WebDavProperty versionNameProp = response.getProperty("D:" + DavProperty.VERSIONNAME);
            if (versionNameProp != null && versionNameProp.getStatus() == DavStatus.OK)
            {
                dialog.enableVersions(true);
            }
            else
            {
                dialog.enableVersions(false);
            }
        }


    }
}
