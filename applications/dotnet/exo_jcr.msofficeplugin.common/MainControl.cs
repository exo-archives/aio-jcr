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
            String item_name = listFiles.FocusedItem.Text;
            doGetFile(item_name);
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

        private bool NodeIsValid(TreeNode node) {
            String serverPrefix = application.getContext().getContextHref();
            String path = node.FullPath;
            path = path.Replace("\\", "/");
            if (path.StartsWith(serverPrefix))
            {
                path = path.Substring(serverPrefix.Length);

                if (path == "")
                {
                    path = "/";
                }
            }
            int currentStatus = getNodeStatus(path);
            if (currentStatus == DavStatus.NOT_FOUND)
            {
                return false;
            }
            else 
            {
                return true;
            }

                
        }


        private void ClearNodes(TreeNode node)
        {

            if (node.Nodes.Count != 0)
            {
                TreeNode[] Children = new TreeNode[node.Nodes.Count];
                foreach (TreeNode n in node.Nodes)
                {
                    ClearNodes(n);
                }
                if (!NodeIsValid(node))
                {
                    node.Remove();
                }
//                MessageBox.Show(node.FullPath);
            }
            else
            {
                if (!NodeIsValid(node))
                {
                    node.Remove();
                }
//                MessageBox.Show(node.FullPath);
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
                    fillFileList(path, (Multistatus)multistatusCache[path]);

                    //foreach (TreeNode n in e.Node.Nodes) {
                    //    string currentNodePath = currentPath + "/" + n.Text;

                    //    int currentStatus = getNodeStatus(currentNodePath);
                    //    if (currentStatus == DavStatus.NOT_FOUND) {
                    //        n.Remove();
                    //    }
                    //}

                    e.Node.Expand();
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

                String item_name = ((ListView)sender).FocusedItem.Text;
                doGetFile(item_name);
                
            }
            else
                return;
        }

        public Boolean doGetFile(String ItemName)
        {
            currentPath = currentPath.Replace("\\", "/");
            String href = "";

            String TopNodeName = NodeTree.TopNode.Text;
            String SelectedNodeText = NodeTree.SelectedNode.Text;

            if (SelectedNodeText.StartsWith(TopNodeName)) {
                SelectedNodeText = SelectedNodeText.Substring(TopNodeName.Length);
            }

            href = NodeTree.SelectedNode.FullPath + "/" + ItemName;
            href = href.Replace("\\", "/");
            
            if (Utils.doGetFile(application, href))
            {
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

//_

        public int getNodeStatus(String path)
        {
            try
            {
                PropFindCommand propFind = new PropFindCommand(application.getContext());
                propFind.setResourcePath(path);

                propFind.addRequiredProperty(DavProperty.DISPLAYNAME);
                propFind.addRequiredProperty(DavProperty.GETCONTENTTYPE);
                propFind.addRequiredProperty(DavProperty.RESOURCETYPE);

                propFind.addRequiredProperty(DavProperty.HREF);

                String jcrPrefix = "jcr";
                String jcrMimeType = "jcr:mimeType";
                String jcrNameSpace = "http://www.jcp.org/jcr/1.0";
                propFind.addRequiredProperty(jcrMimeType, jcrPrefix, jcrNameSpace);

                propFind.setDepth(1);

                status = propFind.execute();

                return status;
            }
            catch (Exception exc)
            {
                return -1;
            }
        }


//_

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
                            if (NodeExists(node, displayName.getDisplayName()))
                            {
                                continue;
                            }
                            else {
                                TreeNode addedNode = node.Nodes.Add(displayName.getDisplayName());
                                addedNode.ImageIndex = 1;
                                addedNode.SelectedImageIndex = 1;

                                ClearNodes(addedNode);
                            }
                        }
                    }
                }

            }
            catch (Exception exc)
            {
                MessageBox.Show("Error at " + exc.StackTrace);
            }

        }

        private bool NodeExists(TreeNode node, String name)
        {
            for (int i = 0; i <= node.Nodes.Count - 1; i++)
            {
                if (node.Nodes[i].Text.Equals(name))
                {
                    return true;
                }
            }
            return false;
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

                    String pattern = "ddd, dd MMM yyyy HH':'mm':'ss 'GMT'";

                    if (displayNameProp != null)                    
                    {
                        displayName = displayNameProp.getDisplayName();

                        if (lastModifiedProp != null)
                        {
                            modified = ParseDate(lastModifiedProp.getLastModified());
                                                     
                        }

                        if (mimeTypeProperty != null) {
                            mimeType = mimeTypeProperty.getTextContent();
                        }

                        if (getContentLengthProp != null)
                        {
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

        private String ParseDate(String date) {
            String result = "";

            date = date.Remove(date.IndexOf(',') ,1);
            date = date.Replace(':', ' ');

            String[] dateParts = date.Split(' ');

            int year = int.Parse(dateParts[3]);            
            string month = dateParts[2];
            int day = int.Parse(dateParts[1]);
            int hour = int.Parse(dateParts[4]);
            int min = int.Parse(dateParts[5]);
            int sec = int.Parse(dateParts[6]);

            DateTime dt = new DateTime(year, 8, day, hour, min, sec, DateTimeKind.Utc);

            return month + ", " + day + " " + year + " " + dt.ToLocalTime().ToLongTimeString();
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

            String fileName = ((ListView)sender).FocusedItem.Text;
            String filePath = this.NodeTree.SelectedNode.FullPath.Replace("\\", "/");

            // selectedHref = response.getHref().getHref();

            selectedHref = filePath + "/" + fileName;



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

        public int createFolder(String name)
        {
            for (int i = 0; i <= NodeTree.SelectedNode.Nodes.Count - 1; i++)
            {
                if (NodeTree.SelectedNode.Nodes[i].Text.ToLower() == name.ToLower()) {
                    MessageBox.Show("The folder with such name already exist", "Error");
                    return -1;
                }
            }

            try
            {
                DavContext context = (DavContext)application.getContext();

                MkColCommand makeCol = new MkColCommand(context);
                makeCol.setResourcePath(currentPath + "/" + name);
                int status = makeCol.execute();

                String serverPrefix = application.getContext().getContextHref();
                String path = NodeTree.SelectedNode.FullPath;

                path = path.Replace("\\", "/");

                if (path.StartsWith(serverPrefix)) {
                    path = path.Substring(serverPrefix.Length);

                    if (path == "") {
                        path = "/";
                    }

                    status = getFileList(path);
  
                    if (status == DavStatus.MULTISTATUS)
                    {
                        fillTreeList(NodeTree.SelectedNode);
                        NodeTree.SelectedNode.Expand();
                    }
                    else
                    {
                        Utils.showMessageStatus(status);
                    }
                }

                return status;
            }
            catch (Exception e) {
                return -1;
                MessageBox.Show("Failed", "Error");
            }
        }
    }
}
