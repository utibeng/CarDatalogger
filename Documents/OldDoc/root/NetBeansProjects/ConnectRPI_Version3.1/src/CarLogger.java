//UTIBE EFFIONG
import java.awt.Color;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.microedition.io.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class CarLogger implements Runnable{

	static StreamConnection conn = null;
	static InputStream input = null;
	static OutputStream output = null;
	static BufferedReader bReader2 = null;
	static BufferedReader bReader2PID = null;
	static String sendThis = null;
	static String lineRead = null;
        static String readPID = null;
	int k = 0;
	final int bufferSize = 10240; // 1024 * 8
        int A = 0;
        int B = 0;
        int rpmRead = 0;
        int throttlePositionRead = 0;
        int fuelLevelInputRead = 0;
        int engineCoolantTemperatureRead = 0;
        int vehicleSpeedRead = 0;  
        int engineLoadRead = 0;//TEST#
        static int gear;
        static int previousGear;
        static int potentialGear;
        static int stopReadingData = 0; 
        static FileWriter fileWriter = null;
        static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	static Date date = new Date();
        static String fileName1 = dateFormat.format(date);
        static DateFormat dataTime = new SimpleDateFormat("HH:mm:ss:SS");
        static int dataToRead = -1;
        DateTime today = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss:SSS");
        int firstDataRead = 0;

	
        @Override
	public void run(){
		
		try {			
			//CREATE A BT CONNECTION WITH OBD2 AND OPEN INPUT & OUTPUT STREAMS
			Dash.jTextField2.setBackground(Color.gray);
                        Dash.jTextField1.setBackground(Color.red);
			conn = (StreamConnection)Connector.open("btspp://8818566898EB:1");
			Dash.jTextField1.setBackground(Color.red);
			System.out.println("\n CONNECTION OPENED ");
			output = conn.openOutputStream();
			System.out.println("\n OUTPUT STREAM OPENED LINKED TO CONNECTION ");
			input = conn.openInputStream();
			System.out.println("\n INPUT STREAM LINKED TO CONNECTION AND OPENED ");
			bReader2=new BufferedReader(new InputStreamReader(input), bufferSize);			
			System.out.println("\n BUFFERED READER LINKED TO INPUT ");
			
                        // TEST//TEST//TEST//TEST// START
                        //SEND BYTE TO REMOVE RESET AND WAIT 2000MS						
			sendThis = "AT Z\r";
			output.write(sendThis.getBytes());				
			output.flush();
                        try{
				Thread.sleep(2000);
			}
			catch(InterruptedException e){
			}    
                        bReader2=new BufferedReader(new InputStreamReader(input));
                        while(input.available()!= 0)
                            lineRead=bReader2.readLine();
                        System.out.println("\n OBD2 RESET " + lineRead);						
			              
                        
                        //TEST//TEST//TEST//TEST// END





                        //SEND BYTE TO REMOVE ECHO AND WAIT 200MS						
			sendThis = "AT E0\r";
			output.write(sendThis.getBytes());				
			output.flush();
                        bReader2=new BufferedReader(new InputStreamReader(input));
                        while(input.available()!= 0)
                            lineRead=bReader2.readLine();
                        System.out.println("\n ECHO REMOVED IS " + lineRead);						
			try{
				Thread.sleep(200);
			}
			catch(InterruptedException e){
			}

			//SEND BYTE TO USE AGRESSIVE ADAPTIVE AQUISITION AND WAIT 200MS		             
                        
                        sendThis = "AT AT2\r";
			output.write(sendThis.getBytes());				
			output.flush();
			bReader2=new BufferedReader(new InputStreamReader(input));
			while(input.available()!= 0)                         
                            lineRead=bReader2.readLine();
                        System.out.println("\n AGGRESIVE ADAPTIVE AQUISITION IS " + lineRead);
			try{
				Thread.sleep(200);
			}
			catch(InterruptedException e){
			}
                        
			//SEND BYTE TO REMOVE SPACES AND WAIT 200MS			
			sendThis = "AT S0\r";
			output.write(sendThis.getBytes());				
			output.flush();
			bReader2=new BufferedReader(new InputStreamReader(input));
			while(input.available()!= 0)
                            lineRead=bReader2.readLine();
                        System.out.println("\n SPACING REMOVED IS " + lineRead);
			try{
				Thread.sleep(200);
			}
			catch(InterruptedException e){
			}	
			
			//SEND BYTE TO REMOVE LINEFEED AND WAIT 200MS			
			sendThis = "AT L0\r";
			output.write(sendThis.getBytes());				
			output.flush();
			bReader2=new BufferedReader(new InputStreamReader(input));
			while(input.available()!= 0)
                            lineRead=bReader2.readLine();                        
                        System.out.println("\n LINEFEED REMOVED IS " + lineRead);			
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}			
   		}
  		catch ( IOException e ) { 		
			System.err.print(e.toString() + "ERROR TYPE\n"); 
			System.out.println("\n LEVEL 5 ERROR IN RUN INITIALISE \n");
                        Dash.jTextField1.setBackground(Color.BLACK);
			try{		
				conn.close();
				input.close();
				output.close();
			}
			catch ( IOException e1RPM ){
				System.err.print(e1RPM.toString() + "ERROR IN RUN() CANT CLOSE STREAMS\n");
			}			
		}

		// CONNECTION HAS BEEN ESTABLISHED, ECHO REMOVED NOW READ DATA IN LOOPS	
		Dash.jTextField1.setBackground(Color.green);			
		Dash.jTextField3.setBackground(Color.green);
                
                //CREATE FILE TO WRITE DATA
		try{
                    File newFile = new File ("/home/pi/JavaPrograms/LoggedData/" + fileName1 + ".csv");
                    //File newFile = new File ("/home/utibe/Documents/LoggedData/" + fileName1 + ".csv");
                    fileWriter = new FileWriter(newFile); 
                    fileWriter.append("Time, RPM (rpm), Vehicle Speed(MPH) , Engine Load(%),  Throttle Position(%), Fuel Level Input (L/H), Engine Coolant(ºC), SpeedRPMRatio , Gear" + "\n");
                    dataToRead = 0;
                    gear = 0;
                    previousGear = 0;
                }
                catch(IOException e){
                    System.out.println("Error in Creating CsvFileWriter " + e.getMessage());
                }
                finally{
                    try {
                        fileWriter.flush();                        
                    }
                    catch (IOException e) {
                        System.out.println("Error while flushing/closing fileWriter !!!");
                    }
                }
                
                for(;;){
                    // Create File to store the data
                    try{
                            fileWriter.append(new DateTime().toString(fmt)); 
                            fileWriter.flush();
                    }
                    catch(IOException e){
                    }
                    
                    // Check if not halted before taking data readings                    
                    if(stopReadingData == 0){
                        
                        // Read RPM Data
                        double readPID1 = readPID("01 0C2\r");
                        logReading("410C", readPID1);
                        
                        // Read Vehicle Speed
			double readPID5 = readPID( "01 0D1\r");
                        logReading("410D", readPID5);
                        
                                      
                        
                         //TEST
                        //Read Engine Load
                        double readPID6 = readPID( "01 041\r");//TEST
                        logReading("4104", readPID6);//TEST
                        
                        
                        //TEST
                                      
                        
                        //Read Throttle Position Data 0 to 100%
			double readPID2 = readPID("01 111\r");
                        logReading("4111", readPID2);
                        
                        
                        // Read fuel Level Input 0 to 100%
			double readPID3 = readPID( "01 2F1\r");
                        logReading("412F", readPID3);
                        
                       	
			// Read Engine Coolant
			double readPID4 = readPID( "01 051\r");
                        logReading("4105", readPID4);
                        
                        //TEST//
                        //Log Gear
                        int gearNum = getGear(readPID1, readPID5);
                        
                        //logGear(gearNum);
                        //logGear(int gearNum);
                        
                        
                        //int gear1 = 3;
                  
                        
                       
                        
                        
                        //TEST
                        
                       
                        
                        //Append newline character at the end of a set of readings
                        try{
                                fileWriter.append("\n" ); 
                                fileWriter.flush();
                                ++firstDataRead;
                        }
                        catch(IOException e){
                            e.printStackTrace();                            
                        }   		
                    }
                    
                    else{
                        Dash.jTextField2.setBackground(Color.green);
                        Dash.jTextField1.setBackground(Color.gray);
                        Dash.jTextField3.setBackground(Color.gray);
                    }
                }
	}

	//@SuppressWarnings("empty-statement")
        public double readPID (String messagePID){
            
            double valRead = 999;
            try {		
		output.write(messagePID.getBytes());
                //output.flush();
                
                //Modification here - Change to 100 if probs
                while(input.available() < 1){
                    try {
                        Thread.sleep(45);
                    }
                    catch (Exception e) {}
                }
                
		if((k==0)){
                        try{
                            bReader2PID=new BufferedReader(new InputStreamReader(input), bufferSize);
                            while(input.available()!= 0)
                                lineRead=bReader2.readLine();
                            System.out.println("\n STRANGE READ IS " + lineRead + "\n");
                            ++k;
                            while(input.available()!= 0)
                                lineRead = bReader2.readLine();
                            System.out.println("\n STRANGE READ NUMBER 2 IS " + lineRead + "\n");                                       
			}
			catch ( IOException e1RPM ){
                            System.err.print(e1RPM.toString() + "PRELIM BUFFER READ FAILED\n");
			}
                        
                        valRead=77777;
                        return valRead;
		}
		
                //TEST
                //bReader2PID=new BufferedReader(new InputStreamReader(input));
                while(input.available()!= 0)
                    lineRead = bReader2.readLine();
                
                //while(input.available()!= 0)
                    //while((lineRead = bReader2.readLine())!= null)
                     //   ++k;
                    //TEST
                
                    //Determine Data Type Read                                            
                    if ((lineRead.length()>6)){
                        readPID = lineRead.trim().substring(1, 5);
                        switch(readPID){
                            case "410C": //RPM 2 bytes
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);
                                readPID = lineRead.substring(7,9);                               
                                B = Integer.parseInt(readPID, 16);
                                rpmRead = ((A*256) + B)/4;    
                                Dash.gaugeRPM.setValue(rpmRead);                                
                                ++dataToRead;
                                valRead = rpmRead;                                
                                
                            break;
                            
                            //TEST
                            
                           
                            case "4104": //Calculated Engine Load rate 1 byte
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);
                                engineLoadRead = A*100/255;
                                Dash.gaugeEngineFuelRate.setValue(engineLoadRead);
                                //fileWriter.append(throttlePositionRead + ",");
                                ++dataToRead;
                                 valRead = engineLoadRead;
                                 //lastthrottlePositionRead = throttlePositionRead; REMOVE                              
                            break;
                            
                            
                            //TEST
                                                     
                               
                            case "4111": //Throttle Position 1 byte
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);
                                throttlePositionRead = A*100/255;
                                Dash.gaugeThrottlePosition.setValue(throttlePositionRead);
                                //fileWriter.append(throttlePositionRead + ",");
                                ++dataToRead;
                                 valRead = throttlePositionRead;
                                 //lastthrottlePositionRead = throttlePositionRead; REMOVE
                            break;
                               
                            case "412F": //Fuel Level Input 1 byte
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);                                                             
                                fuelLevelInputRead = A*100/255;
                                Dash.gaugeFuelInput.setValue(fuelLevelInputRead);
                                //fileWriter.append(fuelLevelInputRead + ",");
                                ++dataToRead;
                                valRead = fuelLevelInputRead;
                                //lastfuelLevelInputRead = fuelLevelInputRead; REMOVE
                            break;
                               
                            case "4105"://Engine Coolant Temp 1 byte
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);                                                            
                                engineCoolantTemperatureRead = A-40;
                                Dash.gaugeengineCoolantTemperature.setValue(engineCoolantTemperatureRead);
                                //fileWriter.append(engineCoolantTemperatureRead + ",");
                                ++dataToRead;
                                valRead = engineCoolantTemperatureRead;
                                //lastengineCoolantTemperatureRead = engineCoolantTemperatureRead; REMOVE
                            break;
                               
                            case "410D"://Vehicle Speed
                                readPID = lineRead.substring(5,7);                               
                                A = Integer.parseInt(readPID, 16);                                                            
                                vehicleSpeedRead = A; 
                                //vehicleSpeedRead = int(1.4); //(vehicleSpeedRead * 0.6214);
                                //Dash.gaugeVehicleSpeed.setValue(Math.round(vehicleSpeedRead * 0.6214));
                                //fileWriter.append(vehicleSpeedRead + "" );
                                ++dataToRead;
                                valRead = vehicleSpeedRead * 0.6214;
                                Dash.gaugeVehicleSpeed.setValue((int)valRead);
                                //lastvehicleSpeedRead = vehicleSpeedRead; REMOVE
                            break;
                            
                            default:
                                //System.out.println(" BAD DATA READ IS " + lineRead + "\n");                       
                            break;
                                
                            }
                        }
        
		}
  		catch ( IOException eRPM ) { 
			try{
				conn.close();
				input.close();
				output.close();
                                fileWriter.close();
			}
			catch ( IOException e1RPM ){
				System.err.print(e1RPM.toString() + "ERROR IN readRPM()");
			}
			System.err.print(eRPM.toString()); 
			System.out.println("\n LEVEL 5 in READRPM ");
		}
                //if (valRead == 999)
                    //System.out.println(" BAD DATA READ FOR " + messagePID + "   " + "\n");
                    //System.out.println(" BAD DATA READ FOR " + messagePID + "   " + "\n");
                        
		return valRead;
 	}
        
        public void logReading(String PIDcode, double readData1){
        
            if (firstDataRead >0){
                switch(PIDcode){
                    case "410C": //Log RPM
                        if ((readData1 != 77777) && (readData1 != 999)){
                            try{
                                fileWriter.append("," + readData1 );
                                //fileWriter.append("," + 111 );
                                fileWriter.flush();                           
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }         
                        
                        }
                        
                        else if(readData1 == 999){
                            
                            try{
                                //fileWriter.append("," + lastrpmRead + '*' );
                                fileWriter.append("," + "***" );
                                System.out.println("logging *** RPM " + "\n");
                                Dash.jTextArea1.append("logging *** RPM " + "\n");
                                //fileWriter.append("," + 111 );
                                fileWriter.flush();                           
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }         
                            
                        }
                        
                        else if (readData1 == 77777){
                            try{
                                //fileWriter.append("," + 111 ); 
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }   
                        } 
                    
                    
                    break; 
                     
                    //TEST
                     
                    case "4104":
                        if ((readData1 != 77777) && (readData1 != 999)){
                            try{
                                fileWriter.append("," + readData1 );
                                //fileWriter.append("," + 111 );
                                fileWriter.flush();                           
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }         
                        
                        }
                        
                        else if(readData1 == 999){
                            
                            try{
                                //fileWriter.append("," + lastrpmRead + '*' );
                                fileWriter.append("," + "***" );
                                System.out.println("logging *** 4104 " + "\n");
                                Dash.jTextArea1.append("logging *** 4104 " + "\n");
                                //fileWriter.append("," + 111 );
                                fileWriter.flush();                           
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }         
                            
                        }
                        
                        else if (readData1 == 77777){
                            try{
                                //fileWriter.append("," + 111 ); 
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }   
                        } 
                    
                    
                    break;               
                
                                         
                    // TEST
                
                    case "4111"://Log Throttle Position
                        if(readData1 != 999){
                            try{
                                fileWriter.append("," + readData1 );
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            } 
                        }
                        else if(readData1 == 999){
                            try{
                                //fileWriter.append("," + lastthrottlePositionRead +"*" );
                                //fileWriter.append("," + 222);
                                fileWriter.append("," +"***" );
                                System.out.println("logging *** throttle position " + "\n");
                                Dash.jTextArea1.append("logging *** throttle position " + "\n");
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }                  
                            
                        }
                    break;
                 
                    case "412F"://Log Fuel Input Level
                        
                        if(readData1 != 999){
                            try{
                                fileWriter.append("," + readData1 );
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            } 
                        }
                        else if(readData1 == 999){
                            try{
                                //fileWriter.append("," + lastfuelLevelInputRead +"*" );
                                fileWriter.append("," + "***" );
                                System.out.println("logging *** fuel input level " + "\n");
                                Dash.jTextArea1.append("logging *** fuel input level " + "\n");
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }                  
                            
                        }     
                    break;               
                
                    case "4105": //Log Coolant temp
                        if(readData1 != 999){
                            try{
                                fileWriter.append("," + readData1 );
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            } 
                        }
                        else if(readData1 == 999){
                            try{
                                //fileWriter.append("," + lastengineCoolantTemperatureRead +"*" );
                                fileWriter.append("," + "***" );
                                System.out.println("logging *** Coolant temp " + "\n");
                                Dash.jTextArea1.append("logging *** Coolant temp " + "\n");
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }                  
                            
                        }             
                    break;
                    
                    case "410D": //Log Vehicle Speed
                        if(readData1 != 999){
                            try{
                                //readData1 = readData1*0.6214;
                                //readData1 = Math.round(readData1);
                                
                                fileWriter.append("," + (int) readData1 );
                                //fileWriter.append("," + 222);
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            } 
                        }
                        else if(readData1 == 999){
                            try{
                                //fileWriter.append("," + lastvehicleSpeedRead +"*" );
                                //fileWriter.append("," + 222);
                                System.out.println("logging *** Vehicle Speed " + "\n");
                                Dash.jTextArea1.append("logging *** Vehicle Speed " + "\n");
                                fileWriter.append("," + "***" );
                                fileWriter.flush();                            
                            }
                            catch(IOException e){
                                e.printStackTrace(System.out);                            
                            }                  
                            
                        }          
                    break;               
                    
                    default:
                       
                    break;
                        
                }
            }
        }
        
        
        int getGear(double rPMVal, double speedVal ){
            
            double speedRPMRatio = 0;
            int tempGear;
            if(rPMVal!= 0)
                speedRPMRatio = 1000.0 * (int)speedVal/rPMVal;
            if ((speedRPMRatio >= 4)&& (speedRPMRatio <= 6))
                tempGear = 1;
            else if ((speedRPMRatio >= 7.5)&& (speedRPMRatio <= 11))
                tempGear = 2;
            else if ((speedRPMRatio >= 11.8)&& (speedRPMRatio <= 13.5))
                tempGear = 3;    
            else if ((speedRPMRatio >= 15)&& (speedRPMRatio <= 17))
                tempGear = 4;
            else if ((speedRPMRatio >= 19)&& (speedRPMRatio <= 21))
                tempGear = 5;
            else
                tempGear = gear;
            
            if(tempGear == previousGear){
                gear = tempGear;                
            }
            
            if(tempGear != 0)
                previousGear = tempGear;
            
            if((rPMVal == 0)||(speedVal == 0))
                gear = 0;
                        
            //double vehicleSpeedRead = 0;
            Dash.jTextField4.setText(gear + "");
            try{
                 fileWriter.append("," + speedRPMRatio );
                 fileWriter.append("," + gear );
            }
            catch(IOException e){
                System.out.println("ERROR WRITING GEAR " + "\n");
            } 
            
            return 5;                     
                             
        }
                            
                        
        public void logGear(int num) {
            try{
                fileWriter.append("," + num );
            }
            catch(IOException e){
                System.out.println("ERROR WRITING GEAR " + "\n");
            } 
        }               
                        
                        

	
}



		
			
			
