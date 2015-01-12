/*
 * Copyright 2005 Hailang Thai, Minh Cuong Tran, Lesmana Zimmer
 *
 * This file is part of AdaLogo.
 *
 * AdaLogo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * AdaLogo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AdaLogo; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package adalogo.visitor;

import java.util.Random;

import adalogo.Engine;
import adalogo.Turtle;
import adalogo.gui.Console;
import adalogo.gui.StatusBar;
import adalogo.lang.ASTAdditionNode;
import adalogo.lang.ASTAndNode;
import adalogo.lang.ASTAssignmentIdentifier;
import adalogo.lang.ASTAssignmentStatement;
import adalogo.lang.ASTBooleanDeclarationNode;
import adalogo.lang.ASTCompilationUnit;
import adalogo.lang.ASTDashNode;
import adalogo.lang.ASTDeclaration;
import adalogo.lang.ASTDivisionNode;
import adalogo.lang.ASTElsePart;
import adalogo.lang.ASTElsifPart;
import adalogo.lang.ASTEqualNode;
import adalogo.lang.ASTExitStatement;
import adalogo.lang.ASTFalseNode;
import adalogo.lang.ASTForIdentifier;
import adalogo.lang.ASTForReverse;
import adalogo.lang.ASTForStatement;
import adalogo.lang.ASTForwardStatement;
import adalogo.lang.ASTGetDirExpression;
import adalogo.lang.ASTGetXExpression;
import adalogo.lang.ASTGetYExpression;
import adalogo.lang.ASTGreaterEqualNode;
import adalogo.lang.ASTGreaterThanNode;
import adalogo.lang.ASTIdentifier;
import adalogo.lang.ASTIfStatement;
import adalogo.lang.ASTIntegerDeclarationNode;
import adalogo.lang.ASTIntegerLiteral;
import adalogo.lang.ASTJumpToStatement;
import adalogo.lang.ASTLessEqualNode;
import adalogo.lang.ASTLessThanNode;
import adalogo.lang.ASTLoopStatement;
import adalogo.lang.ASTMaxExpression;
import adalogo.lang.ASTMinExpression;
import adalogo.lang.ASTModNode;
import adalogo.lang.ASTMultiplicationNode;
import adalogo.lang.ASTNewLineStatement;
import adalogo.lang.ASTNotEqualNode;
import adalogo.lang.ASTNotNode;
import adalogo.lang.ASTNullStatement;
import adalogo.lang.ASTOrNode;
import adalogo.lang.ASTPenDownStatement;
import adalogo.lang.ASTPenUpStatement;
import adalogo.lang.ASTProcedureCallStatement;
import adalogo.lang.ASTProcedureCallStatementIdentifier;
import adalogo.lang.ASTProcedureCallStatementParameters;
import adalogo.lang.ASTProcedureDeclaration;
import adalogo.lang.ASTProcedureDeclarationIdentifier;
import adalogo.lang.ASTProcedureDeclarationParameters;
import adalogo.lang.ASTPutLineStatement;
import adalogo.lang.ASTPutStatement;
import adalogo.lang.ASTRandomExpression;
import adalogo.lang.ASTRemNode;
import adalogo.lang.ASTResetTurtleStatement;
import adalogo.lang.ASTSemi;
import adalogo.lang.ASTSequenceOfStatement;
import adalogo.lang.ASTStringLiteral;
import adalogo.lang.ASTSubtractionNode;
import adalogo.lang.ASTTrueNode;
import adalogo.lang.ASTTurnStatement;
import adalogo.lang.ASTTurnToStatement;
import adalogo.lang.ASTVariableDeclaration;
import adalogo.lang.ASTVariableDeclarationIdentifier;
import adalogo.lang.ASTWhileStatement;
import adalogo.lang.LangVisitor;
import adalogo.lang.SimpleNode;

//TODO a procedure defining at the point ealier can not see the a procedure
//the point later. It is not easy to fix this, changing the grammar etc.

//TODO implements a small browser in AdaLogo/help/help for showing helpsite
//http://adalogo.cuong.net -> java.net

/**
 * This is the heart of the interpreter in AdaLogo.
 * After using Lang.jjt with JavaCC-3.2 a nice parse tree
 * was built, so it "should be not a big thing" to interpret
 * the building tree. ;)
 */
public class InterpreterVisitor implements LangVisitor {

    //search BREAKPOINT for break point handling

    private Engine engine;
    private StatusBar statusBar;
    private Console console;
    private Turtle turtle;

    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    private static final String PROCEDURE = "procedure";
    private static final String BLOCK = "block";

    /**
     * this one is for just checking the semantic.
     * if false no turtle actions will be done.
     * the idea is to run once, check for errors.
     * the run again and execute with turtle moves.
     * TODOlater fix DIRTY HACK
     */
    private boolean execute = true;

    private Random random = new Random();

    /**
     * the thread which controls this visitor
     */
    private VisitorMaster master;

    private SymbolTable symtab;
    private ProgramCounter pc;

    /**
     * create and start an interpreter.
     * @param engine
     * @param master the thread for this interpeter (for step by step).
     * @param node the AST node which the interpreter should interpret.
     */
    public InterpreterVisitor(Engine engine, VisitorMaster master, SimpleNode node, boolean execute) {

        this.engine = engine;
        this.statusBar = engine.getStatusBar();
        this.console = engine.getConsole();
        this.turtle = engine.getTurtle();

        this.master = master;

        this.execute = execute;

        symtab = new SymbolTable();
        pc = new ProgramCounter();

        if (master.isDebugRun()) {
            engine.getVarMonitor().beginMonitoring(symtab, pc);
        }

        node.jjtAccept(this, "");

        if (master.isDebugRun()) {
            engine.getVarMonitor().endMonitoring();
        }

    }

    //-------------------------------------------------------------------------
    // here come Exception classes
    //-------------------------------------------------------------------------
    /**
     * ExitStatementException
     * nothing special here.
     */
    public class ExitStatementException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ExitStatementException(String message) {
            super(message);
        }
    }

    /**
     * AssignmentStatementException
     * nothing special here.
     */
    public class AssignmentStatementException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AssignmentStatementException(String message) {
            super(message);
        }
    }

    /**
     * ProcedureCallStatementException
     * nothing special here.
     */
    public class ProcedureCallStatementException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ProcedureCallStatementException(String message) {
            super(message);
        }
    }


    /**
     * InterpreterVisitorException
     * nothing special here.
     */
    public class InterpreterAbortException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InterpreterAbortException(String message) {
            super(message);
        }
    }

    /**
     * IdentifierException
     * nothing special here.
     */
    public class IdentifierException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IdentifierException(String message) {
            super(message);
        }
    }

    /**
     * IntegerIdentifierException
     * nothing special here.
     */
    public class WrongNumberOfChildrenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WrongNumberOfChildrenException(String message) {
            super(message);
        }
    }

    /**
     * WrongBooleanExpressionException
     * nothing special here.
     */
    public class WrongBooleanExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WrongBooleanExpressionException(String message) {
            super(message);
        }
    }

    /**
     * WrongIntegerExpressionException
     * nothing special here.
     */
    public class WrongIntegerExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WrongIntegerExpressionException(String message) {
            super(message);
        }
    }

    /**
     * WrongIntegerExpressionException
     * nothing special here.
     */
    public class AdaLogoSyntaxSemanticException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AdaLogoSyntaxSemanticException(String message) {
            super(message);
        }
    }

    /**
     * Get the parse tree and return true, if ExitStatement is the right place,
     * otherwise false.
     */
    private boolean checkingExitStatementAtWrongPlace(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "ExitStatement") {
            return false;
        }
        else if (node.toString() == "ForStatement" |
                node.toString() == "LoopStatement" |
                node.toString() == "WhileStatement") {
            return true;
        }
        else {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingExitStatementAtWrongPlace(childI);
            }
            return check;
        }
    }

    /**
     * Because of the ambiguity of the grammar it is essential
     * to check if a BooleanExpression really look like a boolean expression.
     * @param node
     * @return boolean
     */
    private boolean checkingBooleanExpression(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "AdditionNode" |
                node.toString() == "SubtractionNode" |
                node.toString() == "MultiplicationNode" |
                node.toString() == "DivisionNode" |
                node.toString() == "ModNode" |
                node.toString() == "RemNode" |
                node.toString() == "DashNode" |
                node.toString() == "IntegerLiteral" |
                node.toString() == "MinExpression" |
                node.toString() == "MaxExpression" |
                node.toString() == "RandomExpression" |
                node.toString() == "GetDirExpression" |
                node.toString() == "GetXExpression" |
                node.toString() == "GetYExpression") {
            return false;
        }
        else if (node.toString() == "Identifier") {
            if ( (symtab.getType(node.getValue().toLowerCase())) == BOOLEAN ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (node.toString() == "TrueNode" |
                node.toString() == "FalseNode" |
                node.toString() == "EqualNode" |
                node.toString() == "NotEqualNode" |
                node.toString() == "GreaterThanNode" |
                node.toString() == "GreaterEqualNode" |
                node.toString() == "LessThanNode" |
                node.toString() == "LessEqualNode" ) {
            return true;
        }
        else if (node.toString() == "OrNode" |
                node.toString() == "AndNode" |
                node.toString() == "NotNode") {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingBooleanExpression(childI);
            }
            return check;
        }
        else {
            //throw new RuntimeException("Something is going wrong at checkingBooleanExpression.");
            return false;
        }
    }

    /**
     * Because of the ambiguity of the grammar it is essential
     * to check if an IntegerExpression really look like an integer expression.
     * @param node
     * @return boolean
     */
    private boolean checkingIntegerExpression(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "OrNode" |
                node.toString() == "AndNode" |
                node.toString() == "NotNode" |
                node.toString() == "TrueNode" |
                node.toString() == "FalseNode" |
                node.toString() == "EqualNode" |
                node.toString() == "NotEqualNode" |
                node.toString() == "GreaterThanNode" |
                node.toString() == "GreaterEqualNode" |
                node.toString() == "LessThanNode" |
                node.toString() == "LessEqualNode" ) {
            return false;
        }
        else if (node.toString() == "Identifier") {
            if ( (symtab.getType(node.getValue().toLowerCase())) == INTEGER ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (node.toString() == "IntegerLiteral" |
                node.toString() == "MinExpression" |
                node.toString() == "MaxExpression" |
                node.toString() == "RandomExpression" |
                node.toString() == "GetDirExpression" |
                node.toString() == "GetXExpression" |
                node.toString() == "GetYExpression") {
            return true;
        }
        else if (node.toString() == "AdditionNode" |
                node.toString() == "SubtractionNode" |
                node.toString() == "MultiplicationNode" |
                node.toString() == "DivisionNode" |
                node.toString() == "ModNode" |
                node.toString() == "RemNode" |
                node.toString() == "DashNode" ) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingIntegerExpression(childI);
            }
            return check;
        }
        else {
            //throw new RuntimeException("Something is going wrong at checkingIntegerExpression.");
            return false;
        }
    }


    //-------------------------------------------------------------------------
    // here come visit methods
    //-------------------------------------------------------------------------
    /**
     * Do nothing, this should never have been called.
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println("visit SimpleNode. this should never happen");
        data = node.childrenAccept(this, data);
        return data;
    }

    /**
     * The begin of every program.
     * Take the children and go down.
     */
    public Object visit(ASTCompilationUnit node, Object data) {
        if (!checkingExitStatementAtWrongPlace((SimpleNode)node)) {
            throw new ExitStatementException("");
        }

        SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

        if (child1.jjtGetNumChildren() == 3) {
            SimpleNode child1_1, child1_2, child1_3;
            child1_1 = (SimpleNode)(child1.jjtGetChild(0));
            child1_2 = (SimpleNode)(child1.jjtGetChild(1));
            child1_3 = (SimpleNode)(child1.jjtGetChild(2));

            symtab.put(new String ("procedure " + child1_1.getValue().toLowerCase()),BLOCK,child1_1.getValue().toLowerCase());
            symtab.put(child1_1.getValue().toLowerCase(),PROCEDURE,node);

            child1_2.jjtAccept(this,data);
            child1_3.jjtAccept(this,data);
        }
        else if (child1.jjtGetNumChildren() == 4) {
            SimpleNode child1_1, child1_2, child1_3, child1_4;
            child1_1 = (SimpleNode)(child1.jjtGetChild(0));
            child1_2 = (SimpleNode)(child1.jjtGetChild(1));
            child1_3 = (SimpleNode)(child1.jjtGetChild(2));
            child1_4 = (SimpleNode)(child1.jjtGetChild(3));

            symtab.put(new String ("procedure " + child1_1.getValue().toLowerCase()),BLOCK,child1_1.getValue().toLowerCase());
            symtab.put(child1_1.getValue().toLowerCase(),PROCEDURE,node);

            child1_2.childrenAccept(this,data);
            child1_3.jjtAccept(this,data);
            child1_4.jjtAccept(this,data);
        }
        else {
            throw new WrongNumberOfChildrenException("CompilationUnit");
        }
        return data;
    }

    /**
     * DeclarationPart of every Procedure
     * If the child is a ProcedureDeclaration,
     * so do it in the symtab and
     * do not execute ProcedureDeclaration.
     * otherwise the children do the work.
     * see: Lang.jjt or Lang.jjt.html
     */
    public Object visit(ASTDeclaration node, Object data) {
        //BREAKPOINT stop at declaration? no.

        for (int i = 0; i < node.jjtGetNumChildren(); i+=2) {
            //Semicolons are ignored
            if (node.jjtGetChild(i).toString() == "ProcedureDeclaration") {
                SimpleNode childI;
                childI = (SimpleNode)(node.jjtGetChild(i));
                SimpleNode childI_1 = (SimpleNode)(childI.jjtGetChild(0));
                symtab.put(childI_1.getValue().toLowerCase(),PROCEDURE,node.jjtGetChild(i));
            }
            else {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
        }
        return data;
    }

    /**
     * Go down __without__ Identifier and Parameters.
     * Identifier and Parameters are done by CompilationUnit or ProcedureCallStatement
     */
    public Object visit(ASTProcedureDeclaration node, Object data) {

        handleBreakpoint(node); //BREAKPOINT
        //TODOlater make procedure break at end

        if (node.jjtGetNumChildren() == 3) {
            node.childrenAccept(this,data);
        }
        else if (node.jjtGetNumChildren() == 4) {
            node.childrenAccept(this,data);
        }
        else {
            throw new WrongNumberOfChildrenException("ProcedureDeclaration");
        }

        return data;
    }

    /**
     * "Dummy-node", everything should be done by
     * parent node ProcedureDeclaration
     */
    public Object visit(ASTProcedureDeclarationParameters node, Object data) {
        return data;
    }


    /**
     * In the Declaration a boolean/integer can de declare with or without
     * an assigment of BooleanExpression.
     * If there is no assigment directly, a random value is put
     * in the symtab.
     */
    public Object visit(ASTVariableDeclaration node, Object data) {

        // Declaration without Assignment, value is random
        if (node.jjtGetNumChildren() == 2) {
            if (node.jjtGetChild(1).toString() == "BooleanDeclarationNode") {
                symtab.put(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                        BOOLEAN,(new Boolean(random.nextBoolean())).toString());
            }
            else if (node.jjtGetChild(1).toString() == "IntegerDeclarationNode") {
                symtab.put( ((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),INTEGER,
                        (new Integer(random.nextInt())).toString() );
            }
            else {
                throw new RuntimeException("VariableDeclaration without Assignment has a wrong child.");
            }
        }

        // Declaration with Assigment
        else if (node.jjtGetNumChildren() == 3) {
            if (node.jjtGetChild(1).toString() == "BooleanDeclarationNode") {
                SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));

                if (!checkingBooleanExpression(child3)) {
                    int line = node.getLine();
                    throw new WrongBooleanExpressionException("Sorry. Your boolean expression at line "+ line + " is not correct!");
                }

                Boolean data3 = (Boolean)(child3.jjtAccept(this,data));
                symtab.put(((SimpleNode)node.jjtGetChild(0)).getValue().toLowerCase(),
                        BOOLEAN,data3.toString());
            }
            else if (node.jjtGetChild(1).toString() == "IntegerDeclarationNode") {
                SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));

                if (!checkingIntegerExpression(child3)) {
                    int line = node.getLine();
                    throw new WrongIntegerExpressionException("Sorry. Your integer expression at line "+ line + " is not correct!");
                }

                Integer data3 = (Integer)(child3.jjtAccept(this,data));
                symtab.put(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                        INTEGER,data3.toString());
            }
            else {
                throw new RuntimeException("VariableDeclaration with Assignment has a wrong child.");
            }
        }
        else {
            throw new WrongNumberOfChildrenException("VariableDeclaration");
        }

        return data;
    }

    /**
     * Visit every child and execute
     */
    public Object visit(ASTSequenceOfStatement node, Object data) {
        //BREAKPOINT special treatment for control structures
        if (node.jjtGetNumChildren() % 2 == 0) {
            for (int i = 0; i < node.jjtGetNumChildren(); i+=2) {
                //TODOlater put case here when implemented
                if (node.jjtGetChild(i).toString() == "ForStatement" ||
                        node.jjtGetChild(i).toString() == "IfStatement" ||
                        node.jjtGetChild(i).toString() == "LoopStatement" ||
                        node.jjtGetChild(i).toString() == "WhileStatement") {
                    node.jjtGetChild(i).jjtAccept(this, data);
                    node.jjtGetChild(i+1).jjtAccept(this, data);
                }
                else {
                    //first semi, then the node because of highlighting code.
                    node.jjtGetChild(i+1).jjtAccept(this, data);
                    node.jjtGetChild(i).jjtAccept(this, data);
                }
            }
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("SequenceOfStatement");
        }
    }

    /**
     * Execute ForwardStatement from Turtle.
     */
    public Object visit(ASTForwardStatement node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

            Integer data1 = (Integer)(child1.jjtAccept(this,INTEGER));

            if (execute) {
                turtle.forward(data1.doubleValue());
            }
        }
        else {
            throw new WrongNumberOfChildrenException("ForwardStatement");
        }
        return data;
    }

    /**
     * Let Turtle jump to point (x,y);
     */
    public Object visit(ASTJumpToStatement node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));

            Integer data1 = (Integer)(child1.jjtAccept(this,INTEGER));
            Integer data2 = (Integer)(child2.jjtAccept(this,INTEGER));

            if (execute) {
                turtle.moveTo(data1.doubleValue(),data2.doubleValue());
            }
        }
        else {
            throw new WrongNumberOfChildrenException("JumpToStatement");
        }
        return data;
    }

    /**
     * new_line; put a new line to the console.
     */
    public Object visit(ASTNewLineStatement node, Object data) {
        if (execute) {
            console.append("");
        }
        return data;
    }

    /**
     * Turtle.penDown();
     */
    public Object visit(ASTPenDownStatement node, Object data) {
        if (execute) {
            turtle.penDown();
        }
        return data;
    }

    /**
     * Turtle.penUp();
     */
    public Object visit(ASTPenUpStatement node, Object data) {
        if (execute) {
            turtle.penUp();
        }
        return data;
    }

    /**
     * This procedure is used by Put- and PutLineStatement.
     * If the parameter is an integer or boolean, so evaluate and put.
     * If it is a string put.
     * Otherwise you get an error.
     */
    private void helpPutOrPutLineStatement(SimpleNode node, Object data, String strAtTheEnd) {
        SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

        if (checkingBooleanExpression(child1)) {
            Boolean result = (Boolean)(child1.jjtAccept(this,BOOLEAN));
            if (execute) {
                console.appendWithoutNewline(result.toString() + strAtTheEnd);
            }
        }
        else if (checkingIntegerExpression(child1)) {
            Integer result = (Integer)(child1.jjtAccept(this,BOOLEAN));
            if (execute) {
                console.appendWithoutNewline(result.toString() + strAtTheEnd);
            }
        }
        else if (child1.toString() == "Identifier") {
            if (symtab.variableExists((Object)child1.getValue())) {
                if (symtab.getType((Object)child1.getValue()) == INTEGER) {
                    if (execute) {
                        console.appendWithoutNewline(child1.jjtAccept(this,INTEGER).toString() + strAtTheEnd);
                    }
                }
                else if (symtab.getType((Object)child1.getValue()) == BOOLEAN) {
                    if (execute) {
                        console.appendWithoutNewline(child1.jjtAccept(this,BOOLEAN).toString() + strAtTheEnd);
                    }
                }
                else {
                    String varName = node.getValue();
                    int line = node.getLine();
                    int col = node.getColumn();
                    throw new AdaLogoSyntaxSemanticException("The variable " + varName +
                            "at line " + line + " and column " + col +
                    " cannot be put to the console.");
                }
            }
            else {
                String varName = node.getValue();
                int line = node.getLine();
                int col = node.getColumn();
                throw new IdentifierException("Sorry! The variable " + varName + " at line " +
                        line + " and column " + col + " exists, but it has " +
                        "the " + symtab.getType(node.getValue().toLowerCase()) + " type");
            }
        }
        else if (child1.toString() == "StringLiteral") {
            if (execute) {
                console.appendWithoutNewline(child1.getValue().replaceAll("\"","") + strAtTheEnd);
            }
        }
        else {
            int line, col;
            line = node.getLine();
            col = node.getColumn();
            throw new AdaLogoSyntaxSemanticException("The statement put or put_line " +
                    "at line " + line +  " and column " +col+
                    " could not be execute, because the boolean or integer expression " +
            "is wrong?");
        }
    }

    /**
     * This will put the result of BooleanExpression, IntegerExpression
     * or StringLiteral. Uses helpPutOrPutLineStatement
     */
    public Object visit(ASTPutStatement node, Object data) {
        helpPutOrPutLineStatement(node,data,"");
        return data;
    }

    /**
     * This will put a line with result of BooleanExpression, IntegerExpression
     * or StringLiteral. Uses helpPutOrPutLineStatement
     */
    public Object visit(ASTPutLineStatement node, Object data) {
        helpPutOrPutLineStatement(node,data,"\n");
        return data;
    }

    /**
     * return "String" (Object).
     */
    public Object visit(ASTStringLiteral node, Object data) {
        return node.getValue();
    }

    /**
     * Reset turtle to point(0,0); The direction of the turtle is -90
     */
    public Object visit(ASTResetTurtleStatement node, Object data) {
        if (execute) {
            turtle.resetTurtle();
        }
        return data;
    }

    /**
     * Turtle.turn();
     */
    public Object visit(ASTTurnStatement node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

            Integer data1 = (Integer)(child1.jjtAccept(this,INTEGER));

            // turn im mathematischen sinne! -> bei turtle
            if (execute) {
                turtle.turn(data1.doubleValue());
            }
        }
        else {
            throw new WrongNumberOfChildrenException("TurnStatement");
        }
        return data;
    }

    /**
     * This procedure let the turtle turn to the degree you want.
     * It get an IntegerExpression as value
     */
    public Object visit(ASTTurnToStatement node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            Integer data1 = (Integer)(child1.jjtAccept(this,INTEGER));
            // turn im mathematischen sinne! -> bei turtle
            if (execute) {
                turtle.turnTo(data1.doubleValue());
            }
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("TurnToStatement");
        }
    }

    /**
     * Assignment of BooleanExpression or IntegerExpression to a
     * defining variable. If the variable not exists -> throws Exception
     * At this place we have also to solve the ambiguity of Identifier.
     */
    public Object visit(ASTAssignmentStatement node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));
            String type = symtab.getType(child1.getValue().toLowerCase());

            int line = child1.getLine();
            int col = child1.getColumn();
            String varName = child1.getValue();

            if (checkingIntegerExpression(child2)) {
                Integer data2 = (Integer)(child2.jjtAccept(this,INTEGER));
                // everything was ok:
                if (type == INTEGER) {
                    symtab.setValue(child1.getValue().toLowerCase(),data2.toString());
                }
                else {
                    throw new AssignmentStatementException("Sorry! Wrong type, the variable "
                            + varName + " at line " + line + " and column " + col +
                    " should be an integer");
                }
            }
            else if (checkingBooleanExpression(child2)) {
                Boolean data2 = (Boolean)(child2.jjtAccept(this,BOOLEAN));
                // everything was ok:
                if (type == BOOLEAN) {
                    symtab.setValue(child1.getValue().toLowerCase(),data2.toString());
                }
                else {
                    throw new AssignmentStatementException("Sorry! Wrong type, the variable "
                            + varName + " at line " + line + " and column " + col +
                    " should be a boolean");
                }
            }
            else if (child2.toString() == "Identifier") {
                if (type == INTEGER) {
                    Integer data2 = (Integer)(child2.jjtAccept(this,INTEGER));
                    symtab.setValue(child1.getValue().toLowerCase(),data2.toString());
                }
                else if (type == BOOLEAN) {
                    Boolean data2 = (Boolean)(child2.jjtAccept(this,BOOLEAN));
                    symtab.setValue(child1.getValue().toLowerCase(),data2.toString());
                }
                else {
                    throw new AssignmentStatementException("This should never happen. " +
                    "Something was going wrong at the AssignmentStatement");
                }
            }
            else {
                throw new AdaLogoSyntaxSemanticException("Sorry! Your assign statement" +
                        " at line " +line+ " and column " +col+ " is a wrong expression." +
                        " You are sure it is really a valid boolean or integer" +
                "expression?");
            }
        }
        else {
            throw new WrongNumberOfChildrenException("AssignmentStatement");
        }
        return data;
    }

    /**
     * everything will be done by parent AssignmentStatement
     */
    public Object visit(ASTAssignmentIdentifier node, Object data) {
        return data;
    }

    /**
     * throws an ExitStatementException. This will be catched by
     * For-, While- or LoopStatement
     */
    public Object visit(ASTExitStatement node, Object data) {
        throw new ExitStatementException("");
    }

    /**
     * A ForStatement. Evaluate the 2 ranges and loop, with
     * or without reverse. Execute SequenceOfStatement in
     * the loop.
     */
    public Object visit(ASTForStatement node, Object data) {

        // without reverse:
        if ( checkingIntegerExpression((SimpleNode)(node.jjtGetChild(1)))
                & node.jjtGetNumChildren() == 4 ) {

            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));
            SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));

            int leftRange = ((Integer)(child2.jjtAccept(this,INTEGER))).intValue();
            int rightRange = ((Integer)(child3.jjtAccept(this,INTEGER))).intValue();

            try {
                symtab.levelUp();
                symtab.put(new String("for"),BLOCK,new String("for"));

                symtab.put(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                        INTEGER,
                        (new Integer(leftRange)).toString());

                handleBreakpoint(node); //BREAKPOINT
                for (int i = leftRange; i <= rightRange; i++) {
                    symtab.setValue(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                            (new Integer(i)).toString());
                    data = node.jjtGetChild(3).jjtAccept(this, data);
                    handleBreakpoint(node); //BREAKPOINT
                }

                symtab.levelDown();
            } catch (ExitStatementException e) {
                symtab.levelDown();
            }
        }
        // with reverse:
        else if ( (((SimpleNode)(node.jjtGetChild(1))).toString() == "ForReverse")
                & node.jjtGetNumChildren() == 5) {

            SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));
            SimpleNode child4 = (SimpleNode)(node.jjtGetChild(3));

            int leftRange = ((Integer)(child3.jjtAccept(this,INTEGER))).intValue();
            int rightRange = ((Integer)(child4.jjtAccept(this,INTEGER))).intValue();

            try {
                symtab.levelUp();
                symtab.put(new String("for reverse"),BLOCK,new String("for reverse"));

                symtab.put(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                        INTEGER,
                        (new Integer(rightRange)).toString());

                handleBreakpoint(node); //BREAKPOINT
                for (int i = rightRange; i >= leftRange; i--) {
                    symtab.setValue(((SimpleNode)(node.jjtGetChild(0))).getValue().toLowerCase(),
                            (new Integer(i)).toString());
                    data = node.jjtGetChild(4).jjtAccept(this, data);
                    handleBreakpoint(node); //BREAKPOINT
                }

                symtab.levelDown();
            } catch (ExitStatementException e) {
                symtab.levelDown();
            }
        }
        else {
            throw new AdaLogoSyntaxSemanticException("Child at position " +
                    "2 is of wrong type: " +
                    ((SimpleNode)(node.jjtGetChild(1))).toString() +
                    "or ForStatement");
        }
        return data;
    }

    /**
     * Will be done in ForStatement
     */
    public Object visit(ASTForIdentifier node, Object data) {
        return data;
    }

    /**
     * "Dummy"-Node
     */
    public Object visit(ASTForReverse node, Object data) {
        return data;
    }

    /**
     * check all the booleanPart of children (+ elsif) and at least do else.
     * and select one posibility to do.
     */
    public Object visit(ASTIfStatement node, Object data) {
        int numOfChildren = node.jjtGetNumChildren();

        //FIXME breakpoint
        if (numOfChildren >= 2) {

            // ifPart
            if (checkingBooleanExpression((SimpleNode)node.jjtGetChild(0))) {
                handleBreakpoint(node); //BREAKPOINT
                if (((Boolean)(node.jjtGetChild(0).jjtAccept(this, BOOLEAN))).booleanValue()) {
                    return node.jjtGetChild(1).jjtAccept(this,data);
                }
            }
            else {
                int line = node.getLine();
                int col = node.getColumn();
                throw new AdaLogoSyntaxSemanticException(
                        "The boolean expression of the if-statement " +
                        "at line " + line + " and column " + col +
                        " is not correct.");
            }

            
            boolean elsePartExists = false;
            if (node.jjtGetNumChildren() > 2 &&
                    ((SimpleNode)(node.jjtGetChild(numOfChildren-1))).toString() == "ElsePart")
            {
                elsePartExists = true;
            }
            
            int tmpNumOfChildren;
            if (elsePartExists) {
                tmpNumOfChildren = numOfChildren;
			}
            else {
            	tmpNumOfChildren = numOfChildren+1;
            }
            
            // one of elsifPart
            for (int i = 3; i< tmpNumOfChildren; i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i-1));

                if (checkingBooleanExpression((SimpleNode)childI.jjtGetChild(0))) {
                    handleBreakpoint(childI); //BREAKPOINT
                    if (((Boolean)(childI.jjtGetChild(0).jjtAccept(this,BOOLEAN))).booleanValue()) {
                        return childI.jjtGetChild(1).jjtAccept(this,data);
                    }
                }
                else {
                    int line = childI.getLine();
                    int col = childI.getColumn();
                    throw new AdaLogoSyntaxSemanticException(
                            "The boolean expression of the elsif-statement " +
                            "at line " + line + " and column " + col +
                            " is not correct.");
                }
            }

            // elsePart
            if (elsePartExists) {
                handleBreakpoint((SimpleNode)node.jjtGetChild(numOfChildren-1)); //BREAKPOINT
                return node.jjtGetChild(numOfChildren-1).jjtAccept(this,data);
            }
            // nothing was true, no else part
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("IfStatement");
        }
    }

    /**
     * Just do what ElsifPart want to be done, without checking.
     * IfStatement do the work.
     */
    public Object visit(ASTElsifPart node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));
            child2.jjtAccept(this,data);
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("ElsifPart");
        }
    }

    /**
     * Just do what ElsePart want to be done, without checking.
     * IfStatement do the work
     */
    public Object visit(ASTElsePart node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            child1.jjtAccept(this,data);
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("ElsePart");
        }
    }

    /**
     * No children, do nothing.
     */
    public Object visit(ASTNullStatement node, Object data) {
        return data;
    }

    /**
     * Loop till the inphinity. Or if there is a ExitStatement which throws
     * ExitStatementException, then go out.
     */
    public Object visit(ASTLoopStatement node, Object data) {
        try {
            int countLoop = 0;
            handleBreakpoint(node); //BREAKPOINT
            while (true) {
                data = node.childrenAccept(this, data);
                countLoop++;
                if (countLoop % 10000000 == 0) {
                    statusBar.setText("You might have an infinite loop here.");
                }
                //if (countLoop == 2147483647) { //too long
                if (countLoop == 50000000) {
                    throw new InterpreterAbortException(
                            "Execution aborted because of infinite loop");
                }
                handleBreakpoint(node); //BREAKPOINT
            }
        } catch (ExitStatementException e) {
        }
        return data;
    }

    /**
     * Look into the symtab and copy the procedure node out and excecute.
     * If there are parameters, so checked the length and the type of the
     * parameters and execute the procedure node with this parameters, other
     * wise throws exception.
     */
    public Object visit(ASTProcedureCallStatement node, Object data) {

        // no parameter
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

            int line = ((SimpleNode)node.jjtGetChild(0)).getLine();
            int col = ((SimpleNode)node.jjtGetChild(0)).getColumn();
            String procedureCallName = child1.getValue().toLowerCase();

            if (symtab.variableExists((Object)procedureCallName)) {
                Object procedureDefNode = symtab.getValue((Object)procedureCallName);
                if (((SimpleNode)procedureDefNode).jjtGetNumChildren() == 3) {
                    symtab.levelUp();
                    symtab.put(new String("procedure " + procedureCallName),BLOCK, procedureCallName);
                    ((SimpleNode)(procedureDefNode)).jjtAccept(this,data);
                    symtab.levelDown();
                }
                else {
                    throw new ProcedureCallStatementException("The procedure "
                            + procedureCallName + " at line " + line + " and column " + col +
                    " has parameters, but you call it without any!");
                }
            }
            else {
                throw new ProcedureCallStatementException("The definition of procedure "
                        + procedureCallName + " at line " + line + " and column " + col +
                " does not exist or is not visible!");
            }
        }
        // with parameters
        else if (node.jjtGetNumChildren() == 2){
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0)); // procedureCallIdentifier
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1)); // procedureCallParameters

            int line = ((SimpleNode)node.jjtGetChild(0)).getLine();
            int col = ((SimpleNode)node.jjtGetChild(0)).getColumn();
            String procedureCallName = child1.getValue().toLowerCase();

            if (symtab.variableExists((Object)procedureCallName)) {
                Object procedureDefNode = symtab.getValue((Object)child1.getValue().toLowerCase());
                SimpleNode defParameters = (SimpleNode)((SimpleNode)procedureDefNode).jjtGetChild(1);

                // *2 or *2-1 because of the semis.
                // check same length
                if (child2.jjtGetNumChildren()*2-1 == defParameters.jjtGetNumChildren()) {

                    // check same types
                    for (int i = 0; i < child2.jjtGetNumChildren(); i++) {

                        if (!((checkingIntegerExpression((SimpleNode)(child2.jjtGetChild(i))) &&
                                defParameters.jjtGetChild(i*2).jjtGetChild(1).toString() == "IntegerDeclarationNode")
                                ||
                                (checkingBooleanExpression((SimpleNode)(child2.jjtGetChild(i))) &&
                                        defParameters.jjtGetChild(i*2).jjtGetChild(1).toString() == "BooleanDeclarationNode")
                                ||
                                child2.jjtGetChild(i).toString() == "Identifier"))
                        {
                            throw new ProcedureCallStatementException("The procedure "
                                    + procedureCallName + " at line " + line + " and column " + col +
                            " you have called has the wrong parameters type.");
                        }
                    }

                    symtab.levelUp();
                    symtab.put(new String("procedure "+child1.getValue().toLowerCase()),BLOCK,child1.getValue().toLowerCase());

                    // do them in the symtab.
                    for (int i = 0; i < child2.jjtGetNumChildren(); i++) {
                        String varName = ((SimpleNode)(defParameters.jjtGetChild(i*2).jjtGetChild(0))).getValue().toLowerCase();

                        if (checkingIntegerExpression((SimpleNode)(child2.jjtGetChild(i)))) {
                            Integer value = (Integer) (child2.jjtGetChild(i)).jjtAccept(this,INTEGER);
                            symtab.put(varName,INTEGER,value.toString());
                        }
                        else if (checkingBooleanExpression((SimpleNode)(child2.jjtGetChild(i)))) {
                            Boolean value = (Boolean) (child2.jjtGetChild(i)).jjtAccept(this,BOOLEAN);
                            symtab.put(varName,BOOLEAN,value.toString());
                        }
                        else if (child2.jjtGetChild(i).toString() == "Identifier") {
                            if (defParameters.jjtGetChild(i*2).jjtGetChild(1).toString() == "IntegerDeclarationNode") {
                                Integer value = (Integer)child2.jjtGetChild(i).jjtAccept(this,INTEGER);
                                symtab.put(varName,INTEGER,value.toString());
                            }
                            else if (defParameters.jjtGetChild(i*2).jjtGetChild(1).toString() == "BooleanDeclarationNode") {
                                Boolean value = (Boolean)child2.jjtGetChild(i).jjtAccept(this,BOOLEAN);
                                symtab.put(varName,INTEGER,value.toString());
                            }
                            else {
                                throw new ProcedureCallStatementException("Something went wrong here." +
                                " ProcedureCallStatementParameters.");
                            }
                        }
                        else {
                            throw new ProcedureCallStatementException("The procedure"
                                    + procedureCallName + " at line " + line + " and column " + col +
                            " has wrong type of parameter(s).");
                        }
                    }

                    ((SimpleNode)procedureDefNode).jjtAccept(this,data);

                    symtab.levelDown();
                }

                else if (((SimpleNode)procedureDefNode).jjtGetNumChildren() == 3) {
                    throw new ProcedureCallStatementException("The procedure "
                            + procedureCallName + " at line " + line + " and column " + col +
                    " has no parameters, but you call it some!");
                }

                else {
                    throw new ProcedureCallStatementException("The procedure "
                            + procedureCallName + " at line " + line + " and column " + col +
                            " you have called has not the same length of" +
                    " parameters with the procedure you have defined.");
                }
            }

            else {
                throw new ProcedureCallStatementException("The definition of procedure "
                        + procedureCallName + " at line " + line + " and column " + col +
                " does not exist or is not visible!");
            }
        }

        else {
            throw new WrongNumberOfChildrenException("ProcedureCallStatement");
        }

        return data;

    }

    public Object visit(ASTProcedureCallStatementParameters node, Object data) {
        return data;
    }

    /**
     * WhileStatement has 2 children. Check the 1st child, if true then execute
     * the 2nd child and execute this node again.
     */
    public Object visit(ASTWhileStatement node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0)); // BooleanExpression
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1)); // SequenceOfStatements.

            if (checkingBooleanExpression(child1)) {
                int countLoop = 0;
                handleBreakpoint(node); //BREAKPOINT
                while (((Boolean)(child1.jjtAccept(this,BOOLEAN))).booleanValue()) {
                    try {
                        child2.jjtAccept(this,data);
                        countLoop++;
                        if (countLoop % 10000000 == 0) {
                            statusBar.setText("You might have an infinite loop here.");
                        }
                        if (countLoop == 50000000) {
                            throw new InterpreterAbortException(
                                    "Execution aborted because of infinite loop");
                        }
                    } catch (ExitStatementException e) {
                        break;
                    }
                    handleBreakpoint(node); //BREAKPOINT
                }

            }
            else {
                int line = node.getLine();
                int col = node.getColumn();
                throw new AdaLogoSyntaxSemanticException(
                        "The boolean expression in the while-statement " +
                        "at line " + line + " and column " + col +
                        " is not correct.");
            }

        }
        else {
            throw new WrongNumberOfChildrenException("WhileStatement");
        }
        return data;
    }

    //-------------------------------------------------------------------------
    // identifier
    //-------------------------------------------------------------------------

    /**
     * Look in the symtab and return.
     */
    public Object visit(ASTIdentifier node, Object data) {
        if ( (symtab.getType(node.getValue().toLowerCase())) == INTEGER ) {
            return new Integer(symtab.getValue(node.getValue().toLowerCase()));
        }
        else if (symtab.getType(node.getValue().toLowerCase()) == BOOLEAN) {
            if (data == BOOLEAN) {
                return new Boolean(symtab.getValue(node.getValue().toLowerCase()));
            }
            else {
                int line = node.getLine();
                int col = node.getColumn();
                String varName = node.getValue();
                throw new IdentifierException("Sorry! The variable "+ varName +
                        " at line " + line + " and column " + col +
                " is an boolean, but stands at a wrong place with a wrong context.");
            }
        }
        else {
            int line = node.getLine();
            int col = node.getColumn();
            String varName = node.getValue();
            throw new IdentifierException("Sorry! The variable " + varName + " at line " +
                    line + " and column " + col + " exists, but it has " +
                    "the " + symtab.getType(node.getValue().toLowerCase()) + " type");
        }
    }

    //-------------------------------------------------------------------------
    // BEGIN of BooleanExpression
    //-------------------------------------------------------------------------

    /**
     * OrNode has 2 children. Go down and operate Or to the 2 children.
     */
    public Object visit(ASTOrNode node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));

            Object data1 = child1.jjtAccept(this,BOOLEAN);
            Object data2 = child2.jjtAccept(this,BOOLEAN);

            return new Boolean(((Boolean)data1).booleanValue() |
                    ((Boolean)data2).booleanValue());
        }
        else {
            throw new WrongNumberOfChildrenException("OrNode");
        }
    }

    /**
     * OrNode has 2 children. Go down and operate And to the 2 children.
     */
    public Object visit(ASTAndNode node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));

            Object data1 = child1.jjtAccept(this,BOOLEAN);
            Object data2 = child2.jjtAccept(this,BOOLEAN);

            return new Boolean(((Boolean)data1).booleanValue() &
                    ((Boolean)data2).booleanValue());
        }
        else {
            throw new WrongNumberOfChildrenException("AndNode");
        }
    }

    /**
     * NotNode has 1 child. Go down and return the reverse result.
     */
    public Object visit(ASTNotNode node, Object data) {
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            Object data1 = child1.jjtAccept(this,BOOLEAN);
            boolean result = ((Boolean)data1).booleanValue();
            result = !result;
            return new Boolean(result);
        }
        else {
            throw new WrongNumberOfChildrenException("NotNode");
        }
    }

    /**
     * turn back true
     */
    public Object visit(ASTTrueNode node, Object data) {
        return new Boolean(true);
    }

    /**
     * turn back false
     */
    public Object visit(ASTFalseNode node, Object data) {
        return new Boolean(false);
    }


    //-------------------------------------------------------------------------
    // Begin of IntegerExpression
    //-------------------------------------------------------------------------

    /**
     * Take the 1st and then 2nd child and depends on the operator calculates the
     * result of the 2 children.
     */
    private Integer helpIntegerExpressionOperation(
            SimpleNode child1, SimpleNode child2, char operator)
    {
        Object data1 = child1.jjtAccept(this,INTEGER);
        Object data2 = child2.jjtAccept(this,INTEGER);

        int result;

        switch (operator) {
        case '+':
            return new Integer(((Integer)data1).intValue() + ((Integer)data2).intValue());
        case '-':
            return new Integer(((Integer)data1).intValue() - ((Integer)data2).intValue());
        case '*':
            return new Integer(((Integer)data1).intValue() * ((Integer)data2).intValue());
        case '/':
            return new Integer(((Integer)data1).intValue() / ((Integer)data2).intValue());
            // MODULO
        case 'm':
            result = ((Integer)data1).intValue() % ((Integer)data2).intValue();
            if (((Integer)data1).intValue() < 0 & ((Integer)data2).intValue() > 0 & result != 0) {
                result = result + ((Integer)data2).intValue();
            }
            if (((Integer)data1).intValue() > 0 & ((Integer)data2).intValue() < 0 & result != 0) {
                result = result + ((Integer)data2).intValue();
            }
            break;
            // REMAINDER
            // http://en.wikipedia.org/wiki/Remainder
            // % is rem in ada!!!
        case 'r':
            return new Integer(((Integer)data1).intValue() % ((Integer)data2).intValue());
        default:
            throw new RuntimeException("helpIntegerExpressionOperation got a " +
            "wrong char operation");
        }
        return new Integer(result);
    }

    /**
     * Take the 1st and the 2nd child and add
     */
    public Object visit(ASTAdditionNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'+');
        }
        else {
            throw new WrongNumberOfChildrenException("AdditionNode");
        }
    }

    /**
     * Take the 1st and the 2nd child and sub
     */
    public Object visit(ASTSubtractionNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'-');
        }
        else {
            throw new WrongNumberOfChildrenException("SubtractionNode");
        }
    }

    /**
     * Take the 1st and the 2nd child and mult
     */
    public Object visit(ASTMultiplicationNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'*');
        }
        else {
            throw new WrongNumberOfChildrenException("MultiplicationNode");
        }
    }

    /**
     * Take the 1st and the 2nd child and div
     */
    public Object visit(ASTDivisionNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'/');
        }
        else {
            throw new WrongNumberOfChildrenException("DivisionNode");
        }
    }


    /**
     * Take the 1st and the 2nd child and mod
     */
    public Object visit(ASTModNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'m');
        }
        else {
            throw new WrongNumberOfChildrenException("ModNode");
        }
    }

    /**
     * Take the 1st and the 2nd child and rem
     */
    public Object visit(ASTRemNode node, Object data) {

        if (node.jjtGetNumChildren() == 2) {
            return helpIntegerExpressionOperation(
                    (SimpleNode)(node.jjtGetChild(0)),
                    (SimpleNode)(node.jjtGetChild(1)),'r');
        }
        else {
            throw new WrongNumberOfChildrenException("RemNode");
        }
    }

    /**
     * Take the 1st child and reverse.
     */
    public Object visit(ASTDashNode node, Object data) {

        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            Object data1 = child1.jjtAccept(this,INTEGER);
            int result = ((Integer)data1).intValue() * -1;
            return new Integer(result);
        }
        else {
            throw new WrongNumberOfChildrenException("Dash");
        }
    }


    /**
     * Look in the SimpleNode and return getValue()
     */
    public Object visit(ASTIntegerLiteral node, Object data) {
        return new Integer(node.getValue());
    }

    //-------------------------------------------------------------------------
    // BEGIN of RelationalExpression etc.
    //-------------------------------------------------------------------------

    /**
     * Takes the 1st and 2nd children and comparing depends on the operator.
     * @return Boolean
     */
    private Boolean helpRelationalExpression(SimpleNode node, char operator) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));

            int data1 = ((Integer)(child1.jjtAccept(this,null))).intValue();
            int data2 = ((Integer)(child2.jjtAccept(this,null))).intValue();

            switch (operator) {
            case '>':
                if (data1 > data2) { return new Boolean(true); }
                else { return new Boolean(false); }
                // GreaterEqual
            case 'g':
                if (data1 >= data2) { return new Boolean(true); }
                else { return new Boolean(false); }
            case '<':
                if (data1 < data2) { return new Boolean(true); }
                else { return new Boolean(false); }
                // LessEqual
            case 'l':
                if (data1 <= data2) { return new Boolean(true); }
                else { return new Boolean(false); }
            case '=':
                if (data1 == data2) { return new Boolean(true); }
                else { return new Boolean(false); }
                // NotEqual
            case '/':
                if (data1 != data2) { return new Boolean(true); }
                else { return new Boolean(false); }
            default:
                throw new RuntimeException("The operator of helpRelationExpression is not valid!");
            }
        }
        else {
            throw new WrongNumberOfChildrenException("RelationalExpression");
        }
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTGreaterThanNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'>');
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTGreaterEqualNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'g');
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTLessThanNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'<');
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTLessEqualNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'l');
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTEqualNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'=');
    }

    /**
     * done by helpRelationalExpression
     */
    public Object visit(ASTNotEqualNode node, Object data) {
        return helpRelationalExpression((SimpleNode)node,'/');
    }

    //-------------------------------------------------------------------------
    // BEGIN of Identifiers
    // This should be ok? the parent nodes do the work....
    //-------------------------------------------------------------------------

    /**
     * "DummyNode" - the parent nodes do the work....
     */
    public Object visit(ASTProcedureDeclarationIdentifier node, Object data) {
        return data;
    }

    /**
     * "DummyNode" - the parent nodes do the work....
     */
    public Object visit(ASTVariableDeclarationIdentifier node, Object data) {
        return data;
    }

    /**
     * "DummyNode" - the parent nodes do the work....
     */
    public Object visit(ASTProcedureCallStatementIdentifier node, Object data) {
        return data;
    }

    /**
     * "DummyNode" - the parent nodes do the work....
     */
    public Object visit(ASTBooleanDeclarationNode node, Object data) {
        return data;
    }

    /**
     * "DummyNode" - the parent nodes do the work....
     */
    public Object visit(ASTIntegerDeclarationNode node, Object data) {
        return data;
    }

    /**
     * This function becomes 2 IntegerExpression and turn the smaller one
     * back
     */
    public Object visit(ASTMinExpression node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2= (SimpleNode)(node.jjtGetChild(1));
            Integer data1 = (Integer)(child1.jjtAccept(this,data));
            Integer data2 = (Integer)(child2.jjtAccept(this,data));
            if (data1.intValue() < data2.intValue()) {
                return data1;
            }
            else {
                return data2;
            }
        }
        else {
            throw new WrongNumberOfChildrenException("MinExpression");
        }
    }

    /**
     * This function becomes 2 IntegerExpression and turn the bigger one
     * back
     */
    public Object visit(ASTMaxExpression node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2= (SimpleNode)(node.jjtGetChild(1));
            Integer data1 = (Integer)(child1.jjtAccept(this,data));
            Integer data2 = (Integer)(child2.jjtAccept(this,data));
            if (data1.intValue() > data2.intValue()) {
                return data1;
            }
            else {
                return data2;
            }
        }
        else {
            throw new WrongNumberOfChildrenException("MaxExpression");
        }
    }

    public Object visit(ASTGetDirExpression node, Object data) {
        if (node.jjtGetNumChildren() == 0) {
            return new Integer((int)(turtle.getDirection()));
        }
        else {
            throw new WrongNumberOfChildrenException("GetDirExpression");
        }
    }

    /**
     * Turn back the x-position of the turtle
     */
    public Object visit(ASTGetXExpression node, Object data) {
        if (node.jjtGetNumChildren() == 0) {
            return new Integer((int)(turtle.getPosition().getX()));
        }
        else {
            throw new WrongNumberOfChildrenException("GetXExpression");
        }
    }

    /**
     * Turn back the y-position of the turtle
     */
    public Object visit(ASTGetYExpression node, Object data) {
        if (node.jjtGetNumChildren() == 0) {
            return new Integer((int)(turtle.getPosition().getY()));
        }
        else {
            throw new WrongNumberOfChildrenException("GetYExpression");
        }
    }

    /**
     * RandomExpression get 2 IntegerExpression and return a
     * value between this to ranges, inclusive the smaller range,
     * exclusive the bigger range.
     * It does not matter which range is bigger.
     */
    public Object visit(ASTRandomExpression node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));
            SimpleNode child2= (SimpleNode)(node.jjtGetChild(1));
            Integer data1 = (Integer)(child1.jjtAccept(this,data));
            Integer data2 = (Integer)(child2.jjtAccept(this,data));

            int diff = Math.abs(data1.intValue() - data2.intValue());

            int result;
            if (data1.intValue() < data2.intValue()) {
                result = data1.intValue();
            }
            else {
                result = data2.intValue();
            }

            result = result + random.nextInt(diff);

            return new Integer(result);
        }
        else {
            throw new WrongNumberOfChildrenException("RandomExpression");
        }
    }

    /**
     * This should be ok. Do nothing.
     */
    public Object visit(ASTSemi node, Object data) {
        handleBreakpoint(node);
        return null;
    }

    /**
     * this will wait if there is a break point at that location
     */
    private void handleBreakpoint(SimpleNode node) {

        if (master.isStopRequested()) {
            throw new InterpreterAbortException("Request stop by user.");
        }

        //dirty hack! dirty dirty dirty hack!
        if (!execute) return;

        pc.increment();

        if (master.isBreak(node.getLine())) {
            master.visitorWaiting(node.getLine());
            //here visitor is waiting for user interaction
            master.visitorRunning();
        }

    }

}
