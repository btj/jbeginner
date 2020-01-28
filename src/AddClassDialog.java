// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class AddClassDialog extends NameDialog {
    AddClassDialog() {
        super("Add Class", Util.getNewKey(Machine.instance.program.classes, "Class"));
    }
    
    void nameEntered(final String name) {
        if (!Character.isUpperCase(name.charAt(0))) {
            error("The name must start with an uppercase letter.");
        } else if (Machine.instance.program.classes.containsKey(name)) {
            error("The name you entered is already used. Please enter a new name.");
        } else {
            final JavaClass javaClass = new JavaClass(name);
            Explorer.instance.doCommand(new Command() {
                public void doIt() {
                    Machine.instance.program.classes.put(name, javaClass);
                }
                public void undoIt() {
                    Machine.instance.program.classes.remove(name);
                }
            });
            dispose();
        }
    }
    
    static void showAddClassDialog() {
        new AddClassDialog().show();
    }
}