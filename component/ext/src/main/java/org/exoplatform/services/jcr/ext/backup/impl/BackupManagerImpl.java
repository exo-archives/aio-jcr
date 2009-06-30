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
package org.exoplatform.services.jcr.ext.backup.impl;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.config.WorkspaceInitializerEntry;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupJobListener;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.jcr.ext.backup.JobEntryInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SysViewWorkspaceInitializer;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.storage.JCRInvalidItemStateException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SAS .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class BackupManagerImpl implements BackupManager, Startable {

  protected static Log               log                            = ExoLogger.getLogger("ext.BackupManagerImpl");

  private final static String        DEFAULT_INCREMENTAL_JOB_PERIOD = "default-incremental-job-period";

  private final static String        BACKUP_PROPERTIES              = "backup-properties";

  private final static String        FULL_BACKUP_TYPE               = "full-backup-type";

  private final static String        INCREMENTAL_BACKUP_TYPE        = "incremental-backup-type";

  private final static String        BACKUP_DIR                     = "backup-dir";

  private static final int           MESSAGES_MAXSIZE               = 5;

  private static final String        SERVICE_NAME                   = "BackupManager";

  private static final long          AUTO_STOPPER_TIMEOUT           = 5000;

  private long                       defaultIncrementalJobPeriod;

  private String                     defIncrPeriod;

  private String                     backupDir;

  private String                     fullBackupType;

  private String                     incrementalBackupType;

  private final Set<BackupChain>     currentBackups;

  /**
   * The list of restore job.
   */
  private List<JobWorkspaceRestore>  restoreJobs;

  private InitParams                 initParams;

  private File                       logsDirectory;

  private final RepositoryService    repoService;

  private final RegistryService      registryService;

  private FileCleaner                fileCleaner;

  private BackupScheduler            scheduler;

  private final BackupMessagesLog    messages;

  private final MessagesListener     messagesListener;

  private final AutoStopper          stopper;

  class MessagesListener implements BackupJobListener {

    public void onError(BackupJob job, String message, Throwable error) {
      messages.addError(makeJobInfo(job, error) + message, error);
      log.error(makeJobInfo(job, error) + message, error);
    }

    public void onStateChanged(BackupJob job) {
      messages.addMessage(makeJobInfo(job, null));
      if (log.isDebugEnabled())
        log.debug(makeJobInfo(job, null));
    }

    private String makeJobInfo(BackupJob job, Throwable error) {
      String jobInfo = "";

      if (job != null) {

        switch (job.getType()) {
        case BackupJob.FULL:
          jobInfo += "FULL BACKUP";
          break;
        case BackupJob.INCREMENTAL:
          jobInfo += "INCREMENTAL BACKUP";
          break;
        }

        jobInfo += " [";
        switch (job.getState()) {
        case BackupJob.FINISHED:
          jobInfo += "FINISHED";
          break;
        case BackupJob.STARTING:
          jobInfo += "STARTING";
          break;
        case BackupJob.WAITING:
          jobInfo += "WAITING";
          break;
        case BackupJob.WORKING:
          jobInfo += "WORKING";
          break;
        }

        jobInfo += "]";

        if (error != null)
          jobInfo += " Error: " + error.getMessage();

        try {
          jobInfo += " log: " + job.getStorageURL().getPath();
        } catch (BackupOperationException e) {
          // skip URL
        } finally {
          jobInfo += " ";
        }
      }

      return jobInfo;
    }

    public String printStackTrace(Throwable error) {

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(out);
      error.printStackTrace(writer);

      return new String(out.toByteArray());
    }
  }

  class LogsFilter implements FileFilter {

    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".xml");
    }
  }

  class TaskFilter implements FileFilter {

    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".task");
    }
  }

  class AutoStopper extends Thread {

    /**
     * {@inheritDoc}
     */
    public void run() {
      while (true) {
        try {
          Thread.sleep(AUTO_STOPPER_TIMEOUT);

          Iterator<BackupChain> it = currentBackups.iterator();
          List<BackupChain> stopedList = new ArrayList<BackupChain>();

          while (it.hasNext()) {
            BackupChain chain = it.next();
            boolean isFinished = (chain.getBackupJobs().get(0).getState() == BackupJob.FINISHED);

            for (int i = 1; i < chain.getBackupJobs().size(); i++)
              isFinished &= (chain.getBackupJobs().get(i).getState() == BackupJob.FINISHED);

            if (isFinished) {
              stopedList.add(chain);
            }
          }

          // STOP backups
          for (BackupChain chain : stopedList)
            stopBackup(chain);
        } catch (InterruptedException e) {
          log.error("The interapted this thread.", e);
        } catch (Throwable e) {
          log.error("The unknown error", e);
        }
      }
    }
  }

  public BackupManagerImpl(InitParams initParams, RepositoryService repoService) {
    this(initParams, repoService, null);
  }

  public BackupManagerImpl(InitParams initParams,
                           RepositoryService repoService,
                           RegistryService registryService) {

    this.messagesListener = new MessagesListener();
    this.repoService = repoService;
    this.registryService = registryService;
    this.initParams = initParams;

    currentBackups = Collections.synchronizedSet(new HashSet<BackupChain>());

    fileCleaner = new FileCleaner(10000);

    messages = new BackupMessagesLog(MESSAGES_MAXSIZE);

    scheduler = new BackupScheduler(this, messages);

    this.restoreJobs = new ArrayList<JobWorkspaceRestore>();
    this.stopper = new AutoStopper();
    this.stopper.start();
  }

  public Set<BackupChain> getCurrentBackups() {
    return currentBackups;
  }

  public BackupMessage[] getMessages() {
    return messages.getMessages();
  }

  public BackupChainLog[] getBackupsLogs() {
    File[] cfs = logsDirectory.listFiles(new LogsFilter());
    List<BackupChainLog> logs = new ArrayList<BackupChainLog>();
    for (int i = 0; i < cfs.length; i++) {
      File cf = cfs[i];

      try {
        if (!isCurrentBackup(cf))
          logs.add(new BackupChainLog(cf));
      } catch (BackupOperationException e) {
        log.warn("Log file " + cf.getAbsolutePath() + " is bussy or corrupted. Skipped. " + e, e);
      }
    }
    BackupChainLog[] ls = new BackupChainLog[logs.size()];
    logs.toArray(ls);
    return ls;
  }

  /**
   * isCurrentBackup.
   * 
   * @param log
   *          File, the log to backup
   * @return boolean return the 'true' if this log is current backup.
   */
  private boolean isCurrentBackup(File log) {
    for (BackupChain chain : currentBackups)
      if (log.getName().equals(new File(chain.getLogFilePath()).getName()))
        return true;

    return false;
  }

  @Deprecated
  public void restore(BackupChainLog log, String repositoryName, WorkspaceEntry workspaceEntry) throws BackupOperationException,
                                                                                                RepositoryException,
                                                                                                RepositoryConfigurationException,
                                                                                                BackupConfigurationException {

    List<JobEntryInfo> list = log.getJobEntryInfos();
    BackupConfig config = log.getBackupConfig();

    String reposytoryName = (repositoryName == null ? config.getRepository() : repositoryName);
    String workspaseName = workspaceEntry.getName();

    // ws should be registered not created
    if (!workspaceAlreadyExist(reposytoryName, workspaseName)) {

      for (int i = 0; i < list.size(); i++) {
        if (i == 0) {
          try {
            fullRestore(list.get(i).getURL().getPath(),
                        reposytoryName,
                        workspaseName,
                        workspaceEntry);
          } catch (FileNotFoundException e) {
            throw new BackupOperationException("Restore of full backup file error " + e, e);
          } catch (IOException e) {
            throw new BackupOperationException("Restore of full backup file I/O error " + e, e);
          }
        } else {
          // TODO do we not restoring same content few times, i.e. on STARTING, WAITIG, FINISHED
          // events which are logged in chan log one after another
          try {
            incrementalRestore(list.get(i).getURL().getPath(), reposytoryName, workspaseName);
          } catch (FileNotFoundException e) {
            throw new BackupOperationException("Restore of incremental backup file error " + e, e);
          } catch (IOException e) {
            throw new BackupOperationException("Restore of incremental backup file I/O error " + e,
                                               e);
          } catch (ClassNotFoundException e) {
            throw new BackupOperationException("Restore of incremental backup error " + e, e);
          }
        }
      }
    } else
      throw new BackupConfigurationException("Workspace should exists " + workspaseName);
  }

  protected void restoreOverInitializer(BackupChainLog log, String repositoryName, WorkspaceEntry workspaceEntry) throws BackupOperationException,
                                                                                               RepositoryException,
                                                                                               RepositoryConfigurationException,
                                                                                               BackupConfigurationException {

    List<JobEntryInfo> list = log.getJobEntryInfos();
    BackupConfig config = log.getBackupConfig();

    String reposytoryName = (repositoryName == null ? config.getRepository() : repositoryName);
    String workspaseName = workspaceEntry.getName();

    // ws should be registered not created
    if (!workspaceAlreadyExist(reposytoryName, workspaseName)) {

      for (int i = 0; i < list.size(); i++) {
        if (i == 0) {
          try {
            fullRestoreOverInitializer(list.get(i).getURL().getPath(),
                        reposytoryName,
                        workspaceEntry);
          } catch (FileNotFoundException e) {
            throw new BackupOperationException("Restore of full backup file error " + e, e);
          } catch (IOException e) {
            throw new BackupOperationException("Restore of full backup file I/O error " + e, e);
          }
        } else {
          // TODO do we not restoring same content few times, i.e. on STARTING, WAITIG, FINISHED
          // events which are logged in chan log one after another
          try {
            incrementalRestore(list.get(i).getURL().getPath(), reposytoryName, workspaseName);
          } catch (FileNotFoundException e) {
            throw new BackupOperationException("Restore of incremental backup file error " + e, e);
          } catch (IOException e) {
            throw new BackupOperationException("Restore of incremental backup file I/O error " + e,
                                               e);
          } catch (ClassNotFoundException e) {
            throw new BackupOperationException("Restore of incremental backup error " + e, e);
          }
        }
      }
    } else
      throw new BackupConfigurationException("Workspace should exists " + workspaseName);
  }

  private boolean workspaceAlreadyExist(String repository, String workspace) throws RepositoryException,
                                                                            RepositoryConfigurationException {
    String[] ws = repoService.getRepository(repository).getWorkspaceNames();

    for (int i = 0; i < ws.length; i++)
      if (ws[i].equals(workspace))
        return true;
    return false;
  }

  public BackupChain startBackup(BackupConfig config) throws BackupOperationException,
                                                     BackupConfigurationException,
                                                     RepositoryException,
                                                     RepositoryConfigurationException {

    return startBackup(config, null);
  }

  /**
   * Internally used for call with job listener from scheduler.
   * 
   * @param config
   * @param jobListener
   * @return
   * @throws BackupOperationException
   * @throws BackupConfigurationException
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  BackupChain startBackup(BackupConfig config, BackupJobListener jobListener) throws BackupOperationException,
                                                                             BackupConfigurationException,
                                                                             RepositoryException,
                                                                             RepositoryConfigurationException {
    
    if (config.getIncrementalJobPeriod() < 0)
      throw new BackupConfigurationException("The parameter 'incremental job period' can not be negative.");
    
    if (config.getIncrementalJobNumber() < 0)
      throw new BackupConfigurationException("The parameter 'incremental job number' can not be negative.");
    

    if (config.getIncrementalJobPeriod() == 0
        && config.getBackupType() == BackupManager.FULL_AND_INCREMENTAL)
      config.setIncrementalJobPeriod(defaultIncrementalJobPeriod);

    BackupChain bchain = new BackupChainImpl(config,
                                             logsDirectory,
                                             repoService.getRepository(config.getRepository()),
                                             fullBackupType,
                                             incrementalBackupType,
                                             IdGenerator.generate());

    bchain.addListener(messagesListener);
    bchain.addListener(jobListener);

    currentBackups.add(bchain);
    bchain.startBackup();

    return bchain;
  }

  public void stopBackup(BackupChain backup) {
    backup.stopBackup();
    currentBackups.remove(backup);
  }

  public void start() {
    // start all scheduled before tasks

    if (registryService != null && !registryService.getForceXMLConfigurationValue(initParams)) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try {
        readParamsFromRegistryService(sessionProvider);
      } catch (Exception e) {
        readParamsFromFile();
        try {
          writeParamsToRegistryService(sessionProvider);
        } catch (Exception exc) {
          log.error("Cannot write init configuration to RegistryService.", exc);
        }
      } finally {
        sessionProvider.close();
      }
    } else {
      readParamsFromFile();
    }

    // scan for task files
    File[] tasks = this.logsDirectory.listFiles(new TaskFilter());
    for (File task : tasks) {
      try {
        scheduler.restore(task);
      } catch (BackupSchedulerException e) {
        log.error("Can't restore backup scheduler task from file " + task.getAbsolutePath(), e);
      } catch (BackupOperationException e) {
        log.error("Can't restore backup scheduler task from file " + task.getAbsolutePath(), e);
      } catch (BackupConfigurationException e) {
        log.error("Can't restore backup scheduler task from file " + task.getAbsolutePath(), e);
      } catch (RepositoryException e) {
        log.error("Can't restore backup scheduler task from file " + task.getAbsolutePath(), e);
      } catch (RepositoryConfigurationException e) {
        log.error("Can't restore backup scheduler task from file " + task.getAbsolutePath(), e);
      }
    }
  }

  public void stop() {
    // 1. stop current backup chains
    // for (Iterator iterator = currentBackups.iterator(); iterator.hasNext();) {
    // BackupChain bc = (BackupChain) iterator.next();
    // stopBackup(bc);
    // }
    //    
    // // 2. stop all scheduled tasks
    // scheduler = null;
  }

  @Deprecated
  private void fullRestore(String pathBackupFile,
                           String repositoryName,
                           String workspaceName,
                           WorkspaceEntry workspaceEntry) throws FileNotFoundException,
                                                         IOException,
                                                         RepositoryException,
                                                         RepositoryConfigurationException {

    RepositoryImpl defRep = (RepositoryImpl) repoService.getRepository(repositoryName);

    defRep.importWorkspace(workspaceEntry.getName(), new FileInputStream(pathBackupFile));
  }
  
  private void fullRestoreOverInitializer(String pathBackupFile,
                           String repositoryName,
                           WorkspaceEntry workspaceEntry) throws FileNotFoundException,
                                                         IOException,
                                                         RepositoryException,
                                                         RepositoryConfigurationException {

    RepositoryImpl defRep = (RepositoryImpl) repoService.getRepository(repositoryName);
    
    // set the initializer SysViewWorkspaceInitializer
    WorkspaceInitializerEntry wiEntry = new WorkspaceInitializerEntry();
    wiEntry.setType(SysViewWorkspaceInitializer.class.getCanonicalName());
    
    List<SimpleParameterEntry> wieParams = new ArrayList<SimpleParameterEntry>();
    wieParams.add(new SimpleParameterEntry(SysViewWorkspaceInitializer.RESTORE_PATH_PARAMETER, pathBackupFile));
    
    wiEntry.setParameters(wieParams);
    
    workspaceEntry.setInitializer(wiEntry);

    //restore
    defRep.configWorkspace(workspaceEntry);
    defRep.createWorkspace(workspaceEntry.getName());
  }

  private void incrementalRestore(String pathBackupFile, String repositoryName, String workspaceName) throws RepositoryException,
                                                                                                     RepositoryConfigurationException,
                                                                                                     BackupOperationException,
                                                                                                     FileNotFoundException,
                                                                                                     IOException,
                                                                                                     ClassNotFoundException {
    SessionImpl sesion = (SessionImpl) repoService.getRepository(repositoryName)
                                                  .getSystemSession(workspaceName);
    WorkspacePersistentDataManager dataManager = (WorkspacePersistentDataManager) sesion.getContainer()
                                                                                        .getComponentInstanceOfType(WorkspacePersistentDataManager.class);

    ObjectInputStream ois = null;
    File backupFile = null;
    try {
      backupFile = new File(pathBackupFile);
      ois = new ObjectInputStream(new FileInputStream(backupFile));

      while (true) {
        TransactionChangesLog changesLog = readExternal(ois);

        ChangesLogIterator cli = changesLog.getLogIterator();
        while (cli.hasNextLog()) {
          if (cli.nextLog().getEventType() == ExtendedEvent.LOCK)
            cli.removeLog();
        }

        saveChangesLog(dataManager, changesLog);
      }
    } catch (EOFException ioe) {
      // ok - reading all data from backup file;
    } finally {
      if (sesion != null)
        sesion.logout();
    }
  }

  private void saveChangesLog(WorkspacePersistentDataManager dataManager,
                              TransactionChangesLog changesLog) throws RepositoryException,
                                                               BackupOperationException {
    try {
      dataManager.save(changesLog);
    } catch (JCRInvalidItemStateException e) {
      TransactionChangesLog normalizeChangesLog = getNormalizedChangesLog(e.getIdentifier(),
                                                                          e.getState(),
                                                                          changesLog);
      if (normalizeChangesLog != null)
        saveChangesLog(dataManager, normalizeChangesLog);
      else
        throw new BackupOperationException("Collisions found during save of restore changes log, but caused item is not found by ID "
                                               + e.getIdentifier() + ". " + e,
                                           e);
    }
  }

  private TransactionChangesLog getNormalizedChangesLog(String collisionID,
                                                        int state,
                                                        TransactionChangesLog changesLog) {
    ItemState citem = changesLog.getItemState(collisionID);

    if (citem != null) {

      TransactionChangesLog result = new TransactionChangesLog();
      result.setSystemId(changesLog.getSystemId());

      ChangesLogIterator cli = changesLog.getLogIterator();
      while (cli.hasNextLog()) {
        ArrayList<ItemState> normalized = new ArrayList<ItemState>();
        PlainChangesLog next = cli.nextLog();
        for (ItemState change : next.getAllStates()) {
          if (state == change.getState()) {
            ItemData item = change.getData();
            // targeted state
            if (citem.isNode()) {
              // Node... by ID and desc path
              if (!item.getIdentifier().equals(collisionID)
                  && !item.getQPath().isDescendantOf(citem.getData().getQPath()))
                normalized.add(change);
            } else if (!item.getIdentifier().equals(collisionID)) {
              // Property... by ID
              normalized.add(change);
            }
          } else
            // another state
            normalized.add(change);
        }

        PlainChangesLog plog = new PlainChangesLogImpl(normalized,
                                                       next.getSessionId(),
                                                       next.getEventType());
        result.addLog(plog);
      }

      return result;
    }

    return null;
  }

  private TransactionChangesLog readExternal(ObjectInputStream in) throws IOException,
                                                                  ClassNotFoundException {
    int changesLogType = in.readInt();

    TransactionChangesLog transactionChangesLog = null;

    if (changesLogType == PendingChangesLog.Type.CHANGESLOG_WITH_STREAM) {

      // read ChangesLog
      transactionChangesLog = (TransactionChangesLog) in.readObject();

      // read FixupStream count
      int iFixupStream = in.readInt();

      ArrayList<FixupStream> listFixupStreams = new ArrayList<FixupStream>();

      for (int i = 0; i < iFixupStream; i++) {
        FixupStream fs = new FixupStream();
        fs.readExternal(in);
        listFixupStreams.add(fs);
      }
      // listFixupStreams.add((FixupStream) in.readObject());

      // read stream data
      int iStreamCount = in.readInt();
      ArrayList<File> listFiles = new ArrayList<File>();

      for (int i = 0; i < iStreamCount; i++) {

        // read file size
        long fileSize = in.readLong();

        // read content file
        File contentFile = getAsFile(in, fileSize);
        listFiles.add(contentFile);
      }

      PendingChangesLog pendingChangesLog = new PendingChangesLog(transactionChangesLog,
                                                                  listFixupStreams,
                                                                  listFiles,
                                                                  fileCleaner);

      pendingChangesLog.restore();

      TransactionChangesLog log = pendingChangesLog.getItemDataChangesLog();

    } else if (changesLogType == PendingChangesLog.Type.CHANGESLOG_WITHOUT_STREAM) {
      transactionChangesLog = (TransactionChangesLog) in.readObject();
    }

    return transactionChangesLog;
  }

  private File getAsFile(ObjectInputStream ois, long fileSize) throws IOException {
    int bufferSize = /* 8191 */1024 * 8;
    byte[] buf = new byte[bufferSize];

    File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.nanoTime());
    FileOutputStream fos = new FileOutputStream(tempFile);
    long readBytes = fileSize;

    while (readBytes > 0) {
      // long longTemp = readByte - bufferSize;
      if (readBytes >= bufferSize) {
        ois.readFully(buf);
        fos.write(buf);
      } else if (readBytes < bufferSize) {
        ois.readFully(buf, 0, (int) readBytes);
        fos.write(buf, 0, (int) readBytes);
      }
      readBytes -= bufferSize;
    }

    fos.flush();
    fos.close();

    return tempFile;
  }

  /**
   * Write parameters to RegistryService.
   * 
   * @param sessionProvider
   *          The SessionProvider
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws RepositoryException
   */
  private void writeParamsToRegistryService(SessionProvider sessionProvider) throws IOException,
                                                                            SAXException,
                                                                            ParserConfigurationException,
                                                                            RepositoryException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement(SERVICE_NAME);
    doc.appendChild(root);

    Element element = doc.createElement(BACKUP_PROPERTIES);
    setAttributeSmart(element, BACKUP_DIR, backupDir);
    setAttributeSmart(element, DEFAULT_INCREMENTAL_JOB_PERIOD, defIncrPeriod);
    setAttributeSmart(element, FULL_BACKUP_TYPE, fullBackupType);
    setAttributeSmart(element, INCREMENTAL_BACKUP_TYPE, incrementalBackupType);
    root.appendChild(element);

    RegistryEntry serviceEntry = new RegistryEntry(doc);
    registryService.createEntry(sessionProvider, RegistryService.EXO_SERVICES, serviceEntry);
  }

  /**
   * Read parameters from RegistryService.
   * 
   * @param sessionProvider
   *          The SessionProvider
   * @throws RepositoryException
   * @throws PathNotFoundException
   */
  private void readParamsFromRegistryService(SessionProvider sessionProvider) throws PathNotFoundException,
                                                                             RepositoryException {
    String entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + BACKUP_PROPERTIES;
    RegistryEntry registryEntry = registryService.getEntry(sessionProvider, entryPath);
    Document doc = registryEntry.getDocument();
    Element element = doc.getDocumentElement();

    backupDir = getAttributeSmart(element, BACKUP_DIR);
    defIncrPeriod = getAttributeSmart(element, DEFAULT_INCREMENTAL_JOB_PERIOD);
    fullBackupType = getAttributeSmart(element, FULL_BACKUP_TYPE);
    incrementalBackupType = getAttributeSmart(element, INCREMENTAL_BACKUP_TYPE);

    log.info("Backup dir from RegistryService: " + backupDir);
    log.info("Default incremental job period from RegistryService: " + defIncrPeriod);
    log.info("Full backup type from RegistryService: " + fullBackupType);
    log.info("Incremental backup type from RegistryService: " + incrementalBackupType);

    checkParams();
  }

  /**
   * Get attribute value.
   * 
   * @param element
   *          The element to get attribute value
   * @param attr
   *          The attribute name
   * @return Value of attribute if present and null in other case
   */
  private String getAttributeSmart(Element element, String attr) {
    return element.hasAttribute(attr) ? element.getAttribute(attr) : null;
  }

  /**
   * Set attribute value. If value is null the attribute will be removed.
   * 
   * @param element
   *          The element to set attribute value
   * @param attr
   *          The attribute name
   * @param value
   *          The value of attribute
   */
  private void setAttributeSmart(Element element, String attr, String value) {
    if (value == null) {
      element.removeAttribute(attr);
    } else {
      element.setAttribute(attr, value);
    }
  }

  /**
   * Get parameters which passed from the file.
   * 
   * @throws RepositoryConfigurationException
   */
  private void readParamsFromFile() {
    PropertiesParam pps = initParams.getPropertiesParam(BACKUP_PROPERTIES);

    backupDir = pps.getProperty(BACKUP_DIR);
    defIncrPeriod = pps.getProperty(DEFAULT_INCREMENTAL_JOB_PERIOD);
    fullBackupType = pps.getProperty(FULL_BACKUP_TYPE);
    incrementalBackupType = pps.getProperty(INCREMENTAL_BACKUP_TYPE);

    log.info("Backup dir from configuration file: " + backupDir);
    log.info("Default incremental job period from configuration file: " + defIncrPeriod);
    log.info("Full backup type from configuration file: " + fullBackupType);
    log.info("Incremental backup type from configuration file: " + incrementalBackupType);

    checkParams();
  }

  /**
   * Check read params and initialize.
   */
  private void checkParams() {
    if (backupDir == null)
      throw new RuntimeException(BACKUP_DIR + " not specified");

    logsDirectory = new File(backupDir);
    if (!logsDirectory.exists())
      logsDirectory.mkdirs();

    if (defIncrPeriod == null)
      throw new RuntimeException(DEFAULT_INCREMENTAL_JOB_PERIOD + " not specified");

    defaultIncrementalJobPeriod = Integer.valueOf(defIncrPeriod);

    if (fullBackupType == null)
      throw new RuntimeException(FULL_BACKUP_TYPE + " not specified");

    if (incrementalBackupType == null)
      throw new RuntimeException(INCREMENTAL_BACKUP_TYPE + " not specified");
  }

  public BackupChain findBackup(String repository, String workspace) {
    Iterator<BackupChain> it = currentBackups.iterator();
    while (it.hasNext()) {
      BackupChain chain = it.next();
      if (repository.equals(chain.getBackupConfig().getRepository())
          && workspace.equals(chain.getBackupConfig().getWorkspace()))
        return chain;
    }
    return null;
  }

  public BackupChain findBackup(String backupId) {
    Iterator<BackupChain> it = currentBackups.iterator();
    while (it.hasNext()) {
      BackupChain chain = it.next();
      if (backupId.equals(chain.getBackupId()))
        return chain;
    }
    return null;
  }

  public BackupScheduler getScheduler() {
    return scheduler;
  }

  /**
   * Used in scheduler
   * 
   * @return
   */
  File getLogsDirectory() {
    return logsDirectory;
  }

  public File getBackupDirectory() {
    return getLogsDirectory();
  }

  public String getFullBackupType() {
    return fullBackupType;
  }

  public String getIncrementalBackupType() {
    return incrementalBackupType;
  }

  public long getDefaultIncrementalJobPeriod() {
    return defaultIncrementalJobPeriod;
  }

  public List<JobWorkspaceRestore> getRestores() {
    return restoreJobs;
  }

  public JobWorkspaceRestore getLastRestore(String repositoryName, String workspaceName) {
    for (int i = restoreJobs.size() - 1; i >= 0; i--) {
      JobWorkspaceRestore job = restoreJobs.get(i);

      if (repositoryName.equals(job.getRepositoryName())
          && workspaceName.equals(job.getWorkspaceName())) {
        return job;

      }
    }

    return null;
  }

  public void restore(BackupChainLog log,
                      String repositoryName,
                      WorkspaceEntry workspaceEntry,
                      boolean asynchronous) throws BackupOperationException,
                                           BackupConfigurationException,
                                           RepositoryException,
                                           RepositoryConfigurationException {
    if (asynchronous) {
      JobWorkspaceRestore jobRestore = new JobWorkspaceRestore(repoService,
                                                               this,
                                                               repositoryName,
                                                               log,
                                                               workspaceEntry);
      restoreJobs.add(jobRestore);
      jobRestore.start();
    } else {
      this.restoreOverInitializer(log, repositoryName, workspaceEntry);
    }
  }
}
