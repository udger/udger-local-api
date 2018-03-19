/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * The Class ParserStatistics.
 */
@ApplicationScoped
public class ParserStatistics {

    private static final int AVG_PERIOD = 10;

    private static class StatisticRec {

        private int counterChunks[] = new int[AVG_PERIOD];

        private int totalRequests;
        private long totalNonos;
        private long lastSec;

        public StatisticRec() {
            for (int i = 0; i < AVG_PERIOD; i++) {
                counterChunks[i] = 0;
            }
        }
    }

    private StatisticRec statisticUA;
    private StatisticRec statisticIP;

    @PostConstruct
    public void init() {
        statisticUA = new StatisticRec();
        statisticIP = new StatisticRec();
    }

    /**
     * @return the total UA requests
     */
    public int getTotalRequestsUA() {
        return statisticUA.totalRequests;
    }

    /**
     * @return the total IP requests IP
     */
    public int getTotalRequestsIP() {
        return statisticIP.totalRequests;
    }

    /**
     * Gets the total nanoseconds in UA requests
     *
     * @return the total nanos UA
     */
    public long getTotalNanosUA() {
        return statisticUA.totalNonos;
    }

    /**
     * @return the total nanos in IP request
     */
    public long getTotalNanosIP() {
        return statisticIP.totalNonos;
    }

    /**
     * @return the avg request throughput UA
     */
    public double getAvgThroughputUA() {
       return doGetAvgThroughput(statisticUA);
    }

    /**
     * Gets the avg throughput IP.
     *
     * @return the avg request throughput IP
     */
    public double getAvgThroughputIP() {
       return doGetAvgThroughput(statisticIP);
    }

    private synchronized double doGetAvgThroughput(StatisticRec stat) {
        if (stat.lastSec > 0) {
            long curSec = System.currentTimeMillis() / 1000;
            shuffleChunksToSec(stat, curSec);

            // don't count last chunk since it is not completed
            long indx = stat.lastSec;

            int tc = 0;
            for (int i = AVG_PERIOD; i > 0; i--) {
                tc += stat.counterChunks[(int) indx % AVG_PERIOD];
                indx --;
            }
            return Math.round(100.0 * tc / (AVG_PERIOD - 0.5)) / 100.0;
        }
        return 0.0;
    }

    /**
     * Update UA request statistic
     *
     * @param nanos the nanoseconds
     */
    public void updateStatisticUA(long nanos) {
        doReport(statisticUA, nanos);
    }

    /**
     * Update IP request statistic
     *
     * @param nanos the nanoseconds
     */
    public void updateStatisticIP(long nanos) {
        doReport(statisticIP, nanos);
    }

    private synchronized void doReport(StatisticRec stat, long nanos) {

        stat.totalRequests ++;
        stat.totalNonos += nanos;

        long curSec = System.currentTimeMillis() / 1000;
        shuffleChunksToSec(stat, curSec);
        stat.counterChunks[(int) curSec % AVG_PERIOD] ++;
    }

    private void shuffleChunksToSec(StatisticRec stat, long curSec) {
        if (curSec != stat.lastSec) {
            long indx = stat.lastSec + 1;
            if (curSec - (AVG_PERIOD - 1) > indx) {
                indx = curSec - (AVG_PERIOD - 1);
            }
            while (indx <= curSec) {
                stat.counterChunks[(int) indx % AVG_PERIOD] = 0;
                indx ++;
            }
            stat.lastSec = curSec;
        }
    }
}
