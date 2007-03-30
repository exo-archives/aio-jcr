/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/
/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * 15.11.2006  
 */



		eXoDavBrowser it is the graphic client for WebDav.

	For compilation of the program it is necessary eXoDavBrowser to execute in a directory in a command line mvn clean install or start a batch file bin/eXoDavBrowser-build.bat.
	Password for key store (key\Key_for_eXoDavBrowser): 123123
	For start of the application it is necessary to start a batch file bin/eXoDavBrowser-run.bat. 

Functionality of the eXoDavBrowser:
-	Creation of folder (File a Create Folder);
-	Recording of folder File a Write Folder);
-	Recording of file (File a Write file);
-	Save a file  (File a Get file); 
-	Blocking of the file/folder for changes (File a Exclusive Lock);
-	Unblocking of the file/folder for changes(if the file/folder had been blocking  at this start of the program ) (File a UnLock);
-	Copying of file/folder (Edit a Copy);
-	Inserting of file/folder  (Edit a Paste);
-	Cut of file/folder (Edit a Cut);
-	Delete of file/folder (Edit a Delete);
-	Addition of the control  of version in the file/folder (Version a Put Under Version Control);
-	For change of contents version the file/folder are necessary it is necessary  execute	 CheckOut   (Version a Check Out);
-	For preservation in a history of change the contents version file/folder  it is necessary of execute CheckIn (Version a Check In);
-	For a cancellation  of changes of contents version file/folder  it is necessary of execute UnCheckOut (Version a UnCheck Out);
-	For viewing versions of the file/folder in is necessary of execute (Version a	Version Report).
