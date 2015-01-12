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

package adalogo.lang;

public class SimpleNode implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Lang parser;

    /**
     * Constructor of SimpleNode. For in-parameter see LangTreeConstants.
     */
    public SimpleNode(int i) {
        id = i;
    }

    /**
     * Constructor of SimpleNode. For in-parameter see LangTreeConstants.
     */
    public SimpleNode(Lang p, int i) {
        this(i);
        parser = p;
    }

    /**
     * Constructor of Node. For in-parameter see LangTreeConstants.
     */
    public static Node jjtCreate(int id) {
        return new SimpleNode(id);
    }

    /**
     * Constructor of Node. For in-parameter see LangTreeConstants.
     */
    public static Node jjtCreate(Lang p, int id) {
        return new SimpleNode(p, id);
    }

    /**
     * what is this?
     */
    public void jjtOpen() {
    }

    /**
     * what is this?
     */
    public void jjtClose() {
    }

    /**
     * Set parent
     */
    public void jjtSetParent(Node n) { parent = n; }

    /**
     * Get parent
     */
    public Node jjtGetParent() { return parent; }

    /**
     * AddChild n to the position i.
     */
    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    /**
     * return child at position i
     */
    public Node jjtGetChild(int i) {
        return children[i];
    }

    /**
     * return the number of children.
     */
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(LangVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /** Accept the visitor. **/
    public Object childrenAccept(LangVisitor visitor, Object data) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                children[i].jjtAccept(visitor, data);
            }
        }
        return data;
    }

    /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
    /**
     * Get the name of the node, for exmaple SequenceOfStatement etc.
     * see LangConstants
     */
    public String toString() { return LangTreeConstants.jjtNodeName[id]; }


    /**
     * Get the name of the node with prefix, for exmaple xxSequenceOfStatement etc.
     * see LangConstants
     */
    public String toString(String prefix) { return prefix + toString(); }

    /**
     * Dump the parse tree with param prefix
     */
    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    //added by MCT
    //modified by LESMANA

    protected String value;
    protected int column = 0;
    protected int line = 0;
    
    protected String attribute = "";

    /**
     * Exception class, uses by SimpleNode.getValue()
     */
    public class SimpleNodeGetValueException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public SimpleNodeGetValueException(String message) {
            super(message);
        }
    }
    
    /**
     * set attribute for the node
     * @param attribute
     */
    public void setAttribute(String attribute) {
    	this.attribute = attribute;
    }
    
    /**
     * get attribute of the node.
     * @return String attribute
     */
    public String getAttribute() {
    	return attribute;
    }

    /**
     * set the value for the node.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * return the value of the node.
     * @throws Exception if value not set
     */
    public String getValue() throws SimpleNodeGetValueException {
        if (value == null)
            throw new SimpleNodeGetValueException("value not set");
        return value;
    }

    /**
     * @return the beginColumn of the node, where you can find
     * in the editor window.
     */
    public int getColumn() {
        return column;
    }

    /**
     * set beginColumn, where you can find the node.
     * this should only use by Lang.jjt
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @return the beginLine of the node, where you can find
     * in the editor window.
     */
    public int getLine() {
        return line;
    }

    /**
     * set beginLine, where you can find the node.
     * this should only use by Lang.jjt
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * This is for getting the type of the node.
     * For example SequenceOfStatement, see LangTreeConstants.java
     */
    public int getId() {
        return id;
    }

    /**
     * This is for setting the type of the node.
     * For example SequenceOfStatement, see LangTreeConstants.java
     */
    public void setId(int i) {
        id = i;
    }

}
