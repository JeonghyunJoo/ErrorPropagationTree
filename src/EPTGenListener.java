
import java.util.HashMap;
import java.util.List;

import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class EPTGenListener extends LustreBaseListener{
	HashMap<ParserRuleContext, EPTNode> map;
	ErrorPropagationTree ept;
	
	public EPTGenListener(ErrorPropagationTree ept) {
		super();
		map = new HashMap<ParserRuleContext, EPTNode>();
		
		this.ept = ept;
	}
	
	private static LustreValueType TYPE(LustreParser.TypeContext ctx) {
		String typestr = ctx.getChild(0).getText();
		if( typestr.equals("bool") ) {
			return LustreValueType.BOOL;
		}else if ( typestr.equals("int") ) {
			return LustreValueType.INT;
		}else if ( typestr.equals("real") ) {
			return LustreValueType.REAL;
		}else if ( typestr.equals("subrange") ) {
			return LustreValueType.INT;//return LustreValueType.SUBRANGE;
		}
		ErrorLog.myErrorLog("ELOG: Unknown Type");
		return null;
	}
	
	public void bindEPTNode2CTX(ParserRuleContext ctx, EPTNode node) {
		if( map.containsKey(ctx) == true ) ErrorLog.myErrorLog("ELOG: map.containsKey(ctx) == true");
		else map.put(ctx, node);
	}
	
	public EPTNode extractEPTNodeFromCTX(ParserRuleContext ctx) {
		if( map.containsKey(ctx) == false ) {
			ErrorLog.myErrorLog("ELOG: map.containsKey(ctx) == false @getEPTNodeFromCTX");
		}
		return map.remove(ctx);
	}
	
	private void processNodeArgs(LustreParser.NodeArgsContext ctx, String argType) {
		if( ctx.shift_expression().size() != 0 ) ErrorLog.myErrorLog("ELOG:ctx.shift_expression().size() != 0");
		for(int i = 0; i < ctx.nodeArgsHelper().size(); i++) {
			String[] idList = ctx.nodeArgsHelper(i).getText().split(",");
			LustreValueType valtype = TYPE( ctx.type(i) );
			
			if( argType.equals("input") ) {
				for( String id : idList ) {
					ept.registerInput( new InputNode(id , valtype) );
				}
			}else if( argType.equals("variable") ) {
				for( String id : idList ) {
					ept.registerVariables(new VariableNode(id, valtype));
				}
			}else if( argType.equals("outvariable") ) {
				for( String id : idList ) {
					ept.registerVariables(new VariableNode(id, valtype, true));
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitLustre(LustreParser.LustreContext ctx) {
		if( ctx.compilationUnit().size() > 1 )
			ErrorLog.myErrorLog("ELOG: This program can not be applied for the lustre program with more than one node. Please retry on the preprocessed lustre file(maybe '.ec' file)");
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitCompilationUnit(LustreParser.CompilationUnitContext ctx) { 
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitConstDeclaration(LustreParser.ConstDeclarationContext ctx) {
		ept.registerConst( new ConstNode(ctx.IDENTIFIER().getText(), extractEPTNodeFromCTX(ctx.simple_expr_p25()), TYPE(ctx.type())) );  
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitNodeDeclaration(LustreParser.NodeDeclarationContext ctx) {
		ept.topnodeName = ctx.IDENTIFIER().getText();
		processNodeArgs(ctx.nodeArgs(0) , "input"); // input variable declarations
		processNodeArgs(ctx.nodeArgs(1) , "outvariable"); // output variable declarations
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitNodeVars(LustreParser.NodeVarsContext ctx) { 
		processNodeArgs(ctx.nodeArgs(), "variable");
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitNodeDefinition(LustreParser.NodeDefinitionContext ctx) { 
		int node_body_size = ctx.nodeBody().size();
		TopNode topnode = null;
		if( node_body_size > 0 ) topnode = new TopNode(ept.topnodeName);
		for(int i = 0; i < node_body_size; i++) {
			EPTNode node = extractEPTNodeFromCTX(ctx.nodeBody(i));
			if( node.nodetype == LustreNodeType.OUTVARIABLE) topnode.addChild(node); 
		}
		ept.root = topnode;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitNodeBody(LustreParser.NodeBodyContext ctx) { 
		if( ctx.assert_expression() == null ) {
			EPTNode node = extractEPTNodeFromCTX( ctx.nodeAff() );
			if( node == null ) ErrorLog.myErrorLog("ELOG: undeifned variable encounted under parsing str:" + ctx.getText());
			else if( node instanceof ConstNode ) ErrorLog.myErrorLog("ELOG: Wrong Semantic. Const value can not be changed @" + ctx.getText());
			else if( node instanceof InputNode ) ErrorLog.myErrorLog("ELOG: Wrong Semantic. Input value can not be changed @" + ctx.getText());
			else if( node instanceof VariableNode){
				((VariableNode) node).ASSIGN( new AssignNode( extractEPTNodeFromCTX( ctx.expression() ) ) );
				bindEPTNode2CTX(ctx, node);
			}else {
				ErrorLog.myErrorLog("ELOG: parsing error @" + ctx.getText());
			}
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitLabel(LustreParser.LabelContext ctx) {
		ErrorLog.myErrorLog("ELOG: undeifned semantic for the label rule :" + ctx.getText());
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitAssert_expression(LustreParser.Assert_expressionContext ctx) {
		ErrorLog.myErrorLog("ELOG: undeifned semantic for the rule :" + ctx.getText());
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitNodeAff(LustreParser.NodeAffContext ctx) { 
		if(ctx.getChild(0).getText().equals("(")) {
			ErrorLog.myErrorLog("ELOG: undeifned semantic for the rule :" + ctx.getText());
		}else {
			if(ctx.vectorHelper().size() != 0) {
				ErrorLog.myErrorLog("ELOG: undeifned semantic for the rule :" + ctx.getText());
			}
			bindEPTNode2CTX(ctx, ept.getIdentifier(ctx.IDENTIFIER(0).getText() ));
		}
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterVectorHelper(LustreParser.VectorHelperContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitVectorHelper(LustreParser.VectorHelperContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterType(LustreParser.TypeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitType(LustreParser.TypeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterBound(LustreParser.BoundContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitBound(LustreParser.BoundContext ctx) { }
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitExpression(LustreParser.ExpressionContext ctx) {
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.binary_expression()));
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterCurrent(LustreParser.CurrentContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitCurrent(LustreParser.CurrentContext ctx) { 
		ErrorLog.myErrorLog("ELOG: can't deal with" + ctx.getText() );
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */

	@Override public void exitBinary_expression(LustreParser.Binary_expressionContext ctx) { 
		//-> : right associative
		int num_of_ifexpr = ctx.if_expression().size();
		EPTNode right = extractEPTNodeFromCTX( ctx.if_expression( --num_of_ifexpr ) );

		while(num_of_ifexpr > 0) {
			EPTNode left = extractEPTNodeFromCTX( ctx.if_expression( --num_of_ifexpr ) );
			right = new ArrowNode(left, right);
		}
		bindEPTNode2CTX(ctx, right);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	
	@Override public void exitIf_expression(LustreParser.If_expressionContext ctx) {
		if(ctx.op != null) {
			bindEPTNode2CTX(ctx, new IfNode(extractEPTNodeFromCTX(ctx.followBy_expression(0)), extractEPTNodeFromCTX(ctx.followBy_expression(1)), extractEPTNodeFromCTX(ctx.followBy_expression(2))));
		}else {
			if( ctx.followBy_expression().size() != 1 ) ErrorLog.myErrorLog("ELOG: ctx.followBy_expression().size() != 1"); 
			bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.followBy_expression(0)));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */

	@Override public void exitFollowBy_expression(LustreParser.FollowBy_expressionContext ctx) {
		// or,nor : left associativity
		int num_child_size = ctx.getChildCount();
		EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( 0 ) );
		
		for(int i = 1; i < num_child_size; i+=2) {
			String op = ctx.getChild(i).getText();
			EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i + 1 ) );
			 
			if( op.equals("or") ) {
				left = new OrNode(left, right);
			}else if( op.equals("nor") ) {
				left = new NorNode(left, right);
			}
		}
		bindEPTNode2CTX(ctx, left);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitOr_expression(LustreParser.Or_expressionContext ctx) {
		// and,nand : left associativity
		int num_child_size = ctx.getChildCount();
		EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( 0 ) );
		
		for(int i = 1; i < num_child_size; i+=2) {
			String op = ctx.getChild(i).getText();
			EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i + 1 ) );
			 
			if( op.equals("and") ) {
				left = new AndNode(left, right);
			}else if( op.equals("nand") ) {
				left = new NandNode(left, right);
			}
		}
		bindEPTNode2CTX(ctx, left);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitAnd_expression(LustreParser.And_expressionContext ctx) {
		// xor,nxor : left associativity
		int num_child_size = ctx.getChildCount();
		EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( 0 ) );
		
		for(int i = 1; i < num_child_size; i+=2) {
			String op = ctx.getChild(i).getText();
			EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i + 1 ) );
			 
			if( op.equals("xor") ) {
				left = new XorNode(left, right);
			}else if( op.equals("nxor") ) {
				left = new NxorNode(left, right);
			}
		}
		bindEPTNode2CTX(ctx, left);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitXor_expression(LustreParser.Xor_expressionContext ctx) {
		// =, == : right associative
		int num_child_size = ctx.getChildCount();
		EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( num_child_size - 1 ) );
		
		for(int i = num_child_size - 3; i >= 0; i-=2) {
			String op = ctx.getChild( i + 1 ).getText();
			EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i ) );
			 
			if( op.equals("=") ) {
				right = new EqNode(left, right);
			}else if( op.equals("<>") ) {
				right = new NeqNode(left, right);
			}
		}
		
		bindEPTNode2CTX(ctx, right);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitEqual_expression(LustreParser.Equal_expressionContext ctx) {
		// Comparing operations < , > , <= , >= have no associativity
		if( ctx.getChildCount() > 3 ) {
			ErrorLog.myErrorLog("ELOG: ctx.getChildCount() > 3");
		}
		EPTNode left = extractEPTNodeFromCTX( ctx.compare_expression(0) );
		if( ctx.op != null ) {
			String op = ctx.op.getText();
			EPTNode right = extractEPTNodeFromCTX( ctx.compare_expression(1) );
			if( op.equals("<") ) {
				bindEPTNode2CTX(ctx, new LtNode(left, right));
			}else if( op.equals(">") ) {
				bindEPTNode2CTX(ctx, new GtNode(left, right));
			}else if( op.equals(">=") ) {
				bindEPTNode2CTX(ctx, new GteNode(left, right));
			}else if( op.equals("<=") ) {
				bindEPTNode2CTX(ctx, new LteNode(left, right));
			}
		}else bindEPTNode2CTX(ctx, left);
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitCompare_expression(LustreParser.Compare_expressionContext ctx) { 
		if( ctx.op != null ) ErrorLog.myErrorLog("ctx.op != null");
		
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.shift_expression(0)));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitShift_expression(LustreParser.Shift_expressionContext ctx) {
		// +,- Àº left associativity
		int num_child_size = ctx.getChildCount();
		EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( 0 ) );
		
		for(int i = 1; i < num_child_size; i+=2) {
			String op = ctx.getChild(i).getText();
			EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i + 1 ) );
			 
			if( op.equals("+") ) {
				left = new PlusNode(left, right);
			}else if( op.equals("-") ) {
				left = new MinusNode(left, right);
			}
		}
		bindEPTNode2CTX(ctx, left);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitAdd_expression(LustreParser.Add_expressionContext ctx) { 
		// *,/,div,mod : left associativity
		int num_child_size = ctx.getChildCount();
		EPTNode left = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( 0 ) );
		
		for(int i = 1; i < num_child_size; i+=2) {
			String op = ctx.getChild(i).getText();
			EPTNode right = extractEPTNodeFromCTX( (ParserRuleContext) ctx.getChild( i + 1 ) );
			 
			if( op.equals("*") ) {
				left = new MulNode(left, right);
			}else if( op.equals("/") ) {
				left = new DivNode(left, right);
			}else if( op.equals("div") ) {
				left = new IntDivNode(left, right);
			}else if( op.equals("mod") ) {
				ErrorLog.myErrorLog("ELOG:[op.equals(\"mod\")]");
			}
		}
		bindEPTNode2CTX(ctx, left);		
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitMul_expression(LustreParser.Mul_expressionContext ctx) { 
		if( ctx.op != null ) {
			ErrorLog.myErrorLog("ELOG:[ctx.op != null]");
		}
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.when_expression(0)));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitWhen_expression(LustreParser.When_expressionContext ctx) { 
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.unaryExpression()));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitUnaryExpression(LustreParser.UnaryExpressionContext ctx) {
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX( ctx.simple_expr_p25() ));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitSimple_expr_p25(LustreParser.Simple_expr_p25Context ctx) { 
		EPTNode expr_p30_node = extractEPTNodeFromCTX( ctx.simple_expr_p30() );
		if(ctx.op != null) {
			String op = ctx.op.getText();
			if(op.equals("-")) {
				bindEPTNode2CTX( ctx, new UnaryMinusNode(expr_p30_node) );
			}else if(op.equals("pre")) {
				bindEPTNode2CTX( ctx, new PreNode(expr_p30_node) );
			}else if(op.equals("not")) {
				bindEPTNode2CTX( ctx, new NotNode(expr_p30_node) );
			}
		}else bindEPTNode2CTX(ctx, expr_p30_node);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitSimple_expr_p30(LustreParser.Simple_expr_p30Context ctx) {
		bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.simple_expr_term()));
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitSimple_expr_term(LustreParser.Simple_expr_termContext ctx) {
		
		String firsttoken = ctx.getChild(0).getText();
		//System.out.println("firsttoken:" + firsttoken);
		//System.out.println( firsttoken.equals("real") + " " + firsttoken.equals("floor") + " " + firsttoken.equals("cast(")  + " " + firsttoken.equals("(") + " " + (ctx.literal() != null) + " " + (ctx.IDENTIFIER() != null) );
		if( firsttoken.equals("real") ) {
			ErrorLog.myErrorLog("ELOG: undefined semantic for real(...) rule");	
		}else if( firsttoken.equals("floor") ) {
			ErrorLog.myErrorLog("ELOG: undefined semantic for floor(...) rule");
		}else if( firsttoken.equals("cast(") ) {
			String castType = ctx.getChild(3).getText();
			LustreValueType castTo = null;
			if(castType.equals("real")) {
				castTo = LustreValueType.REAL;
			}else if(castType.equals("int")) {
				castTo = LustreValueType.INT;
			}
			bindEPTNode2CTX(ctx, new CastNode(extractEPTNodeFromCTX(ctx.expression()), castTo));
			//ErrorLog.myErrorLog("ELOG: undefined semantic for cast(...) rule");
		}else if( firsttoken.equals("(") ) {
			bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.expression()) );
		}else if( ctx.literal() != null ) {
			bindEPTNode2CTX(ctx, extractEPTNodeFromCTX(ctx.literal()));
		}else if( ctx.IDENTIFIER() != null ) {
			bindEPTNode2CTX(ctx, ept.getIdentifier(ctx.IDENTIFIER().getText()));
		}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override 
	public void exitLiteral(LustreParser.LiteralContext ctx) { 
		if( ctx.DECIMAL() != null ) {
			bindEPTNode2CTX(ctx, new LiteralNode(ctx.DECIMAL().getText(), LustreValueType.INT));
		}else if( ctx.REAL() != null ) {
			bindEPTNode2CTX(ctx, new LiteralNode(ctx.REAL().getText(), LustreValueType.REAL));
		}else if( ctx.BOOLCONSTANT() != null ) {
			bindEPTNode2CTX(ctx, new LiteralNode(ctx.BOOLCONSTANT().getText(), LustreValueType.BOOL));
		}else if( ctx.TYPED_INT_LIT() != null ) {
			ErrorLog.myErrorLog("ELOG: undefined semantic for TYPED_INT_LIT rule");
		}else if( ctx.TYPED_REAL_LIT() != null ) {
			ErrorLog.myErrorLog("ELOG: undefined semantic for TYPED_REAL_LIT rule");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitTerminal(TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitErrorNode(ErrorNode node) { }
	

}
