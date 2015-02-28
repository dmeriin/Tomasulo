

public class IntegerReserveRow {

	private byte Opcode;
	int ID;
	int Vk;
	int Vj;
	int Qj;
	int Qk;
	int ROB;
	boolean Busy;
	int Address;
	int PC;
	boolean Taken; // used for branch ops only. 
			       // Although pc is present and BTB could be accessed using it, by the time the BTB would be accessed to see if predication was right, 
				   //				the BTB might delete the original record. So the original decision must be saved.
	
	
	public IntegerReserveRow(byte opcode, int id, int pc, boolean taken) {
		Opcode = opcode;
		ID = id;
		Busy = true;
		PC = pc;
		Taken = taken;
	}


	public byte GetOpcode(){
		return (byte) (this.Opcode & 0x0F);
	}
}
