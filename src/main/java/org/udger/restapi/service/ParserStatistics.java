package org.udger.restapi.service;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParserStatistics {

    private int requestsUA;
    private int requestsIP;
    private long nanosRequestUA;
    private long nanosRequestIP;

    public int getRequestsUA() {
        return requestsUA;
    }

    public void incRequestUA() {
        requestsUA ++;
    }

    public int getRequestsIP() {
        return requestsIP;
    }

    public void incRequestIP() {
        requestsIP ++;
    }

    public long getNanosRequestUA() {
        return nanosRequestUA;
    }

    public void addNanosRequestUA(long nanos) {
       nanosRequestUA += nanos;
    }

    public long getNanosRequestIP() {
        return nanosRequestIP;
    }

    public void addNanosRequestIP(long nanos) {
       nanosRequestIP += nanos;
    }
}
