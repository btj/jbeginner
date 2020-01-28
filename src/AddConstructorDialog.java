// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.util.List;

class AddConstructorDialog extends OkCancelDialog {
    ParametersPanel parametersPanel;
    JavaClass javaClass;
    
    AddConstructorDialog(JavaClass javaClass) {
        super("Add Constructor");
        this.javaClass = javaClass;
        init(getBodyComponent());
    }
    
    Component getBodyComponent() {
        parametersPanel = new ParametersPanel();
        return parametersPanel;
    }
    
    void okButtonClicked() {
        final JavaConstructor constructor = new JavaConstructor(javaClass, parametersPanel.getParameters());
        final String key = ".ctor";
        Explorer.instance.doCommand(new Command() {
            public void doIt() {
                javaClass.constructors.put(key, constructor);
            }
            public void undoIt() {
                javaClass.constructors.remove(key);
            }
        });
        dispose();
    }
    
    static void showAddConstructorDialog(JavaClass javaClass) {
        new AddConstructorDialog(javaClass).show();
    }
}