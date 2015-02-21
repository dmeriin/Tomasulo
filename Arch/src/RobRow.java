
public class RobRow {

	private byte Opcode;
	int ID;
	int Destination;
	Object Value;
	boolean Ready;
	
	
	/*public RobRow() {
		this((byte)-1,-1,-1,null,false);
	}*/
	
	public RobRow(byte opcode, int id, int destination, Object value,
			boolean ready) {
		super();
		Opcode = opcode;
		ID = id;
		Destination = destination;
		Value = value;
		Ready = ready;
	}



	public byte GetOpcode(){
		return (byte) (this.Opcode & 0x0F);
	}
}
