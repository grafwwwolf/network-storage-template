package ru.gb.storage.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ClientEcho {
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final static Scanner SCANNER = new Scanner(System.in);
    private static SocketChannel SOCKET;
    static ByteBuffer bf = ByteBuffer.allocate(256);

    public static void main(String[] args) {

        try {
            start_echo_client();
        } finally {
            SCANNER.close();
        }
    }

    public static void start_echo_client() {
        try {
            SOCKET = SocketChannel.open(new InetSocketAddress("localhost", 9000));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Welcome to the Echo-Server");

        executorService.execute(()->{
            read();
        });
        readSendMessageServer();
    }

    public static void readSendMessageServer() {

        while (true) {
            System.out.print("Enter your message->>");
            String msg = SCANNER.nextLine();
            try {
                SOCKET.write(ByteBuffer.wrap(msg.getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Send message from server: " + msg);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void read () {
        try {
            SOCKET.read(bf);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = new String(bf.array());
        System.out.println("Received message from server: " + message);
    }
}