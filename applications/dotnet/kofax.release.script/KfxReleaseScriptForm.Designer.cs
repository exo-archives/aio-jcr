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

namespace Exo.KfxReleaseScript
{
    partial class KfxReleaseScriptForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(KfxReleaseScriptForm));
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.textName = new System.Windows.Forms.TextBox();
            this.batchClassLabel = new System.Windows.Forms.Label();
            this.documentClassLabel = new System.Windows.Forms.Label();
            this.tabControl1 = new System.Windows.Forms.TabControl();
            this.tabConnection = new System.Windows.Forms.TabPage();
            this.textPassword = new System.Windows.Forms.TextBox();
            this.textUserName = new System.Windows.Forms.TextBox();
            this.textURI = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.tabDestination = new System.Windows.Forms.TabPage();
            this.label12 = new System.Windows.Forms.Label();
            this.comboDocumentName = new System.Windows.Forms.ComboBox();
            this.textNodeType = new System.Windows.Forms.TextBox();
            this.label8 = new System.Windows.Forms.Label();
            this.textPath = new System.Windows.Forms.TextBox();
            this.label7 = new System.Windows.Forms.Label();
            this.tabMapping = new System.Windows.Forms.TabPage();
            this.label13 = new System.Windows.Forms.Label();
            this.labelNumLinks = new System.Windows.Forms.Label();
            this.label11 = new System.Windows.Forms.Label();
            this.comboKofaxIndexData = new System.Windows.Forms.ComboBox();
            this.buttonRemoveMapping = new System.Windows.Forms.Button();
            this.listLinks = new System.Windows.Forms.ListBox();
            this.buttonAddMapping = new System.Windows.Forms.Button();
            this.textJCRPropName = new System.Windows.Forms.TextBox();
            this.label10 = new System.Windows.Forms.Label();
            this.label9 = new System.Windows.Forms.Label();
            this.buttonOK = new System.Windows.Forms.Button();
            this.buttonCancel = new System.Windows.Forms.Button();
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.tabControl1.SuspendLayout();
            this.tabConnection.SuspendLayout();
            this.tabDestination.SuspendLayout();
            this.tabMapping.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 9);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(65, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Batch class:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 33);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(86, 13);
            this.label2.TabIndex = 1;
            this.label2.Text = "Document class:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 57);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(38, 13);
            this.label3.TabIndex = 2;
            this.label3.Text = "Name:";
            // 
            // textName
            // 
            this.textName.Location = new System.Drawing.Point(118, 54);
            this.textName.Name = "textName";
            this.textName.Size = new System.Drawing.Size(318, 20);
            this.textName.TabIndex = 3;
            // 
            // batchClassLabel
            // 
            this.batchClassLabel.AutoSize = true;
            this.batchClassLabel.Location = new System.Drawing.Point(115, 9);
            this.batchClassLabel.Name = "batchClassLabel";
            this.batchClassLabel.Size = new System.Drawing.Size(0, 13);
            this.batchClassLabel.TabIndex = 4;
            // 
            // documentClassLabel
            // 
            this.documentClassLabel.AutoSize = true;
            this.documentClassLabel.Location = new System.Drawing.Point(115, 33);
            this.documentClassLabel.Name = "documentClassLabel";
            this.documentClassLabel.Size = new System.Drawing.Size(0, 13);
            this.documentClassLabel.TabIndex = 5;
            // 
            // tabControl1
            // 
            this.tabControl1.Controls.Add(this.tabConnection);
            this.tabControl1.Controls.Add(this.tabDestination);
            this.tabControl1.Controls.Add(this.tabMapping);
            this.tabControl1.Location = new System.Drawing.Point(15, 80);
            this.tabControl1.Name = "tabControl1";
            this.tabControl1.SelectedIndex = 0;
            this.tabControl1.Size = new System.Drawing.Size(425, 347);
            this.tabControl1.TabIndex = 6;
            // 
            // tabConnection
            // 
            this.tabConnection.Controls.Add(this.textPassword);
            this.tabConnection.Controls.Add(this.textUserName);
            this.tabConnection.Controls.Add(this.textURI);
            this.tabConnection.Controls.Add(this.label6);
            this.tabConnection.Controls.Add(this.label5);
            this.tabConnection.Controls.Add(this.label4);
            this.tabConnection.Location = new System.Drawing.Point(4, 22);
            this.tabConnection.Name = "tabConnection";
            this.tabConnection.Padding = new System.Windows.Forms.Padding(3);
            this.tabConnection.Size = new System.Drawing.Size(417, 321);
            this.tabConnection.TabIndex = 0;
            this.tabConnection.Text = "Connection";
            this.tabConnection.UseVisualStyleBackColor = true;
            // 
            // textPassword
            // 
            this.textPassword.Location = new System.Drawing.Point(99, 84);
            this.textPassword.Name = "textPassword";
            this.textPassword.PasswordChar = '*';
            this.textPassword.Size = new System.Drawing.Size(312, 20);
            this.textPassword.TabIndex = 5;
            // 
            // textUserName
            // 
            this.textUserName.Location = new System.Drawing.Point(99, 58);
            this.textUserName.Name = "textUserName";
            this.textUserName.Size = new System.Drawing.Size(312, 20);
            this.textUserName.TabIndex = 4;
            // 
            // textURI
            // 
            this.textURI.Location = new System.Drawing.Point(99, 32);
            this.textURI.Name = "textURI";
            this.textURI.Size = new System.Drawing.Size(312, 20);
            this.textURI.TabIndex = 3;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(6, 87);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(56, 13);
            this.label6.TabIndex = 2;
            this.label6.Text = "Password:";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(6, 61);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(61, 13);
            this.label5.TabIndex = 1;
            this.label5.Text = "User name:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(6, 35);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(77, 13);
            this.label4.TabIndex = 0;
            this.label4.Text = "WebDAV URI:";
            // 
            // tabDestination
            // 
            this.tabDestination.Controls.Add(this.label12);
            this.tabDestination.Controls.Add(this.comboDocumentName);
            this.tabDestination.Controls.Add(this.textNodeType);
            this.tabDestination.Controls.Add(this.label8);
            this.tabDestination.Controls.Add(this.textPath);
            this.tabDestination.Controls.Add(this.label7);
            this.tabDestination.Location = new System.Drawing.Point(4, 22);
            this.tabDestination.Name = "tabDestination";
            this.tabDestination.Padding = new System.Windows.Forms.Padding(3);
            this.tabDestination.Size = new System.Drawing.Size(417, 321);
            this.tabDestination.TabIndex = 1;
            this.tabDestination.Text = "Destination";
            this.tabDestination.UseVisualStyleBackColor = true;
            // 
            // label12
            // 
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(6, 87);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(88, 13);
            this.label12.TabIndex = 11;
            this.label12.Text = "Document name:";
            // 
            // comboDocumentName
            // 
            this.comboDocumentName.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboDocumentName.FormattingEnabled = true;
            this.comboDocumentName.Location = new System.Drawing.Point(100, 84);
            this.comboDocumentName.Name = "comboDocumentName";
            this.comboDocumentName.Size = new System.Drawing.Size(312, 21);
            this.comboDocumentName.TabIndex = 10;
            // 
            // textNodeType
            // 
            this.textNodeType.Location = new System.Drawing.Point(99, 58);
            this.textNodeType.Name = "textNodeType";
            this.textNodeType.Size = new System.Drawing.Size(312, 20);
            this.textNodeType.TabIndex = 3;
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(6, 61);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(59, 13);
            this.label8.TabIndex = 2;
            this.label8.Text = "Node type:";
            // 
            // textPath
            // 
            this.textPath.Location = new System.Drawing.Point(99, 32);
            this.textPath.Name = "textPath";
            this.textPath.Size = new System.Drawing.Size(312, 20);
            this.textPath.TabIndex = 1;
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(6, 35);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(32, 13);
            this.label7.TabIndex = 0;
            this.label7.Text = "Path:";
            // 
            // tabMapping
            // 
            this.tabMapping.Controls.Add(this.labelNumLinks);
            this.tabMapping.Controls.Add(this.label11);
            this.tabMapping.Controls.Add(this.comboKofaxIndexData);
            this.tabMapping.Controls.Add(this.buttonRemoveMapping);
            this.tabMapping.Controls.Add(this.listLinks);
            this.tabMapping.Controls.Add(this.buttonAddMapping);
            this.tabMapping.Controls.Add(this.textJCRPropName);
            this.tabMapping.Controls.Add(this.label10);
            this.tabMapping.Controls.Add(this.label9);
            this.tabMapping.Controls.Add(this.label13);
            this.tabMapping.Location = new System.Drawing.Point(4, 22);
            this.tabMapping.Name = "tabMapping";
            this.tabMapping.Size = new System.Drawing.Size(417, 321);
            this.tabMapping.TabIndex = 2;
            this.tabMapping.Text = "Mapping";
            this.tabMapping.UseVisualStyleBackColor = true;
            // 
            // label13
            // 
            this.label13.AutoSize = true;
            this.label13.Location = new System.Drawing.Point(96, 298);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(90, 13);
            this.label13.TabIndex = 12;
            this.label13.Text = "Number of Links :";
            // 
            // labelNumLinks
            // 
            this.labelNumLinks.AutoSize = true;
            this.labelNumLinks.Location = new System.Drawing.Point(183, 298);
            this.labelNumLinks.Name = "labelNumLinks";
            this.labelNumLinks.Size = new System.Drawing.Size(13, 13);
            this.labelNumLinks.TabIndex = 11;
            this.labelNumLinks.Text = "0";
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(3, 117);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(35, 13);
            this.label11.TabIndex = 10;
            this.label11.Text = "Links:";
            // 
            // comboKofaxIndexData
            // 
            this.comboKofaxIndexData.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboKofaxIndexData.FormattingEnabled = true;
            this.comboKofaxIndexData.Location = new System.Drawing.Point(99, 32);
            this.comboKofaxIndexData.Name = "comboKofaxIndexData";
            this.comboKofaxIndexData.Size = new System.Drawing.Size(312, 21);
            this.comboKofaxIndexData.TabIndex = 9;
            // 
            // buttonRemoveMapping
            // 
            this.buttonRemoveMapping.Location = new System.Drawing.Point(337, 292);
            this.buttonRemoveMapping.Name = "buttonRemoveMapping";
            this.buttonRemoveMapping.Size = new System.Drawing.Size(75, 24);
            this.buttonRemoveMapping.TabIndex = 8;
            this.buttonRemoveMapping.Text = "Remove";
            this.buttonRemoveMapping.UseVisualStyleBackColor = true;
            this.buttonRemoveMapping.Click += new System.EventHandler(this.buttonRemoveMapping_Click);
            // 
            // listLinks
            // 
            this.listLinks.FormattingEnabled = true;
            this.listLinks.Location = new System.Drawing.Point(99, 117);
            this.listLinks.Name = "listLinks";
            this.listLinks.Size = new System.Drawing.Size(312, 173);
            this.listLinks.TabIndex = 7;
            // 
            // buttonAddMapping
            // 
            this.buttonAddMapping.Location = new System.Drawing.Point(336, 85);
            this.buttonAddMapping.Name = "buttonAddMapping";
            this.buttonAddMapping.Size = new System.Drawing.Size(75, 24);
            this.buttonAddMapping.TabIndex = 6;
            this.buttonAddMapping.Text = "Add";
            this.buttonAddMapping.UseVisualStyleBackColor = true;
            this.buttonAddMapping.Click += new System.EventHandler(this.buttonAddMapping_Click);
            // 
            // textJCRPropName
            // 
            this.textJCRPropName.Location = new System.Drawing.Point(99, 59);
            this.textJCRPropName.Name = "textJCRPropName";
            this.textJCRPropName.Size = new System.Drawing.Size(312, 20);
            this.textJCRPropName.TabIndex = 5;
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(3, 62);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(71, 13);
            this.label10.TabIndex = 3;
            this.label10.Text = "JCR property:";
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.Location = new System.Drawing.Point(3, 35);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(60, 13);
            this.label9.TabIndex = 2;
            this.label9.Text = "Index data:";
            // 
            // buttonOK
            // 
            this.buttonOK.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.buttonOK.Location = new System.Drawing.Point(365, 435);
            this.buttonOK.Name = "buttonOK";
            this.buttonOK.Size = new System.Drawing.Size(75, 24);
            this.buttonOK.TabIndex = 7;
            this.buttonOK.Text = "OK";
            this.buttonOK.UseVisualStyleBackColor = true;
            this.buttonOK.Click += new System.EventHandler(this.buttonOK_Click);
            // 
            // buttonCancel
            // 
            this.buttonCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.buttonCancel.Location = new System.Drawing.Point(284, 435);
            this.buttonCancel.Name = "buttonCancel";
            this.buttonCancel.Size = new System.Drawing.Size(75, 24);
            this.buttonCancel.TabIndex = 8;
            this.buttonCancel.Text = "Cancel";
            this.buttonCancel.UseVisualStyleBackColor = true;
            // 
            // pictureBox1
            // 
            this.pictureBox1.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox1.Image")));
            this.pictureBox1.Location = new System.Drawing.Point(15, 429);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(100, 37);
            this.pictureBox1.TabIndex = 9;
            this.pictureBox1.TabStop = false;
            // 
            // KfxReleaseScriptForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(452, 471);
            this.Controls.Add(this.pictureBox1);
            this.Controls.Add(this.buttonCancel);
            this.Controls.Add(this.buttonOK);
            this.Controls.Add(this.tabControl1);
            this.Controls.Add(this.documentClassLabel);
            this.Controls.Add(this.batchClassLabel);
            this.Controls.Add(this.textName);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "KfxReleaseScriptForm";
            this.ShowIcon = false;
            this.Text = "eXo Release Setup";
            this.tabControl1.ResumeLayout(false);
            this.tabConnection.ResumeLayout(false);
            this.tabConnection.PerformLayout();
            this.tabDestination.ResumeLayout(false);
            this.tabDestination.PerformLayout();
            this.tabMapping.ResumeLayout(false);
            this.tabMapping.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox textName;
        private System.Windows.Forms.Label batchClassLabel;
        private System.Windows.Forms.Label documentClassLabel;
        private System.Windows.Forms.TabControl tabControl1;
        private System.Windows.Forms.TabPage tabConnection;
        private System.Windows.Forms.TabPage tabDestination;
        private System.Windows.Forms.Button buttonOK;
        private System.Windows.Forms.Button buttonCancel;
        private System.Windows.Forms.PictureBox pictureBox1;
        private System.Windows.Forms.TabPage tabMapping;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox textUserName;
        private System.Windows.Forms.TextBox textURI;
        private System.Windows.Forms.TextBox textPassword;
        private System.Windows.Forms.TextBox textPath;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.TextBox textNodeType;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.Button buttonRemoveMapping;
        private System.Windows.Forms.ListBox listLinks;
        private System.Windows.Forms.Button buttonAddMapping;
        private System.Windows.Forms.TextBox textJCRPropName;
        private System.Windows.Forms.ComboBox comboKofaxIndexData;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.ComboBox comboDocumentName;
        private System.Windows.Forms.Label labelNumLinks;
        private System.Windows.Forms.Label label13;
    }
}