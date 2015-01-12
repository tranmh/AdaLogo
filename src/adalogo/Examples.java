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

import java.io.InputStream;

/**
 * static class to load examples.
 * the example file are in the example directory
 * which will be packed in the jar.
 * files from the jar can only be opened as inputstream.
 */
public class Examples {

    /**
     * hardcoded list of examples.
     * this is necessary because a dir in a jar cannot be parsed
     * (at least i have no idea how).
     * the first in the list should always be template.adl
     */
    public static final String[] example = {
            "template.adl",
            "befehle.adl",
            "Parabel.adl",
            "kreis.adl",
            "quadrat.adl",
            //"n_eck.adl",
            //"ada.adl",
            //"schnauzbart.adl",
            //"fibonacci.adl",
            //"dreieck.adl",
            //"gerade_ungerade.adl",
            "pusteblume.adl",
            //"schachbrett.adl",
            //"stern_fraktal.adl",
            //"dreieck_fraktal.adl",
            "kreise_mandala.adl",
            "hilbert.adl"
            //"tunnel.adl"
    };

    /**
     * returns the template.adl stream.
     * this should be equivalent with getExample(0);
     * this will be called by editor to load editor with template
     */
    public static InputStream getTemplate() {
        return Examples.class.getResourceAsStream("/examples/template.adl");
    }

    /**
     * returns the stream for example number number
     */
    public static InputStream getExample(int number) {
        return Examples.class.getResourceAsStream("/examples/"+example[number]);
    }

    /*
     * cannot use this because files from jar can only be accessed as stream.
     * this will only work when the files are on disk.
     * left here for nostalgic reasons :)
     */
    /*
    public Action[] getExampleActions(Engine engine) {

        URL url = Examples.class.getResource("/examples");
        File dir = new File(url.getPath());
        File[] file = dir.listFiles();
        Action[] action = new Action[file.length];
        for (int i = 0; i < file.length; i++) {
            //System.out.println(file[i]);
            action[i] = new ExampleAction(engine, file[i]);
        }

        return action;

    }
    /**/

}
