import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MutantErrorPropagationTree extends ErrorPropagationTree{
	
	ErrorPropagationTree oracle;
	
	EPTNode originalNode;
	EPTNode mutantNode;
	//ArrayList<Integer> mutantNodeId;
	
	int mutantCategory;
	int mutantNodeId;
	int mutantParam;
	
	static final int ARITHMETIC_OP_MUTANT = 0;
	static final int RELATIONAL_OP_MUTANT = 1;
	static final int LOGICAL_OP_MUTANT = 2;
	static final int LITERAL_MUTANT = 3;
	static final int NOT_MUTANT = 4;
	
	class MaskingEffectLog{
		int testcaseNumber; // masking 발생한 테스트 케이스 번호
		int step; // maksing 발생한 step 번호
		int maskNodeid; // mask가 발생한 노드 아이디
		//String maskNodeType; // mask가 발생한 노드의 타입
		String maskNodeType;
		int maskingCategory; // 마스크 발생한 종류
		int distance; // error 근원지로 부터 mask가 발생하기까지 전파된 거리
		
		public static final int MASK_BY_TIME = 0;
		public static final int MASK_BY_LOGICOP = 1;
		public static final int MASK_BY_RELOP = 2;
		public static final int MASK_BY_IF = 3;
		public static final int MASK_BY_OTHER = 4;
		public static final int MASK_RECOVERY = 5; // 뮤턴트가 발생한 자리에서 에러가 발생하지 않은 경우
		public static final int MASK_BY_ARITHOP = 6;
		
		//의미 : testcaseNumber인 테스트 케이스의 step인 타입 스텝에서 발생된 오류는 mutantNode로 부터 거리가 distance이고 아이디는 maskNodeid이고 타입은 maskNodeType인 노드에서 에러가 사라짐. 에러가 사라진 마스크 유형은 maskingCategory임  
		MaskingEffectLog(int testcaseNumber , int step, int maskNodeid, String maskNodeType, int maskingCategory, int distance){
			this.testcaseNumber = testcaseNumber;
			this.step = step;
			this.maskNodeid = maskNodeid;
			this.maskingCategory = maskingCategory;
			this.distance = distance;
			this.maskNodeType = maskNodeType;
		}
		
		public String toString() {
			String categoryStr = null;
			switch(maskingCategory) {
			case MASK_BY_TIME:
				categoryStr = "TIME"; break;
			case MASK_BY_LOGICOP:
				categoryStr = "LOGIC OP"; break;
			case MASK_BY_RELOP:
				categoryStr = "Relational Op"; break;
			case MASK_BY_IF:
				categoryStr = "If"; break;
			case MASK_BY_OTHER:
				categoryStr = "Other"; break;
			case MASK_RECOVERY:
				categoryStr = "Recovory"; break;
			case MASK_BY_ARITHOP:
				categoryStr = "Arith"; break;
			}
			return String.format("Case[%d], T%d, node[%d], %s, %s, %d", testcaseNumber, step, maskNodeid, maskNodeType, categoryStr, distance);
		}
		
		public String toStringV2() { // Test Case 번호와 Node Id는 제외한 결과에 대한 문자열 반환 
			String categoryStr = null;
			switch(maskingCategory) {
			case MASK_BY_TIME:
				categoryStr = "TIME"; break;
			case MASK_BY_LOGICOP:
				categoryStr = "LOGIC OP"; break;
			case MASK_BY_RELOP:
				categoryStr = "Relational Op"; break;
			case MASK_BY_IF:
				categoryStr = "If"; break;
			case MASK_BY_OTHER:
				categoryStr = "Other"; break;
			case MASK_RECOVERY:
				categoryStr = "Recovory"; break;
			case MASK_BY_ARITHOP:
				categoryStr = "Arith"; break;
			}
			return String.format("node[%d], %s, %s, %d", maskNodeid, maskNodeType, categoryStr, distance);
		}
		
	}
	
	private MutantErrorPropagationTree(ErrorPropagationTree oracle, int mutantType, int mutantNodeID, int mutantParam) {
		super(oracle);
		this.oracle = oracle;
		
		this.mutateTree(mutantNodeID, mutantType, mutantParam);
	}
	
	public void mutateInformation() {
		System.out.println("originalNode:" + originalNode);
		System.out.println("mutantNode:" + mutantNode);
	}
	
	/*
	public static MutantErrorPropagationTree CreateMutant(ErrorPropagationTree originalEPT) {
		return CreateMutant(originalEPT, -1, -1);
	}
	
	
	public static MutantErrorPropagationTree CreateMutant(ErrorPropagationTree originalEPT, int mutantType) {
		boolean[] visited = new boolean[originalEPT.size];
		Arrays.fill(visited, false);
		ArrayList<Integer> mutableNodes = new ArrayList<>();
		getMutableNode(originalEPT.root, visited, mutableNodes, mutantType);
		
		if(mutableNodes.size() == 0) return null;
		
		int mutantNodeID = mutableNodes.get( new Random().nextInt(mutableNodes.size()) );
		int mutantParam = getMutateParameter(originalEPT.nodelist[mutantNodeID], mutantType, -1);
		
		MutantErrorPropagationTree mEPT = new MutantErrorPropagationTree(originalEPT, mutantNodeID, mutantParam);
		
		return mEPT;
	}
	*/
	public static MutantErrorPropagationTree CreateMutant(ErrorPropagationTree originalEPT, int mutantType, int mutantNodeID, int mutantParam) {
		//System.out.println("Mutant Node Id:" + mutantNodeID);
		//System.out.println("Mutant Param:" + mutantParam);
		
		MutantErrorPropagationTree mEPT = new MutantErrorPropagationTree(originalEPT, mutantType, mutantNodeID, mutantParam);
		
		return mEPT;
	}
	
	void mutateTree(int mutantNodeID, int mutantType, int mutantParam) {
		this.mutantCategory = mutantType;
		this.mutantParam = mutantParam;
		this.mutantNodeId = mutantNodeID;
		
		this.mutantNode = mutate(nodelist[mutantNodeID], mutantType, mutantParam);
		
		nodelist[ mutantNodeID ] = mutantNode;
		originalNode = oracle.nodelist[mutantNodeID];	
	}
	
	void writeMaskingInfo(String dirPath, int caseNumber, int step, ArrayList<MaskingEffectLog> maskLog) {
		if(maskLog.size() != 0) {
		try {
			FileWriter wr = new FileWriter(dirPath+String.format("maskLog_test%d_t%d.csv", caseNumber, step));
			for(int i = 0; i < maskLog.size(); i++ ) {
				wr.write(maskLog.get(i).toString());
				wr.write("\n");
			}
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}
	
	
	/*
	void maskingAnalysis(Testsuite ts, String outputDir) throws IOException {
		if(ts == null ) return;
		
		//String modelFileName = oracle.lusfileName;
		//String modelFileDir = modelFileName.substring(0, modelFileName.lastIndexOf("\\") + 1); 
		
		BufferedWriter fw = new BufferedWriter( new FileWriter(outputDir + "result.csv") );
		fw.write(",Category, From, To, ID, Parameter\n");
		fw.write("Mutant Info,");
		switch(mutantCategory) {
		case ARITHMETIC_OP_MUTANT:
			fw.write(String.format("Arithmetic Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case RELATIONAL_OP_MUTANT:
			fw.write(String.format("Relational Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case LOGICAL_OP_MUTANT:
			fw.write(String.format("Logical Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case LITERAL_MUTANT:
			fw.write(String.format("Literal Op Mutant,%s,%s,%d,%d\n", originalNode.get(0), mutantNode.get(0), mutantNodeId, mutantParam)); break;
		}
		
		ArrayList<InputNode> inputNodes_orc = new ArrayList<>();
		ArrayList<VariableNode> localVarNodes_orc = new ArrayList<>();
		ArrayList<VariableNode> outputVarNodes_orc =  new ArrayList<>();
		
		ArrayList<InputNode> inputNodes_mut = new ArrayList<>();
		ArrayList<VariableNode> localVarNodes_mut = new ArrayList<>();
		ArrayList<VariableNode> outputVarNodes_mut =  new ArrayList<>();
		
		ArrayList<String> variableNames = new ArrayList<>();
		
		for(int i = 0 ; i < oracle.inputNodeIds.size(); i++) {
			InputNode inputNode_orc = (InputNode) oracle.nodelist[ oracle.inputNodeIds.get(i) ] ;
			if(inputNode_orc != null && inputNode_orc.connected) {
				inputNodes_orc.add( inputNode_orc );
				inputNodes_mut.add( this.inputNodes.get( inputNode_orc.name ) );
			}
		}
		for(int i = 0 ; i < oracle.variableNodeIds.size(); i++) {
			VariableNode variableNode_orc = (VariableNode) oracle.nodelist[ oracle.variableNodeIds.get(i) ] ;
			if(variableNode_orc != null && variableNode_orc.connected) {
				if( variableNode_orc.nodetype == LustreNodeType.OUTVARIABLE ) {
					outputVarNodes_orc.add( variableNode_orc );
					outputVarNodes_mut.add( this.variables.get( variableNode_orc.name ) );
				}else {
					localVarNodes_orc.add( variableNode_orc );
					localVarNodes_mut.add( this.variables.get( variableNode_orc.name ) );
				}
			}
		}
		
		fw.write(",");
		for(int i = 0 ; i < outputVarNodes_orc.size(); i++) fw.write(",[O]");
		for(int i = 0 ; i < inputNodes_orc.size(); i++) fw.write(",[I]"); 
		for(int i = 0 ; i < localVarNodes_orc.size(); i++) fw.write(",[L]");
		fw.newLine();
		fw.write(",Variable Name");
		
		for(int i = 0 ; i < outputVarNodes_orc.size(); i++) {
			if( !outputVarNodes_orc.get(i).name.equals( outputVarNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @maskingAnalysis");
				return;  
			}
			fw.write(","+outputVarNodes_orc.get(i).name);
		}
		
		for(int i = 0 ; i < inputNodes_orc.size(); i++) {
			if( !inputNodes_orc.get(i).name.equals( inputNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @maskingAnalysis");
				return;  
			}
			fw.write(","+inputNodes_orc.get(i).name);
		}
		
		for(int i = 0 ; i < localVarNodes_orc.size(); i++) {
			if( !localVarNodes_orc.get(i).name.equals( localVarNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @maskingAnalysis");
				return;  
			}
			fw.write(","+localVarNodes_orc.get(i).name);
		}
		
		fw.newLine();
		
		int numOftestcase = ts.testsuiteSize();
		
		ArrayList<MaskingEffectLog> log = new ArrayList<>();
		
		//boolean[] matched = new boolean[inputNodes_orc.size() + localVarNodes_orc.size() + outputVarNodes_orc.size()];
		
		int total_num_of_time_mask = 0;
		int total_num_of_rel_mask = 0;
		int total_num_of_log_mask = 0;
		int total_num_of_if_mask = 0;
		int total_num_of_other_mask = 0;
		
		for(int tcNumber = 0; tcNumber < numOftestcase; tcNumber++) {
			
			fw.write( String.format("Test Case[%d]\n", tcNumber) );
			
			int stepsize = ts.getStepSize(tcNumber);
			
			oracle.simulate(ts.getTestCase(tcNumber), stepsize);
			this.simulate(ts.getTestCase(tcNumber), stepsize);
			
			int[] num_of_time_mask = new int[stepsize];
			Arrays.fill(num_of_time_mask, 0);
			
			for(int step = 0; step < stepsize; step++) {
				int distance = 0;
				maskingAnalysis(mutantNode, tcNumber, step, distance, log, stepsize);
				
				fw.write(String.format("Step %d,Oracle", step));
				for(int i = 0 ; i < outputVarNodes_orc.size(); i++) fw.write(","+outputVarNodes_orc.get(i).get(step).value);
				for(int i = 0 ; i < inputNodes_orc.size(); i++) fw.write(","+inputNodes_orc.get(i).get(step).value);
				for(int i = 0 ; i < localVarNodes_orc.size(); i++) fw.write(","+localVarNodes_orc.get(i).get(step).value);
				fw.newLine();
				fw.write(String.format(",Mutant", step));
				for(int i = 0 ; i < outputVarNodes_mut.size(); i++) fw.write(","+outputVarNodes_mut.get(i).get(step).value);
				for(int i = 0 ; i < inputNodes_mut.size(); i++) fw.write(","+inputNodes_mut.get(i).get(step).value);
				for(int i = 0 ; i < localVarNodes_mut.size(); i++) fw.write(","+localVarNodes_mut.get(i).get(step).value);
				fw.newLine();
				
				fw.write(",Comparison");
				for(int i = 0 ; i < outputVarNodes_orc.size(); i++) {
					if(outputVarNodes_orc.get(i).get(step).equals(outputVarNodes_mut.get(i).get(step))) {
						if(outputVarNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				for(int i = 0 ; i < inputNodes_orc.size(); i++) {
					if(inputNodes_orc.get(i).get(step).equals(inputNodes_mut.get(i).get(step))) {
						if(inputNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				for(int i = 0 ; i < localVarNodes_orc.size(); i++) {
					if(localVarNodes_orc.get(i).get(step).equals(localVarNodes_mut.get(i).get(step))) {
						if(localVarNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				fw.newLine();
				
				int num_of_rel_mask = 0;
				int num_of_log_mask = 0;
				int num_of_if_mask = 0;
				int num_of_other_mask = 0;
				for(int i = 0; i < log.size(); i++) {
					switch(log.get(i).maskingCategory) {
					case MaskingEffectLog.MASK_BY_TIME: num_of_time_mask[step]++; break;
					case MaskingEffectLog.MASK_BY_RELOP: num_of_rel_mask++; break;
					case MaskingEffectLog.MASK_BY_LOGICOP: num_of_log_mask++; break;
					case MaskingEffectLog.MASK_BY_IF: num_of_if_mask++; break;
					case MaskingEffectLog.MASK_BY_OTHER: num_of_other_mask++; break;
					}
				}
				writeMaskingInfo(outputDir, tcNumber, step, log);
				log.clear();
				fw.write(",Mask,BY_TIME,BY_RELATION_OP,BY_LOGICAL_OP,BY_IF,BY_OTHER\n");
				fw.write(String.format(",,%d,%d,%d,%d,%d\n", num_of_time_mask[step],num_of_rel_mask, num_of_log_mask, num_of_if_mask, num_of_other_mask));
				
				total_num_of_time_mask += num_of_time_mask[step];
				total_num_of_rel_mask += num_of_rel_mask;
				total_num_of_log_mask += num_of_log_mask;
				total_num_of_if_mask += num_of_if_mask;
				total_num_of_other_mask += num_of_other_mask;
			}
		}
		
		fw.write("Total,Mask,BY_TIME,BY_RELATION_OP,BY_LOGICAL_OP,BY_IF,BY_OTHER\n");
		fw.write(String.format(",,%d,%d,%d,%d,%d\n", total_num_of_time_mask, total_num_of_rel_mask, total_num_of_log_mask, total_num_of_if_mask, total_num_of_other_mask));
		fw.close();
		
		fw = new BufferedWriter( new FileWriter(outputDir + "summary.txt") );
	}
	*/
	
	boolean maskingAnalysis(Testsuite ts, String outputDir) throws IOException { // 실패 (=false)/성공(=success) 여부 반환
		if(ts == null ) {
			ErrorLog.myErrorLog("ELOG: empty test suite @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
			return false;
		}
		
		BufferedWriter fw = new BufferedWriter( new FileWriter(outputDir + File.separator + "Simulation Result.csv") );
		
		BufferedWriter fw2 = new BufferedWriter( new FileWriter(outputDir + File.separator + "masking_info.csv") );
		WriteMaskingIntroInfoFile(fw2);
		
		fw.write(",Category, From, To, ID, Parameter\n");
		fw.write("Mutant Info,");
		switch(mutantCategory) {
		case ARITHMETIC_OP_MUTANT:
			fw.write(String.format("Arithmetic Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case RELATIONAL_OP_MUTANT:
			fw.write(String.format("Relational Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case LOGICAL_OP_MUTANT:
			fw.write(String.format("Logical Op Mutant,%s,%s,%d,%d\n", originalNode.getLabel(), mutantNode.getLabel(), mutantNodeId, mutantParam)); break;
		case LITERAL_MUTANT:
			fw.write(String.format("Literal Op Mutant,%s,%s,%d,%d\n", originalNode.get(0), mutantNode.get(0), mutantNodeId, mutantParam)); break;
		}
		
		ArrayList<InputNode> inputNodes_orc = new ArrayList<>();
		ArrayList<VariableNode> localVarNodes_orc = new ArrayList<>();
		ArrayList<VariableNode> outputVarNodes_orc =  new ArrayList<>();
		
		ArrayList<InputNode> inputNodes_mut = new ArrayList<>();
		ArrayList<VariableNode> localVarNodes_mut = new ArrayList<>();
		ArrayList<VariableNode> outputVarNodes_mut =  new ArrayList<>();
		
		for(int i = 0 ; i < oracle.inputNodeIds.size(); i++) {
			InputNode inputNode_orc = (InputNode) oracle.nodelist[ oracle.inputNodeIds.get(i) ] ;
			if(inputNode_orc != null && inputNode_orc.connected) {
				inputNodes_orc.add( inputNode_orc );
				inputNodes_mut.add( this.inputNodes.get( inputNode_orc.name ) );
			}
		}
		for(int i = 0 ; i < oracle.variableNodeIds.size(); i++) {
			VariableNode variableNode_orc = (VariableNode) oracle.nodelist[ oracle.variableNodeIds.get(i) ] ;
			if(variableNode_orc != null && variableNode_orc.connected) {
				if( variableNode_orc.nodetype == LustreNodeType.OUTVARIABLE ) {
					outputVarNodes_orc.add( variableNode_orc );
					outputVarNodes_mut.add( this.variables.get( variableNode_orc.name ) );
				}else {
					localVarNodes_orc.add( variableNode_orc );
					localVarNodes_mut.add( this.variables.get( variableNode_orc.name ) );
				}
			}
		}
		
		fw.write(",");
		for(int i = 0 ; i < outputVarNodes_orc.size(); i++) fw.write(",[O]");
		for(int i = 0 ; i < inputNodes_orc.size(); i++) fw.write(",[I]"); 
		for(int i = 0 ; i < localVarNodes_orc.size(); i++) fw.write(",[L]");
		fw.newLine();
		fw.write(",Variable Name");
		
		for(int i = 0 ; i < outputVarNodes_orc.size(); i++) {
			if( !outputVarNodes_orc.get(i).name.equals( outputVarNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.write("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.close();
				fw2.close();
				return false;  
			}
			fw.write(","+outputVarNodes_orc.get(i).name);
		}
		
		for(int i = 0 ; i < inputNodes_orc.size(); i++) {
			if( !inputNodes_orc.get(i).name.equals( inputNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.write("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.close();
				fw2.close();
				return false;  
			}
			fw.write(","+inputNodes_orc.get(i).name);
		}
		
		for(int i = 0 ; i < localVarNodes_orc.size(); i++) {
			if( !localVarNodes_orc.get(i).name.equals( localVarNodes_mut.get(i).name ) ){
				ErrorLog.myErrorLog("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.write("ELOG: Unmatched variable name between oracle and mutant @MutantErrorPropagationTree.maskingAnalysis(Testsuite ts, String outputDir)");
				fw.close();
				fw2.close();
				return false;  
			}
			fw.write(","+localVarNodes_orc.get(i).name);
		}
		
		fw.newLine();
		
		int numOftestcase = ts.testsuiteSize();
		
		ArrayList<MaskingEffectLog> log = new ArrayList<>(); //log 삭제
		
		int[][] num_of_time_mask = new int[numOftestcase][];
		int[][] num_of_rel_mask = new int[numOftestcase][];
		int[][] num_of_log_mask = new int[numOftestcase][];
		int[][] num_of_if_mask = new int[numOftestcase][];
		int[][] num_of_other_mask = new int[numOftestcase][];
		boolean[][] error_propagated = new boolean[numOftestcase][];
		
		int totalStep = 0;
		int total_time_mask = 0;
		int total_rel_mask = 0;
		int total_log_mask = 0;
		int total_if_mask = 0;
		int total_other_mask = 0;
		
		int total_time_mask_in_masked_tc = 0;
		int total_rel_mask_in_masked_tc = 0;
		int total_log_mask_in_masked_tc = 0;
		int total_if_mask_in_masked_tc = 0;
		int total_other_mask_in_masked_tc = 0;
		
		int numOfMaskedTestCase = 0;
		
		int[] timestamps = new int[nodelist.length]; // 각 노드 별로 masking 분석 과정에서 가장 최근에 방문된 timestamp를 저장하는 변수 
		Arrays.fill(timestamps, -1);
		int timestamp = 0;
		
		for(int tcNumber = 0; tcNumber < numOftestcase; tcNumber++) {
			fw.write( String.format("Test Case[%d]\n", tcNumber) );
			
			int stepsize = ts.getStepSize(tcNumber);
			totalStep += stepsize;
			
			num_of_time_mask[tcNumber] = new int[stepsize]; Arrays.fill(num_of_time_mask[tcNumber], 0);
			num_of_rel_mask[tcNumber] = new int[stepsize]; Arrays.fill(num_of_rel_mask[tcNumber], 0);
			num_of_log_mask[tcNumber] = new int[stepsize]; Arrays.fill(num_of_log_mask[tcNumber], 0);
			num_of_if_mask[tcNumber] = new int[stepsize]; Arrays.fill(num_of_if_mask[tcNumber], 0);
			num_of_other_mask[tcNumber] = new int[stepsize]; Arrays.fill(num_of_other_mask[tcNumber], 0);
			error_propagated[tcNumber] = new boolean[stepsize]; Arrays.fill(error_propagated[tcNumber], false);
			
			int total_time_mask_tc = 0;
			int total_rel_mask_tc = 0;
			int total_log_mask_tc = 0;
			int total_if_mask_tc = 0;
			int total_other_mask_tc = 0;
			
			oracle.simulate(ts.getTestCase(tcNumber), stepsize);
			this.simulate(ts.getTestCase(tcNumber), stepsize);
			
			if(this.SimulationErrorCode != NO_ERROR) {
				ErrorLog.myErrorLog("ELOG: Simulation Error - " + this.SimulationErrorMessage + "/ testcase " + tcNumber);
				fw.write("ELOG: Simulation Error - " + this.SimulationErrorMessage + "/ testcase " + tcNumber);
				fw.close();
				fw2.close();
				return false;
			}
			
			boolean maskedTestCase = true;
			
			ArrayList<MaskingEffectLog> partialLog = new ArrayList<>();
			int numberOfpartialLog = 0;
			
			for(int step = 0; step < stepsize; step++) {
				int distance = 0;
				if(oracle.root.get(step).value.equals("")) {
					System.out.println(String.format("Oracle Evaluation Failure @Tc:%d, step:%d",tcNumber,step));
				}
				if(this.root.get(step).value.equals("")) {
					System.out.println(String.format("Mutant Evaluation Failure @Tc:%d, step:%d",tcNumber,step));
				}
				
				boolean masked = maskingAnalysis(mutantNode, tcNumber, step, distance, partialLog, stepsize, timestamps, timestamp);
				
				
				
				if( masked == false ) {
					maskedTestCase = false;
					error_propagated[tcNumber][step] = true;
				}
				
				while( numberOfpartialLog < partialLog.size() ) {
					log.add(partialLog.get(numberOfpartialLog));
					int maskingStep = partialLog.get(numberOfpartialLog).step;
					int maskingCategory = partialLog.get(numberOfpartialLog).maskingCategory;
					switch(maskingCategory) {
					case MaskingEffectLog.MASK_BY_TIME: num_of_time_mask[tcNumber][maskingStep]++; break;
					case MaskingEffectLog.MASK_BY_RELOP: num_of_rel_mask[tcNumber][maskingStep]++; break;
					case MaskingEffectLog.MASK_BY_LOGICOP: num_of_log_mask[tcNumber][maskingStep]++; break;
					case MaskingEffectLog.MASK_BY_IF: num_of_if_mask[tcNumber][maskingStep]++; break;
					case MaskingEffectLog.MASK_BY_OTHER: num_of_other_mask[tcNumber][maskingStep]++; break;
					}
					
					numberOfpartialLog++;
				}
				
				total_time_mask_tc += num_of_time_mask[tcNumber][step];
				total_rel_mask_tc += num_of_rel_mask[tcNumber][step];
				total_log_mask_tc += num_of_log_mask[tcNumber][step];
				total_if_mask_tc += num_of_if_mask[tcNumber][step];
				total_other_mask_tc += num_of_other_mask[tcNumber][step];
				
				
				fw.write(String.format("Step %d,Oracle", step));
				for(int i = 0 ; i < outputVarNodes_orc.size(); i++) fw.write(","+outputVarNodes_orc.get(i).get(step).value);
				for(int i = 0 ; i < inputNodes_orc.size(); i++) fw.write(","+inputNodes_orc.get(i).get(step).value);
				for(int i = 0 ; i < localVarNodes_orc.size(); i++) fw.write(","+localVarNodes_orc.get(i).get(step).value);
				fw.newLine();
				fw.write(String.format(",Mutant", step));
				for(int i = 0 ; i < outputVarNodes_mut.size(); i++) fw.write(","+outputVarNodes_mut.get(i).get(step).value);
				for(int i = 0 ; i < inputNodes_mut.size(); i++) fw.write(","+inputNodes_mut.get(i).get(step).value);
				for(int i = 0 ; i < localVarNodes_mut.size(); i++) fw.write(","+localVarNodes_mut.get(i).get(step).value);
				fw.newLine();
				
				fw.write(",Comparison");
				for(int i = 0 ; i < outputVarNodes_orc.size(); i++) {
					if(outputVarNodes_orc.get(i).get(step).equals(outputVarNodes_mut.get(i).get(step))) {
						if(outputVarNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				for(int i = 0 ; i < inputNodes_orc.size(); i++) {
					if(inputNodes_orc.get(i).get(step).equals(inputNodes_mut.get(i).get(step))) {
						if(inputNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				for(int i = 0 ; i < localVarNodes_orc.size(); i++) {
					if(localVarNodes_orc.get(i).get(step).equals(localVarNodes_mut.get(i).get(step))) {
						if(localVarNodes_orc.get(i).get(step).value.equals("")) fw.write(","+ "-");
						else fw.write(","+ "O");
					}else fw.write(","+ "X");
				}
				fw.newLine();
				fw.write("Masked," + String.valueOf(masked));
				fw.newLine();
				
				timestamp++;
			}

			total_time_mask += total_time_mask_tc;
			total_rel_mask += total_rel_mask_tc;
			total_log_mask += total_log_mask_tc;
			total_if_mask += total_if_mask_tc;
			total_other_mask +=	total_other_mask_tc;
			
			if(maskedTestCase == true) {
				for(int i = 0; i < partialLog.size(); i++) {
					WriteMaskingInfoFile(fw2, partialLog);
					log.add(partialLog.get(i));
				}
				total_time_mask_in_masked_tc += total_time_mask_tc;
				total_rel_mask_in_masked_tc += total_rel_mask_tc;
				total_log_mask_in_masked_tc += total_log_mask_tc;
				total_if_mask_in_masked_tc += total_if_mask_tc;
				total_other_mask_in_masked_tc += total_other_mask_tc;
				
				numOfMaskedTestCase++;
			}
		}
		
		fw2.close();
		fw.close();
		WriteSummaryFile(outputDir, numOftestcase, totalStep, numOfMaskedTestCase, total_time_mask, total_rel_mask, total_log_mask, total_if_mask, total_other_mask, num_of_time_mask, num_of_rel_mask, num_of_log_mask, num_of_if_mask, num_of_other_mask, error_propagated, total_time_mask_in_masked_tc, total_rel_mask_in_masked_tc, total_log_mask_in_masked_tc, total_if_mask_in_masked_tc, total_other_mask_in_masked_tc);
		WriteMaskingInfoFile(outputDir, log);
		return true;
	}
	
	void WriteSummaryFile(String outputDir, int numOfTestCase, int stepSum, int numOfMaskedTestCase, int total_time_mask, int total_rel_mask, int total_log_mask, int total_if_mask, int total_other_mask, int[][] num_of_time_mask, int[][] num_of_rel_mask, int[][] num_of_log_mask, int[][]num_of_if_mask, int[][] num_of_other_mask, boolean[][] error_propagated, int total_time_mask_in_masked_tc, int total_rel_mask_in_masked_tc, int total_log_mask_in_masked_tc, int total_if_mask_in_masked_tc, int total_other_mask_in_masked_tc) {
		BufferedWriter fw;
		try {
			fw = new BufferedWriter( new FileWriter(outputDir + File.separator + "summary.txt") );
		
			fw.write("Mutant Information"); fw.newLine();
			fw.write("- Category: ");
			switch(mutantCategory) {
			case ARITHMETIC_OP_MUTANT:
				fw.write(String.format("Arithmetic Operator Mutant [%d]", ARITHMETIC_OP_MUTANT)); fw.newLine(); break;
			case RELATIONAL_OP_MUTANT:
				fw.write(String.format("Relational Operator Mutant [%d]", RELATIONAL_OP_MUTANT)); fw.newLine(); break;
			case LOGICAL_OP_MUTANT:
				fw.write(String.format("Logical Operator Mutant [%d]", LOGICAL_OP_MUTANT)); fw.newLine(); break;
			case LITERAL_MUTANT:
				fw.write(String.format("Literal Value Mutant [%d]", LITERAL_MUTANT)); fw.newLine(); break;
			}
			
			if(mutantCategory == LITERAL_MUTANT) {
				fw.write(String.format("- From: %s", originalNode.get(0).value)); fw.newLine();
				fw.write(String.format("- To: %s", mutantNode.get(0).value)); fw.newLine();
			}else {
				fw.write(String.format("- From: %s", originalNode.getLabel())); fw.newLine();
				fw.write(String.format("- To: %s", mutantNode.getLabel())); fw.newLine();
			}
			
			fw.write(String.format("- Mutant Node ID: %d", mutantNodeId)); fw.newLine();
			fw.write(String.format("- Mutant Parameter Value: %d", mutantParam)); fw.newLine();
			fw.newLine();
			fw.write("Test Suite information"); fw.newLine();
			fw.write(String.format("- Number of Test Case: %d", numOfTestCase)); fw.newLine();
			fw.write(String.format("- Total Sum of Step: %d", stepSum)); fw.newLine();
			fw.newLine();
			fw.write("Masking Information"); fw.newLine();
			
			int totalSumOfMask = total_time_mask + total_rel_mask + total_log_mask + total_if_mask + total_other_mask;
			fw.write(String.format("- Ratio of Masking: %d / %d (%.1f%%) # Number of Test Case Masking Error / Number of Test Case",numOfMaskedTestCase ,numOfTestCase ,(double) numOfMaskedTestCase / numOfTestCase * 100)); fw.newLine();
			fw.write(String.format("- Total Sum of Temporal Masking in Masked Test Case: %d", total_time_mask_in_masked_tc)); fw.newLine();
			fw.write(String.format("- Total Sum of Relational Op Masking in Masked Test Case: %d", total_rel_mask_in_masked_tc)); fw.newLine();
			fw.write(String.format("- Total Sum of Logical Op Masking in Masked Test Case: %d", total_log_mask_in_masked_tc)); fw.newLine();
			fw.write(String.format("- Total Sum of If Op Masking in Masked Test Case: %d", total_if_mask_in_masked_tc)); fw.newLine();
			fw.write(String.format("- Total Sum of Other Masking in Masked Test Case: %d", total_other_mask_in_masked_tc)); fw.newLine();
			fw.newLine();
			
			fw.write(String.format("- Total Sum of Masking: %d", totalSumOfMask)); fw.newLine();
			fw.write(String.format("- Total Sum of Temporal Masking: %d (%.1f%%)", total_time_mask, totalSumOfMask == 0 ? 0 : (double)total_time_mask/totalSumOfMask * 100)); fw.newLine();
			fw.write(String.format("- Total Sum of Relational Op Masking: %d (%.1f%%)", total_rel_mask, totalSumOfMask == 0 ? 0 : (double)total_rel_mask/totalSumOfMask * 100)); fw.newLine();
			fw.write(String.format("- Total Sum of Logical Op Masking: %d (%.1f%%)", total_log_mask, totalSumOfMask == 0 ? 0 : (double)total_log_mask/totalSumOfMask * 100)); fw.newLine();
			fw.write(String.format("- Total Sum of If Op Masking: %d (%.1f%%)", total_if_mask, totalSumOfMask == 0 ? 0 : (double)total_if_mask/totalSumOfMask * 100)); fw.newLine();
			fw.write(String.format("- Total Sum of Other Masking: %d (%.1f%%)", total_other_mask, totalSumOfMask == 0 ? 0 : (double)total_other_mask/totalSumOfMask * 100)); fw.newLine();
			fw.newLine();
			
			fw.write("- Detail"); fw.newLine();
			for(int i = 0; i < numOfTestCase; i++) {
				int parital_sum_time_mask = 0;
				int parital_sum_rel_mask = 0;
				int parital_sum_log_mask = 0;
				int parital_sum_if_mask = 0;
				int parital_sum_other_mask = 0;
				
				boolean error_propagated_tc = false;
				
				for(int step = 0; step < num_of_time_mask[i].length; step++) {
					parital_sum_time_mask += num_of_time_mask[i][step];
					parital_sum_rel_mask += num_of_rel_mask[i][step];
					parital_sum_log_mask += num_of_log_mask[i][step];
					parital_sum_if_mask += num_of_if_mask[i][step];
					parital_sum_other_mask += num_of_other_mask[i][step];
					
					if( error_propagated[i][step] == true ) {
						error_propagated_tc = true;
					}
				}
				
				fw.write(String.format("C[%d]: (T:%d, R:%d, L:%d, I:%d, O:%d) EP:%b", i, parital_sum_time_mask, parital_sum_rel_mask, parital_sum_log_mask, parital_sum_if_mask, parital_sum_other_mask, error_propagated_tc));
				fw.newLine();
				for(int step = 0; step < num_of_time_mask[i].length; step++) {
					fw.write(String.format("t%d: (T:%d, R:%d, L:%d, I:%d, O:%d) EP:%b", step, num_of_time_mask[i][step], num_of_rel_mask[i][step], num_of_log_mask[i][step], num_of_if_mask[i][step], num_of_other_mask[i][step], error_propagated[i][step]));
					fw.newLine();
				}
			}
			fw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void WriteMaskingInfoFile(BufferedWriter fw, ArrayList<MaskingEffectLog> maskLog) {
		try {
			for(int i = 0; i < maskLog.size(); i++) {
				MaskingEffectLog pastLog = maskLog.get(i);
				fw.write(String.format("TC[%d]S[%d]", pastLog.testcaseNumber, pastLog.step)); fw.newLine();
				String maskingCategoryStr = null;
				switch(pastLog.maskingCategory) {
				case MaskingEffectLog.MASK_BY_IF: maskingCategoryStr = "BY_IF"; break;
				case MaskingEffectLog.MASK_BY_LOGICOP: maskingCategoryStr = "BY_LOGICOP"; break;
				case MaskingEffectLog.MASK_BY_TIME: maskingCategoryStr = "BY_TEMPORAL"; break;
				case MaskingEffectLog.MASK_BY_RELOP: maskingCategoryStr = "BY_RELOP"; break;
				case MaskingEffectLog.MASK_BY_OTHER: maskingCategoryStr = "BY_OTHER"; break;
				case MaskingEffectLog.MASK_RECOVERY: maskingCategoryStr = "RECOVERY"; break;
				case MaskingEffectLog.MASK_BY_ARITHOP: maskingCategoryStr = "BY_ARITH"; break;
				}
				fw.write(String.format("id:,%d,type:,%s,category:,%s,distance:%d", pastLog.maskNodeid, pastLog.maskNodeType, maskingCategoryStr, pastLog.distance)); fw.newLine();	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void WriteMaskingIntroInfoFile(BufferedWriter fw) {
		try {
			fw.write("Mutant Information"); fw.newLine();
			fw.write("Category:");
			switch(mutantCategory) {
			case ARITHMETIC_OP_MUTANT:
				fw.write(String.format(",Arithmetic Operator Mutant [%d]", ARITHMETIC_OP_MUTANT)); fw.newLine(); break;
			case RELATIONAL_OP_MUTANT:
				fw.write(String.format(",Relational Operator Mutant [%d]", RELATIONAL_OP_MUTANT)); fw.newLine(); break;
			case LOGICAL_OP_MUTANT:
				fw.write(String.format(",Logical Operator Mutant [%d]", LOGICAL_OP_MUTANT)); fw.newLine(); break;
			case LITERAL_MUTANT:
				fw.write(String.format(",Literal Value Mutant [%d]", LITERAL_MUTANT)); fw.newLine(); break;
			}
			
			if(mutantCategory == LITERAL_MUTANT) {
				fw.write(String.format("From:,%s", originalNode.get(0).value)); fw.newLine();
				fw.write(String.format("To:,%s", mutantNode.get(0).value)); fw.newLine();
			}else {
				fw.write(String.format("From:,%s", originalNode.getLabel())); fw.newLine();
				fw.write(String.format("To:,%s", mutantNode.getLabel())); fw.newLine();
			}
			
			fw.write(String.format("Mutant Node ID:,%d", mutantNodeId)); fw.newLine();
			fw.write(String.format("Mutant Parameter Value:,%d", mutantParam)); fw.newLine();
			fw.newLine();
			fw.write("Mask Information"); fw.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	void WriteMaskingInfoFile(String outputDir, ArrayList<MaskingEffectLog> maskLog) {
		BufferedWriter fw;
		try {
			fw = new BufferedWriter( new FileWriter(outputDir + File.separator + "masking_info.csv") );
		
			fw.write("Mutant Information"); fw.newLine();
			fw.write("Category:");
			switch(mutantCategory) {
			case ARITHMETIC_OP_MUTANT:
				fw.write(String.format(",Arithmetic Operator Mutant [%d]", ARITHMETIC_OP_MUTANT)); fw.newLine(); break;
			case RELATIONAL_OP_MUTANT:
				fw.write(String.format(",Relational Operator Mutant [%d]", RELATIONAL_OP_MUTANT)); fw.newLine(); break;
			case LOGICAL_OP_MUTANT:
				fw.write(String.format(",Logical Operator Mutant [%d]", LOGICAL_OP_MUTANT)); fw.newLine(); break;
			case LITERAL_MUTANT:
				fw.write(String.format(",Literal Value Mutant [%d]", LITERAL_MUTANT)); fw.newLine(); break;
			}
			
			if(mutantCategory == LITERAL_MUTANT) {
				fw.write(String.format("From:,%s", originalNode.get(0).value)); fw.newLine();
				fw.write(String.format("To:,%s", mutantNode.get(0).value)); fw.newLine();
			}else {
				fw.write(String.format("From:,%s", originalNode.getLabel())); fw.newLine();
				fw.write(String.format("To:,%s", mutantNode.getLabel())); fw.newLine();
			}
			
			fw.write(String.format("Mutant Node ID:,%d", mutantNodeId)); fw.newLine();
			fw.write(String.format("Mutant Parameter Value:,%d", mutantParam)); fw.newLine();
			fw.newLine();
			//fw.write("Test Suite information"); fw.newLine();
			//fw.write(String.format("Number of Test Case:,%d", numOfTestCase)); fw.newLine();
			//fw.newLine();
			fw.write("Mask Information"); fw.newLine();
			
			TreeMap<Integer, ArrayList<MaskingEffectLog>> tempmap = new TreeMap<>();
			
			int testCaseNumber = -1;
			
			for(int i = 0; i < maskLog.size(); i++) {
				MaskingEffectLog log = maskLog.get(i);
				if(testCaseNumber != log.testcaseNumber) {
					for(Integer key : tempmap.keySet()) {
						ArrayList<MaskingEffectLog> pastLogList = tempmap.get(key);
						for(MaskingEffectLog pastLog : pastLogList) {
							fw.write(String.format("TC[%d]S[%d]", pastLog.testcaseNumber, pastLog.step)); fw.newLine();
							String maskingCategoryStr = null;
							switch(pastLog.maskingCategory) {
							case MaskingEffectLog.MASK_BY_IF: maskingCategoryStr = "BY_IF"; break;
							case MaskingEffectLog.MASK_BY_LOGICOP: maskingCategoryStr = "BY_LOGICOP"; break;
							case MaskingEffectLog.MASK_BY_TIME: maskingCategoryStr = "BY_TEMPORAL"; break;
							case MaskingEffectLog.MASK_BY_RELOP: maskingCategoryStr = "BY_RELOP"; break;
							case MaskingEffectLog.MASK_BY_OTHER: maskingCategoryStr = "BY_OTHER"; break;
							case MaskingEffectLog.MASK_RECOVERY: maskingCategoryStr = "RECOVERY"; break;
							}
							
							fw.write(String.format("id:,%d,type:,%s,category:,%s,distance:%d", pastLog.maskNodeid, pastLog.maskNodeType, maskingCategoryStr, pastLog.distance)); fw.newLine();	
						}
					}
					
					testCaseNumber = log.testcaseNumber;
					tempmap.clear();
				}
				if( tempmap.containsKey(log.step) ) {
					tempmap.get(log.step).add(log);
				}else {
					ArrayList<MaskingEffectLog> list = new ArrayList<>();
					list.add(log);
					tempmap.put(log.step, list);
				}
			}
			
			fw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//2019-10-4 뮤턴트 리스트 파일 생성 : 200110-3
	public static void CreateMutantListFile(String inputLusfile, int NumberOfArithmeticMutant, int NumberOfRelationalMutant, int NumberOfLogicalMutant, int NumberOfLiteralMutant, int NumberOfNotMutant) {
		final int NumberOfArithmeticOp = 4; // +, - , * , ( / , INTDIV ) INTDIV와 DIV는 같은 나눗셈 계열로 한번만 카운트
		final int NumberOfLogicalOp = 3; // And, Or, Xor
		final int NumberOfRelOp = 4; // <, <=, >, >=
		
		ErrorPropagationTree oracle = null;
		try {
			System.out.println("inputLusfile: " + inputLusfile);
			oracle = new ErrorPropagationTree(inputLusfile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		String dirPath = inputLusfile.substring(0, inputLusfile.indexOf(File.separatorChar, inputLusfile.indexOf("example"+File.separatorChar)+8)); 
//		String mutantListFilePathPrefix = dirPath + File.separator + "mutant" + File.separator + "mutantlist";
		String mutantListFilePathPrefix = dirPath + File.separator + "mutantlist";
		
		int postfix = 1; 
		
		File mutantListFile = new File(mutantListFilePathPrefix + ".txt");
		
		while(mutantListFile.exists()) {
			mutantListFile = new File(String.format("%s(%d).txt", mutantListFilePathPrefix, postfix));
			postfix++;
		}
		
		int count = 0;
		
		ArrayList<Integer> ArithmeticNodeId = new ArrayList<>();
		ArrayList<Integer> LogicalNodeId = new ArrayList<>();
		ArrayList<Integer> RelationalNodeId = new ArrayList<>();
		ArrayList<Integer> LiteralNodeId = new ArrayList<>();
		ArrayList<Integer> NotNodeId = new ArrayList<>();
		//ArrayList<Integer> OtherNodeId = new ArrayList<>();
		
		for(int nodeid = 0; nodeid < oracle.nodelist.length; nodeid++) {
			if( oracle.nodelist[nodeid] != null ) {
				EPTNode node = oracle.nodelist[nodeid];
				switch(node.nodetype) {
				// Arithmetic Operations
				case PLUS: 
				case MINUS:
				case MUL:
				case DIV:
				case INTDIV:
					ArithmeticNodeId.add(nodeid);
					break;
				// Logical Operations
				case OR:
				case AND:
				case XOR:
					LogicalNodeId.add(nodeid);
					NotNodeId.add(nodeid);
					break;
				// Relational Operations
				case GT:
				case GTE:
				case LT:
				case LTE:
					RelationalNodeId.add(nodeid);
					break;
				// Literal Operations
				case LITERAL:
					LiteralNodeId.add(nodeid);
					break;
				case EQUAL:
				case NOTEQUAL:
				case NAND:
				case NXOR:
				case NOR:
					NotNodeId.add(nodeid);
					break;
				}
			}
		}
		
		ArrayList<String> fileString = new ArrayList<>();
		
		ArrayList<Integer> mut_param_selector = new ArrayList<Integer>();		
		for(int i = 0; i < ArithmeticNodeId.size() * (NumberOfArithmeticOp - 1); i++) {
			mut_param_selector.add(i);
		}
		Collections.shuffle(mut_param_selector);
		
		if( mut_param_selector.size() < NumberOfArithmeticMutant )
			NumberOfArithmeticMutant = mut_param_selector.size();
		
		for(int i = 0; i < NumberOfArithmeticMutant; i++) {
			int param = mut_param_selector.get(i);
			int mutantNodeID = ArithmeticNodeId.get( param / (NumberOfArithmeticOp - 1) );
			int mutantParam = calculateMutantParameter(oracle.nodelist[mutantNodeID].nodetype, ARITHMETIC_OP_MUTANT, ( param % (NumberOfArithmeticOp - 1) ) + 1);
			
			MutantErrorPropagationTree mutant = MutantErrorPropagationTree.CreateMutant(oracle, ARITHMETIC_OP_MUTANT, mutantNodeID, mutantParam);
			
			String mutantTypeStr = "Arithmetic Mutant";
			fileString.add(String.format("M%d: (%d,   %d,   %s,   %s,   %s)", count, mutant.mutantNodeId, mutant.mutantParam, mutantTypeStr, oracle.nodelist[mutant.mutantNodeId], mutant.nodelist[mutant.mutantNodeId]));
			count++;
			System.out.println("Arithmetic Mutant Creation Completed " + (i + 1) + "/" + NumberOfArithmeticMutant);
		}
		
		mut_param_selector.clear();		
		for(int i = 0; i < RelationalNodeId.size() * (NumberOfRelOp - 1); i++) {
			mut_param_selector.add(i);
		}
		Collections.shuffle(mut_param_selector);
		
		if( mut_param_selector.size() < NumberOfRelationalMutant )
			NumberOfRelationalMutant = mut_param_selector.size();
		
		for(int i = 0; i < NumberOfRelationalMutant; i++) {
			int param = mut_param_selector.get(i);
			int mutantNodeID = RelationalNodeId.get( param / (NumberOfRelOp - 1) );
			int mutantParam =  calculateMutantParameter(oracle.nodelist[mutantNodeID].nodetype, RELATIONAL_OP_MUTANT, ( param % (NumberOfRelOp - 1) ) + 1);
			
			MutantErrorPropagationTree mutant = MutantErrorPropagationTree.CreateMutant(oracle, RELATIONAL_OP_MUTANT, mutantNodeID, mutantParam);
			
			String mutantTypeStr = "Relational Mutant";
			fileString.add(String.format("M%d: (%d,   %d,   %s,   %s,   %s)", count, mutant.mutantNodeId, mutant.mutantParam, mutantTypeStr, oracle.nodelist[mutant.mutantNodeId], mutant.nodelist[mutant.mutantNodeId]));
			count++;
			System.out.println("Arithmetic Mutant Creation Completed " + (i + 1) + "/" + NumberOfArithmeticMutant);
		}
		
		
		mut_param_selector.clear();		
		for(int i = 0; i < LogicalNodeId.size() * (NumberOfLogicalOp - 1); i++) {
			mut_param_selector.add(i);
		}
		Collections.shuffle(mut_param_selector);
		
		if( mut_param_selector.size() < NumberOfLogicalMutant )
			NumberOfLogicalMutant = mut_param_selector.size();
		
		for(int i = 0; i < NumberOfLogicalMutant; i++) {
			int param = mut_param_selector.get(i);
			int mutantNodeID = LogicalNodeId.get( param / (NumberOfLogicalOp - 1) );
			int mutantParam =  calculateMutantParameter(oracle.nodelist[mutantNodeID].nodetype, LOGICAL_OP_MUTANT, ( param % (NumberOfLogicalOp - 1) ) + 1);
			
			MutantErrorPropagationTree mutant = MutantErrorPropagationTree.CreateMutant(oracle, LOGICAL_OP_MUTANT, mutantNodeID, mutantParam);
			
			String mutantTypeStr = "Logical Mutant";
			fileString.add(String.format("M%d: (%d,   %d,   %s,   %s,   %s)", count, mutant.mutantNodeId, mutant.mutantParam, mutantTypeStr, oracle.nodelist[mutant.mutantNodeId], mutant.nodelist[mutant.mutantNodeId]));
			count++;
			System.out.println("Logical Mutant Creation Completed " + (i + 1) + "/" + NumberOfLogicalMutant);
		}
		
		
		Collections.shuffle(LiteralNodeId);
//		if( LiteralNodeId.size() < NumberOfLogicalMutant )
//			NumberOfLiteralMutant = LiteralNodeId.size();
		if( LiteralNodeId.size() < NumberOfLiteralMutant )
			NumberOfLiteralMutant = LiteralNodeId.size();		
		
		for(int i = 0; i < NumberOfLiteralMutant; i++) {
			int mutantNodeID = LiteralNodeId.get(i);
			int mutantParam = new Random().nextBoolean() ? 1 : 0;
			MutantErrorPropagationTree mutant = MutantErrorPropagationTree.CreateMutant(oracle, LITERAL_MUTANT, mutantNodeID, mutantParam);
			
			String mutantTypeStr = "Literal Mutant";
			fileString.add(String.format("M%d: (%d,   %d,   %s,   %s,   %s)", count, mutant.mutantNodeId, mutant.mutantParam, mutantTypeStr, oracle.nodelist[mutant.mutantNodeId], mutant.nodelist[mutant.mutantNodeId]));
			count++;
			System.out.println("Literal Mutant Creation Completed " + (i + 1) + "/" + NumberOfLiteralMutant);
		}
		
		Collections.shuffle(NotNodeId);
		
//		if( mut_param_selector.size() < NumberOfLogicalMutant )
//			NumberOfLogicalMutant = mut_param_selector.size();
		if( NotNodeId.size() < NumberOfNotMutant )
			NumberOfNotMutant = NotNodeId.size();
		
		for(int i = 0; i < NumberOfNotMutant; i++) {			
			int mutantNodeID = NotNodeId.get(i);
//			System.out.println(i + ", " + mutantNodeID + ", " + NumberOfNotMutant);
			int mutantParam = 0;
			MutantErrorPropagationTree mutant = MutantErrorPropagationTree.CreateMutant(oracle, NOT_MUTANT, mutantNodeID, mutantParam);
			
			String mutantTypeStr = "Not Mutant";
			fileString.add(String.format("M%d: (%d,   %d,   %s,   %s,   %s)", count, mutant.mutantNodeId, mutant.mutantParam, mutantTypeStr, oracle.nodelist[mutant.mutantNodeId], mutant.nodelist[mutant.mutantNodeId]));
			count++;
//			System.out.println("Not Mutant Creation Completed " + (i + 1) + "/" + NumberOfLiteralMutant);
			System.out.println("Not Mutant Creation Completed " + (i + 1) + "/" + NumberOfNotMutant);
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(mutantListFile));
			bw.write("Oracle: " + oracle.lusfileName);
			bw.newLine();
			bw.write(String.format("Node statistics - (Arithmetic: %d (%d), Relational: %d (%d), Logical: %d (%d), Literal: %d, Not: %d)", ArithmeticNodeId.size(), ArithmeticNodeId.size()*(NumberOfArithmeticOp - 1), RelationalNodeId.size(), RelationalNodeId.size()*(NumberOfRelOp - 1), LogicalNodeId.size(), LogicalNodeId.size()*(NumberOfLogicalOp - 1), LiteralNodeId.size(), NotNodeId.size()));
			bw.newLine();
			bw.write(String.format("# of mutants: %d (Arithmetic: %d, Relational: %d, Logical: %d, Literal: %d, Not: %d", count, NumberOfArithmeticMutant, NumberOfRelationalMutant, NumberOfLogicalMutant, NumberOfLiteralMutant, NumberOfNotMutant));
			bw.newLine();
			bw.write("M#: (mutant node id, mutant param, mutant type, original node, mutant node)");
			bw.newLine();
			for(int i = 0; i < fileString.size(); i++) {
				String str = fileString.get(i);
				bw.write(str);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<int[]> ReadMutantListFile(String mutantlistFile) {
		File mutantlist = new File(mutantlistFile);
		try {
			ArrayList<int[]> mutantInfoList = new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new FileReader(mutantlist));
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();
			
			String line = null;
			
			while( (line = br.readLine()) != null ) {
				String[] data = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).split(",");
				
				int mutantNodeId = Integer.valueOf(data[0].trim());
				int mutantParam = Integer.valueOf(data[1].trim());
				int mutantCategory = -1;
				
				switch(data[2].trim()) {
				case "Arithmetic Mutant": mutantCategory = MutantErrorPropagationTree.ARITHMETIC_OP_MUTANT; break;
				case "Relational Mutant": mutantCategory = MutantErrorPropagationTree.RELATIONAL_OP_MUTANT; break;
				case "Logical Mutant": mutantCategory = MutantErrorPropagationTree.LOGICAL_OP_MUTANT; break;
				case "Literal Mutant": mutantCategory = MutantErrorPropagationTree.LITERAL_MUTANT; break;
				case "Not Mutant": mutantCategory = MutantErrorPropagationTree.NOT_MUTANT; break;
				default: mutantCategory = -1; 
				}
				
				mutantInfoList.add( new int[] {mutantNodeId, mutantParam, mutantCategory} );
				
				//System.out.println(String.format("M%d (id:%d, param:%d)", count, mutantNodeId, mutantParam));
			}
			br.close();
			return mutantInfoList;	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	void Test(Testsuite ts) {
		int numOftestcase = ts.testsuiteSize();
		for(int tcNumber = 0; tcNumber < numOftestcase; tcNumber++) {
			int stepsize = ts.getStepSize(tcNumber);
			oracle.simulate(ts.getTestCase(tcNumber), stepsize);
			this.simulate(ts.getTestCase(tcNumber), stepsize);
			
			HashMap<String, Value[]> testcase = ts.getTestCase(tcNumber);
			System.out.println("------------------------------------------------------------------------------");
			System.out.println("TestCase " + tcNumber);
			for(int step = 0; step < stepsize; step++) {
				System.out.print("Step " + step);
				Iterator<String> it = testcase.keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					Value[] values = testcase.get(key);
					System.out.printf("    %s : %s   ", key, values[step]);
				}
				System.out.println();
				Iterator<EPTNode> oracleIT = oracle.root.getChildren();
				Iterator<EPTNode> mutantIT = this.root.getChildren();
				
				while(oracleIT.hasNext() && mutantIT.hasNext()) {
					EPTNode oracleNode = oracleIT.next();
					EPTNode mutantNode = mutantIT.next();
					if( !oracleNode.get(step).equals(mutantNode.get(step)) ) {
						System.out.println("Error Detected");
					}
				}
				if(oracleIT.hasNext() || mutantIT.hasNext()) {
					System.out.println("Error. Different Number of Children");
				}
			}
		}
	}
	
	/*
	 * Error Value를 가진 node에서 부터 값을 부모 방향으로 propagation 시킴. 
	 * 만일 output node에 도달하면 false 반환. 도달하지 못하면 true 반환
	 */
	boolean maskingAnalysis(EPTNode node, int testCaseNumber, int step, int distance, ArrayList<MaskingEffectLog> log, int maxstep, int[] timestamps, int timestamp) {//, int[][] numOftimemask, int[][] numOfrelmask, int[][] numOflogmask, int[][] numOfifmask, int[][] numOfothermask) {
		timestamps[ node.nodeid ] = timestamp;
		if( node.nodetype == LustreNodeType.TOP) return false;
		if( node.get(step).value.equals("") || oracle.nodelist[node.nodeid].get(step).value.equals("") ) return true;
		
		if( !oracle.nodelist[node.nodeid].get(step).equals( node.get(step) ) ) {
			boolean masked = true;
			Iterator<EPTNode> it = node.getParent();
			while(it.hasNext()) {
				EPTNode parent = it.next();
				if(parent.nodetype == LustreNodeType.PRE ) {
					if( step + 1 < maxstep ) {
						if( timestamps[ parent.nodeid ] < timestamp ) {
							if( maskingAnalysis( parent, testCaseNumber, step + 1, distance + 1, log, maxstep, timestamps, timestamp/*, numOftimemask, numOfrelmask, numOflogmask, numOfifmask, numOfothermask*/) == false ) masked = false;
						}
					}else { // mask by PRE
						log.add( new MaskingEffectLog(testCaseNumber, step, parent.nodeid, "PRE", MaskingEffectLog.MASK_BY_TIME , distance + 1) );
						//numOftimemask[testCaseNumber][step]++;
					}
				}else if(parent.nodetype == LustreNodeType.ARROW) {
					if( (step == 0 && ((ArrowNode) parent).lChild == node) || (step > 0 && ((ArrowNode) parent).rChild == node) ) {
						if( timestamps[ parent.nodeid ] < timestamp ) {
							if( maskingAnalysis( parent, testCaseNumber, step , distance + 1, log, maxstep, timestamps, timestamp/*, numOftimemask, numOfrelmask, numOflogmask, numOfifmask, numOfothermask*/) == false ) masked = false;
						}
					}else { // mask by ARROW
						log.add( new MaskingEffectLog(testCaseNumber, step, parent.nodeid, "ARROW", MaskingEffectLog.MASK_BY_TIME , distance + 1) );	
						//numOftimemask[testCaseNumber][step]++;
					}
				}else {
					if( timestamps[ parent.nodeid ] < timestamp ) {
						if( maskingAnalysis(parent, testCaseNumber, step, distance + 1, log, maxstep, timestamps, timestamp/*, numOftimemask, numOfrelmask, numOflogmask, numOfifmask, numOfothermask*/) == false ) masked = false;
					}
				}
			}
			return masked;
		}else { // mask 발생 혹은 리커버리
			if(distance == 0) {
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_RECOVERY , distance ) );
				return true;
			}
			switch(node.nodetype) {
			case AND:
			case OR:
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_BY_LOGICOP , distance ) );
				//numOflogmask[testCaseNumber][step]++;
				break;
			case LT:
			case LTE:
			case GT:
			case GTE:
			case EQUAL:
			case NOTEQUAL:
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_BY_RELOP , distance ) );
				//numOfrelmask[testCaseNumber][step]++;
				break;
			case IF:
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_BY_IF , distance ) );
				//numOfifmask[testCaseNumber][step]++;
				break;
			case PLUS:
			case MUL:
			case MINUS:
			case INTDIV:
			case DIV:
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_BY_ARITHOP , distance ) );
				break;
			default:
				log.add( new MaskingEffectLog(testCaseNumber, step, node.nodeid, node.getLabel() , MaskingEffectLog.MASK_BY_OTHER , distance ) );
				//numOfothermask[testCaseNumber][step]++;
			}
			return true;
		}
	}
	
	int[] maskingAnalysisRevised_debug(EPTNode node, int testCaseNumber, int distance, int step, int maxstep, ArrayList<Map<Integer, int[]>> NodeIdmaskingTableMap) {
		Map<Integer, int[]> map = NodeIdmaskingTableMap.get(step);
		if( map.containsKey(node.nodeid) ) return map.get(node.nodeid);
		int[] maskingSourceTable = new int[] {0, 0, 0, 0, 0, 0, 0};
		map.put(node.nodeid, maskingSourceTable);
		
		if( node.nodetype == LustreNodeType.TOP) return maskingSourceTable;
		if( node.get(step).value.equals("") || oracle.nodelist[node.nodeid].get(step).value.equals("") ) return maskingSourceTable;
		
		if( !oracle.nodelist[node.nodeid].get(step).equals( node.get(step) ) ) {
			Iterator<EPTNode> it = node.getParent();
			while(it.hasNext()) {
				EPTNode parent = it.next();
				if(parent.nodetype == LustreNodeType.PRE ) {
					if( step + 1 < maxstep ) {
						int[] parentMaskingTable = maskingAnalysisRevised_debug( parent, testCaseNumber, distance + 1, step + 1, maxstep, NodeIdmaskingTableMap);
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
						maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
						maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
						maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
						maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
					}else { // mask by PRE
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME]++;
					}
				}else if(parent.nodetype == LustreNodeType.ARROW) {
					if( (step == 0 && ((ArrowNode) parent).lChild == node) || (step > 0 && ((ArrowNode) parent).rChild == node) ) {
						int[] parentMaskingTable = maskingAnalysisRevised_debug( parent, testCaseNumber, distance + 1, step, maxstep, NodeIdmaskingTableMap);
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
						maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
						maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
						maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
						maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
						maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
					}else { // mask by ARROW
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME]++;
					}
				}else {
					int[] parentMaskingTable = maskingAnalysisRevised_debug(parent, testCaseNumber, distance + 1, step, maxstep, NodeIdmaskingTableMap);
					maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
					maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
					maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
					maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
					maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
					maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
					maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
				}
			}
			
			System.out.println(String.format("Distance:%d ] node:%s(id:%d),step:%d , Time:%d, IF:%d, LOGICOP:%d, OTHER:%d, RELOP:%d, REC:%d, ARITHOP:%d ", distance, node, node.nodeid, step, maskingSourceTable[MaskingEffectLog.MASK_BY_TIME],
					maskingSourceTable[MaskingEffectLog.MASK_BY_IF], maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP], maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER], maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP], maskingSourceTable[MaskingEffectLog.MASK_RECOVERY], maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP]));
			return maskingSourceTable;
		}else { // mask 발생 혹은 리커버리
			if(distance == 0) {
				maskingSourceTable[MaskingEffectLog.MASK_RECOVERY]++;
				return maskingSourceTable;
			}
			switch(node.nodetype) {
			case AND:
			case OR:
				maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP]++;
				break;
			case LT:
			case LTE:
			case GT:
			case GTE:
			case EQUAL:
			case NOTEQUAL:
				maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP]++;
				break;
			case IF:
				maskingSourceTable[MaskingEffectLog.MASK_BY_IF]++;
				break;
			case PLUS:
			case MUL:
			case MINUS:
			case INTDIV:
			case DIV:
				maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP]++;
				break;
			default:
				maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER]++;
			}
			return maskingSourceTable;
		}
	}
	
	int[] maskingAnalysisRevised(EPTNode node, int testCaseNumber, int distance, int step, int maxstep, ArrayList<Map<Integer, int[]>> NodeIdmaskingTableMap) {
		Map<Integer, int[]> map = NodeIdmaskingTableMap.get(step);
		if( map.containsKey(node.nodeid) ) return map.get(node.nodeid);
		int[] maskingSourceTable = new int[] {0, 0, 0, 0, 0, 0, 0};
		map.put(node.nodeid, maskingSourceTable);
		
		if( node.nodetype == LustreNodeType.TOP) return maskingSourceTable;
		if( node.get(step).value.equals("") || oracle.nodelist[node.nodeid].get(step).value.equals("") ) return maskingSourceTable;
		
		if( !oracle.nodelist[node.nodeid].get(step).equals( node.get(step) ) ) {
			Iterator<EPTNode> it = node.getParent();
			while(it.hasNext()) {
				EPTNode parent = it.next();
				if(parent.nodetype == LustreNodeType.PRE ) {
					if( step + 1 < maxstep ) {
						int[] parentMaskingTable = maskingAnalysisRevised( parent, testCaseNumber, distance + 1, step + 1, maxstep, NodeIdmaskingTableMap);
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
						maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
						maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
						maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
						maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
					}else { // mask by PRE
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME]++;
					}
				}else if(parent.nodetype == LustreNodeType.ARROW) {
					if( (step == 0 && ((ArrowNode) parent).lChild == node) || (step > 0 && ((ArrowNode) parent).rChild == node) ) {
						int[] parentMaskingTable = maskingAnalysisRevised( parent, testCaseNumber, distance + 1, step, maxstep, NodeIdmaskingTableMap);
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
						maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
						maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
						maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
						maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
						maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
						maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
					}else { // mask by ARROW
						maskingSourceTable[MaskingEffectLog.MASK_BY_TIME]++;
					}
				}else {
					int[] parentMaskingTable = maskingAnalysisRevised(parent, testCaseNumber, distance + 1, step, maxstep, NodeIdmaskingTableMap);
					maskingSourceTable[MaskingEffectLog.MASK_BY_TIME] += parentMaskingTable[MaskingEffectLog.MASK_BY_TIME];
					maskingSourceTable[MaskingEffectLog.MASK_BY_IF] += parentMaskingTable[MaskingEffectLog.MASK_BY_IF];
					maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_LOGICOP];
					maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER] += parentMaskingTable[MaskingEffectLog.MASK_BY_OTHER];
					maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_RELOP];
					maskingSourceTable[MaskingEffectLog.MASK_RECOVERY] += parentMaskingTable[MaskingEffectLog.MASK_RECOVERY];
					maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP] += parentMaskingTable[MaskingEffectLog.MASK_BY_ARITHOP];
				}
			}
			return maskingSourceTable;
		}else { // mask 발생 혹은 리커버리
			if(distance == 0) {
				maskingSourceTable[MaskingEffectLog.MASK_RECOVERY]++;
				return maskingSourceTable;
			}
			switch(node.nodetype) {
			case AND:
			case OR:
				maskingSourceTable[MaskingEffectLog.MASK_BY_LOGICOP]++;
				break;
			case LT:
			case LTE:
			case GT:
			case GTE:
			case EQUAL:
			case NOTEQUAL:
				maskingSourceTable[MaskingEffectLog.MASK_BY_RELOP]++;
				break;
			case IF:
				maskingSourceTable[MaskingEffectLog.MASK_BY_IF]++;
				break;
			case PLUS:
			case MUL:
			case MINUS:
			case INTDIV:
			case DIV:
				maskingSourceTable[MaskingEffectLog.MASK_BY_ARITHOP]++;
				break;
			default:
				maskingSourceTable[MaskingEffectLog.MASK_BY_OTHER]++;
			}
			return maskingSourceTable;
		}
	}
	
	static int calculateMutantParameter(LustreNodeType nodetype, int MutationType, int shiftValue) {
		int mutantParameter = -1;
		int nodeTypeValue = -1;
		
		switch(MutationType) {
		// Arithmetic Operations
		case ARITHMETIC_OP_MUTANT: 
			final int NUMBER_OF_ARITHMETCOPERATIONS = 4;
			if( shiftValue < 0 )
				shiftValue = new Random().nextInt(NUMBER_OF_ARITHMETCOPERATIONS - 1) + 1;
			switch(nodetype) {
			case PLUS: nodeTypeValue = 0; break; 
			case MINUS: nodeTypeValue = 1; break;  
			case MUL: nodeTypeValue = 2; break;
			case DIV: //DiV와 IntDIV는 같은 종류로 취급. 정수 타입인 경우 IntDIV로 변환, 실수 타입인 경우 DIV로 변환 
			case INTDIV: nodeTypeValue = 3; break;
			}
			mutantParameter = (nodeTypeValue + shiftValue) % NUMBER_OF_ARITHMETCOPERATIONS;
			break;
		// Logical Operations
		case LOGICAL_OP_MUTANT:
			final int NUMBER_OF_LOGICALOPERATIONS = 3;
			if( shiftValue < 0 )
				shiftValue = new Random().nextInt(NUMBER_OF_LOGICALOPERATIONS - 1) + 1;
			switch(nodetype) {
			// Logical Operations
			case OR: nodeTypeValue = 0; break;
			case AND: nodeTypeValue = 1; break;
			case XOR: nodeTypeValue = 2; break;
			}
			mutantParameter = (nodeTypeValue + shiftValue) % NUMBER_OF_LOGICALOPERATIONS;
			break;
		// Relational Operations
		case RELATIONAL_OP_MUTANT:
			final int NUMBER_OF_RELATIONALOPERATIONS = 4;
			if( shiftValue < 0 )
				shiftValue = new Random().nextInt(NUMBER_OF_RELATIONALOPERATIONS - 1) + 1;

			switch(nodetype) {
			// Relational Operations
			case GT: nodeTypeValue = 0; break;
			case GTE: nodeTypeValue = 1; break;
			case LT: nodeTypeValue = 2; break;
			case LTE: nodeTypeValue = 3; break;
			}
			mutantParameter = (nodeTypeValue + shiftValue) % NUMBER_OF_RELATIONALOPERATIONS;
			break;
		// Literal Operations
		case LITERAL_MUTANT:
			if( shiftValue < 0 ) {
				boolean ranValue = new Random().nextBoolean(); 
				mutantParameter = ranValue ? 1 : 0;
			}else
				mutantParameter = shiftValue;
			break;
		case NOT_MUTANT:
			mutantParameter = 0;
			break;
		}
		return mutantParameter;
	}
	
	/* old version */
	/*
	static int getMutateParameter(EPTNode node) {
		int mutateParameter = 0;
		switch(node.nodetype) {
		// Arithmetic Operations
		case PLUS: 
		case MINUS:
		case MUL:
		case DIV:
		case INTDIV:
			final int NUMBER_OF_ARITHMETCOPERATIONS = 4;
			mutateParameter = new Random().nextInt(NUMBER_OF_ARITHMETCOPERATIONS - 1) + 1;
			switch(node.nodetype) {
			case PLUS: mutateParameter += 0; break; 
			case MINUS: mutateParameter += 1; break;  
			case MUL: mutateParameter += 2; break;
			case DIV: //DiV와 IntDIV는 같은 종류로 취급. 정수 타입인 경우 IntDIV로 변환, 실수 타입인 경우 DIV로 변환 
			case INTDIV: mutateParameter += 3; break;
			}
			mutateParameter %= NUMBER_OF_ARITHMETCOPERATIONS;
			break;
		// Logical Operations
		case OR:
		case AND:
		case XOR:
		case NOR:
		case NAND:
		case NXOR:
			final int NUMBER_OF_LOGICALOPERATIONS = 3;
			mutateParameter = new Random().nextInt(NUMBER_OF_LOGICALOPERATIONS - 1) + 1;
			switch(node.nodetype) {
			// Logical Operations
			case OR: mutateParameter += 0; break;
			case AND: mutateParameter += 1; break;
			case XOR: mutateParameter += 2; break;
			}
			mutateParameter %= NUMBER_OF_LOGICALOPERATIONS;
			break;
		// Relational Operations
		case GT:
		case GTE:
		case LT:
		case LTE:
		case EQUAL:
		case NOTEQUAL:
			final int NUMBER_OF_RELATIONALOPERATIONS = 6;
			 
			if(((BinaryNode) node).lChild.valtype != LustreValueType.BOOL) {//if(node.valtype != LustreValueType.BOOL) {
				mutateParameter= new Random().nextInt(NUMBER_OF_RELATIONALOPERATIONS - 1) + 1;
				switch(node.nodetype) {
				// Relational Operations
				case GT: mutateParameter += 0; break;
				case GTE: mutateParameter += 1; break;
				case LT: mutateParameter += 2; break;
				case LTE: mutateParameter += 3; break;
				case EQUAL: mutateParameter += 4; break;
				case NOTEQUAL: mutateParameter += 5; break;
				}
				mutateParameter %= NUMBER_OF_RELATIONALOPERATIONS;
			}else {
				if(node.nodetype == LustreNodeType.EQUAL) mutateParameter = 5;
				else if(node.nodetype == LustreNodeType.NOTEQUAL) mutateParameter = 4;
			}
			break;
		// Literal Operations
		case LITERAL:
			boolean ranValue = new Random().nextBoolean(); 
			mutateParameter = ranValue ? 1 : 0;
		}
		return mutateParameter;
	}
	*/
	private EPTNode mutate(EPTNode node, int MutationType, int mutateParameter) {
		EPTNode mutantNode = null;
		EPTNode lChild = null;
		EPTNode rChild = null;
		
		if(node.nodetype != LustreNodeType.LITERAL) {
			BinaryNode bNode = (BinaryNode) node;
			lChild = bNode.lChild;
			rChild = bNode.rChild;
			lChild.parent.remove(bNode);
			rChild.parent.remove(bNode);
		}
		
		switch(MutationType) {
		// Arithmetic Operations
		case ARITHMETIC_OP_MUTANT:
			switch(mutateParameter) {
			case 0: mutantNode = new PlusNode(lChild, rChild); break; 
			case 1: mutantNode = new MinusNode(lChild, rChild); break;  
			case 2: mutantNode = new MulNode(lChild, rChild); break;
			case 3: 
				if(node.valtype == LustreValueType.REAL)
					mutantNode = new DivNode(lChild, rChild);
				else if(node.valtype == LustreValueType.INT) {
					mutantNode = new IntDivNode(lChild, rChild);
				}
				break;
			}
			break;
		case LOGICAL_OP_MUTANT:
			switch(mutateParameter) {
			case 0: mutantNode = new OrNode(lChild, rChild); break; 
			case 1: mutantNode = new AndNode(lChild, rChild); break;  
			case 2: mutantNode = new XorNode(lChild, rChild); break;
			}
			break;
		case RELATIONAL_OP_MUTANT:
			switch(mutateParameter) {
			case 0: mutantNode = new GtNode(lChild, rChild); break; 
			case 1: mutantNode = new GteNode(lChild, rChild); break;  
			case 2: mutantNode = new LtNode(lChild, rChild); break;
			case 3: mutantNode = new LteNode(lChild, rChild); break;
			}
			break;
		case LITERAL_MUTANT:
			if(node.valtype == LustreValueType.INT) {
				int originalValue = Integer.valueOf(node.history[0].value);
				if(mutateParameter == 1) {
					// 200621 : mutation range 1 => 1000
					// originalValue += 1;
					originalValue += (int) (Math.random() * 1000);
				}else {
					// originalValue -= 1;					
					originalValue -= (int) (Math.random() * 1000);
				}
				node.history[0].value = String.valueOf(originalValue);
			}else if(node.valtype == LustreValueType.REAL) {
				double originalValue = Double.valueOf(node.history[0].value);
				if(mutateParameter == 1) {
					// originalValue += 1;
					originalValue += (int) (Math.random() * 1000);
				}else {
					// originalValue -= 1;					
					originalValue -= (int) (Math.random() * 1000);
				}
				node.history[0].value = String.valueOf(originalValue);
			}else if(node.valtype == LustreValueType.BOOL) {
				boolean originalValue = Boolean.valueOf(node.history[0].value);
				originalValue = !originalValue;
				node.history[0].value = String.valueOf(originalValue);
			}
			mutantNode = node;
			break;
		case NOT_MUTANT:
			switch(node.nodetype) {
			case EQUAL: mutantNode = new NeqNode(lChild, rChild); break;
			case NOTEQUAL: mutantNode = new EqNode(lChild, rChild); break;
			case AND: mutantNode = new NandNode(lChild, rChild); break;
			case NAND: mutantNode = new AndNode(lChild, rChild); break;
			case OR: mutantNode = new NorNode(lChild, rChild); break;
			case NOR: mutantNode = new OrNode(lChild, rChild); break;
			case XOR: mutantNode = new NxorNode(lChild, rChild); break;
			case NXOR: mutantNode = new XorNode(lChild, rChild); break;
			}
			break;
		}
		
		if(node.nodetype != LustreNodeType.LITERAL) { //Liter Mutant를 제외하고는 모두 새로운 노드를 생성하므로 mutantNode != null 만족
			mutantNode.nodeid = node.nodeid;
			mutantNode.valtype = node.valtype;
			mutantNode.mutable = node.mutable;
			mutantNode.connected = node.connected;
			mutantNode.parent = node.parent;
			mutantNode.ept = this;
			Iterator<EPTNode> it = node.parent.iterator();
			while(it.hasNext()) {
				EPTNode parent = it.next();
				if(parent instanceof UnaryNode) {
					((UnaryNode) parent).child = mutantNode;
				}else if(parent instanceof BinaryNode) {
					if( ((BinaryNode) parent).lChild == node ) ((BinaryNode) parent).lChild = mutantNode;
					else if( ((BinaryNode) parent).rChild == node ) ((BinaryNode) parent).rChild = mutantNode;
				}else if(parent instanceof TernaryNode) {
					if( ((TernaryNode) parent).lChild == node ) ((TernaryNode) parent).lChild = mutantNode;
					else if( ((TernaryNode) parent).mChild == node ) ((TernaryNode) parent).mChild = mutantNode;
					else if( ((TernaryNode) parent).rChild == node ) ((TernaryNode) parent).rChild = mutantNode;
				}
			}
		}
		
		return mutantNode;
	}
	
	private static void getMutableNode(EPTNode root, boolean[] visited, ArrayList<Integer> mutableNodes, int mutantType) {
		if( root != null && visited[root.nodeid] != true) {
			visited[root.nodeid] = true;
			if(mutantType == MutantErrorPropagationTree.ARITHMETIC_OP_MUTANT) {
				switch(root.nodetype) {
				/*Arithmetic Operations*/
				case PLUS:
				case MINUS:
				case MUL:
				case DIV:
				case INTDIV:
					mutableNodes.add(root.nodeid);
				default: break;
				}
			}else if(mutantType == MutantErrorPropagationTree.LITERAL_MUTANT) {
				switch(root.nodetype) {
				/*Literal Operations*/
				case LITERAL:
					mutableNodes.add(root.nodeid);
				}
			}else if(mutantType == MutantErrorPropagationTree.RELATIONAL_OP_MUTANT) {
				switch(root.nodetype) {
				/*Relational Operations*/
				case GT:
				case GTE:
				case LT:
				case LTE:
				case EQUAL:
				case NOTEQUAL:
					mutableNodes.add(root.nodeid);
				default: break;
				}
			}else if(mutantType == MutantErrorPropagationTree.LOGICAL_OP_MUTANT) {
				switch(root.nodetype) {
				/*Logical Operations*/
				case OR:
				case AND:
				case XOR:
				case NOR:
				case NAND:
				case NXOR:
					mutableNodes.add(root.nodeid);
				default: break;
				}
			}
			
			Iterator<EPTNode> it = root.getChildren();
			while(it.hasNext()) {
				getMutableNode(it.next(), visited, mutableNodes, mutantType);
			}
		}
	}
	
	private static void getMutableNode(EPTNode root, boolean[] visited, ArrayList<Integer> mutableNodes) {
		if( root != null && visited[root.nodeid] != true) {
			visited[root.nodeid] = true;
			switch(root.nodetype) {
			/*Arithmetic Operations*/
			case PLUS:
			case MINUS:
			case MUL:
			case DIV:
			case INTDIV:
			/*Logical Operations*/
			case OR:
			case AND:
			case XOR:
			case NOR:
			case NAND:
			case NXOR:
			/*Relational Operations*/
			case GT:
			case GTE:
			case LT:
			case LTE:
			case EQUAL:
			case NOTEQUAL:
			/*Literal Operations*/
			case LITERAL:
				mutableNodes.add(root.nodeid);
			}
			
			Iterator<EPTNode> it = root.getChildren();
			while(it.hasNext()) {
				getMutableNode(it.next(), visited, mutableNodes);
			}
		}
	}
	
	
	
/*
	private EPTNode mutate(EPTNode node) {
		EPTNode mutantNode = null;
		switch(node.nodetype) {
		/*Arithmetic Operations*/
/*		case PLUS: 
		case MINUS:
		case MUL:
		case DIV:
		case INTDIV:
			mutantNode = mutateArithmeticOperation(node); break;
		/*Logical Operations*/
/*		case OR:
		case AND:
		case XOR:
		case NOR:
		case NAND:
		case NXOR:
			mutantNode = mutateLogicalOperation(node); break;
		/*Relational Operations*/
/*		case GT:
		case GTE:
		case LT:
		case LTE:
		case EQUAL:
		case NOTEQUAL:
			mutantNode = mutateRelationalOperation(node); break;
		/*Literal Operations*/
/*		case LITERAL:
			mutantNode = mutateLiteral(node); break;
		}
		return mutantNode;
	}
	
	private EPTNode mutateArithmeticOperation(EPTNode node) {
		final int NUMBER_OF_ARITHMETCOPERATIONS = 5;
		int offset = new Random().nextInt(NUMBER_OF_ARITHMETCOPERATIONS - 1) + 1;
		switch(node.nodetype) {
		case PLUS: offset += 0; break; 
		case MINUS: offset += 1; break;  
		case MUL: offset += 2; break;
		case DIV: offset += 3; break;
		case INTDIV: offset += 4; break;
		}
		System.out.println("Arithmetic Mutation , offset : " + offset);
		offset %= NUMBER_OF_ARITHMETCOPERATIONS;
		
		EPTNode mutantNode = null;
		BinaryNode bNode = (BinaryNode) node;
		EPTNode lChild = bNode.lChild;
		EPTNode rChild = bNode.rChild;
		lChild.parent.remove(bNode);
		rChild.parent.remove(bNode);
		
		switch(offset) {
		case 0: mutantNode = new PlusNode(lChild, rChild); break; 
		case 1: mutantNode = new MinusNode(lChild, rChild); break;  
		case 2: mutantNode = new MulNode(lChild, rChild); break;
		case 3: mutantNode = new DivNode(lChild, rChild); break;
		case 4: mutantNode = new IntDivNode(lChild, rChild); break;
		}
		
		mutantNode.nodeid = node.nodeid;
		mutantNode.valtype = node.valtype;
		mutantNode.mutable = node.mutable;
		mutantNode.connected = node.connected;
		mutantNode.parent = node.parent;
		Iterator<EPTNode> it = node.parent.iterator();
		while(it.hasNext()) {
			EPTNode parent = it.next();
			if(parent instanceof UnaryNode) {
				((UnaryNode) parent).child = mutantNode;
			}else if(parent instanceof BinaryNode) {
				if( ((BinaryNode) parent).lChild == node ) ((BinaryNode) parent).lChild = mutantNode;
				else if( ((BinaryNode) parent).rChild == node ) ((BinaryNode) parent).rChild = mutantNode;
			}else if(parent instanceof TernaryNode) {
				if( ((TernaryNode) parent).lChild == node ) ((TernaryNode) parent).lChild = mutantNode;
				else if( ((TernaryNode) parent).mChild == node ) ((TernaryNode) parent).mChild = mutantNode;
				else if( ((TernaryNode) parent).rChild == node ) ((TernaryNode) parent).rChild = mutantNode;
			}
		}
		
		mutantCategory = ARITHMETIC_OP_MUTANT;
		mutantNodeId = node.nodeid;
		mutantParam = offset;

		
		return mutantNode;
	}
	
	private EPTNode mutateLogicalOperation(EPTNode node) {
		final int NUMBER_OF_LOGICALOPERATIONS = 3;
		int offset = new Random().nextInt(NUMBER_OF_LOGICALOPERATIONS - 1) + 1;
		switch(node.nodetype) {
		/*Logical Operations*/
/*		case OR: offset += 0; break;
		case AND: offset += 1; break;
		case XOR: offset += 2; break;
		}
		System.out.println("Logical Mutation , offset : " + offset);
		offset %= NUMBER_OF_LOGICALOPERATIONS;
		
		EPTNode mutantNode = null;
		BinaryNode bNode = (BinaryNode) node;
		EPTNode lChild = bNode.lChild;
		EPTNode rChild = bNode.rChild;
		lChild.parent.remove(bNode);
		rChild.parent.remove(bNode);
		
		switch(offset) {
		case 0: mutantNode = new OrNode(lChild, rChild); break; 
		case 1: mutantNode = new AndNode(lChild, rChild); break;  
		case 2: mutantNode = new XorNode(lChild, rChild); break;
		}
		
		mutantNode.nodeid = node.nodeid;
		mutantNode.valtype = node.valtype;
		mutantNode.mutable = node.mutable;
		mutantNode.connected = node.connected;
		mutantNode.parent = node.parent;
		Iterator<EPTNode> it = node.parent.iterator();
		while(it.hasNext()) {
			EPTNode parent = it.next();
			if(parent instanceof UnaryNode) {
				((UnaryNode) parent).child = mutantNode;
			}else if(parent instanceof BinaryNode) {
				if( ((BinaryNode) parent).lChild == node ) ((BinaryNode) parent).lChild = mutantNode;
				else if( ((BinaryNode) parent).rChild == node ) ((BinaryNode) parent).rChild = mutantNode;
			}else if(parent instanceof TernaryNode) {
				if( ((TernaryNode) parent).lChild == node ) ((TernaryNode) parent).lChild = mutantNode;
				else if( ((TernaryNode) parent).mChild == node ) ((TernaryNode) parent).mChild = mutantNode;
				else if( ((TernaryNode) parent).rChild == node ) ((TernaryNode) parent).rChild = mutantNode;
			}
		}
		
		mutantCategory = LOGICAL_OP_MUTANT;
		mutantNodeId = node.nodeid;
		mutantParam = offset;
		
		return mutantNode;
	}
	
	private EPTNode mutateRelationalOperation(EPTNode node) {
		final int NUMBER_OF_RELATIONALOPERATIONS = 6;
		int offset = -1;
		if(node.valtype != LustreValueType.BOOL) {
			offset = new Random().nextInt(NUMBER_OF_RELATIONALOPERATIONS - 1) + 1;
			switch(node.nodetype) {
			/*Relational Operations*/
/*			case GT: offset += 0; break;
			case GTE: offset += 1; break;
			case LT: offset += 2; break;
			case LTE: offset += 3; break;
			case EQUAL: offset += 4; break;
			case NOTEQUAL: offset += 5; break;
			}
			System.out.println("Relational Mutation , offset : " + offset);
			offset %= NUMBER_OF_RELATIONALOPERATIONS;
		}else {
			if(node.nodetype == LustreNodeType.EQUAL) offset = 5;
			else if(node.nodetype == LustreNodeType.NOTEQUAL) offset = 4;
		}
		offset = 5;
		EPTNode mutantNode = null;
		BinaryNode bNode = (BinaryNode) node;
		EPTNode lChild = bNode.lChild;
		EPTNode rChild = bNode.rChild;
		lChild.parent.remove(bNode);
		rChild.parent.remove(bNode);
		
		switch(offset) {
		case 0: mutantNode = new GtNode(lChild, rChild); break; 
		case 1: mutantNode = new GteNode(lChild, rChild); break;  
		case 2: mutantNode = new LtNode(lChild, rChild); break;
		case 3: mutantNode = new LteNode(lChild, rChild); break;
		case 4: mutantNode = new EqNode(lChild, rChild); break;
		case 5: mutantNode = new NeqNode(lChild, rChild); break;
		}
		
		mutantNode.nodeid = node.nodeid;
		mutantNode.valtype = node.valtype;
		mutantNode.mutable = node.mutable;
		mutantNode.connected = node.connected;
		mutantNode.parent = node.parent;
		Iterator<EPTNode> it = node.parent.iterator();
		while(it.hasNext()) {
			EPTNode parent = it.next();
			if(parent instanceof UnaryNode) {
				((UnaryNode) parent).child = mutantNode;
			}else if(parent instanceof BinaryNode) {
				if( ((BinaryNode) parent).lChild == node ) ((BinaryNode) parent).lChild = mutantNode;
				else if( ((BinaryNode) parent).rChild == node ) ((BinaryNode) parent).rChild = mutantNode;
			}else if(parent instanceof TernaryNode) {
				if( ((TernaryNode) parent).lChild == node ) ((TernaryNode) parent).lChild = mutantNode;
				else if( ((TernaryNode) parent).mChild == node ) ((TernaryNode) parent).mChild = mutantNode;
				else if( ((TernaryNode) parent).rChild == node ) ((TernaryNode) parent).rChild = mutantNode;
			}
		}
		
		mutantCategory = RELATIONAL_OP_MUTANT;
		mutantNodeId = node.nodeid;
		mutantParam = offset;
		
		return mutantNode;
	}
	
	private EPTNode mutateLiteral(EPTNode node) {
		boolean ranValue = new Random().nextBoolean(); 
		
		if(node.valtype == LustreValueType.INT) {
			int originalValue = Integer.valueOf(node.history[0].value);
			if(ranValue == true) {
				originalValue += 1;
			}else {
				originalValue -= 1;
			}
			node.history[0].value = String.valueOf(originalValue);
		}else if(node.valtype == LustreValueType.REAL) {
			double originalValue = Double.valueOf(node.history[0].value);
			if(ranValue == true) {
				originalValue += 1;
			}else {
				originalValue -= 1;
			}
			node.history[0].value = String.valueOf(originalValue);
		}else if(node.valtype == LustreValueType.BOOL) {
			boolean originalValue = Boolean.valueOf(node.history[0].value);
			originalValue = !originalValue;
			node.history[0].value = String.valueOf(originalValue);
		}
		System.out.println("Literal Mutation , value: " + node.history[0].value);
		
		mutantCategory = RELATIONAL_OP_MUTANT;
		mutantNodeId = node.nodeid;
		mutantParam = ranValue ? 1 : 0;
		
		return node;
	}
	*/
	
	
}
