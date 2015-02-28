
public class MemBufferRow {
	int ID;
	int ROB;
	int Vj;
	int Qj;
	int Vk;
	boolean Busy;
	int Address;
	
	public MemBufferRow( int id )
	{
		ID = id;
		Busy = true;
	}
	
}
