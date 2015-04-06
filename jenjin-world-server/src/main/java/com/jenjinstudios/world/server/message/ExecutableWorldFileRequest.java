package com.jenjinstudios.world.server.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.world.server.WorldClientHandler;
import com.jenjinstudios.world.server.WorldServerMessageContext;

/**
 * Process a WorldChecksumRequest.
 *
 * @author Caleb Brinkman
 */
public class ExecutableWorldFileRequest extends WorldExecutableMessage<WorldServerMessageContext>
{
	/**
	 * Construct a new ExecutableMessage.  Must be implemented by subclasses.
	 *
	 * @param handler The handler using this ExecutableMessage.
	 * @param message The message.
	 * @param context The context in which to execute the message.
	 */
	public ExecutableWorldFileRequest(WorldClientHandler handler, Message message, WorldServerMessageContext context) {
		super(message, context);
	}

	@Override
	public Message execute() {
		byte[] worldFileBytes = getContext().getWorldBytes();
		return WorldServerMessageFactory.generateWorldFileResponse(worldFileBytes);
	}
}
