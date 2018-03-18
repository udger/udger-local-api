package org.udger.restapi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DbFileManager {

    private static final String DEFAULT_FILE_NAME = "/udgerdb/udgerdb_v3.dat";
    private static final String DOWNLOAD_URL = "http://data.udger.com/";

    private String dbFileName;
    private String clientKey;
    private boolean clientKeyLoaded;

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

    public boolean downloadDbFile() throws MalformedURLException, IOException, UdgerException {
        if (getClientKey() != null) {
            URL website;
            website = new URL(DOWNLOAD_URL + getClientKey());
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(getNewDbFileName());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return true;
        }
        throw new UdgerException("Client key missing.");
    }

    public String getNewDbFileName() {
        return getDbFileName() + ".new";
    }

    public boolean hasNewFile() {
        return new File(getNewDbFileName()).isFile();
    }

    public boolean moveDbFile() {
        File fnew = new File(getNewDbFileName());
        File fold = new File(getDbFileName());
        if (fnew.isFile()) {
            return fnew.renameTo(fold);
        }
        return false;
    }

}
