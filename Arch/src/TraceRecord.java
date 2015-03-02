
public class TraceRecord {
	public int ID;
	public String Instruction ;
	public int CycleIssued ;
	public int CycleExeuctedStart ;
	public int WriteCdb ;
	public int CycleCommit ;
	
	public int CycleFetch; // Doesn't enter to logged trace.
	
	public TraceRecord(int id, int Instruction ){
		WriteCdb = -1;
		this.ID = id;
		this.Instruction = String.format("%08x",Instruction);
		
		this.CycleIssued = -1;
		this.CycleExeuctedStart = -1;
		this.CycleCommit = -1;
	}

}
