import java.util.ArrayList;


public class Trace {
	public static ArrayList<TraceRecord> records = new ArrayList<TraceRecord>();
	public static int ID = 0;
	
	// Add record to the trace and return the assoicated ID.
	public static int AddRecord( int instruction )
	{
		int assignedID = ID;
		TraceRecord record = new TraceRecord(assignedID, instruction);
		records.add(record);
		
		ID++;
		
		return assignedID ;
	}
	
	// Get record by ID
	public static TraceRecord GetRecord( int id )
	{
		return records.get(id);
	}
}
