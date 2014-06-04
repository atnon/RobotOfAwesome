package com.se.chalmers.roa;

import static org.junit.Assert.*;

import org.junit.Test;

public class MotorControlTest {

	
	@Test
	public void testGenerateControlString() {
		
		MotorControl dut = new MotorControl(null);
		
		/* Check full forward speed. */
		assertArrayEquals("set m1speed 127\n".getBytes(), dut.generateControlString(1, 1));
		assertArrayEquals("set m2speed 127\n".getBytes(), dut.generateControlString(2, 1));
		
		/* Check zero speed. */
		assertArrayEquals("set m1speed 0\n".getBytes(), dut.generateControlString(1, 0));
		assertArrayEquals("set m2speed 0\n".getBytes(), dut.generateControlString(2, 0));
		
		/* Check full reverse speed. */
		assertArrayEquals("set m1speed -127\n".getBytes(), dut.generateControlString(1, -1));
		assertArrayEquals("set m2speed -127\n".getBytes(), dut.generateControlString(2, -1));
	}

	@Test
	public void testClipRange() {
		MotorControl dut = new MotorControl(null);
		
		/* Asser that the range clipping works as it should. */
		assertEquals(dut.clipRange(3.13, -1, 1), 1, 0);
		assertEquals(dut.clipRange(-3.13, -1, 1), -1, 0);
		assertEquals(dut.clipRange(0, -1, 1), 0, 0);
	}

}
