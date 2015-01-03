import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FileHandler {
	
	private static void endRun(String errStr){
		System.out.println(errStr);
		System.exit(1);
	}
	
	private final static int configSize = 10; 
	private final static int memSize = 1024;
	private final static int registerNum = 16;
	
	public static Config ReadConfig(String cfgFile)
	{
		Path path = Paths.get(cfgFile);
		List<String> allLines = null ;
		String errStr = "Exception while reading config from "  + cfgFile;
		Config cfgtoRet = new Config();
		int[] cfgVals = new int[configSize];
		
		try{
			allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
		}
		catch(Exception ex) {
			endRun(errStr);
		}
		for (int i = 0 ; i < configSize ; i ++){
			String[] strSplit = allLines.get(i).split("=") ;
			if ( strSplit.length != 2 )
			{
				endRun(errStr);
			}
			cfgVals[i] = Integer.parseInt(strSplit[1].trim());
		}
		
		cfgtoRet.IntDelay = cfgVals[0];
		cfgtoRet.AddDelay = cfgVals[1];
		cfgtoRet.MulDelay = cfgVals[2];
		cfgtoRet.MemDelay = cfgVals[3];
		cfgtoRet.RobEntries = cfgVals[4]; 
		cfgtoRet.AddNrReservation = cfgVals[5];
		cfgtoRet.MulNrReservation = cfgVals[6];
		cfgtoRet.IntNrReservation = cfgVals[7];
		cfgtoRet.MemNrLoadBuffers = cfgVals[8];
		cfgtoRet.MemNrStoreBuffers = cfgVals[9];
		
		return cfgtoRet;
		
	}
	
	public static int[] ReadMainMem(String MemIn)
	{
		Path path = Paths.get(MemIn);
		List<String> allLines = null ;
		String errStr = "Exception while reading memory from "  + MemIn;
		int[] mem = new int[memSize];
		try{
			allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
		}
		catch(Exception ex) {
			endRun(errStr);
		}
		for (int i = 0 ; i < memSize ; i ++){
			mem[i] = (int) Long.parseLong(allLines.get(i).trim(),16);
		}
		return mem;
	
	}
	
	public static void WriteMemOut(int[] memOut, String fileName)
	{
	     Path file = Paths.get(fileName);
        List<String> lines = new ArrayList<String>();
        String errStr = "Exception while writing memory to "  + fileName;
        for (int i=0 ; i < memSize; i ++){
        	lines.add(String.format("%08x",memOut[i]));
        }
 
        try {
            // Write all lines to the file.
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
        	endRun(errStr);
        }
	}
	
	public static void writeRegInt ( int[] regInt, String fileName)
	{
		 Path file = Paths.get(fileName);
        List<String> lines = new ArrayList<String>();
        String errStr = "Exception while writing integer registers  to "  + fileName;
        for (int i=0 ; i < registerNum; i ++){
        	lines.add(String.valueOf(regInt[i]));
        }
 
        try {
            // Write all lines to the file.
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
        	endRun(errStr);
        }	
	}
	public static void writeRegOut(float[] regFp, String fileName)
	{
		 Path file = Paths.get(fileName);
        List<String> lines = new ArrayList<String>();
        String errStr = "Exception while float registers to "  + fileName;
        for (int i=0 ; i < registerNum; i ++){
        	lines.add(String.valueOf(regFp[i]));
        }
 
        try {
            // Write all lines to the file.
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
        	endRun(errStr);
        }	
	}
	
	public static void WriteTraceToFile(String fileName)
	{
		 Path file = Paths.get(fileName);
        List<String> lines = new ArrayList<String>();
        String errStr = "Exception while writing trace to "  + fileName;
        // trace.ID is the next ID that a new instruction would get, therefore it's also the max threshold.
        for (int i=0 ; i < Trace.ID; i ++){
        	TraceRecord record = Trace.GetRecord(i);
        	String tempStr = record.Instruction;
        	tempStr += " " + record.CycleIssued;
        	tempStr += " " + record.CycleExeuctedStart;
        	tempStr += " " + record.WriteCdb;
        	tempStr += " " + record.CycleCommit;
        	
        	lines.add(tempStr);
        }
 
        try {
            // Write all lines to the file.
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
        	endRun(errStr);
        }	
	}

}
