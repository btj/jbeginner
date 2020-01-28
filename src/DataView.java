// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.List;

class ReferenceView {
    int variableX;
    int variableY;
    JavaObject target;
    
    ReferenceView(int variableX, int variableY, JavaObject target) {
        this.variableX = variableX;
        this.variableY = variableY;
        this.target = target;
    }
    
    void draw(Graphics g) {
        g.setColor(Color.black);
        g.fillOval(variableX - 3, variableY - 3, 6, 6);
        
        int left = target.x;
        int right = left + target.javaClass.screenWidth;
        int top = target.y;
        int bottom = top + target.javaClass.screenHeight;
        
        int arrowHeadX;
        int arrowHeadY;
        
        if (left <= variableX && variableX <= right && top <= variableY && variableY <= bottom) {
            int targetCenterX = (left + right) / 2;
            int targetCenterY = (top + bottom) / 2;
            arrowHeadX = targetCenterX;
            arrowHeadY = targetCenterY;
        } else {
            arrowHeadX = variableX < left ? left : variableX < right ? variableX : right;
            arrowHeadY = variableY < top ? top : variableY < bottom ? variableY : bottom;
        }

        g.drawLine(variableX, variableY, arrowHeadX, arrowHeadY);
        
        int deltaX = variableX - arrowHeadX;
        int deltaY = variableY - arrowHeadY;
        int length = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (length > 0) {
            int vx = deltaX * 10 / length;
            int vy = deltaY * 10 / length;
            int[] xs = new int[3];
            int[] ys = new int[3];
            xs[0] = arrowHeadX;
            ys[0] = arrowHeadY;
            xs[1] = arrowHeadX + vx - vy / 2;
            ys[1] = arrowHeadY + vy + vx / 2;
            xs[2] = arrowHeadX + vx + vy / 2;
            ys[2] = arrowHeadY + vy - vx / 2;
            g.fillPolygon(xs, ys, 3);
        }
    }
}

class DataView extends JPanel {
    static final int margin = 10;
    static final int innerMargin = 6;
    static final int padding = 3;
    static final Color activeColor = SwingUtil.desaturate(Color.yellow);
    static final Color inactiveColor = SwingUtil.desaturate(Color.green);
    static final int radius = 15;
    
	Dimension preferredSize = new Dimension();
	/** Right edge of stack view. */
	int stackRight = 150;
	boolean showArrows;
    boolean showObjectNames;
    Rectangle[] frameRectangles;
	MouseInputListener pressedMouseInputListener;
    
    DataView() {
		MouseInputListener baseMouseInputListener = new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
			    DataView.this.mousePressed(e);
			}
			public void mouseReleased(MouseEvent e) {
			    if (pressedMouseInputListener != null)
			        pressedMouseInputListener.mouseReleased(e);
			    pressedMouseInputListener = null;
            }
            public void mouseDragged(MouseEvent e) {
                if (pressedMouseInputListener != null)
                    pressedMouseInputListener.mouseDragged(e);
            }
		};
		addMouseListener(baseMouseInputListener);
		addMouseMotionListener(baseMouseInputListener);
	}
	
	void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
        
        {
            Iterator iter = Machine.instance.heap.objects.values().iterator();
            while (iter.hasNext()) {
                final JavaObject object = (JavaObject) iter.next();
                if (object.x <= x && object.y <= y && x < object.x + object.javaClass.screenWidth && y < object.y + object.javaClass.screenHeight) {
                    if (e.isPopupTrigger()) {
                        pressedMouseInputListener = null;
                        showObjectPopupMenu(object, x, y);
                    } else {
                        final int deltaX = x - object.x;
                        final int deltaY = y - object.y;
                        pressedMouseInputListener = new MouseInputAdapter() {
                            boolean dragged;
                            public void mouseDragged(MouseEvent e) {
                                dragged = true;
                                object.x = e.getX() - deltaX;
                                object.y = e.getY() - deltaY;
                                repaint();
                            }
                            public void mouseReleased(MouseEvent e) {
                                if (dragged) {
                                    object.x = e.getX() - deltaX;
                                    object.y = e.getY() - deltaY;
                                    repaint();
                                } else {
                                    if (e.isPopupTrigger()) {
                                        pressedMouseInputListener = null;
                                        showObjectPopupMenu(object, e.getX(), e.getY());
                                    }
                                }
                            }
                        };
                    }
                    return;
                }
            }
        }
        
        for (int i = 0; i < frameRectangles.length; i++) {
			if (frameRectangles[i].contains(x, y)) 
			{
			    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
    				Machine.instance.selectedStackFrameIndex = i;
    				Explorer.instance.repaint();
    			}
				return;
			}
		}
    }
    
    void showObjectPopupMenu(final JavaObject object, int x, int y) {
        Map methods = object.javaClass.methods;
        boolean empty = true;
        JPopupMenu menu = new JPopupMenu();
        JMenu methodMenu = new JMenu("Invoke Method");
        menu.add(methodMenu);
        Iterator iter = methods.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            final JavaMethod method = (JavaMethod) entry.getValue();
            if (!method.isStatic()) {
                JMenuItem item = new JMenuItem(name);
                methodMenu.add(item);
                empty = false;
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        method.invoke(object);
                        Explorer.instance.repaint();
                    }
                });
            }
        }
        if (!empty)
            menu.show(this, x, y);
    }
    
    public Dimension getPreferredSize() {
        paintComponent(null);
        return new Dimension(preferredSize);
    }
    
    void dataChanged() {
        revalidate();
    }
    
    public void paintComponent(Graphics g) {
        if (g != null)
            super.paintComponent(g);
        
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        int fontAscent = metrics.getAscent();
        int fontDescent = metrics.getDescent();
        int fontExtent = fontAscent + fontDescent;

        int width = getWidth();
        int halfWidth = width / 2;
        
        int halfStackRight = stackRight / 2;
        
		int selectedFrameIndex = Machine.instance.selectedStackFrameIndex;
		
        Stack stack = Machine.instance.stack;
        
		frameRectangles = new Rectangle[stack.size()];
		
		List referenceViews = new ArrayList();

        int y = margin;
        for (int i = 0; i < stack.size(); i++) {
            JavaStackFrame frame = (JavaStackFrame) stack.get(i);
            
            int frameHeight = 2 * innerMargin + (fontExtent + 4 + padding) * (1 + frame.getVariableCount()) - padding;
            
            if (g != null) {
                g.setColor(Color.black);
                g.drawRect(margin, y, stackRight - 2 * margin - 1, frameHeight + 1);
            }
            
            y++;
            
			frameRectangles[i] = new Rectangle(margin + 1, y, stackRight - 2 * margin - 2, frameHeight);

            if (g != null) {
                g.setColor(i == stack.size() - 1 ? activeColor : inactiveColor);
                g.fillRect(margin + 1, y, stackRight - 2 * margin - 2, frameHeight);
				if (i == selectedFrameIndex) {
					g.setColor(Color.black);
					g.drawRect(margin + 1, y, stackRight - 2 * margin - 3, frameHeight - 1);
				}
			}
            
            y += innerMargin;

            y += 2 + fontAscent;
			if (g != null) 
			{
				g.setColor(Color.black);
				Font regularFont = g.getFont();
				Font boldFont = SwingUtil.deriveBoldFont(regularFont);
				g.setFont(boldFont);
				SwingUtil.drawStringCentered(g, frame.javaMethodBase.toString(), y, margin + 1 + innerMargin, stackRight - margin - 1 - innerMargin);
				g.setFont(regularFont);
			}
			y += fontDescent + 2;

            Stack scopeStack = frame.scopeStack;
            for (int j = 0; j < scopeStack.size(); j++) {
                Scope scope = (Scope) scopeStack.get(j);
                List vars = scope.varList;
                for (int k = 0; k < vars.size(); k++) {
                    StackVariable var = (StackVariable) vars.get(k);

                    y += padding;
                    
                    int variableLeft = halfStackRight;
                    int variableTop = y;
                    int variableWidth = halfStackRight - margin - 1 - innerMargin;
                    int variableHeight = fontExtent + 4;
                    
                    y += 2 + fontAscent;
                    
                    if (g != null) {
                        g.setColor(Color.black);
                        SwingUtil.drawStringRightJustified(g, var.decl.name, y, margin + 1 + innerMargin, halfStackRight - 4);
                        drawVariable(g, referenceViews, var, variableLeft, variableTop, variableWidth, variableHeight, y);
                    }
                    
                    y += fontDescent + 2;
                }
            }
            
            y += innerMargin;
        }
        
        y += 1;
        
        if (preferredSize.width < stackRight) {
            preferredSize.width = stackRight + margin;
            revalidate();
        }
        if (preferredSize.height < y) {
            preferredSize.height = y + margin;
            revalidate();
        }

        paintObjects(g, referenceViews);
        
        if (g != null) {
            for (int i = 0; i < referenceViews.size(); i++) {
                ((ReferenceView) referenceViews.get(i)).draw(g);
            }
        }
    }

    public void paintObjects(Graphics g, List referenceViews) {
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int fontAscent = metrics.getAscent();
        int fontDescent = metrics.getDescent();
        int fontExtent = fontAscent + fontDescent;

        Font regularFont = null;
        Font boldFont = null;

        if (g != null) {
            regularFont = g.getFont();
            boldFont = SwingUtil.deriveBoldFont(regularFont);
        }

        int defaultX = stackRight + margin;
        int defaultY = margin + fontExtent + 2;
        
        Iterator iter = Machine.instance.heap.objects.values().iterator();
        while (iter.hasNext()) {
            JavaObject javaObject = (JavaObject) iter.next();
            int width = javaObject.javaClass.screenWidth;
            if (!javaObject.positioned) {
                javaObject.x = defaultX;
                javaObject.y = defaultY;
                javaObject.positioned = true;
            }
            int x = javaObject.x;
            int y = javaObject.y - fontExtent - 2;
            int top = y;
            String name = javaObject.name;
            y += fontAscent;
            if (g != null && showObjectNames)
                SwingUtil.drawStringCentered(g, name, y, x, x + width);
            y += fontDescent + 2;
            int fieldCount = javaObject.fieldValues.size();
            int objectHeight = (fontExtent + 5) * (1 + fieldCount) + 3;
            defaultX = javaObject.x;
            defaultY = javaObject.y + objectHeight + margin + fontExtent + 2;
            javaObject.javaClass.screenHeight = objectHeight;
            if (preferredSize.width < javaObject.x + width) {
                preferredSize.width = javaObject.x + width + margin;
                revalidate();
            }
            if (preferredSize.height < javaObject.y + objectHeight) {
                preferredSize.height = javaObject.y + objectHeight + margin;
                revalidate();
            }
            if (g != null) {
                g.setColor(Color.pink);
                g.fillRoundRect(x, y, width, objectHeight, radius, radius);
                g.setColor(Color.black);
                y += 2 + fontAscent;
                g.setFont(boldFont);
                SwingUtil.drawStringCentered(g, javaObject.javaClass.name, y, x, x + width);
                g.setFont(regularFont);
                y += fontDescent + 3;
                Iterator fieldIter = javaObject.fieldValues.values().iterator();
                while (fieldIter.hasNext()) {
                    HeapVariable variable = (HeapVariable) fieldIter.next();
                    int variableLeft = x + width / 2;
                    int variableTop = y + 1;
                    int variableWidth = width / 2 - 6;
                    int variableHeight = fontExtent + 2;
                    y += fontAscent + 2;
                    g.setColor(Color.black);
                    SwingUtil.drawStringRightJustified(g, variable.field.name, y, x, x + width / 2 - 4);
                    drawVariable(g, referenceViews, variable, variableLeft, variableTop, variableWidth, variableHeight, y);
                    y += fontDescent + 3;
                }
            }
        }
    }
    
    void drawVariable(Graphics g, List referenceViews, Variable variable, int left, int top, int width, int height, int baseline) {
        g.setColor(Color.white);
        g.fillRect(left, top, width, height);
        g.setColor(Color.black);
        if (showArrows && variable.getType() instanceof JavaClass && variable.getValue() != null)
            referenceViews.add(new ReferenceView(left + width / 2, top + height / 2, (JavaObject) variable.getValue()));
        else
            SwingUtil.drawStringBounded(g, String.valueOf(variable.getValue()), baseline, left + 2, left + width - 2);
    }
}