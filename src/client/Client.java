package client;

import java.util.regex.*;
import java.util.Scanner;
import java.net.Socket;
import java.io.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private static Socket connect() throws Exception {
        return new Socket(HOST, PORT);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("===== HTTP CLIENT =====");
            System.out.println("1.GET首页");
            System.out.println("2.HEAD首页");
            System.out.println("3.登录");
            System.out.println("4.退出登录");
            System.out.println("5.退出");
            System.out.print("请输入: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    sendGet("/");
                    break;
                case 2:
                    sendHead("/");
                    break;
                case 3:
                    login(scanner);
                    break;
                case 4:
                    logout();
                    break;
                case 5:
                    return;
            }
        }
    }

    static void sendGet(String path) {
        try {
            HttpResponse response = getResponse(path);
            if (path.equals("/")) {
                DownloadManager.createDownloadFolder();
                FileUtil.saveFile("/index.html", response.getBody());
                String html = response.getBodyAsString();
                System.out.println(html);
                downloadImages(html);
            } else {
                if (path.equals("/logout")) {
                    return;
                }
                FileUtil.saveFile(path, response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sendHead(String path) {
        try {
            if (path.equals("/logout")) return;
            Socket socket = connect();
            OutputStream out = socket.getOutputStream();
            String request = "HEAD " + path + " HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "\r\n";
            out.write(request.getBytes());
            out.flush();
            HttpResponse response = receiveResponse(socket);
            System.out.println(response.getHeaders());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login(Scanner scanner) {
        try {
            System.out.print("用户名:");
            String username = scanner.next();
            System.out.print("密码:");
            String password = scanner.next();
            String body = "username=" + username + "&password=" + password;
            Socket socket = connect();
            OutputStream out = socket.getOutputStream();
            String request = "POST /login HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "Content-Type: application/x-www-form-urlencoded\r\n" +
                    "Content-Length: " +
                    body.length() +
                    "\r\n\r\n" +
                    body;
            out.write(request.getBytes());
            out.flush();
            HttpResponse response = receiveResponse(socket);
            CookieManager.printCookies();
            socket.close();
            System.out.println("登录成功，欢迎 " + CookieManager.getCookie("username") + "!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logout() {
        String username = CookieManager.getCookie("username");
        if (username.isEmpty()) {
            System.out.println("未登录! ");
            return;
        }
        sendGet(("/logout"));
        System.out.println("注销成功, 再见 " + username + "!");
    }

    private static HttpResponse receiveResponse(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        HttpResponse response = new HttpResponse();
        String statusLine = readLine(in);
        response.setStatusLine(statusLine);
        System.out.println("StatusLine: " + statusLine);
        String line;
        while ((line = readLine(in)) != null) {
            if (line.isEmpty()) {
                break;
            }
            int index = line.indexOf(':');
            if (index > 0) {
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                response.getHeaders().put(key, value);
            }
        }
        String contentLength = response.getHeaders().get("Content-Length");
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength);
            byte[] body = in.readNBytes(length);
            response.setBody(body);
        }
        String setCookie = response.getHeaders().get("Set-Cookie");
        if (setCookie != null) {
            System.out.println("Set-Cookie: " + setCookie);
            CookieManager.saveCookie(setCookie);
        }
        return response;
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                int next = in.read();
                if (next == '\n') {
                    break;
                }
                buffer.write(c);
                if (next != -1) {
                    buffer.write(next);
                }
            } else {
                buffer.write(c);
            }
        }
        return buffer.toString("UTF-8");
    }

    private static HttpResponse getResponse(String path) throws Exception {
        Socket socket = new Socket(HOST, PORT);
        OutputStream out = socket.getOutputStream();
        String cookie = CookieManager.buildCookieHeader();
        String request = "GET " + path +
                " HTTP/1.1\r\n" +
                "Host: localhost\r\n";
        if (!cookie.isEmpty()) {
            request += "Cookie: " +
                    cookie +
                    "\r\n";
        }
        request += "\r\n";
        out.write(request.getBytes());
        out.flush();
        HttpResponse response = receiveResponse(socket);
        socket.close();
        return response;
    }

    private static void downloadImages(String html) throws Exception {
        Pattern pattern = Pattern.compile("src=['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String imagePath = matcher.group(1);
            System.out.println("发现图片: " + imagePath);
            downloadResource(imagePath);
        }
    }

    private static void downloadResource(String path) throws Exception {
        String resPath = path;
        if(!resPath.startsWith("/")){
            resPath = "/" + resPath;
        }
        HttpResponse response = getResponse(resPath);
        FileUtil.saveFile(resPath, response.getBody());
        System.out.println("下载完成: " + resPath);
    }

}
