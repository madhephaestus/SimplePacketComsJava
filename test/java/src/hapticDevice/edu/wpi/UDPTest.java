package hapticDevice.edu.wpi;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.device.gameController.GameController;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;

public class UDPTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		HashSet<InetAddress> addresses = UDPSimplePacketComs.getAllAddresses("*");
		if (addresses.size() < 1) {
			fail("No devices found");
		}
		for (InetAddress add : addresses) {
			System.out.println("Got " + add);
			UDPSimplePacketComs device = new UDPSimplePacketComs(add);
			final BytePacketType gameController = new BytePacketType(1970, 64);
			device.addPollingPacket(gameController);
			device.addEvent(1970, new Runnable() {
				@Override
				public void run() {
					System.out.print("\r\n\r\nPacket updated: ");
					for (int i = 0; i < gameController.getUpstream().length; i++) {
						System.out.print("Got: " + gameController.getUpstream()[i] + " ");

					}
				}
			});
			device.connect();
			Thread.sleep(100);
			for (int i = 0; i < gameController.getDownstream().length; i++) {
				gameController.getDownstream()[i]=new Byte((byte) 42);
				System.out.print("Send "+i+" " + gameController.getDownstream()[i] + " ");

			}

			Thread.sleep(100);
			for (int i = 0; i < gameController.getDownstream().length; i++) {
				gameController.getDownstream()[i]=new Byte((byte) 61);
				System.out.print("Send "+i+" " + gameController.getDownstream()[i] + " ");

			}
			Thread.sleep(10000);

			device.disconnect();
		}

	}
	@Test
	public void swarmTest() throws Exception {
		
		List<GameController> gameControllers = GameController.get("hidapi");
		List<GameController> gameControllers2 = GameController.get("GameController_22");
		

		assertEquals(0, gameControllers.size());
		assertEquals(1, gameControllers2.size());
		gameControllers2.get(0).connect();
		Thread.sleep(500);// wait for the name packet to be sent
		List<GameController> gameControllers3 = GameController.get("hidapi");
		assertEquals(0, gameControllers3.size());
		gameControllers2.get(0).disconnect();
	}
}
