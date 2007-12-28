eXo WebDav Client Library
----------------------------


1. Intro

eXoWebDavClient is a  library written on C# ,  that allows to use WebDav  service from  any other application.


2. Library description 


The main parts of this library are:

·	Commands  - set of classes that’s implements all standard WebDav commands;
·	DavProperties – set of properties required for some commands, such as PropFind, Report e.t.c
·	Response – server response processor;
·	DavContext – configuration and user-management interface;


Supported DAV properties:

-	Checked In;
-	Checked Out;
-	Content Length;
-	Content Type;
-	Creator Display Name;
-     Creation Date;
-	Display Name;
-	Last Modified;
-	Lock Discovery;
-	Resource Type;
-	Supported Lock;
-	Supported Method;
-	Supported Query Grammar Set;
-	Version Name;
      and others, see DavProperty.cs for full list.


Supported Search methods:

-	XPath Search;
-	SQL Search;



Currently implemented a next list of commands:
	
PropFind, Report commands – multistatus commands, which requires a XML-request and normally returns 207 status and XML-response;

Get, Put commands – you must using a setRequestBody and getResponseBody methods
for transmitting (receiving) contents of the file from/to server. Returns 200(GET) or 201(PUT) when success;

Copy, Move commands – simple commands that’s only returns 201 status when succeed.
Requires a Resource Path and Destination Path into request parameters;

MkCol – creates a new folder. Returns 201 when succeed;

Delete command – return 204 (No content) status when succeed. Requires a Resource Path to be set;

Version Control, Check In, Check Out, UnCheckOut commands – using for enabling and operating with versionable files and folders.  Must returns 200 or 201 (for Check-In command) status;

Lock, Unlock commands – using for locking and unlocking folders at server.
Lock command returns a 200 status with the Lock Token key, which must be used for 
unlocking folder back (UnLock must returns 204 );

Search Command – currently implemented SQL Search and XPath Search methods.
This is a multistatus type commands;


ProPatch command;



