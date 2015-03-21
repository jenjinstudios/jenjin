package com.jenjinstudios.server.net;

import com.jenjinstudios.core.Connection;
import com.jenjinstudios.server.authentication.Authenticator;

import java.io.IOException;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base Server class for implementation of the JGSA.  It contains extensible execution functionality designed to be
 * used by Executable Messages from ClientHandlers.
 *
 * @author Caleb Brinkman
 */
public class Server extends Thread
{
    /** The logger used by this class. */
    public static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    /** The updates per second. */
    protected final int UPS;
    /** The period of the update in milliseconds. */
    protected final int PERIOD;
	protected final Authenticator authenticator;
	protected final List<Runnable> repeatedTasks;
	protected final Deque<Runnable> syncedTasks;
	/** The list of {@code ClientListener}s working for this server. */
	private final ClientListener clientListener;
    /** The list of {@code ClientHandler}s working for this server. */
    private final Map<Integer, ClientHandler> clientHandlers;
    private final KeyPair rsaKeyPair;
	private ScheduledExecutorService loopTimer;
	private ServerUpdateTask serverUpdateTask;

	/**
	 * Construct a new Server without a SQLHandler.
     *
     * @param initInfo The initialization object to use when constructing the server.
     *
     * @throws java.io.IOException If there is an IO Error initializing the server.
     * @throws NoSuchMethodException If there is no appropriate constructor for the specified ClientHandler
     * constructor.
     */
    @SuppressWarnings("unchecked")
	protected Server(ServerInit initInfo, Authenticator authenticator) throws IOException, NoSuchMethodException {
		super("Server");
        LOGGER.log(Level.FINE, "Initializing Server.");
        UPS = initInfo.getUps();
        PERIOD = 1000 / UPS;
        clientListener = new ClientListener(getClass(), initInfo.getHandlerClass(), initInfo.getPort());
        clientHandlers = new TreeMap<>();
        rsaKeyPair = initInfo.getKeyPair() == null ? Connection.generateRSAKeyPair() : initInfo.getKeyPair();
		this.authenticator = authenticator;
		repeatedTasks = new LinkedList<>();
		syncedTasks = new LinkedList<>();
	}

    /**
     * Add new clients that have connected to the client listeners.
     */
    public void checkListenerForClients() {
		Iterable<ClientHandler> nc = clientListener.getNewClients();
		for (ClientHandler h : nc)
        {
            addClientHandler(h);
            h.start();
        }

    }

	public void runRepeatedTasks() {
		synchronized (repeatedTasks)
		{
			repeatedTasks.forEach(Runnable::run);
		}
	}

	public void runSyncedTasks() {
		synchronized (syncedTasks)
		{
			while (!syncedTasks.isEmpty()) { syncedTasks.remove().run(); }
		}
	}

	public int getUps() { return UPS; }

	public long getCycleStartTime() {
		return (serverUpdateTask != null) ? serverUpdateTask.getCycleStartTime() : -1;
	}

	protected void addRepeatedTask(Runnable r) {
		synchronized (repeatedTasks)
		{
			repeatedTasks.add(r);
		}
	}

	private void addClientHandler(ClientHandler h) {
		int nullIndex = 0;
        synchronized (clientHandlers)
        {
            while (clientHandlers.containsKey(nullIndex)) nullIndex++;
            clientHandlers.put(nullIndex, h);
        }
        h.setHandlerId(nullIndex);
        h.setRSAKeyPair(rsaKeyPair);
    }

    /**
     * Run all messages currently waiting in ClientHandler queues.
     */
    public void runClientHandlerQueuedMessages() {
        synchronized (clientHandlers)
        {
            Collection<ClientHandler> handlers = clientHandlers.values();
            handlers.stream().
                  filter(current -> current != null).
                  forEach(c -> c.getExecutableMessageQueue().runQueuedExecutableMessages());
        }
    }

    /** Broadcast all outgoing messages to clients. */
    public void broadcast() {
        synchronized (clientHandlers)
        {
			Collection<ClientHandler> toShutdown = new LinkedList<>();
			clientHandlers.values().stream().forEach(c -> {
				if (c != null)
				{
					try
					{
						c.getMessageIO().writeAllMessages();
					} catch (IOException ignored)
					{
						toShutdown.add(c);
					}
				}
			});
			toShutdown.forEach(ClientHandler::shutdown);
		}
	}

    /** Update all clients before they sendAllMessages. */
    public void update() {
        synchronized (clientHandlers)
        {
            Set<Integer> integers = clientHandlers.keySet();
            for (int i : integers)
            {
                ClientHandler t = clientHandlers.get(i);
                if (t != null)
                {
                    t.update();
                }
            }
        }
    }

	public Authenticator getAuthenticator() { return authenticator; }

	/** Run the server. */
	@Override
    public void run() {
        clientListener.startListening(this);

		serverUpdateTask = new ServerUpdateTask(this);

		loopTimer = Executors.newSingleThreadScheduledExecutor(new ServerUpdateThreadFactory());
		loopTimer.scheduleAtFixedRate(serverUpdateTask, 0, PERIOD, TimeUnit.MILLISECONDS);
	}

    /**
     * Shutdown the server, forcing all client links to close.
     *
     * @throws IOException if there is an error shutting down a client.
     */
	public void shutdown() throws IOException {
		synchronized (clientHandlers)
        {
            Set<Integer> integers = clientHandlers.keySet();
            for (int i : integers)
            {
                ClientHandler t = clientHandlers.get(i);
                if (t != null)
                {
                    t.shutdown();
                }
            }
        }
        clientListener.stopListening();

		if (loopTimer != null)
			loopTimer.shutdown();
	}

    /**
     * Schedule a client to be removed during the next update.
     *
     * @param handler The client handler to be removed.
     */
    protected void removeClient(ClientHandler handler) {
        synchronized (clientHandlers)
        {
            clientHandlers.remove(handler.getHandlerId());
        }
    }
}
