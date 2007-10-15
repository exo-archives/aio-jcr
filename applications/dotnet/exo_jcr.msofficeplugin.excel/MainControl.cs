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
using Microsoft.Office.Interop.Excel;
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

namespace exo_jcr.msofficeplugin.excel
{
    public partial class MainControl : UserControl
    {
        private Connect connect;
        private Hashtable multistatusCache = new Hashtable();
        private int status = 0;
        private String currentPath;
        private ArrayList filteredResponses = new ArrayList();

        public Hashtable getMultistatusCache()
        {
            return multistatusCache;
        }

        
        public MainControl()
        {
            InitializeComponent();
        }

       private void setCurrentPath(String path)
       {
           this.currentPath = path;
       }


        public void SetConnect(Connect connect) {
            this.connect = connect;
        }



        private void MainControl_Load(object sender, EventArgs e)
        {
            if (connect == null)
            {
                connect = new Connect();
            }

            DavContext context = connect.getContext();

            if (context != null)
            {
                string treeName = context.getContextHref() + "/" + connect.Workspace;
                TreeNode repositoryNode = NodeTree.Nodes.Add(treeName);
            }
            else {
                ParentForm.Close();
            }
        }

        public void openClick() 
        {
            if (listFiles.SelectedItems.Count == 0) return;
            int item_index = listFiles.FocusedItem.Index;
            doGetFile(item_index);
        }


        public void saveClick(Microsoft.Office.Interop.Excel._Application app, String entered_filename)
        {
            String path = NodeTree.SelectedNode.FullPath;
            path = path.Replace("\\", "/");

            Microsoft.Office.Interop.Excel.Workbook doc = app.ActiveWorkbook;

            String localFilePath = app.ActiveWorkbook.FullName;
            if (localFilePath.StartsWith(connect.getCacheFolder()))
            {
                if (!doc.Saved) {
                    doc.Save();
                }

                String remoteFileName = path.Substring(path.IndexOf("/" + connect.Workspace));
                doPutFile(localFilePath, remoteFileName + entered_filename);
            }
            else
            {
                String tmp = path.Substring(connect.getContext().getContextHref().Length + 1);                
                String localFolder = (connect.getCacheFolder() + tmp + "/").Replace('/', '\\'); 

                if (!Directory.Exists(localFolder)) {
                    Directory.CreateDirectory(localFolder);
                }

                String localNameToSave = (localFolder + entered_filename + ".xls").Replace('/', '\\');

                try
                {
                    String wFileName = localNameToSave;
                    object omissing = Missing.Value;
                    doc.SaveAs(wFileName,  omissing,  omissing,  omissing,  omissing,
                         omissing, Microsoft.Office.Interop.Excel.XlSaveAsAccessMode.xlShared,   omissing,  omissing,  omissing,
                         omissing,  omissing);

                    
                    String filename = app.ActiveWorkbook.Name;
                    String remoteFileName = path.Substring(path.IndexOf("/" + connect.Workspace));

                    remoteFileName = ("/" + localNameToSave.Substring(connect.getCacheFolder().Length) ).Replace('\\', '/');
                    localNameToSave = localNameToSave.Replace('\\', '/');

                  //  MessageBox.Show("LP: [" + localNameToSave + "]");
                  //  MessageBox.Show("RP: [" + remoteFileName + "]");

                    doPutFile(localNameToSave, remoteFileName);
                }
                catch (Exception e)
                {
                    MessageBox.Show("Error! " + e.Message + " " + e.StackTrace);
                }
                
            }
            
        }

        private void doPutFile(String localName, String remoteName)
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

                DavContext context = connect.getContext();
                PutCommand put = new PutCommand(context);
                //MessageBox.Show("remoteFileName: " + remoteFileName);
                put.setResourcePath(remoteName);
                put.setRequestBody(filedata);
                int status = put.execute();
                if (status != DavStatus.CREATED)
                {
                    MessageBox.Show("Can't save file. Status: " + status, "Error",
                     MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
                else
                {
                    MessageBox.Show("File saved successfully!", "Info", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    ParentForm.Close();
                }
            }
            catch (FileNotFoundException ee)
            {

                MessageBox.Show("Please, save the file locally first!", "Can't read file", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;

            }
        }

        private void NodeTree_AfterSelect(object sender, TreeViewEventArgs e)
        {
            String serverPrefix = connect.getContext().getContextHref();

            String path = e.Node.FullPath;
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

        public void doGetFile(int ItemId)
        {
            currentPath = currentPath.Replace("\\", "/");

            DavResponse response = (DavResponse)filteredResponses[ItemId];

            String href = response.getHref().getHref();
            doGetFile(href);
           
        }

        public void doGetFile(string href)
        {
            String contexthref = connect.getContext().getContextHref();
            href = href.Substring(contexthref.Length);

            Environment.SpecialFolder p = Environment.SpecialFolder.Personal;
            String s_p = Environment.GetFolderPath(p);

            int index1 = href.IndexOf(connect.Workspace);
            int index2 = href.LastIndexOf("/");
            String folder = s_p + "\\repository\\" + href.Substring(index1, index2 - index1);
            folder = folder.Replace("/", "\\");

            if (!Directory.Exists(folder))
            {
                try
                {
                    DirectoryInfo dirinfo = Directory.CreateDirectory(folder);
                }
                catch (Exception ee)
                {
                    MessageBox.Show("At doGetFile():" + ee.Message + ee.StackTrace);
                }
            }

            String f_name = href.Substring(href.LastIndexOf("/") + 1);
            String FILE_NAME = folder + "\\" + f_name;

            DavContext context = connect.getContext();
            try
            {
                GetCommand get = new GetCommand(context);
                get.setResourcePath(href);

                int status = get.execute();
                if (status == DavStatus.OK)
                {
                    byte[] resp = get.getResponseBody();

                    if (File.Exists(FILE_NAME))
                    {
                        File.Delete(FILE_NAME);
                    }
                    Thread.Sleep(200);

                    FileStream fs = new FileStream(FILE_NAME, FileMode.Create, FileAccess.ReadWrite, FileShare.ReadWrite);
                    BinaryWriter w = new BinaryWriter(fs);
                    for (long i = 0; i < resp.Length; i++)
                    {
                        w.Write(resp[i]);
                    }
                    w.Close();
                    fs.Close();
                }
                //MessageBox.Show("SAVED AS:"+FILE_NAME);
                connect.Filename = FILE_NAME;
                ParentForm.Close();

            }
            catch (IOException rr)
            {
                MessageBox.Show("The file seemed to be already opened", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Stop);
            }
            catch (Exception ed)
            {
                MessageBox.Show("AT doGetFile " + ed.Message + ed.StackTrace);
                return;
            }


        }


        public int getFileList(String path)
        {
            try
            {
                PropFindCommand propFind = new PropFindCommand(connect.getContext());
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
                MessageBox.Show("Cannot receive multistatus,\n please check Settings.", "Error",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
                //this.Close();
            }
            return -1;
        }


        public void fillTreeList(TreeNode node)
        {
            try
            {
                
                Multistatus multistatus = (Multistatus)multistatusCache[getFullPath(node)];
                ArrayList responses = multistatus.getResponses();
                
                for (int i = 0; i < responses.Count; i++)
                {
                    DavResponse response = (DavResponse)responses[i];

                    //MessageBox.Show("H1: [" + response.getHref().getHref() + "]\r\n" + 
                    //    "H2: [" + node.FullPath + "]");
                    //MessageBox.Show("HREF:" + response.getHref().getHref() + "\n" + node.FullPath);
                    if (response.getHref().getHref().Equals(node.FullPath.Replace("\\", "/")))
                    {
                        
                        continue;
                    }

                    DisplayNameProperty displayName = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                    ResourceTypeProperty resourceType = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                    if (displayName != null)
                    {
                        if (resourceType != null && resourceType.getResourceType() == ResourceTypeProperty.RESOURCE)
                        {
                            //addedNode.ImageIndex = 2;
                            //addedNode.SelectedImageIndex = 2;
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
                MessageBox.Show("EXCEPTION " + exc.Message + " : " + exc.StackTrace);
            }

        }

        private String getFullPath(TreeNode node)
        {
            String serverPrefix = connect.getContext().getContextHref();
            String fullPath = node.FullPath.Substring(serverPrefix.Length);
            if (fullPath == "")
            {
                fullPath = "/";
            }
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

        public void fillFileList(String remotePath, Multistatus multistatus)
        {
            fillFilteredResponses(multistatus);

            listFiles.Items.Clear();

            try
            {
                for (int i = 0; i < filteredResponses.Count; i++)
                {
                    DavResponse response = (DavResponse)filteredResponses[i];

                    String displayName = "";
                    String created = "";
                    String modified = "";
                    String size = "";

                    DisplayNameProperty displayNameProp = (DisplayNameProperty)response.getProperty(DavProperty.DISPLAYNAME);
                    ResourceTypeProperty resourceTypeProp = (ResourceTypeProperty)response.getProperty(DavProperty.RESOURCETYPE);
                    CreationDateProperty creationDateProp = (CreationDateProperty)response.getProperty(DavProperty.CREATIONDATE);
                    LastModifiedProperty lastModifiedProp = (LastModifiedProperty)response.getProperty(DavProperty.GETLASTMODIFIED);
                    ContentLenghtProperty getContentLengthProp = (ContentLenghtProperty)response.getProperty(DavProperty.GETCONTENTLENGTH);

                    WebDavProperty versionNameProp = response.getProperty("D:" + DavProperty.VERSIONNAME);

                    if (displayNameProp != null)                    
                    {
                        displayName = displayNameProp.getDisplayName();

                        if (creationDateProp != null)
                        {
                            created = creationDateProp.getCreationDate();
                        }

                        if (lastModifiedProp != null)
                        {
                            modified = lastModifiedProp.getLastModified();
                        }

                        if (getContentLengthProp != null)
                        {
                            size = getContentLengthProp.getContentLenght();
                        }

                        int imageId = 2;
                        if (versionNameProp != null && versionNameProp.getStatus() == DavStatus.OK) {
                            imageId = 6;
                        }

                        ListViewItem viewItem = new ListViewItem(new string[] {
                            displayName,
                            created,
                            modified,
                            size}, imageId);

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
            if (ParentForm.Name.Equals("NOpen"))
            {
                int item_index = ((ListView)sender).FocusedItem.Index;

                DavResponse response = (DavResponse)filteredResponses[item_index];
                selectedHref = response.getHref().getHref();
                WebDavProperty versionNameProp = response.getProperty("D:" + DavProperty.VERSIONNAME);
                if (versionNameProp != null && versionNameProp.getStatus() == DavStatus.OK)
                {
                    ((NOpen)ParentForm).activateVersionButton(true);
                }
                else { 
                    ((NOpen)ParentForm).activateVersionButton(false);
                } 
            }
            else
                return;
        }


    }
}
