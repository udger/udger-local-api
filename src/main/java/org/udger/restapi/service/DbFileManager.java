/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

/**
 * The Class DbFileManager.
 */
@ApplicationScoped
public class DbFileManager {

    private static final Logger LOG =  Logger.getLogger(DbFileManager.class.getName());

    private static final String DEFAULT_FILE_NAME = "/udgerdb/udgerdb_v3.dat";
    private static final String DOWNLOAD_URL = "http://data.udger.com/";

    private String dbFileName;
    private String clientKey;
    private boolean clientKeyLoaded;

    /**
     * Gets the client key.
     *
     * @return the client key
     */
    public String getClientKey() {
        if (clientKey == null && !clientKeyLoaded) {
            synchronized(this) {
                if (!clientKeyLoaded) {
                    clientKey = System.getProperty("udger.clientkey");
                    clientKeyLoaded = true;
                }
            }
        }
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    /**
     * Gets the db file name.
     *
     * @return the db file name
     */
    public String getDbFileName() {
        if (dbFileName == null) {
            synchronized (this) {
                if (dbFileName == null) {
                    dbFileName = System.getProperty("udgerdb");
                    if (dbFileName == null) {
                        dbFileName = DEFAULT_FILE_NAME;
                    }
                }
            }
        }
        return dbFileName;
    }

    /**
     * Download db file from client url.
     *
     * @return true, if successful
     * @throws UdgerException the udger exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public boolean downloadDbFile() throws UdgerException, IOException, ClassNotFoundException {
        if (getClientKey() != null) {
            URL website = null;
            String newDbFileName = getNewDbFileName();
            try {
                website = new URL(DOWNLOAD_URL + getClientKey() + "/udgerdb_v3.dat");
            } catch (MalformedURLException e) {
                throw new UdgerException(e.getMessage());
            }
            try (FileOutputStream fos = new FileOutputStream(newDbFileName)) {
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            if (!doCheckSqlLiteFileStructure(newDbFileName)) {
                throw new UdgerException("Downloaded file is not SQLite file!");
            }
            return true;
        }
        throw new UdgerException("Client key missing.");
    }

    /**
     * Checks for sqlite db file.
     *
     * @return true, if successful
     * @throws ClassNotFoundException
     */
    public boolean hasSqliteDbFile() throws ClassNotFoundException {
        String dbFn = getDbFileName();
        return new File(dbFn).isFile() && doCheckSqlLiteFileStructure(dbFn);
    }

    public boolean doCheckSqlLiteFileStructure(String dbfn) throws ClassNotFoundException {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:/"+ dbfn);
            try (Statement statement = conn.createStatement()) {
                ResultSet rs = statement.executeQuery("SELECT * FROM udger_db_info");
            }
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "downloadDbFile(): close connection failed.", e);
                }
            }
        }
    }

    private String getNewDbFileName() {
        return getDbFileName() + ".NEW";
    }

    /**
     * Checks if new file exists
     *
     * @return true, if successful
     */
    public boolean hasNewFile() {
        return new File(getNewDbFileName()).isFile();
    }

    /**
     * Update db file. Rename new dbfile to dbfile
     *
     * @return true, if successful
     */
    public boolean updateDbFile() {
        File fnew = new File(getNewDbFileName());
        File fold = new File(getDbFileName());
        if (fnew.isFile()) {
            return fnew.renameTo(fold);
        }
        return false;
    }

}
