


class DecodedInstruction
{
	byte Opcode;
	int		Dst;
	int		Src0;
	int		Src1;
	short	Imm;
}

public class Issue {

	// Masks to instruction different parts
	final static int OpcodeMask = 	0xF0000000;
	final static int DstMask = 		0x0F000000;
	final static int Src0Mask = 	0x00F00000;
	final static int Src1Mask = 	0x000F0000;
	final static int ImmMask = 		0x0000FFFF;
	
	// Decode the given instruction. and returns a DecodedInstruction object.
	static DecodedInstruction decode( int instruction )
	{
		DecodedInstruction decodedInst = new DecodedInstruction();
		decodedInst.Opcode = (byte) ( (instruction & OpcodeMask) >>> 28 );
		
		// if op code is not familiar, set it to not supported.
		if ( ! (decodedInst.Opcode >= OpCodes.LD_OPCODE && decodedInst.Opcode <= OpCodes.HALT_OPCODE ) )
		{
			decodedInst.Opcode = OpCodes.NOT_SUPPORTED;
		}
		decodedInst.Dst = ( instruction & DstMask ) >>> 24;
		decodedInst.Src0 = ( instruction & Src0Mask ) >>> 20;
		decodedInst.Src1 = ( instruction & Src1Mask ) >>> 16;
		decodedInst.Imm	=  (short) (instruction & ImmMask);
		
		return decodedInst;
	
	}
	
	// Issue an instruction from the head of the instructino queue if rob is not full and the associated reservation table is vacant. 
	public static void run()
	{
		// No need to issue instruction if rob table is full or the associated reservation station is full.
		if (!Utils.RobTable.IsFull())
		{			
			InstructionContainer headInst = Utils.InstructionQueue.peek();
			if ( headInst != null )
			{
				// make sure head instruction wasn't fetched in the same cycle. 
				 boolean isInstructionReady = Trace.GetRecord(headInst.ID).CycleFetch != Utils.CycleCounter;
				
				 if ( isInstructionReady )
				 {				 
					DecodedInstruction decodedInst = decode ( headInst.Instruction );
					
					switch (decodedInst.Opcode)
					{
						case OpCodes.ADD_S_OPCODE:
						case OpCodes.SUB_S_OPCODE:
						case OpCodes.MULT_S_OPCODE:
							issueFloat(headInst, decodedInst);
							break;
						
						case OpCodes.ADDI_OPCODE:
						case OpCodes.SUBI_OPCODE:
							// true - stands for immediate operations  
							issueInt(headInst, decodedInst,true);
							break;
							
						case OpCodes.ADD_OPCODE:
						case OpCodes.SUB_OPCODE:
						case OpCodes.BEQ_OPCODE:
						case OpCodes.BNE_OPCODE:
							// false - stands for non immediate operations 
							issueInt(headInst, decodedInst,false);
							break;	
							
						case OpCodes.LD_OPCODE:
						case OpCodes.ST_OPCODE:
							issueMem(headInst, decodedInst);
							break;
							
						case OpCodes.JUMP_OPCODE:
							// for jump ops, if it's the first time then there was a misprediction form the BTB.
							// Therefore, flush instruction queue. 
							handleFirstTimeJump( headInst, decodedInst );
							
						case OpCodes.HALT_OPCODE:
						case OpCodes.NOT_SUPPORTED:
							issueNoRevStat(headInst, decodedInst);
							break;
							
					}
				 }
			}
		}
	}
	
	// Handle a jump encountered for the first time ( or issued after erased from BTB ) 
	static void handleFirstTimeJump( InstructionContainer headInst, DecodedInstruction decodedInst ) {
		
		// if jump is not taken, it means it didn't appear in the BTB, there for instruction queue must be flush and BTB needs to be updated.
		if ( !headInst.Taken )
		{
			// Flush instruction queue, except the first insturction which is the jump one.
			
			while ( Utils.InstructionQueue.size() != 1 )
			{
				Utils.InstructionQueue.removeLast();
			}
			
			
			
			// If BTB is at its max capacity, remove an item from the list.
			if (Utils.BTB.size() >= 16)
			{
				Integer keyToRemove = (Integer) Utils.BTB.keySet().toArray()[0];
				Utils.BTB.remove(keyToRemove);
			}
			
			// add row to the btb.
			Utils.BTB.put( headInst.PC, headInst.PC + decodedInst.Imm * 4 );
			
			Utils.PC =  headInst.PC + decodedInst.Imm * 4;
		}
		
		// Log issue
		Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		
	}

	// Issue an operation that doesn't require a reservation station ( e.g. jump, halt or not supported )
	private static void issueNoRevStat(InstructionContainer headInst,
			DecodedInstruction decodedInst) {
		
		//safe to pop instruction from instruction queue, since no reservation station is required
		Utils.InstructionQueue.pop();
		
		Object value = null;
		
		if ( decodedInst.Opcode == OpCodes.JUMP_OPCODE )
		{
			// Calculate target address, assumed to have another hardware for address calculation.
			value = headInst.PC + decodedInst.Imm * 4;
		}
		
		// Add new Rob raw, operation is ready by default.
		 Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, value, true));
		
		// Log issue
		Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		
	}
	
	// Issue a memory operation
	private static void issueMem(InstructionContainer headInst, DecodedInstruction decodedInst )
	{
		MemBufferRow[] memBuffer ;
		
		if ( decodedInst.Opcode == OpCodes.LD_OPCODE )
		{
			memBuffer = Utils.LoadBuffer;
		}
		else // decodedInst.Opcode == OpCodes.ST_OPCODE
		{
			memBuffer = Utils.StoreBuffer;
		}
		
		if ( !ResvStatHandler.IsResvStatFull(memBuffer) )
		{ 	
			int robID;
		
			//safe to pop instruction from instruction queue
			Utils.InstructionQueue.pop();

			IntRegStatus intRegStatus0 =  Utils.IntRegStatusTable[decodedInst.Src0];			
			MemBufferRow memBuffRow = new MemBufferRow( headInst.ID );
	
			fillMemBufferRow(intRegStatus0, decodedInst.Imm, memBuffRow );
			
			// Add new Rob raw
			if ( decodedInst.Opcode == OpCodes.LD_OPCODE )
			{
				robID = Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, null, false));
			}
			else
			{
				// Value in rob address will hold id of float register and not the actual value. 
				robID = Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, -1, decodedInst.Src1, false));
			}
			
			memBuffRow.ROB = robID; 
			
			// Add row to buffer station
			ResvStatHandler.AddRowToResvStat( memBuffer, memBuffRow );
			
			if ( decodedInst.Opcode == OpCodes.LD_OPCODE )
			{
				// Update fp registers table only for load op
				Utils.FpStatusTable[decodedInst.Dst].Rob = robID;
			}
			
			// Log issue
			Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
				
			
		}
	}

	// Fill Vj/Qj/Vk fields of the given memBuffRow
	private static void fillMemBufferRow(IntRegStatus intRegStatus0, short imm,
			MemBufferRow memBuffRow) {
		
		if ( intRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
		{
			memBuffRow.Vj = intRegStatus0.Value;
			memBuffRow.Qj = -1;
		}
		// Get the value from ROB only if the value is ready
		else if ( Utils.RobTable.queue[intRegStatus0.Rob].Ready )
		{
			memBuffRow.Vj = (int) Utils.RobTable.queue[intRegStatus0.Rob].Value;
			memBuffRow.Qj = -1;
		}
		else
		{
			memBuffRow.Vj = 0;
			memBuffRow.Qj = intRegStatus0.Rob;
		}
		
		memBuffRow.Vk = imm;
		
	}

	// Issue an integer operation
	private static void issueInt(InstructionContainer headInst, DecodedInstruction decodedInst, boolean isImmediate) 
	{
		if ( !ResvStatHandler.IsResvStatFull_Int() )
		{
			//safe to pop instruction from instruction queue
			Utils.InstructionQueue.pop();
			
			IntRegStatus intRegStatus0 =  Utils.IntRegStatusTable[decodedInst.Src0];
			IntRegStatus intRegStatus1 = null ;
			if (!isImmediate)
			{
				intRegStatus1 =  Utils.IntRegStatusTable[decodedInst.Src1];
			}
			IntegerReserveRow intReserveRow = new IntegerReserveRow(decodedInst.Opcode, headInst.ID, headInst.PC , headInst.Taken);
			
			// fill different fields for imm or non immediate operations
			if (isImmediate)
			{
				fillIntReserveRow_Imm(intRegStatus0, decodedInst.Imm, intReserveRow );
			}
			else
			{
				fillIntReserveRow_NotImm(intRegStatus0, intRegStatus1, intReserveRow );
			}
			
			// If branch, calc address and set into address field of reservation row.
			if ( decodedInst.Opcode == OpCodes.BEQ_OPCODE || decodedInst.Opcode == OpCodes.BNE_OPCODE )
			{
				// Calculate target address, assumed to have another hardware for address calculation.
				intReserveRow.Address = headInst.PC +  decodedInst.Imm * 4;
			}
			
			// Add new Rob raw
			int robID = Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, null, false));
			intReserveRow.ROB = robID; 
			
			// Add row to int reservation station
			ResvStatHandler.AddRowToResvStat_Int( intReserveRow );
			
			if (decodedInst.Opcode != OpCodes.BNE_OPCODE && decodedInst.Opcode != OpCodes.BEQ_OPCODE )
			{
				// Update int registers table
				Utils.IntRegStatusTable[decodedInst.Dst].Rob = robID;
			}
			
			// Log issue
			Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		}
	}
	
	// Fill Vj/Qj/Vk/Qk fields of the given intReserveRow for non-immediate operations
	private static void fillIntReserveRow_NotImm(IntRegStatus intRegStatus0,
			IntRegStatus intRegStatus1, IntegerReserveRow intReserveRow) {

			// Vj 
			if ( intRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
			{
				intReserveRow.Vj = intRegStatus0.Value;
				intReserveRow.Qj = -1;
			}
			// Get the value from ROB only if the value is ready
			else if ( Utils.RobTable.queue[intRegStatus0.Rob].Ready )
			{
				intReserveRow.Vj = (int) Utils.RobTable.queue[intRegStatus0.Rob].Value;
				intReserveRow.Qj = -1;
			}
			else
			{
				intReserveRow.Vj = 0;
				intReserveRow.Qj = intRegStatus0.Rob;
			}
			
			if ( intRegStatus1.Rob == RobQueue.INVALID_ROB_ID )
			{
				intReserveRow.Vk = intRegStatus1.Value;
				intReserveRow.Qk = -1;
			}
			// Get the value from ROB only if the value is ready
			else if ( Utils.RobTable.queue[intRegStatus1.Rob].Ready )
			{
				intReserveRow.Vk = (int) Utils.RobTable.queue[intRegStatus1.Rob].Value;
				intReserveRow.Qk = -1;
			}
			else
			{
				intReserveRow.Vk = 0;
				intReserveRow.Qk = intRegStatus1.Rob;
			}		
		
	}

	// Fill Vj/Qj/Vk/Qk fields of the given intReserveRow for immediate operations
	private static void fillIntReserveRow_Imm(IntRegStatus intRegStatus0,
			short imm, IntegerReserveRow intReserveRow) {

		// Vj 
		if ( intRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
		{
			intReserveRow.Vj = intRegStatus0.Value;
			intReserveRow.Qj = -1;
		}
		// Get the value from ROB only if the value is ready
		else if ( Utils.RobTable.queue[intRegStatus0.Rob].Ready )
		{
			intReserveRow.Vj = (int) Utils.RobTable.queue[intRegStatus0.Rob].Value;
			intReserveRow.Qj = -1;
		}
		else
		{
			intReserveRow.Vj = 0;
			intReserveRow.Qj = intRegStatus0.Rob;
		}
		
		//Vk
		intReserveRow.Vk = imm;
		intReserveRow.Qk = -1;
	}

	// Issue float operations
	private static void issueFloat(InstructionContainer headInst, DecodedInstruction decodedInst) {
		
		Object[] resvStat = null;
		
		switch ( decodedInst.Opcode )
		{
			case OpCodes.ADD_S_OPCODE:
			case OpCodes.SUB_S_OPCODE: 
				resvStat = Utils.FpAddReserveStation;
				break;
				
			case OpCodes.MULT_S_OPCODE:
				resvStat = Utils.FpMulReserveStation;
				break;
		}
		
		if ( !ResvStatHandler.IsResvStatFull(resvStat) )
		{
			//safe to pop instruction from instruction queue
			Utils.InstructionQueue.pop();
			
			FpRegStatus fpRegStatus0 =  Utils.FpStatusTable[decodedInst.Src0];
			FpRegStatus fpRegStatus1 =  Utils.FpStatusTable[decodedInst.Src1];
			FpReserveRow fpReserveRow = new FpReserveRow(decodedInst.Opcode, headInst.ID);
			
			fillFpReserveRow(fpRegStatus0, fpRegStatus1, fpReserveRow );
			
			// Add new Rob raw
			int robID = Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, null, false));
			fpReserveRow.ROB = robID; 
			
			// Add row to FP reservation station
			ResvStatHandler.AddRowToResvStat(resvStat, fpReserveRow );
			
			// Update fp registers table
			Utils.FpStatusTable[decodedInst.Dst].Rob = robID;
			
			// Log issue
			Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		}
	}
	
	// Fill Vj/Qj/Vk/Qk fields of the given fpReserveRow
	private static void fillFpReserveRow(FpRegStatus fpRegStatus0,
			FpRegStatus fpRegStatus1, FpReserveRow fpReserveRow) {

		// Vj Float
		if ( fpRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
		{
			fpReserveRow.Vj = fpRegStatus0.Value;
			fpReserveRow.Qj = -1;
		}
		// Get the value from ROB only if the value is ready
		else if ( Utils.RobTable.queue[fpRegStatus0.Rob].Ready )
		{
			fpReserveRow.Vj = (float) Utils.RobTable.queue[fpRegStatus0.Rob].Value;
			fpReserveRow.Qj = -1;
		}
		else
		{
			fpReserveRow.Vj = 0;
			fpReserveRow.Qj = fpRegStatus0.Rob;
		}
		
		// Vk Float
		
		if ( fpRegStatus1.Rob == RobQueue.INVALID_ROB_ID )
		{
			fpReserveRow.Vk = fpRegStatus1.Value;
			fpReserveRow.Qk = -1;
		}
		else if ( Utils.RobTable.queue[fpRegStatus1.Rob].Ready )
		{
			fpReserveRow.Vk = (float) Utils.RobTable.queue[fpRegStatus1.Rob].Value;
			fpReserveRow.Qk = -1;
		}
		else
		{
			fpReserveRow.Vk = 0;
			fpReserveRow.Qk = fpRegStatus1.Rob;
		}
	}
}

