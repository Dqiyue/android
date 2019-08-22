package com.example.mentor;

import java.net.*;
import java.io.*;

public class MyClient {
    public String greet(String host,int port,String message) {
        String resp = null;
        try {
            Socket s = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            s.connect(socketAddress, 2000);
            if (!s.isConnected()) {
                resp = "greet " + host +  ":" + port + " failed !";
                return resp;
            }
            if (s.isOutputShutdown()) {
                resp = "greet " + host +  ":" + port + " failed ! the input stream is shutdown!";
                return resp;
            }
            //BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //s.getInputStream().read(message.getBytes());
            s.getOutputStream().write(message.getBytes());
            s.getOutputStream().flush();
            resp = "greet " + host +  ":" + port + " success !";
            s.close();
        }catch (IOException e) {
            e.printStackTrace();
            resp = "greet " + host +  ":" + port + " failed !:" + e.getMessage();
            return resp;
        }

        return resp;
    }
}
