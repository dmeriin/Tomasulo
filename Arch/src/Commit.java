
public class Commit {


	// Gets rob row in the head of the queue and commits it.
	// Also, handles ongoing store operations.
	public static boolean run(){
		RobQueue ROB = Utils.RobTable;
		if ( ROB.head !=  -1  )
		{
			RobRow head = ROB.queue[ROB.head];
			if ( head != null )
			{
				TraceRecord record = Trace.GetRecord(head.ID);
				
				//check if the head of the rob was added in this cycle. if true then don't commit the head this cycle.
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
							// Update Float register table. If the rob associated with the register is the head rob, change it to invalid. 
							Utils.FpStatusTable[head.Destination].Value=(float) head.Value;
							if(Utils.FpStatusTable[head.Destination].Rob == ROB.head)
								Utils.FpStatusTable[head.Destination].Rob = RobQueue.INVALID_ROB_ID;
							break;
						case OpCodes.ADD_OPCODE:
						case OpCodes.SUB_OPCODE:
						case OpCodes.ADDI_OPCODE:
						case OpCodes.SUBI_OPCODE:
							// Update Integer register table. If the rob associated with the register is the head rob, change it to invalid.
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
							// For non supported operations, mark halt as true .
							Utils.Halt=true;
						}
						record.CycleCommit= Utils.CycleCounter;
						ROB.Delete(ROB.head);
					}
					else // Else this is a store operation.
					{
						// Start storing the value
						if(Utils.MemInUse == false)
						{
							record.CycleCommit= Utils.CycleCounter;
							addToRowsToStore ( head );
							ROB.Delete(ROB.head);
						}
					}
				}
			}
			
			// handle Store Of Commits in process. 
			handleStoreOfCommits();
		}
		// this is the last step in this cycle so we set the MemInUse to false for next cycle.
		Utils.MemInUse = false;
		return true;
	}

	private static RobRow[] rowsToStore = new RobRow[Utils.ConfigParams.MemNrStoreBuffers];
	private static int[] 	CommitSTCounter = new int[Utils.ConfigParams.MemDelay];
	
	// Add 'row' to the rows to store array. Doing so will ensure the row to save its value in the memory.
	private static void addToRowsToStore( RobRow row )
	{
		for (int i =0 ; i < rowsToStore.length ; i++)
		{
			if (rowsToStore[i] == null)
			{
				row.Value = Float.floatToIntBits(Utils.FpStatusTable[(int) row.Value].Value);
				rowsToStore[i] = row;
				// initialize counter
				CommitSTCounter[i] = Utils.ConfigParams.MemDelay;
				break;
			}
		}
	}
	
	// Returns true if any store operations are ongoing, false otherwise.
	public static boolean hasMoreStoreToCommit()
	{
		boolean hasMoreStore = false;
		for (int i =0 ; i < rowsToStore.length ; i++)
		{
			if (rowsToStore[i] != null)
			{
				hasMoreStore = true;
			}
		}
		
		return hasMoreStore;
	}
	
	
	//Advances one cycle per ongoing store. If it's the last cycle, the data is saed to memory and the opeation is delete from the store buffer .
	public static void handleStoreOfCommits()
	{
		for (int i =0 ; i < rowsToStore.length ; i++)
		{
			if (rowsToStore[i] != null)
			{
				CommitSTCounter[i]--;
				
				if(CommitSTCounter[i]==0)
				{
					Utils.MainMem[Utils.AddressToRowNum(rowsToStore[i].Destination * 4)] = (int) rowsToStore[i].Value;
					CommitSTCounter[i] = -1;
					
					
					// Remove row from resv stat
					for ( int j = 0 ; j < Utils.StoreBuffer.length ; j++)
					{
						if (  Utils.StoreBuffer[j] != null && Utils.StoreBuffer[j].ID == rowsToStore[i].ID )
						{
							Utils.StoreBuffer[j] = null;
						}
					}
					
					// mark as free
					rowsToStore[i] = null;
				}
			}
		}
	}
	
	// Returns true if the head row was added this cycle, false otherwise.
	private static boolean headInsertThisCycle(RobRow head, TraceRecord record) {
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
