
public class MemBufferRow {
	int ID;
	int ROB;
	int Vj;
	int Qj;
	int Vk;
	boolean Busy;
	int Address;
	
	public MemBufferRow( int id, int rob, int address )
	{
		ID = id;
		ROB = rob;
		Busy = true;
		Address = address;
	}
	
}
