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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * PoolManager - handle ParserPool
 */
@ApplicationScoped
public class PoolManager {

    private static final Logger LOG =  Logger.getLogger(PoolManager.class.getName());

    @Inject
    private ParserPool parserPool;

    @Inject
    private DbFileManager dbFileManager;

    private TaskExecutor task;

    /**
     * Download DB file and restart pool
     *
     * @return true, if successful
     * @throws MalformedURLException the malformed URL exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UdgerException the udger exception
     */
    public boolean updateDb() throws MalformedURLException, IOException, UdgerException {
       dbFileManager.downloadDbFile();
       return restartPool();
    }

    /**
     * Restart pool -
     *
     * @return true, if successful
     */
    private boolean restartPool() {
        boolean result = true;
       if (dbFileManager.hasNewFile()) {
           parserPool.closePool(false);
           result = dbFileManager.moveDbFile();
           parserPool.startPool();
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
                       task = new TaskExecutor(()-> scheduledUpdateDb(),hr, min);
                       task.start();
                   }
               }
            }
        }
        return false;
    }

    private void scheduledUpdateDb() {
        try {
            updateDb();
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
