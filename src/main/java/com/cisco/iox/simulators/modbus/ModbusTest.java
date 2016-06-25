package com.cisco.iox.simulators.modbus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.gandev.modjn.ModbusClient;
import de.gandev.modjn.entity.exception.ConnectionException;
import de.gandev.modjn.entity.exception.ErrorResponseException;
import de.gandev.modjn.entity.exception.NoResponseException;
import de.gandev.modjn.entity.func.response.ReadCoilsResponse;
import de.gandev.modjn.entity.func.response.ReadDiscreteInputsResponse;
import de.gandev.modjn.entity.func.response.ReadInputRegistersResponse;

public class ModbusTest {

	public static void main(String[] args) {

		// ModbusClient client = new ModbusClient("localhost", 502);
		short unit = 1;
		ModbusClient client = new ModbusClient("10.232.24.17", 502, unit);

		try {
			client.setup();
			File f = new File("SortingStation.csv");
			OutputStream f1 = new FileOutputStream(f);
			StringBuffer s = new StringBuffer();

			while (true) {
				ReadDiscreteInputsResponse resp1 = client.readDiscreteInputs(0, 24);
				for (int i = 8; i <= 12; i += 2)
					s.append(resp1.getInputStatus().get(i) + ",");

				for (int i = 3; i <= 8; i++) {
					ReadInputRegistersResponse resp = client.readInputRegisters(i, 1);
					s.append(resp.getInputRegisters()[0] + ",");
				}
				
				ReadInputRegistersResponse resp = client.readInputRegisters(9, 1);
				s.append(resp.getInputRegisters()[0] + ",");
				s.append(resp1.getInputStatus().get(19) + ",");
				
				ReadCoilsResponse rcr = client.readCoils(0, 1);
				s.append(rcr.getCoilStatus().get(0) + "\n");
				
				f1.write(s.toString().getBytes());
				f1.flush();
				System.out.println("Iteration done");
				Thread.sleep(1000);
			}
		} catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrorResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}

}
