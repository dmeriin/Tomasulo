

public class sim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*float[] flArr = new float[16];
		flArr[0] = 3.256f;
		flArr[10] = 789789.211111f;
		flArr[2] = 100000000f;
		flArr[9] = -75.5998f;
		flArr[8] = 3.1415928f;
		FileHandler.writeRegOut(flArr, "fpint.txt");*/
		/*TraceRecord rec1 = new TraceRecord();
		rec1.CycleCommit = 10;
		rec1.CycleExeuctedStart = 9;
		rec1.CycleIssued = 3;
		rec1.Instruction = "6f1ebbaa";
		*/
		Trace.AddRecord(54);
		/*
		TraceRecord rec2 = new TraceRecord();
		rec2.CycleCommit = 100;
		rec2.CycleExeuctedStart = 95;
		rec2.CycleIssued = 22;
		rec2.Instruction = "00112200";
		rec2.WriteCdb = 5;
		*/
		Trace.AddRecord(-32);
		/*
		TraceRecord rec3 = new TraceRecord();
		rec3.CycleCommit = 103;
		rec3.CycleExeuctedStart = 93;
		rec3.CycleIssued = 21;
		rec3.Instruction = "c0112200";
		rec3.WriteCdb = 4;
		Trace.ID = 3;
		*/
		Trace.AddRecord(0x0ABB0033);
		FileHandler.WriteTraceToFile("trace.txt");
		
		
		int[] mem = FileHandler.ReadMainMem("memin.txt");
		mem[0] = 1;
		mem[1] = 3;
		FileHandler.WriteMemOut(mem, "memout.txt");
		
		System.out.println("Done");
		
	}

}
