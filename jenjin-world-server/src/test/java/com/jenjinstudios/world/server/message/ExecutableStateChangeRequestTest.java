package com.jenjinstudios.world.server.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageRegistry;
import com.jenjinstudios.server.net.ServerMessageContext;
import com.jenjinstudios.world.World;
import com.jenjinstudios.world.math.Angle;
import com.jenjinstudios.world.server.Player;
import com.jenjinstudios.world.server.WorldClientHandler;
import com.jenjinstudios.world.server.WorldServer;
import com.jenjinstudios.world.util.WorldUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Caleb Brinkman
 */
@SuppressWarnings("unchecked")
public class ExecutableStateChangeRequestTest
{
	private static final MessageRegistry messageRegistry = MessageRegistry.getGlobalRegistry();

	@Test
	public void testValidRequest() throws InterruptedException {
		World world = WorldUtils.createDefaultWorld();
		Player player = new Player("FooBar");
		world.getWorldObjects().add(player);
		world.update();
		WorldClientHandler mock = mock(WorldClientHandler.class);
		when(mock.getUser()).thenReturn(player);
		WorldServer worldServer = mock(WorldServer.class);
		when(mock.getServer()).thenReturn(worldServer);
		when(worldServer.getUps()).thenReturn(50);
		Message request = mock(Message.class);
		when(request.getArgument("relativeAngle")).thenReturn(Angle.FRONT);
		when(request.getArgument("absoluteAngle")).thenReturn(0.0);
		when(request.getArgument("xCoordinate")).thenReturn(0.0);
		when(request.getArgument("yCoordinate")).thenReturn(0.0);
		when(request.getArgument("timeOfChange")).thenReturn(System.currentTimeMillis());
		ExecutableStateChangeRequest executableStateChangeRequest = new ExecutableStateChangeRequest(mock, request,
			  null);
		executableStateChangeRequest.execute();
		world.update();

		Assert.assertEquals(player.getAngle(), new Angle(0.0, Angle.FRONT));
	}

	@Test
	public void testInvalidRequestCoordinates() throws Exception {
		World world = WorldUtils.createDefaultWorld();
		Player player = new Player("FooBar");

		// Add player and update world
		world.getWorldObjects().add(player);
		world.update();

		ServerMessageContext<Player> context = mock(ServerMessageContext.class);
		WorldClientHandler mock = mock(WorldClientHandler.class);
		WorldServer worldServer = mock(WorldServer.class);
		when(worldServer.getUps()).thenReturn(50);
		when(context.getUser()).thenReturn(player);
		when(mock.getServer()).thenReturn(worldServer);

		// Create a state change request, with coordinates set further than the allowed error
		Message request = mock(Message.class);
		when(request.getArgument("relativeAngle")).thenReturn(Angle.FRONT);
		when(request.getArgument("absoluteAngle")).thenReturn(0.0);
		when(request.getArgument("xCoordinate")).thenReturn(15.0);
		when(request.getArgument("yCoordinate")).thenReturn(15.0);
		when(request.getArgument("timeOfChange")).thenReturn(System.currentTimeMillis());

		// Create the executable state change request
		ExecutableStateChangeRequest exec = new ExecutableStateChangeRequest(mock, request, context);
		exec.execute();

		world.update();

		Assert.assertEquals(player.getAngle(), new Angle(0.0, Angle.IDLE));
	}

	@Test
	public void testInvalidRequestTime() {
		// Functionally the same as testing excessive delay.
		World world = WorldUtils.createDefaultWorld();
		Player player = new Player("FooBar");
		world.getWorldObjects().add(player);
		world.update();

		WorldClientHandler mock = mock(WorldClientHandler.class);
		WorldServer worldServer = mock(WorldServer.class);
		Message request = mock(Message.class);
		ServerMessageContext<Player> context = mock(ServerMessageContext.class);

		when(context.getUser()).thenReturn(player);
		when(mock.getServer()).thenReturn(worldServer);
		when(worldServer.getUps()).thenReturn(50);
		when(request.getArgument("relativeAngle")).thenReturn(Angle.FRONT);
		when(request.getArgument("absoluteAngle")).thenReturn(0.0);
		when(request.getArgument("xCoordinate")).thenReturn(0.0);
		when(request.getArgument("yCoordinate")).thenReturn(0.0);
		when(request.getArgument("timeOfChange")).thenReturn(System.currentTimeMillis() - 2000);
		ExecutableStateChangeRequest exec = new ExecutableStateChangeRequest(mock, request, context);
		exec.execute();
		world.update();

		Assert.assertEquals(player.getAngle(), new Angle(0.0, Angle.IDLE));
	}

	@Test
	public void testExcessiveDelay() throws InterruptedException {
		World world = WorldUtils.createDefaultWorld();
		Player player = new Player("FooBar");
		world.getWorldObjects().add(player);
		world.update();

		WorldClientHandler mock = mock(WorldClientHandler.class);
		WorldServer worldServer = mock(WorldServer.class);
		Message request = mock(Message.class);
		ServerMessageContext<Player> context = mock(ServerMessageContext.class);

		when(mock.getServer()).thenReturn(worldServer);
		when(worldServer.getUps()).thenReturn(50);
		when(context.getUser()).thenReturn(player);
		when(request.getArgument("relativeAngle")).thenReturn(Angle.FRONT);
		when(request.getArgument("absoluteAngle")).thenReturn(0.0);
		when(request.getArgument("xCoordinate")).thenReturn(0.0);
		when(request.getArgument("yCoordinate")).thenReturn(0.0);
		when(request.getArgument("timeOfChange")).thenReturn(System.currentTimeMillis());
		Thread.sleep(1200);
		ExecutableStateChangeRequest exec = new ExecutableStateChangeRequest(mock, request, context);
		exec.execute();
		world.update();

		Assert.assertEquals(player.getAngle(), new Angle(0.0, Angle.IDLE));
	}
}
