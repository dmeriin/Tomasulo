
public class MemBufferRow {
	int ID;
	int ROB;
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
