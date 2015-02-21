
public class Commit {

	private static boolean duringSTCommit=false;
	private static int CommitCounter = 0;
	

	public static boolean run(){
		RobQueue ROB = Utils.RobTable;
		RobRow head = ROB.queue[ROB.head];
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
						Utils.FpStatusTable[head.Destination].Rob = (short)-1;
					break;
				case OpCodes.ADD_OPCODE:
				case OpCodes.SUB_OPCODE:
				case OpCodes.ADDI_OPCODE:
				case OpCodes.SUBI_OPCODE:
					Utils.IntRegStatusTable[head.Destination].Value = (int) head.Value;
					if(Utils.IntRegStatusTable[head.Destination].Rob == ROB.head)
						Utils.IntRegStatusTable[head.Destination].Rob = (short) -1;
					break;
				case OpCodes.JUMP_OPCODE:
				case OpCodes.BEQ_OPCODE:
				case OpCodes.BNE_OPCODE:
					break;
				case OpCodes.HALT_OPCODE:
					Utils.Halt=true;
					break;
				default:
						return false;
				}
				TraceRecord record = Trace.GetRecord(head.ID);
				record.CycleCommit= Utils.CycleCounter;
				ROB.Delete(ROB.head);
			}
			else
			{
				if(duringSTCommit)
				{
					CommitCounter--;
					if(CommitCounter==0)
					{
						TraceRecord record = Trace.GetRecord(head.ID);
						record.CycleCommit= Utils.CycleCounter;
						duringSTCommit = false;
						Utils.MainMem[Utils.AddressToRowNum(head.Destination)] = (int) head.Value;
						ROB.Delete(ROB.head);
					}
				}
				else
				{
					if(Utils.MemInUse == false)
					{
						CommitCounter = Utils.ConfigParams.MemDelay-1;
						duringSTCommit = true;
					}
				}
			}
		}
		// this is the last step in this cycle so we set the MemInUse to false for next cycle.
		//and increment the CycleCounter by one.
		Utils.MemInUse = false;
		Utils.CycleCounter++;
		return true;
	}
}
