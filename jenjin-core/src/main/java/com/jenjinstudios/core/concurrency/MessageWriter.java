package com.jenjinstudios.core.concurrency;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageOutputStream;

import java.io.IOException;
import java.security.Key;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MessageWriter class handles a timer which attempts to write all enqueued messages once every 10 milliseconds.
 * This timer can be started by calling the {@code start} method. If an IO error is encountered during the message
 * writes, writing is halted and the {@code isErrored} method will return true.  The timer will continue to attempt to
 * run until {@code stop} is called, and if it runs again successfully, {@code isErrored} will return false.
 *
 * @author Caleb Brinkman
 */
public class MessageWriter
{
	private static final Logger LOGGER = Logger.getLogger(MessageWriter.class.getName());
	private final MessageOutputStream outputStream;
	private final ScheduledExecutorService executorService;
	private final WriteTask writeTask;
	private final MessageContext context;
	private volatile boolean errored;

	/**
	 * Construct a MessageWriter that will write messages to the given MessageOutputStream.
	 *
	 * @param outputStream The output stream to which this writer will write.
	 * @param context The context in which messages should be executed.
	 */
	public MessageWriter(MessageOutputStream outputStream, MessageContext context) {
		this.outputStream = outputStream;
		this.context = context;
		writeTask = new WriteTask();
		executorService = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Returns true if the timer writing messages has encountered and error, and not run again successfully since then.
	 *
	 * @return True if the timer writing messages has encountered and error, and not run again successfully since then.
	 */
	public boolean isErrored() { return errored; }

	/**
	 * Begin writing messages to the output stream.
	 */
	public void start() { executorService.scheduleWithFixedDelay(writeTask, 0, 10, TimeUnit.MILLISECONDS); }

	/**
	 * Stop writing messages to the output stream.  Once this has been called, the timer may not be restarted.
	 */
	public void stop() {
		executorService.shutdown();
		try
		{
			outputStream.close();
		} catch (IOException e)
		{
			LOGGER.log(Level.FINE, "Exception when closing output stream", e);
		}
	}

	private class WriteTask implements Runnable
	{
		@Override
		public void run() {
			Deque<Message> outgoing = (context != null) ? context.getOutgoing() : new LinkedList<>();
			boolean noErrorThisExecution = true;
			while (!outgoing.isEmpty() && noErrorThisExecution)
			{
				noErrorThisExecution = sendNextMessage(outgoing);
			}
		}

		protected boolean sendNextMessage(Deque<Message> outgoing) {
			boolean noErrorThisExecution = true;
			Message message = outgoing.removeFirst();
			Key encryptionKey = (context != null) ? context.getEncryptionKey() : null;
			try
			{
				if (encryptionKey != null)
				{
					outputStream.writeMessage(message, encryptionKey);
				} else
				{
					outputStream.writeMessage(message);
				}
				errored = false;
			} catch (IOException e)
			{
				LOGGER.log(Level.WARNING, "MessageWriter encountered an error while writing message", e);
				outgoing.addFirst(message);
				errored = true;
				noErrorThisExecution = false;
			}
			return noErrorThisExecution;
		}
	}
}
