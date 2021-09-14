import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

class TestCaseInputInfo{
	String inputName;
	LustreValueType type;
	double rangeMax;
	double rangeMin;
	
	TestCaseInputInfo(String inputName, LustreValueType type){
		this(inputName, type, Double.NaN, Double.NaN);
	}
	
	TestCaseInputInfo(String inputName, LustreValueType type, double rangeMin, double rangeMax){
		this.inputName = inputName;
		this.type = type;
		this.rangeMax = rangeMax;
		this.rangeMin = rangeMin;
	}
	
}

public class Testsuite {
	public String testcaseFileName;
	
	ArrayList<String> inputNames;
	ArrayList<String[]> testCaseString;
	ArrayList<HashMap<String, Value[]>> testsuite;
	ArrayList<Integer> stepSizeList;
	boolean valid;
	
	static final String DELIMITER = ",";
	
	public Testsuite(){
		inputNames = new ArrayList<>();
		testCaseString = new ArrayList<>();
		testsuite = new ArrayList<>();
		stepSizeList = new ArrayList<>();
		valid = false;
	}
	
	public Testsuite(String testcaseFileename){ //testcase 파일을 읽어서 Testsuite 인스턴스 생성
		inputNames = new ArrayList<>();
		testCaseString = new ArrayList<>();
		testsuite = new ArrayList<>();
		stepSizeList = new ArrayList<>();
		valid = false;
		this.testcaseFileName = testcaseFileename;
		
		read(testcaseFileename);
	}
	
	
	public Testsuite(ErrorPropagationTree ept, String testcaseFileename){ //testcase 파일을 읽어서 Testsuite 인스턴스 생성
		inputNames = new ArrayList<>();
		testCaseString = new ArrayList<>();
		testsuite = new ArrayList<>();
		stepSizeList = new ArrayList<>();
		valid = false;
		this.testcaseFileName = testcaseFileename;
		
		read(testcaseFileename, ept);
	}
	
	public static Testsuite GenerateRandomTestSuite(ArrayList<Integer> stepSizeList, ArrayList<TestCaseInputInfo> inputInfoList, String testcaseFilename) {
		Testsuite randomTs = new Testsuite();
		randomTs.testcaseFileName = testcaseFilename;
		
		for(TestCaseInputInfo inputInfo : inputInfoList) {
			randomTs.inputNames.add(inputInfo.inputName);
		}
		
		for(int stepSize: stepSizeList) {
			randomTs.stepSizeList.add(stepSize);
			HashMap<String, Value[]> testcase = GenerateRandomTestCase(stepSize, inputInfoList); 
			randomTs.testsuite.add(testcase);
			
			StringBuffer[] stb = new StringBuffer[stepSize];
			for(int step = 0; step < stepSize; step++) {
				stb[step] = new StringBuffer();
			}
			
			for(int i = 0; i < randomTs.inputNames.size(); i++) {
				String inputName = randomTs.inputNames.get(i);
				Value[] values = testcase.get(inputName);
				for(int step = 0; step < stepSize; step++) {
					stb[step].append(values[step].value);
					if(i != randomTs.inputNames.size() - 1)
						stb[step].append(",");
				}
			}
			
			String[] testCaseLines = new String[stepSize];
			for(int step = 0; step < stepSize; step++) {
				testCaseLines[step] = stb[step].toString();
			}
			
			randomTs.testCaseString.add(testCaseLines);
		}
		
		
		randomTs.valid = true;
		return randomTs;
	}
	
	public void clear(){
		testCaseString.clear();
		testsuite.clear();
		stepSizeList.clear();;
		valid = true;
	}
	
	public void addTestCase(HashMap<String, Value[]> testcase, int stepSize){
		testsuite.add(testcase);
		stepSizeList.add(stepSize);
	}
	
	public void toFile(String testcaseFileName) {
		if(valid == true && testcaseFileName != null) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(testcaseFileName));
				StringBuffer stb = new StringBuffer();
				
				for(int i = 0; i < inputNames.size(); i++) {
					stb.append(inputNames.get(i));
					if(i != inputNames.size() - 1) {
						stb.append(",");
					}
				}
				
				bw.write(stb.toString());
				bw.newLine();
				
				for(int testCaseNumber = 0; testCaseNumber < testsuite.size(); testCaseNumber++){
					int stepSize = stepSizeList.get(testCaseNumber);
					HashMap<String, Value[]> testcase = testsuite.get(testCaseNumber);
					
					StringBuffer[] stbArr = new StringBuffer[stepSize];
					for(int i = 0; i < stbArr.length; i++){
						stbArr[i] = new StringBuffer();
					}
					
					for(int i = 0; i < inputNames.size(); i++) {
						String inputName = inputNames.get(i);
						Value[] values = testcase.get(inputName);
						
						for(int step = 0; step < stepSize; step++){
							stbArr[step].append(values[step].value);
							if(i != inputNames.size() - 1) {
								stbArr[step].append(",");
							}								
						}
					}
					
					for(int step = 0; step < stepSize; step++){
						bw.write(stbArr[step].toString());
						bw.newLine();
					}
					if( testCaseNumber != testsuite.size() - 1 )
						bw.newLine();
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static HashMap<String, Value[]> GenerateRandomTestCase(int step, ArrayList<TestCaseInputInfo> inputInfoList){
		HashMap<String, Value[]> testcase = new HashMap<>();
		
		for(TestCaseInputInfo inputInfo : inputInfoList) {
			Value[] value = new Value[step];
			
			double rangeMax = !Double.isNaN(inputInfo.rangeMax) ? inputInfo.rangeMax : Integer.MAX_VALUE;
			double rangeMin = !Double.isNaN(inputInfo.rangeMin) ? inputInfo.rangeMin : Integer.MIN_VALUE;
			
			for(int i = 0; i < step; i++) {
				value[i] = new Value();
				value[i].type = inputInfo.type;
				
				if( inputInfo.type == LustreValueType.BOOL ) {
					value[i].value = String.valueOf( new Random().nextBoolean() ); 
				}else {
					double randomValue = new Random().nextDouble() * (rangeMax - rangeMin) + rangeMin;
					if( inputInfo.type == LustreValueType.INT ) {
						value[i].value = String.valueOf( (int) randomValue );
					}else if( inputInfo.type == LustreValueType.REAL ) {
						value[i].value = String.valueOf( randomValue );
					}
				}
			}
			
			testcase.put(inputInfo.inputName, value);
		}
		
		return testcase;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public int testsuiteSize() {
		return testsuite.size();
	}
	
	public String[] getTestCaseString(int index) {
		return testCaseString.get(index);
	}
	
	public HashMap<String, Value[]> getTestCase(int index){
		return testsuite.get(index);
	}
	
	public int getStepSize(int index) {
		return stepSizeList.get(index);
	}
	
	public Iterator<HashMap<String, Value[]>> iterator(){
		return testsuite.iterator();
	}
	
	private void read(String filename, ErrorPropagationTree ept) {
		valid = false;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String firstLine = br.readLine();
			
			String[] variables = firstLine.split(DELIMITER);
			int number_of_input = variables.length;
			
			for(int i = 0; i < number_of_input; i++) {
				inputNames.add(variables[i]);
			}
			
			LustreValueType[] typeList = new LustreValueType[number_of_input];
			boolean typeCheck = false;
			
			if(ept != null) {
				typeCheck = true;
				
				if( ept.inputNodeIds.size() != number_of_input ) {
					ErrorLog.myErrorLog("ELOG: [ept.inputNodeIds.size() != number_of_input]");
					br.close();
					return;
				}else {
					for(int i = 0; i < variables.length; i++) {
						if( !ept.inputNodes.containsKey( variables[i] ) ) {
							ErrorLog.myErrorLog("ELOG: !ept.inputNodes.containsKey( inputName )");
							br.close();
							return;
						}
						typeList[i] = ept.inputNodes.get(variables[i]).valtype;
					}
				}
			}
			
			ArrayList<String> lines = new ArrayList<>();
			String line = null;
			
			ArrayList<Value[]> listOfvalurArr = new ArrayList<>(number_of_input);
			
			while( true ) {
				line = br.readLine();
				if(line != null && !line.equals("")) lines.add(line);
				else if( lines.size() != 0) {
					int stepsize = lines.size();
					
					for(int i = 0; i < number_of_input; i++) {
						listOfvalurArr.add(new Value[stepsize]);
					}
					
					for(int step = 0; step < stepsize; step++) {
						String[] values = lines.get(step).split(DELIMITER);
						
						for(int inputIndex = 0; inputIndex < number_of_input; inputIndex++) {
							Value[] inputNodeValues = listOfvalurArr.get(inputIndex);
							
							if(typeCheck == true) {
								switch(typeList[inputIndex]) {
								case INT: 
									if( !isInteger(values[inputIndex]) ) {
										ErrorLog.myErrorLog("ELOG:[!isInteger(values[inputIndex])]");
										return;
									}
									break;
								case REAL:
									if( !isReal(values[inputIndex]) ) {
										ErrorLog.myErrorLog("ELOG:[!isReal(values[inputIndex])]");
										return;
									}
									break;
								case BOOL:
									if(values[inputIndex].equals("0") || values[inputIndex].toLowerCase().equals("false") ) {
										values[inputIndex] = "false";
									}else if(values[inputIndex].equals("1") || values[inputIndex].toLowerCase().equals("true")){
										values[inputIndex] = "true";
									}else {
										ErrorLog.myErrorLog("ELOG:[isBoolean(values[inputIndex])]");
										return;
									}
									/*
									if( !isBoolean(values[inputIndex]) ) {
										ErrorLog.myErrorLog("ELOG:[isBoolean(values[inputIndex])]");
										return;
									}*/
									break;
								default:
								}
							}
							inputNodeValues[step] = new Value(typeList[inputIndex] ,values[inputIndex]);
						}
					}
					
					HashMap<String, Value[]> testcase = new HashMap<>();
					
					for(int inputIndex = 0; inputIndex < number_of_input; inputIndex++) {
						 testcase.put(variables[inputIndex], listOfvalurArr.get(inputIndex));
					}
					testsuite.add(testcase);
					stepSizeList.add(stepsize);
					
					String[] testCaseLines = new String[lines.size()];
					for(int linesIndex = 0; linesIndex < lines.size(); linesIndex++) {
						testCaseLines[linesIndex] = lines.get(linesIndex);
					}
					testCaseString.add(testCaseLines);
					
					lines.clear();
					listOfvalurArr.clear();
				}else if( line == null ) break;
			}
			
			br.close();
			valid = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void read(String filename) {
		valid = false;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String firstLine = br.readLine();
			
			String[] variables = firstLine.split(DELIMITER);
			int number_of_input = variables.length;
			
			for(int i = 0; i < number_of_input; i++) {
				inputNames.add(variables[i]);
			}
			
			LustreValueType[] typeList = new LustreValueType[number_of_input];
			boolean typeCheck = false;
			
			ArrayList<String> lines = new ArrayList<>();
			String line = null;
			
			ArrayList<Value[]> listOfvalurArr = new ArrayList<>(number_of_input);
			
			while( true ) {
				line = br.readLine();
				if(line != null && !line.equals("")) lines.add(line);
				else if( lines.size() != 0) {
					int stepsize = lines.size();
					
					for(int i = 0; i < number_of_input; i++) {
						listOfvalurArr.add(new Value[stepsize]);
					}
					
					for(int step = 0; step < stepsize; step++) {
						String[] values = lines.get(step).split(DELIMITER);
						
						for(int inputIndex = 0; inputIndex < number_of_input; inputIndex++) {
							Value[] inputNodeValues = listOfvalurArr.get(inputIndex);
							
							inputNodeValues[step] = new Value(null ,values[inputIndex]);
						}
					}
					
					HashMap<String, Value[]> testcase = new HashMap<>();
					
					for(int inputIndex = 0; inputIndex < number_of_input; inputIndex++) {
						 testcase.put(variables[inputIndex], listOfvalurArr.get(inputIndex));
					}
					testsuite.add(testcase);
					stepSizeList.add(stepsize);
					
					String[] testCaseLines = new String[lines.size()];
					for(int linesIndex = 0; linesIndex < lines.size(); linesIndex++) {
						testCaseLines[linesIndex] = lines.get(linesIndex);
					}
					testCaseString.add(testCaseLines);
					
					lines.clear();
					listOfvalurArr.clear();
				}else if( line == null ) break;
			}
			
			br.close();
			valid = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void test() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter( new FileWriter("testout.txt") );
		if(valid == false) return;
		for(int i = 1; i <= testsuite.size(); i++) {
			System.out.println("----------------------------Test Case no."+i);
			bw.write("----------------------------Test Case no."+i); bw.newLine();
			
			HashMap<String, Value[]> testcase = testsuite.get(i - 1);
			int stepsize = stepSizeList.get(i - 1);
			
			for(int step = 0; step < stepsize; step++) {
				System.out.print("step " + step + ": ");
				bw.write("step " + step + ": ");
				Iterator<String> it = testcase.keySet().iterator();
				while(it.hasNext()) {
					String s = it.next();
					Value[] v = testcase.get(s);
					System.out.printf(" [%s , %s] ", s , v[step] );
					bw.write(" [" + s + " , " + v[step] +"] ");
				}
				System.out.println();
				bw.newLine();
			}
			System.out.println();
			bw.newLine();
		}
		bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static boolean isInteger(String value) {
		try {
	        Integer.parseInt(value);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
	private static boolean isReal(String value) {
		try {
	        Double.parseDouble(value);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
	private static boolean isBoolean(String value) {
		String value_ = value.toLowerCase();
		return value_.equals("true") || value_.equals("false"); 
	}
	
	public static void main(String[] args) {
		String filename = "example\\alarm\\test\\mcdc_inputs_reduced.csv";
		Testsuite ts = new Testsuite(null, null);
		//ts.read(filename);
	}
}
