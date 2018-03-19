/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.udger.parser.UdgerParser;
import org.udger.parser.UdgerParser.ParserDbData;

/**
 * The Class ParserPool.
 */
@ApplicationScoped
public class ParserPool {

    private static final Logger LOG =  Logger.getLogger(ParserPool.class.getName());

    private static final int INITIAL_POOL_SIZE = 5;
    private static final int INITIAL_PARSER_CACHE_SIZE = 10;

    @Inject
    private DbFileManager dbFileManager;

    private volatile LinkedBlockingQueue<UdgerParser> borrowingPool;
    private LinkedBlockingQueue<UdgerParser> pool = new LinkedBlockingQueue<>();
    private int poolSize;
    private boolean parsersStopped = true;        // means all Parsers in pool is finished
    private boolean poolStarted = false;

    @PostConstruct
    public void init() {
        pool = borrowingPool = new LinkedBlockingQueue<>();
        try {
            if (dbFileManager.hasSqliteDbFile()) {
                startPool(dbFileManager.getDbFileName());
            } else {
                LOG.info("Pool not poolStarted, no DB file.");
            }
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Parse pool init failed.", e);
        }
    }

    /**
     * @return true, if pool is poolStarted
     */
    public boolean isStarted() {
        return poolStarted;
    }

    /**
     * Borrow parser from internal pool.
     *
     * @return the udger parser
     */
    public UdgerParser borrowParser() {
        if (borrowingPool != null) {
            try {
                return borrowingPool.take();
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "takeParser(): interupted.", e);
            }
        }
        return null;
    }

    /**
     * Return parser to internal pool
     *
     * @param parser the parser
     */
    public void returnParser(UdgerParser parser) {
        try {
            pool.put(parser);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "returnParser(): interupted.", e);
        }
    }

    /**
     * Close pool.
     *
     * @param shutdown the shutdown
     */
    public void closePool(boolean shutdown) {
        borrowingPool = shutdown ? null : new LinkedBlockingQueue<>();
        try {
            int cnt = poolSize;
            while (cnt > 0) {
                try {
                    UdgerParser parser = pool.take();
                    cnt--;
                    parser.close();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "closePool(): close parser failed.", e);
                }
            }
        } finally {
            pool = borrowingPool;
        }
        parsersStopped = true;
    }

    /**
     * Start pool.
     */
    public void startPool(String dbFileName) {
        if (parsersStopped && pool != null && pool.isEmpty()) {
            buildPool(INITIAL_POOL_SIZE, dbFileName, INITIAL_PARSER_CACHE_SIZE);
        }
    }

    private void buildPool(int clstrSize, String dbFileName, int cacheSize) {
        try {
            Class.forName("org.sqlite.JDBC");
            ParserDbData dbData = new UdgerParser.ParserDbData(dbFileName);
            poolSize = 0;
            for (int i=0; i < clstrSize; i++) {
                pool.put(new UdgerParser(dbData, cacheSize));
                poolSize ++;
            }
            parsersStopped = false;
            poolStarted = true;
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "buildPool(): interupted.", e);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "buildPool(): sqlite drive not found.", e);
        }
    }

}
