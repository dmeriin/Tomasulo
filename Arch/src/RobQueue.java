
public class RobQueue {
	RobRow[] queue;
	int QueueMaxSize;
	int	 head;
	int	 tail;
	
	final public static int INVALID_ROB_ID = -1;
	
	public int Add(RobRow row){
		if (head==tail)
			return -1;
		else{
			if(head==-1){
				head = Increment(head);
			}
			int res =tail;
			queue[tail]=row;
			tail = Increment(tail);
			return res;
		}
	}
	
	public void Delete(int index){
		queue[index]=null;
		if (head==index)
			head = Increment(head);
	}
	
	public void FlushAfter(int index){
		tail = Increment(index); 
		int temp = tail;
		while (temp!=head) {
			Delete(temp);
			temp = Increment(temp);
		}
	}
	
	public RobQueue( int queueSize ){
		this.QueueMaxSize=queueSize;
		this.queue=new RobRow[queueSize];
		head=-1;
		tail=0;
	}
	public int Increment(int index){
		return index < (QueueMaxSize -1) ? index+1 : 0;	
	}
	
	public int Decrement(int index){
		return index > 0 ? index - 1 : QueueMaxSize - 1;	
	}
	
	
	public boolean IsFull()
	{
		return head == tail;
	}

	// Sets the last robID assoicated with "regiserID" that is set between head and robID. If not found INVALID_ROB_ID is returned.
	public boolean setLastRobForRegisterTable(int robID, int registerID, boolean isFloat) 
	{
		// no need to check in case robID == head, since it has no predecessors. 
		if (robID == head)
		{
			return false;
		}
		
		int robToCheck = Decrement(robID);
		boolean passedHead = false;
		
		while (!passedHead)
		{
			RobRow row = queue[robToCheck];
			
			if ( row != null )
			{
				// If Destination register is same as given regiser id and the operation is float andn the target regiser is float , change the rob id assoicated with that register.
				if ( 	row.Destination == registerID &&
						isFloat &&
						OpCodes.isOpSetToFloat(row.GetOpcode()) )
				{
					Utils.FpStatusTable[registerID].Rob = robToCheck;
					break;
				}
				// If Destination register is same as given regiser id and the operation is float andn the target regiser is float , change the rob id assoicated with that register.
				else if (	row.Destination == registerID &&
							!isFloat &&
							OpCodes.isOpSetToInt(row.GetOpcode()) )
				{
					Utils.IntRegStatusTable[registerID].Rob = robToCheck;
					break;
				}
				
				robToCheck = Decrement(robToCheck);
				if (robToCheck == head)
				{
					passedHead = true;
					
					// If another rob wasn't found, set rob ID back to INVALID_ROB_ID
					
					
				}
			}
		}
		
		// If passedHead, then rob id wasn't set.
		return !passedHead;
		
	}

	public boolean isMemoryAliasing(int robIndex, int srcAddr) {
		robIndex = Decrement(robIndex);
		while(robIndex != head)
		{
			if(queue[robIndex].Destination == srcAddr)
			{
				return true;
			}
			robIndex = Decrement(robIndex);
		}
		if(queue[robIndex].Destination == srcAddr)
		{
			return true;
		}
	return false;
	}

	
	
}

