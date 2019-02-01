package edu.wpi.SimplePacketComs;

import java.util.ArrayList;
import java.util.HashMap;

import edu.wpi.SimplePacketComs.device.Device;

public abstract class AbstractSimpleComsDevice implements Device, IPhysicalLayer {
	HashMap<Integer, ArrayList<Runnable>> events = new HashMap<>();
	HashMap<Integer, ArrayList<Runnable>> toRemove = new HashMap<>();

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
		if (toRemove.get(id) == null) {
			toRemove.put(id, new ArrayList<Runnable>());
		}
		toRemove.get(id).add(event);
	}

	public void addEvent(Integer id, Runnable event) {
		if (events.get(id) == null) {
			events.put(id, new ArrayList<Runnable>());
		}
		events.get(id).add(event);
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
		try {
			if (!isVirtual()) {
				try {
					byte[] message = packet.command(packet.idOfCommand, packet.getDownstream());
					// println "Writing: "+ message
					int val = write(message, message.length, 1);
					if (val > 0) {
						int read = read(message, getReadTimeout());
						if (read >= packet.getUpstream().length) {
							// println "Parsing packet"
							// println "read: "+ message
							int ID = PacketType.getId(message);
							if (ID == packet.idOfCommand) {
								if (isTimedOut) {
									System.out.println("Timout resolved " + ID);
								}
								isTimedOut = false;
								Number[] up = packet.parse(message);
								for (int i = 0; i < packet.getUpstream().length; i++) {
									packet.getUpstream()[i] = up[i];
								}
								// System.out.println("Took "+(System.currentTimeMillis()-start));
							} else {
								// readTimeout=readTimeout+(readTimeout/2);

								// System.out.print("\r\nCross Talk expected " + packet.idOfCommand + " Got: " +
								// ID+" waiting "+readTimeout);

								for (int i = 0; i < 3; i++) {
									read(message, getReadTimeout());// clear any possible stuck messages

								}
								System.out.println(" ");
								isTimedOut = true;
								return;
							}
						} else {
							// System.out.println("Read failed");
							// readTimeout=readTimeout+(readTimeout/2);
							isTimedOut = true;
							return;
						}

					} else
						return;
				} catch (Throwable t) {
					t.printStackTrace(System.out);
					disconnect();
				}
			} else {
				// println "Simulation"
				for (int j = 0; j < packet.getDownstream().length && j < packet.getUpstream().length; j++) {
					packet.getUpstream()[j] = packet.getDownstream()[j];
				}

			}
			// println "updaing "+upstream+" downstream "+downstream

			if (events.get(packet.idOfCommand) != null) {
				if (toRemove.get(packet.idOfCommand) != null)
					if (toRemove.get(packet.idOfCommand).size() > 0) {
						for (Runnable e : toRemove.get(packet.idOfCommand)) {
							events.get(packet.idOfCommand).remove(e);
						}
						toRemove.get(packet.idOfCommand).clear();
					}
				for (Runnable e : events.get(packet.idOfCommand)) {
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
			// t.printStackTrace(System.out);
		}
		packet.done = true;
	}

	private int getReadTimeout() {

		return readTimeout;
	}

	public boolean connect() {
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
							if (pollingPacket.sendOk())
								process(pollingPacket);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(1);
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

}
