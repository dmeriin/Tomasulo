
public class WriteCDB {

	
	private static void writeCDB_Int_ALU( RobRow robRow )
	{
		
		// Notify Rob
		robRow.Value = Execution.AluIntResult;
		robRow.Ready = true;
		
		// Update reservation stations
		for ( IntegerReserveRow row :  Utils.IntReserveStation )
		{
			// if non empty, move the value that the operation was waiting for to VJ and Vk ( if needed ).
			if ( row != null )
			{
				if (row.Qj == Execution.ReadyIntRow.ROB )
				{
					row.Vj = Execution.AluIntResult;
					row.Qj = -1;
				}
				
				if (row.Qk == Execution.ReadyIntRow.ROB  )
				{
					row.Vk = Execution.AluIntResult;
					row.Qk = -1;
				}
			}
			
		}
		
		// Remove row from resv stat
		Utils.IntReserveStation[Execution.ReadyIntRowIndex] = null;
	}
	
	private static void deleteRobFromResvStat ( byte opcode, int robID )
	{
		switch (opcode)
		{
			case OpCodes.ADD_S_OPCODE:
			case OpCodes.SUB_S_OPCODE:
				ResvStatHandler.removeRowFromRestStatByRobID_Fp(Utils.FpAddReserveStation, robID);
				break;
			
			case OpCodes.MULT_S_OPCODE:
				ResvStatHandler.removeRowFromRestStatByRobID_Fp(Utils.FpMulReserveStation, robID);
				break;
			
			case OpCodes.ADDI_OPCODE:
			case OpCodes.SUBI_OPCODE:				
			case OpCodes.ADD_OPCODE:
			case OpCodes.SUB_OPCODE:
			case OpCodes.BEQ_OPCODE:
			case OpCodes.BNE_OPCODE:
				ResvStatHandler.removeRowFromRestStatByRobID_Int(Utils.IntReserveStation, robID);
				break;	
				
			case OpCodes.LD_OPCODE:
				ResvStatHandler.removeRowFromRestStatByRobID_Mem(Utils.LoadBuffer, robID);
					
				break;
				
			case OpCodes.ST_OPCODE:
				ResvStatHandler.removeRowFromRestStatByRobID_Mem(Utils.StoreBuffer, robID);
				break;
		}
	}
	
	// When deleting a rob record, regiser status table must be upadted in case the rob is assoicated with any register.
	// The new rob id that will be set is a rob that is trying to change the same register and is between hean and robID.
	private static void updateRegisterTable(byte opcode, int robID) {
		switch (opcode)
		{
			// Float register table changing ops
			case OpCodes.ADD_S_OPCODE:
			case OpCodes.SUB_S_OPCODE:
			case OpCodes.MULT_S_OPCODE:
			case OpCodes.LD_OPCODE:
				for (int i = 0 ; i < Utils.FpStatusTable.length ; i ++)
				{
					if ( Utils.FpStatusTable[i].Rob == robID )
					{
						// set the last rob for reigster table. Before robID, for register i, true indicated that the register is a float register.
						Utils.RobTable.setLastRobForRegisterTable( robID, i, true );
					}
				}
				break;

			// Int register table changing ops
			case OpCodes.ADDI_OPCODE:
			case OpCodes.SUBI_OPCODE:				
			case OpCodes.ADD_OPCODE:
			case OpCodes.SUB_OPCODE:
				for (int i = 0 ; i < Utils.IntRegStatusTable.length ; i ++)
				{
					if ( Utils.IntRegStatusTable[i].Rob == robID )
					{
						// set the last rob for reigster table. Before robID, for register i, false indicated that the register is an int register.
						Utils.RobTable.setLastRobForRegisterTable( robID, i, false );
					}
				}
				break;	
				
		}
	}
	
	private static void writeCDB_Branch( byte opcode, RobRow robRow )
	{
		
		boolean actuallyTaken = !( Execution.AluIntResult == Execution.BRANCH_NOT_TAKEN );
		boolean predicatedTaken = Execution.ReadyIntRow.Taken;
		
		// No need to write value to rob for branch ops
		robRow.Ready = true;
		
		if ( (actuallyTaken && predicatedTaken) || (!actuallyTaken && !predicatedTaken))  
		{
			//do nothing because guess was successful .
		}
		else // predication was wrong
		{
			//Flush of all rows between current row to head of rob is needed.
			flushOnFalsePredicition(opcode, actuallyTaken, predicatedTaken, 
									Execution.ReadyIntRow.ROB, Execution.ReadyIntRow.PC, Execution.AluIntResult );
			
			// Update pc
			Utils.PC = Execution.AluIntResult;
		}
		
		// Remove row from resv stat
		Utils.IntReserveStation[Execution.ReadyIntRowIndex] = null;
	}

	public static void flushOnFalsePredicition(	byte opcode, boolean actuallyTaken,
												boolean predicatedTaken, int robID, int pc, int address) {
		
		//Remove related rows from different reservation stations and update register table.
		int temp = Utils.RobTable.Increment(robID);
		while (temp!=Utils.RobTable.tail) {
			deleteRobFromResvStat( Utils.RobTable.queue[temp].GetOpcode(), temp);
			updateRegisterTable(  Utils.RobTable.queue[temp].GetOpcode(), temp);
			temp = Utils.RobTable.Increment(temp);
		}
		
		// Flush ROB table after the current ROB id ( untill tail )
		Utils.RobTable.FlushAfter(robID);
		
		// Flust instruction queue
		Utils.InstructionQueue.clear();
		
		// Branch was not taken but predicated to be taken
		if (( !actuallyTaken && predicatedTaken ) )
		{
			// delete row from btb.
			Utils.BTB.remove(pc);				
		}
		
		// Branch was taken but predicated not to be taken
		if (( actuallyTaken && !predicatedTaken ) )
		{
			// If BTB is at its max capacity, remove an item from the list.
			if (Utils.BTB.size() >= 16)
			{
				Integer keyToRemove = (Integer) Utils.BTB.keySet().toArray()[0];
				Utils.BTB.remove(keyToRemove);
			}
			
			// add row to the btb.
			Utils.BTB.put(pc, address);
		}
	}

	private static void writeCDB_Int()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyIntRow.ROB];
		byte opcode = Execution.ReadyIntRow.GetOpcode();			
		
		switch ( opcode )
		{
			case OpCodes.BEQ_OPCODE:
			case OpCodes.BNE_OPCODE:
				writeCDB_Branch( opcode, robRow );
				break;
			default: // alu ops
				writeCDB_Int_ALU( robRow );
				break;
		}
	}
	
	private static void writeCDB_FpAdd()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyFpAddRow.ROB];
		// Notify Rob
		robRow.Value = Execution.FpAddResult;
		robRow.Ready = true;
		
		// Update reservation stations
		UpdateFpResvStat(Utils.FpAddReserveStation, Execution.ReadyFpAddRow.ROB, Execution.FpAddResult);
		UpdateFpResvStat(Utils.FpMulReserveStation, Execution.ReadyFpAddRow.ROB, Execution.FpAddResult);
		
		// Remove row from resv stat
		Utils.FpAddReserveStation[Execution.ReadyFpAddRowIndex] = null;
		
		
	}

	private static void UpdateFpResvStat( FpReserveRow[] table, int robID, float value ) {
		for ( FpReserveRow row :  table )
		{
			// if non empty, move the value that the operation was waiting for to VJ and Vk ( if needed ).
			if ( row != null )
			{
				if (row.Qj == robID )
				{
					row.Vj = value;
					row.Qj = -1;
				}
				
				if (row.Qk == robID )
				{
					row.Vk = value;
					row.Qk = -1;
				}
			}
			
		}
	}
	
	private static void writeCDB_FpMul()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyFpMulRow.ROB];
		// Notify Rob
		robRow.Value = Execution.FpMulResult;
		robRow.Ready = true;
		
		// Update reservation stations
		UpdateFpResvStat(Utils.FpAddReserveStation, Execution.ReadyFpMulRow.ROB, Execution.FpMulResult);
		UpdateFpResvStat(Utils.FpMulReserveStation, Execution.ReadyFpMulRow.ROB, Execution.FpMulResult);
		
		// Remove row from resv stat
		Utils.FpMulReserveStation[Execution.ReadyFpMulRowIndex] = null;
	}
	
	private static void writeCDB_Ld()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyLdRow.ROB];
		// Notify Rob
		robRow.Value = Execution.LdResult;
		robRow.Ready = true;
		
		// Update reservation stations
		UpdateFpResvStat(Utils.FpAddReserveStation, Execution.ReadyLdRow.ROB, Execution.LdResult);
		UpdateFpResvStat(Utils.FpMulReserveStation, Execution.ReadyLdRow.ROB, Execution.LdResult);
		
		// Remove row from resv stat
		Utils.LoadBuffer[Execution.ReadyLdRowIndex] = null;
	}
	
	private static void writeCDB_St()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyStRow.ROB];
		// Notify Rob
		robRow.Destination = Execution.StResult;
		robRow.Ready = true;
		
		// Remove row from resv stat
		Utils.StoreBuffer[Execution.ReadyStRowIndex] = null;
	}
	
	public static void run()
	{
		// if row  is different than null, it means a row has finished. 
		
		if ( Execution.ReadyIntRow != null )
		{
			writeCDB_Int();
			
			byte opcode = Execution.ReadyIntRow.GetOpcode();
			
			if ( 	opcode !=  OpCodes.BEQ_OPCODE && 
					opcode !=  OpCodes.BNE_OPCODE &&
					opcode !=  OpCodes.JUMP_OPCODE)
			{
				Trace.GetRecord(Execution.ReadyIntRow.ID).WriteCdb = Utils.CycleCounter;
			}
			Execution.ReadyIntRow = null;
		}
		
		if ( Execution.ReadyFpAddRow != null )
		{
			writeCDB_FpAdd();
			
			Trace.GetRecord(Execution.ReadyFpAddRow.ID).WriteCdb = Utils.CycleCounter;
			Execution.ReadyFpAddRow = null;
		}
		
		if ( Execution.ReadyFpMulRow != null )
		{
			writeCDB_FpMul();
			
			Trace.GetRecord(Execution.ReadyFpMulRow.ID).WriteCdb = Utils.CycleCounter;
			Execution.ReadyFpMulRow = null;
		}
		
		if ( Execution.ReadyLdRow != null )
		{
			writeCDB_Ld();
			
			Trace.GetRecord(Execution.ReadyLdRow.ID).WriteCdb = Utils.CycleCounter;
			Execution.ReadyLdRow = null;
		}
		
		if ( Execution.ReadyStRow != null )
		{
			writeCDB_St();
			
			Execution.ReadyStRow = null;
		}
		
	}
	
}
