package com.cisco.iox.simulators.modbus;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import de.gandev.modjn.ModbusServer;
import de.gandev.modjn.entity.exception.ConnectionException;


public class ModbusSlave implements Closeable{
	
	final ModbusServer modbusServer;
	
	public  ModbusSlave(String host, int port, List<String[]> dataSet, String[] headers) throws ConnectionException {
		System.out.println("host - " + host);
		System.out.println("port - " + port);
		this.modbusServer = new ModbusServer(host, port);
		modbusServer.setup(new ModbusSlaveHandler(dataSet, headers));
	}

	public void close() throws IOException {
		if(modbusServer != null) {
			modbusServer.close();
		}
	}
	
}
