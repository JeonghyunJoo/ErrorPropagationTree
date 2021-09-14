import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class ErrorPropagationTree {
	HashMap<String, InputNode> inputNodes;
	ArrayList<Integer> inputNodeIds;
	
	HashMap<String, VariableNode> variables;
	ArrayList<Integer> variableNodeIds;
	
	HashMap<String, ConstNode> constNodes;
	ArrayList<Integer> constNodeIds;
	
	EPTNode[] nodelist;
	EPTNode root;
	int size; // total number of nodes in the Graph 
	int treeSize; // total number of nodes in the connected component containing output variable nodes
	
	String topnodeName;
	String lusfileName;
	
	int SimulationErrorCode = NO_ERROR;
	String SimulationErrorMessage = "";
	static final int NO_ERROR = 0;
	static final int DIVIDE_BY_ZERO_ERROR = 1;
	
	private ErrorPropagationTree() {
		inputNodes = new HashMap<String, InputNode>();
		inputNodeIds = new ArrayList<>();
		
		variables = new HashMap<String, VariableNode>();
		variableNodeIds = new ArrayList<>();
		
		constNodes = new HashMap<>();
		constNodeIds = new ArrayList<>();
		
		nodelist = null;
		root = null;
		size = 0;
		treeSize = 0;
		
		topnodeName = null;
		lusfileName = null;
	}
	
	protected ErrorPropagationTree(ErrorPropagationTree copyTree) {
		this();
		
		this.size = copyTree.size;
		this.treeSize = copyTree.treeSize;
		this.topnodeName = copyTree.topnodeName;
		this.nodelist = new EPTNode[copyTree.nodelist.length];
		Arrays.fill(this.nodelist, null);
		this.root = clone(this, copyTree.root);
		
		String lusfileName = copyTree.lusfileName;
		if( lusfileName.lastIndexOf("\\") != -1 ) lusfileName = lusfileName.substring(lusfileName.lastIndexOf("\\") + 1 , lusfileName.length());
		this.lusfileName = "[C]" + lusfileName;
	}
	
	public ErrorPropagationTree(String lusfilename) throws IOException {
		this();
		this.lusfileName = lusfilename;
		build();
	}
	
	public ErrorPropagationTree clone() {
		return new ErrorPropagationTree(this);
	}
	
	private static EPTNode clone(ErrorPropagationTree tree, EPTNode root) {
		if( root != null ) {
			if(tree.nodelist[ root.nodeid ] == null) {
				EPTNode cloneRoot = root.semiClone();
				tree.nodelist[ cloneRoot.nodeid ] = cloneRoot;
				cloneRoot.SetErrorPropagationTree(tree);
				
				if( cloneRoot instanceof VariableNode ) {
					tree.registerVariables((VariableNode) cloneRoot);
				}else if( cloneRoot instanceof InputNode ) {
					tree.registerInput((InputNode) cloneRoot);
				}else if( cloneRoot instanceof ConstNode ) {
					tree.registerConst((ConstNode) cloneRoot);
				}
				
				Iterator<EPTNode> it = root.getChildren();
				
				while(it.hasNext()) {
					EPTNode child = it.next();
					EPTNode cloneSubTree = clone(tree, child);
					cloneRoot.addChild(cloneSubTree);
				}
				return cloneRoot;
			}else return tree.nodelist[ root.nodeid ];
		}
		return null;
	}
	
	private void build() throws IOException {
		LustreLexer lex = new LustreLexer(new ANTLRFileStream(lusfileName));
		//Pass it to the token stream
		CommonTokenStream tokens = new CommonTokenStream(lex);
		//Pass the token stream to the parser
		LustreParser parser = new LustreParser(tokens);

		//Get parse tree
		ParseTree tree = parser.lustre();
		
		EPTGenListener eptGen = new EPTGenListener(this);
		EPTNode.COUNTER = 0;
		ParseTreeWalker.DEFAULT.walk(eptGen,tree);
		this.size = EPTNode.COUNTER;
		EPTNode.COUNTER = 0;
		nodelist = new EPTNode[this.size];
		
		boolean[] visited = new boolean[this.size];
		Arrays.fill(visited, false);
		treeSize = traversal(root, visited);
		
		Iterator<String> it = inputNodes.keySet().iterator();
		
		while(it.hasNext()) {
			String varname = it.next();
			if( nodelist[ inputNodes.get(varname).nodeid ] == null ) {
				ErrorLog.myErrorLog("Warning: Unused input - " + varname);
			}
		}
		
		it = variables.keySet().iterator();
		
		while(it.hasNext()) {
			String varname = it.next();
			if( nodelist[ variables.get(varname).nodeid ] == null ) {
				if( variables.get(varname).nodetype == LustreNodeType.OUTVARIABLE ) 
					ErrorLog.myErrorLog("ELOG: This code should not be reached - " + varname);
				//else 
				//	ErrorLog.myErrorLog("Warning: Unused variable - " + varname);
			}
		}
		
		it = constNodes.keySet().iterator();
		
		while(it.hasNext()) {
			String varname = it.next();
			if( nodelist[ constNodes.get(varname).nodeid ] == null ) {
				ErrorLog.myErrorLog("Warning: Unused Constant variable - " + varname);
			}
		}
	}
	
	public void registerVariables(VariableNode varNode) {
		if( varNode == null || variables.containsKey(varNode.name) ) ErrorLog.myErrorLog("ELOG: [varNode == null || variables.containsKey(varNode.name) == true]");
		else variables.put(varNode.name, varNode);
		variableNodeIds.add(varNode.nodeid);
	}
	
	public void registerInput(InputNode inputNode) {
		if( inputNode == null || inputNodes.containsKey(inputNode.name) ) ErrorLog.myErrorLog("ELOG: [inputNode == null || inputNodes.containsKey(inputNode) == true]");
		else inputNodes.put(inputNode.name, inputNode);
		inputNodeIds.add(inputNode.nodeid);
	}
	
	public void registerConst(ConstNode constNode) {
		if( constNode == null || constNodes.containsKey(constNode.name) ) ErrorLog.myErrorLog("ELOG: [constNode == null || constNodes.containsKey(constNode.name)]");
		else constNodes.put(constNode.name, constNode);
		constNodeIds.add(constNode.nodeid);
	}
	
	public String getLustreFileName() {
		return lusfileName;
	}
	
	public EPTNode getIdentifier(String name) {
		if( inputNodes.containsKey(name) ) return inputNodes.get(name);
		if( variables.containsKey(name) ) return variables.get(name);
		if( constNodes.containsKey(name) ) return constNodes.get(name);
		ErrorLog.myErrorLog("ELOG: unknown identifier: "+ name);
		return null;
	}
	
	public EPTNode getNode(int id) {
		if( id < 0 || id >= nodelist.length ) return null;
		return nodelist[id];
	}
	
	private int traversal(EPTNode root, boolean[] visited) {
		if(root != null) {
			nodelist[root.nodeid] = root;
			visited[root.nodeid] = true;
			root.connected = true;
			root.SetErrorPropagationTree(this);
			Iterator<EPTNode> children = root.getChildren();
			
			int subTreeSize = 0;
			while(children.hasNext()) {
				EPTNode child = children.next();
				if(!visited[child.nodeid]) {
					subTreeSize += traversal(child, visited);
				}
			}
			return subTreeSize + 1;
		}
		return 0;
	}
	
	public EPTNode getRoot() {
		return root;
	}
	
	public void ErrorSimulation(int errorNodeId, HashMap<String, Value[]> testcase, int stepsize) {
		simulate(testcase, stepsize);
		
		for(int i = 0; i < size; i++) {
			if(nodelist[i] != null) {
				nodelist[i].eppSimulationInit(stepsize);	
			}
		}
		
		for(int step = 0; step < stepsize; step++) { //Error Soruce¿¡ Error ÁÖÀÔ
			nodelist[ errorNodeId ].eppHistory[step] = 1;
			nodelist[ errorNodeId ].eppTimestamp = stepsize;
		}
		
		for(int step = 0; step < stepsize; step++) {
			for(int i = 0; i < nodelist.length; i++) {
				if( nodelist[ i ] != null )
					nodelist[ i ].eppGet(step);
			}
		}
	}
	
	
	public void setInputVariable(HashMap<String, Value[]> testcase, int stepsize) {
		Iterator<InputNode> inputIter = inputNodes.values().iterator();
		
		while(inputIter.hasNext()) {
			InputNode inputnode = inputIter.next();
			Value[] values = testcase.get(inputnode.name);
			if(inputnode.history == null || inputnode.history.length < stepsize) {
				inputnode.history = new Value[stepsize];
				for(int i = 0; i < stepsize; i++) 
					inputnode.history[i] = new Value(inputnode.valtype, values[i].value);
			}else {
				for(int i = 0; i < stepsize; i++)
					inputnode.history[i].value = values[i].value;
			}
			inputnode.timestamp = stepsize - 1;
		}
	}
	
	public SimulationLog simulate(HashMap<String, Value[]> testcase, int stepsize) {
		if(testcase == null) {
			ErrorLog.myErrorLog("ELOG: null testcase @simulate");
			return null;
		}
		
		SimulationErrorCode = NO_ERROR;
		SimulationErrorMessage = "";
		for(int i = 0; i < size; i++) {
			if(nodelist[i] != null) {
				nodelist[i].simulationInit(stepsize);	
			}
		}
		
		setInputVariable(testcase, stepsize);
		
		for(int step = 0; step < stepsize; step++) {
			for(int i = 0; i < nodelist.length; i++) {
				if( nodelist[ i ] != null )
					nodelist[ i ].get(step);
			}
		}
		
		return null;
	}
}

