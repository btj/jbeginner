// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

class AddFieldDialog extends OkCancelDialog {
    JComboBox typeBox = new JComboBox(Util.getTypeWrappers());
    JTextField nameField = new JTextField(20);
    JavaClass javaClass;
    
    AddFieldDialog(JavaClass javaClass) {
        super("Add Field");
        
        this.javaClass = javaClass;
        
        nameField.setText(Util.getNewKey(javaClass.fields, "field"));
        
        GridBagLayout layout = new GridBagLayout();
        JPanel dataPanel = new JPanel(layout);
        JLabel typeLabel = new JLabel("Type:");
        dataPanel.add(typeLabel);
        dataPanel.add(typeBox);
        Component rigidArea = Box.createRigidArea(new Dimension(11, 11));
        dataPanel.add(rigidArea);
        JLabel nameLabel = new JLabel("Name:");
        dataPanel.add(nameLabel);
        dataPanel.add(nameField);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        layout.setConstraints(typeLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        layout.setConstraints(typeBox, c);
        c.gridx = 1; c.gridy = 1; c.weightx = 0;
        layout.setConstraints(rigidArea, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST;
        layout.setConstraints(nameLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; c.gridy = 2; c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(nameField, c);
        
        nameField.selectAll();
        
        init(dataPanel);
    }
        
    void okButtonClicked() {
        final String name = nameField.getText();
        if (name.length() == 0) {
            error("Please enter a name for the new field.");
        } else if (!Util.isJavaIdentifier(name)) {
            error("The name must start with a letter and consist only of letters and digits.");
        } else if (Util.isReservedWord(name)) {
            error("The word \"" + name + "\" is a keyword in Java. It cannot be used as a name.");
        } else if (javaClass.fields.containsKey(name)) {
            error("The name you entered is already used. Please enter a new name.");
        } else {
            TypeWrapper type = (TypeWrapper) typeBox.getSelectedItem();
            JavaType javaType = type.type;
            final JavaField field = new JavaField(javaType, name);
            Explorer.instance.doCommand(new Command() {
                public void doIt() {
                    javaClass.fields.put(name, field);
                }
                public void undoIt() {
                    javaClass.fields.remove(name);
                }
            });
            dispose();
        }
    }
    
    static void showAddFieldDialog(JavaClass javaClass) {
        new AddFieldDialog(javaClass).show();
    }
}