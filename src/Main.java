import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.v4.runtime.RecognitionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import ErrorCatchTable.SortElement;
//import MutantErrorPropagationTree.MaskingEffectLog;

class SP{
	public static final int MICROWAVE = 0;
	public static final int INFUSION = 1;
	public static final int ALARM = 2;
	public static final int DOCKING = 3;
	public static final int ASW = 4;
	public static final int FGS = 5;
	public static final int LEVEL1 = 6;
	public static final int WBS = 7;
	public static final int TEST = 8;
	
	private static int model_ = 0;
	
	public static final int TS_COND_INDEX = 0;
	public static final int TS_OCOND_INDEX = 1;
	public static final int TS_DECI_INDEX = 2;
	public static final int TS_ODECI_INDEX = 3;
	public static final int TS_MCDC_INDEX = 4;
	public static final int TS_OMCDC_INDEX = 5;
	
	public static final int NOTHING = 0;
	public static final int COND = (1 << TS_COND_INDEX);
	public static final int OCOND = (1 << TS_OCOND_INDEX);
	public static final int DECI = (1 << TS_DECI_INDEX);
	public static final int ODECI = (1 << TS_ODECI_INDEX);
	public static final int MCDC = (1 << TS_MCDC_INDEX);
	public static final int OMCDC = (1 << TS_OMCDC_INDEX);
	public static final int GOOD = 64;
	public static final int BAD = 128;
	public static final int ALL = COND | OCOND | DECI | ODECI | MCDC | OMCDC;	
	
	private static String[] infusion_testcaseFileNames = new String[] {"example\\infusion\\infusion-condition.reduced.csv", "", "example\\infusion\\infusion-decision.reduced.csv", "", "example\\infusion\\infusion-mcdc.reduced.csv", "", "", ""};
	private static String[] alarm_testcaseFileNames = new String[] {"example\\alarm\\alarm-condition.reduced.csv", "", "example\\alarm\\alarm-decision.reduced.csv", "", "example\\alarm\\alarm-mcdc.reduced.csv", "", "", ""};
	private static String[] micro_testcaseFileNames = new String[] {"example\\microwave\\microwave-condition.reduced.csv", "", "example\\microwave\\microwave-decision.reduced.csv", "", "example\\microwave\\microwave-mcdc.reduced.csv", "", "", ""};
	private static String[] docking_testcaseFileNames = new String[] {"example\\DockingExample\\DockingExample-condition.reduced.csv", "", "example\\DockingExample\\DockingExample-decision.reduced.csv", "", "example\\DockingExample\\DockingExample-mcdc.reduced.csv", "", "", ""}; // condition , decision , MCDC 占쎈쐻占쎈윥占쎈뻘占쎈쐻占쎈윪筌뤿뱶�뒙占쎈뙔占쎌굲 占쎈쎗占쎈즲占쏙퐦�삕占쎈�듸옙�쐻占쎈윥筌랃옙 占쎈눇�뙼蹂��굲
	private static String[] asw_testcaseFileNames = new String[] { "example\\asw\\asw-condition.reduced.csv", "", "example\\asw\\asw-decision.reduced.csv", "", "example\\asw\\asw-mcdc.reduced.csv", "" };
	private static String[] fgs_testcaseFileNames = new String[] { "example\\fgs\\fgs-condition.reduced.csv", "", "example\\fgs\\fgs-decision.reduced.csv", "", "example\\fgs\\fgs-mcdc.reduced.csv", "" };
	private static String[] level1_testcaseFileNames = new String[] { "example\\level1\\level1-condition.reduced.csv", "", "example\\level1\\level1-decision.reduced.csv", "", "example\\level1\\level1-mcdc.reduced.csv", "" };
	private static String[] wbs_testcaseFileNames = new String[] { "example\\wbs\\wbs-condition.reduced.csv", "", "example\\wbs\\wbs-decision.reduced.csv", "", "example\\wbs\\wbs-mcdc.reduced.csv", "" };	
	private static String[] test_testcaseFileNames = new String[] { "example\\test\\test-condition.reduced.csv", "", "example\\test\\test-decision.reduced.csv", "", "example\\test\\test-mcdc.reduced.csv", "" };
	
	private static String[] alarm_mutantFiles = new String[]{"example\\alarm\\mutantlist.txt"};
	private static String[] infusion_mutantFiles = new String[]{"example\\infusion\\mutantlist.txt"};
	private static String[] microwave_mutantFiles = new String[]{"example\\microwave\\mutantlist.txt"};	
	private static String[] docking_mutantFiles = new String[]{"example\\DockingExample\\mutantlist.txt"};
	private static String[] asw_mutantFiles = new String[]{"example\\asw\\mutantlist.txt"};
	private static String[] fgs_mutantFiles = new String[]{"example\\fgs\\mutantlist.txt"};
	private static String[] level1_mutantFiles = new String[]{"example\\level1\\mutantlist.txt"};
	private static String[] wbs_mutantFiles = new String[]{"example\\wbs\\mutantlist.txt"};	
	private static String[] test_mutantFiles = new String[]{"example\\test\\mutantlist.txt"};
	
	
	static void setModel(int model){
		model_ = model;
	}
	static String getModelFileName(){
		switch(model_){
		case MICROWAVE: return "example\\microwave\\microwave.lus";
		case INFUSION: return "example\\infusion\\infusion.lus"; // "example\\infusion\\INFUSION_MGR_Functional_Faulty.lus";
		case ALARM: return "example\\alarm\\alarm-for-ept.lus"; // "example\\alarm\\ALARM_Functional_R2012.lus";
		case DOCKING: return "example\\DockingExample\\DockingExample.lus";
		case ASW: return "example\\asw\\asw.lus";	// "example\\asw\\asw_flattened.lus";
		case FGS: return "example\\fgs\\fgs.lus";	// "example\\fgs\\model\\FGS_no_interfaces_flattened.lus";
		case LEVEL1: return "example\\level1\\level1.lus";	// "example\\level1\\model\\Level1_revised_flatten.lus";
		case WBS: return "example\\wbs\\wbs.lus";	// "example\\wbs\\model\\WBS_Node_flattened.lus";
		case TEST: return "example\\test\\test.lus";
		}
		return null;
	}
	
	static String[] getMutantFileNames(){
		switch(model_){
		case MICROWAVE: return microwave_mutantFiles;
		case INFUSION: return infusion_mutantFiles;
		case ALARM: return alarm_mutantFiles;
		case DOCKING: return docking_mutantFiles;
		case ASW: return asw_mutantFiles;
		case FGS: return fgs_mutantFiles;
		case LEVEL1: return level1_mutantFiles;
		case WBS: return wbs_mutantFiles;
		case TEST: return test_mutantFiles;
		}
		return null;
	}
	
	static ArrayList<String> getTestCases(int testmode){
		ArrayList<String> testSuiteFileNames = new ArrayList<>();
		
		String[] model_testSuiteFileNames = null;
		
		switch(model_){
		case MICROWAVE: model_testSuiteFileNames = micro_testcaseFileNames; break;
		case INFUSION: model_testSuiteFileNames = infusion_testcaseFileNames; break;
		case ALARM: model_testSuiteFileNames = alarm_testcaseFileNames; break;
		case DOCKING: model_testSuiteFileNames = docking_testcaseFileNames; break;
		case ASW: model_testSuiteFileNames = asw_testcaseFileNames; break;
		case FGS: model_testSuiteFileNames = fgs_testcaseFileNames; break;
		case LEVEL1: model_testSuiteFileNames = level1_testcaseFileNames; break;
		case WBS: model_testSuiteFileNames = wbs_testcaseFileNames; break;	
		case TEST: model_testSuiteFileNames = wbs_testcaseFileNames; break;
		}
		
		int index = 0;
		while(testmode != 0) {
			if( (testmode & 1) != 0) {
				testSuiteFileNames.add(model_testSuiteFileNames[index]);
			}
			testmode >>= 1;
			index++;
		}
		return testSuiteFileNames;
	}
	
	
	static String[] getTestCases(){
		switch(model_){
		case MICROWAVE: return micro_testcaseFileNames;
		case INFUSION: return infusion_testcaseFileNames;
		case ALARM: return alarm_testcaseFileNames;
		case DOCKING: return docking_testcaseFileNames;
		case ASW: return asw_testcaseFileNames;
		case FGS: return fgs_testcaseFileNames;
		case LEVEL1: return level1_testcaseFileNames;
		case WBS: return wbs_testcaseFileNames;			
		case TEST: return test_testcaseFileNames;
		}
		return null;
	}
	
	static String getMutantFileName(){
		switch(model_){
		case MICROWAVE: return "example\\microwave\\mutantlist.txt"; // "example\\micro_wave\\mutant\\mutantlist_191015.txt";
		case INFUSION: return "example\\infusion\\mutantlist.txt";
		case ALARM: return "example\\alarm\\mutantlist.txt";
		case DOCKING: return "example\\DockingExample\\mutantlist.txt";
		case ASW: return "example\\asw\\mutantlist.txt";
		case FGS: return "example\\fgs\\mutantlist.txt";
		case LEVEL1: return "example\\level1\\mutantlist.txt";
		case WBS: return "example\\wbs\\mutantlist.txt";
		case TEST: return "example\\test\\mutantlist.txt";
		}
		return null;
	}
}

class ErrorCatchTable{
	String errorCatchTableFile;
	String modelFile;
	String mutantFile;
	ArrayList<String> testSuiteFiles;
	HashMap<String, Integer> endIndex;
	HashMap<String, Integer> startIndex;
	ArrayList<int[]> table; // row : mutant index , col : testcase index
	int[] mutantKillCount; //Test Case 蹂� mutantKillCount 媛�
	
	int numOfTestCase;
	int numOfMutants;
	
	public static final int KILL = 0;
	public static final int NOTKILLED = 1;
	public static final int ERROR = 2;
	public static final int FIRSTCOL = 3;
	
	ErrorCatchTable(String fileName){
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			errorCatchTableFile = fileName;
			testSuiteFiles = new ArrayList<>();
			table = new ArrayList<>();
			startIndex = new HashMap<>();
			endIndex = new HashMap<>();
			
			
			String line = null;
			line = br.readLine();
			modelFile = line.substring(line.indexOf(":")+2);
			line = br.readLine();
			mutantFile = line.substring(line.indexOf(":")+2);
			
			line = br.readLine();
			String[] testSuiteFileNames = line.split(",");
			
			for(int i = 1; i < testSuiteFileNames.length; i++){
				if( !testSuiteFileNames[i].equals(testSuiteFileNames[i-1]) ){
					if( i != 1 ){
						endIndex.put(testSuiteFileNames[i-1], i - 2);
					}
					startIndex.put(testSuiteFileNames[i], i - 1);
					testSuiteFiles.add( testSuiteFileNames[i] );
				}
			}
			endIndex.put(testSuiteFileNames[testSuiteFileNames.length - 1], testSuiteFileNames.length - 2);
			
			numOfTestCase = testSuiteFileNames.length - 1;
			
			line = br.readLine(); //�뀒�뒪�듃 耳��씠�뒪 踰덊샇 �씪�씤
			
			while( (line = br.readLine()) != null ){
				String[] tableLine = line.split(",");
				int[] tableLineInt = new int[tableLine.length - 1];
				
				for(int i = 1; i < tableLine.length; i++){
					if(tableLine[i].equals("")){
						tableLineInt[i - 1] = NOTKILLED;  	
					}else if(tableLine[i].equals("O")){
						tableLineInt[i - 1] = KILL;  	
					}else if(tableLine[i].equals("E")){
						tableLineInt[i - 1] = ERROR;  	
					}
				}
				table.add(tableLineInt);
			}
			numOfMutants = table.size();
			
			mutantKillCount = new int[numOfTestCase];
			Arrays.fill(mutantKillCount, 0);
			
			for(int[] tableLine : table){
				for(int i = 0; i < tableLine.length; i++){
					if( tableLine[i] == KILL )					
						mutantKillCount[i]++;
				}	
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String getTestSuiteFileName(int index){
		for(int i = 0; i < testSuiteFiles.size(); i++){
			String testSuiteFileName = testSuiteFiles.get(i);
			int startindex = startIndex.get(testSuiteFileName);
			int endindex = endIndex.get(testSuiteFileName);
			if( startindex <= index && index <= endindex){
				return testSuiteFileName;
			}
		}
		return null;
	}
	
	int getTestCaseNumber(int index){
		for(int i = 0; i < testSuiteFiles.size(); i++){
			String testSuiteFileName = testSuiteFiles.get(i);
			int startindex = startIndex.get(testSuiteFileName);
			int endindex = endIndex.get(testSuiteFileName);
			if( startindex <= index && index <= endindex){
				return index - startindex;
			}
		}
		return -1;
	}
	
	double mutantKillRatio(int testCaseIndex){
		return (double) mutantKillCount[testCaseIndex] / numOfMutants;
	}
	
	double mutantKillRatio(int startIndex, int endIndex){
		int mutantKillCount = 0;
		for(int[] tableLine : table){
			for(int i = startIndex; i <= endIndex; i++){
				if( tableLine[i] == KILL ){
					mutantKillCount++;
					break;
				}
			}	
		}
		return (double) mutantKillCount / numOfMutants;
	}
	
	double mutantKillRatio_tc(int testcaseIndex, ArrayList<Integer> mutantIndices){
		int mutantKillCount = 0;
		for(int mutantIndex : mutantIndices){
			int[] tableLine = table.get(mutantIndex);
			if( tableLine[testcaseIndex] == KILL ){
				mutantKillCount++;
			}	
		}
		return (double) mutantKillCount / mutantIndices.size();
	}
	
	double mutantKillRatio_ts(ArrayList<Integer> testcaseIndices, ArrayList<Integer> mutantIndices){
		int mutantKillCount = 0;
		
		for(int mutantIndex : mutantIndices){
			int[] tableLine = table.get(mutantIndex);
			for(int testcaseIndex : testcaseIndices){
				if( tableLine[testcaseIndex] == KILL ){
					mutantKillCount++;
					break;
				}
			}
		}
		
		return (double) mutantKillCount / mutantIndices.size();
	}
	
	int mutantKillCount(ArrayList<Integer> tcIndices){ //�빐�떦 test case�뱾�뿉 �쓽�빐 �옟�엳�뒗 裕ㅽ꽩�듃�쓽 �닽�옄
		int mutant_kill_count = 0; //�옟�쓣 �닔 �엳�뒗 裕ㅽ꽩�듃 以� �븘吏� �븞�옟�엺 裕ㅽ꽩�듃�쓽 �닔
		
		for(int[] tableLine : table){
			for(int tcIndex : tcIndices){
				if( tableLine[tcIndex] == KILL ){
					mutant_kill_count++;
					break;
				}
			}	
		}
		
		return mutant_kill_count;
	}
	
	class SortElement{
		int key;
		int value;
		
		SortElement(int key, int value){
			this.key = key;
			this.value = value;
		}
	}
	
	ArrayList<Integer> optimization(ArrayList<Integer> mutantIndices, ArrayList<Integer> tcIndices){
		ArrayList<Integer> result = new ArrayList<>(); //理쒖쟻�솕�맂 寃곌낵 TC瑜� 媛�吏�怨� �엳�뒗 諛곗뿴
		
		int[] mutantKillCount = new int[numOfTestCase]; //�젣�븳�맂 裕ㅽ꽩�듃 �궡�뿉�꽌 Test Case 蹂� mutantKillCount 媛�
		Arrays.fill(mutantKillCount, 0);
		
		boolean[] mutantKilled = new boolean[numOfMutants];
		Arrays.fill(mutantKilled, false);
		
		int numOfmut_left = 0; //�옟�쓣 �닔 �엳�뒗 裕ㅽ꽩�듃 以� �븘吏� �븞�옟�엺 裕ㅽ꽩�듃�쓽 �닔
		
		for(int mutantIndex : mutantIndices){
			int[] tableLine = table.get(mutantIndex);
			for(int tcIndex : tcIndices){
				if( tableLine[tcIndex] == KILL ){
					numOfmut_left++;
					break;
				}
			}	
		}
	
		for(int mutantIndex : mutantIndices){
			int[] tableLine = table.get(mutantIndex);
			for(int tcIndex : tcIndices){
				if(tableLine[tcIndex] == KILL)
					mutantKillCount[tcIndex]++;
			}
		}
		
		SortElement[] sort = new SortElement[tcIndices.size()];
		Comparator<SortElement> comp = new Comparator<SortElement>(){
			@Override
			public int compare(SortElement arg0, SortElement arg1) {
				return arg1.key - arg0.key;
			}
		};
		
		int i = 0;
		for(int tcIndex : tcIndices){
			sort[i++] = new SortElement(mutantKillCount[tcIndex], tcIndex);
		}
		
		while(numOfmut_left > 0){
			Arrays.sort(sort, comp);
			int selectedTcIndex = sort[0].value;
			numOfmut_left -= sort[0].key;
			mutantKillCount[selectedTcIndex] = 0;
			
			for(int mutantIndex : mutantIndices){
				int[] tableLine = table.get(mutantIndex);
				if(tableLine[selectedTcIndex] == KILL && mutantKilled[mutantIndex] == false){
					for(int tcIndex : tcIndices){
						if(tableLine[tcIndex] == KILL)
							mutantKillCount[tcIndex]--;
					}	
					mutantKilled[mutantIndex] = true;
				}	
			}
			
			i = 0;
			for(int tcIndex : tcIndices){
				sort[i].key = mutantKillCount[tcIndex];
				sort[i].value = tcIndex;
				i++;
			}
			
			result.add(selectedTcIndex);
		}
		
		return result;
	}
	
	ArrayList<Integer> goodTestCase(int number){
		SortElement[] sort = new SortElement[mutantKillCount.length];
		for(int i = 0; i < sort.length; i++){
			sort[i] = new SortElement(mutantKillCount[i], i);
		}
		Arrays.sort(sort, new Comparator<SortElement>(){
			@Override
			public int compare(SortElement arg0, SortElement arg1) {
				return arg1.key - arg0.key;
			}
		});
		
		ArrayList<Integer> goodTCindex = new ArrayList<>();
		for(int i = 0; i < number; i++){
			goodTCindex.add( sort[i].value );
		}
		return goodTCindex;
	}
	
	ArrayList<Integer> badTestCase(int number){
		SortElement[] sort = new SortElement[mutantKillCount.length];
		for(int i = 0; i < sort.length; i++){
			sort[i] = new SortElement(mutantKillCount[i], i);
		}
		Arrays.sort(sort, new Comparator<SortElement>(){
			@Override
			public int compare(SortElement arg0, SortElement arg1) {
				return arg0.key - arg1.key;
			}
		});
		
		ArrayList<Integer> badTCindex = new ArrayList<>();
		for(int i = 0; i < number; i++){
			badTCindex.add( sort[i].value );
		}
		return badTCindex;
	}
	
	double mutantKillRatio(String testSuiteFile){
		int startindex = startIndex.get(testSuiteFile);
		int endindex = endIndex.get(testSuiteFile);
		return mutantKillRatio(startindex, endindex);
	}
	
	void test(){
		ArrayList<Integer> goodtc = goodTestCase(100);
		int count = 0;
		for(int index : goodtc){
			System.out.println(String.format("%d. mkc[%d]:%d",++count, index, mutantKillCount[index]));
		}
		
		ArrayList<Integer> badtc = badTestCase(1000);
		count = 0;
		for(int index : badtc){
			System.out.println(String.format("%d. mkc[%d]:%d",++count, index, mutantKillCount[index]));
		}
	}
	
	void print(){
		System.out.println("errorCatchTableFile:"+errorCatchTableFile);
		System.out.println("modelFile:"+modelFile);
		System.out.println("mutantFile:"+mutantFile);

		System.out.println("numOfTestCase:"+numOfTestCase);
		System.out.println("numOfMutants:"+numOfMutants);
		
		for(String testSuiteFile: testSuiteFiles){
			System.out.println(String.format("Test Suite:%s , start index:%d, end index:%d , mutant_kill_ratio: %f",
					testSuiteFile, startIndex.get(testSuiteFile), endIndex.get(testSuiteFile), mutantKillRatio(testSuiteFile)));
		}
	}
}


class ErrorProbTable{
	boolean fileFound;
	ArrayList<Integer> nodeIds;
	ArrayList<ArrayList<Double>> eppValues;
	String modelName;
	String date;
	String TestFile;
	ArrayList<Double> eppOftc; // Error Propagation Probability of TestCase
	ArrayList<Double> eppOfNode;// Error Propagation Probability of Node
	double eppValue;
	
	ErrorProbTable(String fileName){
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			fileFound = true;
			eppValues = new ArrayList<>();
			eppOftc = new ArrayList<>();
			nodeIds = new ArrayList<>();
			eppOfNode = new ArrayList<>();
			
			
			String line = null;
			
			line = br.readLine();
			modelName = line.substring(line.indexOf(',') + 1);
			line = br.readLine();
			date = line.substring(line.indexOf(',') + 1);
			line = br.readLine();
			TestFile = line.substring(line.indexOf(',') + 1);
			line = br.readLine();
			String[] strs = line.split(",");
			for(int i = 1; i < strs.length; i++){
				String str = strs[i];
				nodeIds.add( Integer.parseInt( str.substring(str.indexOf("[") + 1, str.indexOf("]")) ) );
			}
			
			while( (line = br.readLine()) != null ){
				strs = line.split(",");
				ArrayList<Double> eppLine = new ArrayList<>();
				
				double avr = 0;
				for(int i = 1; i < strs.length; i++){
					String str = strs[i];
					double epp = Double.parseDouble(str);
					eppLine.add( epp );
					avr += epp;
				}
				eppValues.add(eppLine);
				avr /= nodeIds.size();
				eppOftc.add(avr);
			}
			
			eppValue = 0;
			for(int i = 0; i < nodeIds.size(); i++){
				//int nodeid = nodeIds.get(i);
				double epp = 1;
				for(int tcNumber = 0; tcNumber < eppValues.size(); tcNumber++){
					epp *= 1 - eppValues.get(tcNumber).get(i);
				}
				epp = 1 - epp;
				eppOfNode.add(epp);
				eppValue += epp;
			}
			eppValue /= nodeIds.size();
			br.close();
		}catch (FileNotFoundException e) {
			fileFound = false;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void print(){
		System.out.println("Model:"+modelName);
		System.out.println("Date:"+date);
		System.out.println("TestFile:"+TestFile);
		System.out.println("EppValue:"+eppValue);
	}
}

public class Main {
	public static void main(String[] args) {
		try{
			
			eppExperiment();
			
		}catch(RecognitionException e){
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void CreateMutantFile_FOREXPERIMENT(){  
		ArrayList<TestInformation> testInfos = new ArrayList<>();
		SP.setModel(SP.ALARM);
		testInfos.add(new TestInformation(SP.getModelFileName(), null, null, 0));
		SP.setModel(SP.INFUSION);
		testInfos.add(new TestInformation(SP.getModelFileName(), null, null, 0));
		SP.setModel(SP.MICROWAVE);
		testInfos.add(new TestInformation(SP.getModelFileName(), null, null, 0));
		SP.setModel(SP.DOCKING);
		testInfos.add(new TestInformation(SP.getModelFileName(), null, null, 0));
		for(TestInformation info: testInfos){
			String lustFileName = info.lustFileName;	
			for(int i = 0; i < 5; i++){
				MutantErrorPropagationTree.CreateMutantListFile(lustFileName, 100, 100, 100, 100, 100);
			}
		}
	}
	
	public static void traverseTree(BufferedWriter fw, String tcNumberStr, int stepsize, 
			ArrayList<Integer> backtrace, 
			EPTNode parentNode, EPTNode currentNode, 
			HashMap parentMap, int level) {
		
		ArrayList< HashMap<String, Object> > ParentChildrenArray = (ArrayList<HashMap<String, Object>>) parentMap.get("children");
			
		HashMap<String, Object> currentMap = new HashMap<>();
		currentMap.put("name", currentNode.getName());
		currentMap.put("id", currentNode.getNodeid());
		currentMap.put("label", currentNode.getLabel());
		currentMap.put("nodeType", currentNode.getNodeTypeStr());
		currentMap.put("valueType", currentNode.getValuTypeStr());
		
//		currentMap.put("values", currentNode.getHistoryStr());		
		String[] values = currentNode.getHistoryStr();
		try {
			fw.write(tcNumberStr + "," + currentNode.getNodeid() + ",");
			
			if (values.length == 1) {
				fw.write(values[0]);
			} else { 
				for (int i = 0; i < stepsize; i++) {
					fw.write(values[i]);
					if (i < stepsize - 1) 
						fw.write(",");
				}
			}
			fw.write("\n");
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		backtrace.add(currentNode.getNodeid());
		currentMap.put("children", new ArrayList< HashMap< String, Object> >());
		
		Iterator<EPTNode> childrenNodes = currentNode.getChildren();
		while(childrenNodes.hasNext()) {
			EPTNode childNode = childrenNodes.next();
//			System.out.println(level + "::" + currentNode.getNodeid() + "::" + currentNode.getLabel() + " goes to the new child " + childNode.getNodeid());
			
			if (backtrace.contains(childNode.getNodeid())) {
//				System.out.println("Circle found");
				
				// <VAR> label�씠 �삉 �굹�솕�떎硫�  �듃由ъ뿉�뒗 �꽔怨� �닚�쉶�뒗 �븞�븿
				if (childNode.getLabel() == "<VAR>") {
					ArrayList< HashMap<String, Object> > childrenArray = (ArrayList<HashMap<String, Object>>) currentMap.get("children");
					HashMap<String, Object> varMap = new HashMap<>();
					varMap.put("name", childNode.getName());
					varMap.put("id", childNode.getNodeid());
					varMap.put("label", childNode.getLabel());
					varMap.put("nodeType", childNode.getNodeTypeStr());
					varMap.put("valueType", childNode.getValuTypeStr());
					childrenArray.add(varMap);
				}
				continue;
			}
			
			traverseTree(fw, tcNumberStr, stepsize, backtrace, currentNode, childNode, currentMap, level+1);
		}
		// �떎 �걹�궡怨� �옄�떊 吏묒뼱�꽔湲�
		ParentChildrenArray.add(currentMap);
	}	
	
	public static void RandomTestCaseGeneration(String lustFileName, String testSuiteFilename) {
		ErrorPropagationTree ept;
		try {
			ept = new ErrorPropagationTree(lustFileName);
			int NumberOfTestCase = 13;
			int StepSize = 6;
			double RangeMax = 10;
			double RangeMin = -5;
			
			ArrayList<TestCaseInputInfo> inputInfoList = new ArrayList<>();
			
			for(int i = 0; i < ept.inputNodeIds.size(); i++) {
				InputNode inputNode = (InputNode) ept.nodelist[ept.inputNodeIds.get(i)];
				TestCaseInputInfo tcInfo = new TestCaseInputInfo(inputNode.name, inputNode.valtype, RangeMin, RangeMax);
			
				inputInfoList.add(tcInfo);
			}
			
			ArrayList<Integer> stepSizeList = new ArrayList<>();
			for(int i = 0; i < NumberOfTestCase; i++) {
				stepSizeList.add(StepSize);
			}
			
			Testsuite.GenerateRandomTestSuite(stepSizeList, inputInfoList, testSuiteFilename).toFile(testSuiteFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	static class TestInformation{
		public String lustFileName;
		public String mutantListFileName;
		public String[] testSuiteNames;
		public String[] mutantListFileNames;
		public int testmode;
		
		public TestInformation(String lustFileName, String mutantListFileName, String[] testSuiteNames, int testmode) {
			this.lustFileName = lustFileName;
			this.mutantListFileName = mutantListFileName;
			this.testSuiteNames = testSuiteNames;
			this.testmode = testmode;
		}
		
		public TestInformation(String lustFileName, String mutantListFileName, String[] testSuiteNames, int testmode, String[] mutantFileNames) {
			this.lustFileName = lustFileName;
			this.mutantListFileName = mutantListFileName;
			this.testSuiteNames = testSuiteNames;
			this.testmode = testmode;
			this.mutantListFileNames = mutantFileNames;
		}
	}
	// Error Propagation Probability
	public static void eppExperiment() { 
		ArrayList<TestInformation> testInfos = new ArrayList<TestInformation>();
		SP.setModel(SP.ALARM);
		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.INFUSION);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.ALARM);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );		
//		SP.setModel(SP.ASW);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.FGS);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.LEVEL1);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.WBS);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );		
//		SP.setModel(SP.DOCKING);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
//		SP.setModel(SP.MICROWAVE);
//		testInfos.add( new TestInformation(SP.getModelFileName(), SP.getMutantFileName(), SP.getTestCases(), SP.COND | SP.DECI | SP.MCDC) );
		

		eppExperiment(testInfos);
		System.out.println("End: " + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()));
	}
	
	public static void eppExperiment(ArrayList<TestInformation> testInfos) { 		
		for(TestInformation testInfo: testInfos) {
			try {
				String lustFileName = testInfo.lustFileName;
				String[] testSuiteNames = testInfo.testSuiteNames;
				
				ErrorPropagationTree oracle = new ErrorPropagationTree(lustFileName);
				
				ArrayList<Testsuite> tsList = new ArrayList<Testsuite>();
				int testmode = testInfo.testmode;
				int index = 0;
				while(testmode != 0) {
					if( (testmode & 1) != 0) {
						tsList.add(new Testsuite(oracle, testSuiteNames[index]));
					}
					testmode >>= 1;
					index++;
				}
				
				for(Testsuite ts : tsList) {
					String testSuiteFileName = ts.testcaseFileName.substring(ts.testcaseFileName.lastIndexOf(File.separator) + 1);
					System.out.println("TestSuite:" + testSuiteFileName);
					
					String outFileName = getMainDirectory(testInfo.lustFileName) + File.separator 
							+ getFileName(testInfo.lustFileName) + "_" + getFileName(testSuiteFileName)+"_errorprob.csv";
					BufferedWriter outfile = new BufferedWriter(new FileWriter(new File(outFileName)));
					outfile.write("Model,"+ testInfo.lustFileName);
					outfile.newLine();
					outfile.write(String.format("Date,%s", new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date())));
					outfile.newLine();
					outfile.write(String.format("TestFile,%s", testSuiteFileName));
					outfile.newLine();
					outfile.write("TcNumber");
					for( int nodeid = 0; nodeid < oracle.nodelist.length - 1; nodeid++ ) { 
						if( oracle.nodelist[nodeid] != null) {
							outfile.write(",EPP[" + nodeid +"]");
						}
					}
					
					// [T] loop
					for(int tcNumber = 0; tcNumber < ts.testsuiteSize(); tcNumber++) {
						int stepsize = ts.getStepSize(tcNumber);
						HashMap<String, Value[]> testcase = ts.getTestCase(tcNumber); 
						
						oracle.simulate(testcase, stepsize);
						
						outfile.newLine();
						outfile.write(""+tcNumber);
						
						// errorsource == output�씠 �븘�땶 node? [A] loop
						for( int ErrorSource = 0; ErrorSource < oracle.nodelist.length - 1; ErrorSource++ ) { 
							if( oracle.nodelist[ErrorSource] != null) {
								oracle.ErrorSimulation(ErrorSource, testcase, stepsize); 
								
								double epOrg = 1;
								
								// Pi (1 - P(A, O, T)) loop
								for(int step = 0; step < stepsize; step++) { // [O]utput loop
									EPTNode topnode = oracle.root;  
									epOrg *= 1 - topnode.eppGet(step);  
								}
								outfile.write(","+ (1 - epOrg)); // 1 - Pi (1 - P(A, O, T)) write 
							}
						}
						System.out.println(String.format("TestCase %d / %d", tcNumber, ts.testsuiteSize()));
					}
					outfile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getFileName(String filepath){
		return filepath.substring(filepath.lastIndexOf(File.separatorChar) + 1);
	}
	
	public static String getMainDirectory(String modelpath){
		String dirPath = modelpath.substring(0, modelpath.indexOf(File.separatorChar, modelpath.indexOf("example"+File.separatorChar)+8)); 
		System.out.println(dirPath);
		return dirPath;
	}
}