
import java.util.HashMap;
import java.util.LinkedList;

public class Utils {
	
		public static final int InsturctionQueueSize = 16;
		public static final int IntRegStatusTableSize = 16;
		public static final int FpStatusTableSize = 16;
	
		public static Config ConfigParams;
		public static int PC=0;				// Program Counter.
		public static IntRegStatus[] IntRegStatusTable = new IntRegStatus[IntRegStatusTableSize]; // Integer register table
		public static FpRegStatus[]  FpStatusTable = new FpRegStatus[FpStatusTableSize];			// Float register table
		public static int CycleCounter = 0;							// Cycle counter
		public static int[] MemCounters;							// Mem counters. used for Load operations in memory unit.
		public static int[] AluIntCounters; 						// Integer counters. Used for execution of integer operations ( ALU )
		public static int[] AluLdCounters;							// Load counters. Used for counting the address calculation stage
		public static int[] AluStCounters;							// Store counters. Used for counting the address calculation stage
		public static int[] FpAddCounters ; 						// Add Float counters. Used for execution of add/sub float operations
		public static int[] FpMulCounters ; 						// Multiplication Float counters. Used for execution of multiplication float operations
		public static RobQueue RobTable;							// Rob Table
		public static LinkedList<InstructionContainer> 	InstructionQueue = new LinkedList<InstructionContainer>(); // Instruction queue.
		public static IntegerReserveRow[] 			IntReserveStation;					// Integer operations reservation station
		public static FpReserveRow[] 				FpAddReserveStation;				// Float Add unit reservation station.
		public static FpReserveRow[] 		FpMulReserveStation;						// Float Multiplys unit reservation station.
		public static MemBufferRow[]		LoadBuffer;									// Load buffer
		public static MemBufferRow[]		StoreBuffer;								// Store buffer
		public static HashMap<Integer,Integer> BTB = new HashMap<Integer,Integer>();	// BTB
		public static int[] 					MainMem = new int[1024]; 				// Main memory
		public static boolean 					Halt = false ;							// Is program to be halted.
		public static boolean MemInUse = false;											// Was a memory operation started accessing memory this cycle. 
		
		
		static public void Init(Config conf){


			IntReserveStation = new IntegerReserveRow[conf.IntNrReservation];
			FpAddReserveStation = new FpReserveRow[conf.AddNrReservation];
			FpMulReserveStation = new FpReserveRow[conf.MulNrReservation];
			LoadBuffer = new MemBufferRow[conf.MemNrLoadBuffers];
			StoreBuffer = new MemBufferRow[conf.MemNrStoreBuffers];
			RobTable  = new RobQueue(conf.RobEntries);
			AluIntCounters = new int[conf.IntNrReservation];
			AluLdCounters = new int[conf.MemNrLoadBuffers];
			AluStCounters = new int[conf.MemNrStoreBuffers];
			FpAddCounters = new int[conf.AddNrReservation];
			FpMulCounters = new int[conf.MulNrReservation];
			MemCounters = new int[conf.MemNrLoadBuffers];
			
			// Init IntRegStatusTable & FpStatusTable
			for (int i = 0 ; i  < 16 ; i++ )
			{
				// Init all integer registers to 0 and all float register to their index
				IntRegStatusTable[i]  = new IntRegStatus(0, RobQueue.INVALID_ROB_ID );
				FpStatusTable[i] = new FpRegStatus( (float) i, RobQueue.INVALID_ROB_ID );
			}			
			
			ConfigParams = conf;
		}
		

		//Gets address and returns the aligned row number, associated with the address
		static public int AddressToRowNum(int address){
			return address / 4;
		}
		
		
	
}
