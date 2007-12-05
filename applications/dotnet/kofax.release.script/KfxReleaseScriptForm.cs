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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace Exo.KfxReleaseScript
{
    public partial class KfxReleaseScriptForm : Form
    {
        // Reference to the Setup Data structure specified by Kofax
        private ReleaseSetupData setupData;

        //**********************************************************************
        // Constructor. Expects a reference to the Setup Data structure.
        //**********************************************************************
        internal KfxReleaseScriptForm(ReleaseSetupData setupData)
        {
            InitializeComponent();
            this.setupData = setupData;
            PopulateControls();
        }

        //**********************************************************************
        // Populates the controls of the Form based on the Setup Data structure.
        //**********************************************************************
        private void PopulateControls()
        {
            Link documentNameLink = null;

            if (this.setupData.New == -1)
            {
                // The form is displayed for the first time. Default values.
                this.textNodeType.Text       = "kfx:document";
                this.textPassword.Text       = "exo@ecm";
                this.textPath.Text           = "/draft/cms/publications";
                this.textURI.Text            = "http://localhost:8080/ecm/repository";
                this.textUserName.Text       = "exoadmin";
            }
            else
            {
                // Some configuration has already been done.
                // Load values from the Setup Data structure.
                this.textName.Text           = this.setupData.Name;
                this.textPassword.Text       = this.setupData.Password;
                this.textURI.Text            = this.setupData.ConnectString;
                this.textUserName.Text       = this.setupData.UserName;

                // Load values from custom properties
                this.textNodeType.Text = Helper.GetCustomProperty(
                    this.setupData.CustomProperties,
                    Helper.CUSTOM_PROP_DESTINATION_TYPE);
                this.textPath.Text = Helper.GetCustomProperty(
                    this.setupData.CustomProperties,
                    Helper.CUSTOM_PROP_DESTINATION_PATH);

                // Process each configured link
                foreach (Link link in this.setupData.Links)
                {
                    if (link.Destination == Helper.DOCUMENT_NAME_DESTINATION)
                    {
                        // The current link corresponds to the document name.
                        // Cache it.
                        documentNameLink = link;
                    }
                    else
                    {
                        // Standard link.
                        // Populate the link list
                        this.listLinks.Items.Add(new ListLinkItem(link));
                    }
                }
            }

            // Populate batch and document class labels
            this.batchClassLabel.Text = this.setupData.BatchClassName;
            this.documentClassLabel.Text = this.setupData.DocClassName;

            // Update the number of Links label
            this.labelNumLinks.Text = listLinks.Items.Count.ToString();

            // Process each index field
            foreach (IndexField index in this.setupData.IndexFields)
            {
                // Populate the index data list and document name list
                ComboIndexItem item = new ComboIndexItem(
                    index.Name, KfxLinkSourceType.KFX_REL_INDEXFIELD);
                this.comboKofaxIndexData.Items.Add(item);
                this.comboDocumentName.Items.Add(item);

                // Determine if the current index matches the document name
                if (documentNameLink != null
                    && documentNameLink.SourceType == KfxLinkSourceType.KFX_REL_INDEXFIELD
                    && documentNameLink.Source == index.Name)
                {
                    // Select the corresponding item in the document name combo
                    this.comboDocumentName.SelectedItem = item;
                }
            }

            // Process each batch field
            foreach (BatchField batch in this.setupData.BatchFields)
            {
                // Populate the index data list and document name list
                ComboIndexItem item = new ComboIndexItem(
                    batch.Name, KfxLinkSourceType.KFX_REL_BATCHFIELD);
                this.comboKofaxIndexData.Items.Add(item);
                this.comboDocumentName.Items.Add(item);

                // Determine if the current index matches the document name
                if (documentNameLink != null
                    && documentNameLink.SourceType == KfxLinkSourceType.KFX_REL_BATCHFIELD
                    && documentNameLink.Source == batch.Name)
                {
                    // Select the corresponding item in the document name combo
                    this.comboDocumentName.SelectedItem = item;
                }
            }

            // Select the first item in the index data list, if existing.
            if (comboKofaxIndexData.Items.Count > 0)
            {
                comboKofaxIndexData.SelectedIndex = 0;
            }

            // Ensure an item is selected in the document name combo
            if (comboDocumentName.SelectedItem == null
                && comboDocumentName.Items.Count > 0)
            {
                comboDocumentName.SelectedIndex = 0;
            }
        }

        //**********************************************************************
        // Populates the Setup Data based on the controls of the Form
        //**********************************************************************
        private void PopulateSetupData()
        {
            // Store values in the Setup Data structure
            this.setupData.ConnectString = this.textURI.Text;
            this.setupData.Name          = this.textName.Text;
            this.setupData.Password      = this.textPassword.Text;
            this.setupData.UserName      = this.textUserName.Text;

            // Store values in custom properties
            Helper.SetCustomProperty(
                this.setupData.CustomProperties,
                Helper.CUSTOM_PROP_DESTINATION_TYPE,
                this.textNodeType.Text);
            Helper.SetCustomProperty(
                this.setupData.CustomProperties,
                Helper.CUSTOM_PROP_DESTINATION_PATH,
                this.textPath.Text);

            // Reset links in the Setup Data structure
            this.setupData.Links.RemoveAll();

            // Process items in the index data list
            foreach (ListLinkItem item in this.listLinks.Items)
            {
                this.setupData.Links.Add(item.SourceName,
                                         item.SourceType,
                                         item.Destination);
            }

            // Process the document name
            ComboIndexItem documentItem = (ComboIndexItem)
                this.comboDocumentName.SelectedItem;
            if (documentItem != null)
            {
                this.setupData.Links.Add(documentItem.Name,
                                         documentItem.Type,
                                         Helper.DOCUMENT_NAME_DESTINATION);
            }
        }

        //**********************************************************************
        // Invoked when the user clicks the OK button
        //**********************************************************************
        private void buttonOK_Click(object sender, EventArgs e)
        {
            // Update the Setup Data structure
            PopulateSetupData();
        }

        //**********************************************************************
        // Creates an entry in the Mapping list
        //**********************************************************************
        private void buttonAddMapping_Click(object sender, EventArgs e)
        {
            ComboIndexItem indexData = (ComboIndexItem)
                comboKofaxIndexData.SelectedItem;
            String jcrPropertyName = textJCRPropName.Text;

            // Ensures the destination is not empty
            if (jcrPropertyName == "")
            {
                return;
            }

            // Ensures the destination has not been mapped yet
            if (isJCRPropertyMapped(jcrPropertyName))
            {
                MessageBox.Show("The JCR property has already been mapped.",
                                "Adding a link",
                                MessageBoxButtons.OK,
                                MessageBoxIcon.Warning);
                return;
            }

            listLinks.Items.Add(new ListLinkItem(indexData.Name,
                                                 indexData.Type,
                                                 jcrPropertyName));

            // Update the number of Links label
            this.labelNumLinks.Text = listLinks.Items.Count.ToString();
        }

        //**********************************************************************
        // Removes an entry from the Mapping list
        //**********************************************************************
        private void buttonRemoveMapping_Click(object sender, EventArgs e)
        {
            object item = listLinks.SelectedItem;

            if (item != null)
            {
                listLinks.Items.Remove(item);
            }

            // Update the number of Links label
            this.labelNumLinks.Text = listLinks.Items.Count.ToString();
        }

        //**********************************************************************
        // Indicates whether a JCR Property has been mapped
        //**********************************************************************
        private Boolean isJCRPropertyMapped(string jcrPropertyName)
        {
            foreach (ListLinkItem item in listLinks.Items)
            {
                if (item.Destination == jcrPropertyName)
                {
                    return true;
                }
            }

            return false;
        }
    }
}