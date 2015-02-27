
public class RobQueue {
	RobRow[] queue;
	int QueueMaxSize;
	int	 head;
	int	 tail;
	private int size;
	
	public int Add(RobRow row){
		if (size==QueueMaxSize)
			return -1;
		else{
			if(head==-1){
				head = Increment(head);
			}
			int res =tail;
			queue[tail]=row;
			tail = Increment(tail);
			size++;
			return res;
		}
	}
	
	public void Delete(int index){
		queue[index]=null;
		size--;
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
		size=0;
	}
	public int Increment(int index){
		return index < (QueueMaxSize -1) ? index+1 : 0;	
	}
	
	public int Size(){
		return this.size;
	}
	
}

