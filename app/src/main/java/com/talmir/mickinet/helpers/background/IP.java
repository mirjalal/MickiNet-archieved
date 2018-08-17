package com.talmir.mickinet.helpers.background;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author miri
 * @since 8/15/2018
 */
public final class IP {

    private static String clientIpAddress = "";

    @Contract(pure = true)
    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    private static void setClientIpAddress(@NonNull String ipAddress) {
        clientIpAddress = ipAddress;
    }

    /**
     * This method performs simple socket connection
     * between client device and server one. On the server
     * side we could get that IP address.
     *
     * @return  a new thread that sends client's
     *          Wi-Fi Direct IP address to the server.
     */
    @NonNull
    public static synchronized Thread sendIpAddress() {
        return new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress("192.168.49.1", 10000)), 5000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * This method accepts incoming socket connection
     * and gets remote socket address of the client.
     *
     * @return  a new thread that receives clients'
     *          Wi-Fi Direct IP addresses
     */
    @NonNull
    public static synchronized Thread receiveIpAddress() {
        return new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                if (!serverSocket.isBound())
                    serverSocket.bind(new InetSocketAddress(10000), 1);

                if (serverSocket.isBound() && !serverSocket.isClosed()) {
                    String clientIP = serverSocket.accept().getRemoteSocketAddress().toString();
                    setClientIpAddress(clientIP.substring(1, clientIP.indexOf(':')));
                }
            } catch (Exception ignored) {  } finally {
                try {
                    if (serverSocket != null && !serverSocket.isClosed())
                        serverSocket.close();
                } catch (Exception ignored) {
                }
            }
        });
    }
}
