// Generated from Lustre.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LustreParser}.
 */
public interface LustreListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LustreParser#lustre}.
	 * @param ctx the parse tree
	 */
	void enterLustre(LustreParser.LustreContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#lustre}.
	 * @param ctx the parse tree
	 */
	void exitLustre(LustreParser.LustreContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(LustreParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(LustreParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#constDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstDeclaration(LustreParser.ConstDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#constDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstDeclaration(LustreParser.ConstDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterNodeDeclaration(LustreParser.NodeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitNodeDeclaration(LustreParser.NodeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeVars}.
	 * @param ctx the parse tree
	 */
	void enterNodeVars(LustreParser.NodeVarsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeVars}.
	 * @param ctx the parse tree
	 */
	void exitNodeVars(LustreParser.NodeVarsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeArgs}.
	 * @param ctx the parse tree
	 */
	void enterNodeArgs(LustreParser.NodeArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeArgs}.
	 * @param ctx the parse tree
	 */
	void exitNodeArgs(LustreParser.NodeArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeArgsHelper}.
	 * @param ctx the parse tree
	 */
	void enterNodeArgsHelper(LustreParser.NodeArgsHelperContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeArgsHelper}.
	 * @param ctx the parse tree
	 */
	void exitNodeArgsHelper(LustreParser.NodeArgsHelperContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeDefinition}.
	 * @param ctx the parse tree
	 */
	void enterNodeDefinition(LustreParser.NodeDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeDefinition}.
	 * @param ctx the parse tree
	 */
	void exitNodeDefinition(LustreParser.NodeDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeBody}.
	 * @param ctx the parse tree
	 */
	void enterNodeBody(LustreParser.NodeBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeBody}.
	 * @param ctx the parse tree
	 */
	void exitNodeBody(LustreParser.NodeBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(LustreParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(LustreParser.LabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#assert_expression}.
	 * @param ctx the parse tree
	 */
	void enterAssert_expression(LustreParser.Assert_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#assert_expression}.
	 * @param ctx the parse tree
	 */
	void exitAssert_expression(LustreParser.Assert_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#nodeAff}.
	 * @param ctx the parse tree
	 */
	void enterNodeAff(LustreParser.NodeAffContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#nodeAff}.
	 * @param ctx the parse tree
	 */
	void exitNodeAff(LustreParser.NodeAffContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#vectorHelper}.
	 * @param ctx the parse tree
	 */
	void enterVectorHelper(LustreParser.VectorHelperContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#vectorHelper}.
	 * @param ctx the parse tree
	 */
	void exitVectorHelper(LustreParser.VectorHelperContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(LustreParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(LustreParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#bound}.
	 * @param ctx the parse tree
	 */
	void enterBound(LustreParser.BoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#bound}.
	 * @param ctx the parse tree
	 */
	void exitBound(LustreParser.BoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(LustreParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(LustreParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#current}.
	 * @param ctx the parse tree
	 */
	void enterCurrent(LustreParser.CurrentContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#current}.
	 * @param ctx the parse tree
	 */
	void exitCurrent(LustreParser.CurrentContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#binary_expression}.
	 * @param ctx the parse tree
	 */
	void enterBinary_expression(LustreParser.Binary_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#binary_expression}.
	 * @param ctx the parse tree
	 */
	void exitBinary_expression(LustreParser.Binary_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#if_expression}.
	 * @param ctx the parse tree
	 */
	void enterIf_expression(LustreParser.If_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#if_expression}.
	 * @param ctx the parse tree
	 */
	void exitIf_expression(LustreParser.If_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#followBy_expression}.
	 * @param ctx the parse tree
	 */
	void enterFollowBy_expression(LustreParser.FollowBy_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#followBy_expression}.
	 * @param ctx the parse tree
	 */
	void exitFollowBy_expression(LustreParser.FollowBy_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#or_expression}.
	 * @param ctx the parse tree
	 */
	void enterOr_expression(LustreParser.Or_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#or_expression}.
	 * @param ctx the parse tree
	 */
	void exitOr_expression(LustreParser.Or_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#and_expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expression(LustreParser.And_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#and_expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expression(LustreParser.And_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#xor_expression}.
	 * @param ctx the parse tree
	 */
	void enterXor_expression(LustreParser.Xor_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#xor_expression}.
	 * @param ctx the parse tree
	 */
	void exitXor_expression(LustreParser.Xor_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#equal_expression}.
	 * @param ctx the parse tree
	 */
	void enterEqual_expression(LustreParser.Equal_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#equal_expression}.
	 * @param ctx the parse tree
	 */
	void exitEqual_expression(LustreParser.Equal_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#compare_expression}.
	 * @param ctx the parse tree
	 */
	void enterCompare_expression(LustreParser.Compare_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#compare_expression}.
	 * @param ctx the parse tree
	 */
	void exitCompare_expression(LustreParser.Compare_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#shift_expression}.
	 * @param ctx the parse tree
	 */
	void enterShift_expression(LustreParser.Shift_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#shift_expression}.
	 * @param ctx the parse tree
	 */
	void exitShift_expression(LustreParser.Shift_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#add_expression}.
	 * @param ctx the parse tree
	 */
	void enterAdd_expression(LustreParser.Add_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#add_expression}.
	 * @param ctx the parse tree
	 */
	void exitAdd_expression(LustreParser.Add_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#mul_expression}.
	 * @param ctx the parse tree
	 */
	void enterMul_expression(LustreParser.Mul_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#mul_expression}.
	 * @param ctx the parse tree
	 */
	void exitMul_expression(LustreParser.Mul_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#when_expression}.
	 * @param ctx the parse tree
	 */
	void enterWhen_expression(LustreParser.When_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#when_expression}.
	 * @param ctx the parse tree
	 */
	void exitWhen_expression(LustreParser.When_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(LustreParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(LustreParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#simple_expr_p25}.
	 * @param ctx the parse tree
	 */
	void enterSimple_expr_p25(LustreParser.Simple_expr_p25Context ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#simple_expr_p25}.
	 * @param ctx the parse tree
	 */
	void exitSimple_expr_p25(LustreParser.Simple_expr_p25Context ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#simple_expr_p30}.
	 * @param ctx the parse tree
	 */
	void enterSimple_expr_p30(LustreParser.Simple_expr_p30Context ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#simple_expr_p30}.
	 * @param ctx the parse tree
	 */
	void exitSimple_expr_p30(LustreParser.Simple_expr_p30Context ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#simple_expr_term}.
	 * @param ctx the parse tree
	 */
	void enterSimple_expr_term(LustreParser.Simple_expr_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#simple_expr_term}.
	 * @param ctx the parse tree
	 */
	void exitSimple_expr_term(LustreParser.Simple_expr_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link LustreParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(LustreParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link LustreParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(LustreParser.LiteralContext ctx);
}