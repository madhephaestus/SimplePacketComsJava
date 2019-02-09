package edu.wpi.SimplePacketComs;

import java.util.ArrayList;
import java.util.HashMap;

import edu.wpi.SimplePacketComs.device.Device;

public abstract class AbstractSimpleComsDevice implements Device, IPhysicalLayer {
	private HashMap<Integer, ArrayList<Runnable>> timeouts = new HashMap<>();
	private HashMap<Integer, ArrayList<Runnable>> timeoutsToRemove = new HashMap<>();
	private HashMap<Integer, ArrayList<Runnable>> events = new HashMap<>();
	private HashMap<Integer, ArrayList<Runnable>> toRemove = new HashMap<>();

	boolean connected = false;

	ArrayList<PacketType> pollingQueue = new ArrayList<PacketType>();

	private boolean virtual = false;
	private String name = "SimpleComsDevice";

	public abstract int read(byte[] message, int howLongToWaitBeforeTimeout);

	public abstract int write(byte[] message, int length, int howLongToWaitBeforeTimeout);

	public abstract boolean disconnectDeviceImp();

	public abstract boolean connectDeviceImp();

	private int readTimeout = 100;
	private boolean isTimedOut = false;

	public void addPollingPacket(PacketType packet) {
		if (getPacket(packet.idOfCommand) != null)
			throw new RuntimeException("Only one packet of a given ID is allowed to poll. Add an event to recive data");

		pollingQueue.add(packet);

	}

	public PacketType getPacket(Integer ID) {
		for (PacketType q : pollingQueue) {
			if (q.idOfCommand == ID.intValue()) {
				return q;
			}
		}
		return null;
	}

	public void removeEvent(Integer id, Runnable event) {

		getToRemove(id).add(event);
	}

	public void addEvent(Integer id, Runnable event) {

		getEvents(id).add(event);
	}

	public void removeTimeout(Integer id, Runnable event) {

		getTimeoutsToRemove(id).add(event);
	}

	public void addTimeout(Integer id, Runnable event) {

		getTimeouts(id).add(event);
	}

	public ArrayList<Integer> getIDs() {
		ArrayList<Integer> ids = new ArrayList<>();
		for (int j = 0; j < pollingQueue.size(); j++) {
			PacketType pt = pollingQueue.get(j);
			ids.add(pt.idOfCommand);
		}
		return ids;
	}

	public void writeFloats(int id, double[] values) {
		if (getPacket(id) == null) {
			FloatPacketType pt = new FloatPacketType(id, 64);
			for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
				pt.getDownstream()[i] = (float) values[i];
			}
			addPollingPacket(pt);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			for (int j = 0; j < pollingQueue.size(); j++) {
				PacketType pt = pollingQueue.get(j);
				if (FloatPacketType.class.isInstance(pt))
					if (pt.idOfCommand == id) {
						for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
							pt.getDownstream()[i] = (float) values[i];
						}
						return;
					}
			}
	}

	public void writeBytes(int id, byte[] values) {
		if (getPacket(id) == null) {
			BytePacketType pt = new BytePacketType(id, 64);
			for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
				pt.getDownstream()[i] = (byte) values[i];
			}
			addPollingPacket(pt);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			for (int j = 0; j < pollingQueue.size(); j++) {
				PacketType pt = pollingQueue.get(j);
				if (BytePacketType.class.isInstance(pt))

					if (pt.idOfCommand == id) {
						for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
							pt.getDownstream()[i] = (byte) values[i];
						}

						return;
					}
			}
	}

	public void writeFloats(Integer id, Double[] values) {
		writeFloats(id, values, true);
	}

	public void writeFloats(Integer id, Double[] values, Boolean polling) {
		if (getPacket(id) == null) {
			FloatPacketType pt = new FloatPacketType(id, 64);
			if (!polling)
				pt.oneShotMode();
			for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
				pt.getDownstream()[i] = values[i].floatValue();
			}
			addPollingPacket(pt);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			for (int j = 0; j < pollingQueue.size(); j++) {
				PacketType pt = pollingQueue.get(j);
				if (FloatPacketType.class.isInstance(pt))
					if (pt.idOfCommand == id) {
						for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
							pt.getDownstream()[i] = values[i].floatValue();
						}
						if (!polling)
							pt.oneShotMode();
						return;
					}
			}
	}

	public void writeBytes(Integer id, Byte[] values) {
		writeBytes(id, values, true);
	}

	public void writeBytes(Integer id, Byte[] values, Boolean polling) {
		if (getPacket(id) == null) {
			PacketType pt = new BytePacketType(id, 64);
			if (!polling)
				pt.oneShotMode();
			for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
				pt.getDownstream()[i] = values[i].byteValue();
			}
			addPollingPacket(pt);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else

			for (int j = 0; j < pollingQueue.size(); j++) {
				PacketType pt = pollingQueue.get(j);
				if (BytePacketType.class.isInstance(pt))

					if (pt.idOfCommand == id) {
						for (int i = 0; i < pt.getDownstream().length && i < values.length; i++) {
							pt.getDownstream()[i] = values[i].byteValue();
						}
						if (!polling)
							pt.oneShotMode();
						return;
					}
			}
	}

	public Double[] readFloats(Integer id) {
		if (getPacket(id) == null) {
			addPollingPacket(new FloatPacketType(id, 64));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PacketType pt = getPacket(id);
		Double[] values = new Double[pt.getUpstream().length];
		for (int i = 0; i < pt.getUpstream().length && i < values.length; i++) {
			values[i] = pt.getUpstream()[i].doubleValue();
		}
		return values;
	}

	public Byte[] readBytes(Integer id) {
		if (getPacket(id) == null) {
			addPollingPacket(new BytePacketType(id, 64));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PacketType pt = getPacket(id);
		Byte[] values = new Byte[pt.getUpstream().length];
		for (int i = 0; i < pt.getUpstream().length && i < values.length; i++) {
			values[i] = pt.getUpstream()[i].byteValue();
		}
		return values;
	}

	public void readFloats(int id, double[] values) {
		if (getPacket(id) == null) {
			addPollingPacket(new FloatPacketType(id, 64));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int j = 0; j < pollingQueue.size(); j++) {
			PacketType pt = pollingQueue.get(j);
			if (FloatPacketType.class.isInstance(pt))

				if (pt.idOfCommand == id) {
					for (int i = 0; i < pt.getUpstream().length && i < values.length; i++) {
						float d = (float) pt.getUpstream()[i];
						values[i] = d;
					}
					return;
				}
		}
	}

	public void readBytes(int id, byte[] values) {
		if (getPacket(id) == null) {
			addPollingPacket(new BytePacketType(id, 64));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int j = 0; j < pollingQueue.size(); j++) {
			PacketType pt = pollingQueue.get(j);
			if (BytePacketType.class.isInstance(pt))
				if (pt.idOfCommand == id) {
					for (int i = 0; i < pt.getUpstream().length && i < values.length; i++) {
						values[i] = (byte) pt.getUpstream()[i];
					}
					return;
				}
		}
	}

	private void process(PacketType packet) {
		packet.started = true;
		long start = System.currentTimeMillis();
		int myID = packet.idOfCommand;
		int retry = 3;
		setReadTimeout(20);
		try {
			if (!isVirtual()) {
				boolean resend = true;
				for (int i = 0; i < retry; i++) {// retry loop
					int val = PacketType.packetSize;
					byte[] message=packet.command();;
					if (resend) {					
						try {
							val = write(message, message.length, 1);
							resend = false;
						} catch (Throwable t) {
							t.printStackTrace(System.out);
							disconnect();
						}
					}
					if (val > 0) {
						int read = 0;
						try {
							read = read(message, getReadTimeout()/retry);
						} catch (Throwable t) {
							t.printStackTrace(System.out);
							disconnect();
						}
						
						if (read >= PacketType.packetSize) {
							// println "Parsing packet"
							// println "read: "+ message
							int ID = PacketType.getId(message);
							
							if (ID == myID) {
								Number[] up = packet.parse(message);
								for (int j = 0; j < packet.getUpstream().length; j++) {
									packet.getUpstream()[j] = up[j];
								}
								break;// pop out of the retry loop
								// System.out.println("Took "+(System.currentTimeMillis()-start));
							} 
						}
					}

				}

			} else {
				// println "Simulation"
				for (int j = 0; j < packet.getDownstream().length && j < packet.getUpstream().length; j++) {
					packet.getUpstream()[j] = packet.getDownstream()[j];
				}

			}
			long commandDone = System.currentTimeMillis();

			ArrayList<Runnable> toRem = getTimeoutsToRemove(packet.idOfCommand);

			if (toRem.size() > 0) {
				for (Runnable e : toRem) {
					getTimeouts(packet.idOfCommand).remove(e);
				}
				toRem.clear();
			}
			if (getToRemove(packet.idOfCommand).size() > 0) {
				for (Runnable e : getToRemove(packet.idOfCommand)) {
					getEvents(packet.idOfCommand).remove(e);
				}
				getToRemove(packet.idOfCommand).clear();
			}
			if (!isTimedOut) {
				for (Runnable e : getEvents(packet.idOfCommand)) {
					if (e != null) {
						try {
							e.run();
						} catch (Throwable t) {
							t.printStackTrace(System.out);

						}
					}
				}
			}
			long eventDone = System.currentTimeMillis();
			long totalDuration = eventDone - start;
			long commandDuration = commandDone - start;
			long eventDuration = eventDone - commandDone;
			if (totalDuration > getReadTimeout()) {
				isTimedOut = true;
			}else
				isTimedOut = false;
			if (isTimedOut) {
				System.out.println("Timeout on command " + myID + " took " + totalDuration
						+ " should have taken " + getReadTimeout() + " command took " + commandDuration
						+ " event handlers took " + eventDuration);
				for (Runnable e : getTimeouts(packet.idOfCommand)) {
					if (e != null) {
						try {
							e.run();
						} catch (Throwable t) {
							t.printStackTrace(System.out);
						}
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
		packet.done = true;
	}

	private int getReadTimeout() {

		return readTimeout;
	}

	public boolean connect() {
		if(connected)
			return true;
		if (connectDeviceImp()) {
			setVirtual(false);
		} else {
			setVirtual(true);
		}

		connected = true;
		new Thread() {
			public void run() {
				// println "Starting HID Thread"
				while (connected) {

					// println "loop"
					try {

						for (int i = 0; i < pollingQueue.size(); i++) {
							PacketType pollingPacket = pollingQueue.get(i);
							if (pollingPacket.sendOk()) {
								long start = System.currentTimeMillis();
								process(pollingPacket);
								long end = System.currentTimeMillis();
								long took = (end - start);
								// if(took >getReadTimeout())
								// System.out.println("Loop took "+(end-start)+" on ID
								// "+pollingPacket.idOfCommand);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(0, 1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						connected = false;
					}
				}
				disconnectDeviceImp();
				System.out.println("SimplePacketComs disconnect");
			}
		}.start();
		// throw new RuntimeException("No HID device found")
		return true;
	}

	public void disconnect() {
		connected = false;
	}

	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
		//new RuntimeException(" Setting timeout to " + readTimeout).printStackTrace();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isTimedOut() {
		return isTimedOut;
	}

	private ArrayList<Runnable> getTimeoutsToRemove(int index) {
		if (timeoutsToRemove.get(index) == null)
			timeoutsToRemove.put(index, new ArrayList<Runnable>());
		return timeoutsToRemove.get(index);
	}

	private ArrayList<Runnable> getToRemove(int index) {
		if (toRemove.get(index) == null)
			toRemove.put(index, new ArrayList<Runnable>());
		return toRemove.get(index);
	}

	private ArrayList<Runnable> getEvents(int index) {
		if (events.get(index) == null)
			events.put(index, new ArrayList<Runnable>());
		return events.get(index);
	}

	private ArrayList<Runnable> getTimeouts(int index) {
		if (timeouts.get(index) == null)
			timeouts.put(index, new ArrayList<Runnable>());
		return timeouts.get(index);
	}

}
