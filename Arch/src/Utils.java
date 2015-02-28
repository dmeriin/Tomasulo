
import java.util.HashMap;
import java.util.LinkedList;

public class Utils {
	
		public static final int InsturctionQueueSize = 16;
		public static final int IntRegStatusTableSize = 16;
		public static final int FpStatusTableSize = 16;
	
		public static Config ConfigParams;
		public static int PC=0;
		public static IntRegStatus[] IntRegStatusTable = new IntRegStatus[IntRegStatusTableSize];
		public static FpRegStatus[]  FpStatusTable = new FpRegStatus[FpStatusTableSize];
		public static int CycleCounter = 0;
		public static int[] MemCounters;
		public static int[] AluIntCounters; 
		public static int[] AluLdCounters;
		public static int[] AluStCounters;
		public static int[] FpAddCounters ; 
		public static int[] FpMulCounters ; 
		public static RobQueue RobTable;
		public static LinkedList<InstructionContainer> 	InstructionQueue = new LinkedList<InstructionContainer>();
		public static IntegerReserveRow[] 			IntReserveStation;
		public static FpReserveRow[] 				FpAddReserveStation;
		public static FpReserveRow[] 		FpMulReserveStation;
		public static MemBufferRow[]		LoadBuffer;
		public static MemBufferRow[]		StoreBuffer;
		public static HashMap<Integer,Integer> BTB = new HashMap<Integer,Integer>();
		public static int[] 					MainMem = new int[1024]; 
		public static boolean 					Halt = false ;
		public static boolean MemInUse = false;
		
		
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
