
import java.util.HashMap;
import java.util.LinkedList;

public class Utils {
	
		public static final int InsturctionQueueSize = 16;
	
		public static Config ConfigParams;
		public static int PC=0;
		public static IntRegStatus[] IntRegStatusTable = new IntRegStatus[16];
		public static FpRegStatus[]  FpStatusTable = new FpRegStatus[16];
		public static int CycleCounter = 0;
		public static int[] MemCounters;
		public static int[] AluIntCounters; 
		public static int[] FpAddCounters ; 
		public static int[] FpMulCounters ; 
		public static RobQueue RobTable;
		public static LinkedList<InstructionContainer> 	InstructionQueue = new LinkedList<InstructionContainer>();
		public static IntegerReserveRow[] 			IntReserveStation;
		public static FpReserveRow[] 				FpAddReserveStation;
		public static FpReserveRow[] 		FpMulReserveStation;
		public static HashMap<Integer,Integer> BTB = new HashMap<Integer,Integer>(16);
		public static int[] 					MainMem = new int[1024]; 
		public static boolean 					Halt = false ;
		public static boolean MemInUse = false;
		
		static public void Init(Config conf){
			// TODO : initalize all objects that their sizes come from config file.
			// IntReserveStation shouold be int stations + load buffers + store buffers
			IntReserveStation = new IntegerReserveRow[conf.IntNrReservation + conf.MemNrLoadBuffers + conf.MemNrStoreBuffers];
			FpAddReserveStation = new FpReserveRow[conf.AddNrReservation];
			FpMulReserveStation = new FpReserveRow[conf.MulNrReservation];
			RobTable  = new RobQueue(conf.RobEntries);
			AluIntCounters = new int[IntReserveStation.length];
			FpAddCounters = new int[conf.AddNrReservation];
			FpMulCounters = new int[conf.MulNrReservation];
			MemCounters = new int[conf.MemNrLoadBuffers];
		}
		

		//Gets address and returns the aligned row number, associated with the address
		static public int AddressToRowNum(int address){
			return address / 4;
		}
	
}
