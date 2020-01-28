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

interface MenuFactory {
    JMenu createMenu();
}

class Action {
    String name;
    Runnable handler;
    JMenu menu;
    MenuFactory menuFactory;
    
    Action(String name, Runnable handler) {
        this.name = name;
        this.handler = handler;
    }
    
    Action(String name, JMenu menu) {
        this.name = name;
        this.menu = menu;
    }
    
    Action(String name, MenuFactory menuFactory) {
        this.name = name;
        this.menuFactory = menuFactory;
    }
}

interface LineSink {
    void addLine(String text);
    void addLine(Line line);
    void addLine(String text, Statement statement);
    void addLine(Line line, Statement statement);
    void addLine(String text, JavaMethodBase methodBase);
    void addLine(Line line, JavaMethodBase methodBase);
    void addLine(String text, Action[] actions, JavaMethodBase methodBase);
}

interface PopupMenuFactory {
    JPopupMenu getPopupMenu();
}

class HotSpot {
    int left;
    /** Index of the rightmost column that is part of the hot spot. */
    int right;
    PopupMenuFactory popupMenuFactory;
    
    HotSpot(int left, int right, PopupMenuFactory popupMenuFactory) {
        this.left = left;
        this.right = right;
        this.popupMenuFactory = popupMenuFactory;
    }
}

class LineBuilder {
    StringBuffer buffer = new StringBuffer();
    List hotSpots = new ArrayList();
    
    void append(String text) {
        buffer.append(text);
    }
    
    void append(String text, PopupMenuFactory popupMenuFactory) {
        int left = buffer.length();
        hotSpots.add(new HotSpot(left, left + text.length() - 1, popupMenuFactory));
        buffer.append(text);
    }
    
    Line toLine() {
        Line line = new Line(buffer.toString());
        line.setHotSpots(hotSpots);
        // Prevent changes to the hotSpots object.
        hotSpots = null;
        return line;
    }
}

class Line {
    String text;
    List actions;
    List hotSpots;
    
    Line(String text, List actions) {
        this.text = text;
        this.actions = actions;
    }
    
    Line(String text, Action[] actions) {
        this(text, Arrays.asList(actions));
    }
    
    Line(String text) {
        this(text, new Action[] {});
    }

    void setHotSpots(List hotSpots) {
        this.hotSpots = hotSpots;
    }
    
    void setActions(Action[] actions) {
        this.actions = Arrays.asList(actions);
    }
    
    void setActions(List actions) {
        this.actions = actions;
    }
}

class ProgramView extends JPanel {
    JavaProgram program;
    List lines;
    HashMap statementLines;
    HashMap methodBaseDoneLines;
    int maxLineLength;
    
    ProgramView(JavaProgram program) {
        this.program = program;
        generateLines();
        setBackground(Color.white);
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseEventOccurred(e);
            }
            public void mouseReleased(MouseEvent e) {
                mouseEventOccurred(e);
            }
        });
    }
    
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        StringBuffer buffer = new StringBuffer(maxLineLength);
        for (int i = 0; i < maxLineLength; i++) {
            buffer.append('x');
        }
        String s = buffer.toString();
        int width = metrics.stringWidth(s) + 1;
        int height = lines.size() * metrics.getHeight();
        return new Dimension(width, height);
    }
    
    void programChanged() {
        generateLines();
        revalidate();
    }
    
    void mouseEventOccurred(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        FontMetrics metrics = getFontMetrics(getFont());
        int lineIndex = y / metrics.getHeight();
        
        if (e.isPopupTrigger()) {
            if (0 <= lineIndex && lineIndex < lines.size()) {
                Line line = (Line) lines.get(lineIndex);
                
                JPopupMenu menu = null;
                
                if (line.text.length() > 0) {
                    int column = x * line.text.length() / metrics.stringWidth(line.text);
                    
                    if (line.hotSpots != null) {
                        for (int i = 0; i < line.hotSpots.size(); i++) {
                            HotSpot hotSpot = (HotSpot) line.hotSpots.get(i);
                            if (hotSpot.left <= column && column <= hotSpot.right) {
                                menu = hotSpot.popupMenuFactory.getPopupMenu();
                                break;
                            }
                        }
                    }
                }
                
                List actions = line.actions;
                if (menu == null && actions.size() > 0) {
                    menu = new JPopupMenu();
                    for (int i = 0; i < actions.size(); i++) {
                        final Action action = (Action) actions.get(i);
                        if (action.handler != null) {
                            JMenuItem item = new JMenuItem(action.name);
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    action.handler.run();
                                    Explorer.instance.repaint();
                                }
                            });
                            menu.add(item);
                        } else {
                            JMenu submenu;
                            if (action.menu != null)
                                submenu = action.menu;
                            else
                                submenu = action.menuFactory.createMenu();
                            menu.add(submenu);
                        }
                    }
                }
                
                if (menu != null)
                    menu.show(this, x, y);
            }
        }
    }
    
    void generateLines() {
        lines = new ArrayList();
        statementLines = new HashMap();
        methodBaseDoneLines = new HashMap();
        maxLineLength = 0;
        final Action[] noActions = new Action[] {};
        program.addLines(new LineSink() {
            public void addLine(String text) {
                addLine(new Line(text));
            }
            public void addLine(Line line) {
                lines.add(line);
                maxLineLength = Math.max(maxLineLength, line.text.length());
            }
            public void addLine(String text, Statement statement) {
                addLine(new Line(text), statement);
            }
            public void addLine(Line line, Statement statement) {
                statementLines.put(statement, new Integer(lines.size()));
                addLine(line);
            }
            public void addLine(String text, JavaMethodBase methodBase) {
                addLine(text, noActions, methodBase);
            }
            public void addLine(Line line, JavaMethodBase methodBase) {
                methodBaseDoneLines.put(methodBase, new Integer(lines.size()));
                addLine(line);
            }                
            public void addLine(String text, Action[] actions, JavaMethodBase methodBase) {
                addLine(new Line(text, actions), methodBase);
            }
        });
    }
    
	int getExecutionLocation() {
		JavaStackFrame frame = Machine.instance.getSelectedStackFrame();
		int lineIndex;
		if (frame.nextStatement == null)
			lineIndex = ((Integer) methodBaseDoneLines.get(frame.javaMethodBase)).intValue();
		else
			lineIndex = ((Integer) statementLines.get(frame.nextStatement)).intValue();
		return lineIndex;
	}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Font font = getFont();
        FontMetrics fontMetrics = getFontMetrics(font);
        
        if (!Machine.instance.stack.isEmpty()) {
            int lineIndex = getExecutionLocation();
			boolean active = Machine.instance.isSelectedStackFrameActive();
            g.setColor(active ? DataView.activeColor : DataView.inactiveColor);
            g.fillRect(0, fontMetrics.getHeight() * lineIndex, getWidth(), fontMetrics.getHeight());
        }
        
        g.setColor(Color.black);
        int n = lines.size();
        for (int i = 0; i < n; i++) {
            Line line = (Line) lines.get(i);
            g.drawString(line.text, 1, (i + 1) * fontMetrics.getHeight() - fontMetrics.getDescent());
        }
    }

	void scrollToExecutionLocation(JViewport viewport) {
		int lineIndex = getExecutionLocation();
		Font font = getFont();
		FontMetrics fontMetrics = getFontMetrics(font);
		int fontHeight = fontMetrics.getHeight();
		int top = fontHeight * lineIndex;
		int bottom = top + fontHeight;
		Rectangle extent = viewport.getViewRect();
		if (top < extent.y || extent.y + extent.height < bottom) {
			int y;
			if (extent.height <= fontHeight)
				y = top;
			else if (top < extent.y)
				y = top;
			else
				y = bottom - extent.height;
			viewport.setViewPosition(new Point(0, y));
		}
	}
}