
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
					row.Qj = 0;
				}
				
				if (row.Qk == Execution.ReadyIntRow.ROB  )
				{
					row.Vk = Execution.AluIntResult;
					row.Qk = 0;
				}
			}
			
		}
		
		// Update int reg table only if the robID is the current rob id
		
		if ( Utils.IntRegStatusTable[robRow.Destination].Rob == Execution.ReadyIntRow.ROB )
		{
			Utils.IntRegStatusTable[robRow.Destination].Value = Execution.AluIntResult;
			Utils.IntRegStatusTable[robRow.Destination].Rob = RobQueue.INVALID_ROB_ID;
		}
		
		// Remove row from resv stat
		Utils.IntRegStatusTable[Execution.ReadyIntRowIndex] = null;
	}
	
	private static void writeCDB_Int_AddressCalc( IntegerReserveRow row, RobRow robRow, int rowIndex )
	{

		Object[] memBuffer ;
		
		if ( row.GetOpcode() == OpCodes.ST_OPCODE )
		{
			memBuffer = Utils.StoreBuffer;
		}
		else
		{
			memBuffer = Utils.LoadBuffer;
		}
		
		// if buffer is empty don't do anything. Next cycles will try to move the row from int resv stat to the associated mem buffer.
		if (!ResvStatHandler.IsResvStatFull(memBuffer))
		{
			
			// Notify Rob only for store.
			// Store is ready when address is ready, since it is performed only on commit and it reads the register to save from register table anyway.
			if ( row.GetOpcode() == OpCodes.ST_OPCODE )
			{
				robRow.Ready = true;
			}
			
			// add to buffer
			MemBufferRow memRow = new MemBufferRow(row.ID, row.ROB, row.Address);
			ResvStatHandler.AddRowToResvStat(memBuffer, memRow);
			
			// Remove row from resv stat
			Utils.IntRegStatusTable[rowIndex] = null;
		}
		
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
				// If not found on load buffer, look up on int reservation station.
				if (!ResvStatHandler.removeRowFromRestStatByRobID_Mem(Utils.LoadBuffer, robID) )
				{
					ResvStatHandler.removeRowFromRestStatByRobID_Int(Utils.IntReserveStation, robID) ;
				}
					
				break;
				
			case OpCodes.ST_OPCODE:
				// If not found on store buffer, look up on int reservation station.
				if (!ResvStatHandler.removeRowFromRestStatByRobID_Mem(Utils.StoreBuffer, robID))
				{
					ResvStatHandler.removeRowFromRestStatByRobID_Int(Utils.IntReserveStation, robID) ;
				}
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
				for (int i = 0 ; i < Utils.FpStatusTable.length ; i ++)
				{
					if ( Utils.FpStatusTable[i].Rob == robID )
					{
						// set the last rob for reigster table. Before robID, for register i, true indicated that the register is an int register.
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
			//Remove related rows from different reservation stations and update register table.
			int temp = Utils.RobTable.Increment(Execution.ReadyIntRow.ROB);
			while (temp!=Utils.RobTable.head) {
				deleteRobFromResvStat( opcode, Execution.ReadyIntRow.ROB);
				updateRegisterTable( opcode, Execution.ReadyIntRow.ROB);
				Utils.RobTable.Increment(temp);
			}
			
			// Flush ROB table after the current ROB id ( untill tail )
			Utils.RobTable.FlushAfter(Execution.ReadyIntRow.ROB);
			
			// Flust instruction queue
			Utils.InstructionQueue.clear();
			
			// Branch was not taken but predicated to be taken
			if (( !actuallyTaken && predicatedTaken ) )
			{
				// delete row from btb.
				Utils.BTB.remove(Execution.ReadyIntRow.PC);				
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
				Utils.BTB.put(Execution.ReadyIntRow.PC, Execution.AluIntResult);
			}

		}
		
		// Remove row from resv stat
		Utils.IntRegStatusTable[Execution.ReadyIntRowIndex] = null;
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
			case OpCodes.LD_OPCODE:
			case OpCodes.ST_OPCODE:
				writeCDB_Int_AddressCalc( Execution.ReadyIntRow, robRow, Execution.ReadyIntRowIndex );
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
		
		// Update fp reg table only if the rob id is the current rob id
		if ( Utils.FpStatusTable[robRow.Destination].Rob == Execution.ReadyFpAddRow.ROB )
		{
			Utils.FpStatusTable[robRow.Destination].Value = Execution.FpAddResult;
			Utils.FpStatusTable[robRow.Destination].Rob = RobQueue.INVALID_ROB_ID;
		}
		
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
					row.Qj = 0;
				}
				
				if (row.Qk == robID )
				{
					row.Vk = value;
					row.Qk = 0;
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
		
		// Update fp reg table only if the rob id is the current rob id
		if ( Utils.FpStatusTable[robRow.Destination].Rob == Execution.ReadyFpMulRow.ROB )
		{
			Utils.FpStatusTable[robRow.Destination].Value = Execution.FpMulResult;
			Utils.FpStatusTable[robRow.Destination].Rob = RobQueue.INVALID_ROB_ID;
		}
		
		// Remove row from resv stat
		Utils.FpMulReserveStation[Execution.ReadyFpMulRowIndex] = null;
	}
	
	private static void writeCDB_Ld()
	{
		RobRow robRow = Utils.RobTable.queue[Execution.ReadyLdRow.ROB];
		// Notify Rob
		robRow.Value = Execution.AluLdResult;
		robRow.Ready = true;
		
		// Update reservation stations
		UpdateFpResvStat(Utils.FpAddReserveStation, Execution.ReadyLdRow.ROB, Execution.AluLdResult);
		UpdateFpResvStat(Utils.FpMulReserveStation, Execution.ReadyLdRow.ROB, Execution.AluLdResult);
		
		// Update fp reg table only if the rob id is the current rob id
		if ( Utils.FpStatusTable[robRow.Destination].Rob == Execution.ReadyLdRow.ROB )
		{
			Utils.FpStatusTable[robRow.Destination].Value = Execution.AluLdResult;
			Utils.FpStatusTable[robRow.Destination].Rob = RobQueue.INVALID_ROB_ID;
		}
		
		// Remove row from resv stat
		Utils.LoadBuffer[Execution.ReadyLdRowIndex] = null;
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
		// If no new row is ready, check if there's a mem row waiting to be moved to buffer
		else
		{
			for ( int i = 0 ; i < Utils.IntReserveStation.length ; i++)
			{
				IntegerReserveRow row = Utils.IntReserveStation[i];
				// If there's a row with busy == false it means that last time the row was ready it couldn't move on to load/store buffer.
				if (row != null && row.Busy == false )
				{
					writeCDB_Int_AddressCalc( row, Utils.RobTable.queue[row.ROB], i);
					Trace.GetRecord(row.ID).WriteCdb = Utils.CycleCounter;
					break;
				}
			}
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
		
	}
	
}
