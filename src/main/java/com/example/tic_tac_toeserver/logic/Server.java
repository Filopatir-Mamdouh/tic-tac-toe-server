package com.example.tic_tac_toeserver.logic;//


import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    DataInputStream input;

    public Server() {
        try {
            serverSocket = new ServerSocket(5005, 0, InetAddress.getByName("10.10.13.101"));
            System.out.println("Server started on port 5005");
            while(true) {
                clientSocket = serverSocket.accept();
                System.out.println("Connected!!");
                input = new DataInputStream(clientSocket.getInputStream());
                String s = input.readUTF();
                System.out.println(s);
            }
        } catch (IOException var2) {
            IOException e = var2;
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Server();
    }
}
