package com.jenjinstudios.server.net;

/**
 * @author Caleb Brinkman
 */
public class ServerInit<T extends ClientHandler>
{
	private final int ups;
	private final Class<T> handlerClass;
	private final int port;

	public ServerInit(int ups, Class<T> handlerClass, int port) {
		this.ups = ups;
		this.handlerClass = handlerClass;
		this.port = port;
	}

	public Class<T> getHandlerClass() { return handlerClass; }

	public int getPort() { return port; }

	public int getUps() { return ups; }

}
