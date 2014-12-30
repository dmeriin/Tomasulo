
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
	boolean Taken;
	
	
	
	public IntegerReserveRow(byte opcode, int id, int vk, int vj, int qj,
			int qk, int rob, boolean busy, int address, boolean taken) {
		super();
		Opcode = opcode;
		ID = id;
		Vk = vk;
		Vj = vj;
		Qj = qj;
		Qk = qk;
		ROB = rob;
		Busy = busy;
		Address = address;
		Taken = taken;
	}



	public byte GetOpcode(){
		return (byte) (this.Opcode & 0x0F);
	}
}
