package com.cisco.iox.simulators.modbus;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import de.gandev.modjn.entity.func.WriteSingleCoil;
import de.gandev.modjn.entity.func.WriteSingleRegister;
import de.gandev.modjn.entity.func.request.ReadCoilsRequest;
import de.gandev.modjn.entity.func.request.ReadDiscreteInputsRequest;
import de.gandev.modjn.entity.func.request.ReadHoldingRegistersRequest;
import de.gandev.modjn.entity.func.request.ReadInputRegistersRequest;
import de.gandev.modjn.entity.func.request.WriteMultipleCoilsRequest;
import de.gandev.modjn.entity.func.request.WriteMultipleRegistersRequest;
import de.gandev.modjn.entity.func.response.ReadCoilsResponse;
import de.gandev.modjn.entity.func.response.ReadDiscreteInputsResponse;
import de.gandev.modjn.entity.func.response.ReadHoldingRegistersResponse;
import de.gandev.modjn.entity.func.response.ReadInputRegistersResponse;
import de.gandev.modjn.entity.func.response.WriteMultipleCoilsResponse;
import de.gandev.modjn.entity.func.response.WriteMultipleRegistersResponse;
import de.gandev.modjn.handler.ModbusRequestHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class ModbusSlaveHandler extends ModbusRequestHandler {

	private List<String[]> dataSet;
	private Map<Integer, Integer> slaveRegisters;
	private Map<Integer, Boolean> slaveCoils;
	private AtomicInteger counter;
	private AtomicInteger row;
	private String[] headers;
	private String[] types;

	public ModbusSlaveHandler(List<String[]> dataSet, String[] headers) {
		this.types = dataSet.remove(0);
		this.dataSet = dataSet;
		this.slaveRegisters = new ConcurrentHashMap<Integer, Integer>();
		this.slaveCoils = new ConcurrentHashMap<Integer, Boolean>();
		this.counter = new AtomicInteger(-1);
		this.row = new AtomicInteger(0);
		this.headers = headers;
		this.loadRegisters();
	}

	private int[] getRegisterStartAndCount(String registerRangeProperty) {
		String[] registerArray = registerRangeProperty.split("-");
		int startingAddress = Integer.parseInt(registerArray[0]);
		int registerCount = 0;
		if (registerArray.length == 1) {
			registerCount = 1;
		} else {
			registerCount = Integer.parseInt(registerArray[1].trim());
		}

		return new int[] { startingAddress, registerCount };
	}

	private static byte [] float2ByteArray (float value)
	{  
	     return ByteBuffer.allocate(4).putFloat(value).array();
	}
	
	private static byte [] long2ByteArray (long value)
	{  
	     return ByteBuffer.allocate(8).putLong(value).array();
	}
	
	private static byte [] double2ByteArray (double value)
	{  
	     return ByteBuffer.allocate(8).putDouble(value).array();
	}
	
	private static byte [] int2ByteArray (int value)
	{  
	     return ByteBuffer.allocate(4).putInt(value).array();
	}
	
	private void loadRegisters() {
		String[] regVals = this.dataSet.get(this.row.get());
		int index = 0;
		for (String header : headers) {
			int[] addrAndCount = getRegisterStartAndCount(header);

			if (addrAndCount[0] >= 30001) {
				if (this.types[index].equalsIgnoreCase("short")) {
					Integer val = Integer.parseInt(regVals[index++]);
					this.addReg(addrAndCount[0], val);
				} else if (this.types[index].equalsIgnoreCase("float")) {
					Float val = Float.parseFloat(regVals[index++]);
					byte[] bytes = this.float2ByteArray(val);
					this.addReg(addrAndCount[0], ((bytes[0] << 8) | (bytes[1] & 0xff)));
					this.addReg(addrAndCount[0]+1, ((bytes[2] << 8) | (bytes[3] & 0xff)));
				} else if (this.types[index].equalsIgnoreCase("long")) {
					Long val = Long.parseLong(regVals[index++]);
					byte[] bytes = this.long2ByteArray(val);
					this.addReg(addrAndCount[0], ((bytes[0] << 8) | (bytes[1] & 0xff)));
					this.addReg(addrAndCount[0]+1, ((bytes[2] << 8) | (bytes[3] & 0xff)));
					this.addReg(addrAndCount[0]+2, ((bytes[4] << 8) | (bytes[5] & 0xff)));
					this.addReg(addrAndCount[0]+3, ((bytes[6] << 8) | (bytes[7] & 0xff)));
				} else if (this.types[index].equalsIgnoreCase("int")) {
					Integer val = Integer.parseInt(regVals[index++]);
					byte[] bytes = this.int2ByteArray(val);
					this.addReg(addrAndCount[0], ((bytes[0] << 8) | (bytes[1] & 0xff)));
					this.addReg(addrAndCount[0]+1, ((bytes[2] << 8) | (bytes[3] & 0xff)));
				} else if (this.types[index].equalsIgnoreCase("double")) {
					Double val = Double.parseDouble(regVals[index++]);
					byte[] bytes = this.double2ByteArray(val);
					this.addReg(addrAndCount[0], ((bytes[0] << 8) | (bytes[1] & 0xff)));
					this.addReg(addrAndCount[0]+1, ((bytes[2] << 8) | (bytes[3] & 0xff)));
					this.addReg(addrAndCount[0]+2, ((bytes[4] << 8) | (bytes[5] & 0xff)));
					this.addReg(addrAndCount[0]+3, ((bytes[6] << 8) | (bytes[7] & 0xff)));
				}

			} else {
				Boolean val = Boolean.parseBoolean(regVals[index++]);
				this.addCoil(addrAndCount[0], val);
			}
		}

		System.out.println("Loaded registers with row : " + this.row.get());
	}

	public void addReg(int addr, Integer val) {
		this.slaveRegisters.put(addr, val);
	}

	public Integer getReg(int addr) {
		return this.slaveRegisters.get(addr);
	}

	public void addCoil(int addr, boolean val) {
		this.slaveCoils.put(addr, val);
	}

	public boolean getCoil(int addr) {
		return this.slaveCoils.get(addr);
	}

	synchronized void checkAndLoadRegisters() {
		int currentCount = this.counter.incrementAndGet();
		if (currentCount == this.headers.length) {
			this.counter.set(-1);
			int rowIndex = this.row.incrementAndGet();
			if (rowIndex == this.dataSet.size()) {
				this.row.set(0);
			}
			this.loadRegisters();
		}
	}

	@Override
	protected ReadCoilsResponse readCoilsRequest(ReadCoilsRequest arg0) {
		this.checkAndLoadRegisters();
		BitSet coils = new BitSet(arg0.getQuantityOfCoils());
		for (int i = 0; i < arg0.getQuantityOfCoils(); i++) {
			boolean val = this.getCoil(1 + arg0.getStartingAddress());
			if (val) {
				coils.set(i);
			}
		}

		return new ReadCoilsResponse(coils);
	}

	@Override
	protected ReadDiscreteInputsResponse readDiscreteInputsRequest(ReadDiscreteInputsRequest arg0) {
		this.checkAndLoadRegisters();
		BitSet coils = new BitSet(arg0.getQuantityOfCoils());
		for (int i = 0; i < arg0.getQuantityOfCoils(); i++) {
			boolean val = this.getCoil(10001 + arg0.getStartingAddress() + i);
			if (val) {
				coils.set(i);
			}
		}
		return new ReadDiscreteInputsResponse(coils);
	}

	@Override
	protected ReadHoldingRegistersResponse readHoldingRegistersRequest(ReadHoldingRegistersRequest arg0) {
		System.out.println(
				"Got a request for " + arg0.getStartingAddress() + " and count " + arg0.getQuantityOfInputRegisters());
		this.checkAndLoadRegisters();
		int[] registers = new int[arg0.getQuantityOfInputRegisters()];
		for (int i = 0; i < arg0.getQuantityOfInputRegisters(); i++) {
			registers[i] = this.getReg(40001 + arg0.getStartingAddress() + i);
		}
		return new ReadHoldingRegistersResponse(registers);
	}

	@Override
	protected ReadInputRegistersResponse readInputRegistersRequest(ReadInputRegistersRequest arg0) {
		this.checkAndLoadRegisters();
		int[] registers = new int[arg0.getQuantityOfInputRegisters()];
		for (int i = 0; i < arg0.getQuantityOfInputRegisters(); i++) {
			registers[i] = this.getReg(30001 + arg0.getStartingAddress() + i);
		}
		return new ReadInputRegistersResponse(registers);
	}

	@Override
	protected WriteMultipleCoilsResponse writeMultipleCoilsRequest(WriteMultipleCoilsRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WriteMultipleRegistersResponse writeMultipleRegistersRequest(WriteMultipleRegistersRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WriteSingleCoil writeSingleCoil(WriteSingleCoil arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WriteSingleRegister writeSingleRegister(WriteSingleRegister arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}