

public class FpReserveRow {

	private byte Opcode;
	int ID;
	float Vk;
	float Vj;
	int Qj;
	int Qk;
	int ROB;
	boolean Busy;
	
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
