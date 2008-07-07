using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace exo_jcr.msofficeplugin.common
{
    public partial class NCreate : Form
    {
        public NCreate()
        {
            InitializeComponent();
        }

        public String folderName = "";

        private void btnOk_Click(object sender, EventArgs e)
        {
            if(tbFolderName.Text == ""){
                MessageBox.Show("Enter a valid folder name!", "Error");
            }
            else if(!Utils.checkNodeNameValid(tbFolderName.Text)){
                MessageBox.Show("Entered name doesn't match the pattern", "Error");
            }
            else if (Utils.checkNodeNameValid(tbFolderName.Text))
            {
                folderName = tbFolderName.Text;
                this.Close();
            }
            else{
                MessageBox.Show("Some unknown mistake happened!" + 
                    "\\10\\13 [" + tbFolderName.Text + "]", "Error");
            }
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}