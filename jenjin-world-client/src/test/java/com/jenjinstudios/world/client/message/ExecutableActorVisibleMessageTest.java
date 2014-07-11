package com.jenjinstudios.world.client.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.world.World;
import com.jenjinstudios.world.WorldObject;
import com.jenjinstudios.world.client.WorldClient;
import com.jenjinstudios.world.math.Angle;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Caleb Brinkman
 */
public class ExecutableActorVisibleMessageTest
{
	@Test
	public void testMessageExecution() throws Exception {
		MessageRegistry messageRegistry = new MessageRegistry();
		Message actorVisibleMessage = messageRegistry.createMessage("ActorVisibleMessage");
		actorVisibleMessage.setArgument("name", "a1b2c3d4e5f6890");
		actorVisibleMessage.setArgument("id", 100);
		actorVisibleMessage.setArgument("resourceID", 100);
		actorVisibleMessage.setArgument("xCoordinate", 1.0);
		actorVisibleMessage.setArgument("yCoordinate", 1.0);
		actorVisibleMessage.setArgument("relativeAngle", Angle.IDLE);
		actorVisibleMessage.setArgument("absoluteAngle", 0.0);
		actorVisibleMessage.setArgument("timeOfVisibility", 100l);

		WorldClient worldClient = mock(WorldClient.class);
		World world = mock(World.class);
		when(worldClient.getWorld()).thenReturn(world);

		ExecutableActorVisibleMessage message = new ExecutableActorVisibleMessage(worldClient, actorVisibleMessage);
		message.runImmediate();
		message.runDelayed();

		verify(world).addObject((WorldObject) any(), eq(100));
	}
}
