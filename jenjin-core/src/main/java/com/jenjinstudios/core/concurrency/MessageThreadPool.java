package com.jenjinstudios.core.concurrency;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageStreamPair;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all four threads necessary for proper, asynchronous message IO.
 *
 * @author Caleb Brinkman
 */
@SuppressWarnings("CyclicClassDependency")
public class MessageThreadPool<T extends MessageContext>
{
	private static final Logger LOGGER = Logger.getLogger(MessageThreadPool.class.getName());
	private final Collection<ShutdownTask> shutdownTasks;
	private final String id = UUID.randomUUID().toString();
	private final MessageStreamPair messageStreamPair;
	private final MessageExecutor messageExecutor;
	private final MessageReader messageReader;
	private final MessageWriter messageWriter;
	private final ErrorChecker errorChecker;
	private final T messageContext;
	private boolean running;

	/**
	 * Construct a MessageThreadPool whose threads will read from and write to the given MessageIO streams.
	 *
	 * @param messageStreamPair The MessageIO containing the streams to read/write.
	 * @param context The context in which messages in this thread pool should execute messages.
	 */
	protected MessageThreadPool(MessageStreamPair messageStreamPair, T context) {
		this.messageStreamPair = messageStreamPair;
		this.messageContext = context;
		messageWriter = new MessageWriter(messageStreamPair.getOut(), context);
		messageReader = new MessageReader(messageStreamPair.getIn());
		errorChecker = new ErrorChecker();
		messageExecutor = new MessageExecutor(this);
		messageExecutor.setMessageContext(context);
		shutdownTasks = new LinkedList<>();
	}

	/**
	 * Get the message context in which messages received by this thread pool will be executed.
	 *
	 * @return The message context.
	 */
	public T getMessageContext() { return messageContext; }

	/**
	 * Start the message reader thread managed by this connection.
	 */
	public void start() {
		messageReader.start();
		messageWriter.start();
		errorChecker.start();
		messageExecutor.start();
		running = true;
	}

	/**
	 * End this connection's execution loop and close any streams.
	 */
	public void shutdown() {
		LOGGER.log(Level.INFO, "Shutting down thread pool");
		messageWriter.stop();
		messageReader.stop();
		errorChecker.stop();
		messageExecutor.stop();
		shutdownTasks.forEach(task -> task.shutdown(this));
		running = false;
	}

	/**
	 * Add a shutdown task to the collection of tasks that will be executed when this thread pool shuts down.
	 *
	 * @param task The task to be executed.
	 */
	public void addShutdownTask(ShutdownTask task) { shutdownTasks.add(task); }

	/**
	 * Get the MessageIO containing the keys and streams used by this connection.
	 *
	 * @return The MessageIO containing the keys and streams used by this connection.
	 */
	public MessageStreamPair getMessageStreamPair() { return messageStreamPair; }

	/**
	 * Queue up the supplied message to be written.
	 *
	 * @param message The message to be sent.
	 */
	public void enqueueMessage(Message message) { messageContext.enqueue(message); }

	/**
	 * Get the unique ID of this MessageThreadPool.
	 *
	 * @return The unique ID of this MessageThreadPool.
	 */
	public String getId() { return id; }

	/**
	 * Return whether the threads managed by this pool are running.
	 *
	 * @return Whether the threads managed by this pool are running.
	 */
	public boolean isRunning() { return running; }

	/**
	 * Get the messages received by the MessageReader since the last time this method was called.
	 *
	 * @return The messages received since the last time this method was called.
	 */
	protected Iterable<Message> getReceivedMessages() { return messageReader.getReceivedMessages(); }

	private class ErrorChecker
	{
		private final CheckErrorsTask checkErrorTask = new CheckErrorsTask();
		private final Timer checkErrorTimer = new Timer("Error Checker");

		public void start() { checkErrorTimer.scheduleAtFixedRate(checkErrorTask, 0, 10); }

		public void stop() { checkErrorTimer.cancel(); }
	}

	private class CheckErrorsTask extends TimerTask
	{
		@Override
		public void run() {
			if (messageReader.isErrored() || messageWriter.isErrored())
			{
				LOGGER.log(Level.SEVERE, "Message reader or writer in error state; shutting down.");
				shutdown();
			}
		}
	}
}
