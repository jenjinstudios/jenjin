package com.jenjinstudios.server.net;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.core.io.MessageStreamPair;
import com.jenjinstudios.server.authentication.AuthenticationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ClientHandler} class is used to communicate with an individual client.
 *
 * @author Caleb Brinkman
 */
public class ClientHandler<T extends ServerMessageContext> extends Connection<T>
{
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

	/**
	 * Construct a new Client Handler using the given socket.  When constructing a new ClientHandler, it is necessary to
     * send the client a FirstConnectResponse message with the server's UPS
	 * @param messageStreamPair The MessageIO used to send and receive messages.
	 * @param context The context in which messages should be executed.
	 */
	public ClientHandler(MessageStreamPair messageStreamPair, T context) {
		super(messageStreamPair, context);
		getMessageContext().setName("ClientHandler " + getId());
	}

	/** Shut down the client handler. */
	@Override
    public void shutdown() {
		if (getMessageContext().getUser() != null)
		{
            try
            {
				getMessageContext().getAuthenticator().logOutUser(getMessageContext().getUser().getUsername());
			} catch (AuthenticationException e)
			{
                LOGGER.log(Level.WARNING, "Unable to perform emergency logout.", e);
            }
        }
		super.shutdown();
	}

}
