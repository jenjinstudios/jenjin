package com.jenjinstudios.core.io;

import java.io.IOException;

/**
 * Used when a {@code MessageInputStream} reads a message that has not been registered.
 *
 * @author Caleb Brinkman
 */
public class MessageTypeException extends IOException
{
	private final short id;

	public MessageTypeException(short id) {
		super("Message " + id + " not registered.");
		this.id = id;
	}

	public short getId() { return id; }
}
