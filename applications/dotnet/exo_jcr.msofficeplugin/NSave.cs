using System;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace exo_jcr.msofficeplugin
{
    public partial class NSave : Form
    {
        Word._Application app;
        Connect connect;

        public NSave(object app, Connect connect)
        {
            this.app = (Word._Application)app;
            this.connect = connect;
            InitializeComponent();
            
            mainControl1.SetConnect(connect);

            String fileName = this.app.ActiveDocument.Name;

            box_filename.Text = fileName;
            this.box_filetype.SelectedIndex = 0;
        }

        private void btn_cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_save_Click(object sender, EventArgs e)
        {
            int fileTpe = FileType.DOCUMENT;

            String contentType = MainControl.MIMETYPE_DOC;
            String selectedType = box_filetype.Text;
            if (selectedType == "Word Document") {
                contentType = MainControl.MIMETYPE_DOC;
            } 
            else if (selectedType == "Word Template")
            {
                contentType = MainControl.MIMETYPE_DOT;
            }
            else if (selectedType == "Text File")
            {
                contentType = MainControl.MIMETYPE_TXT;
            } 
            else if (selectedType == "HTML File") {
                contentType = MainControl.MIMETYPE_HTML;
            }
            
            mainControl1.saveClick(app , box_filename.Text, contentType);
        }

    }
}