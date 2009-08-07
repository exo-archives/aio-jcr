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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupJobListener;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class BackupChainImpl implements BackupChain {

  private final BackupConfig           config;

  private List<BackupJob>              jobs;

  private AbstractFullBackupJob        fullBackup;

  private AbstractIncrementalBackupJob incrementalBackup;

  private final BackupChainLog         chainLog;

  private int                          state;

  private PeriodConroller              periodConroller;

  private Timer                        timer;

  private final Calendar               timeStamp;

  private Set<BackupJobListener>       listeners = new LinkedHashSet<BackupJobListener>();

  public BackupChainImpl(BackupConfig config,
                         File logDirectory,
                         ManageableRepository repository,
                         String fullBackupType,
                         String incrementalBackupType) throws BackupOperationException,
      BackupConfigurationException {
    this.config = config;
    this.jobs = new ArrayList<BackupJob>();
    this.chainLog = new BackupChainLog(logDirectory, config, fullBackupType, incrementalBackupType);
    this.timeStamp = Calendar.getInstance();

    try {
      this.fullBackup = (AbstractFullBackupJob) Class.forName(fullBackupType).newInstance();
    } catch (Exception e) {
      throw new BackupConfigurationException("FullBackupType error, " + e, e);
    }
    fullBackup.init(repository, config.getWorkspace(), config, timeStamp);

    if (config.getBuckupType() == BackupManager.FULL_AND_INCREMENTAL) {
      try {
        this.incrementalBackup = (AbstractIncrementalBackupJob) Class.forName(incrementalBackupType)
                                                                     .newInstance();
      } catch (Exception e) {
        throw new BackupConfigurationException("IncrementalBackupType error, " + e, e);
      }
      incrementalBackup.init(repository, config.getWorkspace(), config, timeStamp);

      periodConroller = new PeriodConroller(config.getIncrementalJobPeriod() * 1000); // sec --> ms
    }
    this.state = INITIALIZED;
    this.timer = new Timer("BackupChain_" + getBackupConfig().getRepository() + "@"
        + getBackupConfig().getWorkspace() + "_PeriodTimer_"
        + new SimpleDateFormat("yyyyMMdd.HHmmss.SSS").format(new Date()), true);
  }

  /**
   * Add all listeners to a given job. Used in startBackup() which itself is synchronized.
   * 
   * @param job
   */
  private void addJobListeners(BackupJob job) {
    for (BackupJobListener jl : listeners)
      job.addListener(jl);
  }

  /**
   * Remove all listeners from a given job. Used in stoptBackup() which itself is synchronized.
   * 
   * @param job
   */
  private void removeJobListeners(BackupJob job) {
    for (BackupJobListener jl : listeners)
      job.removeListener(jl);
  }

  public void addListener(BackupJobListener listener) {
    if (listener != null) {
      synchronized (jobs) {
        for (BackupJob job : jobs)
          job.addListener(listener);
      }
      synchronized (listeners) {
        listeners.add(listener);
      }
    }
  }

  public void removeListener(BackupJobListener listener) {
    if (listener != null) {
      try {
        synchronized (jobs) {
          for (BackupJob job : jobs)
            job.removeListener(listener);
        }
      } finally {
        // remove anyway
        synchronized (listeners) {
          listeners.remove(listener);
        }
      }
    }
  }

  public List<BackupJob> getBackupJobs() {
    return jobs;
  }

  public final synchronized void startBackup() {

    addJobListeners(fullBackup);

    Thread fexecutor = new Thread(fullBackup, config.getRepository() + "@" + config.getWorkspace()
        + "-" + fullBackup.getId());
    fexecutor.start();
    state |= FULL_WORKING;
    chainLog.addJobEntry(fullBackup);
    jobs.add(fullBackup);

    if (incrementalBackup != null) {
      addJobListeners(incrementalBackup);

      Thread iexecutor = new Thread(incrementalBackup, config.getRepository() + "@"
          + config.getWorkspace() + "-" + incrementalBackup.getId());
      iexecutor.start();
      state |= INCREMENTAL_WORKING;
      chainLog.addJobEntry(incrementalBackup);
      jobs.add(incrementalBackup);
    }

    if (config.getIncrementalJobPeriod() > 0)
      periodConroller.start();
  }

  public final synchronized void stopBackup() {
    if (fullBackup.getState() != BackupJob.FINISHED) {
      fullBackup.stop();
      chainLog.addJobEntry(fullBackup);
      removeJobListeners(fullBackup);
    }

    if (incrementalBackup != null && incrementalBackup.getState() != BackupJob.FINISHED) {
      if (config.getIncrementalJobPeriod() > 0)
        periodConroller.stop();

      incrementalBackup.stop();
      chainLog.addJobEntry(incrementalBackup);
      removeJobListeners(incrementalBackup);
    }

    this.state |= FINISHED;
    chainLog.endLog();
  }

  public void restartIncrementalBackup() {
    incrementalBackup.suspend();
    incrementalBackup.resume();
    chainLog.addJobEntry(incrementalBackup);

    // [PN] 05.02.2008 don't add same job
    // jobs.add(incrementalBackup);
  }

  public BackupConfig getBackupConfig() {
    return config;
  }

  public int getFullBackupState() {
    return fullBackup.getState();
  }

  public String getLogFilePath() {
    return chainLog.getLogFilePath();
  }

  private class PeriodConroller extends TimerTask {
    protected Log   log     = ExoLogger.getLogger("ext.PeriodConroller");

    protected Long  period;

    private boolean isFirst = false;

    public PeriodConroller(long period) {
      this.period = period;
    }

    @Override
    public void run() {
      if (!isFirst) {
        isFirst = true;
      } else {
        Thread starter = new Thread() {
          public void run() {
            restartIncrementalBackup();
            if (log.isDebugEnabled())
              log.debug("Restart incrementalBackup :" + new Date().toString());
          }
        };
        starter.start();
      }
    }

    public void start() {
      timer.schedule(this, new Date(), period);
    }

    public boolean stop() {
      log.info("Stop period controller");
      return this.cancel();
    }
  }

  public int getState() {
    return state;
  }

  public boolean isFinished() {
    return ((state & BackupChain.FINISHED) == BackupChain.FINISHED ? true : false);
  }
}
