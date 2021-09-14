import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

public class SimulationLog {
	int stepsize;
	int numOfvar;
	TreeMap<String, Value[]> result;
	
	SimulationLog(TreeMap<String, Value[]> result, int stepsize){
		this.result = result;
		this.stepsize = stepsize;
	}
	
	SimulationLog(String resultFile){
		//읽기
		TreeMap<String, Value[]> result;
	}
	
	public boolean compareWithmut(SimulationLog mutant) {
		if( mutant == null || mutant.result == null ) {
			ErrorLog.myErrorLog("ELOG: [mutant == null || mutant.result == null]");
			return false;
		}
		if( this.stepsize != mutant.stepsize ) {
			ErrorLog.myErrorLog("ELOG: [this.stepsize != mutant.stepsize]");
			return false;
		}
		
		TreeMap<String, Value[]> mutResult = mutant.result;
		
		Iterator<String> it = this.result.keySet().iterator();
		while(it.hasNext()) {
			String varName = it.next();
			
			if( !mutResult.containsKey(varName) ) {
				ErrorLog.myErrorLog("ELOG: !mutResult.containsKey(varName)"); // 두 결과 파일에 수록된 변수 목록이 같지 않음
				return false;
			}
		}
		mutant.result.keySet().iterator();
		
		for(int step = 0; step < stepsize; step++) {
			
		}
		
		
		return true;
	}
	
	public void toFile(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(filename) );
			Iterator<String> it = result.keySet().iterator();
			bw.write("Variable Name");
			for(int i = 0; i < stepsize; i++) {
				bw.write(",Step "+i);
			}
			bw.newLine();
			while(it.hasNext()) {
				String varname = it.next();
				bw.write(varname);
				Value[] values = result.get(varname);
				
				for(Value v : values) {
					bw.write("," + v.value);
				}
				bw.newLine();
			}
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

class SimulationHelper{
	static void ReadTestSwiteFile() {
		
	}
}
