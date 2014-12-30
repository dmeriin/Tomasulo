
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Utils {
	
		public static final int InsturctionQueueSize = 16;
	
		public static Config ConfigParams;
		public static int PC;
		public static IntRegStatus[] IntRegStatusTable = new IntRegStatus[16];
		public static FpRegStatus[]  FpStatusTable = new FpRegStatus[16];
		public static int CycleCounter = 0;
		public static int MemCounter	 = 0;
		public static int IntCounter	 = 0; 
		public static int FpAddCounter = 0; 
		public static int FpMulCounter = 0;
		public static int LoadBufCounter = 0; 
		public static int StoreBufCounter = 0; 
		public static RobQueue RobTable ;
		public static LinkedList<InstructionContainer> 	InstructionQueue = new LinkedList<InstructionContainer>();
		public static IntegerReserveRow[] 			IntReserveStation;
		public static FpReserveRow[] 				FpAddReserveStation;
		public public static FpReserveRow[] 		FpMulReserveStation;
		public static HashMap<Integer,Integer> BTB = new HashMap<Integer,Integer>(16);
		public static int[] 					MainMem = new int[1024]; 
		public static boolean 					Halt = false ;
		
		static public void Init(Config conf){
			// TODO : initalize all objects that their sizes come from config file.
			// IntReserveStation shouold be int stations + load buffers + store buffers
		}
		

		//Gets address and returns the aligned row number, associated with the address
		static public int AddressToRowNum(int address){
			return 0;
		}
	
}
