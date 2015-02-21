
public class Execution {

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
			CurrIntRow = null;
		}
		if(CurrLdRow != null && CurrLdRow.Busy==false){
			ReadyLdRow = CurrLdRow;
			CurrLdRow=null;
		}
		if(CurrStRow != null && CurrStRow.Busy==false){
			ReadyStRow = CurrStRow;
			CurrStRow = null;
		}
		if(CurrFpAddRow != null && CurrFpAddRow.Busy==false){
			ReadyFpAddRow = CurrFpAddRow;
			CurrFpAddRow = null;
		}
		if(CurrFpMulRow != null && CurrFpMulRow.Busy==false){
			ReadyFpMulRow = CurrFpMulRow;
			CurrFpMulRow = null;
		}
		
		/*
		 * find int operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.IntReserveStation.length; i++)
		{
			if(Utils.IntReserveStation[i] != null)
			{			
				if(Utils.IntReserveStation[i].GetOpcode()!= OpCodes.LD_OPCODE && Utils.IntReserveStation[i].GetOpcode()!= OpCodes.ST_OPCODE)
				{
					if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vj != Integer.MIN_VALUE && Utils.IntReserveStation[i].Vk != Integer.MIN_VALUE)
					{
						Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
						TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
					}
				}
				else if(Utils.IntReserveStation[i].GetOpcode()== OpCodes.LD_OPCODE){
					//TODO check how LD operation look in the reservation station
						/*if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vj != Integer.MIN_VALUE && Utils.IntReserveStation[i].Vk != Integer.MIN_VALUE){
							Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
							TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
							record.CycleExeuctedStart = Utils.CycleCounter;
							break;
						}*/
						
					} 
					else//TODO check how SD operation look in the reservation station
					{
						/*if(Utils.IntReserveStation[i].Busy && Utils.IntReserveStation[i].Vj != Integer.MIN_VALUE && Utils.IntReserveStation[i].Vk != Integer.MIN_VALUE){
						Utils.AluIntCounters[i]=Utils.ConfigParams.IntDelay;
						TraceRecord record = Trace.GetRecord(Utils.IntReserveStation[i].ID);
						record.CycleExeuctedStart = Utils.CycleCounter;
						break;
					}*/
					}
			}
		}
		
		/*
		 * find FP Add operation that can start in the current cycle and write the operation record.
		 */
		for (int i = 0; i < Utils.FpAddReserveStation.length; i++)
		{
			if(Utils.FpAddReserveStation[i] != null)
			{			
				if(Utils.FpAddReserveStation[i].Busy && Utils.FpAddReserveStation[i].Vj != Float.MIN_VALUE && Utils.FpAddReserveStation[i].Vk != Float.MIN_VALUE)
				{
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
			if(Utils.FpMulReserveStation[i] != null)
			{			
				if(Utils.FpMulReserveStation[i].Busy && Utils.FpMulReserveStation[i].Vj != Float.MIN_VALUE && Utils.FpMulReserveStation[i].Vk != Float.MIN_VALUE)
				{
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
					FpAddResult = DoFpOperation(CurrFpAddRow.GetOpcode(),CurrFpAddRow.Vj,CurrFpAddRow.Vk);
					CurrFpAddRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					ReadyFpAddRowIndex = i;
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
					FpMulResult = DoFpOperation(CurrFpMulRow.GetOpcode(),CurrFpMulRow.Vj,CurrFpMulRow.Vk);
					CurrFpMulRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					ReadyFpMulRowIndex = i;
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
					
					//TODO depend on how the issue put the branch operation data - where is the jump address in vk or Address ?
					//if its in vk don't need the if - else, and if its in Address we need the if - else.
					
					/*if(CurrIntRow.GetOpcode() == OpCodes.BEQ_OPCODE || CurrIntRow.GetOpcode() == OpCodes.BNE_OPCODE || CurrIntRow.GetOpcode() == OpCodes.JUMP_OPCODE)
					{
						AluIntResult = DoIntOperation(CurrIntRow.GetOpcode(),CurrIntRow.Vj,CurrIntRow.Address);
					}
					else
					{*/
					AluIntResult = DoIntOperation(CurrIntRow.GetOpcode(),CurrIntRow.Vj,CurrIntRow.Vk);
					//}
					CurrIntRow.Busy=false;//in the next cycle need to be removed by issue or in this cycle by wcdb.
					ReadyIntRowIndex = i;
				}
			}
		}
		/*
		 * advance one step each of the LD operation that already started.
		 */
		for (; i < lenOfInt + lenOfLDBuff; i++) {
	
			if(Utils.AluIntCounters[i]!=0){
				Utils.AluIntCounters[i]--;
				//if the address calculation is finished
				if (Utils.AluIntCounters[i]==0)
				{
				//if this load operation did not started yet to load from memory 
				//and the memory is not used by another operation in this cycle,
				//we can start the load operation from memory
					
					if(Utils.MemCounters[i-lenOfInt] == 0)
					{
						if(Utils.MemInUse == false)
						{
							Utils.MemCounters[i-lenOfInt] = Utils.ConfigParams.MemDelay;
							Utils.MemInUse = true;
						}
						else//the memory in use so wait for the next cycle
						{
							Utils.AluIntCounters[i]++;
						}
					}
					else//we already start to load from memory, advance one step.
					{
						Utils.MemCounters[i-lenOfInt]--;
						if(Utils.MemCounters[i-lenOfInt]==0)
						{//if we finish load from memory we can write the result in the next cycle.
							CurrLdRow = Utils.IntReserveStation[i];
							AluLdResult = DoIntOperation(CurrLdRow.GetOpcode(),CurrLdRow.Vj,CurrLdRow.Address);
							CurrLdRow.Address = CurrLdRow.Vj+CurrLdRow.Address;
							ReadyLdRowIndex = i;
							CurrLdRow.Busy = false;
						}
					}
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
					AluStResult = DoIntOperation(CurrStRow.GetOpcode(),CurrStRow.Vk,CurrStRow.Address);
					CurrStRow.Busy = false;
					ReadyStRowIndex = i;
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
		case OpCodes.JUMP_OPCODE :
			return vk;//jump always taken, return the address.
		case OpCodes.BEQ_OPCODE :
			if(vj==0)
				return vk; // branch take return the address.
			return -1;//branch did not taken return -1.
		case OpCodes.BNE_OPCODE :
			if(vj!=0)
				return vk; // branch take return the address.
			return -1;//branch did not taken return -1.
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
