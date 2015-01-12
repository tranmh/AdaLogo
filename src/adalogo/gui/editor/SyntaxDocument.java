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

package adalogo.gui.editor;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * hardcoded syntax highlighter.
 * <br>
 * <br>
 * original code from The_Developer
 * <br>
 * http://forum.java.sun.com/thread.jspa?threadID=589109
 */
public class SyntaxDocument extends DefaultStyledDocument {

    //TODOlater rewrite this to highlight with thread using javacc produced AST

    private DefaultStyledDocument doc;

    private Element rootElement;

    private MutableAttributeSet normal;
    private MutableAttributeSet keyword;
    private MutableAttributeSet comment;
    private MutableAttributeSet quote;
    private MutableAttributeSet number;

    private Color normalColor = Color.BLACK;
    private Color keywordColor = new Color(0, 0, 140);
    private Color commentColor = new Color(0, 120, 0);
    private Color quoteColor = new Color(140, 0, 0);
    private Color numberColor = new Color(140, 0, 0);

    private Set keywords;

    public SyntaxDocument() {

        doc = this;
        rootElement = doc.getDefaultRootElement();

        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        normal = new SimpleAttributeSet();
        StyleConstants.setForeground(normal, normalColor);

        comment = new SimpleAttributeSet();
        StyleConstants.setForeground(comment, commentColor);
        //StyleConstants.setItalic(comment, true);

        keyword = new SimpleAttributeSet();
        StyleConstants.setForeground(keyword, keywordColor);
        //StyleConstants.setBold(keyword, true);

        quote = new SimpleAttributeSet();
        StyleConstants.setForeground(quote, quoteColor);

        number = new SimpleAttributeSet();
        StyleConstants.setForeground(number, numberColor);

        Object dummyObject = new Object();

        keywords = new HashSet();

        keywords.add("null");

        keywords.add("forward");
        keywords.add("turn");
        keywords.add("turn_to");
        keywords.add("move_to");
        keywords.add("pen_up");
        keywords.add("pen_down");
        keywords.add("turtle_reset");
        keywords.add("turtle_dir");
        keywords.add("turtle_x");
        keywords.add("turtle_y");

        keywords.add("new_line");
        keywords.add("put");
        keywords.add("put_line");

        keywords.add("random");
        keywords.add("max");
        keywords.add("min");
        keywords.add("mod");
        keywords.add("rem");

        keywords.add("with");
        keywords.add("use");

        keywords.add("procedure");
        keywords.add("is");
        keywords.add("begin");
        keywords.add("end");

        //keywords.add("function");
        //keywords.add("return");

        keywords.add("boolean");
        keywords.add("integer");

        keywords.add("true");
        keywords.add("false");

        keywords.add("and");
        keywords.add("or");
        keywords.add("not");

        keywords.add("if");
        keywords.add("then");
        keywords.add("else");
        keywords.add("elsif");

        keywords.add("for");
        keywords.add("in");
        keywords.add("reverse");
        keywords.add("while");
        keywords.add("loop");
        keywords.add("exit");

    }

    //-------------------------------------------------------------------------

    /**
     * Override to apply syntax highlighting after the document has been updated
     */
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        processChangedLines(offset, str.length());
    }

    /**
     * Override to apply syntax highlighting after the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }

    //DISABLED this makes trouble with undo manager (style undo)
    /*
    protected void fireInsertUpdate(DocumentEvent evt) {
        super.fireInsertUpdate(evt);
        processChangedLines(evt.getOffset(), evt.getLength());
    }
    /*
    protected void fireChangedUpdate(DocumentEvent evt) {
        super.fireChangedUpdate(evt);
        processChangedLines(evt.getOffset(), evt.getLength());
    }
    /*
    protected void fireRemoveUpdate(DocumentEvent evt) {
        super.fireRemoveUpdate(evt);
        processChangedLines(evt.getOffset(), evt.getLength());
    }
    /**/

    //-------------------------------------------------------------------------

    /**
     * Determine how many lines have been changed, then apply highlighting to
     * each line
     */
    private void processChangedLines(int offset, int length) {

        try {

            String content = doc.getText(0, doc.getLength());

            // The lines affected by the latest document update
            int startLine = rootElement.getElementIndex(offset);
            int endLine = rootElement.getElementIndex(offset + length);

            // Do the actual highlighting
            for (int i = startLine; i <= endLine; i++)
                applyHighlighting(content, i);

        } catch (BadLocationException e) {
            //e.printStackTrace();
            System.out.println("syntax highlight failed! should never happen");
        }

    }

    /**
     * Parse the line to determine the appropriate highlighting
     */
    private void applyHighlighting(String content, int line) {

        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;
        int lineLength = endOffset - startOffset;
        int contentLength = content.length();

        if (endOffset >= contentLength)
            endOffset = contentLength - 1;

        // set normal attributes for the line
        doc.setCharacterAttributes(startOffset, lineLength, normal, true);

        // check for single line comment
        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ((index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
            endOffset = index - 1;
        }

        // check for tokens
        checkForTokens(content, startOffset, endOffset);
    }

    /**
     * Parse the line for tokens to highlight
     */
    private void checkForTokens(String content, int startOffset, int endOffset) {

        while (startOffset <= endOffset) {

            // skip the delimiters to find the start of a new token
            while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
                if (startOffset < endOffset)
                    startOffset++;
                else
                    return;
            }

            String nextChar = content.substring(startOffset, startOffset + 1);

            // Extract and process the entire token
            if (isQuoteDelimiter(nextChar))
                startOffset = getQuoteToken(content, startOffset, endOffset);
            else if (isNumberConstant(nextChar))
                startOffset = getNumberToken(content, startOffset, endOffset);
            else
                startOffset = getOtherToken(content, startOffset, endOffset);

        }
    }

    /**
     * Parse the line to get the quotes and highlight it
     */
    private int getQuoteToken(String content, int startOffset, int endOffset) {

        String quoteDelimiter = content.substring(startOffset, startOffset + 1);
        String escapeString = getEscapeString(quoteDelimiter);

        int index;
        int endOfQuote = startOffset;

        // skip over the escape quotes in this quote
        /* AdaLogo does not use this.
        index = content.indexOf(escapeString, endOfQuote + 1);
        while ((index > -1) && (index < endOffset)) {
            endOfQuote = index + 1;
            index = content.indexOf(escapeString, endOfQuote);
        }
        */

        // now find the matching delimiter
        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
        if ((index < 0) || (index > endOffset))
            endOfQuote = endOffset;
        else
            endOfQuote = index;

        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);

        return endOfQuote + 1;
    }

    /**
     * Parse the line to get a number constant and highlight it
     */
    private int getNumberToken(String content, int startOffset, int endOffset) {

        int endOfToken = startOffset + 1;

        while (endOfToken <= endOffset) {
            if (!isNumberConstant(content.substring(endOfToken, endOfToken + 1)))
                break;
            endOfToken++;
        }

        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, number, false);

        //return endOfToken + 1; //LESMANA this seems to be wrong
        return endOfToken;
    }

    /**
     * Parse the line for a token and highlight it
     */
    private int getOtherToken(String content, int startOffset, int endOffset) {

        int endOfToken = startOffset + 1;

        while (endOfToken <= endOffset) {

            if (isDelimiter(content.substring(endOfToken, endOfToken + 1)))
                break;
            else if (isNumberConstant(content.substring(endOfToken, endOfToken + 1)))
                break;
            endOfToken++;
        }

        String token = content.substring(startOffset, endOfToken);

        if (isKeyword(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, keyword, false);

        //return endOfToken + 1; //LESMANA this seems to be wrong
        return endOfToken;
    }

    //-------------------------------------------------------------------------

    protected boolean isDelimiter(String character) {
        //LESMANA is this adalogo conform?
        String operands = ";:{}()[]+-/%<=>!&|^~*";
        if (Character.isWhitespace(character.charAt(0))
                || operands.indexOf(character) != -1)
            return true;
        else
            return false;
    }

    protected boolean isQuoteDelimiter(String character) {
        //LESMANA adalogo doesnt have string/char yet
        String quoteDelimiters = "\"'";
        if (quoteDelimiters.indexOf(character) < 0)
            return false;
        else
            return true;
    }

    protected boolean isNumberConstant(String character) {
        //LESMANA created number constants
        String quoteDelimiters = "0123456789";
        if (quoteDelimiters.indexOf(character) < 0)
            return false;
        else
            return true;
    }

    protected boolean isKeyword(String token) {
        //LESMANA made case unsensitive
        //Object o = keywords.get(token.toLowerCase());
        //return o == null ? false : true;
        // MCT: case unsensitive:
        return keywords.contains(token.toLowerCase());
    }

    protected String getSingleLineDelimiter() {
        //LESMANA modified to ada comment
        return "--";
    }

    protected String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

}
