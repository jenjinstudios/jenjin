package com.jenjinstudios.world;

import com.jenjinstudios.net.AuthServer;
import com.jenjinstudios.world.sql.WorldSQLHandler;

import java.io.IOException;

/**
 * The WorldServer class is responsible for updating a game world.
 * @author Caleb Brinkman
 */
public class WorldServer extends AuthServer<WorldClientHandler>
{
	/** The default updates-per-second for the world server. */
	public static final int DEFAULT_UPS = 50;
	/** The default port used for the world server. */
	public static final int DEFAULT_PORT = 51015;
	/** The world used by this server. */
	private final World world;

	/**
	 * Construct a new WorldServer.
	 * @param sqlHandler The WorldSqlHandler used to communicate with the MySql Database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler constructor.
	 */
	public WorldServer(WorldSQLHandler sqlHandler) throws IOException, NoSuchMethodException {
		this(new World(), sqlHandler);
	}

	/**
	 * Construct a new Server without a SQLHandler.
	 * @param world The world to be used by this server.
	 * @param sqlHandler The WorldSqlHandler used to communicate with the MySql Database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler constructor.
	 */
	public WorldServer(World world, WorldSQLHandler sqlHandler) throws IOException, NoSuchMethodException {
		this(world, DEFAULT_PORT, sqlHandler);
	}

	/**
	 * Construct a new Server without a SQLHandler.
	 * @param world The world to be used by this server.
	 * @param port The port number on which this server will listen.
	 * @param sqlHandler The WorldSqlHandler used to communicate with the MySql Database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler constructor.
	 */
	public WorldServer(World world, int port, WorldSQLHandler sqlHandler) throws IOException, NoSuchMethodException {
		this(world, DEFAULT_UPS, port, sqlHandler);
	}

	/**
	 * Construct a new Server without a SQLHandler.
	 * @param world The world to be used by this server.
	 * @param ups The cycles per second at which this server will run.
	 * @param port The port number on which this server will listen.
	 * @param sqlHandler The WorldSqlHandler used to communicate with the MySql Database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler constructor.
	 */
	public WorldServer(World world, int ups, int port, WorldSQLHandler sqlHandler) throws IOException, NoSuchMethodException {
		this(world, ups, port, WorldClientHandler.class, sqlHandler);
	}

	/**
	 * Construct a new Server without a SQLHandler.
	 * @param worldToBeUsed The world to be used by this server.
	 * @param ups The cycles per second at which this server will run.
	 * @param port The port number on which this server will listen.
	 * @param wchClass The class of WorldClientHandler to use.
	 * @param sqlHandler The WorldSqlHandler used to communicate with the MySql Database.
	 * @throws java.io.IOException If there is an IO Error when initializing the server.
	 * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler constructor.
	 */
	public WorldServer(World worldToBeUsed, int ups, int port, Class<? extends WorldClientHandler> wchClass,
					   WorldSQLHandler sqlHandler) throws IOException, NoSuchMethodException
	{
		super(ups, port, wchClass, sqlHandler);
		this.world = worldToBeUsed;
		addRepeatedTask(new Runnable()
		{
			@Override
			public void run() {
				world.update();
			}
		});
	}

	/**
	 * Get the world used by this server.
	 * @return The world used by this server.
	 */
	public World getWorld() {
		return world;
	}

	@Override
	public WorldSQLHandler getSqlHandler() {
		return (WorldSQLHandler) super.getSqlHandler();
	}

}
