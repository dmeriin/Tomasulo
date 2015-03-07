/*
 * In the Execution stage we calculate the results of the operation in the Function Units of the processor
 * In each cycle we start in each FU at most one operation, and advanced one step in the pipe the operations that already start using it.
 * Because of the fact that at most one operation starts in each cycle for each FU, at most one operation can finish in each FU each cycle.
 * When operation is done in the current cycle we save the appropriate row and the calculated result,
 * so we will be able to write this result to the appropriate ROB row and other reservation stations in the write result(write CDB) stage.
 */
public class Execution {
	

	public final static int BRANCH_NOT_TAKEN = -1;
	
	//protected static fields that will be use in write result stage
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
	
	//private static fields that are in use only in execution
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
			//Integer operation finished last cycle so prepare the appropriate IntRow for write result stage
			ReadyIntRow = Utils.IntReserveStation[CurrIntRowIndex];
			//save the index of the integer operation in the Integer reservation station so we can remove the row from the 
			//reservation station during the write result stage
			ReadyIntRowIndex = CurrIntRowIndex;
			//prepare the result of the operation for the write result stage
			AluIntResult = CurrAluIntResult;
			//after saving the data, erase CurrIntRowIndex and CurrAluIntResult
			CurrIntRowIndex = -1;
			CurrAluIntResult = Integer.MIN_VALUE;
		}
		if(CurrLdRowIndex != -1 && Utils.LoadBuffer[CurrLdRowIndex] != null && Utils.LoadBuffer[CurrLdRowIndex].Busy==false){
			//load word operation finished last cycle so prepare the appropriate MemBufferRow for write result stage
			ReadyLdRow = Utils.LoadBuffer[CurrLdRowIndex];
			//save the index of the load operation in the load Buffer so we can remove the row from the 
			//load Buffer during the write result stage
			ReadyLdRowIndex = CurrLdRowIndex;
			//prepare the result of the operation for the write result stage
			LdResult = CurrLdResult;
			//after saving the data, erase CurrLdRowIndex and CurrLdResult
			CurrLdRowIndex = -1;
			CurrLdResult = Float.MIN_VALUE;
		}
		if(CurrStRowIndex != -1 && Utils.StoreBuffer[CurrStRowIndex] != null && Utils.StoreBuffer[CurrStRowIndex].Busy==false){
			//store word operation finished last cycle so prepare the appropriate MemBufferRow for write result stage
			ReadyStRow = Utils.StoreBuffer[CurrStRowIndex];
			//save the index of the store operation in the store Buffer so we can remove the row from the 
			//store Buffer during the write result stage
			ReadyStRowIndex = CurrStRowIndex;
			//prepare the result of the operation for the write result stage
			StResult = CurrStResult;
			//after saving the data, erase CurrStRowIndex and CurrStResult
			CurrStRowIndex = -1;
			CurrStResult = Integer.MIN_VALUE;
		}
		if(CurrFpAddRowIndex != -1 && Utils.FpAddReserveStation[CurrFpAddRowIndex] != null && Utils.FpAddReserveStation[CurrFpAddRowIndex].Busy==false){
			//FP Add operation finished last cycle so prepare the appropriate ReadyFpAddRow for write result stage
			ReadyFpAddRow = Utils.FpAddReserveStation[CurrFpAddRowIndex];
			//save the index of the FP Add operation in the Fp Add Reservation Station so we can remove the row from the 
			//reservation station during the write result stage
			ReadyFpAddRowIndex = CurrFpAddRowIndex;
			//prepare the result of the operation for the write result stage
			FpAddResult = CurrFpAddResult;
			//after saving the data, erase CurrFpAddRowIndex and CurrFpAddResult
			CurrFpAddRowIndex = -1;
			CurrFpAddResult = Float.MIN_VALUE;
		}
		if(CurrFpMulRowIndex != -1 && Utils.FpMulReserveStation[CurrFpMulRowIndex] != null && Utils.FpMulReserveStation[CurrFpMulRowIndex].Busy==false){
			//FP Multiply operation finished last cycle so prepare the appropriate ReadyFpMulRow for write result stage
			ReadyFpMulRow = Utils.FpMulReserveStation[CurrFpMulRowIndex];
			//save the index of the FP Mul operation in the Fp Mul Reservation Station so we can remove the row from the 
			//reservation station during the write result stage
			ReadyFpMulRowIndex = CurrFpMulRowIndex;
			//prepare the result of the operation for the write result stage
			FpMulResult = CurrFpMulResult;
			//after saving the data, erase CurrFpMulRowIndex and CurrFpMulResult
			CurrFpMulRowIndex = -1;
			CurrFpMulResult = Float.MIN_VALUE;
		}
		
		/*
		 * find Integer operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.IntReserveStation.length; i++)
		{
			//check if there is an operation in this row and that the operation didn't started yet
			if(Utils.IntReserveStation[i] != null && Utils.AluIntCounters[i]==0)
			{			
				//check if the operation's operands are ready.
				if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Qj == -1 && Utils.IntReserveStation[i].Qk == -1)
				{
					//if we start in this cycle check that this operation didn't issued in the current cycle
					//because we start the execution only 1 cycle after it issued
					TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						//if it didn't issued in this cycle update the counter for this row to Int delay,
						//record the current cycle as the start execution for this operation
						//and set AluInUse because no other operation can start using the Int ALU this cycle.
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
			//check if there is an operation in this row and that the operation didn't started yet
			if(Utils.StoreBuffer[i] != null && Utils.AluStCounters[i]==0)
			{
				//if AluInUse is true that means that another operation started using the Int ALU this cycle 
				//so we can't start another one
				if(AluInUse)
				{
					break;
				}
				//if no operation started to use the Int ALU yet so we check if the operation's operand is ready
				if(Utils.StoreBuffer[i].Busy && Utils.StoreBuffer[i].Qj == -1)
				{
					//if the operand and the row is ready check that this operation didn't issued in this cycle
					TraceRecord record = Trace.GetRecord(Utils.StoreBuffer[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						//if it didn't issued in this cycle update the counter for this row to Int delay,
						//record the current cycle as the start execution for this operation
						//and set AluInUse because no other operation can start using the Int ALU this cycle.
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
		
			//check if there is an operation in this row and that the operation didn't started yet
			if(Utils.LoadBuffer[i] != null && Utils.AluLdCounters[i]==0)
			{
				//if AluInUse is true that means that another operation started using the Int ALU this cycle 
				//so we can't start another one
				if(AluInUse){
					break;
				}
				//if no operation started to use the Int ALU yet, check that we didn't already finished calculate the address
				//for this load operation and that we are during the load from memory by checking the memory counter for this op.
				if(Utils.MemCounters[i] == 0)
				{
					//check if the operation's operand is ready
					if(Utils.LoadBuffer[i].Busy && Utils.LoadBuffer[i].Qj == -1){
						TraceRecord record = Trace.GetRecord(Utils.LoadBuffer[i].ID);
						//if the operand and the row is ready check that this operation didn't issued in this cycle
						if(!IsIssuedThisCycle(record))
						{	
							//if it didn't issued in this cycle update the counter for this row to Int delay,
							//record the current cycle as the start execution for this operation
							//and set AluInUse because no other operation can start using the Int ALU this cycle.
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
			//check if there is an operation in this row and that the operation didn't started yet
			if(Utils.FpAddReserveStation[i] != null && Utils.FpAddCounters[i] == 0)
			{			
				//check if the operation's operands are ready
				if(Utils.FpAddReserveStation[i].Busy && Utils.FpAddReserveStation[i].Qj == -1 && Utils.FpAddReserveStation[i].Qk == -1)
				{
					//if the operands and the row is ready check that this operation didn't issued in this cycle
					TraceRecord record = Trace.GetRecord(Utils.FpAddReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						//if it didn't issued in this cycle update the counter for this row to Fp Add delay,
						//record the current cycle as the start execution for this operation
						//and end the loop because no other operation can start using the Fp Add ALU this cycle.
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
			//check if there is an operation in this row and that the operation didn't started yet
			if(Utils.FpMulReserveStation[i] != null && Utils.FpMulCounters[i] == 0)
			{			
				//check if the operation's operands are ready
				if(Utils.FpMulReserveStation[i].Busy && Utils.FpMulReserveStation[i].Qj == -1 && Utils.FpMulReserveStation[i].Qk == -1)
				{
					//if the operands and the row is ready check that this operation didn't issued in this cycle
					TraceRecord record = Trace.GetRecord(Utils.FpMulReserveStation[i].ID);
					if(!IsIssuedThisCycle(record))
					{
						//if it didn't issued in this cycle update the counter for this row to Fp Mul delay,
						//record the current cycle as the start execution for this operation
						//and end the loop because no other operation can start using the Fp Mul ALU this cycle.
						Utils.FpMulCounters[i]=Utils.ConfigParams.MulDelay;
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
			//if the operation are in the FU then move the operation one step in the FU pipeline
			if(Utils.FpAddCounters[i]!=0){
				Utils.FpAddCounters[i]--;
				//if the FP Add operation is finished calculate the result for writing it in the next cycle.
				if (Utils.FpAddCounters[i]==0)
				{
					//save the row
					CurrFpAddRow = Utils.FpAddReserveStation[i];
					//calculate the result of the operation
					CurrFpAddResult = DoFpOperation(CurrFpAddRow.GetOpcode(),CurrFpAddRow.Vj,CurrFpAddRow.Vk);
					//in the next cycle the row needs to be remove in write result stage.
					//so mark the row as not busy and save the row index
					CurrFpAddRow.Busy=false;
					CurrFpAddRowIndex = i;
				}
			}
		}
		
		/*
		 * advance one step each of the FP Multiply operation that already started.
		 */

		for (int i = 0; i < Utils.FpMulReserveStation.length; i++) {
			//if the operation are in the FU then move the operation one step in the FU pipeline
			if(Utils.FpMulCounters[i]!=0){
				Utils.FpMulCounters[i]--;
				//if the FP Multiply operation is finished calculate the result for writing it in the next cycle.
				if (Utils.FpMulCounters[i]==0)
				{
					//save the row
					CurrFpMulRow = Utils.FpMulReserveStation[i];
					//calculate the operation result
					CurrFpMulResult = DoFpOperation(CurrFpMulRow.GetOpcode(),CurrFpMulRow.Vj,CurrFpMulRow.Vk);
					//in the next cycle the row needs to be remove in the write result stage.
					//so mark the row as not busy and save the row index
					CurrFpMulRow.Busy=false;
					CurrFpMulRowIndex = i;
				}
			}
		}
		
		/*
		 * advance one step each of the int operation that already started.
		 */
		for (int i = 0; i < Utils.IntReserveStation.length; i++) {
			//if the operation are in the FU then move the operation one step in the FU pipeline
			if(Utils.AluIntCounters[i]!=0){
				Utils.AluIntCounters[i]--;
				//if the int operation is finished calculate the result for writing it in the next cycle.
				if (Utils.AluIntCounters[i]==0)
				{
					//save the row
					CurrIntRow = Utils.IntReserveStation[i];
					//calculate the operation result
					CurrAluIntResult = DoIntOperation(CurrIntRow.GetOpcode(),CurrIntRow.Vj,CurrIntRow.Vk);
					// if the operation is a branch operation
					if(CurrIntRow.GetOpcode() == OpCodes.BEQ_OPCODE || CurrIntRow.GetOpcode() == OpCodes.BNE_OPCODE || CurrIntRow.GetOpcode() == OpCodes.JUMP_OPCODE)
					{
						//check if the result is 1 then the branch is taken so put in the result the address to jump to
						//if the jump didn't taken then we keep the result as is equal to -1
						if(CurrAluIntResult == 1)
							CurrAluIntResult = CurrIntRow.Address;
					}
					//in the next cycle the row needs to be remove in the write result stage.
					//so mark the row as not busy and save the row index
					CurrIntRow.Busy=false;
					CurrIntRowIndex = i;
				}
			}
		}
		/*
		 * advance one step each of the LD operation that already started.
		 */
		for (int i=0; i < Utils.LoadBuffer.length; i++) {
			//if the operation are in the FU then move the operation one step in the FU pipeline
			if(Utils.AluLdCounters[i]!=0)
			{
				Utils.AluLdCounters[i]--;
				//if the address calculation is finished
				if (Utils.AluLdCounters[i]==0)
				{
				//if this load operation did not started yet to load from memory 
				//and the memory is not used by another operation in this cycle,
				//we can start the load operation from memory
					
					if(Utils.MemCounters[i] == 0)
					{//update the memory counter for this row to memory delay
						Utils.MemCounters[i] = Utils.ConfigParams.MemDelay;
						//calculate the address for the load operation
						Utils.LoadBuffer[i].Address = DoIntOperation(OpCodes.LD_OPCODE, Utils.LoadBuffer[i].Vj, Utils.LoadBuffer[i].Vk);
					}
				}
			}
			//if we already finished calculate the address and already start to load from memory,
			//advance the load one step in the memory FU pipeline.
			else if(Utils.AluLdCounters[i]==0 && Utils.MemCounters[i]!=0)
			{
				
				//for the first cycle of loading from memory
				if(Utils.MemCounters[i] == Utils.ConfigParams.MemDelay)
				{
					//check if there is no other memory operation that started use the memory FU this cycle
					//and that is no memory aliasing for this address
					if(Utils.MemInUse == false && !Utils.RobTable.isMemoryAliasing(Utils.LoadBuffer[i].ROB, Utils.LoadBuffer[i].Address))
					{
						//set MemInUse to be true so no other memory operation will start use the memory FU this cycle
						//so no other memory operation can start using the memory FU this cycle
						Utils.MemInUse = true;
					}
					else
					{
						continue;
					}
				}
				//advance one step in the memory FU pipeline
				Utils.MemCounters[i]--;
				//if we finish load from memory we can write the result in the next cycle.
				if(Utils.MemCounters[i]==0)
				{
					//save the row
					CurrLdRow = Utils.LoadBuffer[i];
					//save the result from memory
					CurrLdResult = LoadFromMem(Utils.LoadBuffer[i].Address);
					//in the next cycle the row needs to be remove in the write result stage.
					//so mark the row as not busy and save the row index
					CurrLdRowIndex = i;
					CurrLdRow.Busy = false;
				}
			}
		}
		/*
		 * advance one step each of the SD operation that already started.
		 */
		for (int i=0; i < Utils.StoreBuffer.length; i++) {
			//if the operation are in the FU then move the operation one step in the FU pipeline
			if(Utils.AluStCounters[i]!=0){
				Utils.AluStCounters[i]--;
				//if the address calculation is finished then SD operation can be moved to the ROB in the next cycle 
				if (Utils.AluStCounters[i]==0)
				{
					//save the row
					CurrStRow = Utils.StoreBuffer[i];
					//calculate the address result
					CurrStResult = DoIntOperation(OpCodes.ST_OPCODE,CurrStRow.Vj,CurrStRow.Vk);
					//in the next cycle the row needs to be remove in the write result stage.
					//so mark the row as not busy and save the row index
					CurrStRow.Busy = false;
					CurrStRowIndex = i;
				}
			}
		}
		//in the end of the execution stage set the AluInUse to false for the next cycle.
		AluInUse = false;
		return true;
	}

	/*
	 * the function gets the operation record and check if the operation issued in the current cycle
	 */
	private static boolean IsIssuedThisCycle(TraceRecord record) {
		if(record.CycleIssued==Utils.CycleCounter)
			return true;
		return false;
	}

	/*
	 * the function gets an integer opCode, vj and vk and operates the appropriate operation on the operands vj and vk
	 */
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
	
	/*
	 * the function gets a Fp opCode, vj and vk and operates the appropriate operation on the operands vj and vk
	 */
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
	
	/*
	 * the function gets an address and the appropriate data from the memory.
	 */
	private static float LoadFromMem(int addr){
		return Float.intBitsToFloat(Utils.MainMem[Utils.AddressToRowNum(addr * 4)]);
	}
}
