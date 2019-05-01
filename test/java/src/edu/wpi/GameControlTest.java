package edu.wpi;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import edu.wpi.SimplePacketComs.device.gameController.GameController;

public class GameControlTest {

	@Test
	public void test() throws Exception {
		GameController control = new GameController("Game*");
		
		System.out.println("Name gotten = "+control.getName());
		assertTrue(control.getName().getBytes().length>0);
	}

}
