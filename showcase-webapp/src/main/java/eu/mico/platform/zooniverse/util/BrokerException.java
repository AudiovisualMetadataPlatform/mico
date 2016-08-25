package eu.mico.platform.zooniverse.util;

import com.sun.corba.se.pept.broker.Broker;

public class BrokerException extends RuntimeException{
    private int brokerHttpStatusCode = -1;

    public BrokerException() {
        super("Undefined error");
    }

    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(String message, int brokerHttpStatusCode) {
        super(message);
        this.brokerHttpStatusCode = brokerHttpStatusCode;
    }

    public int getBrokerHttpStatusCode() {
        return brokerHttpStatusCode;
    }
}
