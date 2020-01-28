// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class OkCancelDialog extends JDialog {
    JPanel contentPane = new JPanel(new BorderLayout(0, 17));
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    
    OkCancelDialog(String title) {
        super(Explorer.instance, title, true);
    }
    
    OkCancelDialog(JDialog owner, String title) {
        super(owner, title, true);
    }
    
    void init(Component bodyComponent) {
        setContentPane(contentPane);
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        contentPane.add(commandPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        commandPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonClicked();
            }
        });
        commandPanel.add(Box.createHorizontalStrut(5));
        commandPanel.add(cancelButton);
        javax.swing.Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        cancelButton.registerKeyboardAction(cancelAction, escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        cancelButton.addActionListener(cancelAction);
        
        contentPane.add(bodyComponent);
        
        pack();
        setLocationRelativeTo(getOwner());
    }

    abstract void okButtonClicked();
    
    void error(String message) {
        JOptionPane.showMessageDialog(this, message, getTitle(), JOptionPane.ERROR_MESSAGE);
    }
}