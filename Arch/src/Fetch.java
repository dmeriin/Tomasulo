
public class Fetch {

	public static boolean run(){
		
		if(Utils.InstructionQueue.size()==Utils.InsturctionQueueSize)
		{
			return true;
		}
		int instruction = Utils.MainMem[Utils.AddressToRowNum(Utils.PC)];
		int id = Trace.AddRecord(instruction);
		boolean isBranchTaken = false;
		Integer tempPc = Utils.BTB.get(Utils.PC);
		if(tempPc==null)
		{
			Utils.PC += 4;
		}
		else
		{
			isBranchTaken = true;
			Utils.PC = tempPc;
		}
		Utils.InstructionQueue.add(new InstructionContainer(instruction, isBranchTaken,id));
		
		return true;
	}
}
