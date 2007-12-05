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

namespace exo_jcr.msofficeplugin.common
{
    partial class Search
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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Search));
            this.box_search = new System.Windows.Forms.TextBox();
            this.btn_search = new System.Windows.Forms.Button();
            this.file_list = new System.Windows.Forms.ListView();
            this.cFileName = new System.Windows.Forms.ColumnHeader();
            this.cPath = new System.Windows.Forms.ColumnHeader();
            this.cLastModified = new System.Windows.Forms.ColumnHeader();
            this.cSize = new System.Windows.Forms.ColumnHeader();
            this.imageList1 = new System.Windows.Forms.ImageList(this.components);
            this.label2 = new System.Windows.Forms.Label();
            this.btn_open = new System.Windows.Forms.Button();
            this.btn_cancel = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // box_search
            // 
            this.box_search.Location = new System.Drawing.Point(89, 11);
            this.box_search.Name = "box_search";
            this.box_search.Size = new System.Drawing.Size(561, 20);
            this.box_search.TabIndex = 0;
            this.box_search.TextChanged += new System.EventHandler(this.textEntered);
            // 
            // btn_search
            // 
            this.btn_search.Enabled = false;
            this.btn_search.Location = new System.Drawing.Point(681, 9);
            this.btn_search.Name = "btn_search";
            this.btn_search.Size = new System.Drawing.Size(75, 23);
            this.btn_search.TabIndex = 1;
            this.btn_search.Text = "Search";
            this.btn_search.UseVisualStyleBackColor = true;
            this.btn_search.Click += new System.EventHandler(this.btn_search_Click);
            // 
            // file_list
            // 
            this.file_list.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.cFileName,
            this.cPath,
            this.cSize,
            this.cLastModified});
            this.file_list.Location = new System.Drawing.Point(15, 43);
            this.file_list.Name = "file_list";
            this.file_list.Size = new System.Drawing.Size(741, 321);
            this.file_list.SmallImageList = this.imageList1;
            this.file_list.TabIndex = 3;
            this.file_list.UseCompatibleStateImageBehavior = false;
            this.file_list.View = System.Windows.Forms.View.Details;
            this.file_list.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.file_list_MouseDoubleClick);
            // 
            // cFileName
            // 
            this.cFileName.Text = "File Name";
            this.cFileName.Width = 180;
            // 
            // cPath
            // 
            this.cPath.Text = "Path";
            this.cPath.Width = 300;
            // 
            // cLastModified
            // 
            this.cLastModified.Text = "Last Modified";
            this.cLastModified.Width = 160;
            // 
            // cSize
            // 
            this.cSize.Text = "Size";
            this.cSize.Width = 70;
            // 
            // imageList1
            // 
            this.imageList1.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imageList1.ImageStream")));
            this.imageList1.TransparentColor = System.Drawing.Color.Transparent;
            this.imageList1.Images.SetKeyName(0, "folder.png");
            this.imageList1.Images.SetKeyName(1, "x-office-document.png");
            this.imageList1.Images.SetKeyName(2, "Folder-Closed_Blue.ico");
            this.imageList1.Images.SetKeyName(3, "Blank.ico");
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 14);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(71, 13);
            this.label2.TabIndex = 6;
            this.label2.Text = "Search String";
            // 
            // btn_open
            // 
            this.btn_open.Enabled = false;
            this.btn_open.Location = new System.Drawing.Point(550, 371);
            this.btn_open.Name = "btn_open";
            this.btn_open.Size = new System.Drawing.Size(100, 23);
            this.btn_open.TabIndex = 7;
            this.btn_open.Text = "Open";
            this.btn_open.UseVisualStyleBackColor = true;
            this.btn_open.Click += new System.EventHandler(this.btn_open_Click);
            // 
            // btn_cancel
            // 
            this.btn_cancel.Location = new System.Drawing.Point(656, 371);
            this.btn_cancel.Name = "btn_cancel";
            this.btn_cancel.Size = new System.Drawing.Size(100, 23);
            this.btn_cancel.TabIndex = 8;
            this.btn_cancel.Text = "Cancel";
            this.btn_cancel.UseVisualStyleBackColor = true;
            this.btn_cancel.Click += new System.EventHandler(this.btn_cancel_Click_1);
            // 
            // Search
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(768, 405);
            this.Controls.Add(this.btn_cancel);
            this.Controls.Add(this.btn_open);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.file_list);
            this.Controls.Add(this.btn_search);
            this.Controls.Add(this.box_search);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.Name = "Search";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Search";
            this.Load += new System.EventHandler(this.Search_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox box_search;
        private System.Windows.Forms.Button btn_search;
        private System.Windows.Forms.ListView file_list;
        private System.Windows.Forms.ColumnHeader cFileName;
        private System.Windows.Forms.ColumnHeader cPath;
        private System.Windows.Forms.ColumnHeader cLastModified;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.ImageList imageList1;
        private System.Windows.Forms.ColumnHeader cSize;
        private System.Windows.Forms.Button btn_open;
        private System.Windows.Forms.Button btn_cancel;

    }
}