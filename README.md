# modbus_simulator
Simulator for testing Modbus Protocol handler functionality

You can use this simulator to send varieties of data that conforms to the data models provisioned on the IOx services gateway.

## Sorting Station simulator ##

To run the sorting station simulator, go to bin directory and run
"java -jar <jar file> "0.0.0.0" 502"

## Building and packaging the simulator ##

Developers who want to use the simulator need to have maven environment to compile and package the simulator.


    $ ls
     LICENSE  README.md  lib  package.xml  pom.xml  src  target

    $ mvn clean install

## Preparing the simulator Environment ##

modbusSimulator-0.0.1-SNAPSHOT-distro.zip archive is created as part of building the modbus simulator. Unzip the archive in a location from where the developer wants to launch the simulator.

When developers unzip the archive, the following contents would be present

    $ unzip ../modbusSimulator-0.0.1-SNAPSHOT-distro.zip
    
    $ ls
    accelero.csv  modbusSimulator-0.0.1-SNAPSHOT.jar  startup.sh tempAndHumidity.csv  test.properties  third-party

## Components in the simulator Environment ##


- test.properties contain the file from which the data can be read.

    $ cat test.properties
    DataFile=tempAndHumidity.csv

- DataFile points to a csv file that has the data set

- CSV file has 2 headers, one header contains the register map of modbus protocol to which the column data is associated with. Second header contains the data type of the column data, short, integer, long, float or double.

- A snippet of a data file as below

>     $ cat tempAndHumidity.csv
>     40001-1,40002-1,40003-2,40005-2
>     short,short,float,float
>     40,20,9.99,30.11

In the above snippet, first column is associated with the modbus register 40001 (Holding register), 1 register value is associated with short and sent and the first data item is 40. Similarly other column data is associated with modbus registers.

- Coils register range starts with 1, Discrete Inputs register range starts with 10001, Input registers address range starts with 30001 and holding registers address range starts from 40001.

## Start simulator ##

A startup file startup.sh is there which help run the simulator with the given data set. Customize it to suite the windows/linux environment appropriately.

>     $ sh startup.sh
>      40001-1: 40002-1: 40003-2: 40005-2:
>     host - localhost
>     port - 502
>     Loaded registers with row : 0
>     Modbus simulator started

## Customize data set ##

In order to use the accelero meter dataset, just change  as below 

    DataFile=accelero.csv

in the test.properties file.

## Using data set in IOx Services gateway ##
The mod-bus register set that the simulator is started with, if it matches exactly with address map of a modbus device type deployed in the IOx provisioning service, then simulator seamlessly respond to the modbus requests from the IOx modbus protocol handler.
