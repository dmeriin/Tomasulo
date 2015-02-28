
public class OpCodes {
	public static final byte LD_OPCODE = 0;
	public static final byte ST_OPCODE = 1;
	public static final byte JUMP_OPCODE = 2;
	public static final byte BEQ_OPCODE = 3;
	public static final byte BNE_OPCODE = 4;
	public static final byte ADD_OPCODE = 5;
	public static final byte ADDI_OPCODE = 6;
	public static final byte SUB_OPCODE = 7;
	public static final byte SUBI_OPCODE = 8;
	public static final byte ADD_S_OPCODE = 9;
	public static final byte SUB_S_OPCODE = 10;
	public static final byte MULT_S_OPCODE = 11;
	public static final byte HALT_OPCODE = 12;
	public static final byte NOT_SUPPORTED = 13;
	
	public static boolean isOpSetToFloat(byte opcode)
	{
		boolean retVal = false;
		
		switch ( opcode )
		{
			case OpCodes.ADD_S_OPCODE:
			case OpCodes.SUB_S_OPCODE:
			case OpCodes.MULT_S_OPCODE:
			case OpCodes.LD_OPCODE:
				retVal = true;
				break;
		}
		
		return retVal; 
	}
	
	public static boolean isOpSetToInt(byte opcode)
	{
		boolean retVal = false;
		
		switch ( opcode )
		{
			case OpCodes.ADDI_OPCODE:
			case OpCodes.SUBI_OPCODE:				
			case OpCodes.ADD_OPCODE:
			case OpCodes.SUB_OPCODE:
				retVal = true;
				break;
		}
		
		return retVal; 
	}
	
}
