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
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;

namespace Exo.KfxReleaseScript
{
    /// <summary>
    /// Definition of the interface implemented by the Release Setup Script.
    /// Author: Brice Revenant
    /// </summary>
    [GuidAttribute("e14599ba-971c-11db-96c2-005056c00008")]
    public interface IKfxReleaseSetupScript
    {
        KfxReturnValue ActionEvent(KfxActionValue action,
                                   string str1,
                                   string str2);
        KfxReturnValue CloseScript();
        KfxReturnValue OpenScript();
        KfxReturnValue RunUI();
        ReleaseSetupData SetupData { get; set; }
    }

    /// <summary>
    /// Implementation of the Release Setup Script.
    /// Author: Brice Revenant
    /// </summary>
    [GuidAttribute("f7b055ee-971c-11db-96c2-005056c00008")]
    [ClassInterface(ClassInterfaceType.None)]
    public class KfxReleaseSetupScript : IKfxReleaseSetupScript
    {
        // ReleaseSetupData object is set by the Release
        // Setup Controller.  This object is used during
        // the document type setup process. It will contain
        // all of the information and interfaces you need to
        // define a document type's release process.
        private ReleaseSetupData setupData;

        //**********************************************************************
        // Invoked when Kofax needs to inform the Setup Script of a change
        //**********************************************************************
        public KfxReturnValue ActionEvent(KfxActionValue action,
                                          string str1,
                                          string str2)
        {
            switch (action)
            {
                // There has been a change in the Fields
                case KfxActionValue.KFX_REL_INDEXFIELD_DELETE:
                case KfxActionValue.KFX_REL_INDEXFIELD_INSERT:
                case KfxActionValue.KFX_REL_INDEXFIELD_RENAME:
                case KfxActionValue.KFX_REL_BATCHFIELD_DELETE:
                case KfxActionValue.KFX_REL_BATCHFIELD_INSERT:
                case KfxActionValue.KFX_REL_BATCHFIELD_RENAME:
                    MessageBox.Show("The following field has changed: "
                                    + str1
                                    + ".\nYou must "
                                    + "update the eXo Release Script configuration "
                                    + "to take into account this change.",
                                    "eXo Release Script");
                    return KfxReturnValue.KFX_REL_SUCCESS;
            }
            
            return KfxReturnValue.KFX_REL_SUCCESS;
        }

        //**********************************************************************
        // Cleanup
        //**********************************************************************
        public KfxReturnValue CloseScript()
        {
            // Prevents a bug from occuring when Ascent Capture is closed
            GC.Collect();
            GC.WaitForPendingFinalizers();

            return KfxReturnValue.KFX_REL_SUCCESS;
        }

        //**********************************************************************
        // Shows a message box when an exception occured
        //**********************************************************************
        private void DisplayException(Exception e)
        {
            MessageBox.Show("An error occured. Please look into the error log : "
                            + e.Message);
        }

        //**********************************************************************
        // Appends the specified exception in the Kofax error log
        //**********************************************************************
        private void LogException(ReleaseSetupData setupData,
                                 Exception e)
        {
            if (setupData != null)
            {
                // Use the Kofax logging handler
                setupData.LogError(0,
                                   0,
                                   0,
                                   e.ToString(),
                                   e.Source,
                                   0);
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
        // Displays the configuration panel
        //**********************************************************************
        public KfxReturnValue RunUI()
        {
            try
            {
                // If the OK button is clicked
                if (new KfxReleaseScriptForm(this.setupData).ShowDialog() ==
                    DialogResult.OK)
                {
                    // Instructs Kofax to process the current Setup Data
                    this.setupData.Apply();
                }

                return KfxReturnValue.KFX_REL_SUCCESS;
            }
            catch (Exception e)
            {
                LogException(this.setupData, e);
                DisplayException(e);
                return KfxReturnValue.KFX_REL_ERROR;
            }
        }

        //**********************************************************************
        // Setup Data accessor
        //**********************************************************************
        public ReleaseSetupData SetupData
        {
            get
            {
                return this.setupData;
            }

            set
            {
                this.setupData = value;
            }
        }
   }
}