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

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * this will store the symbol tables
 * used during interpretation of adalogo code
 */
public class SymbolTable {

    // TODO remove this later. we do not need this anymore.
	//private static final String FUNCTION = "function";
	//private static final String PROCEDURE = "procedure";
	
    /**
     * symbol table exceptions.
     * nothing special here.
     */
    public class SymbolTableException extends RuntimeException {
    	
		private static final long serialVersionUID = 1L;

		//TODOlater convert RuntimeException to real Exception
        //runtime exception was used to to avoid massive numbers
        //of try catch blocks in interpreter
        public SymbolTableException(String message) {
            super(message);
        }
    }

    /**
     * a variable consisting of type and value
     * this will be put in the hashmap
     * where it will be mapped to a name
     */
    private class Variable {
        public Object type;
        public Object value;
        public Variable(Object type, Object value) {
            this.type=type;
            this.value=value;
        }
        /**
         * this just for debug to look nice :)
         */
        public String toString() {
            return "{"+type+", "+value+"}";
        }
    }

    /**
     * this stack containing HashMaps
     * will be the symtab
     */
    private Stack stack;

    public SymbolTable() {
        stack = new Stack();
        levelUp();
    }

    /**
     * add a value to the symbol table.
     * throws exception if variable already exists.
     */
    public void put(Object name, Object type, Object value) throws SymbolTableException {
        Map symtab = (HashMap)stack.peek();
        if (symtab.containsKey(name)) {
			// TODO write this exception with line and column
            throw new SymbolTableException("Variable, function or procedure "+name+" already exists");
        }
        symtab.put(name, new Variable(type, value));
        fireNewVariable(name); //HLT
    }

    /**
     * find a variable in the symtab.
     * throws exception if variable not found.
     */
    private Variable getVariable(Object name) throws SymbolTableException {
        for (ListIterator i = stack.listIterator(stack.size()); i.hasPrevious(); ) {
            Map symtab = (Map)i.previous();
            if (symtab.containsKey(name)) {
                return (Variable)symtab.get(name);
            }
        } 
        // TODO write this exception with line and column
        throw new SymbolTableException("Variable, function or procedure "+name+" does not exist");
    }

    /**
     * Search in the SymbolTable for name and return true if exists,
     * false otherwise.
     */
    public boolean variableExists(Object name) {
        for (ListIterator i = stack.listIterator(stack.size()); i.hasPrevious(); ) {
            Map symtab = (Map)i.previous();
            if (symtab.containsKey(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * get the type of the variable name.
     * throws exception if variable not found.
     */
    public Object getType(Object name) throws SymbolTableException {
        return getVariable(name).type;
    }

    /**
     * get the type of the variable name, returns String.
     * throws exception if variable not found.
     */
    public String getType(String name) throws SymbolTableException {
        return (String)(getVariable(name).type);
    }

    /**
     * get the value of the variable name.
     * throws exception if variable not found.
     */
    public Object getValue(Object name) throws SymbolTableException {
        return getVariable(name).value;
    }

    /**
     * get the value of the variable name, returns String.
     * throws exception if variable not found.
     */
    public String getValue(String name)  throws SymbolTableException {
        return (String)(getVariable(name).value);
    }

    /**
     * set a new value for an alreay existing variable.
     * throws exception if variable not found.
     */
    public void setValue(Object name, Object value) throws SymbolTableException {
        //System.out.println("debug");
        getVariable(name).value = value;
        fireValueChanged(name); //HLT
    }
    
    public void setType(Object name, Object type) throws SymbolTableException {
    	getVariable(name).type = type;
    	// TODO: something like fireTypeChanged(name)
    	//fireValueChanged(name); // MCT
    }

    /**
     * use this when going in to a declarative part
     */
    public void levelUp() {
        stack.push(new HashMap());
        fireLevelUp(); //HLT
    }

    /**
     * use this when going out of a declarative part.
     */
    public void levelDown() throws SymbolTableException {
        try {
            stack.pop();
            fireLevelDown(); //HLT
        } catch (EmptyStackException e) {
            //e.printStackTrace();
            throw new SymbolTableException("level was downest");
        }
    }
    

    // TODO remove this later. we do not need this anymore.
    /**
     *  
     * @return
     */
//    public boolean existsUnprotectedFunction() {
//    	Variable variable;
//    	variable = new Variable(FUNCTION,null);
//        Map symtab = (HashMap)stack.peek();
//        return symtab.containsValue(variable);
//    }

    /**
     *  
     * @return
     */
//    public boolean existsUnprotectedProcedure() {
//    	Variable variable;
//    	variable = new Variable(PROCEDURE,null);
//        Map symtab = (HashMap)stack.peek();
//        return symtab.containsValue(variable);
//    }


    //-------------------------------------------------------------------------
    //event listener architecture

    /** HLT
     *  EventList for SymTabChangedEvents
     */
    private Set listeners = new HashSet();

    public interface SymbolTableListener {
        public void LevelUpPerformed();
        public void LevelDownPerformed();
        public void NewVariablePerformed(Object name);
        public void ValueChangedPerformed(Object name);
    }

    /** HLT
     *  method to add SymTabChangedListener into EventListeners
     */
    public void addSymTabChangedListener(SymbolTableListener listener){
        listeners.add(listener);
    }

    /** HLT
     *  fire Event methods
     */
    public void fireLevelUp(){
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SymbolTableListener l = (SymbolTableListener) i.next();
            l.LevelUpPerformed();
        }
    }

    /** HLT
     *  fire Event methods
     */
    public void fireLevelDown(){
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SymbolTableListener l = (SymbolTableListener) i.next();
            l.LevelDownPerformed();
        }
    }

    /** HLT
     *  fire Event methods
     */
    public void fireNewVariable(Object name){
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SymbolTableListener l = (SymbolTableListener) i.next();
            l.NewVariablePerformed(name);
        }
    }

    /** HLT
     *  fire Event methods
     */
    public void fireValueChanged(Object name){
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SymbolTableListener l = (SymbolTableListener) i.next();
            l.ValueChangedPerformed(name);
        }
    }

    /**
     * HLT temporary solution for the VarMonitor. VarMonitor needs access to the
     * whole symboltable to work.
     */
    public ListIterator getIterator() {
        return stack.listIterator(stack.size());
    }

    //-------------------------------------------------------------------------
    //test

    /**
     * a small testbench for the class.
     * made static inner class so it will compile as SymbolTable$Test.java.
     * <br>
     * to run: "java adalogo.lang.SymbolTable$Test.class"
     */
    public static class Test {

        public static void main(String[] args) throws Exception {
            SymbolTable symtab = new SymbolTable();

            boolean exceptionThrown = false;

            //test all standard methods
            symtab.put("a","boolean","true");
            if (!symtab.getType("a").equals("boolean"))
                throw new Exception();
            if (!symtab.getValue("a").equals("true"))
                throw new Exception();
            symtab.setValue("a", "false");
            if (!symtab.getValue("a").equals("false"))
                throw new Exception();

            //same for a second var
            symtab.put("b","integer","3");
            if (!symtab.getType("b").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("b").equals("3"))
                throw new Exception();
            symtab.setValue("b", "4");
            if (!symtab.getValue("b").equals("4"))
                throw new Exception();

            //test creation of a var which already exists
            exceptionThrown = false;
            try {
                symtab.put("a","boolean","false");
            } catch (Exception e) {
                exceptionThrown = true;
            }
            if (!exceptionThrown)
                throw new Exception();

            symtab.levelUp();

            //test access to var in lower level
            if (!symtab.getType("a").equals("boolean"))
                throw new Exception();
            if (!symtab.getValue("a").equals("false"))
                throw new Exception();
            symtab.setValue("a", "true");
            if (!symtab.getValue("a").equals("true"))
                throw new Exception();

            //test creation of new var in higher level
            //hiding an already existing var in lower level
            symtab.put("a","integer","5");
            if (!symtab.getType("a").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("a").equals("5"))
                throw new Exception();
            symtab.setValue("a", "6");
            if (!symtab.getValue("a").equals("6"))
                throw new Exception();

            //test creation of new var in higher level
            symtab.put("c","integer","7");
            if (!symtab.getType("c").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("c").equals("7"))
                throw new Exception();
            symtab.setValue("c", "8");
            if (!symtab.getValue("c").equals("8"))
                throw new Exception();

            //test creation of a var which already exists
            //in higher level
            exceptionThrown = false;
            try {
                symtab.put("c","boolean","false");
            } catch (Exception e) {
                exceptionThrown = true;
            }
            if (!exceptionThrown)
                throw new Exception();

            symtab.levelUp();

            //test access to var in lower level
            //one level down
            if (!symtab.getType("c").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("c").equals("8"))
                throw new Exception();
            symtab.setValue("c", "7");
            if (!symtab.getValue("c").equals("7"))
                throw new Exception();

            //two levels down
            if (!symtab.getType("b").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("b").equals("4"))
                throw new Exception();
            symtab.setValue("b", "3");
            if (!symtab.getValue("b").equals("3"))
                throw new Exception();

            //with existing var one and two levels down
            //we want the var one level down
            if (!symtab.getType("a").equals("integer"))
                throw new Exception();
            if (!symtab.getValue("a").equals("6"))
                throw new Exception();
            symtab.setValue("a", "5");
            if (!symtab.getValue("a").equals("5"))
                throw new Exception();

            symtab.levelDown();
            symtab.levelDown();

            //this var may no longer exist
            exceptionThrown = false;
            try {
                symtab.getType("c");
            } catch (Exception e) {
                exceptionThrown = true;
            }
            if (!exceptionThrown)
                throw new Exception();

            //these vars have to exist
            if (!symtab.getType("a").equals("boolean"))
                throw new Exception();
            if (!symtab.getType("b").equals("integer"))
                throw new Exception();

            System.out.println("all tests passed");
        }

    }


}
