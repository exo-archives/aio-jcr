JCR Storage Plugin for MS Office
********************************


1. Introduction

eXoWebDavClient is a library written in C#, that allows to use WebDAV service from any other application. 
It is notably leveraged in an MS Office plug-in, which is available in Word, Excel and Power Point. This
plugin comes with its own user interface.

The plugin allows to:
 - Load .doc, .xls, .ppt files from JCR repository into MS Office; 
 - Edit and Save files onto repository; 
 - Make full-text repository search; 
 - View properties list; 
 - View and Edit some versions of current file and compare it (Word only) with its base version (current state). 


2. Installation

First of all, you need to have Microsoft Office 2003 Word and Microsoft .NET framework installed on your system. 
If you do not have Microsoft .NET framework runtime environment already installed, the installation procedure plugin
will ask you to do so and point to an URL where you can get it. 

Also, an important condition is to have installed Microsoft Hot fix Office2003-kb907417 ENU, 
which allows to use external libraries into MS Word. This update is available in the SVN or in the 
Microsoft web site at: http://support.microsoft.com/kb/907417. Please, install also extensibilityMSM.msi update from this hotfix pack.

When these conditions are reached, you can install eXoClient application by running eXoWebDavOfficePlugin.msi or setup.exe 
(if your system does not support MSI installer).

When the installation succesfully ends, a new menu column called "Remote Documents" appears in the applications you selected during the
installation.


3. Configuration

Before opening any documents, you need to configure your connection information. You can do this using a 
"Settings" dialog window. Use a "Test connection" button to make sure you have entered the correct settings. 
Please note that the "Proxy" subsettings currently don't have any effect on your connection.

Please, be attentive when you enter the username and password. They can vary, depending on 
your server settings. Default login and pass for :
- eXo JCR standalone based server are admin/admin,
- eXo ECM based server are exoadmin/exo@ecm.
Similarly, the port numbers vary on the application server :
- 8080 for Tomcat and JBoss,
- 9000 for JOnAS.


4. Usage

When configuration is done, you can already start browsing your documents. Click on "Open" in the "Remote Documents" 
menu. A dialog appears, from which you can browse the repository using left the nodes tree. To open any file in the
right list, click twice on it or select the file then press the "Open" button.

If you have opened a file from the repository and want to save any changes, you can use both - "Save" and "Save As..." buttons. 
The difference between them is that "Save" puts the document at the same place whereas "Save As.." allows you to save it into a
different folder.

To browse the document versions history, use the "Versions" button. This one is only active when you have selected a versionable file. 
You can also make a full-text search into repository. Press the "Search" menu action, put keywords to search into the text 
box and press "Search" button. Results will appear into the file list. 


5. Uninstallation

To uninstall the plugin, open the Windows Control Panel and select "Add or Remove Programs". Then find the item called
"eXo WebDav MSOffice Plugin" and click on Remove.

Note: Microsoft Word stores the state of all his menu bars into normal.dot. So, in some cases, after uninstalling plugin, you can 
see a "Remote Documents" bar still appearing into Word's menu. So, you need to rewrite (or simply delete, Word will create new)
your normal.dot.


Thank you for using eXo products !