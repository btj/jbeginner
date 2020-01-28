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

class StatementSwingUtil {
    static JPopupMenu createExpressionPopupMenu(final ExpressionContainer c) {
        Map env = c.envSource.getEnv();
        JPopupMenu menu = new JPopupMenu();

        for (int i = 0; i < ExpressionFactories.factories.length; i++) {
            ExpressionFactory factory = ExpressionFactories.factories[i];
            final JMenu submenu = new JMenu(factory.getName());
            factory.add(env, c.type, c, new ExpressionFactory.Sink() {
                public void add(final Expression e) {
                    add(e.toString(), new Runnable() {
                        public void run() {
                            c.put(e);
                        }
                    });
                }
                public void add(String label, final Runnable putter) {
                    JMenuItem item = new JMenuItem(label);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            putter.run();
                        }
                    });
                    submenu.add(item);
                }
            });
            if (submenu.getItemCount() > 0)
                menu.add(submenu);
        }
        
        return menu;
    }
    
    static Action createAddStatementAction(EnvSource parent, final StaticScope staticScope, final List statements) {
        final EnvSource envSource;
        if (statements.size() == 0)
            envSource = parent;
        else
            envSource = (Statement) statements.get(statements.size() - 1);
        
        Action addStatement = new Action("Add Statement", new MenuFactory() {
            public JMenu createMenu() {
                javax.swing.JMenu menu = StatementSwingUtil.createAddStatementMenu(envSource, staticScope, new StatementContinuation() {
                    public void run(final Statement statement) {
                        Explorer.instance.doCommand(new Command() {
                            public void doIt() {
                                statements.add(statement);
                            }
                            public void undoIt() {
                                statements.remove(statements.size() - 1);
                            }
                        });
                    }
                });
                return menu;
            }
        });
        
        return addStatement;
    }
    
    static Action createRemoveStatementAction(final List statements) {
        return new Action("Remove Statement", new Runnable() {
            public void run() {
                Explorer.instance.doCommand(new Command() {
                    Statement statement;
                    public void doIt() {
                        statement = (Statement) statements.remove(statements.size() - 1);
                    }
                    public void undoIt() {
                        statements.add(statement);
                    }
                });
           }
        });
    }
    
    static JMenu createAddStatementMenu(final EnvSource envSource, final StaticScope staticScope, final StatementContinuation cont) {
        final Map env = envSource.getEnv();
        JMenu menu = new JMenu("Add Statement");
        for (int i = 0; i < StatementFactories.factories.length; i++) {
            final StatementFactory factory = StatementFactories.factories[i];
            final JMenu stmtMenu = new JMenu(factory.getName());
            factory.addStatements(env, envSource, staticScope, new StatementSink() {
                public void add(String label, final Statement stmt) {
                    JMenuItem item = new JMenuItem(label);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            cont.run(stmt);
                        }
                    });
                    stmtMenu.add(item);
                }
            });
            if (stmtMenu.getItemCount() > 0)
                menu.add(stmtMenu);
        }
        return menu;
    }
}