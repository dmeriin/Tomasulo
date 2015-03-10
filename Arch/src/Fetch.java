public class Fetch {

	// Reads instruction from memory and changes PC according to relevance in BTB.
	public static boolean run(){
		
		// Returns if insturction queue is full.
		if(Utils.InstructionQueue.size()==Utils.InsturctionQueueSize)
		{
			return true;
		}
		int instruction = Utils.MainMem[Utils.AddressToRowNum(Utils.PC)];
		int id = Trace.AddRecord(instruction);
		// For internal use.
		Trace.GetRecord(id).CycleFetch = Utils.CycleCounter;
		
		// Find PC in BTB, if found change the PC, otherwise increment the pc by 4.
		Integer tempPc = Utils.BTB.get(Utils.PC);
		if(tempPc!=null)
		{
			Utils.InstructionQueue.add(new InstructionContainer(instruction, true,id, Utils.PC));
			Utils.PC = tempPc;
		}
		else 
		{
			Utils.InstructionQueue.add(new InstructionContainer(instruction, false,id, Utils.PC));
			Utils.PC +=4;
		}	
		return true;
	}
}