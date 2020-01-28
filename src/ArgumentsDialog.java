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

class ArgumentsDialog extends OkCancelDialog {
    List parameters;
    LiteralEditor[] editors;
    Object[] values;
    
    ArgumentsDialog(List parameters) {
        super("Arguments");
        this.parameters = parameters;
        
        GridBagLayout layout = new GridBagLayout();
        JPanel argumentsPanel = new JPanel(layout);
        GridBagConstraints c = new GridBagConstraints();
        
        int n = parameters.size();
        editors = new LiteralEditor[n];
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                Component rigidArea = Box.createRigidArea(new Dimension(11, 11));
                argumentsPanel.add(rigidArea);
                c.gridx = 1; c.gridy = 2 * i - 1; c.weightx = 0;
                layout.setConstraints(rigidArea, c);
            }
            
            VarDecl parameter = (VarDecl) parameters.get(i);
            JLabel label = new JLabel(parameter.name + ":");
            argumentsPanel.add(label);
            c.gridx = 0; c.gridy = 2 * i; c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.NONE; c.weightx = 0;
            layout.setConstraints(label, c);
            
            JavaType type = parameter.type;
            LiteralEditor editor = type.getValueEditor(type.getDefaultValue());
            editors[i] = editor;
            JComponent component = editor.getJComponent();
            argumentsPanel.add(component);
            c.gridx = 2; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
            layout.setConstraints(component, c);
        }
        
        if (n == 1) {
            Component strut = Box.createHorizontalStrut(11);
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0;
            argumentsPanel.add(strut);
            layout.setConstraints(strut, c);
        }
        
        init(argumentsPanel);
    }
    
    void okButtonClicked() {
        class Continuation implements ErrorContinuation {
            int index;
            Object[] values;
            
            Continuation() {
                values = new Object[editors.length];
            }
            
            void next() {
                if (index < values.length)
                    editors[index].tryGetValue(this);
                else {
                    ArgumentsDialog.this.values = values;
                    dispose();
                }
            }
            
            public void success(Object value) {
                values[index] = value;
                index++;
                next();
            }
            
            public void error(String message) {
                JOptionPane.showMessageDialog(ArgumentsDialog.this, "Argument " + index + ": " + message, "Arguments", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        new Continuation().next();
    }
    
    static Object[] show(List parameters) {
        ArgumentsDialog dialog = new ArgumentsDialog(parameters);
        dialog.show();
        return dialog.values;
    }
}