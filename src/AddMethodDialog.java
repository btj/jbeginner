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

class AddMethodDialog extends OkCancelDialog {
    ParametersPanel parametersPanel = new ParametersPanel();
    JCheckBox staticBox = new JCheckBox("Static");
    JComboBox returnTypeBox;
    JTextField nameField = new JTextField();
    JavaClass javaClass;
    
    AddMethodDialog(final JavaClass javaClass) {
        super("Add Method");
        
        this.javaClass = javaClass;
        
        staticBox.setSelected(true);
        Vector returnTypes = Util.getTypeWrappers();
        returnTypes.add(0, "void"); // This must be the literal "void".
        returnTypeBox = new JComboBox(returnTypes);
        nameField.setText(Util.getNewKey(javaClass.methods, "method"));
        
        JPanel content = new JPanel(new BorderLayout(0, 17));
        
        GridBagLayout layout = new GridBagLayout();
        JPanel dataPanel = new JPanel(layout);
        dataPanel.add(staticBox);
        Component rigidArea1 = Box.createRigidArea(new Dimension(11, 11));
        dataPanel.add(rigidArea1);
        JLabel typeLabel = new JLabel("Return Type:");
        typeLabel.setLabelFor(returnTypeBox);
        dataPanel.add(typeLabel);
        dataPanel.add(returnTypeBox);
        Component rigidArea = Box.createRigidArea(new Dimension(11, 11));
        dataPanel.add(rigidArea);
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setLabelFor(nameField);
        dataPanel.add(nameLabel);
        dataPanel.add(nameField);
        content.add(dataPanel, BorderLayout.NORTH);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        layout.setConstraints(staticBox, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        layout.setConstraints(rigidArea1, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        layout.setConstraints(typeLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; c.gridy = 2; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        layout.setConstraints(returnTypeBox, c);
        c.gridx = 1; c.gridy = 3; c.weightx = 0;
        layout.setConstraints(rigidArea, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 4; c.anchor = GridBagConstraints.EAST;
        layout.setConstraints(nameLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; c.gridy = 4; c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(nameField, c);
        
        nameField.selectAll();
        
        content.add(parametersPanel);
        
        init(content);
    }
    
    void okButtonClicked() {
        final String name = nameField.getText();
        if (name.length() == 0) {
            error("Please enter a name for the new method.");
        } else if (!Util.isJavaIdentifier(name)) {
            error("The name must start with a letter and consist only of letters and digits.");
        } else if (Util.isReservedWord(name)) {
            error("The word \"" + name + "\" is a keyword in Java. It cannot be used as a name.");
        } else {
            Object returnType = returnTypeBox.getSelectedItem();
            JavaType type = returnType == "void" ? null : ((TypeWrapper) returnType).type;
            boolean isStatic = staticBox.isSelected();
            final JavaMethod method = new JavaMethod(javaClass, isStatic, type, name, parametersPanel.getParameters());
            if (javaClass.methods.containsKey(name)) {
                error("Class " + javaClass.name + " already contains a method with name " + name + ". Please choose a different name.");
            } else {
                Explorer.instance.doCommand(new Command() {
                    public void doIt() {
                        javaClass.methods.put(name, method);
                    }
                    public void undoIt() {
                        javaClass.methods.remove(name);
                    }
                });
                dispose();
            }
        }
    }
    
    static void showAddMethodDialog(JavaClass javaClass) {
        new AddMethodDialog(javaClass).show();
    }
}