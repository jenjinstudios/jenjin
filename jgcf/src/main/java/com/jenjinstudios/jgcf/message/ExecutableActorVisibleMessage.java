package com.jenjinstudios.jgcf.message;

import com.jenjinstudios.jgcf.WorldClient;
import com.jenjinstudios.message.Message;
import com.jenjinstudios.world.ClientActor;
import com.jenjinstudios.world.state.MoveState;

/**
 * Process an ActorVisibleMessage.
 *
 * @author Caleb Brinkman
 */
public class ExecutableActorVisibleMessage extends WorldClientExecutableMessage
{
	/** The newly visible actor. */
	ClientActor newlyVisible;

	/**
	 * Construct an ExecutableMessage with the given Message.
	 *
	 * @param client  The client invoking this message.
	 * @param message The Message.
	 */
	protected ExecutableActorVisibleMessage(WorldClient client, Message message)
	{
		super(client, message);
	}

	@Override
	public void runSynced()
	{
	}

	@Override
	public void runASync()
	{
		Message message = getMessage();
		String name = (String) message.getArgument("name");
		int id = (int) message.getArgument("id");
		double xCoord = (double) message.getArgument("xCoord");
		double zCoord = (double) message.getArgument("zCoord");
		int direction = (int) message.getArgument("direction");
		double angle = (double) message.getArgument("angle");
		int stepsFromLast = (int) message.getArgument("stepsTaken");
		int stepsUntilChange = (int) message.getArgument("stepsUntilChange");

		newlyVisible = new ClientActor(id, name);
		newlyVisible.setVector2D(xCoord, zCoord);
		MoveState state = new MoveState(direction, stepsUntilChange, angle);
		newlyVisible.setCurrentMoveState(state);
		newlyVisible.setStepsTaken(stepsFromLast);
	}
}
