package adalogo.visitor;

import adalogo.Engine;
import adalogo.Turtle;
import adalogo.gui.Console;
import java.util.HashSet;
import java.util.Iterator;

public class AdaLogoRoutine {

//	// procedures without parameters
//	private static final String NEW_LINE = "new_line"; // 0 or 1 parameter. Integer
//	private static final String PEN_UP = "pen_up";  
//	private static final String PEN_DOWN = "pen_down";
//	private static final String TURTLE_RESET = "turtle_reset";
//	// procedures with 1 parameter
//	private static final String FORWARD = "forward"; // float or integer
//	private static final String GET = "get"; // float or integer or boolean
//	private static final String TURN = "turn"; // float or integer
//	private static final String TURN_TO = "turn_to"; // float or integer
//	private static final String PUT = "put"; // float, integer, boolean, string or character
//	private static final String PUT_LINE = "put_line"; // float, integer, boolean, string or character
//	// with 2 parameters
//	private static final String MOVE_TO = "move_to"; // (integer, integer) or (float, float)
//
//	// function without parameters
//	private static final String TURTLE_PEN_DOWN = "turtle_pen_down"; // returns boolean
//	private static final String TURTLE_DIR = "turtle_dir"; // returns float or integer
//	private static final String TURTLE_X = "turtle_x"; // return float or integer
//	private static final String TURTLE_Y = "turtle_y"; // return float or integer
//	// function with 1 parameter
//	private static final String SIN = "sin"; // get float, return float.
//	private static final String COS = "cos"; // get float, return float.
//	private static final String TAN = "tan"; // get float, return float.
//	private static final String TO_INTEGER = "to_integer"; // get float, return integer. 
//	private static final String TO_FLOAT = "to_float";  // get integer, return float.
//	// function with 2 parameters
//	private static final String RANDOM = "random"; // (integer, integer) return integer or (float, float) return float
	
//	private static final String TYPE_BOOLEAN = "boolean";
//	private static final String TYPE_FLOAT = "float";
//	private static final String TYPE_INTEGER = "integer";
//	private static final String TYPE_STRING = "string";
//	private static final String TYPE_CHARACTER = "character";

	private static final String ADALOGO_FUNCTION = "adalogo_function";
	private static final String ADALOGO_PROCEDURE = "adalogo_procedure";
	
    private Engine engine;
    private VisitorMaster master;
    private Console console;
    
    private Turtle turtle;
    
    private SymbolTable symtab;
    
    private HashSet adaLogoFunctionsReturnInteger = new HashSet();
    private HashSet adaLogoFunctionsReturnFloat = new HashSet();
    private HashSet adaLogoFunctionsReturnBoolean = new HashSet();
    
    private HashSet adaLogoProcedure = new HashSet();

	public AdaLogoRoutine(Engine engine, VisitorMaster master, SymbolTable symtab) {
		this.engine = engine;
		this.master = master;
		
		this.symtab = symtab;
		
		this.console = engine.getConsole();
		this.turtle = engine.getTurtle();
	
		// the order of this 4 void is important...!
		fillAdaLogoFunctionsReturnInteger();
		fillAdaLogoFunctionsReturnFloat();
		fillAdaLogoFunctionsReturnBoolean();
		fillAdaLogoProcedure();
		
		symtab.levelUp();
	}
	
	private void fillAdaLogoFunctionsReturnInteger() {
		adaLogoFunctionsReturnInteger.add("turtle_dir return integer"); // 0
		adaLogoFunctionsReturnInteger.add("turtle_x return integer"); // 1
		adaLogoFunctionsReturnInteger.add("turtle_y return integer"); // 2
		adaLogoFunctionsReturnInteger.add("to_integer(float;) return integer"); // 3
		adaLogoFunctionsReturnInteger.add("random(integer;integer;) return integer"); // 4
		int i = 0;
		for (Iterator iter = adaLogoFunctionsReturnInteger.iterator(); iter.hasNext();) {
			String signature = (String) iter.next();
			symtab.put(signature,ADALOGO_FUNCTION,new Integer(i));
			i++;
		}
	}
	
	private void fillAdaLogoFunctionsReturnFloat() {
		adaLogoFunctionsReturnFloat.add("turtle_dir return float"); // 5
		adaLogoFunctionsReturnFloat.add("turtle_x return float"); // 6
		adaLogoFunctionsReturnFloat.add("turtle_y return float"); // 7
		adaLogoFunctionsReturnFloat.add("sin(float;) return float"); // 8 
		adaLogoFunctionsReturnFloat.add("cos(float;) return float"); // 9
		adaLogoFunctionsReturnFloat.add("tan(float;) return float"); // 10
		adaLogoFunctionsReturnInteger.add("to_float(integer;) return float"); // 11
		adaLogoFunctionsReturnInteger.add("random(float;float;) return float"); // 12
		int i = adaLogoFunctionsReturnInteger.size();
		for (Iterator iter = adaLogoFunctionsReturnFloat.iterator(); iter.hasNext();) {
			String signature = (String) iter.next();
			symtab.put(signature, ADALOGO_FUNCTION, new Integer(i));
			i++;
		}
	}
	
	private void fillAdaLogoFunctionsReturnBoolean() {
		adaLogoFunctionsReturnBoolean.add("turtle_pen_down return boolean"); // 13
		int i = adaLogoFunctionsReturnInteger.size()
				+ adaLogoFunctionsReturnFloat.size();
		for (Iterator iter = adaLogoFunctionsReturnBoolean.iterator(); iter.hasNext();) {
			String signature = (String) iter.next();
			symtab.put(signature, ADALOGO_FUNCTION, new Integer(i));
			i++;
		}
	}
	
	private void fillAdaLogoProcedure() {
		adaLogoProcedure.add("new_line"); // 14
		adaLogoProcedure.add("new_line(integer;)"); // 15
		adaLogoProcedure.add("pen_up"); // 16
		adaLogoProcedure.add("pen_down"); // 17
		adaLogoProcedure.add("turtle_reset"); // 18
		adaLogoProcedure.add("forward(integer;)"); // 19
		adaLogoProcedure.add("forward(float;)"); // 20
		adaLogoProcedure.add("get(integer;)"); // 21
		adaLogoProcedure.add("get(float;)"); // 22
		adaLogoProcedure.add("get(boolean;)"); // 23
		adaLogoProcedure.add("turn(integer;)"); // 24
		adaLogoProcedure.add("turn(float;)"); // 25
		adaLogoProcedure.add("turn_to(integer;)"); // 26
		adaLogoProcedure.add("turn_to(float;)"); // 27
		adaLogoProcedure.add("put(integer;)"); // 28
		adaLogoProcedure.add("put(float;)"); // 29
		adaLogoProcedure.add("put(boolean;)"); // 30
		adaLogoProcedure.add("put(string;)"); // 31
		adaLogoProcedure.add("put(character;)"); // 32
		adaLogoProcedure.add("put_line(integer;)"); // 33
		adaLogoProcedure.add("put_line(float;)"); // 34
		adaLogoProcedure.add("put_line(boolean;)"); // 35
		adaLogoProcedure.add("put_line(string;)"); // 36
		adaLogoProcedure.add("put_line(character;)"); // 37
		adaLogoProcedure.add("move_to(integer;integer;)"); // 38 
		adaLogoProcedure.add("move_to(float;float;)"); // 39
		int i = adaLogoFunctionsReturnInteger.size()
				+ adaLogoFunctionsReturnFloat.size()
				+ adaLogoFunctionsReturnBoolean.size();
		for (Iterator iter = adaLogoProcedure.iterator(); iter.hasNext();) {
			String signature = (String) iter.next();
			symtab.put(signature, ADALOGO_PROCEDURE, new Integer(i));
			i++;
		}
	}
	
	// TODO should we remove this???
	public boolean isFunctionOfAdaLogo(String signature) {
		if (adaLogoFunctionsReturnBoolean.contains(signature) ||
			adaLogoFunctionsReturnFloat.contains(signature) ||
			adaLogoFunctionsReturnInteger.contains(signature) ) {
			return true;
		} 
		else {
			return false;
		}
	}

	// TODO should we remove this?
	public boolean isProcedureOfAdaLogo(String signature) {
		return adaLogoProcedure.contains(signature);		
	}
	
	// TODO
	public Object executeFunctionOfAdaLogo(String args[], int functionNo) {
		return null;
	}
	
	// TODO TEST ME
	public void executeProcedureOfAdaLogo(String args[], int procedureNo) {
		
		double step;
		double degree;
		double x;
		double y;
		
		switch (procedureNo) {
		case 14: // new_line
			console.append("");
			break;
		case 15: // new_line(integer;)
			int howOften = new Integer(args[0]).intValue();
			for (int i = 0; i < howOften; i++) {
				console.append("");
			}
			break;
		case 16: // pen_up
			turtle.penUp();
			break;
		case 17: // pen_down
			turtle.penDown();
		case 18: // turtle_reset
			turtle.resetTurtle();
			break;
		case 19: // forward(integer;)
			step = new Double(new Integer(args[0]).toString()).doubleValue();
			turtle.forward(step);
			break;
		case 20: // forward(float;)
			step = new Double(new Float(args[0]).toString()).doubleValue();
			turtle.forward(step);
			break;
		case 21: // get(integer;) // TODO
			break;
		case 22: // get(float;) // TODO
			break;
		case 23: // get(boolean;) // TODO
			break;
		case 24: // turn(integer;)
			degree = new Double(new Integer(args[0]).toString()).doubleValue();
			turtle.turn(degree);
			break;
		case 25: // turn(float;)
			degree = new Double(new Float(args[0]).toString()).doubleValue();
			turtle.turn(degree);
			break;
		case 26: // turn_to(integer;)
			degree = new Double(new Integer(args[0]).toString()).doubleValue();
			turtle.turnTo(degree);
			break;
		case 27: // turn_to(float;)
			degree = new Double(new Float(args[0]).toString()).doubleValue();
			turtle.turnTo(degree);
			break;
		case 28: // put(integer;)
			console.appendWithoutNewline((new Integer(args[0])).toString());
			break;
		case 29: // put(float;)
			console.appendWithoutNewline((new Float(args[0])).toString());
			break;
		case 30: // put(boolean;)
			console.appendWithoutNewline((new Boolean(args[0])).toString());
			break;
		case 31: // put(string;)
			console.appendWithoutNewline(args[0]);
			break;
		case 32: // put(character;)
			console.appendWithoutNewline((new String(args[0])));
			break;
		case 33: // put_line(integer;)
			console.append((new Integer(args[0])).toString());
			break;
		case 34: // put_line(float;)
			console.append((new Float(args[0])).toString());
			break;
		case 35: // put_line(boolean;)
			console.append((new Boolean(args[0])).toString());
			break;
		case 36: // put_line(string;)
			console.append(args[0]);
			break;
		case 37: // put_line(character;)
			console.append((new String(args[0])));
			break;
		case 38: // move_to(integer;integer;)
			x = new Double(new Integer(args[0]).toString()).doubleValue();
			y = new Double(new Integer(args[1]).toString()).doubleValue();
			turtle.moveTo(x, y);
			break;
		case 39: // move_to(float;float;)
			x = new Double(new Float(args[0]).toString()).doubleValue();
			y = new Double(new Float(args[1]).toString()).doubleValue();
			turtle.moveTo(x, y);
			break;
		default:
			throw new RuntimeException("executeProcedureOfAdaLogo. This should never happen");
		}
	} 
}