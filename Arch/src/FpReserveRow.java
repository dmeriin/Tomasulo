

public class FpReserveRow {

	private byte Opcode;
	int ID;
	float Vk;
	float Vj;
	int Qj;
	int Qk;
	int ROB;
	boolean Busy;
	
	
	// TODO: is needed?
	public FpReserveRow(byte opcode, int id, float vk, float vj, int qj, int qk,
			int rob) {
		Opcode = opcode;
		ID = id;
		Vk = vk;
		Vj = vj;
		Qj = qj;
		Qk = qk;
		ROB = rob;
		Busy = true;
	}
	
	public FpReserveRow( byte opcode, int id )
	{
		this.Opcode = opcode;
		this.ID = id;
		this.Busy = true;
	}
	
	public byte GetOpcode(){
		return (byte) (this.Opcode & 0x0F);
	}
}
