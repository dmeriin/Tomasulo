
enum EnumOps
{
	LD 	,	// 0 
	ST	, 	// 1
	JUMP, 	// 2
	BEQ, 	// 3
	BNE, 	// 4
	ADD, 	// 5
	ADDI, 	// 6
	SUB, 	// 7
	SUBI, 	// 8
	ADD_S, 	// 9
	SUB_S, 	// 10
	MULT_S,// 11
	HALT, 	// 12
	NOT_SUPPORTED; //13
	
    public static EnumOps fromInt( int intVal )
    {
    	EnumOps retVal;
    	switch (intVal)
    	{
    		case 0:
    			retVal = LD;
    			break;
    		case 1:
    			retVal = ST;
    			break;
    		case 2:
    			retVal = JUMP;
    			break;
    		case 3:
    			retVal = BEQ;
    			break;
    		case 4:
    			retVal = BNE;
    			break;
    		case 5:
    			retVal = ADD;
    			break;
    		case 6:
    			retVal = ADDI;
    			break;
    		case 7:
    			retVal = SUB;
    			break;
    		case 8:
    			retVal = SUBI;
    			break;
    		case 9:
    			retVal = ADD_S;
    			break;
    		case 10:
    			retVal = SUB_S;
    			break;
    		case 11:
    			retVal = MULT_S;
    			break;
    		case 12:
    			retVal = HALT;
    			break;
    		default:
    			retVal = NOT_SUPPORTED;
    			break;
    	}
    	
    	return retVal;
    }
}



class DecodedInstruction
{
	EnumOps Opcode;
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
		decodedInst.Opcode = EnumOps.fromInt(instruction & OpcodeMask);
		decodedInst.Dst = instruction & DstMask;
		decodedInst.Src0 = instruction & Src0Mask;
		decodedInst.Src1 = instruction & Src1Mask;
		// TODO: MAke sure cast is correct for signed values (1100...)
		decodedInst.Imm	=  (short) (instruction & ImmMask);
		
		return decodedInst;
	
	}
	
	public static void run()
	{
		
	}
}
