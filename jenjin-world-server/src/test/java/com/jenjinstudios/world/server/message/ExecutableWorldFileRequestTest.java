package com.jenjinstudios.world.server.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.world.server.WorldClientHandler;
import com.jenjinstudios.world.server.WorldServer;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Caleb Brinkman
 */
public class ExecutableWorldFileRequestTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteMessage() {
		Message message = mock(Message.class);
		Message response = MessageRegistry.getInstance().createMessage("WorldFileResponse");
		byte[] fileBytes = {1, 2, 3, 4, 5};
		response.setArgument("fileBytes", fileBytes);
		WorldClientHandler clientHandler = mock(WorldClientHandler.class);
		WorldServer server = mock(WorldServer.class);
		WorldServerMessageFactory messageFactory = mock(WorldServerMessageFactory.class);
		when(messageFactory.generateWorldFileResponse(any())).thenReturn(response);
		when(server.getWorldFileChecksum()).thenReturn(fileBytes);
		when(clientHandler.getMessageFactory()).thenReturn(messageFactory);
		when(clientHandler.getServer()).thenReturn(server);

		ExecutableWorldFileRequest exec = new ExecutableWorldFileRequest(clientHandler, message);
		exec.runImmediate();
		exec.runDelayed();

		verify(clientHandler).queueOutgoingMessage(response);
	}
}
