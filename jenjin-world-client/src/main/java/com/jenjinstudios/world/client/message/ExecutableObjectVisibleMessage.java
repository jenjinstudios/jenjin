package com.jenjinstudios.world.client.message;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.world.World;
import com.jenjinstudios.world.client.WorldClientMessageContext;
import com.jenjinstudios.world.math.Vector2D;
import com.jenjinstudios.world.object.WorldObject;

/**
 * Process an ActorVisibleMessage.
 *
 * @author Caleb Brinkman
 */
@SuppressWarnings("WeakerAccess")
public class ExecutableObjectVisibleMessage extends WorldClientExecutableMessage<WorldClientMessageContext>
{
	/**
	 * Construct an ExecutableMessage with the given Message.
     *
     * @param message The Message.
	 * @param context The context in which to execute the message.
	 */
	public ExecutableObjectVisibleMessage(Message message, WorldClientMessageContext context) {
		super(message, context);
	}

    @Override
	public Message execute() {
		Message message = getMessage();
        String name = (String) message.getArgument("name");
        int id = (int) message.getArgument("id");
        int resourceID = (int) message.getArgument("resourceID");
        double xCoordinate = (double) message.getArgument("xCoordinate");
        double yCoordinate = (double) message.getArgument("yCoordinate");
        Vector2D vector2D = new Vector2D(xCoordinate, yCoordinate);

		WorldObject newlyVisible = new WorldObject(name);
		newlyVisible.getIdentification().setId(id);
		newlyVisible.getIdentification().setTypeId(resourceID);
		newlyVisible.getGeometry2D().setPosition(vector2D);

		World world = getContext().getWorld();
		world.scheduleUpdateTask(() -> world.getWorldObjects().set
			  (newlyVisible.getIdentification().getId(), newlyVisible));
		return null;
	}
}
