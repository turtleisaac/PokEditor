//package io.github.turtleisaac.pokeditor.gui.projects.projectwindow;
//
//import javax.swing.*;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//
//public class PoppedEditorFrame extends JFrame
//{
//    ProjectWindow.EditorTypes editorType;
//    ProjectWindow projectWindow;
//
//    PoppedEditorFrame(ProjectWindow projectWindow, ProjectWindow.EditorTypes editorType) {
//        super();
//        this.projectWindow = projectWindow;
//        this.editorType = editorType;
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                projectWindow.unpopEditors(editorType);
//                super.windowClosing(e);
//            }
//        });
//    }
//}
