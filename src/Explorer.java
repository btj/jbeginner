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

interface Command {
    void doIt();
    void undoIt();
}

/** Thrown when the user's program performs an illegal operation. */
class JavaExecutionException extends RuntimeException {
    JavaExecutionException(String message) {
        super(message);
    }
}

class Explorer extends JFrame {
    static final Explorer instance = new Explorer();

	JScrollPane programScrollPane;
    ProgramView programView;
    DataView dataView;
    List commands = new ArrayList();
    /** Number of commands that have not been undone. */
    int commandCount;
    JMenuItem undoMenuItem;
    JMenuItem redoMenuItem;
    JCheckBoxMenuItem showArrowsMenuItem = new JCheckBoxMenuItem("Show Arrows");
    JCheckBoxMenuItem showObjectNamesMenuItem = new JCheckBoxMenuItem("Show Object Names");
    
    Explorer() {
        super("JBeginner");
        
        programView = new ProgramView(Machine.instance.program);
        dataView = new DataView();
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newMenuItem = new JMenuItem("New");
        fileMenu.add(newMenuItem);
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newProgram();
            }
        });
        fileMenu.addSeparator();
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        undoMenuItem = new JMenuItem("Undo");
        KeyStroke ctrlZKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
        undoMenuItem.setAccelerator(ctrlZKeyStroke);
        editMenu.add(undoMenuItem);
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        redoMenuItem = new JMenuItem("Redo");
        KeyStroke ctrlShiftZKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        redoMenuItem.setAccelerator(ctrlShiftZKeyStroke);
        editMenu.add(redoMenuItem);
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(showArrowsMenuItem);
        viewMenu.add(showObjectNamesMenuItem);
        showArrowsMenuItem.setSelected(true);
        dataView.showArrows = true;
        showArrowsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataView.showArrows = showArrowsMenuItem.isSelected();
                dataView.repaint();
            }
        });
        showObjectNamesMenuItem.setSelected(true);
        dataView.showObjectNames = true;
        showObjectNamesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataView.showObjectNames = showObjectNamesMenuItem.isSelected();
                dataView.repaint();
            }
        });
        
        JMenu programMenu = new JMenu("Program");
        menuBar.add(programMenu);
        JMenuItem addClassMenuItem = new JMenuItem("Add Class...");
        programMenu.add(addClassMenuItem);
        addClassMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddClassDialog.showAddClassDialog();
            }
        });
        JMenu runMenu = new JMenu("Run");
        menuBar.add(runMenu);
        JMenuItem stepMenuItem = new JMenuItem("Step");
        runMenu.add(stepMenuItem);
        stepMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
        stepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        JMenuItem stopMenuItem = new JMenuItem("Stop Running");
        runMenu.add(stopMenuItem);
        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopRunning();
            }
        });
        JMenuItem resetMachineMenuItem = new JMenuItem("Reset Machine");
        runMenu.add(resetMachineMenuItem);
        resetMachineMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetMachine();
            }
        });
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        JMenuItem usersGuideMenuItem = new JMenuItem("User's Guide...");
        helpMenu.add(usersGuideMenuItem);
        usersGuideMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HelpDialog().show();
            }
        });
        helpMenu.addSeparator();
        JMenuItem aboutMenuItem = new JMenuItem("About...");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AboutDialog().show();
            }
        });
        
		programScrollPane = new JScrollPane(programView);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, programScrollPane, new JScrollPane(dataView));
        getContentPane().add(splitPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(50, 50, 700, 500);
        validate();
        setVisible(true);
        splitPane.setDividerLocation(0.5);
    }
    
    void step() {
        if (!Machine.instance.stack.isEmpty()) {
            JavaStackFrame frame = (JavaStackFrame) Machine.instance.stack.peek();
            try {
                if (frame.nextStatement != null)
                    frame.nextStatement.execute();
                else {
                    frame.javaMethodBase.doReturn();
                }
                dataView.dataChanged();
				executionLocationChanged();
                repaint();
            } catch (JavaExecutionException e) {
                executeError(e.getMessage());
            }
        }
    }

	void executionLocationChanged() {
		if (!Machine.instance.stack.isEmpty()) 
		{
			Machine.instance.selectTopStackFrame();
			programView.scrollToExecutionLocation(programScrollPane.getViewport());
		}
	}
    
    void stopRunning() {
        Machine.instance.stack.clear();
        dataView.dataChanged();
        repaint();
    }

    void resetMachine() {
        stopRunning();
        Machine.instance.heap.clear();
        dataView.dataChanged();
        repaint();
    }
    
    void programChanged() {
        resetMachine();
        programView.programChanged();
        repaint();
    }
    
    void newProgram() {
        Machine.instance.program.clear();
        programChanged();
        commands.clear();
        commandCount = 0;
        undoMenuItem.setEnabled(commandCount > 0);
        redoMenuItem.setEnabled(commandCount < commands.size());
    }
    
    void executeError(String message) {
        JOptionPane.showMessageDialog(this, message, "JBeginner", JOptionPane.ERROR_MESSAGE);
    }
    
    void doCommand(Command command) {
        // Remove commands that have been undone.
        commands.subList(commandCount, commands.size()).clear();
        commands.add(command);
        commandCount++;
        command.doIt();
        programChanged();
        undoMenuItem.setEnabled(commandCount > 0);
        redoMenuItem.setEnabled(commandCount < commands.size());
    }
    
    void undo() {
        Command command = (Command) commands.get(--commandCount);
        command.undoIt();
        programChanged();
        undoMenuItem.setEnabled(commandCount > 0);
        redoMenuItem.setEnabled(commandCount < commands.size());
    }
    
    void redo() {
        Command command = (Command) commands.get(commandCount++);
        command.doIt();
        programChanged();
        undoMenuItem.setEnabled(commandCount > 0);
        redoMenuItem.setEnabled(commandCount < commands.size());
    }
}

class ExplorerApplication {
    public static void main(String[] args) {
        // Do all initialization on the AWT thread.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Explorer.instance.show();
            }
        });
    }
}