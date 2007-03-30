using System;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace exo_jcr.msofficeplugin.ppoint
{
    public partial class NSave : Form
    {
        Microsoft.Office.Interop.PowerPoint._Application app;
        Connect connect;

        public NSave(object app, Connect connect)
        {
            this.app = (Microsoft.Office.Interop.PowerPoint._Application)app;
            this.connect = connect;
            InitializeComponent();
            
            mainControl1.SetConnect(connect);

            box_filename.Text = this.app.ActivePresentation.Name;
            }

        private void btn_cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void btn_save_Click(object sender, EventArgs e)
        {
            mainControl1.saveClick(app , box_filename.Text);
        }
    }
}