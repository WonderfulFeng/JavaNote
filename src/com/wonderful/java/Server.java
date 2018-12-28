package com.wonderful.java;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<Socket> sockets = new ArrayList<>();

    public Server() throws IOException {
        try {
            System.out.println("初始化服务端...");
            serverSocket = new ServerSocket(8088);
            threadPool = Executors.newFixedThreadPool(50);
            System.out.println("服务端初始化完毕 ...");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void start() {

        System.out.println("等待客户端连接。。。");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("客户端连接上了...");
                threadPool.execute(new ServerRunable(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void sendToAllClient(String message, Socket target) {
        for (Socket socket : sockets) {
            if (target == socket) continue;
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
//            InetAddress address = socket.getInetAddress();
//            String ha = address.getHostAddress();
//            int port = socket.getPort();
            writer.println(message);
        }
    }

    public synchronized void addClent(Socket socket) {
        sockets.add(socket);
    }

    public synchronized void removeClent(Socket target) {
        for (Socket socket : sockets) {
            if (socket == socket) {
                sockets.remove(socket);
                break;
            }
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器初始化失败！");
            return;
        }
    }

    class ServerRunable implements Runnable {
        private Socket socket;

        public ServerRunable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                addClent(socket);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                String message = "";
                while ((message = reader.readLine()) != null) {
                    sendToAllClient(message, socket);
                }
            } catch (Exception e) {

            } finally {
                try {
                    removeClent(socket);
                    System.out.println("当前在线人数：" + sockets.size());
                    socket.close();//内部会自动管流
                    System.out.println("一个客户端下线了！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
