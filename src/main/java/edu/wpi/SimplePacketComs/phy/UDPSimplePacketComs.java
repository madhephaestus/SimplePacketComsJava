package edu.wpi.SimplePacketComs.phy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice;
import edu.wpi.SimplePacketComs.bytepacket.BytePacketType;

public class UDPSimplePacketComs extends AbstractSimpleComsDevice {
	private static final byte[] BROADCAST = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
	public static final int PACKET_SIZE = 64;
	private InetAddress address;
	private static InetAddress broadcast;
	private static final HashSet<InetAddress> addrs = new HashSet<>();
	private DatagramSocket udpSock;
	private byte[] receiveData = new byte[PACKET_SIZE];
	private static final int port = 1865;
	private DatagramPacket receivePacket = new DatagramPacket(receiveData, PACKET_SIZE);
	private String name;

	public UDPSimplePacketComs(InetAddress address) throws Exception {
		this.address = address;
	}

	public static HashSet<InetAddress> getAllAddresses() throws Exception {
		return getAllAddresses(null);
	}

	public static HashSet<InetAddress> getAllAddresses(String name) throws Exception {
		broadcast = InetAddress.getByAddress(BROADCAST);
		addrs.clear();
		UDPSimplePacketComs pinger = new UDPSimplePacketComs(broadcast);
		pinger.connect();
		BytePacketType namePacket = new BytePacketType(1917, PACKET_SIZE);
		if (name != null) {
			byte[] bytes = name.getBytes();
			for (int i = 0; i < namePacket.downstream.length && i < name.length(); i++)
				namePacket.downstream[i] = bytes[i];
		}else {
			for (int i = 0; i < namePacket.downstream.length; i++)
				namePacket.downstream[i] = (byte)0xFF;
		}
		
		byte[] message = namePacket.command();
		pinger.write(message, message.length, 1000);
		for (int i = 0; i < 100; i++) {
			pinger.read(message, 2);// Allow all possible packets to be processed
			Thread.sleep(2);
		}
		pinger.disconnect();
		return addrs;
	}

	@Override
	public int read(byte[] message, int howLongToWaitBeforeTimeout) {

		try {
			udpSock.setSoTimeout(howLongToWaitBeforeTimeout);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Timeout the socket after 1 ms
		try {
			udpSock.receive(receivePacket);

		} catch (SocketTimeoutException ste) {
			return 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
		addrs.add(receivePacket.getAddress());
		int len = receivePacket.getLength();
		byte[] data = receivePacket.getData();
		for (int i = 0; i < len; i++) {
			message[i] = data[i];
		}

		return len;
	}

	@Override
	public int write(byte[] message, int length, int howLongToWaitBeforeTimeout) {

		DatagramPacket sendPacket = new DatagramPacket(message, length, address, port);
		// Log.info("Sending UDP packet: "+sendPacket);
		try {
			udpSock.send(sendPacket);
			return length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean disconnectDeviceImp() {
		udpSock.disconnect();
		return true;
	}

	@Override
	public boolean connectDeviceImp() {
		try {
			udpSock = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
