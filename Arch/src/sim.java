

public class sim {
	
	// Application entry point.
	public static void main(String[] args) 
	{
		// Excepts to receive 6 arguments, otherwise exits the application. 
		if (args.length != 6)
		{
			System.out.println(" Please Provide 6 arguments. ");
			System.out.println(" for example :  ");
			System.out.println(" java -jar sim.jar cfg.txt memin.txt memout.txt regint.txt regout.txt trace.txt ");
			
			return;
		}
		
		// Init configuration, main memory and global objects.
		Utils.ConfigParams = FileHandler.ReadConfig(args[0]);
		Utils.MainMem  = FileHandler.ReadMainMem(args[1]);
		Utils.Init(Utils.ConfigParams );
		
		System.out.println(" Simultation has started. ");
		
		// While halt ( or non-supported ) operation is not commited, run all 5 stages per cycle.
		// although stages are run after each other, actions in the same cycle won't affect other stages in that same cycle. 
		while (!Utils.Halt)
		{
			Fetch.run();
			Issue.run();
			Execution.run();
			WriteCDB.run();
			Commit.run();

			Utils.CycleCounter++;
		}
		
		// In case halt is commited but there are still more store operations on going, complete them first.
		while (Commit.hasMoreStoreToCommit())
		{
			Commit.handleStoreOfCommits();
		}
		
		// Write run data into the given paths. 
		FileHandler.WriteMemOut(Utils.MainMem, args[2]);
		FileHandler.writeRegInt(Utils.IntRegStatusTable, args[3]);
		FileHandler.writeRegOut(Utils.FpStatusTable, args[4]);
		FileHandler.WriteTraceToFile(args[5]);
		
		System.out.println(" Simultation has ended. ");
		
	}

}
