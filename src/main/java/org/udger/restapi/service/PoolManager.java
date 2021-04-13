/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * PoolManager - handle ParserPool
 */
@ApplicationScoped
@Startup
public class PoolManager {

    private static final Logger LOG =  Logger.getLogger(PoolManager.class.getName());

    @Inject
    private ParserPool parserPool;
    @Inject
    private DbFileManager dbFileManager;

    private TaskExecutor task;
    private Object updatingLock = new Object();
    private volatile boolean updatingDb = false;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        String autoupdateTime = System.getProperty("udger.autoupdate.time");
        if (autoupdateTime != null && !autoupdateTime.isEmpty()) {
            if (!scheduleUpdateDb(autoupdateTime)) {
                LOG.log(Level.WARNING, "Invalid daily autoupdate time=" + autoupdateTime + " Format HH:mm expected.");
            }
        }
    }


    /**
     * Download DB file and restart pool
     *
     * @return true, if successful
     * @throws MalformedURLException the malformed URL exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UdgerException the udger exception
     * @throws ClassNotFoundException
     */
    public boolean updateDb(boolean doDownloadDbFromUrl) throws UdgerException, IOException, ClassNotFoundException {
        if (!updatingDb) {
            boolean doUpdate = false;
            synchronized (updatingLock) {
                if (!updatingDb) {
                    updatingDb = true;
                    doUpdate = true;
                }
            }
            if (doUpdate) {
                try {
                    if (doDownloadDbFromUrl) {
                        dbFileManager.downloadDbFile();
                    }
                    boolean result = restartPool();
                    if (result) {
                        LOG.info("DB updated.");
                    }
                    return result;
                } finally {
                    updatingDb = false;
                }
            }
        }
        throw new UdgerException("DB is already updating!");
    }

    /**
     * Restart pool
     *
     * @return true, if successful
     */
    private boolean restartPool() {
       boolean result = true;
       if (dbFileManager.hasNewFile()) {
           parserPool.closePool(false);
           result = dbFileManager.updateDbFile();
           parserPool.startPool(dbFileManager.getDbFileName());
       }
       return result;
    }

    /**
     * Schedule update db
     *
     * @param at the schedule at time
     * @return true, if successful
     */
    public boolean scheduleUpdateDb(String at) {
        if (at != null && !at.isEmpty()) {
            String hrmin[] = at.split(":");
            if (hrmin.length == 2) {
               Integer hr = intVal(hrmin[0]);
               Integer min = intVal(hrmin[1]);
               if (hr != null && hr >=0 && hr < 24 &&  min != null && min >= 0 && min < 60) {
                   synchronized (this) {
                       if (task != null) {
                           if (task.getTargetHour() == hr && task.getTargetMin() == min) {
                               return true;
                           }
                           task.cancel();
                       }
                       task = new TaskExecutor(()-> scheduledUpdateDb(), hr, min);
                       task.start();
                       LOG.log(Level.INFO, "Update DB task scheduled for " + at);
                       return true;
                   }
               }
            } else {
                LOG.log(Level.WARNING, "Invalid schedule time format=" + at + " Time format \"HH:mm\" expected.");
            }
        }
        return false;
    }

    private void scheduledUpdateDb() {
        try {
            updateDb(true);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "scheduledUpdateDb(): failed", e);
        }
    }

    private Integer intVal(String val) {
        try {
            return Integer.valueOf(val);
        } catch (Exception e) {
        }
        return null;
    }
}
