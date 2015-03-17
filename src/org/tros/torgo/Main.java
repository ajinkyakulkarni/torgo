/*
 * Copyright 2015 Matthew Aguirre
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tros.torgo;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main entry point for torgo
 *
 * @author matta
 */
public class Main {

    /**
     * Entry Point
     *
     * @param args
     */
    public static void main(String[] args) {
        //force opengl
        System.setProperty("sun.java2d.opengl", "true");
        //initialize the logging
        org.tros.utils.logging.Logging.initLogging(TorgoInfo.Instance);
        Options options = new Options();
        options.addOption("l", "lang", true, "Open using the desired language. [default is 'logo']");
        options.addOption("i", "list", false, "List available languages.");
        String lang = "logo";
        try {
            CommandLineParser parser = new org.apache.commons.cli.BasicParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("lang") || cmd.hasOption("l")) {
                lang = cmd.getOptionValue("lang");
            } else if (cmd.hasOption("i") || cmd.hasOption("list")) {
                Set<String> toolkits = TorgoToolkit.getToolkits();
                toolkits.stream().forEach((name) -> {
                    System.out.println(name);
                });
                //will force an exit
                lang = null;
            }
        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //set look and feel (laf) to that of the system.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        final String controlLang = lang;
        SwingUtilities.invokeLater(() -> {
            Controller controller = TorgoToolkit.getController(controlLang);
            if (controller != null) {
                controller.run();
            }
        });
    }
}