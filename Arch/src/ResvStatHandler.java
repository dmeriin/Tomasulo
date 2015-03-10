
public class ResvStatHandler {

	// Checks if resevation station is full. 
	// @resvStation - station to check on.
	// @startIndex - index to start check from.
	// @endIndex - index to end check on (exclusive).
	// 
	// Return - true if full, false otherwise.
	static boolean IsResvStatFull ( Object[] resvStation, int startIndex, int endIndex )
	{
		boolean isFull = true;
		for ( int i = startIndex ; i < endIndex; i++ )
		{
			if (resvStation[i] == null)
			{
				isFull = false;
				break;
			}
		}
		
		return isFull;
	}
	
	static public boolean IsResvStatFull_Int ( )
	{
		 return IsResvStatFull ( Utils.IntReserveStation, 0, Utils.ConfigParams.IntNrReservation );
	}
	
	
	static public boolean IsResvStatFull ( Object[] resvStation )
	{
		return IsResvStatFull (resvStation, 0, resvStation.length );
		
	}
	
	// Addes row to resvStation in the given range, assumes function is called when there is a free row. 
	// @resvStation - station to check on.
	// @row - row to add reservation stations.
	// @startIndex - index to start check from.
	// @endIndex - index to end check on (exclusive).
	// 
	// Return - index of added row.
	static int AddRowToResvStat ( Object[] resvStation, Object row, int startIndex, int endIndex )
	{
		// Assuming the function was called only when there's a free row
		for ( int i = startIndex ; i < endIndex; i++ )
		{
			if (resvStation[i] == null)
			{
				resvStation[i] = row;
				return i;
			}
		}
		
		return -1;
	}	
	
	static public int AddRowToResvStat_Int ( IntegerReserveRow row )
	{
		 return AddRowToResvStat ( Utils.IntReserveStation, row, 0, Utils.ConfigParams.IntNrReservation );
	}
	
	static public int AddRowToResvStat (Object[] resvStation, Object row )
	{
		 return AddRowToResvStat ( resvStation, row, 0, resvStation.length );
	}
	
	// Removed a floating pointer Reservation Row from the reservation station.
	// Returns true if removed, false otherwise.
	static public boolean removeRowFromRestStatByRobID_Fp(FpReserveRow[] resvStation, int robID)
	{
		boolean deleted = false;
		
		for ( int i = 0; i < resvStation.length ; i++ )
		{
			FpReserveRow row = resvStation[i];  
			if ( row!=null && row.ROB == robID )
			{
				resvStation[i] = null;
				if (resvStation == Utils.FpAddReserveStation)
				{
					Utils.FpAddCounters[i] = 0;
				}
				else
				{
					Utils.FpMulCounters[i] = 0;
				}
				deleted  = true;
			}
		}
		
		return deleted;
	}
	
	// Removed a Integer Reservation Row from the reservation station.
	// Returns true if removed, false otherwise.
	static public boolean removeRowFromRestStatByRobID_Int(IntegerReserveRow[] resvStation, int robID)
	{
		boolean deleted = false;
		
		for ( int i = 0; i < resvStation.length ; i++ )
		{
			IntegerReserveRow row = resvStation[i];  
			if ( row!=null && row.ROB == robID )
			{
				resvStation[i] = null;
				Utils.AluIntCounters[i] = 0;
				deleted  = true;
				break;
			}
		}
		
		return deleted;
	}
	
	// Removed a memory buffer Reservation Row from the reservation station.
	// Returns true if removed, false otherwise.
	static public boolean removeRowFromRestStatByRobID_Mem(MemBufferRow[] resvStation, int robID)
	{
		boolean deleted = false;
		
		for ( int i = 0; i < resvStation.length ; i++ )
		{
			MemBufferRow row = resvStation[i];  
			if ( row!=null && row.ROB == robID )
			{
				resvStation[i] = null;
				if (resvStation == Utils.LoadBuffer)
				{
					Utils.AluLdCounters[i] = 0;
					Utils.MemCounters[i] = 0;
				}
				else
				{
					Utils.AluStCounters[i] = 0;
				}
				deleted  = true;
				break;
			}
		}
		
		return deleted;
	}
	
}
