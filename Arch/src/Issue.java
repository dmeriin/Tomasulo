
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
	
	

	
	
	static DecodedInstruction decode( int instruction )
	{
		DecodedInstruction decodedInst = new DecodedInstruction();
		decodedInst.Opcode = (byte) ( (instruction & OpcodeMask) >>> 28 );
		// TODO: validate dst,src0,src1 according to opcode and change command to halt.
		decodedInst.Dst = ( instruction & DstMask ) >>> 24;
		decodedInst.Src0 = ( instruction & Src0Mask ) >>> 20;
		decodedInst.Src1 = ( instruction & Src1Mask ) >>> 16;
		// TODO: MAke sure cast is correct for signed values (1100...)
		decodedInst.Imm	=  (short) (instruction & ImmMask);
		
		return decodedInst;
	
	}
	//
	
	public static void run()
	{
		// No need to issue instruction if rob table is full or the associated reservation station is full.
		if (!Utils.RobTable.IsFull())
		{			
			InstructionContainer headInst = Utils.InstructionQueue.peek();
			if ( headInst != null )
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
					case OpCodes.HALT_OPCODE:
					case OpCodes.NOT_SUPPORTED:
						issueNoRevStat(headInst, decodedInst);
						break;
						
				}
			}
		}
	}

	private static void issueNoRevStat(InstructionContainer headInst,
			DecodedInstruction decodedInst) {
		
		//safe to pop instruction from instruction queue, since no reservation station is required
		Utils.InstructionQueue.pop();
		
		Object value = null;
		
		if ( decodedInst.Opcode == OpCodes.JUMP_OPCODE )
		{
			// Calculate target address, assumed to have another hardware for address calculation.
			value = headInst.PC + decodedInst.Imm;
		}
		
		// Add new Rob raw, operation is ready by default.
		 Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, value, true));
		
		// Log issue
		Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		
	}
	
	private static void issueMem(InstructionContainer headInst, DecodedInstruction decodedInst )
	{
		if ( !ResvStatHandler.IsResvStatFull_Int() )
		{ 	
			int robID;
		
			//safe to pop instruction from instruction queue
			Utils.InstructionQueue.pop();

			IntRegStatus intRegStatus0 =  Utils.IntRegStatusTable[decodedInst.Src0];			
			IntegerReserveRow intReserveRow = new IntegerReserveRow(decodedInst.Opcode, headInst.ID, headInst.PC, headInst.Taken );
	
			fillIntReserveRow_Imm(intRegStatus0, decodedInst.Imm, intReserveRow );
			
			// Add row to int reservation station
			ResvStatHandler.AddRowToResvStat_Int( intReserveRow );
				
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
			
			intReserveRow.ROB = robID; 
			
			// Add row to int reservation station
			ResvStatHandler.AddRowToResvStat_Int( intReserveRow );
			
			if ( decodedInst.Opcode == OpCodes.LD_OPCODE )
			{
				// Update fp registers table only for load op
				Utils.FpStatusTable[decodedInst.Dst].Rob = robID;
			}
				
			
		}
	}

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
				intReserveRow.Address = headInst.PC +  decodedInst.Imm;
			}
			
			// Add new Rob raw
			int robID = Utils.RobTable.Add(new RobRow(decodedInst.Opcode, headInst.ID, decodedInst.Dst, null, false));
			intReserveRow.ROB = robID; 
			
			// Add row to int reservation station
			ResvStatHandler.AddRowToResvStat_Int( intReserveRow );
			
			// Update int registers table
			Utils.IntRegStatusTable[decodedInst.Dst].Rob = robID;
			
			// Log issue
			Trace.GetRecord(headInst.ID).CycleIssued = Utils.CycleCounter;
		}
	}
	
	private static void fillIntReserveRow_NotImm(IntRegStatus intRegStatus0,
			IntRegStatus intRegStatus1, IntegerReserveRow intReserveRow) {

			// Vj 
			if ( intRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
			{
				intReserveRow.Vj = intRegStatus0.Value;
				intReserveRow.Qj = 0;
			}
			else
			{
				intReserveRow.Vj = 0;
				intReserveRow.Qj = intRegStatus0.Rob;
			}
			
			if ( intRegStatus1.Rob == RobQueue.INVALID_ROB_ID )
			{
				intReserveRow.Vk = intRegStatus1.Value;
				intReserveRow.Qk = 0;
			}
			else
			{
				intReserveRow.Vk = 0;
				intReserveRow.Qk = intRegStatus1.Rob;
			}		
		
	}

	private static void fillIntReserveRow_Imm(IntRegStatus intRegStatus0,
			short imm, IntegerReserveRow intReserveRow) {

		// Vj 
		if ( intRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
		{
			intReserveRow.Vj = intRegStatus0.Value;
			intReserveRow.Qj = 0;
		}
		else
		{
			intReserveRow.Vj = 0;
			intReserveRow.Qj = intRegStatus0.Rob;
		}
		
		//Vk
		intReserveRow.Vk = imm;
		intReserveRow.Qk = 0;
	}

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
	private static void fillFpReserveRow(FpRegStatus fpRegStatus0,
			FpRegStatus fpRegStatus1, FpReserveRow fpReserveRow) {

		// Vj Float
		if ( fpRegStatus0.Rob == RobQueue.INVALID_ROB_ID )
		{
			fpReserveRow.Vj = fpRegStatus0.Value;
			fpReserveRow.Qj = 0;
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
			fpReserveRow.Qk = 0;
		}
		else
		{
			fpReserveRow.Vk = 0;
			fpReserveRow.Qk = fpRegStatus1.Rob;
		}
	}
}

