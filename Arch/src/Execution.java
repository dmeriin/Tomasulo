
public class Execution {
	
	public final static int BRANCH_NOT_TAKEN = -1;

	static int AluIntResult = Integer.MIN_VALUE;
	static int AluLdResult= Integer.MIN_VALUE;
	static int AluStResult= Integer.MIN_VALUE;
	static float FpAddResult= Float.MIN_VALUE;
	static float FpMulResult= Float.MIN_VALUE;
	static FpReserveRow ReadyFpAddRow;
	static FpReserveRow ReadyFpMulRow;
	static IntegerReserveRow ReadyIntRow;
	static IntegerReserveRow ReadyLdRow;
	static IntegerReserveRow ReadyStRow;
	static int ReadyIntRowIndex = -1;
	static int ReadyLdRowIndex = -1;
	static int ReadyStRowIndex = -1;
	static int ReadyFpMulRowIndex = -1;
	static int ReadyFpAddRowIndex = -1;
	
	private static int CurrAluIntResult = Integer.MIN_VALUE;
	private static int CurrAluLdResult= Integer.MIN_VALUE;
	private static int CurrAluStResult= Integer.MIN_VALUE;
	private static float CurrFpAddResult= Float.MIN_VALUE;
	private static float CurrFpMulResult= Float.MIN_VALUE;
	private static int CurrIntRowIndex = -1;
	private static int CurrLdRowIndex = -1;
	private static int CurrStRowIndex = -1;
	private static int CurrFpMulRowIndex = -1;
	private static int CurrFpAddRowIndex = -1;
	private static IntegerReserveRow CurrIntRow;
	private static IntegerReserveRow CurrLdRow;
	private static IntegerReserveRow CurrStRow;
	private static FpReserveRow CurrFpAddRow;
	private static FpReserveRow CurrFpMulRow;
	
	public static boolean run(){
		int lenOfLDBuff = Utils.ConfigParams.MemNrLoadBuffers;
		int lenOfInt = Utils.ConfigParams.IntNrReservation;
		int lenOfFpAdd = Utils.ConfigParams.AddNrReservation;
		int lenOfFpMul = Utils.ConfigParams.MulNrReservation;
		
		/*
		 * the operations that finished in the last cycle can be written to CDB in this cycle.
		 */
		if(CurrIntRow != null && CurrIntRow.Busy==false){
			ReadyIntRow = CurrIntRow;
			ReadyIntRowIndex = CurrIntRowIndex;
			AluIntResult = CurrAluIntResult;
			System.out.println("Write CDB - int: " +"op code: "+ Utils.IntReserveStation[ReadyIntRowIndex].GetOpcode() +" index: " + ReadyIntRowIndex + " Rseult: " + AluIntResult);
			CurrIntRow = null;
			CurrIntRowIndex = -1;
			CurrAluIntResult = Integer.MIN_VALUE;
		}
		if(CurrLdRow != null && CurrLdRow.Busy==false){
			ReadyLdRow = CurrLdRow;
			ReadyLdRowIndex = CurrLdRowIndex;
			AluLdResult = CurrAluLdResult;
			System.out.println("Write CDB - int: " +"op code: "+ Utils.IntReserveStation[ReadyLdRowIndex].GetOpcode() +" index: " + ReadyLdRowIndex + " Rseult: " + AluLdResult);
			CurrLdRow=null;
			CurrLdRowIndex = -1;
			CurrAluLdResult = Integer.MIN_VALUE;
		}
		if(CurrStRow != null && CurrStRow.Busy==false){
			ReadyStRow = CurrStRow;
			ReadyStRowIndex = CurrStRowIndex;
			AluStResult = CurrAluStResult;
			System.out.println("Write CDB - int: " +"op code: "+ Utils.IntReserveStation[ReadyStRowIndex].GetOpcode() +" index: " + ReadyStRowIndex + " Rseult: " + AluStResult);
			CurrStRow = null;
			CurrStRowIndex = -1;
			CurrAluStResult = Integer.MIN_VALUE;
		}
		if(CurrFpAddRow != null && CurrFpAddRow.Busy==false){
			ReadyFpAddRow = CurrFpAddRow;
			ReadyFpAddRowIndex = CurrFpAddRowIndex;
			FpAddResult = CurrFpAddResult;
			System.out.println("Write CDB - FP: " +"op code: "+ Utils.FpAddReserveStation[ReadyFpAddRowIndex].GetOpcode() +" index: " + ReadyFpAddRowIndex + " Rseult: " + FpAddResult);
			CurrFpAddRow = null;
			CurrFpAddRowIndex = -1;
			CurrFpAddResult = Float.MIN_VALUE;
		}
		if(CurrFpMulRow != null && CurrFpMulRow.Busy==false){
			ReadyFpMulRow = CurrFpMulRow;
			ReadyFpMulRowIndex = CurrFpMulRowIndex;
			FpMulResult = CurrFpMulResult;
			System.out.println("Write CDB - FP: " +"op code: "+ Utils.FpMulReserveStation[ReadyFpMulRowIndex].GetOpcode() +" index: " + ReadyFpMulRowIndex + " Rseult: " + FpMulResult);
			CurrFpMulRow = null;
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
				if(Utils.IntReserveStation[i].GetOpcode()!= OpCodes.LD_OPCODE && Utils.IntReserveStation[i].GetOpcode()!= OpCodes.ST_OPCODE)
				{
					if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vj != Integer.MIN_VALUE && Utils.IntReserveStation[i].Vk != Integer.MIN_VALUE)
					{
						System.out.println("Start - Int: "+"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i);
						Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
						TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
					}
				}
				else if(Utils.IntReserveStation[i].GetOpcode()== OpCodes.ST_OPCODE){
						if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vk != Integer.MIN_VALUE){
						System.out.println("Start - Int: "+"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i);
						Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
						TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
						}
					} 
					else if(Utils.MemCounters[i-lenOfInt] == 0)
					{
						if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vj != Integer.MIN_VALUE){
							System.out.println("Start - Int: "+"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i);
							Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
							TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
							record.CycleExeuctedStart = Utils.CycleCounter;
							break;
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
				if(Utils.FpAddReserveStation[i].Busy && Utils.FpAddReserveStation[i].Vj != Float.MIN_VALUE && Utils.FpAddReserveStation[i].Vk != Float.MIN_VALUE)
				{
					System.out.println("Start - FP: "+"op code: "+ Utils.FpAddReserveStation[i].GetOpcode() +" index: " + i);
					Utils.FpAddCounters[i]=Utils.ConfigParams.AddDelay;
					TraceRecord record = Trace.GetRecord(Utils.FpAddReserveStation[i].ID);
					record.CycleExeuctedStart = Utils.CycleCounter;
					break;
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
				if(Utils.FpMulReserveStation[i].Busy && Utils.FpMulReserveStation[i].Vj != Float.MIN_VALUE && Utils.FpMulReserveStation[i].Vk != Float.MIN_VALUE)
				{
					System.out.println("Start - FP: "+"op code: "+ Utils.FpMulReserveStation[i].GetOpcode() +" index: " + i);
					Utils.FpMulCounters[i]=Utils.ConfigParams.AddDelay;
					TraceRecord record = Trace.GetRecord(Utils.FpMulReserveStation[i].ID);
					record.CycleExeuctedStart = Utils.CycleCounter;
					break;
				}
			}
		}
		
		/*
		 * advance one step each of the FP Add operation that already started.
		 */

		for (int i = 0; i < lenOfFpAdd; i++) {
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

		for (int i = 0; i < lenOfFpMul; i++) {
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
		int i;
		for (i = 0; i < lenOfInt; i++) {
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
		for (; i < lenOfInt + lenOfLDBuff; i++) {
	
			if(Utils.AluIntCounters[i]!=0)
			{
				Utils.AluIntCounters[i]--;
				//if the address calculation is finished
				if (Utils.AluIntCounters[i]==0)
				{
				//if this load operation did not started yet to load from memory 
				//and the memory is not used by another operation in this cycle,
				//we can start the load operation from memory
					
					if(Utils.MemCounters[i-lenOfInt] == 0)
					{
						
							Utils.MemCounters[i-lenOfInt] = Utils.ConfigParams.MemDelay;
						
						/*else//the memory in use so wait for the next cycle
						{
							Utils.AluIntCounters[i]++;
						}*/
					}
				}
			}
			//load from memory, advance one step.
			else if(Utils.AluIntCounters[i]==0 && Utils.MemCounters[i-lenOfInt]!=0)
			{
				
				//set MemInUse to be true only for the first cycle of loading from memory
				if(Utils.MemCounters[i-lenOfInt] == Utils.ConfigParams.MemDelay)
				{
					if(Utils.MemInUse == false)
					{
						Utils.MemInUse = true;
						System.out.println("Start Load : " + " index: " + i);
					}
					else
					{
						continue;
					}
				}
				Utils.MemCounters[i-lenOfInt]--;
				if(Utils.MemCounters[i-lenOfInt]==0)
				{//if we finish load from memory we can write the result in the next cycle.
					CurrLdRow = Utils.IntReserveStation[i];
					CurrAluLdResult = DoIntOperation(CurrLdRow.GetOpcode(),CurrLdRow.Vj,CurrLdRow.Address);
					CurrLdRow.Address = CurrLdRow.Vj+CurrLdRow.Address;
					CurrLdRowIndex = i;
					CurrLdRow.Busy = false;
					System.out.println("Finish - int: " +"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i + " Rseult: " + CurrAluLdResult);
				}
			}
		}
		/*
		 * advance one step each of the SD operation that already started.
		 */
		for (; i < Utils.IntReserveStation.length; i++) {
			
			if(Utils.AluIntCounters[i]!=0){
				Utils.AluIntCounters[i]--;
				//if the address calculation is finished then SD operation can be moved to the ROB in the next cycle 
				if (Utils.AluIntCounters[i]==0)
				{
					CurrStRow = Utils.IntReserveStation[i];
					CurrAluStResult = DoIntOperation(CurrStRow.GetOpcode(),CurrStRow.Vk,CurrStRow.Address);
					CurrStRow.Busy = false;
					CurrStRowIndex = i;
					System.out.println("Finish - int: " +"op code: "+ Utils.IntReserveStation[i].GetOpcode() +" index: " + i + " Rseult: " + CurrAluStResult);
				}
			}
		}
		
		return true;
	}

	
	private static int DoIntOperation(byte getOpcode, int vj, int vk) {

		switch(getOpcode){
		case OpCodes.LD_OPCODE :
			return Utils.MainMem[Utils.AddressToRowNum(vj + vk)];
		case OpCodes.ST_OPCODE :
			return vj + vk;
		/*case OpCodes.JUMP_OPCODE :
			return 1;//jump always taken, return the address.*/
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
}
