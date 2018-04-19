package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室服务器端
 * 
 * @author liudanfeng
 *
 */
public class Server {
	/**
	 * 1申请服务端口; 2监听服务端口，建立连接之后就创建一个socket用户与该客户端通讯
	 */
	private ServerSocket server;
	// 该集合用来存放所有客户端的输出流，用于将消息广播给所有客户端
	private List<PrintWriter> allOut;

	/**
	 * 构造方法 初始化指定服务端口，不能和系统其它应用端口重复
	 * 
	 * @throws IOException
	 */
	public Server() throws IOException {
		server = new ServerSocket(8088);
		allOut = new ArrayList<PrintWriter>();
	}

	public void start() {
		try {
			while (true) {
				System.out.println("等待客户端连接。。。");
				Socket socket = server.accept();
				System.out.println("客户端连接了。。。");
				// 启动一个线程与该客户端交互
				ClientHandler handler = new ClientHandler(socket);
				Thread thread = new Thread(handler);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将给定的消息广播给所有客户端
	 * 
	 * @param host
	 * @param message
	 */
	private void sendMessage(String message) {
		synchronized (allOut) {
			for (PrintWriter o : allOut) {
				o.println(message);
			}
		}
	}

	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private class ClientHandler implements Runnable {
		/**
		 * 当前线程通过这个socket与指定客户端交互
		 */
		private Socket socket;
		// 客户端的地址信息
		private String host;

		public ClientHandler(Socket socket) {
			this.socket = socket;
			InetAddress address = socket.getInetAddress();
			// 获取Ip地址
			this.host = address.getHostAddress();
		}

		public void run() {
			PrintWriter pw = null;
			try {
				// 接收读取客户端的输入流
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);

				// 通过socket获取输出流，将数据发送给客户端
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				pw = new PrintWriter(osw, true);

				// 将客户端的输出流存入到集合中
				// 由于多个线程都会调用该集合的add，为了保证线程安全，将该集合加锁
				synchronized (allOut) {
					allOut.add(pw);
				}
				sendMessage(host + "上线了，当前在线" + allOut.size() + "人");
				String message = null;
				while ((message = br.readLine()) != null) {
					// System.out.println("客户端" + host + "说：" + message);
					// 回复给当前客户端
					// pw.println(host + "说：" + message);
					// 转发给所有客户端
					sendMessage(host + " " + message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 处理客户端断开连接以后的工作
				// 将该客户端的输出流从共享集合中删除
				synchronized (allOut) {
					allOut.remove(pw);
				}
				sendMessage(host + "下线了，当前在线" + allOut.size() + "人");
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
