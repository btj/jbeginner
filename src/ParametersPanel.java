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

class ParametersPanel extends JPanel {
    static class ParameterListModel extends AbstractListModel {
        List parameters = new ArrayList();
        Map parameterMap = new HashMap();
        
        public int getSize() {
            return parameters.size();
        }
        
        public Object getElementAt(int index) {
            return ((VarDecl) parameters.get(index)).getDeclarationText();
        }
        
        void add(VarDecl parameter) {
            int i = parameters.size();
            parameters.add(parameter);
            parameterMap.put(parameter.name, parameter);
            fireIntervalAdded(this, i, i);
        }
    }
    
    class AddParameterDialog extends OkCancelDialog {
        JComboBox typeBox;
        JTextField nameField;
        
        AddParameterDialog(JDialog owner) {
            super(owner, "Add Parameter");
            init(getBodyComponent());
        }
        
        Component getBodyComponent() {
            typeBox = new JComboBox(Util.getTypeWrappers());
            nameField = new JTextField(20);
            nameField.setText(Util.getNewKey(model.parameterMap, "param"));
            nameField.selectAll();
            
            JPanel content = new JPanel(new BorderLayout(0, 17));
            
            GridBagLayout layout = new GridBagLayout();
            JPanel dataPanel = new JPanel(layout);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE; c.weightx = 0;
            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
            JLabel typeLabel = new JLabel("Type:");
            dataPanel.add(typeLabel);
            layout.setConstraints(typeLabel, c);
            c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
            c.gridx = 2; c.gridy = 0;
            dataPanel.add(typeBox);
            layout.setConstraints(typeBox, c);
            c.fill = GridBagConstraints.NONE; c.weightx = 0;
            c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
            Component rigidArea = Box.createRigidArea(new Dimension(11, 11));
            dataPanel.add(rigidArea);
            layout.setConstraints(rigidArea, c);
            JLabel nameLabel = new JLabel("Name:");
            c.fill = GridBagConstraints.NONE; c.weightx = 0;
            c.gridx = 0; c.gridy = 2;
            dataPanel.add(nameLabel);
            layout.setConstraints(nameLabel, c);
            c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
            c.gridx = 2; c.gridy = 2;
            dataPanel.add(nameField);
            layout.setConstraints(nameField, c);
            content.add(dataPanel);
            return content;
        }
        
        void okButtonClicked() {
            String name = nameField.getText();
            if (name.length() == 0) {
                error("Please enter a name for the new parameter.");
            } else if (!Util.isJavaIdentifier(name)) {
                error("The name must start with a letter and consist only of letters and digits.");
            } else if (Util.isReservedWord(name)) {
                error("The word \"" + name + "\" is a keyword in Java. It cannot be used as a name.");
            } else if (model.parameterMap.containsKey(name)) {
                error("The name you entered is already used. Please enter a new name.");
            } else {
                TypeWrapper type = (TypeWrapper) typeBox.getSelectedItem();
                model.add(new VarDecl(type.type, name));
                dispose();
            }
        }
    }

    ParameterListModel model = new ParameterListModel();
    JList parametersBox = new JList(model);
    
    ParametersPanel() {
        super(new BorderLayout(11, 6));
        
        JButton addButton = new JButton("Add");
        addButton.setMnemonic(KeyEvent.VK_A);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AddParameterDialog((JDialog) getTopLevelAncestor()).show();
            }
        });
        add(new JLabel("Parameters:"), BorderLayout.NORTH);
        add(new JScrollPane(parametersBox));
        JPanel parametersCommandsPanel = new JPanel(new BorderLayout());
        parametersCommandsPanel.add(addButton, BorderLayout.NORTH);
        add(parametersCommandsPanel, BorderLayout.EAST);
    }
    
    List getParameters() {
        return model.parameters;
    }
}