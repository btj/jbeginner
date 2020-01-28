// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class AboutDialog extends JDialog {
    static String[] text = {
        "JBeginner",
        "Version 0.1",
        "Running on JRE version " + System.getProperty("java.version"),
        "",
        "https://github.com/btj/jbeginner",
        "",
        "This is public domain software.",
        "",
        "This software is provided 'as-is', without any express or implied",
        "warranty. In no event will the authors be held liable for any",
        "damages arising from the use of this software."
    };
    
    AboutDialog() {
        super(Explorer.instance, "About", true);
        
        JPanel content = new JPanel(new BorderLayout(0, 17));
        setContentPane(content);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        JPanel dataPanel = new JPanel(new GridLayout(0, 1));
        for (int i = 0; i < text.length; i++)
            dataPanel.add(new JLabel(text[i]));
        
        content.add(dataPanel);
        
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
        
        pack();
        setLocationRelativeTo(Explorer.instance);
    }
}
