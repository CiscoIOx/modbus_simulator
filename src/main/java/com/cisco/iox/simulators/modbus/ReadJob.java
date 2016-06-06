/*
 * Copyright 2014 DataGenerator Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.iox.simulators.modbus;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.opencsv.CSVReader;

/**
 * 
 * @author srichivu
 * 
 * Reads a record at a time from the csv
 * Also get the header from the csv.
 *
 */
public class ReadJob  {

    private final CSVReader csvFile;
    private String[] header;
	private List<String[]> allVals;
    private AtomicInteger index;

    public List<String[]> getAllVals() {
		return allVals;
	}


    /**
     * Constructor
     *
     * @param csvFile output file name
     * @throws IOException if the output file can not be opened for writing
     */
    public ReadJob(final FileReader reader) throws IOException {
        this.csvFile = new CSVReader(reader, ',');
       
        this.allVals = this.csvFile.readAll();
        this.header = this.allVals.remove(0);
               
        this.index = new AtomicInteger(1);
    }
    
    public Iterator<String[]> getIterator() {
    	return this.csvFile.iterator();
    }

    public String[] getHeader() {
		return header;
	}

	public String[] readNextRecord() {
		try {
			return allVals.get(this.index.getAndIncrement());
		} catch (IndexOutOfBoundsException e) {
			this.index.set(1);
			return allVals.get(this.index.getAndIncrement());
		}
    }
    

    /**
     * Closes the CSV file, should be called once all writing is done
     */
    public void closeCSVFile() {
        try {
            csvFile.close();
        } catch (IOException e) {
            System.out.println("ERROR! Failed to close csv file");
        }
    }

}
