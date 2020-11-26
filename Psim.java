/* On my honor, I have neither given nor received unauthorized aid on this assignment */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Psim {
	Read_INM inm;
	Read_RGF rgf;
	Read_DAM dam;
	Buf inb, aib, sib, prb = null;
	RGF Register_ASU, Register_MLU2, Register_Write;
	REBuffer reb;
	RGF adb;	
	DAM addressT;
	int Step_index = 0;
	
	// Instruction Memory
	public class INM{
		String Opcode, Dest;
			
		String Src1, Src2;
		public INM(String Opcode, String Dest, String Src1, String Src2){
			this.Opcode = Opcode;
			this.Dest = Dest;
			this.Src1 = Src1;
			this.Src2 = Src2;			
		}
	}
	
	//Register File
	public class RGF{
		String regname;
		int regvalue;
		public RGF(String regname, int regvalue) {
			this.regname = regname;
			this.regvalue = regvalue;
			
		}
	}
	
	//Data Memory
	public class DAM{
		int address;
		int regvalue;
		public DAM(int address, int regvalue){
			this.address = address;
			this.regvalue = regvalue; 	
			
		}
	}
	
	//ResultBuffer
	public class REBuffer{
		ArrayList<RGF> RE_Buf_array = new ArrayList<RGF>();
		public void storeResult(RGF resultReg) {
			RE_Buf_array.add(resultReg);
		}
		
	}
	// Instruction Buffer
	public class Buf{
		String Opcode, Dest;	
		int Src1, Src2;
		public Buf(String Opcode, String Dest, int Src1, int Src2){
			this.Opcode = Opcode;
			this.Dest = Dest;
			this.Src1 = Src1;
			this.Src2 = Src2;			
		}
	}
	
	
	public static void main(String[] args){
		Psim P = new Psim();
		P.init();
		P.INB_start();
				
	}
	
	public void init() {
		inm = new Read_INM();
		inm.ReadINM();
		
		rgf = new Read_RGF();
		rgf.ReadRGF();
		
		dam = new Read_DAM();
		dam.ReadDAM();
				
	}
	public void INB_start() {
		
		try{
			PrintStream fileStream = new PrintStream("simulation.txt");
			System.setOut(fileStream);
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.print("STEP " + Step_index++ + ":");
		PrintText();	
		while(inm.INM_array.size() > 0 || inb != null || aib != null || prb != null || sib != null || adb != null || reb != null )
		{	System.out.print("\nSTEP " + Step_index++ + ":");

			if(reb != null) Write();
			
			if(prb != null) MLU2();
			if(aib != null) {
				if(aib.Opcode.equals("MUL")) MLU1();
				else ASU();
			}
			if(adb != null) Store();
			if(sib != null) ADDR();
			if(inb != null) {
				if(inb.Opcode.equals("ST")) ISSUE2();
				else ISSUE1();}
			if(inm.INM_array.size() > 0) DECODE();
			PrintText();
		}
	}

	public void PrintText() {
		System.out.print("\nINM:");
		if(inm.INM_array.size() > 0) {	
			for(int i=0; i < inm.INM_array.size();i++) 
				{System.out.print("<"+inm.INM_array.get(i).Opcode+","+ inm.INM_array.get(i).Dest+","+inm.INM_array.get(i).Src1+","+inm.INM_array.get(i).Src2+">");		
				if(i!=inm.INM_array.size()-1) System.out.print(",");				
				}
			}
		System.out.print("\nINB:");
		if(inb != null) System.out.print("<"+inb.Opcode+","+inb.Dest+","+inb.Src1+","+inb.Src2+">");
		System.out.print("\nAIB:");
		if(aib != null) System.out.print("<"+aib.Opcode+","+aib.Dest+","+aib.Src1+","+aib.Src2+">");
		System.out.print("\nSIB:");
		if(sib != null) System.out.print("<"+sib.Opcode+","+sib.Dest+","+sib.Src1+","+sib.Src2+">");		
		System.out.print("\nPRB:");
		if(prb != null) System.out.print("<"+prb.Opcode+","+prb.Dest+","+prb.Src1+","+prb.Src2+">");		
		System.out.print("\nADB:");
		if(adb != null) System.out.print("<"+adb.regname+","+adb.regvalue+">");
		System.out.print("\nREB:");
		if(reb != null){
			System.out.print("<"+reb.RE_Buf_array.get(0).regname+","+reb.RE_Buf_array.get(0).regvalue+">");
			for(int j=1; j<reb.RE_Buf_array.size(); j++)
				System.out.print(",<"+reb.RE_Buf_array.get(j).regname+","+reb.RE_Buf_array.get(j).regvalue+">");
		}
		System.out.print("\nRGF:" + "<"+rgf.RGF_array.get(0).regname+","+rgf.RGF_array.get(0).regvalue+">");
		for(int j=1; j<rgf.RGF_array.size(); j++)
			System.out.print(",<"+rgf.RGF_array.get(j).regname+","+rgf.RGF_array.get(j).regvalue+">");
		System.out.print("\nDAM:" + "<"+dam.DAM_array.get(0).address+","+dam.DAM_array.get(0).regvalue+">");
		for(int j=1; j<dam.DAM_array.size(); j++)
			System.out.print(",<"+dam.DAM_array.get(j).address+","+dam.DAM_array.get(j).regvalue+">");
		System.out.print("\n");
			
	}
	

	
	public void DECODE() {
		INM TokenINM = inm.INM_array.get(0);
		inm.INM_array.remove(0);
		String [] RegSrc = {TokenINM.Src1, TokenINM.Src2};
		int RegSrcValue[] = GetRegValue(RegSrc);
		inb = new Buf(TokenINM.Opcode, TokenINM.Dest, RegSrcValue[0], RegSrcValue[1]);
		

	}
	
	public int[] GetRegValue(String[] RegSrc) {
		int RegSrcValue[] = new int[2];
		for(RGF i : rgf.RGF_array) {
			if(i.regname.equals(RegSrc[0])) {
				RegSrcValue[0] = i.regvalue;
			}
			if(i.regname.equals(RegSrc[1])) {
				RegSrcValue[1] = i.regvalue;
			}
			try{
				RegSrcValue[1] = Integer.parseInt(RegSrc[1]);
			} catch(NumberFormatException e){
			}
		}
		return RegSrcValue;
		
	}
	
	public void ISSUE1() {
		aib = new Buf(inb.Opcode, inb.Dest, inb.Src1, inb.Src2);
		inb = null;
	}
	
	public void ISSUE2() {
		sib = new Buf(inb.Opcode, inb.Dest, inb.Src1, inb.Src2);
		inb = null;
	}
	
	public void ASU() {
		int sum = 0;
		if(aib.Opcode.contentEquals("ADD")) {
			sum = aib.Src1 + aib.Src2;
		}
		else {
			sum = aib.Src1 - aib.Src2;
		}
		Register_ASU = new RGF(aib.Dest, sum);
		if(reb == null)
			reb = new REBuffer();
		reb.storeResult(Register_ASU);
		aib = null;
		
	}
	
	
	
	public void MLU1() {
		prb = new Buf(aib.Opcode, aib.Dest, aib.Src1, aib.Src2);
		aib = null;		
	}
	
	public void MLU2() {
		int result_mul = prb.Src1 * prb.Src2;
		Register_MLU2 = new RGF(prb.Dest, result_mul);
		if(reb == null)
			reb = new REBuffer();
		reb.storeResult(Register_MLU2);		
		prb = null;		
	}
	
	public void ADDR(){
		int address = sib.Src1 + sib.Src2;
		adb = new RGF(sib.Dest, address);
		sib = null;
		
	}	
	

	
	public void Store() {
		String[] regValue = {adb.regname, "0"};
		int[] regValues = GetRegValue(regValue);
		addressT = new DAM(adb.regvalue, regValues[0]);
		dam.StoreDAM(addressT);
		adb = null;				
	}
	

	public void Write() {
		String regname = reb.RE_Buf_array.get(0).regname;		
		int regvalue = reb.RE_Buf_array.get(0).regvalue;
		Register_Write = new RGF(regname, regvalue);
		rgf.StoreRGF(Register_Write);
		reb.RE_Buf_array.remove(0);
		if(reb.RE_Buf_array.size()==0)
			reb = null;
	}
	

	
	
	////Read_instruction 
	class Read_INM{
		String filename = "instructions.txt";
		String Opcode, Dest, Src1, Src2;
		String ReadINMline;
		ArrayList<INM> INM_array = new ArrayList<INM>();////
		public void ReadINM() {
			File file = new File(filename);
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((ReadINMline = br.readLine()) != null) {
					String eachInst = ReadINMline.substring(ReadINMline.indexOf("<")+1, ReadINMline.indexOf(">"));
					String[] eachline = eachInst.split(",");
					Opcode = eachline[0];
					Dest = eachline[1];
					Src1 = eachline[2];
					Src2 = eachline[3];
					INM IN_token = new INM(Opcode, Dest, Src1, Src2);
					INM_array.add(IN_token);									
																						
				}
				br.close();			
			} catch(IOException e ){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	///Read Register File
	class Read_RGF{
		String regname;
		int regvalue;
		ArrayList<RGF> RGF_array = new ArrayList<RGF>();
		String filename = "registers.txt";
		String ReadRGFline;
		public void ReadRGF() {
			File file = new File(filename);
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((ReadRGFline = br.readLine()) != null) {
					String eachReg = ReadRGFline.substring(ReadRGFline.indexOf("<")+1, ReadRGFline.indexOf(">"));
					String[] eachline = eachReg.split(",");
					regname = eachline[0];
					regvalue = Integer.parseInt(eachline[1]);
					RGF RegisterT = new RGF(regname, regvalue);
					RGF_array.add(RegisterT);									
																						
				}
				br.close();		
			} catch(IOException e ){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}
			
			
		
					
		}
		
		public void StoreRGF(RGF RegisterT) {
			
			int left = 0;
			int right = RGF_array.size()-1;
			int mid = 0;
			int index = 0;
			int RegisterTint = Integer.parseInt(RegisterT.regname.replaceAll("[^0-9]", ""));
			int regInt = 0;
			
			while(right>=left) {				
				mid = (right + left) / 2;
				regInt = Integer.parseInt(RGF_array.get(mid).regname.replaceAll("[^0-9]", ""));			
				
				if(RegisterTint == regInt) {
					RGF_array.get(mid).regvalue = RegisterT.regvalue;
					index = mid;
					break;
				}
				if(RegisterTint > regInt) {
					left = mid + 1;
					index = mid + 1;
				}
				if(RegisterTint < regInt) {
					right = mid - 1;
					index = mid;
				}
				
			}
			if(RegisterTint != regInt) {
				RGF_array.add(index, RegisterT);
			}  	
			
		}
				
		
		
	}
	
	//Read DataMemory
	class Read_DAM{
		int address;
		int regvalue;
		ArrayList<DAM> DAM_array = new ArrayList<DAM>();
		String filename = "datamemory.txt";
		String ReadDAMline;
		public void ReadDAM() {
			File file = new File(filename);
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((ReadDAMline = br.readLine()) != null) {
					String eachReg = ReadDAMline.substring(ReadDAMline.indexOf("<")+1, ReadDAMline.indexOf(">"));
					String[] eachline = eachReg.split(",");
					address = Integer.parseInt(eachline[0]);
					regvalue = Integer.parseInt(eachline[1]);
					DAM DAMemory = new DAM(address, regvalue);
					DAM_array.add(DAMemory);									
																						
				}
				br.close();		
			} catch(IOException e ){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}
								
		}

		public void StoreDAM(DAM addressT){			
			
			int left = 0;
			int right = DAM_array.size()-1;
			int mid = 0;
			int index = 0;
			while(right>=left) {
				mid = (right + left) / 2;
				if(addressT.address == DAM_array.get(mid).address) {
					DAM_array.get(mid).regvalue = addressT.regvalue;
					index = mid;
					break;
				}
				if(addressT.address > DAM_array.get(mid).address) {
					left = mid + 1;
					index = mid + 1;
				}
				if(addressT.address < DAM_array.get(mid).address) {
					right = mid - 1;
					index = mid;
				}
				
			}
			if(addressT.address != DAM_array.get(index).address) {
				DAM_array.add(index, addressT);
			}  					
			
		}
		
				
		
		
	}
	
	
	
	
}
