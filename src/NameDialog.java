// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class NameDialog extends OkCancelDialog {
    JTextField nameField;
    
    NameDialog(String title, String name) {
        super(title);
        init(getBodyComponent());
        nameField.setText(name);
        nameField.selectAll();
    }
     
    Component getBodyComponent() {
        nameField = new JTextField(20);
        
        JPanel namePanel = new JPanel(new BorderLayout(11, 0));
        namePanel.add(new JLabel("Name:"), BorderLayout.WEST);
        namePanel.add(nameField);
        return namePanel;
    }
    
    void okButtonClicked() {
        final String name = nameField.getText();
        if (name.length() == 0) {
            error("Please enter a name for the new class.");
        } else if (!Util.isJavaIdentifier(name)) {
            error("The name must start with an uppercase letter and consist only of letters and digits.");
        } else {
            nameEntered(name);
        }
    }
    
    abstract void nameEntered(String name);
}