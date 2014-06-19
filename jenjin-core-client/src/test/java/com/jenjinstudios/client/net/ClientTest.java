package com.jenjinstudios.client.net;

import com.jenjinstudios.core.io.Message;
import com.jenjinstudios.core.io.MessageOutputStream;
import com.jenjinstudios.core.io.MessageRegistry;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Caleb Brinkman
 */
public class ClientTest
{
	@Test
	public void testAddRepeatedTask() {
		Runnable r = Mockito.mock(Runnable.class);
		Socket sock = Mockito.mock(Socket.class);
		Client client = new Client(sock);
		client.addRepeatedTask(r);
		client.runRepeatedTasks();
		Mockito.verify(r).run();
	}

	@Test
	public void testDoPostConnectInit() throws Exception {
		int ups = 100;
		int period = 1000 / ups;
		// Build a FirstConnectResponse message
		MessageRegistry mr = new MessageRegistry();
		Message fcr = mr.createMessage("FirstConnectResponse");
		fcr.setArgument("ups", ups);

		// Mock a stream containing a FirstConnectResponse
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MessageOutputStream mos = new MessageOutputStream(mr, bos);
		mos.writeMessage(fcr);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		// Mock a socket which returns the mocked stream.
		Socket sock = Mockito.mock(Socket.class);
		Mockito.when(sock.getInputStream()).thenReturn(bis);
		// Doesn't really matter, just has to have a valid stream
		Mockito.when(sock.getOutputStream()).thenReturn(bos);

		Client client = new Client(sock);
		client.run();

		Assert.assertEquals(client.getPeriod(), period);
	}

	@Test
	public void testRunFailure() throws Exception {
		int ups = 100;
		// Build a FirstConnectResponse message
		MessageRegistry mr = new MessageRegistry();
		Message fcr = mr.createMessage("FirstConnectResponse");
		fcr.setArgument("ups", ups);

		// Mock a stream containing a FirstConnectResponse
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MessageOutputStream mos = new MessageOutputStream(mr, bos);
		mos.writeMessage(fcr);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		// Mock a socket which returns the mocked stream.
		Socket sock = Mockito.mock(Socket.class);
		Mockito.when(sock.getInputStream()).thenReturn(bis);
		// Stream throws an IOException, causing run to exit before attempting to process messages.
		Mockito.when(sock.getOutputStream()).thenThrow(new IOException());

		Client client = new Client(sock);
		client.run();

		// We can verify that run() aborted early by checking to see if the period has been set
		// Since there is already a FirstConnectResponse waiting in the InputStream, if run does
		// not abort early, the period will be equal to 1000 / ups, as above.
		Assert.assertEquals(client.getPeriod(), 0);
	}

	@Test
	public void testBlockingStart() throws Exception {
		int ups = 100;
		MessageRegistry mr = new MessageRegistry();

		// Build a FirstConnectResponse message
		Message fcr = mr.createMessage("FirstConnectResponse");
		fcr.setArgument("ups", ups);

		// Mock a stream containing a FirstConnectResponse
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MessageOutputStream mos = new MessageOutputStream(mr, bos);
		mos.writeMessage(fcr);

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ByteArrayOutputStream clientOut = new ByteArrayOutputStream();
		Socket sock = Mockito.mock(Socket.class);
		Mockito.when(sock.getInputStream()).thenReturn(bis);
		Mockito.when(sock.getOutputStream()).thenReturn(clientOut);

		Client client = new Client(sock);

		// Nastiness.
		byte[] clientKey = client.getPublicKey().getEncoded();
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		byte[] aesKeyBytes = keyGenerator.generateKey().getEncoded();
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientKey));
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedAESKey = cipher.doFinal(aesKeyBytes);
		Message aesMessage = mr.createMessage("AESKeyMessage");
		// Construct the AESKeyMessage
		aesMessage.setArgument("key", encryptedAESKey);
		bos = new ByteArrayOutputStream();
		mos = new MessageOutputStream(mr, bos);
		mos.writeMessage(aesMessage);
		bis = new ByteArrayInputStream(bos.toByteArray());
		Mockito.when(sock.getInputStream()).thenReturn(bis);

		Assert.assertTrue(client.blockingStart());
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testInvalidDoPostConnectInit() throws Exception {
		int ups = 100;
		// Build a FirstConnectResponse message
		MessageRegistry mr = new MessageRegistry();
		Message fcr = mr.createMessage("FirstConnectResponse");
		fcr.setArgument("ups", ups);

		// Mock a stream containing a FirstConnectResponse
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MessageOutputStream mos = new MessageOutputStream(mr, bos);
		mos.writeMessage(fcr);
		mos.writeMessage(fcr);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		// Mock a socket which returns the mocked stream.
		Socket sock = Mockito.mock(Socket.class);
		Mockito.when(sock.getInputStream()).thenReturn(bis);
		// Doesn't really matter, just has to have a valid stream
		Mockito.when(sock.getOutputStream()).thenReturn(bos);

		Client client = new Client(sock);
		client.run();
	}
}
