import java.util.ArrayList;
import java.util.Iterator;

enum LustreValueType{
	BOOL, INT, REAL, SUBRANGE, INVALID
}

enum LustreNodeType{
	TOP, INPUT, CONSTANT, LITERAL, VARIABLE, OUTVARIABLE, ASSIGN , UNARYMINUS , NOT , PRE , AND , NAND , OR , NOR , XOR , NXOR , EQUAL , NOTEQUAL , LT , GT , LTE , GTE, PLUS , MINUS , MUL , DIV , ARROW , IF , CAST , INTDIV
}

class Value{
	LustreValueType type;
	String value;
	
	Value(LustreValueType type, String value){
		this.type = type;
		this.value = value;
	}
	
	Value(){
		this(LustreValueType.INVALID, "");
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !(obj instanceof Value) ) return false;
		Value v = (Value) obj;
		if(v.type != this.type) return false;
		switch(type) {
		case INT: return Integer.valueOf(v.value) == Integer.valueOf(this.value);
		case REAL: return Double.valueOf(v.value) == Double.valueOf(this.value);
		case BOOL: return Boolean.valueOf(v.value) == Boolean.valueOf(this.value);
		default: 
			ErrorLog.myErrorLog("ELOG: Unsupported Value Type @Value.equals");
			return false;
		}
	}

	public String toString() {
		String typestr = null;
		switch(type) {
		case INT: typestr = "int"; break;
		case REAL: typestr = "real"; break;
		case BOOL: typestr = "bool"; break;
		}
		return "("+typestr+","+value+")";
	}
}

public abstract class EPTNode{
	static int COUNTER = 0;
	
	//EPT 생성과정에서 값이 고정되는 필드들
	int nodeid;
	LustreNodeType nodetype;
	ArrayList<EPTNode> parent;
	boolean mutable;
	LustreValueType valtype;
	boolean connected; // output node 중 하나와 연결되는지 여부
	ErrorPropagationTree ept; //노드가 속한 트리
	
	//Simulation 동안에 동적으로 값이 변하는 필드들 
	boolean[] errorProp; // 각 step에서 발생한 error가 output node까지 전파될 수 있는지 기록
	Value[] history;//ArrayList<Value> history;
	double[] eppHistory;
	
	int eppTimestamp; // epp를 계산하기 위한 timestamp;
	
	int timestamp;//check step;
	boolean epAnalysisResult; // 테스트 suite 내에서 한번이라도 error를 propagation 시킬 수 있다면 true, 그렇지 않으면 false
	
	EPTNode(LustreNodeType nodetype){
		this.nodetype = nodetype;
		parent = new ArrayList<EPTNode>();
		history = null;
		eppHistory = null;
		errorProp = null;
		ept = null;
		mutable = true;
		nodeid = COUNTER++;
	}
	
	EPTNode(EPTNode copyNode){ //Semi-Copy Constructor
		this(copyNode.nodetype);
		this.nodeid = copyNode.nodeid;
		this.valtype = copyNode.valtype;
		this.mutable = copyNode.mutable;
		this.connected = copyNode.connected;
	}
	
	public EPTNode getParent(int index) {
		return parent.get(index);
	}
	
	public Iterator<EPTNode> getParent() {
		return parent.iterator();
	}
	
	public Iterator<EPTNode> getChildren() {
		return new Iterator<EPTNode>() {
			public boolean hasNext() { return false; }
			public EPTNode next() { return null; }	
		};
	}
	
	void simulationInit(int stepsize) { //각 testcase simulation 수행전 1번만 수행하면 됨
		if(mutable) { //mutable 하지 않은 것은 history 필요 없음
			timestamp = -1;
			if(history == null || history.length < stepsize) { 
				history = new Value[stepsize];
				for(int i = 0; i < stepsize; i++) {
					history[i] = new Value(this.valtype, "");
				}
			}else {
				for(int i = 0; i < stepsize; i++) {
					history[i].value = "";
				}
			}
		}
	}
	
	public Value get(int step) { // immutable node (const, liter)은 override 해야 함
		while(timestamp < step) {
			timestamp++;
			history[timestamp].value = evaluate(timestamp);	
		}
		return history[step];
	}
	
	abstract String evaluate(int step);
	abstract public EPTNode semiClone();
	// must be overridden by subclass
	// add child node to the last position
	abstract void addChild(EPTNode child);
	
	
	void eppSimulationInit(int stepsize) {
		eppTimestamp = -1;
		if(eppHistory == null || eppHistory.length < stepsize) { 
			eppHistory = new double[stepsize];
		}
	}
	
	public double eppGet(int step) {
		while(eppTimestamp < step) {
			eppTimestamp++;
			eppHistory[eppTimestamp] = eppEvaluate(eppTimestamp);	
		}
		return eppHistory[step];
	}
	
	abstract double eppEvaluate(int step);
	
	
	
	public boolean assertNotValueType(LustreValueType valuetype) {
		if( this.valtype == valuetype ) {
			ErrorLog.myErrorLog("Error: @assertValueType");
		}
		return this.valtype != valuetype;
	}
	
	public boolean assertNodeType(LustreNodeType nodetype) {
		if( this.nodetype != nodetype ) {
			ErrorLog.myErrorLog("Error: @assertNodeType");
		}
		return this.nodetype == nodetype;
	}
	
	public boolean assertValueType(LustreValueType valuetype) {
		if( this.valtype != valuetype ) {
			ErrorLog.myErrorLog("Error: @assertValueType");
		}
		return this.valtype == valuetype;
	}
	
	public String toString() {
		switch(nodetype) {
		case TOP: return "TOP";
		//case INPUT: @Override
		//case CONSTANT: @Override
		//case LITERAL: @Override
		//case VARIABLE: @Override
		case ASSIGN: return "=";
		case UNARYMINUS: return "UNARYMINUS";
		case NOT: return "NOT";
		case PRE: return "PRE";
		case AND: return "AND";
		case NAND: return "NAND";
		case OR: return "OR";
		case NOR: return "NOR";
		case XOR: return "XOR";
		case NXOR: return "NXOR";
		case EQUAL: return "==";
		case NOTEQUAL: return "!=";
		case LT: return "<";
		case GT: return ">";
		case LTE: return "<=";
		case GTE: return ">=";
		case PLUS: return "+";
		case MINUS: return "-";
		case MUL: return "*";
		case DIV: return "/";
		case ARROW: return "->";
		case IF: return "IF";
		case INTDIV: return "INTDIV";
		}
		return "";
	}
	
	void SetErrorPropagationTree(ErrorPropagationTree ept) {
		this.ept = ept;
	}
	
	public boolean getConnected() {
		return connected;
	}
	
	public int getNodeid() {
		return nodeid;
	}
	
	public boolean getMutable() {
		return mutable;
	}
	
	public String getLabel() {
		switch(nodetype) {
		case TOP: return "TOP";
		case INPUT: return "<IN>";
		case CONSTANT: return "<CON>";
		case LITERAL: 
			if(valtype == LustreValueType.INT) return "int";
			if(valtype == LustreValueType.REAL) return "real";
			if(valtype == LustreValueType.BOOL) return "bool";
			return "lit";
		case VARIABLE: return "<VAR>";
		case ASSIGN: return "=";
		case UNARYMINUS: return "-";
		case NOT: return "NOT";
		case PRE: return "PRE";
		case AND: return "AND";
		case NAND: return "NAND";
		case OR: return "OR";
		case NOR: return "NOR";
		case XOR: return "XOR";
		case NXOR: return "NXOR";
		case EQUAL: return "==";
		case NOTEQUAL: return "!=";
		case LT: return "<";
		case GT: return ">";
		case LTE: return "<=";
		case GTE: return ">=";
		case PLUS: return "+";
		case MINUS: return "-";
		case MUL: return "*";
		case DIV: return "/";
		case INTDIV: return "DIV";
		case ARROW: return "\u2192";
		case IF: return "IF";
		case CAST: return "CAST";
		case OUTVARIABLE: return "<OUT>";
		}
		return "";
	}
	
	public String getNodeTypeStr() {
		switch(nodetype) {
		case TOP: return "TOP";
		case INPUT: return "Input";
		case CONSTANT: return "Constant Var";
		case LITERAL: return "Literal";
		case VARIABLE: return "Variable";
		case ASSIGN: return "Assignment";
		case UNARYMINUS: return "U-minus";
		case NOT: return "Not";
		case PRE: return "Pre";
		case AND: return "And";
		case NAND: return "Nand";
		case OR: return "Or";
		case NOR: return "Nor";
		case XOR: return "Xor";
		case NXOR: return "NXor";
		case EQUAL: return "Equality";
		case NOTEQUAL: return "Not Euality";
		case LT: return "Lt";
		case GT: return "Gt";
		case LTE: return "Lte";
		case GTE: return "Gte";
		case PLUS: return "Addition";
		case MINUS: return "Subtraction";
		case MUL: return "Multiplication";
		case DIV: return "Division";
		case INTDIV: return "Int Division";
		case ARROW: return "Arrow";
		case IF: return "If";
		case CAST: return "Cast";
		case OUTVARIABLE: return "Output";
		}
		return "";
	}
	
	public String getName() {
		System.out.println(nodetype);
		switch(nodetype) {
		case TOP: return ((TopNode)this).name;
		case OUTVARIABLE:
		case VARIABLE: 
			System.out.println(((VariableNode) this).name);
			return ((VariableNode) this).name;
		case INPUT: return ((InputNode) this).name;
		case CONSTANT: return ((ConstNode) this).name;
		default: return "";
		}
	}
	
	public String getValuTypeStr() {
		if(valtype == null) return "";
		switch(valtype) {
		case BOOL: return "Boolean";
		case INT: return "Int";
		case REAL: return "Real";
		default: return "";
		}
	}
	
	public String[] getHistoryStr() {
		if(history == null) return null;
		String[] historyStr = new String[history.length];
		int index = 0;
		for(Value v : history) {
			historyStr[index++] = v.value;
		}
		return historyStr;
	}
}


class TopNode extends EPTNode {
	String name;
	ArrayList<EPTNode> children;
	
	TopNode(String name){
		super(LustreNodeType.TOP);
		children = new ArrayList<>();
		this.name = name;
	}
	
	TopNode(TopNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.name = copyNode.name;
		children = new ArrayList<>();
	}
	
	public TopNode semiClone() { // must be overridden by subclass
		return new TopNode(this);
	}
	
	public TopNode clone() { // must be overridden by subclass
		TopNode c = new TopNode(this.name);
		return null;
	}

	@Override
	String evaluate(int step) {
		boolean success = true;
		for(int i = 0; i < children.size(); i++) {
			if( children.get(i).get(step).value.equals("") ) success = false;
		}
		return success ? "true" : "false";
	}	
	
	void addChild(EPTNode node) {
		children.add(node);
		node.parent.add(this);
	}
	
	public Iterator<EPTNode> getChildren() {
		return children.iterator();
	}

	@Override
	double eppEvaluate(int step) {
		Iterator<EPTNode> it = getChildren();
		double noneErrorProb = 1;
		while(it.hasNext()) {
			VariableNode outputVar = (VariableNode)it.next();
			noneErrorProb *= 1 - outputVar.eppGet(step);
		}
		return 1 - noneErrorProb;
	}
}

class InputNode extends EPTNode{
	String name;
	
	InputNode(String name, LustreValueType valtype) {
		super(LustreNodeType.INPUT);
		this.name = name;
		this.valtype = valtype;
	}
	
	InputNode(InputNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.name = copyNode.name;
	}
	
	public InputNode semiClone() { // must be overridden by subclass
		return new InputNode(this);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return name.hashCode();
	}
	
	public String toString() {
		String typeStr = null;
		if(valtype == LustreValueType.BOOL) typeStr = "bool";
		else if(valtype == LustreValueType.INT ) typeStr = "int";
		else if(valtype == LustreValueType.REAL ) typeStr = "real";
		else if(valtype == LustreValueType.SUBRANGE ) typeStr = "subrange";
		return "input:('"+ name + "'," + typeStr +")";
	}

	@Override
	void addChild(EPTNode child) {}

	@Override
	String evaluate(int step) {
		return null;
	}

	@Override
	double eppEvaluate(int step) {
		return 0;
	}
}

class LiteralNode extends EPTNode{
	LiteralNode(String value, LustreValueType valtype){
		super(LustreNodeType.LITERAL);
		this.mutable = false; //Literal은 mutable하지 않음 
		this.timestamp = Integer.MAX_VALUE; 
		this.history = new Value[] {new Value(valtype, value)};
		this.valtype = valtype;
	}
	
	LiteralNode(LiteralNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.timestamp = copyNode.timestamp;
		this.history = new Value[] {new Value(copyNode.valtype, copyNode.history[0].value)};
		this.valtype = copyNode.valtype;
	}
	
	public LiteralNode semiClone() { // must be overridden by subclass
		return new LiteralNode(this);
	}
	
	@Override
	public Value get(int step) {
		return history[0];
	}
	
	@Override
	String evaluate(int step) {
		return null;
	}

	public String toString() {
		String typeStr = null;
		if(valtype == LustreValueType.BOOL) typeStr = "bool";
		else if(valtype == LustreValueType.INT ) typeStr = "int";
		else if(valtype == LustreValueType.REAL ) typeStr = "real";
		else if(valtype == LustreValueType.SUBRANGE ) typeStr = "subrange";
		return "("+history[0].value+","+typeStr+")";
	}

	@Override
	void addChild(EPTNode child) {}



	@Override
	double eppEvaluate(int step) {
		return 0;
	}
}

abstract class UnaryNode extends EPTNode{
	UnaryNode(LustreNodeType nodetype){
		super(nodetype);
	}
	
	UnaryNode(LustreNodeType nodetype, EPTNode child){
		super(nodetype);
		addChild(child);
	}
	
	EPTNode child;
	
	UnaryNode(UnaryNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public void addChild(EPTNode child) {
		clearChild();
		this.child = child;
		child.parent.add(this);
	}
	
	public void clearChild() {
		if(this.child != null) {
			child.parent.remove(this);
			this.child = null;
		}
	}
	
	public Iterator<EPTNode> getChildren() {
		return new Iterator<EPTNode>() {
			int returned = 0;
			@Override
			public boolean hasNext() {
				return returned == 0;
			}
			@Override
			public EPTNode next() {
				returned++;
				if( returned == 1) return child;
				else return null;
			}
		};
	}
}

class VariableNode extends UnaryNode{
	String name; // variable name
	
	VariableNode(String name, LustreValueType valtype) {
		super(LustreNodeType.VARIABLE);
		this.name = name;
		this.valtype = valtype;
	}
	
	VariableNode(String name, LustreValueType valtype, boolean isOutVariable) {
		super(isOutVariable ? LustreNodeType.OUTVARIABLE : LustreNodeType.VARIABLE);
		this.name = name;
		this.valtype = valtype;
	}
	
	VariableNode(VariableNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.name = copyNode.name;
	}
	
	public VariableNode semiClone() { // must be overridden by subclass
		return new VariableNode(this);
	}
	
	void ASSIGN(EPTNode node) {
		if( child != null ) { ErrorLog.myErrorLog("ELOG: duplicated assignment"); }
		else if( (node instanceof AssignNode) == false ) { ErrorLog.myErrorLog("ELOG: wrong assignment"); }
		else {
			assertValueType(node.valtype); 
			addChild(node);
		}
	}
	
	
	@Override
	String evaluate(int step) {
		return child.get(step).value;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return name.hashCode();
	}
	
	public String toString() {
		String typeStr = null;
		if(valtype == LustreValueType.BOOL) typeStr = "bool";
		else if(valtype == LustreValueType.INT ) typeStr = "int";
		else if(valtype == LustreValueType.REAL ) typeStr = "real";
		else if(valtype == LustreValueType.SUBRANGE ) typeStr = "subrange";
		return "var:('"+ name + "'," + typeStr +")";
	}

	@Override
	double eppEvaluate(int step) {
		return child.eppGet(step);
	}	
}

class ConstNode extends UnaryNode{
	String name;
	
	ConstNode(String name, EPTNode child, LustreValueType valtype) {
		super(LustreNodeType.CONSTANT);
		this.mutable = false; //constant는 mutable하지 않음
		this.timestamp = Integer.MAX_VALUE;
		this.name = name;
		this.valtype = valtype;
		this.child = child;
		
		history = new Value[1];
		history[0] = child.get(0);
		assertValueType(child.valtype);
	}
	
	ConstNode(ConstNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.timestamp = Integer.MAX_VALUE;
		this.name = copyNode.name;
		this.valtype = copyNode.valtype;
		
		history = new Value[1];
		history[0] = new Value(copyNode.valtype, copyNode.history[0].value);
	}
	
	public ConstNode semiClone() { // must be overridden by subclass
		return new ConstNode(this);
	}
	
	public Value get(int step) { // immutalbe node (const, liter)은 override 해야 함
		return history[0];
	}
	
	public String toString() {
		String typeStr = null;
		if(valtype == LustreValueType.BOOL) typeStr = "bool";
		else if(valtype == LustreValueType.INT ) typeStr = "int";
		else if(valtype == LustreValueType.REAL ) typeStr = "real";
		else if(valtype == LustreValueType.SUBRANGE ) typeStr = "subrange";
		return "const:('"+ name + "'," + typeStr +")";
	}

	@Override
	String evaluate(int step) {
		return history[0].value;
	}

	@Override
	double eppEvaluate(int step) {
		return child.eppGet(step);
	}
}

class CastNode extends UnaryNode{
	LustreValueType castTo;
	
	CastNode(EPTNode child, LustreValueType castTo) {
		super(LustreNodeType.CAST);
		this.mutable = true;
		
		this.castTo = castTo;
		this.child = child;
		
		this.valtype = castTo;
		child.assertNotValueType(LustreValueType.BOOL);
	}
	
	CastNode(CastNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
		this.castTo = copyNode.castTo;
	}
	
	public CastNode semiClone() { // must be overridden by subclass
		return new CastNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String childValue = child.get(step).value;
		if( childValue.equals("") ) return "";
		if( castTo == child.valtype ) return childValue; 
		
		switch(castTo) {
		case INT:
			double dv = Double.valueOf(childValue);
			return String.valueOf( (int) dv  );
		case REAL:
			int iv = Integer.valueOf(childValue);
			return String.valueOf( (double) iv  );
		}
		return null;
	}
	
	
	
	public String toString() {
		String typeStr = null;
		if(castTo == LustreValueType.INT ) typeStr = "int";
		else if(castTo == LustreValueType.REAL ) typeStr = "real";
		return "cast:('"+ typeStr +")";
	}

	@Override
	double eppEvaluate(int step) {
		return child.eppGet(step);
	}
}

class AssignNode extends UnaryNode{
	
	AssignNode(EPTNode child) {
		super(LustreNodeType.ASSIGN, child);
		this.valtype = child.valtype;
	}
	
	AssignNode(AssignNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public AssignNode semiClone() { // must be overridden by subclass
		return new AssignNode(this);
	}
	
	@Override
	String evaluate(int step) {
		return child.get(step).value;
	}

	@Override
	double eppEvaluate(int step) {
		return child.eppGet(step);
	}
}

class UnaryMinusNode extends UnaryNode{

	UnaryMinusNode(EPTNode child) {
		super(LustreNodeType.UNARYMINUS ,child);
		child.assertNotValueType(LustreValueType.BOOL);
		this.valtype = child.valtype;
	}
	
	UnaryMinusNode(UnaryMinusNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public UnaryMinusNode semiClone() { // must be overridden by subclass
		return new UnaryMinusNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String childValue = child.get(step).value;
		if( childValue.equals("") ) return "";
		if(valtype == LustreValueType.INT) {
			int val = Integer.valueOf(childValue);
			return String.valueOf( -val );
		}else if(valtype == LustreValueType.REAL){
			double val = Double.valueOf(childValue);
			return String.valueOf( -val );
		}else {
			ErrorLog.myErrorLog("ELOG: unrechable caes @evaluate");
		}
		return null;
	}

	@Override
	double eppEvaluate(int step) {
		// TODO Auto-generated method stub
		return child.eppGet(step);
	}
}

class NotNode extends UnaryNode{

	NotNode(EPTNode child) {
		super(LustreNodeType.NOT ,child);
		child.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	NotNode(NotNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public NotNode semiClone() { // must be overridden by subclass
		return new NotNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String childValue = child.get(step).value;
		if( childValue.equals("") ) return "";
		return String.valueOf(!Boolean.valueOf(childValue));
	}

	@Override
	double eppEvaluate(int step) {
		return child.eppEvaluate(step);
	}
}

class PreNode extends UnaryNode{

	PreNode(EPTNode child) {
		super(LustreNodeType.PRE ,child);
		this.valtype = child.valtype;
	}
	
	PreNode(PreNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public PreNode semiClone() {
		return new PreNode(this);
	}
	
	@Override
	String evaluate(int step) {
		return step <= 0 ? "" : child.get(step - 1).value; 
	}

	@Override
	double eppEvaluate(int step) {
		return step <= 0 ? 0 : child.eppGet(step - 1);
	}
	
}

abstract class BinaryNode extends EPTNode{
	EPTNode lChild;
	EPTNode rChild;
	
	BinaryNode(LustreNodeType nodetype, EPTNode lChild, EPTNode rChild){
		super(nodetype);
		addChildren(lChild, rChild);
	}
	
	BinaryNode(BinaryNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public void clearChildren() {
		if(this.lChild != null) {
			lChild.parent.remove(this);
			this.lChild = null;
		}
		if(this.rChild != null) {
			rChild.parent.remove(this);
			this.rChild = null;
		}
	}
	
	void addChild(EPTNode child) {
		if(this.lChild == null) {
			this.lChild = child;
			lChild.parent.add(this);
		}else if(this.rChild == null) {
			this.rChild = child;
			rChild.parent.add(this);
		}
	}
	
	void addChildren(EPTNode lChild, EPTNode rChild) {
		clearChildren();
		this.lChild = lChild;
		this.rChild = rChild;
		
		lChild.parent.add(this);
		rChild.parent.add(this);
	}
	
	public Iterator<EPTNode> getChildren() {
		return new Iterator<EPTNode>() {
			int returned = 0;
			@Override
			public boolean hasNext() {
				return returned <= 1;
			}
			@Override
			public EPTNode next() {
				returned++;
				switch(returned) {
				case 1: return lChild;
				case 2: return rChild;
				default: return null;
				}
			}
		};
	}
}

class AndNode extends BinaryNode{
	AndNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.AND, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	AndNode(AndNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public AndNode semiClone() {
		return new AndNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(Boolean.valueOf(lChildValue) && Boolean.valueOf(rChildValue)); 
	}
	
	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double trueProbLC = Boolean.valueOf(lChildValue) == true ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // trueProbLC : 왼쪽 자식이 true 값을 가질 확률
		double trueProbRC = Boolean.valueOf(rChildValue) == true ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // trueProbRC : 오른쪽 자식이 true 값을 가질 확률
		
		double trueProb = trueProbLC * trueProbRC; // And노드가 true 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return trueProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return 1 - trueProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class NandNode extends BinaryNode{
	NandNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.NAND, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	NandNode(NandNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public NandNode semiClone() {
		return new NandNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(!(Boolean.valueOf(lChildValue) && Boolean.valueOf(rChildValue))); 
	}

	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double trueProbLC = Boolean.valueOf(lChildValue) == true ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // trueProbLC : 왼쪽 자식이 true 값을 가질 확률
		double trueProbRC = Boolean.valueOf(rChildValue) == true ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // trueProbRC : 오른쪽 자식이 true 값을 가질 확률
		
		double falseProb = trueProbLC * trueProbRC; // NAnd노드가 false 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return 1 - falseProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return falseProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class OrNode extends BinaryNode{
	OrNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.OR, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	OrNode(OrNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public OrNode semiClone() {
		return new OrNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(Boolean.valueOf(lChildValue) || Boolean.valueOf(rChildValue)); 
	}

	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double falseProbLC = Boolean.valueOf(lChildValue) == false ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
		double falseProbRC = Boolean.valueOf(rChildValue) == false ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
		
		double falseProb = falseProbLC * falseProbRC; // Or노드가 false 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return 1 - falseProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return falseProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class NorNode extends BinaryNode{
	NorNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.NOR, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	NorNode(NorNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public NorNode semiClone() {
		return new NorNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(!(Boolean.valueOf(lChildValue) || Boolean.valueOf(rChildValue))); 
	}

	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double falseProbLC = Boolean.valueOf(lChildValue) == false ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
		double falseProbRC = Boolean.valueOf(rChildValue) == false ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
		
		double trueProb = falseProbLC * falseProbRC; // NOr노드가 true 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return trueProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return 1 - trueProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class XorNode extends BinaryNode{
	XorNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.XOR, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	XorNode(XorNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public XorNode semiClone() {
		return new XorNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(Boolean.valueOf(lChildValue) != Boolean.valueOf(rChildValue)); 
	}

	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double falseProbLC = Boolean.valueOf(lChildValue) == false ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
		double falseProbRC = Boolean.valueOf(rChildValue) == false ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
		
		double trueProb = falseProbLC * (1 - falseProbRC) + (1 - falseProbLC) * falseProbRC; // Xor노드가 true 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return trueProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return 1 - trueProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class NxorNode extends BinaryNode{
	NxorNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.NXOR, lChild, rChild);
		lChild.assertValueType(LustreValueType.BOOL);
		rChild.assertValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	NxorNode(NxorNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public NxorNode semiClone() {
		return new NxorNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		return String.valueOf(Boolean.valueOf(lChildValue) == Boolean.valueOf(rChildValue)); 
	}

	@Override
	double eppEvaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		
		double falseProbLC = Boolean.valueOf(lChildValue) == false ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
		double falseProbRC = Boolean.valueOf(rChildValue) == false ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
		
		double falseProb = falseProbLC * (1 - falseProbRC) + (1 - falseProbLC) * falseProbRC; // Nxor노드가 false 값을 가질 확률 
		
		if( Boolean.valueOf( get(step).value ) == false ) return 1 - falseProb; // 본래 값이 false일 경우, true 값을 가질 확률이 Error 확률
		else return falseProb; // 본래 값이 true일 경우, false 값을 가질 확률이 Error 확률
	}
}

class EqNode extends BinaryNode{
	EqNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.EQUAL, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		this.valtype = LustreValueType.BOOL;
	}
	
	EqNode(EqNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public EqNode semiClone() {
		return new EqNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) == Integer.valueOf(rChildValue));
		case BOOL:
			return String.valueOf(Boolean.valueOf(lChildValue) == Boolean.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) == Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: EqNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		if( lChild.valtype == LustreValueType.BOOL ) { // Boolean Case
			String lChildValue = lChild.get(step).value;
			String rChildValue = rChild.get(step).value;
			
			double trueProbLC = Boolean.valueOf(lChildValue) == true ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
			double trueProbRC = Boolean.valueOf(rChildValue) == true ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
			
			double trueProb = trueProbLC * trueProbRC + (1 - trueProbRC) * (1 - trueProbLC);
			
			if( Boolean.valueOf( get(step).value) == false ) return trueProb;
			else return 1 - trueProb;	
		}else { // Numeric Case
			if( Boolean.valueOf( get(step).value) == false ) return 0;
			else return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));			
		}
	}
}

class NeqNode extends BinaryNode{
	NeqNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.NOTEQUAL, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		this.valtype = LustreValueType.BOOL;
	}
	
	NeqNode(NeqNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public NeqNode semiClone() {
		return new NeqNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) != Integer.valueOf(rChildValue));
		case BOOL:
			return String.valueOf(Boolean.valueOf(lChildValue) != Boolean.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) != Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: NEqNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		if( lChild.valtype == LustreValueType.BOOL ) { // Boolean Case
			String lChildValue = lChild.get(step).value;
			String rChildValue = rChild.get(step).value;
			
			double trueProbLC = Boolean.valueOf(lChildValue) == true ? 1 - lChild.eppGet(step) : lChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
			double trueProbRC = Boolean.valueOf(rChildValue) == true ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
			
			double trueProb = (1 - trueProbLC) * trueProbRC + trueProbLC * (1 - trueProbRC); // Xor노드가 true 값을 가질 확률 
			if( Boolean.valueOf( get(step).value ) == false ) return trueProb;
			else return 1 - trueProb;	
		}else { // Numeric Case
			if( Boolean.valueOf( get(step).value) == false ) return  1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
			else return 0;			
		}
	}
}

class LtNode extends BinaryNode{
	LtNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.LT, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	LtNode(LtNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public LtNode semiClone() {
		return new LtNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) < Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) < Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: LtNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return (1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step))) * 0.5;
	}
}

class GtNode extends BinaryNode{
	GtNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.GT, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	GtNode(GtNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public GtNode semiClone() {
		return new GtNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) > Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) > Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: GtNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return (1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step))) * 0.5;
	}
}

class LteNode extends BinaryNode{
	LteNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.LTE, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	LteNode(LteNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public LteNode semiClone() {
		return new LteNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) <= Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) <= Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: LteNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return (1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step))) * 0.5;
	}
}

class GteNode extends BinaryNode{
	GteNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.GTE, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = LustreValueType.BOOL;
	}
	
	GteNode(GteNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public GteNode semiClone() {
		return new GteNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) >= Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) >= Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: GteNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return (1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step))) * 0.5;
	}
}

class PlusNode extends BinaryNode{
	PlusNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.PLUS, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = lChild.valtype;
	}
	
	PlusNode(PlusNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public PlusNode semiClone() {
		return new PlusNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) + Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) + Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: PlusNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
	}
}

class MinusNode extends BinaryNode{
	MinusNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.MINUS, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = lChild.valtype;
	}
	
	MinusNode(MinusNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public MinusNode semiClone() {
		return new MinusNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) - Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) - Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: MinusNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
	}
}

class MulNode extends BinaryNode{
	MulNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.MUL, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		lChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = lChild.valtype;
	}
	
	MulNode(MulNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public MulNode semiClone() {
		return new MulNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		switch(lChild.valtype) {
		case INT:
			return String.valueOf(Integer.valueOf(lChildValue) * Integer.valueOf(rChildValue));
		case REAL:
			return String.valueOf(Double.valueOf(lChildValue) * Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: MulNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
	}
}

class DivNode extends BinaryNode{
	DivNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.DIV, lChild, rChild);
		lChild.assertNotValueType(LustreValueType.BOOL);
		rChild.assertNotValueType(LustreValueType.BOOL);
		this.valtype = lChild.valtype;
	}
	
	DivNode(DivNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public DivNode semiClone() {
		return new DivNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		
		switch(lChild.valtype) {
		case INT:
			if(Integer.valueOf(rChildValue) == 0) {
				ept.SimulationErrorCode = ErrorPropagationTree.DIVIDE_BY_ZERO_ERROR;
				ept.SimulationErrorMessage = "Divide by 0 (Integer) @"+nodeid;
				System.out.println("Divide by 0 (Integer) @"+nodeid);
				return "";
			}
			return String.valueOf(Integer.valueOf(lChildValue) / Integer.valueOf(rChildValue));
		case REAL:
			if(Double.valueOf(rChildValue) == 0) {
				ept.SimulationErrorCode = ErrorPropagationTree.DIVIDE_BY_ZERO_ERROR;
				ept.SimulationErrorMessage = "Divide by 0 (Real) @"+nodeid;
				System.out.println("Divide by 0 (Real) @"+nodeid);
				return "";
			}
			return String.valueOf(Double.valueOf(lChildValue) / Double.valueOf(rChildValue));
		default: ErrorLog.myErrorLog("ELOG: DivNode Evaluation");
			return null;
		}
	}

	@Override
	double eppEvaluate(int step) {
		return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
	}
}

class IntDivNode extends BinaryNode{
	IntDivNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.INTDIV, lChild, rChild);
		lChild.assertValueType(LustreValueType.INT);
		rChild.assertValueType(LustreValueType.INT);
		this.valtype = LustreValueType.INT;
	}
	
	IntDivNode(IntDivNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public IntDivNode semiClone() {
		return new IntDivNode(this);
	}
	
	@Override
	String evaluate(int step) {
		String lChildValue = lChild.get(step).value;
		String rChildValue = rChild.get(step).value;
		if( lChildValue.equals("") ) return "";
		if( rChildValue.equals("") ) return "";
		
		if(Integer.valueOf(rChildValue) == 0) {
			ept.SimulationErrorCode = ErrorPropagationTree.DIVIDE_BY_ZERO_ERROR;
			ept.SimulationErrorMessage = "Divide by 0 (Integer) @"+nodeid;
			System.out.println("Divide by 0 (Integer) @"+nodeid);
			return ""; // 시뮬레이션 종료
		}
		
		return String.valueOf(Integer.valueOf(lChildValue) / Integer.valueOf(rChildValue));
	}

	@Override
	double eppEvaluate(int step) {
		return 1 - (1 - lChild.eppGet(step)) * (1 - rChild.eppGet(step));
	}
}

class ArrowNode extends BinaryNode{ // lchild -> rchild
	ArrowNode(EPTNode lChild, EPTNode rChild){
		super(LustreNodeType.ARROW, lChild, rChild);
		lChild.assertValueType(rChild.valtype);
		this.valtype = lChild.valtype;
	}
	
	ArrowNode(ArrowNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public ArrowNode semiClone() {
		return new ArrowNode(this);
	}
	
	@Override
	String evaluate(int step) {
		if(step == 0) {
			return lChild.get(step).value;
		}
		if( step > 0){
			return rChild.get(step).value;
		}	
		return ""; //Error 상황
	}
	
	@Override
	double eppEvaluate(int step) {
		if(step == 0) {
			return lChild.eppGet(step);
		}else{
			return rChild.eppGet(step);
		}
	}
}

abstract class TernaryNode extends EPTNode{
	EPTNode lChild; 
	EPTNode mChild; 
	EPTNode rChild;
	
	TernaryNode(LustreNodeType nodetype, EPTNode lChild, EPTNode mChild, EPTNode rChild){
		super(nodetype);
		addChildren(lChild, mChild, rChild);
	}
	
	TernaryNode(TernaryNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public void clearChildren() {
		if(this.lChild != null) {
			lChild.parent.remove(this);
			this.lChild = null;
		}
		if(this.mChild != null) {
			mChild.parent.remove(this);
			this.mChild = null;
		}
		if(this.rChild != null) {
			rChild.parent.remove(this);
			this.rChild = null;
		}
	}
	
	void addChild(EPTNode child) {
		if(this.lChild == null) {
			this.lChild = child;
			lChild.parent.add(this);
		}else if(this.mChild == null){
			this.mChild = child;
			mChild.parent.add(this);
		}else if(this.rChild == null) {
			this.rChild = child;
			rChild.parent.add(this);
		}
	}
	
	void addChild(int index, EPTNode child) { // must be overridden by subclass
		if(index == 0) {
			if(this.lChild != null) {
				lChild.parent.remove(this);
				this.lChild = null;
				lChild.parent.add(this);
			}
			this.lChild = child;
		}else if(index == 1) {
			if(this.mChild != null) {
				mChild.parent.remove(this);
				this.mChild = null;
				mChild.parent.add(this);
			}
			this.mChild = child;
		}else if(index == 2) {
			if(this.rChild != null) {
				rChild.parent.remove(this);
				this.rChild = null;
				rChild.parent.add(this);
			}
			this.rChild = child;
		}
	}
	
	void addChildren( EPTNode lChild, EPTNode mChild, EPTNode rChild) {
		clearChildren();
		this.lChild = lChild;
		this.mChild = mChild;
		this.rChild = rChild;
		
		lChild.parent.add(this);
		mChild.parent.add(this);
		rChild.parent.add(this);
	}	
	
	public Iterator<EPTNode> getChildren() {
		return new Iterator<EPTNode>() {
			int returned = 0;
			@Override
			public boolean hasNext() {
				return returned <= 2;
			}
			@Override
			public EPTNode next() {
				returned++;
				switch(returned) {
				case 1: return lChild;
				case 2: return mChild;
				case 3: return rChild;
				default: return null;
				}
			}
		};
	}	
}

class IfNode extends TernaryNode{
	IfNode(EPTNode condNode, EPTNode trueNode, EPTNode falseNode){
		super(LustreNodeType.IF, condNode, trueNode, falseNode);
		condNode.assertValueType(LustreValueType.BOOL);
		trueNode.assertValueType(falseNode.valtype);
		this.valtype = trueNode.valtype;
	}
	
	IfNode(IfNode copyNode){ //Semi-Copy Constructor
		super(copyNode);
	}
	
	public IfNode semiClone() {
		return new IfNode(this);
	}
	
	@Override
	String evaluate(int step) {
		if( lChild.get(step).value.equals("") ) return "";
		boolean cond = Boolean.valueOf(lChild.get(step).value);
		return cond ? mChild.get(step).value : rChild.get(step).value; 
	}
	
	EPTNode getConditionNode() {
		return lChild;
	}
	
	EPTNode getTrueNode() {
		return mChild;
	}
	
	EPTNode getFalseNode() {
		return rChild;
	}

	@Override
	double eppEvaluate(int step) {
		double eppThen = -1; //Then 구문이 선택되었을 때의 에러 확률
		double eppElse = -1; //Else 구문이 선택되었을 때의 에러 확률
		
		String thenValue = mChild.get(step).value;
		String elseValue = rChild.get(step).value;
		
		if( lChild.valtype == LustreValueType.BOOL ) { // Boolean Case
			double trueProbThen = Boolean.valueOf(thenValue) == true ? 1 - mChild.eppGet(step) : mChild.eppGet(step); // falseProbLC : 왼쪽 자식이 false 값을 가질 확률
			double trueProbElse = Boolean.valueOf(elseValue) == true ? 1 - rChild.eppGet(step) : rChild.eppGet(step); // falseProbRC : 오른쪽 자식이 false 값을 가질 확률
			
			if( Boolean.valueOf( get(step).value) == false ) {
				eppThen = trueProbThen;
				eppElse = trueProbElse;
			}else {
				eppThen = 1 - trueProbThen;
				eppElse = 1 - trueProbElse;
			}
		}else { // Numeric Case
			if( lChild.valtype == LustreValueType.REAL ) {
				double oracleValue = Double.valueOf( get(step).value );
				if( oracleValue != Double.valueOf(thenValue) ) {
					eppThen = 1;
				}else {
					eppThen = mChild.eppGet(step);
				}
				if( oracleValue != Double.valueOf(elseValue) ) {
					eppElse = 1;
				}else {
					eppElse = rChild.eppGet(step);
				}
			}else if( lChild.valtype == LustreValueType.INT ) {
				int oracleValue = Integer.valueOf( get(step).value );
				if( oracleValue != Integer.valueOf(thenValue) ) {
					eppThen = 1;
				}else {
					eppThen = mChild.eppGet(step);
				}
				if( oracleValue != Integer.valueOf(elseValue) ) {
					eppElse = 1;
				}else {
					eppElse = rChild.eppGet(step);
				}
			}
		}
		
		String condValue = lChild.get(step).value;
		double elseProb; // Else 구문이 선택될 확률
		if( Boolean.valueOf(condValue) == true ) {
			elseProb = lChild.eppGet(step);
		}else {
			elseProb = 1 - lChild.eppGet(step);
		}
		
		return (1 - elseProb) * eppThen + (elseProb) * eppElse;
	}
}