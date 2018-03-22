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

    private static final int BUCKETS_SECS = 10;
    private static final int BUCKETS_PER_SECS = 4;
    private static final int TOTAL_BUCKETS = BUCKETS_SECS * BUCKETS_PER_SECS;

    private static class StatisticRec {

        private int buckets[] = new int[TOTAL_BUCKETS];

        private int totalRequests;
        private long totalNonos;
        private long lastBucket;

        public StatisticRec() {
            for (int i = 0; i < TOTAL_BUCKETS; i++) {
                buckets[i] = 0;
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
        if (stat.lastBucket > 0) {
            long curBucket = System.currentTimeMillis() * BUCKETS_PER_SECS / 1000;
            shuffleBucket(stat, curBucket);

            int tc = 0;
            for (int i = 0; i < TOTAL_BUCKETS; i++) {
                tc += stat.buckets[i];
            }
            return Math.round(100.0 * tc / (BUCKETS_SECS - 0.5)) / 100.0;
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

        long curBucket = System.currentTimeMillis() * BUCKETS_PER_SECS / 1000;
        shuffleBucket(stat, curBucket);
        stat.buckets[(int) (curBucket % TOTAL_BUCKETS)] ++;
    }

    private void shuffleBucket(StatisticRec stat, long bucket) {
        if (bucket != stat.lastBucket) {
            long indx = stat.lastBucket + 1;
            if (bucket - (TOTAL_BUCKETS - 1) > indx) {
                indx = bucket - (TOTAL_BUCKETS - 1);
            }
            for(;indx <= bucket;indx++) {
                stat.buckets[(int) (indx % TOTAL_BUCKETS)] = 0;
            }
            stat.lastBucket = bucket;
        }
    }
}
