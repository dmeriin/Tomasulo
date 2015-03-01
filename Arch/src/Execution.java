
public class Execution {
	
	public final static int BRANCH_NOT_TAKEN = -1;

	static int AluIntResult = Integer.MIN_VALUE;
	static float LdResult= Float.MIN_VALUE;
	static int StResult= Integer.MIN_VALUE;
	static float FpAddResult= Float.MIN_VALUE;
	static float FpMulResult= Float.MIN_VALUE;
	static FpReserveRow ReadyFpAddRow;
	static FpReserveRow ReadyFpMulRow;
	static IntegerReserveRow ReadyIntRow;
	static MemBufferRow ReadyLdRow;
	static MemBufferRow ReadyStRow;
	static int ReadyIntRowIndex = -1;
	static int ReadyLdRowIndex = -1;
	static int ReadyStRowIndex = -1;
	static int ReadyFpMulRowIndex = -1;
	static int ReadyFpAddRowIndex = -1;
	
	private static int CurrAluIntResult = Integer.MIN_VALUE;
	private static float CurrLdResult= Float.MIN_VALUE;
	private static int CurrStResult= Integer.MIN_VALUE;
	private static float CurrFpAddResult= Float.MIN_VALUE;
	private static float CurrFpMulResult= Float.MIN_VALUE;
	private static int CurrIntRowIndex = -1;
	private static int CurrLdRowIndex = -1;
	private static int CurrStRowIndex = -1;
	private static int CurrFpMulRowIndex = -1;
	private static int CurrFpAddRowIndex = -1;
	private static IntegerReserveRow CurrIntRow;
	private static MemBufferRow CurrLdRow;
	private static MemBufferRow CurrStRow;
	private static FpReserveRow CurrFpAddRow;
	private static FpReserveRow CurrFpMulRow;
	private static boolean AluInUse = false;
	
	public static boolean run(){
		
		/*
		 * the operations that finished in the last cycle can be written to CDB in this cycle.
		 */
		if(CurrIntRowIndex != -1 && Utils.IntReserveStation[CurrIntRowIndex] != null && Utils.IntReserveStation[CurrIntRowIndex].Busy==false){
			ReadyIntRow = Utils.IntReserveStation[CurrIntRowIndex];
			ReadyIntRowIndex = CurrIntRowIndex;
			AluIntResult = CurrAluIntResult;
			System.out.println("Write CDB - int: " +"op code: "+ Utils.IntReserveStation[ReadyIntRowIndex].GetOpcode() +" index: " + ReadyIntRowIndex + " Rseult: " + AluIntResult);
			CurrIntRowIndex = -1;
			CurrAluIntResult = Integer.MIN_VALUE;
		}
		if(CurrLdRowIndex != -1 && Utils.LoadBuffer[CurrLdRowIndex] != null && Utils.LoadBuffer[CurrLdRowIndex].Busy==false){
			ReadyLdRow = Utils.LoadBuffer[CurrLdRowIndex];
			ReadyLdRowIndex = CurrLdRowIndex;
			LdResult = CurrLdResult;
			System.out.println("Write CDB - int: " +"op code: "+ OpCodes.LD_OPCODE +" index: " + ReadyLdRowIndex + " Rseult: " + LdResult);
			CurrLdRowIndex = -1;
			CurrLdResult = Float.MIN_VALUE;
		}
		if(CurrStRowIndex != -1 && Utils.StoreBuffer[CurrStRowIndex] != null && Utils.StoreBuffer[CurrStRowIndex].Busy==false){
			ReadyStRow = Utils.StoreBuffer[CurrStRowIndex];
			ReadyStRowIndex = CurrStRowIndex;
			StResult = CurrStResult;
			System.out.println("Write CDB - int: " +"op code: "+ OpCodes.ST_OPCODE +" index: " + ReadyStRowIndex + " Rseult: " + StResult);
			CurrStRowIndex = -1;
			CurrStResult = Integer.MIN_VALUE;
		}
		if(CurrFpAddRowIndex != -1 && Utils.FpAddReserveStation[CurrFpAddRowIndex] != null && Utils.FpAddReserveStation[CurrFpAddRowIndex].Busy==false){
			ReadyFpAddRow = Utils.FpAddReserveStation[CurrFpAddRowIndex];
			ReadyFpAddRowIndex = CurrFpAddRowIndex;
			FpAddResult = CurrFpAddResult;
			System.out.println("Write CDB - FP: " +"op code: "+ Utils.FpAddReserveStation[ReadyFpAddRowIndex].GetOpcode() +" index: " + ReadyFpAddRowIndex + " Rseult: " + FpAddResult);
			CurrFpAddRowIndex = -1;
			CurrFpAddResult = Float.MIN_VALUE;
		}
		if(CurrFpMulRowIndex != -1 && Utils.FpMulReserveStation[CurrFpMulRowIndex] != null && Utils.FpMulReserveStation[CurrFpMulRowIndex].Busy==false){
			ReadyFpMulRow = Utils.FpMulReserveStation[CurrFpMulRowIndex];
			ReadyFpMulRowIndex = CurrFpMulRowIndex;
			FpMulResult = CurrFpMulResult;
			System.out.println("Write CDB - FP: " +"op code: "+ Utils.FpMulReserveStation[ReadyFpMulRowIndex].GetOpcode() +" index: " + ReadyFpMulRowIndex + " Rseult: " + FpMulResult);
			CurrFpMulRowIndex = -1;
			CurrFpMulResult = Float.MIN_VALUE;
		}
		
		/*
		 * find int operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.IntReserveStation.length; i++)
		{
			if(Utils.IntReserveStation[i] != null && Utils.AluIntCounters[i]==0)
			{			
				if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Qj == -1 && Utils.IntReserveStation[i].Qk == -1)
				{
					TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						System.out.println("Start - Int: "+"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i);
						Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
						record.CycleExeuctedStart = Utils.CycleCounter;
						AluInUse = true;
						break;
					}
				}
			}
		}
		/*
		 * If Alu unit is free then find Store Address calculation that can start in the current cycle and write the Store record.
		 */
		for (int i = 0; i < Utils.StoreBuffer.length; i++) 
		{
			if(Utils.StoreBuffer[i] != null && Utils.AluStCounters[i]==0)
			{
				if(AluInUse)
				{
					break;
				}
				if(Utils.StoreBuffer[i].Busy && Utils.StoreBuffer[i].Qj == -1)
				{
					TraceRecord record = Trace.GetRecord(Utils.StoreBuffer[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						System.out.println("Start - Int: "+"op code: "+ OpCodes.ST_OPCODE +" index: " + i);
						Utils.AluStCounters[i]=Utils.ConfigParams.IntDelay;
						record.CycleExeuctedStart = Utils.CycleCounter;
						AluInUse = true;
						break;
					}
				} 
			}
		}
		/*
		 * If Alu unit is free then find Load Address calculation that can start in the current cycle.
		 */
		for (int i = 0; i < Utils.LoadBuffer.length; i++) {
		
			if(Utils.LoadBuffer[i] != null && Utils.AluLdCounters[i]==0)
			{
				if(AluInUse){
					break;
				}
				if(Utils.MemCounters[i] == 0)
				{
					if(Utils.LoadBuffer[i].Busy && Utils.LoadBuffer[i].Qj == -1){
						TraceRecord record = Trace.GetRecord(Utils.LoadBuffer[i].ID);
						if(!IsIssuedThisCycle(record))
						{
							System.out.println("Start - Int: "+"op code: "+ OpCodes.LD_OPCODE +" index: " + i);
							Utils.AluLdCounters[i]=Utils.ConfigParams.IntDelay;
							record.CycleExeuctedStart = Utils.CycleCounter;
							AluInUse = true;
							break;
						}
					}
				}
			}
		}
		
		/*
		 * find FP Add operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.FpAddReserveStation.length; i++)
		{
			if(Utils.FpAddReserveStation[i] != null && Utils.FpAddCounters[i] == 0)
			{			
				if(Utils.FpAddReserveStation[i].Busy && Utils.FpAddReserveStation[i].Qj == -1 && Utils.FpAddReserveStation[i].Qk == -1)
				{
					TraceRecord record = Trace.GetRecord(Utils.FpAddReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						System.out.println("Start - FP: "+"op code: "+ Utils.FpAddReserveStation[i].GetOpcode() +" index: " + i);
						Utils.FpAddCounters[i]=Utils.ConfigParams.AddDelay;
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
					}
				}
			}
		}
		
		/*
		 * find FP Multiply operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.FpMulReserveStation.length; i++)
		{
			if(Utils.FpMulReserveStation[i] != null && Utils.FpMulCounters[i] == 0)
			{			
				if(Utils.FpMulReserveStation[i].Busy && Utils.FpMulReserveStation[i].Qj == -1 && Utils.FpMulReserveStation[i].Qk == -1)
				{
					TraceRecord record = Trace.GetRecord(Utils.FpMulReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						System.out.println("Start - FP: "+"op code: "+ Utils.FpMulReserveStation[i].GetOpcode() +" index: " + i);
						Utils.FpMulCounters[i]=Utils.ConfigParams.AddDelay;
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
					}
				}
			}
		}
		
		/*
		 * advance one step each of the FP Add operation that already started.
		 */

		for (int i = 0; i < Utils.FpAddReserveStation.length; i++) {
			if(Utils.FpAddCounters[i]!=0){
				Utils.FpAddCounters[i]--;
				//if the FP Add operation is finished calculate the result for writing it in the next cycle.
				if (Utils.FpAddCounters[i]==0)
				{
					CurrFpAddRow = Utils.FpAddReserveStation[i];
					CurrFpAddResult = DoFpOperation(CurrFpAddRow.GetOpcode(),CurrFpAddRow.Vj,CurrFpAddRow.Vk);
					CurrFpAddRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					CurrFpAddRowIndex = i;
					System.out.println("Finish - FP: " +"op code: "+ Utils.FpAddReserveStation[i].GetOpcode() +" index: " + i + " Rseult: " + CurrFpAddResult);
				}
			}
		}
		
		/*
		 * advance one step each of the FP Multiply operation that already started.
		 */

		for (int i = 0; i < Utils.FpMulReserveStation.length; i++) {
			if(Utils.FpMulCounters[i]!=0){
				Utils.FpMulCounters[i]--;
				//if the FP Multiply operation is finished calculate the result for writing it in the next cycle.
				if (Utils.FpMulCounters[i]==0)
				{
					CurrFpMulRow = Utils.FpMulReserveStation[i];
					CurrFpMulResult = DoFpOperation(CurrFpMulRow.GetOpcode(),CurrFpMulRow.Vj,CurrFpMulRow.Vk);
					CurrFpMulRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					CurrFpMulRowIndex = i;
					System.out.println("Finish - FP: " +"op code: "+ Utils.FpMulReserveStation[i].GetOpcode() +" index: " + i + " Rseult: " + CurrFpMulResult);
				}
			}
		}
		
		/*
		 * advance one step each of the int operation that already started.
		 */
		for (int i = 0; i < Utils.IntReserveStation.length; i++) {
			if(Utils.AluIntCounters[i]!=0){
				Utils.AluIntCounters[i]--;
				//if the int operation is finished calculate the result for writing it in the next cycle.
				if (Utils.AluIntCounters[i]==0)
				{
					CurrIntRow = Utils.IntReserveStation[i];
					CurrAluIntResult = DoIntOperation(CurrIntRow.GetOpcode(),CurrIntRow.Vj,CurrIntRow.Vk);
					if(CurrIntRow.GetOpcode() == OpCodes.BEQ_OPCODE || CurrIntRow.GetOpcode() == OpCodes.BNE_OPCODE || CurrIntRow.GetOpcode() == OpCodes.JUMP_OPCODE)
					{
						if(CurrAluIntResult == 1)
							CurrAluIntResult = CurrIntRow.Address;
					}
					CurrIntRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					CurrIntRowIndex = i;
					System.out.println("Finish - int: " +"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i + " Rseult: " + CurrAluIntResult);
				}
			}
		}
		/*
		 * advance one step each of the LD operation that already started.
		 */
		for (int i=0; i < Utils.LoadBuffer.length; i++) {
	
			if(Utils.AluIntCounters[i]!=0)
			{
				Utils.AluIntCounters[i]--;
				//if the address calculation is finished
				if (Utils.AluIntCounters[i]==0)
				{
				//if this load operation did not started yet to load from memory 
				//and the memory is not used by another operation in this cycle,
				//we can start the load operation from memory
					
					if(Utils.MemCounters[i] == 0)
					{
						Utils.MemCounters[i] = Utils.ConfigParams.MemDelay;
						Utils.LoadBuffer[i].Address = DoIntOperation(OpCodes.LD_OPCODE, Utils.LoadBuffer[i].Vj, Utils.LoadBuffer[i].Vk);
					}
				}
			}
			//load from memory, advance one step.
			else if(Utils.AluLdCounters[i]==0 && Utils.MemCounters[i]!=0)
			{
				
				//set MemInUse to be true only for the first cycle of loading from memory
				if(Utils.MemCounters[i] == Utils.ConfigParams.MemDelay)
				{
					if(Utils.MemInUse == false && !Utils.RobTable.isMemoryAliasing(Utils.LoadBuffer[i].ROB, Utils.LoadBuffer[i].Address))
					{
						Utils.MemInUse = true;
						System.out.println("Start Load : " + " index: " + i);
					}
					else
					{
						continue;
					}
				}
				Utils.MemCounters[i]--;
				if(Utils.MemCounters[i]==0)
				{//if we finish load from memory we can write the result in the next cycle.
					CurrLdRow = Utils.LoadBuffer[i];
					CurrLdResult = LoadFromMem(Utils.LoadBuffer[i].Address);
					CurrLdRowIndex = i;
					CurrLdRow.Busy = false;
					System.out.println("Finish - int: " +"op code: "+ OpCodes.LD_OPCODE +" index: " + i + " Rseult: " + CurrLdResult);
				}
			}
		}
		/*
		 * advance one step each of the SD operation that already started.
		 */
		for (int i=0; i < Utils.StoreBuffer.length; i++) {
			
			if(Utils.AluStCounters[i]!=0){
				Utils.AluStCounters[i]--;
				//if the address calculation is finished then SD operation can be moved to the ROB in the next cycle 
				if (Utils.AluStCounters[i]==0)
				{
					CurrStRow = Utils.StoreBuffer[i];
					CurrStResult = DoIntOperation(OpCodes.ST_OPCODE,CurrStRow.Vj,CurrStRow.Vk);
					CurrStRow.Busy = false;
					CurrStRowIndex = i;
					System.out.println("Finish - int: " +"op code: "+ OpCodes.ST_OPCODE +" index: " + i + " Rseult: " + CurrStResult);
				}
			}
		}
		AluInUse = false;
		return true;
	}

	
	private static boolean IsIssuedThisCycle(TraceRecord record) {
		if(record.CycleIssued==Utils.CycleCounter)
			return true;
		return false;
	}


	private static int DoIntOperation(byte getOpcode, int vj, int vk) {

		switch(getOpcode){
		case OpCodes.LD_OPCODE :
			return vj + vk;
		case OpCodes.ST_OPCODE :
			return vj + vk;
		case OpCodes.BEQ_OPCODE :
			if(vj==vk)
				return 1; // branch take return the address.
			return BRANCH_NOT_TAKEN;//branch did not taken return -1.
		case OpCodes.BNE_OPCODE :
			if(vj!=vk)
				return 1; // branch take return the address.
			return BRANCH_NOT_TAKEN;//branch did not taken return -1.
		case OpCodes.ADD_OPCODE :
			return vj + vk;
		case OpCodes.ADDI_OPCODE :
			return vj + vk;
		case OpCodes.SUB_OPCODE :
			return vj - vk;
		default ://SUBI_OPCODE
			return vj - vk;
		}
		
	}
	private static float DoFpOperation(byte getOpcode, float vj, float vk) {
		switch(getOpcode)
		{
		case OpCodes.ADD_S_OPCODE :
			return vj + vk;
		case OpCodes.SUB_S_OPCODE :
			return vj - vk;
		default ://MULT_S_OPCODE
			return vj * vk;
			
		}
	}
	private static float LoadFromMem(int addr){
		return Float.intBitsToFloat(Utils.MainMem[addr * 4]);
	}
}
