package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static AtomicInteger count = new AtomicInteger(1);

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("HTTP服务器启动成功");
            System.out.println("监听端口:8080");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\n客户端连接：" + socket.getInetAddress());
                pool.execute(new RequestHandler(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
