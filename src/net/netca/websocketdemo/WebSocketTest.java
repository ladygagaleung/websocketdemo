package net.netca.websocketdemo;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
public class WebSocketTest {

	private static final Set connections = new CopyOnWriteArraySet<>();

	
	/*
	 * @OnPen，连接创建时调用的方法
	 * 
	 * @OnClose，连接关闭时调用的方法
	 * 
	 * @OnMessage，传输信息过程中调用的方法
	 * 
	 * @OnError，发生错误时调用的方法
	 */

	@OnMessage
	public void onMessage(String message, Session session) throws IOException,
			InterruptedException {
		broadcast(message, session);
	       
	}

	private static synchronized void broadcast(String message, Session session) {
		for (Object clientObj : connections) {
			Session client = (Session) clientObj;
			String respMsg;
			if (session == client) {
				respMsg = "[you]" + message;
			}else{
				String ip = WebSocketUtil.getClientIp(session);
				respMsg = "[" + ip + ":" + session.getId() + "]" + message;
			}
			try {
				client.getBasicRemote().sendText(respMsg);
			} catch (IOException e) {
				System.out
						.println("Chat Error: Failed to send message to client");
				connections.remove(client);
				try {
					client.close();
				} catch (IOException e1) {
					// Ignore
				}
			}
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Client connected");
		connections.add(session);
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection closed");
		connections.remove(session);
	}
}
