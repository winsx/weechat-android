package com.ubergeek42.weechat.relay.connection;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLConnection extends AbstractConnection {
    /** SSL Settings */
    private KeyStore sslKeyStore;

    public SSLConnection(String server, int port) {
        this.server = server;
        this.port = port;
        this.connector = sslConnector;

    }

    public void setSSLKeystore(KeyStore ks) {
        sslKeyStore = ks;
    }
    /**
     * Connects to the server(Via SSL) in a new thread, so we can interrupt it if we want to cancel the
     * connection
     */

    private Thread sslConnector = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(sslKeyStore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
                SSLSocket sslSock = (SSLSocket) sslContext.getSocketFactory().createSocket(server, port);
                sslSock.setKeepAlive(true);

                sock = sslSock;
                out_stream = sock.getOutputStream();
                in_stream = sock.getInputStream();
                connected = true;
                notifyHandlers(STATE.CONNECTED);
            } catch (Exception e) {
                e.printStackTrace();
                notifyHandlersOfError(e);
            }
        }
    });
}
