

public class sim {
	
	public static void main(String[] args) 
	{
		if (args.length != 6)
		{
			System.out.println(" Please Provide 6 arguments. ");
			System.out.println(" for example :  ");
			System.out.println(" java -jar sim.jar cfg.txt memin.txt memout.txt regint.txt regout.txt trace.txt ");
			
			return;
		}
		
		Utils.ConfigParams = FileHandler.ReadConfig(args[0]);
		Utils.MainMem  = FileHandler.ReadMainMem(args[1]);
		Utils.Init(Utils.ConfigParams );
		
		System.out.println(" Simultation has started. ");
		
		while (!Utils.Halt)
		{
			Fetch.run();
			Issue.run();
			Execution.run();
			WriteCDB.run();
			Commit.run();

			Utils.CycleCounter++;
		}
		
		while (Commit.hasMoreStoreToCommit())
		{
			Commit.handleStoreOfCommits();
		}
		
		FileHandler.WriteMemOut(Utils.MainMem, args[2]);
		FileHandler.writeRegInt(Utils.IntRegStatusTable, args[3]);
		FileHandler.writeRegOut(Utils.FpStatusTable, args[4]);
		FileHandler.WriteTraceToFile(args[5]);
		
		System.out.println(" Simultation has ended. ");
		
	}

}
