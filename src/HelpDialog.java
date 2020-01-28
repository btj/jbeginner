// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class HelpDialog extends JDialog {
    HelpDialog() {
        super(Explorer.instance, "User's Guide", true);
        
        JPanel content = new JPanel(new BorderLayout(0, 17));
        setContentPane(content);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        JEditorPane editorPane;
        try {
            editorPane = new JEditorPane(HelpDialog.class.getResource("UsersGuide.html"));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        editorPane.setEditable(false);
        content.add(new JScrollPane(editorPane));
        
        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton okButton = new JButton("OK");
        commandPanel.add(okButton);
        content.add(commandPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        setSize(new Dimension(600, 400));
        validate();
        setLocationRelativeTo(Explorer.instance);
    }
}