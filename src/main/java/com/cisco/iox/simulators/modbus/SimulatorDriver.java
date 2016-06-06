package com.cisco.iox.simulators.modbus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimulatorDriver {
	private static Logger log = LoggerFactory.getLogger(SimulatorDriver.class);

	public static final String FILE_PROP = "DataFile";

	private Properties props;
	private ReadJob modbusDS;
	private String confFile;

	private ModbusSlave ms;

	private String port;

	private String host;

	public SimulatorDriver() {
		// default constructor is needed
	}

	public SimulatorDriver(String confFile, String host, String port) {
		this.confFile = confFile;
		this.host = host;
		this.port = port;
	}

	private ReadJob loadCSV(String fileName) throws Exception {
		File confFile = getFile(fileName);
		FileReader reader = null;
		try {
			reader = new FileReader(confFile);
			return new ReadJob(reader);
		}catch (Exception e) {
			log.error("Error while reading datafile", e);
			throw e;
		}
	}

	private void loadProperties() throws Exception {
		File confFile = getFile(this.confFile);
		FileReader reader = null;
		this.props = new Properties();
		try {
			reader = new FileReader(confFile);
			this.props.load(reader);
		} catch (Exception e)  {
			log.error("Error while load properties", e);
			throw e;
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private File getFile(String file) throws Exception {
		File confFile = new File(file);
        if (!confFile.exists() && !confFile.isAbsolute()) {
            URL resource = getClass().getResource("/"+file);
            if(resource == null) {
            	log.error("file {} not found in classpath", resource);
            	throw new Exception("file " + resource + " not found in classpath");
            }
			confFile = new File(resource.getFile());
        }
		return confFile;
	}
	
	public void setup() {
		try {
			log.debug(" modbus host : {} and port - {}", this.getHost() , this.getPort());
			loadProperties();
			Object fileProp = this.props.get(FILE_PROP);

			if (fileProp == null || "".equalsIgnoreCase(fileProp.toString())) {
				log.error(FILE_PROP + " not found ");
				throw new Exception(FILE_PROP + " not found ");
			}

			this.modbusDS = this.loadCSV(fileProp.toString());
			for (String header : this.modbusDS.getHeader()) {
				System.out.print(" " + header + ":");
			}

			ms = new ModbusSlave(getHost(), Integer.parseInt(getPort()), this.modbusDS.getAllVals(),
					this.modbusDS.getHeader());
		} catch ( Throwable e) {
			log.error("Error - ", e);
			throw new RuntimeException(e);
		}
		
		
	}
	
	public void close() {
		if(ms != null) {
			try {
				log.debug(" closing ModbusSlave");
				ms.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getConfFile() {
		return confFile;
	}

	public void setConfFile(String confFile) {
		this.confFile = confFile;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
