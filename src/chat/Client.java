package chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 聊天室客户端
 * 
 * @author liudanfeng
 *
 */
public class Client {
	private Socket socket;

	/**
	 * 构造方法，初始化客户端
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public Client() throws UnknownHostException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("config.conf")));
		String host = br.readLine();
		int port = Integer.parseInt(br.readLine());
		System.out.println("正在与服务端建立连接。。。");
		socket = new Socket(host, port);
		System.out.println("与服务端连接成功！");
	}

	/**
	 * 客户端的启动方法，从这里开始执行逻辑
	 */
	public void start() {
		try {
			OutputStream out = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
			PrintWriter pw = new PrintWriter(osw, true);
			Scanner sc = new Scanner(System.in);

			// 启动读取线程
			ServerHandler handler = new ServerHandler();
			Thread t = new Thread(handler);
			t.start();

			String usermsg = null;
			String nickname = null;
			System.out.println("输入一个昵称吧");
			nickname = sc.nextLine();
			System.out.println("开始聊天吧！");
			long time = System.currentTimeMillis();
			while (true) {
				usermsg = sc.nextLine();
				long curr = System.currentTimeMillis();
				if (curr - time > 1000) {
					pw.println(nickname + "：" + usermsg);
					time = curr;
				} else {
					System.out.println("说话速度太快，歇会儿再说吧");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Client client = new Client();
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage() + "客户端启动失败！");
		}
	}

	/**
	 * 该线程用来循环接收服务端发来的消息并输出到客户端
	 * 
	 * @author liudanfeng
	 *
	 */
	private class ServerHandler implements Runnable {
		public void run() {
			try {
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String message = null;
				while ((message = br.readLine()) != null) {
					System.out.println(message);
				}
			} catch (Exception e) {

			}
		}
	}

}
