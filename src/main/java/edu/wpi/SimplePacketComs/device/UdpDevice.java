package edu.wpi.SimplePacketComs.device;

import java.net.InetAddress;

import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;

public abstract class UdpDevice extends UDPSimplePacketComs  implements Device{
	private PacketType getName = new BytePacketType(1776, 64);
	private InetAddress address;
	private byte[] name = new byte[60];
	public UdpDevice(String name ) throws Exception {
		this(getByName( name));
	}
	public 	UdpDevice(InetAddress add) throws Exception {
		super(add);
		this.address=add;
		getName.getDownstream()[0]=(byte)'*';// read name
		
		addEvent(getName.idOfCommand, new Runnable() {
			@Override
			public void run() {
				readBytes(getName.idOfCommand, name);// read name
			}
		});			
		addPollingPacket(getName);
		getName.oneShotMode();

	}

	public InetAddress getAddress() {
		return address;
	}

	public String getName() {
		return (new String(name)).trim();
	}
}
