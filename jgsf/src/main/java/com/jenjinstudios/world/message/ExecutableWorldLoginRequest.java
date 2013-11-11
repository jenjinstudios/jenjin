package com.jenjinstudios.world.message;

import com.jenjinstudios.message.Message;
import com.jenjinstudios.sql.WorldSQLHandler;
import com.jenjinstudios.world.Actor;
import com.jenjinstudios.world.InvalidLocationException;
import com.jenjinstudios.world.WorldClientHandler;

/**
 * Handles requests to login to the world.
 * @author Caleb Brinkman
 */
public class ExecutableWorldLoginRequest extends WorldExecutableMessage
{
	/** The SQL handler used by this executable message. */
	private final WorldSQLHandler sqlHandler;
	/** The player added to the world. */
	private Actor player;
	/** The LoginResponse to send to the client. */
	private Message loginResponse;

	/**
	 * Construct a new ExecutableMessage.  Must be implemented by subclasses.
	 * @param handler The handler using this ExecutableMessage.
	 * @param message The message.
	 */
	public ExecutableWorldLoginRequest(WorldClientHandler handler, Message message) {
		super(handler, message);
		sqlHandler = handler.getServer().getSqlHandler();
	}

	@Override
	public void runSynced() {
		if (player != null)
		{
			try
			{
				getClientHandler().getServer().getWorld().addObject(player);
			} catch (InvalidLocationException ex)
			{
				loginResponse.setArgument("success", false);
				loginResponse.setArgument("id", -1);
				loginResponse.setArgument("loginTime", getClientHandler().getLoggedInTime());
				loginResponse.setArgument("xCoord", 0d);
				loginResponse.setArgument("zCoord", 0d);
			}
		}
		getClientHandler().queueMessage(loginResponse);

	}

	@Override
	public void runASync() {
		boolean success;
		if (sqlHandler != null && !getClientHandler().isLoggedIn())
		{
			String username = (String) getMessage().getArgument("username");
			String password = (String) getMessage().getArgument("password");
			/* The map used to create the player. */
			player = sqlHandler.logInPlayer(username, password);
		}

		success = player != null;
		getClientHandler().setLoginStatus(success);

		loginResponse = new Message("WorldLoginResponse");
		loginResponse.setArgument("success", success);

		if (success)
		{
			getClientHandler().setActor(player);
			loginResponse.setArgument("loginTime", getClientHandler().getLoggedInTime());
			loginResponse.setArgument("xCoord", player.getVector2D().getXCoordinate());
			loginResponse.setArgument("zCoord", player.getVector2D().getZCoordinate());
			loginResponse.setArgument("id", player.getId());
		} else
		{
			loginResponse.setArgument("id", -1);
			loginResponse.setArgument("loginTime", getClientHandler().getLoggedInTime());
			loginResponse.setArgument("xCoord", 0d);
			loginResponse.setArgument("zCoord", 0d);
		}


	}
}
