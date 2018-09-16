package com.pavelsklenar.portscanner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class ScannerService {

    @Value("${scanner.timeout:100}")
    private int timeout;

    @Async
    public Future<ScanResult> checkPort(String ip, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return new AsyncResult<>(new ScanResult(port, true));
        } catch (IOException ex) {
            return new AsyncResult<>(new ScanResult(port, false));
        }
    }

    public final class ScanResult {

        private final int port;
        private final boolean isOpen;

        public ScanResult(int port, boolean isOpen) {
            this.port = port;
            this.isOpen = isOpen;
        }

        public int getPort() {
            return port;
        }

        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public String toString() {
            return "port " + port + " - " + (isOpen ? "open" : "closed");
        }
    }


}
