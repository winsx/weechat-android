// Copyright 2012 Keith Johnson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ubergeek42.weechat.relay;

import com.ubergeek42.weechat.relay.connection.Connection;
import com.ubergeek42.weechat.relay.protocol.Info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelayConnection implements Connection, Connection.Observer {
    private static Logger logger = LoggerFactory.getLogger("RelayConnection");

    final private static String ID_VERSION = "version";
    final private static String ID_LIST_BUFFERS = "listbuffers";

    private Connection.Observer observer;

    private long version;
    private String password;
    private Connection connection;

    public RelayConnection(Connection connection, String password) {
        this.connection = connection;
        this.password = password;
        connection.setObserver(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public void sendMessage(String string) {
        connection.sendMessage(string);
    }

    @Override public void setObserver(Observer observer) {
        this.observer = observer;
    }

    @Override public STATE getState() {
        return null;
    }

    public void connect() {
        logger.debug("connect()");
        connection.connect();
    }

    public void disconnect() {
        logger.debug("disconnect()");
        sendMessage("quit\n");
        connection.disconnect();
    }

    public long getVersion() {
        return version;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public void onStateChanged(STATE state) {
        observer.onStateChanged(state);
        if (state == STATE.CONNECTED) authenticate();
    }

    // ALWAYS followed by onStateChanged(STATE.SHUT_DOWN). might be StreamClosed
    @Override public void onException(Exception e) {
        observer.onException(e);
    }

    @Override public void onMessage(RelayMessage message) {
        String id = message.getID();
        logger.trace("onMessage(id = {})", id);

        if (ID_VERSION.equals(id)) {
            version = Long.parseLong(((Info) message.getObjects()[0]).getValue());
            logger.info("WeeChat version: {}", String.format("0x%x", version));
            onStateChanged(STATE.AUTHENTICATED);
        }

        observer.onMessage(message);

        // ID_LIST_BUFFERS must get requested after onAuthenticated() (BufferList does that)
        if (ID_LIST_BUFFERS.equals(id)) onStateChanged(STATE.BUFFERS_LISTED);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////// auth

    private void authenticate() {
        String password = this.password.replace(",", "\\,");
        sendMessage(String.format("init password=%s,compression=zlib\n" +
                "(%s) info version_number\n", password, ID_VERSION));
    }

}