/*
 * Created by JFormDesigner on Sun Jan 03 23:43:53 EST 2021
 */

package io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.console;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class ConsoleWindow extends JFrame {

    PrintStream originalOut;
    PrintStream originalErr;
    PrintStream newOut;
    PrintStream newErr;
    BufferedWriter consoleOut;

    public ConsoleWindow() {
        initComponents();
        setVisible(false);

        originalOut = System.out;
        originalErr = System.err;

        consoleOut= new BufferedWriter(new OutputStreamWriter(originalOut));

//        redirectSystemStreams();

        setPreferredSize(new Dimension(500,500));
        setMinimumSize(new Dimension(500,500));
        pack();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();

        //======== this ========
        setTitle("Console");
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]"));

        //======== scrollPane1 ========
        {

            //---- textArea1 ----
            textArea1.setEditable(false);
            scrollPane1.setViewportView(textArea1);
        }
        contentPane.add(scrollPane1, "cell 0 0,grow");
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    private synchronized void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> textArea1.append(text));
    }

    private synchronized void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b)
            {
                updateTextArea(String.valueOf((char) b));
//                try{
//                    consoleOut.write(String.valueOf((char) b));
//                }
//                catch(IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void write(byte[] b, int off, int len){
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        newOut = new PrintStream(out, true);
        newErr = new PrintStream(out, true);

        System.setOut(newOut);
        System.setErr(newErr);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
