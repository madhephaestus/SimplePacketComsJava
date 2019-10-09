package edu.wpi;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.wpi.SimplePacketComs.server.device.GameControllerServer;

public class GameControllerServerTest {
	GameControllerServer server=null;
	@Before
	public void setUp() throws Exception {
		server=new GameControllerServer("GameController_22", 2);
		server.connect();
	}

	@After
	public void tearDown() throws Exception {
		server.disconnect();
	}

	@Test
	public void test() throws Exception {
		for(int i=0;i<1000;i++) {
			Thread.sleep(10);
			//System.out.println(System.currentTimeMillis());
		}
	}

}
