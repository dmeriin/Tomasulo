

public class sim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		testExecution();
		System.out.println("Done");
		
	}
	
	public static void testExecution(){
		float[] flArr = new float[5];
		flArr[0] = 3.256f;
		flArr[1] = 78.211111f;
		flArr[2] = 10.0f;
		flArr[3] = -75.5998f;
		flArr[4] = 3.1415928f;
		int[] intArr = {1,2,3,4,5};
		
		Utils.ConfigParams = new Config();
		Utils.ConfigParams.AddDelay=4;
		Utils.ConfigParams.AddNrReservation=5;
		Utils.ConfigParams.IntDelay=2;
		Utils.ConfigParams.IntNrReservation = 8;
		Utils.ConfigParams.MemDelay = 3;
		Utils.ConfigParams.MemNrLoadBuffers = 2;
		Utils.ConfigParams.MemNrStoreBuffers = 2;
		Utils.ConfigParams.MulDelay = 8;
		Utils.ConfigParams.MulNrReservation = 5;
		Utils.ConfigParams.RobEntries = 10;
		Utils.Init(Utils.ConfigParams);
		
		FpReserveRow fp1 = new FpReserveRow(OpCodes.ADD_S_OPCODE, 1, flArr[0], flArr[0], Integer.MIN_VALUE, Integer.MIN_VALUE, 0, true);
		FpReserveRow fp2 = new FpReserveRow(OpCodes.ADD_S_OPCODE, 2, flArr[0], flArr[1], Integer.MIN_VALUE, Integer.MIN_VALUE, 1, true);
		FpReserveRow fp3 = new FpReserveRow(OpCodes.MULT_S_OPCODE, 3, Float.MIN_VALUE, Float.MIN_VALUE, 1, 0, 2, true);

		
		IntegerReserveRow int1 = new IntegerReserveRow(OpCodes.ADD_OPCODE, 4, intArr[0], intArr[0], Integer.MIN_VALUE, Integer.MIN_VALUE, 3, true,-1,false);
		IntegerReserveRow int2 = new IntegerReserveRow(OpCodes.ADD_OPCODE, 5, intArr[0], intArr[1], Integer.MIN_VALUE, Integer.MIN_VALUE, 4, true,-1,false);
		IntegerReserveRow int3 = new IntegerReserveRow(OpCodes.SUB_OPCODE, 6, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, 4, 5, true,-1,false);
		
		
		IntegerReserveRow int4 = new IntegerReserveRow(OpCodes.ADDI_OPCODE, 7, intArr[0], 5, Integer.MIN_VALUE, Integer.MIN_VALUE, 6, true,-1,false);
		IntegerReserveRow int5 = new IntegerReserveRow(OpCodes.SUBI_OPCODE, 8, intArr[0], 5, Integer.MIN_VALUE, Integer.MIN_VALUE, 7, true,-1,false);
		IntegerReserveRow int6 = new IntegerReserveRow(OpCodes.ADD_OPCODE, 9, Integer.MIN_VALUE, Integer.MIN_VALUE, 6, 7, 8, true,-1,false);
		
		IntegerReserveRow int7 = new IntegerReserveRow(OpCodes.LD_OPCODE, 10, intArr[0], intArr[0], Integer.MIN_VALUE, Integer.MIN_VALUE, 3, true,-1,false);
		IntegerReserveRow int8 = new IntegerReserveRow(OpCodes.ST_OPCODE, 11, intArr[0], intArr[1], Integer.MIN_VALUE, Integer.MIN_VALUE, 4, true,-1,false);
		IntegerReserveRow int9 = new IntegerReserveRow(OpCodes.BEQ_OPCODE, 12, Integer.MIN_VALUE, Integer.MIN_VALUE, 8, 6, 9, true,-1,false);
		
		Utils.FpAddReserveStation[0] = fp1;
		Utils.FpAddReserveStation[1] = fp2;
		Utils.FpMulReserveStation[2] = fp3;
		
		Utils.IntReserveStation[0] = int1;
		Utils.IntReserveStation[1] = int2;
		Utils.IntReserveStation[2] = int3;
		Utils.IntReserveStation[3] = int4;
		Utils.IntReserveStation[4] = int5;
		Utils.IntReserveStation[5] = int6;
		Utils.IntReserveStation[8] = int7;
		Utils.IntReserveStation[10] = int8;
		Utils.IntReserveStation[6] = int9;
		for (int i = 0; i < 20; i++) {
			Trace.AddRecord(i);
		}
		for(int i =0;i<15;i++){
			System.out.println("Cycle " + i + " : ");
			Execution.run();
			if(i==4){
				Utils.FpMulReserveStation[2].Vk = flArr[0]+ flArr[0];
				Utils.IntReserveStation[5].Vj = intArr[0] + 5;
				Utils.IntReserveStation[6].Vj = intArr[0] + 5;
				
			}
			if(i==5){
				Utils.FpMulReserveStation[2].Vj = flArr[0]+ flArr[1];
				Utils.IntReserveStation[5].Vk = intArr[0] - 5;
			}
			if(i==2){
				Utils.IntReserveStation[2].Vj = intArr[0] + intArr[0];
			}
			if(i==3){
				Utils.IntReserveStation[2].Vk = intArr[0] + intArr[1];
			}
			if(i==8){
				Utils.IntReserveStation[6].Vk = intArr[0] + intArr[0];
			}
		}
	}

}
