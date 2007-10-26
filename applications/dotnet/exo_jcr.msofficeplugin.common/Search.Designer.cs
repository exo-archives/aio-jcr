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
            this.File_Name = new System.Windows.Forms.ColumnHeader();
            this.path = new System.Windows.Forms.ColumnHeader();
            this.lastmodidied = new System.Windows.Forms.ColumnHeader();
            this.columnHeader1 = new System.Windows.Forms.ColumnHeader();
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
            this.File_Name,
            this.path,
            this.lastmodidied,
            this.columnHeader1});
            this.file_list.Location = new System.Drawing.Point(15, 43);
            this.file_list.Name = "file_list";
            this.file_list.Size = new System.Drawing.Size(741, 321);
            this.file_list.SmallImageList = this.imageList1;
            this.file_list.TabIndex = 3;
            this.file_list.UseCompatibleStateImageBehavior = false;
            this.file_list.View = System.Windows.Forms.View.Details;
            this.file_list.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.file_list_MouseDoubleClick);
            // 
            // File_Name
            // 
            this.File_Name.Text = "File Name";
            this.File_Name.Width = 173;
            // 
            // path
            // 
            this.path.Text = "Path";
            this.path.Width = 169;
            // 
            // lastmodidied
            // 
            this.lastmodidied.Text = "Last Modified";
            this.lastmodidied.Width = 118;
            // 
            // columnHeader1
            // 
            this.columnHeader1.Text = "Size";
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
        private System.Windows.Forms.ColumnHeader File_Name;
        private System.Windows.Forms.ColumnHeader path;
        private System.Windows.Forms.ColumnHeader lastmodidied;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.ImageList imageList1;
        private System.Windows.Forms.ColumnHeader columnHeader1;
        private System.Windows.Forms.Button btn_open;
        private System.Windows.Forms.Button btn_cancel;

    }
}