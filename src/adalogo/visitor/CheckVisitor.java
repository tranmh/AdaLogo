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
import adalogo.lang.ASTArrayDeclaration;
import adalogo.lang.ASTArrayDeclarationOthers;
import adalogo.lang.ASTAssignmentStatement;
import adalogo.lang.ASTBody;
import adalogo.lang.ASTBoolean;
import adalogo.lang.ASTCallParameters;
import adalogo.lang.ASTCaseStatement;
import adalogo.lang.ASTCaseValues;
import adalogo.lang.ASTCharacter;
import adalogo.lang.ASTCharacterLiteral;
import adalogo.lang.ASTCompilationUnit;
import adalogo.lang.ASTDashNode;
import adalogo.lang.ASTDeclaration;
import adalogo.lang.ASTDeclarationParameters;
import adalogo.lang.ASTDeclareBlock;
import adalogo.lang.ASTDiscreteDeclaration;
import adalogo.lang.ASTDivisionNode;
import adalogo.lang.ASTElsePart;
import adalogo.lang.ASTElsifPart;
import adalogo.lang.ASTEqualNode;
import adalogo.lang.ASTExitStatement;
import adalogo.lang.ASTFalseNode;
import adalogo.lang.ASTFloat;
import adalogo.lang.ASTFloatLiteral;
import adalogo.lang.ASTForReverse;
import adalogo.lang.ASTForStatement;
import adalogo.lang.ASTFunctionCallExpression;
import adalogo.lang.ASTFunctionDeclaration;
import adalogo.lang.ASTGreaterEqualNode;
import adalogo.lang.ASTGreaterThanNode;
import adalogo.lang.ASTIdentifier;
import adalogo.lang.ASTIfStatement;
import adalogo.lang.ASTIn;
import adalogo.lang.ASTInteger;
import adalogo.lang.ASTIntegerLiteral;
import adalogo.lang.ASTLessEqualNode;
import adalogo.lang.ASTLessThanNode;
import adalogo.lang.ASTLoopStatement;
import adalogo.lang.ASTModNode;
import adalogo.lang.ASTMultiplicationNode;
import adalogo.lang.ASTNotEqualNode;
import adalogo.lang.ASTNotNode;
import adalogo.lang.ASTNullStatement;
import adalogo.lang.ASTOrNode;
import adalogo.lang.ASTOut;
import adalogo.lang.ASTPackageDeclaration;
import adalogo.lang.ASTParameter;
import adalogo.lang.ASTParameterMode;
import adalogo.lang.ASTPointerDeclaration;
import adalogo.lang.ASTProcedureCallStatement;
import adalogo.lang.ASTProcedureDeclaration;
import adalogo.lang.ASTRecordDeclaration;
import adalogo.lang.ASTRemNode;
import adalogo.lang.ASTReturnStatement;
import adalogo.lang.ASTSemi;
import adalogo.lang.ASTSequenceOfStatement;
import adalogo.lang.ASTString;
import adalogo.lang.ASTStringLiteral;
import adalogo.lang.ASTSubtractionNode;
import adalogo.lang.ASTTrueNode;
import adalogo.lang.ASTType;
import adalogo.lang.ASTTypeDeclaration;
import adalogo.lang.ASTTypeExpression;
import adalogo.lang.ASTUseClause;
import adalogo.lang.ASTVariableDeclaration;
import adalogo.lang.ASTWhileStatement;
import adalogo.lang.ASTWithClause;
import adalogo.lang.ASTXorNode;
import adalogo.lang.LangVisitor;
import adalogo.lang.SimpleNode;
import adalogo.visitor.InterpreterVisitor.ProcedureCallStatementException;

// TODO
// collect AdaLogoSyntaxSemanticException and put them all out at once
// not finished!!! what do we do with WrongIntegerExpression and others...?

// THE BIG THINGS
// DEBUG and TESTING
// functioncallstatement
// procedurecallstatement
// assignmentstatement

// FIXME 1.0 <op> 1 is available
// <op> can be =, >=, > etc.

// FIXME: this is available: 1.0 mod 1 
// in Ada mod and rem are only available in between integer and integer

public class CheckVisitor implements LangVisitor {
	
    private Engine engine;
    private StatusBar statusBar;
    private Console console;
    private Turtle turtle;
	
    private AdaLogoRoutine adaLogoRoutine;
    
    /**
     * the thread which controls this visitor
     */
    private VisitorMaster master;

    private SymbolTable symtab;
    private ProgramCounter pc;
    
    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    private static final String FLOAT = "float";
    private static final String PROCEDURE = "procedure";
    private static final String FUNCTION = "function";
    private static final String BLOCK = "block";
    
    private static final String ADALOGO = "adalogo";
    
    private static final String PROTECTED_BOOLEAN = "#boolean";
    private static final String PROTECTED_INTEGER = "#integer";
    private static final String PROTECTED_FLOAT = "#float";
    private static final String PROTECTED_PROCEDURE = "#procedure"; 
    private static final String PROTECTED_FUNCTION = "#function";
	private static final String ARRAY = "array"; 
    
    private Random random = new Random();
    
    // collect the wrong syntax and semantic exception and throw them
    // after execute CompilationUnit.
    private boolean fault = false;
    private String faultNotice = "Execution aborted.\n";
    private int countFault = 0;

    public CheckVisitor(Engine engine, VisitorMaster master, SimpleNode node) {
        this.engine = engine;
        this.statusBar = engine.getStatusBar();
        this.console = engine.getConsole();
        this.turtle = engine.getTurtle();

        symtab = new SymbolTable();
        pc = new ProgramCounter();
        
        adaLogoRoutine = new AdaLogoRoutine(engine, master, symtab);
        
        this.master = master;
        
        
        node.jjtAccept(this, "");
    }
    
    /*************************************************************************/
    // EXCEPTIONS
    /*************************************************************************/
    /**
     * WrongNumberOfChildrenException
     * nothing special here.
     */
    public class WrongNumberOfChildrenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WrongNumberOfChildrenException(String message) {
            super(message);
        }
    }
    
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
     * NotSupportYetException
     * nothing special here.
     */
    public class NotSupportYetException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public NotSupportYetException(String message) {
            super(message);
        }
    }
    
    /**
     * InterpreterAbortException
     * nothing special here.
     */
    public class InterpreterAbortException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InterpreterAbortException(String message) {
            super(message);
        }
    }
    
    /**
     * AdaLogoSyntaxSemanticException
     * nothing special here.
     */
    public class AdaLogoSyntaxSemanticException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AdaLogoSyntaxSemanticException(String message) {
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
     * WrongFloatExpressionException
     * nothing special here.
     */
    public class WrongFloatExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WrongFloatExpressionException(String message) {
            super(message);
        }
    }

    
    /*************************************************************************/
    // VISITS
    /*************************************************************************/
    /**
     * Do nothing, this should never have been called.
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println("visit SimpleNode. this should never happen");
        data = node.childrenAccept(this, data);
        return data;
    }

	public Object visit(ASTCompilationUnit node, Object data) throws AdaLogoSyntaxSemanticException {
        checkingExitStatementAtWrongPlace((SimpleNode)node);
		node.childrenAccept(this,data);
		if (fault) {
			throw new AdaLogoSyntaxSemanticException("There was/were " 
					+ countFault + " error(s) found.\n" + faultNotice);
		}
		return null;
	}

	public Object visit(ASTWithClause node, Object data) {
		int numChildren = node.jjtGetNumChildren();
		if (numChildren == 2 && ((SimpleNode) (node.jjtGetChild(0))).getValue().toLowerCase().equals(ADALOGO) ) {
		}
		else {
			console.appendError("WARNING: AdaLogo does not support the package feature yet!");
		}
		return null;
	}

	public Object visit(ASTUseClause node, Object data) {
		int numChildren = node.jjtGetNumChildren();
		if (numChildren == 2 && ((SimpleNode) (node.jjtGetChild(0))).getValue().toLowerCase().equals(ADALOGO) ) {
		}
		else {
			console.appendError("WARNING: AdaLogo does not support the package feature yet!");
		}
		return null;
	}

	public Object visit(ASTSemi node, Object data) {
		// TODO remove this later
		handleBreakpoint(node);
		return null;
	}

	public Object visit(ASTDeclaration node, Object data) {
		
		checkingSignatureWithoutBody((SimpleNode)node);
		
		checkingVariableHasSameNameWithProcedureOrFunction((SimpleNode)node);
		
		node.childrenAccept(this,data);
		return null;
	}

	// TODO TESTED enough?
	/**
	 * checking procedures and function with a signature,
	 * but later they do not have a declaration with body.
	 * @param node as Declaration
	 */
	private void checkingSignatureWithoutBody(SimpleNode node) {
		SimpleNode declarationNode;
		int numChildren; 
		boolean found;
		String signature;
		SimpleNode compareNode;
		int numChildrenCN;
		
		for (int i = 0; i < node.jjtGetNumChildren(); i = i+2) {
			declarationNode = (SimpleNode)node.jjtGetChild(i);
			numChildren = declarationNode.jjtGetNumChildren();
			
			if (declarationNode.toString() == "FunctionDeclaration" && (numChildren == 2 || numChildren == 3)) {
				found = false;
				signature = getSignatureOfFunctionDeclaration(declarationNode);
				
				for (int j = i+2; j < node.jjtGetNumChildren(); j = j+2) {
					compareNode = (SimpleNode)node.jjtGetChild(j);
					numChildrenCN = compareNode.jjtGetNumChildren();
					
					if (compareNode.toString() == "FunctionDeclaration"	&& 
							(numChildrenCN == 4 || numChildrenCN == 5) && 
							signature.equals(getSignatureOfFunctionDeclaration(compareNode)) &&
							compareNode.getAttribute() != "used" &&
							found == false) {
						found = true;
						compareNode.setAttribute("used");
					}
					else if (compareNode.toString() == "FunctionDeclaration"	&& 
							(numChildrenCN == 4 || numChildrenCN == 5) && 
							signature.equals(getSignatureOfFunctionDeclaration(compareNode)) &&
							compareNode.getAttribute() == "used" &&
							found == false) {
			        	appendFaultNotice("There are 2 or more function headers" +
			        			"with the same signature at line " + declarationNode.getLine() + 
			        			" column " + declarationNode.getColumn());
					}
					else if (compareNode.toString() == "FunctionDeclaration" && 
							(numChildrenCN == 4 || numChildrenCN == 5) && 
							signature.equals(getSignatureOfFunctionDeclaration(compareNode)) && 
							found == true) {
			        	appendFaultNotice("There are 2 or more function defitions with body" +
			        			"with the same signature at line " + compareNode.getLine() + 
			        			" column " + compareNode.getColumn());
					}
				}
				
				if (!found) {
		        	appendFaultNotice("The definition of the function header at line "
		        			+ declarationNode.getLine() + " column " 
		        			+ declarationNode.getColumn() + 
		        			" has no function declaration with body with " +
		        			"the same signture in the declaration part.");
				}
			}

			if (declarationNode.toString() == "ProcedureDeclaration" && (numChildren == 1 || numChildren == 2)) {
				found = false;
				signature = getSignatureOfProcedureDeclaration(declarationNode);
				
				for (int j = i+2; j < node.jjtGetNumChildren(); j = j+2) {
					compareNode = (SimpleNode)node.jjtGetChild(j);
					numChildrenCN = compareNode.jjtGetNumChildren();
					
					if (compareNode.toString() == "ProcedureDeclaration" && 
							(numChildrenCN == 3 || numChildrenCN == 4) && 
							signature.equals(getSignatureOfProcedureDeclaration(compareNode)) &&
							compareNode.getAttribute() != "used" &&
							found == false) {
						found = true;
						compareNode.setAttribute("used");
					}
					else if (compareNode.toString() == "ProcedureDeclaration" && 
							(numChildrenCN == 3 || numChildrenCN == 4) && 
							signature.equals(getSignatureOfProcedureDeclaration(compareNode)) &&
							compareNode.getAttribute() == "used" &&
							found == false) {
			        	appendFaultNotice("There are 2 or more procedure headers" +
			        			"with the same signature at line " + 
			        			declarationNode.getLine() + " column " + 
			        			declarationNode.getColumn() + ".");
					}
					else if (compareNode.toString() == "ProcedureDeclaration" && 
							(numChildrenCN == 3 || numChildrenCN == 4) && 
							signature.equals(getSignatureOfProcedureDeclaration(compareNode)) && 
							found == true) {
			        	appendFaultNotice("There are 2 or more procedure defitions with body" +
			        			"with the same signature at line " + compareNode.getLine() + 
			        			" column " + compareNode.getColumn() + ".");
					}
				}
				
				if (!found) {
		        	appendFaultNotice("The definition of the procedure header at line " + 
		        			declarationNode.getLine() + " column " + 
		        			declarationNode.getColumn() +
		        			" has no procedure declaration with body with " +
		        			"the same signture in the declaration part.");
				}
			}
		}
	}
	
	/**
	 * TODO TEST ME
	 * @param node as Declaration
	 */
	private void checkingVariableHasSameNameWithProcedureOrFunction(
			SimpleNode node) {
		int numChildren = node.jjtGetNumChildren();
		SimpleNode childI;
		for (int i = 0; i < numChildren; i++) {
			childI = (SimpleNode) node.jjtGetChild(i);
			if (childI.toString() == "VariableDeclaration") {
				SimpleNode childIdentifier;
				for (int j = 0; j < getNumOfIdentifier(childI); j++) {
					childIdentifier = (SimpleNode) childI.jjtGetChild(j);
					SimpleNode compareNode;
					for (int k = 0; k < numChildren; k++) {
						compareNode = (SimpleNode) node.jjtGetChild(k);
						if ( (compareNode.toString() == "ProcedureDeclaration" 
							 || compareNode.toString() == "FunctionDeclaration")
								&& ((SimpleNode) compareNode.jjtGetChild(0))
										.getValue().toLowerCase().equals(
												childIdentifier.getValue()
														.toLowerCase())) {
							appendFaultNotice("The definition of an variable at line "
									+ childI.getLine()
									+ " has the same name and conflicts"
									+ " with a definition of a function or procedure"
									+ " at line " + compareNode.getLine() + ".");
						}
					}
				}
			}
		}
	}

	// TODO tested enough?
	public Object visit(ASTProcedureDeclaration node, Object data) {
		SimpleNode child0 = (SimpleNode)node.jjtGetChild(0);

		String signature = getSignatureOfProcedureDeclaration((SimpleNode)node);
		
		if (adaLogoRoutine.isProcedureOfAdaLogo(signature)) {
        	appendFaultNotice("The definition of " +
					"the procedure " + child0.getValue() + " at line "
					+ child0.getLine() + ", column " + child0.getColumn() +
					" has a conflict with the definition of AdaLogo procedures.");
		}
		
		// without DeclarationParameter and Declaration and SequenceOfStatement 1
		// without DeclarationParameters, with (Declaration and SequenceOfStatement) 3
	    if (node.jjtGetNumChildren() == 1 || 
	    		node.jjtGetNumChildren() == 3) {
            if (node.jjtGetNumChildren() == 1) {
            	symtab.put(signature,PROCEDURE,null);
            }
            if (node.jjtGetNumChildren() == 3) {
            	if (symtab.variableExists(signature)) {
            		if (symtab.getType(signature) == PROCEDURE) {
                    	symtab.setType(signature,PROTECTED_PROCEDURE);
                    	symtab.setValue(signature,node);
            		}
            		else {
    					appendFaultNotice("The definition of " +
    							"the procedure " + child0.getValue() + " at line "
    							+ child0.getLine() + ", column " + child0.getColumn() +
    							" already exists.");
            		}
				} 
            	else {
                    symtab.put(signature,PROTECTED_PROCEDURE,node);
				}
            	
            }
        } 
		// with DeclarationParameter and without (Declaration and SequenceOfStatement) 2        
	    // with all 4
	    else if (node.jjtGetNumChildren() == 2 || 
        		node.jjtGetNumChildren() == 4) {
            if (node.jjtGetNumChildren() == 2) {
            	symtab.put(signature,PROCEDURE,null);
            }
            if (node.jjtGetNumChildren() == 4) {
            	if (symtab.variableExists(signature)) {
            		if (symtab.getType(signature) == PROCEDURE) {
                    	symtab.setType(signature,PROTECTED_PROCEDURE);
                    	symtab.setValue(signature,node);
            		}
            		else {
            			appendFaultNotice("The definition of " +
    							"the procedure " + child0.getValue() + " at line "
    							+ child0.getLine() + ", column " + child0.getColumn() +
    							" already exists.");
            		}
				} 
            	else {
                    symtab.put(signature,PROTECTED_PROCEDURE,node);
				}
            }
            
        }
        else {
        	console.appendError(new Integer(node.jjtGetNumChildren()).toString());
            throw new WrongNumberOfChildrenException("ProcedureDeclaration");
        }
	    
    	symtab.levelUp();
        symtab.put("procedure " + signature,BLOCK,child0.getValue().toLowerCase());
        node.childrenAccept(this,data);
        if (node.jjtGetNumChildren() == 3 || node.jjtGetNumChildren() == 4 ) {
    		checkingUnreachableCode((SimpleNode)node.jjtGetChild(node.jjtGetNumChildren()-1));
		}
        symtab.levelDown();

        return data;
	}

	public Object visit(ASTPackageDeclaration node, Object data) {
		throw new NotSupportYetException("PackageDeclaration");
	}

	public Object visit(ASTBody node, Object data) {
		throw new NotSupportYetException("PackageDeclaration");
	}

	/**
	 * Do nothing. Identifier has different meaning in different context. 
	 */
	public Object visit(ASTIdentifier node, Object data) {
		return null;
	}

	/**
	 * Do nothing. DeclarationParameters can be the son of 
	 * FunctionDeclaration or the son of ProcedureDeclaration
	 * The ProcedureDeclaraton-node does the stuff. 
	 */
	public Object visit(ASTDeclarationParameters node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Parameter is the son of DeclarationParameters.
	 * The ProcedureDeclaraton-node does the stuff. 
	 */
	public Object visit(ASTParameter node, Object data) {
		return null;
	}

	/**
	 * Do nothing. ParameterMode is the son of Parameter.
	 * The ProcedureDeclaraton-node does the stuff. 
	 */
	public Object visit(ASTParameterMode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. In is the son of ParameterMode
	 * The ProcedureDeclaraton-node does the stuff. 
	 */
	public Object visit(ASTIn node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Out is the son of ParameterMode
	 * The ProcedureDeclaraton-node does the stuff. 
	 */
	public Object visit(ASTOut node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Type has different meaning in different context. 
	 */
	public Object visit(ASTType node, Object data) {
		return null;
	}

	/**
	 * TypeExpression has different meaning in different context.
	 */
	public Object visit(ASTTypeExpression node, Object data) {
		if (node.jjtGetChild(0).toString().equals("CharacterLiteral")) {
			node.setAttribute("CharacterLiteral");
		}
		else if (node.jjtGetChild(0).toString().equals("StringLiteral")) {
			node.setAttribute("StringLiteral");
		}
		else {
			if (checkingBooleanExpression(node)) {
				node.setAttribute(BOOLEAN);
			} 
			else if (checkingIntegerExpression(node)) {
				node.setAttribute(INTEGER);
			}
			else if (checkingFloatExpression(node)) {
				node.setAttribute(FLOAT);
			}
			else { // TODO write a better exception-message.
				appendFaultNotice("This is not a valid Expression.");
			}
		}
		return null;
	}

	/**
	 * Do nothing. Boolean is the son of Type
	 */
	public Object visit(ASTBoolean node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Character is the son of Type
	 */
	public Object visit(ASTCharacter node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Float is the son of Type
	 */
	public Object visit(ASTFloat node, Object data) {
		return null;
	}

	/**
	 * Do nothing. Integer is the son of Type
	 */
	public Object visit(ASTInteger node, Object data) {
		return null;
	}

	/**
	 * Do nothing. String is the son of Type
	 */
	public Object visit(ASTString node, Object data) {
		return null;
	}

	// TODO tested enough?
	public Object visit(ASTFunctionDeclaration node, Object data) {
		SimpleNode child0 = (SimpleNode)node.jjtGetChild(0);
		
		String signature = getSignatureOfFunctionDeclaration((SimpleNode)node);
		
		if (adaLogoRoutine.isFunctionOfAdaLogo(signature)) {
			appendFaultNotice("The definition of " +
					"the function " + child0.getValue() + " at line "
					+ child0.getLine() + ", column " + child0.getColumn() +
					" has a conflict with the definition of AdaLogo functions.");
		}
		
		// without DeclarationParameter and Declaration and SequenceOfStatement 2
		// without DeclarationParameters, with (Declaration and SequenceOfStatement) 4
	    if (node.jjtGetNumChildren() == 2 || 
	    		node.jjtGetNumChildren() == 4) {
            if (node.jjtGetNumChildren() == 2) {
            	symtab.put(signature,FUNCTION,null);
            }
            if (node.jjtGetNumChildren() == 4) {
            	if (symtab.variableExists(signature)) {
            		if (symtab.getType(signature) == FUNCTION) {
                    	symtab.setType(signature,PROTECTED_FUNCTION);
                    	symtab.setValue(signature,node);
            		}
            		else {
    					appendFaultNotice("The definition of " +
    							"the function " + child0.getValue() + " at line "
    							+ child0.getLine() + ", column " + child0.getColumn() +
    							" already exists.");
            		}
				} 
            	else {
                    symtab.put(signature,PROTECTED_FUNCTION,node);
				}
            }
        } 
		// with DeclarationParameter and without (Declaration and SequenceOfStatement) 3        
	    // with all 5
	    else if (node.jjtGetNumChildren() == 3 || 
        		node.jjtGetNumChildren() == 5) {
            if (node.jjtGetNumChildren() == 3) {
                symtab.put(signature,FUNCTION,null);
            }
            if (node.jjtGetNumChildren() == 5) {
            	if (symtab.variableExists(signature)) {
            		if (symtab.getType(signature) == FUNCTION) {
                    	symtab.setType(signature,PROTECTED_FUNCTION);
                    	symtab.setValue(signature,node);
            		}
            		else {
    					appendFaultNotice("The definition of " +
    							"the function " + child0.getValue() + " at line "
    							+ child0.getLine() + ", column " + child0.getColumn() +
    							" already exists.");
            		}
				} 
            	else {
                    symtab.put(signature,PROTECTED_FUNCTION,node);
				}
            }
        }
        else {
        	console.appendError(new Integer(node.jjtGetNumChildren()).toString());
            throw new WrongNumberOfChildrenException("FunctionDeclaration");
        }
	    
    	symtab.levelUp();
        symtab.put("function " + signature,BLOCK,child0.getValue().toLowerCase());
        node.childrenAccept(this,data);
        if (node.jjtGetNumChildren() == 4 || node.jjtGetNumChildren() == 5 ) {
    		checkingUnreachableCode((SimpleNode)node.jjtGetChild(node.jjtGetNumChildren()-1));
    		checkingReturnStatementOfFunction((SimpleNode)node.jjtGetChild(node.jjtGetNumChildren()-1), 
    				node.jjtGetChild(node.jjtGetNumChildren()-3).jjtGetChild(0).toString());
		}
        symtab.levelDown();

        return data;
	}

	// TODO TEST ME
	public Object visit(ASTVariableDeclaration node, Object data) {
		int numOfIdentifier = getNumOfIdentifier(node);
		int numOfChildren = node.jjtGetNumChildren();
		
        // VariableDeclaration without Assignment, value is random
        if (node.jjtGetChild(numOfChildren-1).toString() == "Type") {
            if (node.jjtGetChild(numOfChildren-1).jjtGetChild(0).toString() == "Boolean") {
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put(((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),
                            BOOLEAN,(new Boolean(random.nextBoolean())).toString());
				}
            }
            else if (node.jjtGetChild(numOfChildren-1).jjtGetChild(0).toString() == "Integer") {
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),INTEGER,
                            (new Integer(random.nextInt())).toString() );
				}
            }
            else if (node.jjtGetChild(numOfChildren-1).jjtGetChild(0).toString() == "Float") {
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),FLOAT,
                            (new Float(random.nextFloat())).toString() );
				}
            }
            else {
            	throw new NotSupportYetException("Variable declaration which not declares float, integer or boolean");
            }
        }

        // Declaration with Assignment
        else if (node.jjtGetChild(numOfChildren-1).toString() == "TypeExpression") {
            if (node.jjtGetChild(numOfChildren-2).jjtGetChild(0).toString() == "Boolean") {
                SimpleNode childTypeExpression = (SimpleNode)(node.jjtGetChild(numOfChildren-1));

                if (!checkingBooleanExpression(childTypeExpression)) {
                    int line = node.getLine();
                    throw new WrongBooleanExpressionException("Sorry. Your boolean expression " +
                    		"at line "+ line + " is not correct!");
                }

                Boolean expressionResult = (Boolean)(childTypeExpression.jjtAccept(this,data));
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),BOOLEAN,expressionResult.toString() );
				}
            }
            else if (node.jjtGetChild(numOfChildren-2).jjtGetChild(0).toString() == "Integer") {
                SimpleNode childTypeExpression = (SimpleNode)(node.jjtGetChild(numOfChildren-1));

                if (!checkingIntegerExpression(childTypeExpression)) {
                    int line = node.getLine();
                    throw new WrongIntegerExpressionException("Sorry. Your integer expression " +
                    		"at line "+ line + " is not correct!");
                }

                Integer expressionResult = (Integer)(childTypeExpression.jjtAccept(this,data));
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),INTEGER,expressionResult.toString() );
				}
            }
            else if (node.jjtGetChild(numOfChildren-2).jjtGetChild(0).toString() == "Float") {
                SimpleNode childTypeExpression = (SimpleNode)(node.jjtGetChild(numOfChildren-1));

                if (!checkingFloatExpression(childTypeExpression)) {
                    int line = node.getLine();
                    throw new WrongFloatExpressionException("Sorry. Your float expression " +
                    		"at line "+ line + " is not correct!");
                }

                Float expressionResult = (Float)(childTypeExpression.jjtAccept(this,data));
            	for (int i = 0; i < numOfIdentifier; i++) {
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),FLOAT,expressionResult.toString() );
				}
            }
            else {
                throw new RuntimeException("VariableDeclaration with Assignment has a wrong child.");
            }
        }
        // TODO TEST ME
        else if (node.jjtGetChild(numOfChildren-1).toString() == "ArrayDeclaration") {
        	SimpleNode arrayDecNode = (SimpleNode)node.jjtGetChild(numOfChildren-1);
        	SimpleNode lastChildADN = (SimpleNode)arrayDecNode.jjtGetChild(arrayDecNode.jjtGetNumChildren()-1);
        	
        	// definition of array without others.
        	if (lastChildADN.toString() == "Type") {
        		SimpleNode childType = lastChildADN;
        		
        		int dim = (arrayDecNode.jjtGetNumChildren()-1) / 2;
        		String arrayType = childType.jjtGetChild(0).toString().toLowerCase();
        		int bound[] = new int[dim * 2];
        		for (int i = 0; i < bound.length; i++) {
					checkingIntegerExpression((SimpleNode) arrayDecNode.jjtGetChild(i));
					bound[i] = 0;
				}
            	for (int i = 0; i < numOfIdentifier; i++) {
    				Array arr = new Array(dim,arrayType,bound);
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),ARRAY,arr );
				}
			} 

        	// definition of array with others.
        	else if (lastChildADN.toString() == "ArrayDeclarationOthers") {
        		SimpleNode childType = (SimpleNode)arrayDecNode.jjtGetChild(arrayDecNode.jjtGetNumChildren()-2);
        		SimpleNode childOthers = (SimpleNode) arrayDecNode.jjtGetChild(arrayDecNode.jjtGetNumChildren()-1);
        		
        		int dim = (arrayDecNode.jjtGetNumChildren()-2) / 2;
        		String arrayType = childType.jjtGetChild(0).toString().toLowerCase();
        		int bound[] = new int[dim * 2];
        		for (int i = 0; i < bound.length; i++) {
					checkingIntegerExpression((SimpleNode) arrayDecNode.jjtGetChild(i));
					bound[i] = 0;
				}
        		if (getDepthOfArrayDeclarationOthers(childOthers) != dim) {
					appendFaultNotice("The array definition at line " 
							+ node.getLine() + " column "
							+ node.getColumn() + " has wrong dimension of others.");
				}
        		SimpleNode arrayValue = getNodeOfArrayDeclarationOthers(childOthers);
        		if (arrayType.equals(FLOAT)) {
					if (!checkingFloatExpression(arrayValue)) {
	                    appendFaultNotice("Sorry. Your float expression " +
	                    		"at line "+ node.getLine() + " is not correct!");
					}
				} 
        		else if (arrayType.equals(INTEGER)) {
					if (!checkingIntegerExpression(arrayValue)) {
	                    appendFaultNotice("Sorry. Your integer expression " +
	                    		"at line "+ node.getLine() + " is not correct!");
					}
				} 
        		else if (arrayType.equals(BOOLEAN)) {
					if (!checkingBooleanExpression(arrayValue)) {
	                    appendFaultNotice("Sorry. Your boolean expression " +
	                    		"at line "+ node.getLine() + " is not correct!");
					}
				} 
        		else {
        			appendFaultNotice("Sorry! The type " + arrayType + 
        					" at line " + node.getLine() + " column " + 
        					node.getColumn() + " is not supported yet by AdaLogo");
				}
            	for (int i = 0; i < numOfIdentifier; i++) {
    				Array arr = new Array(dim,arrayType,bound,"");
                    symtab.put( ((SimpleNode)(node.jjtGetChild(i))).getValue().toLowerCase(),ARRAY,arr );
				}
			} 
        	else {
                throw new WrongNumberOfChildrenException("ArrayDeclaration");
			}
        }
        else {
            throw new WrongNumberOfChildrenException("VariableDeclaration");
        }

        return data;
	}
	
	/**
	 * Get VariableDeclaration and return the number of the identifier.
	 * @param node
	 * @return
	 */
	private int getNumOfIdentifier(SimpleNode node) {
		int result = 0;
		boolean stop = false;
		
		while (!stop) {
			if (node.jjtGetChild(result).toString() == "Identifier") {
				result++;
			}
			else {
				stop = true;
			}
		}
		return result;
	}
	
	/**
	 * Count the depth of ArrayDeclarationOthers
	 * @param node
	 * @return
	 */
	private int getDepthOfArrayDeclarationOthers(SimpleNode node) {
		if (node.toString() == "ArrayDeclarationOthers") {
			return 1 + getDepthOfArrayDeclarationOthers((SimpleNode)node.jjtGetChild(0));
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Get the node with value of ArrayDeclarationOthers 
	 * @param node
	 * @return
	 */
	private SimpleNode getNodeOfArrayDeclarationOthers(SimpleNode node) {
		if (node.toString() == "ArrayDeclarationOthers") {
			return getNodeOfArrayDeclarationOthers((SimpleNode)node.jjtGetChild(0));
		}
		else {
			return node;
		}
	}
	
	
	/**
	 * Do nothing. VariableDeclaration does it.
	 */
	public Object visit(ASTArrayDeclaration node, Object data) {
		node.childrenAccept(this,data);
		return null;
	}

	/**
	 * Do nothing. VariableDeclaration does it.
	 */
	public Object visit(ASTArrayDeclarationOthers node, Object data) {
		return null;
	}

	public Object visit(ASTTypeDeclaration node, Object data) {
		node.childrenAccept(this,data);
		throw new NotSupportYetException("TypeDeclaration");
	}

	public Object visit(ASTPointerDeclaration node, Object data) {
		node.childrenAccept(this,data);
		throw new NotSupportYetException("PointerDeclaration");
	}

	public Object visit(ASTDiscreteDeclaration node, Object data) {
		node.childrenAccept(this,data);
		throw new NotSupportYetException("DiscreteDeclaration");
	}

	public Object visit(ASTRecordDeclaration node, Object data) {
		node.childrenAccept(this,data);
		throw new NotSupportYetException("RecordDeclaration");
	}

	/**
	 * Do nothing. The children do it.
	 */
	public Object visit(ASTSequenceOfStatement node, Object data) {
		node.childrenAccept(this,data);
		return null;
	}

	/**
	 * Do nothing. The father does it.
	 */
	public Object visit(ASTCharacterLiteral node, Object data) {
		return null;
	}

	/**
	 * Do nothing. The father does it.
	 */
	public Object visit(ASTStringLiteral node, Object data) {
		return null;
	}

	// TODO Auto-generated method stub
	public Object visit(ASTAssignmentStatement node, Object data) {
		node.childrenAccept(this,data);
		return null;
	}

	public Object visit(ASTCaseStatement node, Object data) {
		throw new NotSupportYetException("CaseStatement");
	}

	public Object visit(ASTCaseValues node, Object data) {
		throw new NotSupportYetException("CaseStatement");
	}

	/**
	 * The children does the job. Open a block. 
	 */
	public Object visit(ASTDeclareBlock node, Object data) {
		symtab.levelUp();
		node.childrenAccept(this,data);
		symtab.levelDown();
		return null;
	}

	/**
	 * Do nothing. CompilationUnit checks the right place 
	 * of the statement.
	 */
	public Object visit(ASTExitStatement node, Object data) {
		return null;
	}

	// TODO TEST ME
	public Object visit(ASTForStatement node, Object data) {

        // without reverse:
        if ( node.jjtGetNumChildren() == 4 ) {

            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1));
            SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));
            
            if (!checkingIntegerExpression(child2)) {
            	// TODO write this better
				throw new AdaLogoSyntaxSemanticException("Wrong inter expression.");
			}
            
            if (!checkingIntegerExpression(child3)) {
            	// TODO write this better
				throw new AdaLogoSyntaxSemanticException("Wrong inter expression.");
			}

            symtab.levelUp();
			symtab.put(new String("for"), BLOCK, new String("for"));
			symtab.put(((SimpleNode) (node.jjtGetChild(0))).getValue()
					.toLowerCase(), PROTECTED_INTEGER, (new Integer(0)));
            data = node.jjtGetChild(3).jjtAccept(this, data);
			symtab.levelDown();
        }
        // with reverse:
        else if ( (((SimpleNode)(node.jjtGetChild(1))).toString() == "ForReverse")
                & node.jjtGetNumChildren() == 5) {

            SimpleNode child3 = (SimpleNode)(node.jjtGetChild(2));
            SimpleNode child4 = (SimpleNode)(node.jjtGetChild(3));
            
            if (!checkingIntegerExpression(child3)) {
            	// TODO write this better
				throw new AdaLogoSyntaxSemanticException("Wrong inter expression.");
			}
            
            if (!checkingIntegerExpression(child4)) {
            	// TODO write this better
				throw new AdaLogoSyntaxSemanticException("Wrong inter expression.");
			}

            symtab.levelUp();
			symtab.put(new String("for"), BLOCK, new String("for"));
			symtab.put(((SimpleNode) (node.jjtGetChild(0))).getValue()
					.toLowerCase(), PROTECTED_INTEGER, (new Integer(0)));
            data = node.jjtGetChild(4).jjtAccept(this, data);
			symtab.levelDown();
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
	 * Do nothing.
	 */
	public Object visit(ASTForReverse node, Object data) {
		return null;
	}

	/**
	 * TODO TEST ME
	 */
	public Object visit(ASTIfStatement node, Object data) {
        int numOfChildren = node.jjtGetNumChildren();

        if (numOfChildren >= 2) {

            // ifPart
            if (checkingBooleanExpression((SimpleNode)node.jjtGetChild(0))) {
            	node.jjtGetChild(1).jjtAccept(this,data);
            }
            else {
                int line = node.getLine();
                int col = node.getColumn();
                throw new AdaLogoSyntaxSemanticException(
                        "The boolean expression of the if-statement " +
                        "at line " + line + " and column " + col +
                        " is not correct.");
            }

            // one of elsifPart
            for (int i = 3; i< numOfChildren; i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i-1));

                if (checkingBooleanExpression((SimpleNode)childI.jjtGetChild(0))) {
           			childI.jjtGetChild(1).jjtAccept(this,data);
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
            boolean elsePartExists = false;
            if (node.jjtGetNumChildren() > 2 &&
                    ((SimpleNode)(node.jjtGetChild(numOfChildren-1))).toString() == "ElsePart")
            {
                elsePartExists = true;
            }
            if (elsePartExists) {
                node.jjtGetChild(numOfChildren-1).jjtAccept(this,data);
            }
            
            return data;
        }
        else {
            throw new WrongNumberOfChildrenException("IfStatement");
        }
	}

	/**
	 * The first child boolean expression was checked from IfStatement
	 * Just do the SequenceOfStatement
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
	 * Just do the SequenceOfStatement
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
	 * Do null; ;)
	 */
	public Object visit(ASTNullStatement node, Object data) {
		return null;
	}

	public Object visit(ASTLoopStatement node, Object data) {
		node.childrenAccept(this,data);
		return null;
	}

	// TODO Auto-generated method stub
	public Object visit(ASTProcedureCallStatement node, Object data) {
        // procedurecallstatement with no parameter
        if (node.jjtGetNumChildren() == 1) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0));

            int line = ((SimpleNode)node.jjtGetChild(0)).getLine();
            int col = ((SimpleNode)node.jjtGetChild(0)).getColumn();
            String procedureCallName = child1.getValue().toLowerCase();

            if (symtab.variableExists((Object)procedureCallName)) {
                Object procedureDefNode = symtab.getValue((Object)procedureCallName);
                if (((SimpleNode)procedureDefNode).jjtGetNumChildren() == 3 ||
                	((SimpleNode)procedureDefNode).jjtGetNumChildren() == 1) {
//                    symtab.levelUp();
//                    symtab.put(new String("procedure " + procedureCallName),BLOCK, procedureCallName);
//                    ((SimpleNode)(procedureDefNode)).jjtAccept(this,data);
//                    symtab.levelDown();
                }
                else {
                    appendFaultNotice("The procedure " + procedureCallName
							+ " at line " + line + " and column " + col
							+ " has parameters, but you call it without any!");
                }
            }
            else {
                appendFaultNotice("The definition of procedure "
						+ procedureCallName + " at line " + line
						+ " and column " + col
						+ " does not exist or is not visible!");
            }
        }
        // procedurecallstatement with parameter(s)
        else if (node.jjtGetNumChildren() == 2 && 
        		node.jjtGetChild(1).toString() == "CallParameters") {
        	
        }
        
        // assignment of "normal" variable
        else if (node.jjtGetNumChildren() == 2 && 
        		node.jjtGetChild(1).toString() == "AssignmentStatement") {
        	
        }
        // assignment of arrays
        else if (node.jjtGetNumChildren() == 3 &&
        		node.jjtGetChild(2).toString() == "AssignmentStatement") {
        	
        }
        else {
        	throw new WrongNumberOfChildrenException("ProcedureCallStatement");
        }
        
        return null;
	}

	/**
	 * Do nothing. different semantic + different context.
	 */
	public Object visit(ASTCallParameters node, Object data) {
		//node.childrenAccept(this,data);
		return null;
	}

	/**
	 * Do nothing. unreachable code and the existing of
	 * return statement in function was checked.
	 */
	public Object visit(ASTReturnStatement node, Object data) {
		return null;
	}

	public Object visit(ASTWhileStatement node, Object data) {
        if (node.jjtGetNumChildren() == 2) {
            SimpleNode child1 = (SimpleNode)(node.jjtGetChild(0)); // BooleanExpression
            SimpleNode child2 = (SimpleNode)(node.jjtGetChild(1)); // SequenceOfStatements.

            if (checkingBooleanExpression(child1)) {
            }
            else {
                int line = node.getLine();
                int col = node.getColumn();
                throw new AdaLogoSyntaxSemanticException(
                        "The boolean expression in the while-statement " +
                        "at line " + line + " and column " + col +
                        " is not correct.");
            }
        	child2.jjtAccept(this,data);
        }
        else {
            throw new WrongNumberOfChildrenException("WhileStatement");
        }
        return data;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTOrNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTXorNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTAndNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTNotNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTEqualNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTNotEqualNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTGreaterThanNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTGreaterEqualNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTLessThanNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTLessEqualNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTTrueNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTFalseNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTAdditionNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTSubtractionNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTMultiplicationNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTDivisionNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTModNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTRemNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTDashNode node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTIntegerLiteral node, Object data) {
		return null;
	}

	/**
	 * Do nothing. BooleanExpression does it.
	 */
	public Object visit(ASTFloatLiteral node, Object data) {
		return null;
	}

	/**
	 * Do nothing. The checking*Expression functions have done it.
	 */
	public Object visit(ASTFunctionCallExpression node, Object data) {
		node.childrenAccept(this,data);
		return null;
	}
	
	
    /*-----------------------------------------------------------------------*/
	// CHECKING*
    /*-----------------------------------------------------------------------*/
	// TODO FIXME
	// TODO write AdaLogoSyntaxSemanticException
	/**
	 * Get SequenceOfStatement and check statically if there is always a reachable
	 * ReturnStatement.
	 */
	private boolean checkingReturnStatementOfFunction(SimpleNode node, String returnType) throws AdaLogoSyntaxSemanticException {
		int numChildren = node.jjtGetNumChildren();
		SimpleNode lastChild = (SimpleNode)node.jjtGetChild(numChildren-1);
		
		if (lastChild.toString() == "ReturnStatement") {
			if (((SimpleNode)lastChild.jjtGetChild(0)).getAttribute() == returnType ) {
				return true;
			}
			return false;
		}
		else if (lastChild.toString() == "IfStatement") {
			boolean result = true;
			// ifpart
			result = result & checkingReturnStatementOfFunction((SimpleNode)lastChild.jjtGetChild(1), returnType);
			
			boolean elsePartExists;
			elsePartExists = (lastChild.jjtGetChild(lastChild.jjtGetNumChildren()-1).toString() == "ElsePart");

			// elsifpart
			if (elsePartExists) {
				for (int j = 2; j < lastChild.jjtGetNumChildren()-2; j++) {
					result = result & checkingReturnStatementOfFunction((SimpleNode)lastChild.jjtGetChild(j).jjtGetChild(1), returnType);
				}
			} 
			else {
				for (int j = 2; j < lastChild.jjtGetNumChildren()-1; j++) {
					result = result & checkingReturnStatementOfFunction((SimpleNode)lastChild.jjtGetChild(j).jjtGetChild(1), returnType);
				}
			}
			
			// elsepart
			if (elsePartExists) {
				result = result & checkingReturnStatementOfFunction((SimpleNode)lastChild.jjtGetChild(lastChild.jjtGetNumChildren()-1).jjtGetChild(0), returnType);
			}
			return result;
		} 
		else {
			return false;
		}
	}

	// TODO FIXME
	// TODO write AdaLogoSyntaxSemanticException
	/**
	 * Get SequenceOfStatement and check statically if there are unreachable code.
	 */
	private boolean checkingUnreachableCode(SimpleNode node){
		boolean result = true; 
		int numChildren = node.jjtGetNumChildren();
		
		if (node.jjtGetChild(numChildren-1).toString() != "ReturnStatement" &&
				node.jjtGetChild(numChildren-1).toString() != "ExitStatement") {
			result = false;
		}
		
		SimpleNode child2I;
		for (int i = 0; i < numChildren/2; i++) {
			child2I = (SimpleNode)node.jjtGetChild(2*i);
			if (child2I.toString() == "LoopStatement") {
				result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(0));
			}
			if (node.jjtGetChild(i*2).toString() == "ForStatement") {
				int numChildren2I = child2I.jjtGetNumChildren();
				result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(numChildren2I-1).jjtGetChild(0));
			}
			if (child2I.toString() == "WhileStatement") {
				result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(1).jjtGetChild(0));
			}
			if (child2I.toString() == "IfStatement") {
				// ifpart
				result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(1));
				
				boolean elsePartExists;
				elsePartExists = (child2I.jjtGetChild(child2I.jjtGetNumChildren()-1).toString() == "ElsePart");

				// elsifpart
				if (elsePartExists) {
					for (int j = 2; j < child2I.jjtGetNumChildren()-2; j++) {
						result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(j).jjtGetChild(1));
					}
				} 
				else {
					for (int j = 2; j < child2I.jjtGetNumChildren()-1; j++) {
						result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(j).jjtGetChild(1));
					}
				}
				
				// elsepart
				if (elsePartExists) {
					result = result & checkingUnreachableCode((SimpleNode)child2I.jjtGetChild(child2I.jjtGetNumChildren()-1).jjtGetChild(0));
				}
			}
		}
		
		return result;
	}
	
    /**
     * Get the parse tree and return true, if ExitStatement is the right place,
     * otherwise false.
     */
    private boolean checkingExitStatementAtWrongPlace(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "ExitStatement") {
        	appendFaultNotice("Exit statement at line " + node.getLine() + 
        	" column " + node.getColumn() + " is only allowed in loops.");
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
    // TODO: TESTING
    private boolean checkingBooleanExpression(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "AdditionNode" ||
                node.toString() == "SubtractionNode" ||
                node.toString() == "MultiplicationNode" ||
                node.toString() == "DivisionNode" ||
                node.toString() == "ModNode" ||
                node.toString() == "RemNode" ||
                node.toString() == "DashNode" ||
                node.toString() == "IntegerLiteral" ||
        		node.toString() == "FloatLiteral") {
            return false;
        }
        else if (node.toString() == "Identifier") {
            if ( (symtab.getType(node.getValue().toLowerCase())) == BOOLEAN ||
            		symtab.getType(node.getValue().toLowerCase()) == PROTECTED_BOOLEAN ||
            		symtab.getType(node.getValue().toLowerCase() + " return " + BOOLEAN) == PROTECTED_FUNCTION ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (node.toString() == "FunctionCallExpression") {
        	// TODO
        	return functionReturnBoolean(node);
        }
        else if (node.toString() == "TrueNode" ||
                node.toString() == "FalseNode" ||
                node.toString() == "EqualNode" ||
                node.toString() == "NotEqualNode" ||
                node.toString() == "GreaterThanNode" ||
                node.toString() == "GreaterEqualNode" ||
                node.toString() == "LessThanNode" ||
                node.toString() == "LessEqualNode" ) {
        	// TODO: this is available: 1.0 > 1
            return true;
        }
        else if (node.toString() == "OrNode" ||
        		node.toString() == "XorNode" ||
                node.toString() == "AndNode" ||
                node.toString() == "NotNode") {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingBooleanExpression(childI);
            }
            return check;
        }
        else {
            throw new RuntimeException("Something is going wrong at checkingBooleanExpression.");
            //return false;
        }
    }

    /**
     * Because of the ambiguity of the grammar it is essential
     * to check if an IntegerExpression really look like an integer expression.
     * @param node
     * @return boolean
     */
    // TODO TESTING
    private boolean checkingIntegerExpression(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "OrNode" ||
        		node.toString() == "XorNode" ||
                node.toString() == "AndNode" ||
                node.toString() == "NotNode" ||
                node.toString() == "TrueNode" ||
                node.toString() == "FalseNode" ||
                node.toString() == "EqualNode" ||
                node.toString() == "NotEqualNode" |
                node.toString() == "GreaterThanNode" ||
                node.toString() == "GreaterEqualNode" ||
                node.toString() == "LessThanNode" ||
                node.toString() == "LessEqualNode" ||
                node.toString() == "FloatLiteral") {
            return false;
        }
        else if (node.toString() == "Identifier") {
            if ( (symtab.getType(node.getValue().toLowerCase())) == INTEGER ||  // FIXME Exception of SymbolTable?
            		symtab.getType(node.getValue().toLowerCase()) == PROTECTED_INTEGER ||
            		symtab.getType(node.getValue().toLowerCase() + " return " + INTEGER) == PROTECTED_FUNCTION ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (node.toString() == "FunctionCallExpression") {
        	// TODO
        	return functionReturnInteger(node);
        }
        else if (node.toString() == "IntegerLiteral") {
            return true;
        }
        else if (node.toString() == "AdditionNode" ||
                node.toString() == "SubtractionNode" ||
                node.toString() == "MultiplicationNode" ||
                node.toString() == "DivisionNode" ||
                node.toString() == "ModNode" ||
                node.toString() == "RemNode" ||
                node.toString() == "DashNode" ) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingIntegerExpression(childI);
            }
            return check;
        }
        else {
            throw new RuntimeException("Something is going wrong at checkingIntegerExpression.");
            //return false;
        }
    }
    
    // TODO TESTING
    private boolean checkingFloatExpression(SimpleNode node) {
        boolean check = true;

        if (node.toString() == "OrNode" ||
        		node.toString() == "XorNode" ||
                node.toString() == "AndNode" ||
                node.toString() == "NotNode" ||
                node.toString() == "TrueNode" ||
                node.toString() == "FalseNode" ||
                node.toString() == "EqualNode" ||
                node.toString() == "NotEqualNode" |
                node.toString() == "GreaterThanNode" ||
                node.toString() == "GreaterEqualNode" ||
                node.toString() == "LessThanNode" ||
                node.toString() == "LessEqualNode" ||
                node.toString() == "IntegerLiteral") {
            return false;
        }
        else if (node.toString() == "Identifier") {
            if ( (symtab.getType(node.getValue().toLowerCase())) == FLOAT ||  // FIXME Exception of SymbolTable?
            		symtab.getType(node.getValue().toLowerCase()) == PROTECTED_FLOAT ||
            		symtab.getType(node.getValue().toLowerCase() + " return " + FLOAT) == PROTECTED_FUNCTION ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (node.toString() == "FunctionCallExpression") {
        	// TODO
        	return functionReturnFloat(node);
        }
        else if (node.toString() == "FloatLiteral") {
            return true;
        }
        else if (node.toString() == "AdditionNode" ||
                node.toString() == "SubtractionNode" ||
                node.toString() == "MultiplicationNode" ||
                node.toString() == "DivisionNode" ||
                node.toString() == "ModNode" ||
                node.toString() == "RemNode" ||
                node.toString() == "DashNode" ) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode childI = (SimpleNode)(node.jjtGetChild(i));
                check = check & checkingIntegerExpression(childI);
            }
            return check;
        }
        else {
            throw new RuntimeException("Something is going wrong at checkingIntegerExpression.");
            //return false;
        }
    }
    
    // TODO
    private boolean functionReturnBoolean(SimpleNode node) {
    	// TODO use getSignatureOfFunctionCall
//    	String signature = getSignatureOfFunctionCall(node);
    	return true;
    }
    
    // TODO
    private boolean functionReturnInteger(SimpleNode node) {
    	// TODO use getSignatureOfFunctionCall
//    	String signature = getSignatureOfFunctionCall(node);
    	return true;
    }

    // TODO
    private boolean functionReturnFloat(SimpleNode node) {
    	// TODO use getSignatureOfFunctionCall
//    	String signature = getSignatureOfFunctionCall(node);
    	return true;
    }
    
    // TODO
    private String getSignatureOfFunctionCall(SimpleNode node) {
    	return "";
    }
    
    // TODO
    private String getSignatureOfProcedureCall(SimpleNode node) {
    	return "";
    }
    
    // TODO TESTING
    private String getSignatureOfFunctionDeclaration(SimpleNode node) {
		SimpleNode child0 = (SimpleNode)node.jjtGetChild(0);
		
		String signature = child0.getValue().toLowerCase();

		String declarationParameters = "";
		String returnType = "";

		// without DeclarationParameters, with (Declaration and SequenceOfStatement)
		// without DeclarationParameter and Declaration and SequenceOfStatement
	    if (node.jjtGetNumChildren() == 2 || 
	    		node.jjtGetNumChildren() == 4) {
	    	returnType = node.jjtGetChild(1).jjtGetChild(0).toString();
	    	if (returnType == "Integer" || 
	    			returnType == "Boolean" ||
	    			returnType == "Float") {
		    	returnType = " return " + returnType.toLowerCase();
				
			} else {
				throw new NotSupportYetException("Functions not returning integer, boolean or float.");
			}
	    	
            signature = signature + returnType;
        } 
	    // with all
		// with DeclarationParameter and without (Declaration and SequenceOfStatement)        
	    else if (node.jjtGetNumChildren() == 3 || 
        		node.jjtGetNumChildren() == 5) {
	    	returnType = node.jjtGetChild(2).jjtGetChild(0).toString();
	    	// TODO remove this later
	    	//console.append(returnType);
	    	if (returnType == "Integer" || 
	    			returnType == "Boolean" ||
	    			returnType == "Float") {
		    	returnType = " return " + returnType.toLowerCase();
				
			} else {
				throw new NotSupportYetException("Functions not returning integer, boolean or float.");
			}

            declarationParameters = "(";
            SimpleNode parameter;
            for (int i = 0; i < node.jjtGetChild(1).jjtGetNumChildren(); i++) {
				parameter = (SimpleNode)node.jjtGetChild(1).jjtGetChild(i);
				// without ParameterMode
				if (parameter.jjtGetNumChildren() == 2 || 
						(parameter.jjtGetNumChildren() == 3 && 
						 parameter.jjtGetChild(2).toString() == "TypeExpression")) {
					//declarationParameters = declarationParameters + " : in ";
					declarationParameters = declarationParameters
							+ ((SimpleNode) parameter.jjtGetChild(1)
									.jjtGetChild(0)).getValue().toLowerCase()
							+ ";";
					
				} // with ParameterMode
				else if (parameter.jjtGetNumChildren() == 3) {
					if (parameter.jjtGetChild(1).jjtGetNumChildren() == 2) {
						throw new AdaLogoSyntaxSemanticException("Error: It is not allowed to use " +
								"in out parameter(s) in function " + child0.getValue() + " at line "
								+ child0.getLine() + ", column " + child0.getColumn() + "!");
					}	
					else if (parameter.jjtGetChild(1).jjtGetChild(0).toString() == "In") {
						//declarationParameters = declarationParameters + " : in ";
					}
					else if (parameter.jjtGetChild(1).jjtGetChild(0).toString() == "Out") {
						throw new AdaLogoSyntaxSemanticException("Error: It is not allowed to use " +
								"out parameter(s) in function " + child0.getValue() + " at line "
								+ child0.getLine() + ", column " + child0.getColumn() + "!");
					}
					else { 
						throw new WrongNumberOfChildrenException("Parameter");
					}
					
					declarationParameters = declarationParameters
							+ ((SimpleNode) parameter.jjtGetChild(2)
									.jjtGetChild(0)).getValue().toLowerCase()
							+ ";";
				}
				else if (parameter.jjtGetNumChildren() == 4) {
					// TODO: what do we do with the default value?
				}
				else {
					throw new WrongNumberOfChildrenException("Parameter");
				}
			}
            declarationParameters = declarationParameters + ")";
            
            signature = signature + declarationParameters + returnType;
            
        }
        else {
        	console.appendError(new Integer(node.jjtGetNumChildren()).toString());
            throw new WrongNumberOfChildrenException("FunctionDeclaration");
        }    	
	    
	    // TODO remove this later
	    console.append(signature);
	    return signature;
    }
    
    // TODO TESTING
    private String getSignatureOfProcedureDeclaration(SimpleNode node) {
		SimpleNode child0 = (SimpleNode)node.jjtGetChild(0);
		
		String signature = child0.getValue().toLowerCase();
		String declarationParameters = "";
		
		// without DeclarationParameter and Declaration and SequenceOfStatement 1
		// without DeclarationParameters, with (Declaration and SequenceOfStatement) 3
	    if (node.jjtGetNumChildren() == 1 || 
	    		node.jjtGetNumChildren() == 3) {
        } 
	    // with all
		// with DeclarationParameter and without (Declaration and SequenceOfStatement)        
	    else if (node.jjtGetNumChildren() == 2 || 
        		node.jjtGetNumChildren() == 4) {

            declarationParameters = "(";
            SimpleNode parameter;
            for (int i = 0; i < node.jjtGetChild(1).jjtGetNumChildren(); i++) {
				parameter = (SimpleNode)node.jjtGetChild(1).jjtGetChild(i);
				// without ParameterMode
				if (parameter.jjtGetNumChildren() == 2 || 
						(parameter.jjtGetNumChildren() == 3 && 
						 parameter.jjtGetChild(2).toString() == "TypeExpression")) {
					//declarationParameters = declarationParameters + " : in ";
					declarationParameters = declarationParameters
							+ ((SimpleNode) parameter.jjtGetChild(1)
									.jjtGetChild(0)).getValue().toLowerCase()
							+ ";";
					
				} // with ParameterMode
				else if (parameter.jjtGetNumChildren() == 3) {
					if (parameter.jjtGetChild(1).jjtGetNumChildren() == 2) {
						//declarationParameters = declarationParameters + " : in out ";
					}	
					else if (parameter.jjtGetChild(1).jjtGetChild(0).toString() == "In") {
						//declarationParameters = declarationParameters + " : in ";
					}
					else if (parameter.jjtGetChild(1).jjtGetChild(0).toString() == "Out") {
						//declarationParameters = declarationParameters + " : out ";						
					}
					else { 
						throw new WrongNumberOfChildrenException("Parameter");
					}
					
					declarationParameters = declarationParameters
							+ ((SimpleNode) parameter.jjtGetChild(2)
									.jjtGetChild(0)).getValue().toLowerCase()
							+ ";";
				}
				else if (parameter.jjtGetNumChildren() == 4) {
					// TODO: what do we do with the default value?
				}
				else {
					throw new WrongNumberOfChildrenException("Parameter");
				}
			}
            declarationParameters = declarationParameters + ")";
            
            signature = signature + declarationParameters;
            
        }
        else {
        	console.appendError(new Integer(node.jjtGetNumChildren()).toString());
            throw new WrongNumberOfChildrenException("ProcedureDeclaration");
        }
	    
	    // TODO remove this later
	    console.append(signature);
	    
    	return signature;
    }
    
    
    private void appendFaultNotice(String notice) {
    	faultNotice = faultNotice + "Error: " + notice + "\n";
    	fault = true;
    	countFault++;
    }
    
    
    /**
     * this will wait if there is a break point at that location
     */
    private void handleBreakpoint(SimpleNode node) {

        if (master.isStopRequested()) {
            throw new InterpreterAbortException("Request stop by user.");
        }

        pc.increment();

        if (master.isBreak(node.getLine())) {
            master.visitorWaiting(node.getLine());
            //here visitor is waiting for user interaction
            master.visitorRunning();
        }
    }
}
