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

import adalogo.Engine;
import adalogo.gui.Console;
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

/**
 * Dumps all nodes.
 * note: all methods are more or less identical.
 */
public class DumpVisitor implements LangVisitor {

    VisitorMaster master;

    Engine engine;
    Console console;

    StringBuffer output;

    public DumpVisitor(Engine engine, VisitorMaster master, SimpleNode node) {

        this.engine = engine;
        this.console = engine.getConsole();

        this.master = master;

        output = new StringBuffer();

        node.jjtAccept(this, new StringBuffer());

        console.appendDebug(output.toString());

    }

    public Object visit(SimpleNode node, Object data) {
        //console.appendDebug((String)data + node + "SimpleNode?");
        output.append((StringBuffer)data);
        output.append(node);
        output.append("SimpleNode?");
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTSemi node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTDeclaration node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureDeclaration node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(">"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureDeclarationIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureDeclarationParameters node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTVariableDeclaration node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(":"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTBooleanDeclarationNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTIntegerDeclarationNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTVariableDeclarationIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTSequenceOfStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTForwardStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTJumpToStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTPenDownStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTPenUpStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTResetTurtleStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTTurnStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTTurnToStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("@"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTAssignmentStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("="));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTAssignmentIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTExitStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTForStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("#"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTForIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTForReverse node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTIfStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("?"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTElsifPart node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTElsePart node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTNullStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTLoopStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("#"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureCallStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("-"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureCallStatementIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTProcedureCallStatementParameters node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append("#"));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTOrNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTAndNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTNotNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTEqualNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTNotEqualNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTGreaterThanNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTGreaterEqualNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTLessThanNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTLessEqualNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTTrueNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTFalseNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTAdditionNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTSubtractionNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTMultiplicationNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTDivisionNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTModNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTRemNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTDashNode node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTIntegerLiteral node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTMinExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTMaxExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTGetDirExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTGetXExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTGetYExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTRandomExpression node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTNewLineStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTPutLineStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTPutStatement node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTStringLiteral node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

    public Object visit(ASTIdentifier node, Object data) {
        //console.appendDebug((String)data + node);
        output.append((StringBuffer)data);
        output.append(node);
        output.append("\n");
        node.childrenAccept(this, ((StringBuffer)data).append(" "));
        ((StringBuffer)data).deleteCharAt(((StringBuffer)data).length()-1);
        return null;
    }

}
