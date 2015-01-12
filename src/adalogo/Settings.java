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

package adalogo;

import java.awt.Font;

public class Settings {

    //just to prevent people from instantiating this
    private Settings() {
    }

    //-------------------------------------------------------------------------
    /**
     * pointer to the applet instance.
     * only relevant if started as applet.
     */
    private static AdaLogo applet;

    /**
     * get the currently running applet.
     * this is called from engine when about to close frame.
     */
    public static AdaLogo getApplet() {
        if (applet == null)
            throw new RuntimeException("This should never happen");
        return applet;
    }

    public static void setApplet(AdaLogo applet) {
        Settings.applet = applet;
    }

    /**
     * returns wether the programm was started as applet or application.
     * @return true if and only if started as applet.
     */
    public static boolean isStartedAsApplet() {
        if (applet == null)
            return false;
        else
            return true;
    }

    //-------------------------------------------------------------------------
    /**
     * information about file access permission.
     * if false, file access is not allowed.
     */
    private static boolean filePermission = true;

    /**
     * true when file access is allowed.
     * this is used by editor actions.
     */
    public static boolean isFilePermission() {
        return filePermission;
    }

    public static void setFilePermission(boolean perm) {
        Settings.filePermission = perm;
    }

    //-------------------------------------------------------------------------
    private static Font consoleFont = new Font(
            //"Lucida Sans Typewriter", Font.PLAIN, 14);
            "Monospaced", Font.PLAIN, 14);

    /**
     * returns the font for the console.
     * @return the font for the console.
     */
    public static Font getConsoleFont() {
        return consoleFont;
    }

    /**
     * set the font to be used by the console.
     * @param consoleFont
     */
    public static void setConsoleFont(Font consoleFont) {
        Settings.consoleFont = consoleFont;
    }

    /**
     * set the font size for the console.
     * @param size
     */
    public static void setConsoleFontSize(float size) {
        consoleFont = consoleFont.deriveFont(size);
    }

    //-------------------------------------------------------------------------
    private static Font editorFont = new Font(
            //"Lucida Sans Typewriter", Font.PLAIN, 14);
            "Monospaced", Font.PLAIN, 14);

    /**
     *
     * @return the font for the editor.
     */
    public static Font getEditorFont() {
        return editorFont;
    }

    /**
     * set the font to be used by the editor.
     *
     * @param editorFont
     */
    public static void setEditorFont(Font editorFont) {
        Settings.editorFont = editorFont;
    }

    /**
     * set the font size for the editor.
     *
     * @param size
     */
    public static void setEditorFontSize(float size) {
        editorFont = editorFont.deriveFont(size);
    }

    //-------------------------------------------------------------------------
    private static boolean antiAlias = true;

    /**
     *
     * @return the antialias property for turtle canvas.
     */
    public static boolean isAntiAlias() {
        return antiAlias;
    }

    /**
     * set the antialias property for turtle canvas.
     * @param antiAlias true for anti alias enabled, false for disabled.
     */
    public static void setAntiAlias(boolean antiAlias) {
        Settings.antiAlias = antiAlias;
    }

    //-------------------------------------------------------------------------
    private static boolean renderQuality = true;

    /**
     *
     * @return the render quality property for turtle canvas.
     */
    public static boolean isRenderQuality() {
        return renderQuality;
    }

    /**
     * set the render quality property for turtle canvas.
     * @param renderQuality true for high render quality,
     * false for speed render quality.
     */
    public static void setRenderQuality(boolean renderQuality) {
        Settings.renderQuality = renderQuality;
    }

    //-------------------------------------------------------------------------
    public static final int TURTLE_FOLLOW_MODE_NEVER = 0;
    public static final int TURTLE_FOLLOW_MODE_EDGE = 1;
    public static final int TURTLE_FOLLOW_MODE_ALWAYS = 2;

    private static int turtleFollowMode = 1;

    /**
     * return the turtle follow mode.
     * this is used by turtle canvas to update view
     * when turtle moves to edge.
     */
    public static int getTurtleFollowMode() {
        return turtleFollowMode;
    }

    public static void setTurtleFollowMode(int turtleFollowMode) {
        Settings.turtleFollowMode = turtleFollowMode;
    }

    //-------------------------------------------------------------------------
    private static boolean printSyntaxTree = false;

    public static boolean isPrintSyntaxTree() {
        return printSyntaxTree;
    }

    public static void setPrintSyntaxTree(boolean printSyntaxTree) {
        Settings.printSyntaxTree = printSyntaxTree;
    }

    //-------------------------------------------------------------------------

    private static boolean expandedbydefault = true;

    public static boolean isExpandedbydefault() {
        return expandedbydefault;
    }

    public static void setExpandedbydefault(boolean expandedbydefault) {
        Settings.expandedbydefault = expandedbydefault;
    }

    private static boolean showroot = false;

    public static boolean isShowroot() {
        return showroot;
    }

    public static void setShowroot(boolean showroot) {
        Settings.showroot = showroot;
    }

    private static boolean showastree = true;

    public static boolean isShowAsTree() {
        return showastree;
    }

    public static void setShowAsTree(boolean showastree) {
        Settings.showastree = showastree;
    }

    public static final String show = "show";
    public static final String inextranode = "in extra node";
    public static final String hide = "hide";

    private static String invisiblenodemode = show;

    public static void setInvisibleNodeModeShow() {
        invisiblenodemode = show;
    }

    public static void setInvisibleNodeModeInExtraNode() {
        invisiblenodemode = inextranode;
    }

    public static void setInvisibleNodeModeHide() {
        invisiblenodemode = hide;
    }

    public static String getInvisibleNodeMode() {
        return invisiblenodemode;
    }

}
