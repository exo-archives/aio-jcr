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

using Kofax.ReleaseLib;
using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient.DavProperties;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Response;
using System;
using System.Collections;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;

namespace Exo.KfxReleaseScript
{
    /// <summary>
    /// Definition of the interface implemented by the Release Script.
    /// Author: Brice Revenant
    /// </summary>
    [GuidAttribute("bf8f3c40-96c2-11db-96c1-005056c00008")]
    public interface IKfxReleaseScript
    {
        KfxReturnValue CloseScript();
        ReleaseData    DocumentData { get; set; }
        KfxReturnValue OpenScript();
        KfxReturnValue ReleaseDoc();
    }

    /// <summary>
    /// Implementation of the Release Script.
    /// Author: Brice Revenant
    /// </summary>
    [GuidAttribute("d175a3f2-96c2-11db-96c1-005056c00008")]
    [ClassInterface(ClassInterfaceType.None)]
    public class KfxReleaseScript : IKfxReleaseScript
    {
        // ReleaseData object is set by the release controller.
        // This object is to be used during the document release
        // process as it will contain the document data and the
        // external data source information defined during the
        // setup process.
        private ReleaseData data;

        // Caches the WebDAV context
        private DavContext davContext;

        // Caches the document path
        private String documentPath;

        //**********************************************************************
        // Cleanup
        //**********************************************************************
        public KfxReturnValue CloseScript()
        {
            // Prevents a bug from occuring
            GC.Collect();
            GC.WaitForPendingFinalizers();

            return KfxReturnValue.KFX_REL_SUCCESS;
        }

        //**********************************************************************
        // Data accessor
        //**********************************************************************
        public ReleaseData DocumentData
        {
            get
            {
                return this.data;
            }
            set
            {
                this.data = value;
            }
        }

        //**********************************************************************
        // Retrieves the WebDAV context
        //**********************************************************************
        private DavContext GetDavContext()
        {
            if (this.davContext == null)
            {
                Uri uri = new Uri (this.data.ConnectString);

                this.davContext = new DavContext(
                    uri.Host,
                    uri.Port,
                    uri.AbsolutePath,
                    this.data.UserName,
                    this.data.Password
                    );
            }

            return this.davContext;
        }

        //**********************************************************************
        // Determines the path of the JCR document to be created
        //**********************************************************************
        private string GetDocumentPath()
        {
            if (this.documentPath == null)
            {
                String documentName = null;

                // Retrieve the value containing the document name
                foreach (Value value in this.data.Values)
                {
                    if (value.Destination == Helper.DOCUMENT_NAME_DESTINATION)
                    {
                        documentName = value.Value;
                    }
                }

                // Provide a default value if no document name has been found
                if (documentName == null)
                {
                    documentName = this.data.UniqueDocumentID.ToString();
                }

                // Compute the document path with the
                // destination folder and document name
                this.documentPath = Helper.GetCustomProperty(
                    this.data.CustomProperties,
                    Helper.CUSTOM_PROP_DESTINATION_PATH)
                    + "/"
                    + documentName;
            }

            return this.documentPath;
        }

        //**********************************************************************
        // Appends the specified exception in the Kofax error log
        //**********************************************************************
        private void LogException(ReleaseData data,
                                  Exception e)
        {
            if (data != null)
            {
                // Use the Kofax logging handler
                data.LogError(
                    0,
                    0,
                    0,
                    "Processing batch : " + data.BatchName + ". " + e.ToString(),
                    e.Source,
                    0
                    );
            }
        }

        //**********************************************************************
        // Initialization
        //**********************************************************************
        public KfxReturnValue OpenScript()
        {
            return KfxReturnValue.KFX_REL_SUCCESS;
        }

        //**********************************************************************
        // Creates the Node corresponding to the released document
        //**********************************************************************
        private void ProcessDocumentNodeCreation()
        {
            // Display the status
            SendMessage("Creating document node in the JCR...");

            // Create the folder
            MkColCommand mkCol = new MkColCommand(GetDavContext());
            mkCol.setResourcePath(GetDocumentPath());
            mkCol.setNodeType(Helper.GetCustomProperty(
                this.data.CustomProperties,
                Helper.CUSTOM_PROP_DESTINATION_TYPE));

            // Determine if the WebDAV command worked successfully
            if(mkCol.execute() != DavStatus.CREATED)
            {
                throw new Exception(
                    "MKCOL returned a wrong status : "
                    + mkCol.getStatus()
                    + " "
                    + Helper.WebDAVStatusToString(mkCol.getStatus()));
            }
        }

        //**********************************************************************
        // Uploads the images
        //**********************************************************************
        private void ProcessPdf()
        {
            // Apparently, Ascent Capture does not allow to retrieve
            // a list of PDFs as it does with images and text files.
            // Ensure a PDF file needs to be released.
            if (data.KofaxPDFFileName != null)
            {
                // Display the status
                SendMessage("Uploading PDF file " + data.KofaxPDFFileName + "...");

                // Upload the file
                UploadFile(data.KofaxPDFFileName, GetDocumentPath());
            }
        }

        //**********************************************************************
        // Add JCR Properties to the document Node
        //**********************************************************************
        private void ProcessProperties()
        {
            // Display the status
            SendMessage("Creating properties...");

            // Configure the WebDAV command
            PropPatchCommand propPatchCommand =
                new PropPatchCommand(GetDavContext());
            propPatchCommand.setResourcePath(GetDocumentPath());

            // Process each values specified to the script
            foreach (Value value in this.data.Values)
            {
                // Ensure the current value does correspond to the document name
                if (value.Destination != Helper.DOCUMENT_NAME_DESTINATION)
                {
                    propPatchCommand.setProperty(value.Destination,
                                                 value.Value);
                }
            }

            // Ensure the response is a multistatus one
            if (propPatchCommand.execute() != DavStatus.MULTISTATUS)
            {
                throw new Exception(
                    "PROPPATCH did not return a MULTISTATUS status : "
                    + propPatchCommand.getStatus()
                    + " "
                    + Helper.WebDAVStatusToString(propPatchCommand.getStatus()));
            }

            // Ensure each returned status is correct
            foreach(DavResponse response in propPatchCommand.
                                            getMultistatus().
                                            getResponses())
            {
                foreach (WebDavProperty property in response.getProperties())
                {
                    if (property.getStatus() != DavStatus.OK)
                    {
                        throw new Exception(
                            "At least one property was not successfully set : "
                                + property.getPropertyName()
                                + " : "
                                + property.getStatus()
                                + " "
                                + Helper.WebDAVStatusToString(property.getStatus()));
                    }
                }
            }
        }

        //**********************************************************************
        // Uploads the images
        //**********************************************************************
        private void ProcessTif()
        {
            // Process each image in the Release Data
            foreach (ImageFile image in this.data.ImageFiles)
            {
                // Display the status
                SendMessage("Uploading image file " + image.FileName + "...");

                // Upload the file
                UploadFile(image.FileName, GetDocumentPath());
            }
        }

        //**********************************************************************
        // Uploads the text
        //**********************************************************************
        private void ProcessTxt()
        {
            // Process each text file in the Release data
            foreach (TextFile text in this.data.TextFiles)
            {
                // Display the status
                SendMessage("Uploading text file " + text.FileName + "...");

                // Upload the file
                UploadFile(text.FileName, GetDocumentPath());
            }
        }

        //**********************************************************************
        // Actually stores the document in the repository
        //**********************************************************************
        public KfxReturnValue ReleaseDoc()
        {
            try
            {
                // This method may be called many times in the same
                // script instance hence we need to reset some fields.
                this.documentPath = null;

                // Execute each successive step of the release process
                ProcessDocumentNodeCreation();
                ProcessProperties();
                ProcessTif();
                ProcessTxt();
                ProcessPdf();

                // If we arrive here, then everything has worked fine
                return KfxReturnValue.KFX_REL_SUCCESS;
            }
            catch (Exception e)
            {
                LogException(this.data, e);
                SendError("The following error occured : " + e.Message);
                return KfxReturnValue.KFX_REL_ERROR;
            }
        }

        //**********************************************************************
        // Display a message in Kofax
        //**********************************************************************
        private void SendMessage(String message)
        {
            this.data.SendMessage(message,
                                  0,
                                  KfxInfoReturnValue.KFX_REL_DOC_MESSAGE);
        }

        //**********************************************************************
        // Display an error message in Kofax
        //**********************************************************************
        private void SendError(String message)
        {
            this.data.SendMessage(message,
                                  1,
                                  KfxInfoReturnValue.KFX_REL_DOC_ERROR);
        }

        //**********************************************************************
        // Uploads the specified local file to the JCR
        //**********************************************************************
        private void UploadFile(String filePath,
                                String destinationPath)
        {
            // Read the content of the file
            FileStream inputStream = new FileStream(filePath, FileMode.Open);
            int fileSize = (int)inputStream.Length;
            byte[] bytes = new byte[fileSize];
            inputStream.Read(bytes, 0, fileSize);
            inputStream.Close();

            // Get the actual file name
            String fileName = new FileInfo(filePath).Name;

            // Upload the file
            PutCommand put = new PutCommand(GetDavContext());
            put.setResourcePath(destinationPath
                                + '/'
                                + fileName);
            put.setRequestBody(bytes);

            // Determine if the WebDAV command worked successfully
            if (put.execute() != DavStatus.CREATED)
            {
                throw new Exception(
                    "PUT returned a wrong status when processing \""
                    + filePath
                    + "\" : "
                    + put.getStatus()
                    + " "
                    + Helper.WebDAVStatusToString(put.getStatus()));
            }
        }
    }
}
