package com.cisco.iox.simulators.modbus;

import java.util.Properties;


public class ModbusSlaveSimulator {

private Properties props;
	
	
	
	public static void main(String[] args) {
    	SimulatorDriver sDriver = new SimulatorDriver("test.properties",args[0], args[1]);
    	try {
			sDriver.setup();
		} catch (NumberFormatException e) {
			System.out.println("Port is invalid");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	System.out.println("Modbus simulator started");

	}

}
