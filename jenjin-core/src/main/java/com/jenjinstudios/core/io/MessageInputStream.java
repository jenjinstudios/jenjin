package com.jenjinstudios.core.io;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads messages registered with the MessageRegistry class from stream.
 * @author Caleb Brinkman
 */
public class MessageInputStream extends DataInputStream
{
	/** The Logger for this class. */
	private static final Logger LOGGER = Logger.getLogger(MessageInputStream.class.getName());
	/** The Connection using this stream. */
	private final MessageRegistry messageRegistry;
	/** The cipher used to decrypt messages. */
	private Cipher decryptCipher;
	private boolean closed;

	/**
	 * Construct a new {@code MessageInputStream} from the given InputStream.
	 * @param inputStream The InputStream from which messages will be read.
	 */
	public MessageInputStream(InputStream inputStream) {
		super(inputStream);
		this.messageRegistry = MessageRegistry.getInstance();
	}

	/**
	 * Read a Message or subclass from the DataStream.
	 * @return The Message constructed form the data stream.
	 */
	public Message readMessage() throws IOException {
		if (closed)
		{
			throw new IOException("Stream closed");
		}
		short id = readShort();
		LinkedList<Class> classes = messageRegistry.getArgumentClasses(id);
		Class<?>[] classArray = new Class[classes.size()];
		classes.toArray(classArray);
		Object[] args = readMessageArgs(classes);
		return new Message(messageRegistry, id, args);

	}

	/**
	 * Close the input stream.
	 * @throws java.io.IOException If there is an IO error.
	 */
	@Override
	public void close() throws IOException {
		super.close();
		closed = true;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		try
		{
			decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			LOGGER.log(Level.SEVERE, "Unable to create cipher, messages will not be decrypted.", e);
		}
	}

	/**
	 * Read from the DataInputStream an array of Objects to be passed as argumentTypes to a message.
	 * @param classes The class names of the argumentTypes to be read.
	 * @return An Object[] containing the message argumentTypes.
	 * @throws IOException If there is an IO error
	 */
	@SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
	private Object[] readMessageArgs(LinkedList<Class> classes) throws IOException {
		Object[] args = new Object[classes.size()];

		for (int i = 0; i < args.length; i++)
		{
			String currentClass = classes.pop().getName();
			switch (currentClass)
			{
				case "java.lang.String":
					args[i] = readString();
					break;
				case "int":
				case "java.lang.Integer":
					args[i] = readInt();
					break;
				case "java.lang.Long":
				case "long":
					args[i] = readLong();
					break;
				case "double":
				case "java.lang.Double":
					args[i] = readDouble();
					break;
				case "float":
				case "java.lang.Float":
					args[i] = readFloat();
					break;
				case "short":
				case "java.lang.Short":
					args[i] = readShort();
					break;
				case "boolean":
				case "java.lang.Boolean":
					args[i] = readBoolean();
					break;
				case "byte":
				case "java.lang.Byte":
					args[i] = readByte();
					break;
				case "[Ljava.lang.Byte;":
				case "[B":
					args[i] = readByteArray();
					break;
				case "[Ljava.lang.String;":
					args[i] = readStringArray();
					break;
			}
		}

		return args;
	}

	/**
	 * Read an array of strings from the DataInputStream.
	 * @return The read array of strings.
	 * @throws IOException If there is an error reading an array of strings.
	 */
	private String[] readStringArray() throws IOException {
		String[] strings;
		int size = readInt();
		strings = new String[size];
		for (int i = 0; i < strings.length; i++)
			strings[i] = readString();
		return strings;
	}

	/**
	 * Read an array of bytes from the DataInputStream.
	 * @return The read array of bytes.
	 * @throws IOException If there is an error reading an array of bytes.
	 */
	private byte[] readByteArray() throws IOException {
		byte[] bytes;
		int size = readInt();
		bytes = new byte[size];
		int numBytesRead = 0;
		while (numBytesRead < size)
		{
			numBytesRead += read(bytes, numBytesRead, size - numBytesRead);
		}
		if (numBytesRead != size) throw new IOException("Incorrect number of bytes read for byte array:" +
			  "Expected " + size + ", got " + numBytesRead);
		return bytes;
	}

	/**
	 * Read a string from the input stream, determining if it is encrypted and decrypting it if necessary.
	 * @return The read, decrypted string.
	 * @throws IOException If there is an IO error.
	 */
	private String readString() throws IOException {
		boolean encrypted = readBoolean();
		String received = readUTF();
		if (encrypted)
		{
			if (decryptCipher != null)
			{
				received = decryptString(received);
			} else
			{
				LOGGER.log(Level.SEVERE, "AES key not properly set, unable to decrypt messages.");
			}
		}
		return received;
	}

	private String decryptString(String encrypted) throws UnsupportedEncodingException {
		String decrypted = encrypted;
		try
		{
			byte[] encBytes = DatatypeConverter.parseHexBinary(encrypted);
			byte[] decBytes = decryptCipher.doFinal(encBytes);
			decrypted = new String(decBytes, "UTF-8");
		} catch (IllegalBlockSizeException | BadPaddingException e)
		{
			LOGGER.log(Level.WARNING, "Unable to decrypt message: ", e);
		}
		return decrypted;
	}

}
