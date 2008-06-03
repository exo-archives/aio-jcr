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
    partial class Settings
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
            this.btn_TestConn = new System.Windows.Forms.Button();
            this.btn_Save = new System.Windows.Forms.Button();
            this.box_Server = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.box_Servlet = new System.Windows.Forms.TextBox();
            this.servlet_lbl = new System.Windows.Forms.Label();
            this.box_Port = new System.Windows.Forms.TextBox();
            this.port_lbl = new System.Windows.Forms.Label();
            this.server_lbl = new System.Windows.Forms.Label();
            this.btn_Cancel = new System.Windows.Forms.Button();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.label3 = new System.Windows.Forms.Label();
            this.box_workspace = new System.Windows.Forms.TextBox();
            this.box_Password = new System.Windows.Forms.MaskedTextBox();
            this.password_lbl = new System.Windows.Forms.Label();
            this.box_Username = new System.Windows.Forms.TextBox();
            this.user_lbl = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.box_repository = new System.Windows.Forms.TextBox();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.SuspendLayout();
            // 
            // btn_TestConn
            // 
            this.btn_TestConn.Location = new System.Drawing.Point(12, 279);
            this.btn_TestConn.Name = "btn_TestConn";
            this.btn_TestConn.Size = new System.Drawing.Size(105, 23);
            this.btn_TestConn.TabIndex = 0;
            this.btn_TestConn.Text = "Test Connection";
            this.btn_TestConn.UseVisualStyleBackColor = true;
            this.btn_TestConn.Click += new System.EventHandler(this.btn_TestConn_Click);
            // 
            // btn_Save
            // 
            this.btn_Save.Location = new System.Drawing.Point(244, 279);
            this.btn_Save.Name = "btn_Save";
            this.btn_Save.Size = new System.Drawing.Size(92, 23);
            this.btn_Save.TabIndex = 1;
            this.btn_Save.Text = "Save";
            this.btn_Save.UseVisualStyleBackColor = true;
            this.btn_Save.Click += new System.EventHandler(this.btn_Save_Click);
            // 
            // box_Server
            // 
            this.box_Server.Location = new System.Drawing.Point(10, 34);
            this.box_Server.Name = "box_Server";
            this.box_Server.Size = new System.Drawing.Size(227, 20);
            this.box_Server.TabIndex = 4;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(54, 25);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(35, 13);
            this.label1.TabIndex = 6;
            this.label1.Text = "label1";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(54, 71);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(35, 13);
            this.label2.TabIndex = 7;
            this.label2.Text = "label2";
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.box_Servlet);
            this.groupBox1.Controls.Add(this.servlet_lbl);
            this.groupBox1.Controls.Add(this.box_Port);
            this.groupBox1.Controls.Add(this.port_lbl);
            this.groupBox1.Controls.Add(this.server_lbl);
            this.groupBox1.Controls.Add(this.box_Server);
            this.groupBox1.Location = new System.Drawing.Point(12, 25);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(246, 233);
            this.groupBox1.TabIndex = 10;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Connection";
            // 
            // box_Servlet
            // 
            this.box_Servlet.Location = new System.Drawing.Point(9, 136);
            this.box_Servlet.Name = "box_Servlet";
            this.box_Servlet.Size = new System.Drawing.Size(228, 20);
            this.box_Servlet.TabIndex = 9;
            // 
            // servlet_lbl
            // 
            this.servlet_lbl.AutoSize = true;
            this.servlet_lbl.Location = new System.Drawing.Point(7, 120);
            this.servlet_lbl.Name = "servlet_lbl";
            this.servlet_lbl.Size = new System.Drawing.Size(68, 13);
            this.servlet_lbl.TabIndex = 8;
            this.servlet_lbl.Text = "Servlet path*";
            // 
            // box_Port
            // 
            this.box_Port.Location = new System.Drawing.Point(9, 81);
            this.box_Port.MaxLength = 5;
            this.box_Port.Name = "box_Port";
            this.box_Port.Size = new System.Drawing.Size(44, 20);
            this.box_Port.TabIndex = 7;
            // 
            // port_lbl
            // 
            this.port_lbl.AutoSize = true;
            this.port_lbl.Location = new System.Drawing.Point(7, 64);
            this.port_lbl.Name = "port_lbl";
            this.port_lbl.Size = new System.Drawing.Size(30, 13);
            this.port_lbl.TabIndex = 6;
            this.port_lbl.Text = "Port*";
            // 
            // server_lbl
            // 
            this.server_lbl.AutoSize = true;
            this.server_lbl.Location = new System.Drawing.Point(7, 17);
            this.server_lbl.Name = "server_lbl";
            this.server_lbl.Size = new System.Drawing.Size(42, 13);
            this.server_lbl.TabIndex = 5;
            this.server_lbl.Text = "Server*";
            // 
            // btn_Cancel
            // 
            this.btn_Cancel.Location = new System.Drawing.Point(342, 279);
            this.btn_Cancel.Name = "btn_Cancel";
            this.btn_Cancel.Size = new System.Drawing.Size(92, 23);
            this.btn_Cancel.TabIndex = 11;
            this.btn_Cancel.Text = "Cancel";
            this.btn_Cancel.UseVisualStyleBackColor = true;
            this.btn_Cancel.Click += new System.EventHandler(this.btn_Cancel_Click);
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.box_repository);
            this.groupBox2.Controls.Add(this.label4);
            this.groupBox2.Controls.Add(this.label3);
            this.groupBox2.Controls.Add(this.box_workspace);
            this.groupBox2.Controls.Add(this.box_Password);
            this.groupBox2.Controls.Add(this.password_lbl);
            this.groupBox2.Controls.Add(this.box_Username);
            this.groupBox2.Controls.Add(this.user_lbl);
            this.groupBox2.Location = new System.Drawing.Point(276, 25);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(158, 233);
            this.groupBox2.TabIndex = 12;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Login";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(9, 167);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(66, 13);
            this.label3.TabIndex = 5;
            this.label3.Text = "Workspace*";
            // 
            // box_workspace
            // 
            this.box_workspace.Location = new System.Drawing.Point(12, 184);
            this.box_workspace.Name = "box_workspace";
            this.box_workspace.Size = new System.Drawing.Size(133, 20);
            this.box_workspace.TabIndex = 4;
            // 
            // box_Password
            // 
            this.box_Password.Location = new System.Drawing.Point(12, 81);
            this.box_Password.Name = "box_Password";
            this.box_Password.Size = new System.Drawing.Size(133, 20);
            this.box_Password.TabIndex = 3;
            this.box_Password.UseSystemPasswordChar = true;
            // 
            // password_lbl
            // 
            this.password_lbl.AutoSize = true;
            this.password_lbl.Location = new System.Drawing.Point(9, 64);
            this.password_lbl.Name = "password_lbl";
            this.password_lbl.Size = new System.Drawing.Size(57, 13);
            this.password_lbl.TabIndex = 2;
            this.password_lbl.Text = "Password*";
            // 
            // box_Username
            // 
            this.box_Username.Location = new System.Drawing.Point(12, 34);
            this.box_Username.Name = "box_Username";
            this.box_Username.Size = new System.Drawing.Size(133, 20);
            this.box_Username.TabIndex = 1;
            // 
            // user_lbl
            // 
            this.user_lbl.AutoSize = true;
            this.user_lbl.Location = new System.Drawing.Point(9, 18);
            this.user_lbl.Name = "user_lbl";
            this.user_lbl.Size = new System.Drawing.Size(59, 13);
            this.user_lbl.TabIndex = 0;
            this.user_lbl.Text = "Username*";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(9, 117);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(61, 13);
            this.label4.TabIndex = 6;
            this.label4.Text = "Repository*";
            // 
            // box_repository
            // 
            this.box_repository.Location = new System.Drawing.Point(12, 133);
            this.box_repository.Name = "box_repository";
            this.box_repository.Size = new System.Drawing.Size(133, 20);
            this.box_repository.TabIndex = 7;
            // 
            // Settings
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(447, 314);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.btn_Cancel);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btn_Save);
            this.Controls.Add(this.btn_TestConn);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "Settings";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Settings";
            this.Load += new System.EventHandler(this.Settings_Load);
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btn_TestConn;
        private System.Windows.Forms.Button btn_Save;
        private System.Windows.Forms.TextBox box_Server;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Label server_lbl;
        private System.Windows.Forms.TextBox box_Servlet;
        private System.Windows.Forms.Label servlet_lbl;
        private System.Windows.Forms.TextBox box_Port;
        private System.Windows.Forms.Label port_lbl;
        private System.Windows.Forms.Button btn_Cancel;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Label user_lbl;
        private System.Windows.Forms.MaskedTextBox box_Password;
        private System.Windows.Forms.Label password_lbl;
        private System.Windows.Forms.TextBox box_Username;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox box_workspace;
        private System.Windows.Forms.TextBox box_repository;
        private System.Windows.Forms.Label label4;
    }
}