package server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestHandler implements Runnable {
    private Socket socket;
    private String method;
    private String path;
    private String version;
    private BufferedReader reader;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private Map<String, String> parameters = new HashMap<>();

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        int id = Server.count.getAndIncrement();
        System.out.println("[" + id + "]" + Thread.currentThread().getName()+" 开始处理");
        try {
            // 解析请求
            parseRequest();
            if (method == null) {
                socket.close();
                return;
            }
            // 处理请求
            if (method.equals("GET")) {
                handleGet();
            } else if (method.equals("HEAD")) {
                handleHead();
            } else if (method.equals("POST")) {
                handlePost();
            } else {
                send404();
            }
            // 关闭连接
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("[" + id + "]" + Thread.currentThread().getName()+" 结束处理");
        }
    }

    private void handleGet() throws Exception {
        Thread.sleep(3000);
        if (path.equals("/") || path.equals("/index.html")) {
            sendHomePage();
            return;
        }
        if (path.equals("/logout")) {
            handleLogout();
            return;
        }
        String requestPath = path;
        // 处理请求资源
        File file = new File("web" + requestPath);
        System.out.println("GET 访问文件: " + file.getAbsolutePath());
        // 返回结果
        if (file.exists() && file.isFile()) {
            sendFile(file, requestPath);
            System.out.println("GET 已发送: " + file.getPath());
        } else {
            send404();
        }
    }

    private void handleHead() throws IOException {
        String requestPath = path;
        // 处理请求资源
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }
        File file = new File("web" + requestPath);
        System.out.println("HEAD 访问文件：" + file.getAbsolutePath());
        // 返回结果
        if (file.exists() && file.isFile()) {
            sendHead(file, requestPath);
            System.out.println("HEAD 已发送: " + file.getPath());

        } else {
            send404();
        }
    }

    private void handlePost() throws IOException {
        System.out.print("POST请求内容: ");
        System.out.println(body);
        String username = parameters.get("username");
        OutputStream out = socket.getOutputStream();
        String header = "HTTP/1.1 302 Found\r\n" +
                "Location: /\r\n" +
                "Set-Cookie: username=" + username + "; Path=/\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
        System.out.println("用户登录: " + username);
    }

    private void handleLogout() throws IOException {
        OutputStream out = socket.getOutputStream();
        String header = "HTTP/1.1 302 Found\r\n" +
                "Location: /\r\n" +
                "Set-Cookie: username=; Max-Age=0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
    }

    private void sendHead(File file, String requestPath) throws IOException {
        OutputStream out = socket.getOutputStream();
        String contentType = getContentType(requestPath);
        long contentLength = file.length();
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();
    }

    private void sendHomePage() throws IOException {
        String username = getUsernameFromCookie();
        String html;
        if (username == null) {
            html = "<html>" +
                    "<head>" +
                    "<title>HTTP服务器</title>\n" +
                    "</head>" +
                    "<body>" +
                    "<h1>欢迎使用 HTTP 服务器!</h1>" +
                    "<img src='images/duck.jpg' width='200'><br><br>" +
                    "<a href='/login.html'>" +
                    "<button>登录</button>" +
                    "</a>" +
                    "</body>" +
                    "</html>";
        } else {
            html = "<html>" +
                    "<head>" +
                    "<title>HTTP服务器</title>\n" +
                    "</head>" +
                    "<body>" +
                    "<h1>欢迎, " + username + "! </h1>" +
                    "<img src='images/duck.jpg' width='200'><br><br>" +
                    "<a href='/logout'>" +
                    "<button>退出</button>" +
                    "</a>" +
                    "</body>" +
                    "</html>";
        }
        byte[] data = html.getBytes();
        OutputStream out = socket.getOutputStream();
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.write(data);
        out.flush();
    }

    private void sendFile(File file, String requestPath) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = fis.readAllBytes();
        fis.close();
        OutputStream out = socket.getOutputStream();
        String contentType = getContentType(requestPath);
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        out.write(header.getBytes());
        out.write(data);
        out.flush();
    }

    private void send404() throws IOException {
        OutputStream out = socket.getOutputStream();
        String body = "<h1>404 Not Found</h1>";
        // 响应头
        String header = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " +
                body.getBytes().length +
                "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.write(body.getBytes());
        out.flush();
    }

    private void parseRequest() throws IOException {
        // 输入流
        InputStream in = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in));
        // 请求头
        String requestLine = reader.readLine();
        String[] parts = requestLine.split(" ");
        method = parts[0];
        path = parts[1];
        version = parts[2];
        System.out.println("\n收到请求: " + requestLine);
        // 保存请求头内容
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) break;
            int index = line.indexOf(":");
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1).trim();
            headers.put(key, value);
            System.out.println(line);
        }
        System.out.println("已读取请求头!");
        // POST请求体处理
        if (method.equals("POST")) {
            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr != null) {
                int contentLength = Integer.parseInt(contentLengthStr);
                char[] buffer = new char[contentLength];
                reader.read(buffer);
                body = new String(buffer);
                parseParameters();
            }
        }
    }

    private void parseParameters() {
        if (body == null || body.isEmpty()) {
            return;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                parameters.put(kv[0], kv[1]);
            }
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html"))
            return "text/html";
        if (fileName.endsWith(".css"))
            return "text/css";
        if (fileName.endsWith(".js"))
            return "application/javascript";
        if (fileName.endsWith(".jpg"))
            return "image/jpeg";
        if (fileName.endsWith(".jpeg"))
            return "image/jpeg";
        if (fileName.endsWith(".png"))
            return "image/png";
        if (fileName.endsWith(".gif"))
            return "image/gif";
        return "application/octet-stream";
    }

    private String getUsernameFromCookie() {
        String cookie = headers.get("Cookie");
        if (cookie == null) {
            return null;
        }
        String[] cookies = cookie.split(";");
        for (String c : cookies) {
            c = c.trim();
            if (c.startsWith("username=")) {
                return c.substring("username=".length());
            }
        }
        return null;
    }
}
