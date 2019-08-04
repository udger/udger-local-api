/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Class TaskExecutor.
 */
public class TaskExecutor {

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduled;
    private Runnable task;
    boolean started = false;
    volatile boolean canceled = false;
    private final int targetHour;
    private final int targetMin;

    public TaskExecutor(Runnable task, int targetHour, int targetMin) {
        this.task = task;
        this.targetHour = targetHour;
        this.targetMin = targetMin;
    }

    public int getTargetHour() {
        return targetHour;
    }

    public int getTargetMin() {
        return targetMin;
    }

    public void start() {
        if (!started) {
            started = true;
            doStart();
        }
    }

    public void cancel() {
        if (!canceled) {
            boolean callCancel = false;
            synchronized (this) {
                if (!canceled) {
                    canceled = true;
                    callCancel = true;
                }
            }
            if (callCancel) {
                scheduled.cancel(false);
            }
        }
    }

    private void doStart() {
        Runnable taskWrapper = new Runnable() {
            @Override
            public void run() {
                if (!canceled) {
                    task.run();
                    start();
                }
            }
        };
        long delay = computeNextDelay(targetHour, targetMin, 0);
        synchronized(this) {
            if (!canceled) {
                scheduled = executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
            }
        }
    }

    private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        if (zonedNow.compareTo(zonedNextTarget) > 0) {
            zonedNextTarget = zonedNextTarget.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.getSeconds();
    }

}
