/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections;
using Extensibility;
using System.Runtime.InteropServices;
using System.Reflection;
using Microsoft.Office.Core;
using Microsoft.Office.Interop.PowerPoint;
using System.Drawing;
using System.Text;
using System.IO;
using System.Windows.Forms;
using System.Threading;
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
 *
 */

[assembly: RegistryPermissionAttribute(SecurityAction.RequestMinimum,
    ViewAndModify = "HKEY_CURRENT_USER")]


namespace exo_jcr.msofficeplugin.ppoint

{

	#region Read me for Add-in installation and setup information.
	// When run, the Add-in wizard prepared the registry for the Add-in.
	// At a later time, if the Add-in becomes unavailable for reasons such as:
	//   1) You moved this project to a computer other than which is was originally created on.
	//   2) You chose 'Yes' when presented with a message asking if you wish to remove the Add-in.
	//   3) Registry corruption.
	// you will need to re-register the Add-in by building the eXo.msofficeplugin project, 
	// right click the project in the Solution Explorer, then choose install.
	#endregion
    [GuidAttribute("0CEBEDF8-C8E8-4C34-B31C-567E24EFCCF6"), ProgId("exo_jcr.msofficeplugin.ppoint.setup.Connect")]
	public class Connect : Object, Extensibility.IDTExtensibility2
	{
        private CommandBarButton Open;
        private CommandBarButton Search;
        private CommandBarButton Save;
        private CommandBarButton SaveAs;
        //private CommandBarButton CompareWithBase;
        private CommandBarButton Settings;
        private CommandBarButton About;
        

        private CommandBarPopup eXoMenu;
        //public Word._Application app;

        public  Microsoft.Office.Interop.PowerPoint._Application app;
        
        private NOpen DialogOpen;
        private NSave DialogSave;
        private Search DialogSearch;
        private AboutBox AboutBox;
        
        private object applicationObject;
        private object addInInstance;

        private String filename;

        private String workspace;

        public string Workspace 
        {
            set
            {
                workspace = value;
            }


            get
            {
                return workspace;
            }
        }

        public string Filename
        {
            set
            {
                filename = value;
            }

            get
            {
                return filename;
            }
        }
        

		public Connect()
		{
		}

        private DavContext davContext; 

        public DavContext getContext(String url)
        {
            davContext = createContext(url);
            if (davContext == null) {
                MessageBox.Show("Cannot load paramethers,\n please run Settings first.", "Error",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
                return null;
            } else {
                return davContext;
            }

            
        }

        public DavContext getContext()
        {
            davContext = createContext("");
            if (davContext == null)
            {
                MessageBox.Show("Cannot load paramethers,\n please run Settings first.", "Error",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
                return null;
            }
            else
            {
                return davContext;
            }
        }

        public void OnConnection(object application, Extensibility.ext_ConnectMode connectMode, object addInInst, ref System.Array custom) {
            //Console.Beep(3000, 20);
            applicationObject = application;
            addInInstance = addInInst;

            if (connectMode != Extensibility.ext_ConnectMode.ext_cm_Startup) {
                OnStartupComplete(ref custom);
            }
        }

        public void OnDisconnection(Extensibility.ext_DisconnectMode disconnectMode, ref System.Array custom) {
            if (disconnectMode != Extensibility.ext_DisconnectMode.ext_dm_HostShutdown)
            {
                OnBeginShutdown(ref custom);
            }
            applicationObject = null;
        }

        
        public void OnAddInsUpdate(ref System.Array custom) {
        }

        #region OnStartupComplete(ref System.Array custom)
        public void OnStartupComplete(ref System.Array custom) {
            CommandBars oCommandBars;
            CommandBar oStandardBar;

            object omissing = System.Reflection.Missing.Value;

            Thread.Sleep(50);
            Console.Beep(3000, 20);

            app = (Microsoft.Office.Interop.PowerPoint._Application)applicationObject;

            try
            {
                oCommandBars = (CommandBars)applicationObject.GetType().InvokeMember("CommandBars", BindingFlags.GetProperty, null, applicationObject, null);
            }
            catch (Exception)
            {
                //// Outlook has the CommandBars collection on the Explorer object.
                //object oActiveExplorer;
                //oActiveExplorer = applicationObject.GetType().InvokeMember("ActiveExplorer", BindingFlags.GetProperty, null, applicationObject, null);
                //oCommandBars = (CommandBars)oActiveExplorer.GetType().InvokeMember("CommandBars", BindingFlags.GetProperty, null, oActiveExplorer, null);
                return;
            }


            // Set up a custom button on the "Standard" commandbar.
            try
            {
                oStandardBar = oCommandBars["Menu Bar"];
            }            
            catch (Exception)
            {
                // Access names its main toolbar Database.
                oStandardBar = oCommandBars["Database"];
            }
            
            CommandBarControls controls = oStandardBar.Controls;

            // remove old menus...
            foreach (CommandBarControl control in controls)
            {
                String caption = control.Caption;

                if (caption.EndsWith("Remote Documents") || caption.EndsWith("Remote documents"))
                {                    
                    control.Delete(null);
                }
            }

            // In case the button was not deleted, use the exiting one.
            try
            {                
                eXoMenu = (CommandBarPopup)oStandardBar.Controls["Remote documents"];
                Open = (CommandBarButton)eXoMenu.Controls["Open"];
                Save = (CommandBarButton)eXoMenu.Controls["Save"];
                SaveAs = (CommandBarButton)eXoMenu.Controls["SaveAs"];
                //CompareWithBase = (CommandBarButton)eXoMenu.Controls["Compare with base"];
                Search = (CommandBarButton)eXoMenu.Controls["Search"];
                Settings = (CommandBarButton)eXoMenu.Controls["Settings"];
                About = (CommandBarButton)eXoMenu.Controls["About"];
            

            }

            catch (Exception)
            {
                eXoMenu = (CommandBarPopup)oStandardBar.Controls.Add(MsoControlType.msoControlPopup, omissing, omissing, omissing, true);
                eXoMenu.Caption = "Remote Documents";
                eXoMenu.Tag = eXoMenu.Caption;

                Open = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                Open.Caption = "Open...";
                Open.Tag = Open.Caption;

                Save = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                Save.Caption = "Save";
                Save.Tag = Save.Caption;

                SaveAs = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                SaveAs.Caption = "Save As...";
                SaveAs.Tag = SaveAs.Caption;

                //CompareWithBase = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                //CompareWithBase.Caption = "Compare with base";
                //CompareWithBase.Tag = CompareWithBase.Caption;
                //CompareWithBase.BeginGroup = true;
               
                Search = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                Search.Caption = "Search...";
                Search.Tag = Search.Caption;

                Settings = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                Settings.Caption = "Settings...";
                Settings.Tag = Settings.Caption;

                About = (CommandBarButton)eXoMenu.Controls.Add(1, omissing, omissing, omissing, omissing);
                About.Caption = "About...";
                About.Tag = About.Caption;
            }
            Open.Visible = true;
            Open.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.Open_Click);

            Save.Visible = true;
            //Save.Enabled = false;
            Save.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.Save_Click);

            SaveAs.Visible = true;
            //SaveAs.Enabled = false;
            SaveAs.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.SaveAs_Click);

            //CompareWithBase.Visible = true;
            //CompareWithBase.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.compareWithBase_Click);
            
            Search.Visible = true;
            Search.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.Search_Click);

            Settings.Visible = true;
            Settings.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.Settings_Click);

            About.Visible = true;
            About.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(this.About_Click);


            object oName = applicationObject.GetType().InvokeMember("Name", BindingFlags.GetProperty, null, applicationObject, null);         
            oStandardBar = null;
            oCommandBars = null;

            clearRepository();
        }
        #endregion

        #region OnBeginShutdown(ref System.Array custom)
        public void OnBeginShutdown(ref System.Array custom)
        {
            object omissing = System.Reflection.Missing.Value;

            Open.Delete(omissing);
            Open = null;

            eXoMenu.Delete(omissing);
            eXoMenu = null;

            Search.Delete(omissing);
            Settings.Delete(omissing);
            Save.Delete(omissing);
        }
        #endregion

        public String getCacheFolder()
        {
            Environment.SpecialFolder p = Environment.SpecialFolder.Personal;
            return Environment.GetFolderPath(p) + "\\repository\\";
        }

        private void clearRepository()
        {
            try
            {
                if (!Directory.Exists(getCacheFolder()))
                {
                    return;
                }
                Directory.Delete(getCacheFolder(), true);
            }
            catch (Exception e)
            {
                //MessageBox.Show("Can't remove cache directory!", "Warning", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        public String getWordFileName()
        {
            try
            {
                return app.ActivePresentation.FullName;
               
            }
            catch (Exception exc)
            {
            }
            return "";
        }
   
        private void Open_Click(CommandBarButton cmdBarbutton, ref bool cancel) {
            //MessageBox.Show("FILENAME: [" + Filename + "]");
            DialogOpen = new NOpen(app, this);
            DialogOpen.ShowDialog();
            onDocumentLoad();
        }

        private void Search_Click(CommandBarButton cmdBarbutton, ref bool cancel)
        {
            DialogSearch = new Search(app, this);
            DialogSearch.ShowDialog();
            onDocumentLoad();
        }

        

        private void Save_Click(CommandBarButton cmdBarbutton, ref bool cancel)
        {
            if (app.ActivePresentation.FullName.StartsWith(getCacheFolder()))
            {
                makePut();
                return;
            }

            DialogSave = new NSave(app, this);
            DialogSave.ShowDialog();
            
        }


        private void SaveAs_Click(CommandBarButton cmdBarbutton, ref bool cancel)
        {
            DialogSave = new NSave(app, this);
            DialogSave.ShowDialog();
            //onDocumentLoad();
        }

        private void compareWithBase_Click(CommandBarButton cmdBarbutton, ref bool cancel)
        {
        //{
        //    String filePath = app.ActiveWorkbook.FullName;
        //    if (!filePath.StartsWith(getCacheFolder()))
        //    {
        //        MessageBox.Show("File must be saved!");
        //        return;
        //    }

        //    String fileName = filePath.Substring(getCacheFolder().Length);
        //    fileName = fileName.Replace("\\", "/");
        //    if (!fileName.StartsWith("/"))
        //    {
        //        fileName = "/" + fileName;
        //    }

        //    try
        //    {
        //        ReportCommand report = new ReportCommand(getContext());
        //        report.setResourcePath(fileName);

        //        report.addRequiredProperty(DavProperty.VERSIONNAME);

        //        int status = report.execute();
        //        if (status != DavStatus.MULTISTATUS)
        //        {
        //            MessageBox.Show("CAN GET VERSION LIST WITH ERROR: " + status);
        //            return;
        //        }

        //        Multistatus multistatus = report.getMultistatus();
        //        ArrayList responses = multistatus.getResponses();
        //        if (responses.Count == 0)
        //        {
        //            MessageBox.Show("File must have versions!");
        //            return;
        //        }

        //        DavResponse response = (DavResponse)responses[responses.Count - 1];

        //        String href = response.getHref().getHref();
        //        href = href.Substring(getContext().getContextHref().Length);

        //        GetCommand getCommand = new GetCommand(getContext());
        //        getCommand.setResourcePath(href);
        //        status = getCommand.execute();

        //        if (status != DavStatus.OK)
        //        {
        //            MessageBox.Show("CAN GET FILE WITH STATUS: " + status.ToString());
        //            return;
        //        }

        //        if (href.StartsWith(""))
        //        {
        //            href = href.Substring(1);
        //        }

        //        href = href.Replace("/", "\\");

        //        String versionedFileName = getCacheFolder() + href;

        //        byte[] resp = getCommand.getResponseBody();
        //        if (File.Exists(versionedFileName))
        //        {
        //            File.Delete(versionedFileName);
        //        }
        //        Thread.Sleep(200);

        //        FileStream fs = new FileStream(versionedFileName, FileMode.Create, FileAccess.ReadWrite, FileShare.ReadWrite);
        //        BinaryWriter w = new BinaryWriter(fs);
        //        for (long i = 0; i < resp.Length; i++)
        //        {
        //            w.Write(resp[i]);
        //        }
        //        w.Close();
        //        fs.Close();

        //        object omissing = Missing.Value;
        //        object target = Word.WdCompareTarget.wdCompareTargetCurrent;
        //        app.ActiveWorkbook.Compare(versionedFileName, ref omissing, ref target, ref omissing, ref omissing,
        //                        ref omissing, ref omissing, ref omissing);
        //    }
        //    catch (Exception exc)
        //    {
        //        MessageBox.Show("CAN'T RUN COMPARER!!!! UNHANDLED ERROR!");
        //    }
        }

        private void Settings_Click(CommandBarButton cmdBarbutton, ref bool cancel)
        {
            Settings DialogSettings = new Settings(app);
            DialogSettings.ShowDialog();
            onDocumentLoad();
        }



        private void About_Click(CommandBarButton cmdBarbutton, ref bool cancel) 
        {
            AboutBox AboutBox = new AboutBox(app);
            AboutBox.ShowDialog();
        }

        private void onDocumentLoad()
        {
            if (Filename == "") {
                return;
            }

            String fileName = Filename;
            object omissing = Missing.Value;
            Microsoft.Office.Interop.PowerPoint.Presentation doc = app.Presentations.Open(fileName, MsoTriState.msoFalse, MsoTriState.msoTrue, MsoTriState.msoFalse);

            doc.NewWindow();
            Save.Enabled = true;
            Filename = "";
        }

        private void makePut() {
            this.app.ActivePresentation.Save();
            String fileSystemName = getWordFileName();
            String remoteFileName = fileSystemName.Substring(fileSystemName.IndexOf("\\"+workspace));
            remoteFileName = remoteFileName.Replace("\\", "/");

            FileStream stream = new FileStream(fileSystemName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
            long len = stream.Length;
            byte[] filedata = new byte[len];
            int readed = 0;
            while (readed < len)
            {
                readed += stream.Read(filedata, 0, (int)(len - readed));
            }

            DavContext context = getContext();
            PutCommand put = new PutCommand(context);
            //MessageBox.Show("remoteFileName: " + remoteFileName);
            put.setResourcePath(remoteFileName);
            put.setRequestBody(filedata);
            int status = put.execute();
            if (status != DavStatus.CREATED)
            {
                MessageBox.Show("Can't save file. Status: " + status, "Error",
                 MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else
            {
                MessageBox.Show("File saved successfully!", "Info",
                 MessageBoxButtons.OK, MessageBoxIcon.Information);
            }
        }

        private DavContext createContext(String url)
        {
            RegistryKey soft_key = Registry.CurrentUser.OpenSubKey(RegKeys.SOFTWARE_KEY);
            try
            {

                RegistryKey exo_key = soft_key.OpenSubKey(RegKeys.EXO_KEY);
                RegistryKey client_key = exo_key.OpenSubKey(RegKeys.CLIENT_KEY);

                String _server = client_key.GetValue(RegKeys.S_ADDDR_KEY, "").ToString();
                int _port = System.Convert.ToInt32(client_key.GetValue(RegKeys.S_PORT_KEY, "").ToString());
                String _servlet = client_key.GetValue(RegKeys.S_SERVLET_KEY, "").ToString();
                String _username = client_key.GetValue(RegKeys.USER_KEY, "").ToString();
                String svalue = client_key.GetValue(RegKeys.PASS_KEY, "").ToString();
                this.workspace = client_key.GetValue(RegKeys.WS_KEY, "").ToString();

                byte[] bs_pass = System.Convert.FromBase64String(svalue);
                String _pass = Encoding.UTF8.GetString(bs_pass);
                String servletPath = "";
                String to_find = _server + ":" + _port;



                if (!url.Equals(""))
                {
                    servletPath = url.Substring(url.IndexOf(to_find) + to_find.Length);

                }

                if (servletPath != "")
                {
                    return new DavContext(_server, _port, servletPath, _username, _pass);
                }
                else
                {
                    return new DavContext(_server, _port, _servlet, _username, _pass);
                }

            }
            catch (Exception regexc)
            {
                //MessageBox.Show(regexc.StackTrace);
                return null;

            }
        }


	}
}