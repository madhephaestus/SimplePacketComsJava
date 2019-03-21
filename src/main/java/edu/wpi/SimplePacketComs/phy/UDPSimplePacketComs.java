package edu.wpi.SimplePacketComs.phy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;

import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice;
import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.PacketType;

public class UDPSimplePacketComs extends AbstractSimpleComsDevice {
	private static final byte[] BROADCAST = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
	public static final int PACKET_SIZE = 64;
	private InetAddress address = null;
	private static InetAddress broadcast;
	private static final HashSet<InetAddress> addrs = new HashSet<>();
	private static final HashMap<InetAddress, String> names = new HashMap<>();
	private DatagramSocket udpSock;
	private byte[] receiveData = new byte[PACKET_SIZE];
	private static final int port = 1865;
	private DatagramPacket receivePacket = new DatagramPacket(receiveData, PACKET_SIZE);
	private boolean listening = false;

	public UDPSimplePacketComs() {
		// this.address = address;
		listening = true;
	}

	public UDPSimplePacketComs(InetAddress address) throws Exception {
		this.address = address;
		listening = false;
	}

	public static HashSet<InetAddress> getAllAddresses() throws Exception {
		return getAllAddresses(null);
	}

	public static HashSet<InetAddress> getAllAddresses(String name) throws Exception {
		broadcast = InetAddress.getByAddress(BROADCAST);
		addrs.clear();
		UDPSimplePacketComs pinger = new UDPSimplePacketComs(broadcast);
		pinger.connect();
		BytePacketType namePacket = new BytePacketType(1776, PACKET_SIZE);
		if (name != null) {
			byte[] bytes = name.getBytes();
			for (int i = 0; i < namePacket.getDownstream().length && i < name.length(); i++)
				namePacket.getDownstream()[i] = bytes[i];
		} else {

			namePacket.getDownstream()[0] = new Byte((byte) '*');
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
		} catch (Exception e) {
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

		int len = receivePacket.getLength();
		byte[] data = receivePacket.getData();
		for (int i = 0; i < len; i++) {
			message[i] = data[i];
		}
		InetAddress tmp = receivePacket.getAddress();
		if (address.equals(broadcast)) {
			if (!addrs.contains(tmp)) {
				if (PacketType.getId(data) == 1776)
					addrs.add(tmp);// add new addresses only on response to a name request
			}
		}
		if (names.get(tmp) == null) {
			if (PacketType.getId(data) == 1776) {
				byte[] namedata = new byte[data.length - 4];
				for (int i = 0; i < namedata.length; i++) {
					namedata[i] = data[i + 4];
				}
				String name = new String(namedata).trim();
				names.put(tmp, name);
			}
		}
		if (address == null) {
			address = tmp;
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
			if (listening)
				udpSock = new DatagramSocket(port);
			else
				udpSock = new DatagramSocket();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String getName() {
		return names.get(address);
	}

	@Override
	public void setName(String name) {
		// this is a value read from the device
	}

}
