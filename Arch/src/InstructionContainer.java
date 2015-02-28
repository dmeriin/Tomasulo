
public class InstructionContainer {
	public int Instruction;
	public boolean Taken;
	public int ID;
	public int PC;
	
	public InstructionContainer(int instruction, boolean taken, int id,int pc ){
		this.Instruction = instruction;
		this.Taken = taken;
		this.ID = id;
		this.PC = pc;
	}
}
