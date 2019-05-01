package edu.wpi.SimplePacketComs.device.gameController;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.device.UdpDevice;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;

public class GameController extends UdpDevice{
	private PacketType gamestate = new BytePacketType(1970, 64);
	private final byte[] status = new byte[60];
	private final byte[] data = new byte[20];
	private final int[] dataI = new int[20];
	private GameController(InetAddress add) throws Exception {
		super(add);
		setup();
	}
	private void setup() throws InterruptedException {
		addPollingPacket(gamestate);
		addEvent(gamestate.idOfCommand, new Runnable() {
			@Override
			public void run() {
				readBytes(gamestate.idOfCommand, data);
				for(int i=0;i<data.length-1;i++) {
					dataI[i]=data[i+1];
					if(dataI[i]<0)
						dataI[i]+=256;
				}
				writeBytes(gamestate.idOfCommand, getStatus());
			}
		});
		connect();
		int i=0;
		while(getName().getBytes().length==0 && i++<10){
			System.out.println("Waiting for device name...");
			Thread.sleep(100);// wait for the name packet to be sent
			//String n = control.getName();
		}
	}
	public GameController(String string) throws Exception {
		super(string);
		setup();
	}
	public static List<GameController> get(String name) throws Exception {
		HashSet<InetAddress> addresses = UDPSimplePacketComs.getAllAddresses(name);
		ArrayList<GameController> robots = new ArrayList<>();
		if (addresses.size() < 1) {
			System.out.println("No GameControllers found named "+name);
			return robots;
		}
		for (InetAddress add : addresses) {
			System.out.println("Got " + add.getHostAddress());
			GameController e = new GameController(add);
			robots.add(e);
		}
		return robots;
	}
	public static List<GameController> get() throws Exception {
		return get("GameController*");
	}
	public byte[] getStatus() {
		return status;
	}
	public int[] getData() {
		return dataI;
	}
	public byte getControllerIndex() {
		return data[0];
	}
}
