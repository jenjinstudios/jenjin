package com.jenjinstudios.core.message;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.core.io.Message;

public class ExecutablePingResponse extends ExecutableMessage
{
	private final Connection connection;

	public ExecutablePingResponse(Connection connection, Message message) {
		super(message);

		this.connection = connection;
	}

	@Override
	public void runDelayed() {
		long requestTime = (long) getMessage().getArgument("requestTimeMillis");
		connection.getPingTracker().addPingTime((System.currentTimeMillis() - requestTime));
	}

	@Override
	public void runImmediate() {

	}
}
