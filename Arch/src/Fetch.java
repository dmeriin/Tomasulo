
public class Fetch {

	public static boolean run(){
		boolean status = true;
		if(Utils.InstructionQueue.size()==Utils.InsturctionQueueSize)
			return status;
		int instruction = Utils.MainMem[Utils.AddressToRowNum(Utils.PC)];
		int id = Trace.AddRecord(instruction);
		Integer tempPc = Utils.BTB.get(Utils.PC);
		if(tempPc==null){
			Utils.InstructionQueue.add(new InstructionContainer(instruction, false,id));
			Utils.PC += 4;
		}
		else{
			Utils.InstructionQueue.add(new InstructionContainer(instruction, true,id));
			Utils.PC = tempPc;
		}
		
		return status;
	}
}
