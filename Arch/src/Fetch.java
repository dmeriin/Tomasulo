public class Fetch {

	public static boolean run(){
		
		if(Utils.InstructionQueue.size()==Utils.InsturctionQueueSize)
		{
			return true;
		}
		int instruction = Utils.MainMem[Utils.AddressToRowNum(Utils.PC)];
		int id = Trace.AddRecord(instruction);
		Trace.GetRecord(id).CycleFetch = Utils.CycleCounter;
		
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