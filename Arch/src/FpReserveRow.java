

public class FpReserveRow {

	private byte Opcode;
	int ID;
	float Vk;
	float Vj;
	int Qj;
	int Qk;
	int ROB;
	boolean Busy;
	
	public FpReserveRow(byte opcode, int id, float vj, float vk, int qj, int qk,
			int rob, boolean busy) {
		super();
		Opcode = opcode;
		ID = id;
		Vk = vk;
		Vj = vj;
		Qj = qj;
		Qk = qk;
		ROB = rob;
		Busy = busy;
	}
	
	public byte GetOpcode(){
		return (byte) (this.Opcode & 0x0F);
	}
}
