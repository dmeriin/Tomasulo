
public class Commit {

	private static boolean duringSTCommit=false;
	private static int CommitSTCounter = 0;
	

	public static boolean run(){
		RobQueue ROB = Utils.RobTable;
		if ( ROB.head !=  -1  )
		{
			RobRow head = ROB.queue[ROB.head];
			if ( head != null )
			{
				TraceRecord record = Trace.GetRecord(head.ID);
				
				//check if the head of the rob is insert in this cycle if true then don't commit the head this cycle.
				if(headInsertThisCycle(head,record)){
					Utils.MemInUse = false;
					return true;
				}
				if(head.Ready){
					byte op = head.GetOpcode();
					if (op != OpCodes.ST_OPCODE)
					{
						switch (op) {
						
						case OpCodes.LD_OPCODE:
						case OpCodes.ADD_S_OPCODE:
						case OpCodes.SUB_S_OPCODE:
						case OpCodes.MULT_S_OPCODE:
							Utils.FpStatusTable[head.Destination].Value=(float) head.Value;
							if(Utils.FpStatusTable[head.Destination].Rob == ROB.head)
								Utils.FpStatusTable[head.Destination].Rob = RobQueue.INVALID_ROB_ID;
							break;
						case OpCodes.ADD_OPCODE:
						case OpCodes.SUB_OPCODE:
						case OpCodes.ADDI_OPCODE:
						case OpCodes.SUBI_OPCODE:
							Utils.IntRegStatusTable[head.Destination].Value = (int) head.Value;
							if(Utils.IntRegStatusTable[head.Destination].Rob == ROB.head)
								Utils.IntRegStatusTable[head.Destination].Rob = RobQueue.INVALID_ROB_ID;
							break;
						case OpCodes.JUMP_OPCODE:
						case OpCodes.BEQ_OPCODE:
						case OpCodes.BNE_OPCODE:
							break;
						case OpCodes.HALT_OPCODE:
							Utils.Halt=true;
							break;
						default:
							Utils.Halt=true;
							ROB.Delete(ROB.head);
							//TODO Print balagan
							return true;
						}
						record.CycleCommit= Utils.CycleCounter;
						ROB.Delete(ROB.head);
					}
					else
					{
						if(duringSTCommit)
						{
							CommitSTCounter--;
							if(CommitSTCounter==0)
							{
								duringSTCommit = false;
								Utils.MainMem[Utils.AddressToRowNum(head.Destination)] = Float.floatToIntBits(Utils.FpStatusTable[(int) head.Value].Value);
							}
						}
						else
						{
							if(Utils.MemInUse == false)
							{
								record.CycleCommit= Utils.CycleCounter;
								ROB.Delete(ROB.head);
								CommitSTCounter = Utils.ConfigParams.MemDelay-1;
								duringSTCommit = true;
							}
						}
					}
				}
			}
		}
		// this is the last step in this cycle so we set the MemInUse to false for next cycle.
		Utils.MemInUse = false;
		return true;
	}


	private static boolean headInsertThisCycle(RobRow head, TraceRecord record) {
		// TODO Auto-generated method stub
		switch(head.GetOpcode()){
		case OpCodes.JUMP_OPCODE :
		case OpCodes.HALT_OPCODE :
		case OpCodes.NOT_SUPPORTED :
			if(record.CycleIssued == Utils.CycleCounter)
				return true;
			return false;
		case OpCodes.ST_OPCODE :
		case OpCodes.BEQ_OPCODE :
		case OpCodes.BNE_OPCODE :
			return false;
		default :
			if(record.WriteCdb==Utils.CycleCounter)
				return true;
			return false;
		}
	}
}
