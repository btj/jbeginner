// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

interface Priority {
    int primary = Integer.MAX_VALUE;
    int unary = 15;
    int multiplicative = 10;
    int additive = 9;
    int relational = 5;
    int equality = relational;
    int xor = 4;
    int and = 3;
    int or = 2;
}

abstract class Expression {
    static void checkNotNull(Object object) {
        if (object == null)
            throw new JavaExecutionException("NullPointerException");
    }
    
    abstract Object evaluate();
    int getPriority() {
        return Priority.primary;
    }
    void addText(LineBuilder builder, ExpressionContainer c) {
        builder.append(toString(), c);
    }
}

class ExpressionContainer implements PopupMenuFactory {
    static List createArgumentExpressionContainers(EnvSource envSource, List parameters, List expressions) {
        List cs = new ArrayList(parameters.size());
        for (int i = 0; i < expressions.size(); i++) {
            VarDecl parameter = (VarDecl) parameters.get(i);
            Expression argument = (Expression) expressions.get(i);
            cs.add(new ExpressionContainer(envSource, parameter.type, argument));
        }
        return cs;
    }

    EnvSource envSource;
    JavaType type;
    Expression value;

    ExpressionContainer(EnvSource envSource, JavaType type, Expression value) {
        this.envSource = envSource;
        this.type = type;
        this.value = value;
    }
    
    void addText(LineBuilder lineBuilder) {
        value.addText(lineBuilder, this);
    }

    Expression get() {
        return value;
    }
    
    public void put(final Expression e) {
        final Expression oldValue = value;
        Explorer.instance.doCommand(new Command() {
            public void doIt() {
                value = e;
            }
            public void undoIt() {
                value = oldValue;
            }
        });
    }
    
    public JPopupMenu getPopupMenu() {
        return StatementSwingUtil.createExpressionPopupMenu(this);
    }
}

interface ExpressionFactory {
    interface Sink {
        void add(Expression expression);
        void add(String label, Runnable putter);
    }
    
    String getName();
    void add(Map env, JavaType type, ExpressionContainer c, Sink sink);
}

class ExpressionFactories {
    static final ExpressionFactory localVariable = new ExpressionFactory() {
        public String getName() { return "Local Variable"; }
        public void add(Map env, JavaType type, ExpressionContainer c, Sink sink) {
            Iterator iter = env.values().iterator();
            while (iter.hasNext()) {
                final VarDecl decl = (VarDecl) iter.next();
                if (decl.type == type) {
                    sink.add(decl);
                }
            }
        }
    };
    
    static final ExpressionFactory literal = new ExpressionFactory() {
        public String getName() { return "Literal"; }
        public void add(Map env, final JavaType type, final ExpressionContainer c, Sink sink) {
            Object[] literalValues = type.getAllLiteralValues();
            boolean requiresEditor = literalValues == null;
            if (requiresEditor) 
                literalValues = type.getSomeLiteralValues();
            for (int i = 0; i < literalValues.length; i++)
                sink.add(new Literal(literalValues[i]));
            if (requiresEditor) {
                sink.add("Enter...", new Runnable() {
                    public void run() {
                        final JDialog dialog = new JDialog(Explorer.instance, "Edit Expression", true);
                        Object value = c.get() instanceof Literal ? ((Literal) c.get()).value : type.getDefaultValue();
                        final LiteralEditor editor = type.getLiteralEditor(value);
                        JPanel content = new JPanel(new BorderLayout(0, 17));
                        dialog.setContentPane(content);
                        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
                        JPanel dataPanel = new JPanel(new BorderLayout(12, 0));
                        dataPanel.add(editor.getJComponent());
                        dataPanel.add(new JLabel("Literal:"), BorderLayout.WEST);
                        content.add(dataPanel);
                        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                        JButton okButton = new JButton("OK");
                        commandPanel.add(okButton);
                        content.add(commandPanel, BorderLayout.SOUTH);
                        okButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                editor.tryGetValue(new ErrorContinuation() {
                                    public void success(Object value) {
                                        dialog.dispose();
                                        c.put(new Literal(value));
                                    }
                                    public void error(String message) {
                                        JOptionPane.showMessageDialog(dialog, message, "Edit Expression", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            }
                        });
                        dialog.getRootPane().setDefaultButton(okButton);
                        dialog.pack();
                        dialog.setLocationRelativeTo(Explorer.instance);
                        dialog.show();
                    }
                });
            }
        }
    };
    
    static final ExpressionFactory fieldUse = new ExpressionFactory() {
        public String getName() { return "Field"; }
        public void add(Map env, JavaType type, ExpressionContainer c, Sink sink) {
            Iterator targetIter = env.values().iterator();
            while (targetIter.hasNext()) {
                VarDecl target = (VarDecl) targetIter.next();
                if (target.type instanceof JavaClass) {
                    JavaClass javaClass = (JavaClass) target.type;
                    Iterator fieldIter = javaClass.fields.values().iterator();
                    while (fieldIter.hasNext()) {
                        JavaField field = (JavaField) fieldIter.next();
                        if (field.type == type) {
                            Expression e = new FieldUse(target, field);
                            sink.add(e);
                        }
                    }
                }
            }
        }
    };
    
    static final ExpressionFactory binaryOperation = new ExpressionFactory() {
        public String getName() { return "Operator"; }
        public void add(Map env, JavaType type, ExpressionContainer c, Sink sink) {
            if (type == JavaType.booleanType) {
                Iterator iter = env.values().iterator();
                while (iter.hasNext()) {
                    VarDecl decl = (VarDecl) iter.next();
                    if (decl.type != JavaType.booleanType)
                        sink.add(new Comparison(c.envSource, decl.type, decl, new Literal(decl.type.getDefaultValue())));
                }
            }
            
            for (int i = 0; i < BinaryOperators.binaryOperators.length; i++) {
                BinaryOperator operator = BinaryOperators.binaryOperators[i];
                if (operator.getResultType() == type) {
                    Object v = operator.getOperandType().getDefaultValue();
                    sink.add(new GenericBinaryOperation(c.envSource, new Literal(v), operator, new Literal(v)));
                }
            }
            
            if (type == JavaType.booleanType) {
                sink.add(new LogicalNegation(c.envSource, new Literal(Boolean.FALSE)));
            }
        }
    };
    
    static final ExpressionFactory[] factories = {
        localVariable, literal, fieldUse, binaryOperation
    };
}

class VarDecl extends Expression {
    JavaType type;
    String name;
    
    VarDecl(JavaType type, String name) {
        this.type = type;
        this.name = name;
    }
    
    String getDeclarationText() {
        StringBuffer buffer = new StringBuffer();
        appendDeclarationText(buffer);
        return buffer.toString();
    }
    
    void addDeclarationText(LineBuilder builder, final EnvSource envSource, final StaticScope staticScope) {
        builder.append(type.getTypeText());
        builder.append(" ");
        builder.append(name, new PopupMenuFactory() {
            public JPopupMenu getPopupMenu() {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem renameMenuItem = new JMenuItem("Rename...");
                menu.add(renameMenuItem);
                renameMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        new NameDialog("Rename Local Variable", name) {
                            void nameEntered(String newName) {
                                if (newName.equals(name)) {
                                    dispose();
                                } else if (envSource.getEnv().containsKey(newName) || staticScope.declares(newName)) {
                                    error("This name would conflict with an existing local variable. Please choose another name.");
                                } else {
                                    dispose();
                                    name = newName;
                                    Explorer.instance.programChanged();
                                }
                            }
                        }.show();
                    }
                });
                return menu;
            }
        });
    }
    
    void appendDeclarationText(StringBuffer buffer) {
        buffer.append(type.getTypeText());
        buffer.append(' ');
        buffer.append(name);
    }
    
    public String toString() {
        return name;
    }
    
    Object evaluate() {
        JavaStackFrame frame = Machine.instance.top();
        return frame.getValue(this);
    }
}

class Literal extends Expression {
    Object value;
    
    Literal(Object value) {
        this.value = value;
    }
    
    public String toString() {
        return String.valueOf(value);
    }
    
    Object evaluate() {
        return value;
    }
}

class FieldUse extends Expression {
    VarDecl target;
    JavaField field;
    
    FieldUse(VarDecl target, JavaField field) {
        this.target = target;
        this.field = field;
    }
    
    Object evaluate() {
        JavaObject targetObject = (JavaObject) target.evaluate();
        checkNotNull(targetObject);
        return targetObject.getFieldValue(field);
    }
    
    public String toString() {
        return target.name + "." + field.name;
    }
}

class LogicalNegation extends Expression {
    ExpressionContainer operand;
    
    LogicalNegation(EnvSource envSource, Expression operand) {
        this.operand = new ExpressionContainer(envSource, JavaType.booleanType, operand);
    }
    
    Object evaluate() {
        return ((Boolean) operand.get().evaluate()).booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    }
    
    void addText(LineBuilder builder, ExpressionContainer c) {
        builder.append("!", c);
        boolean parens = operand.get().getPriority() < Priority.unary;
        if (parens)
            builder.append("(");
        operand.addText(builder);
        if (parens)
            builder.append(")");
    }
    
    public String toString() {
        LineBuilder builder = new LineBuilder();
        addText(builder, null);
        return builder.buffer.toString();
    }
}

abstract class BinaryOperation extends Expression {
    ExpressionContainer op1;
    ExpressionContainer op2;
    
    BinaryOperation(EnvSource envSource, JavaType operandType, Expression op1, Expression op2) {
        this.op1 = new ExpressionContainer(envSource, operandType, op1);
        this.op2 = new ExpressionContainer(envSource, operandType, op2);
    }
    
    abstract Object evaluate(Object v1, Object v2);
    abstract String getOperatorText();
    abstract int getLeftMinPriority();
    abstract int getRightMinPriority();
    
    Object evaluate() {
        Object v1 = op1.get().evaluate();
        Object v2 = op2.get().evaluate();
        return evaluate(v1, v2);
    }
    
    void addText(LineBuilder builder, ExpressionContainer c) {
        {
            boolean parens = op1.get().getPriority() < getLeftMinPriority();
            if (parens)
                builder.append("(");
            op1.addText(builder);
            if (parens)
                builder.append(")");
        }
        builder.append(" ");
        builder.append(getOperatorText(), c);
        builder.append(" ");
        {
            boolean parens = op2.get().getPriority() < getRightMinPriority();
            if (parens)
                builder.append("(");
            op2.addText(builder);
            if (parens)
                builder.append(")");
        }
    }
    
    public String toString() {
        LineBuilder builder = new LineBuilder();
        addText(builder, null);
        return builder.buffer.toString();
    }
}

class Comparison extends BinaryOperation {
    Comparison(EnvSource envSource, JavaType operandType, Expression op1, Expression op2) {
        super(envSource, operandType, op1, op2);
    }
    
    Object evaluate(Object v1, Object v2) {
        boolean result = v1 == null || v2 == null ? v1 == v2 : v1.equals(v2);
        return result ? Boolean.TRUE : Boolean.FALSE;
    }
    
    int getPriority() { return Priority.equality; }
    String getOperatorText() { return "=="; }
    int getLeftMinPriority() { return Priority.equality + 1; }
    int getRightMinPriority() { return Priority.equality + 1; }
}

class GenericBinaryOperation extends BinaryOperation {
    BinaryOperator operator;
    
    GenericBinaryOperation(EnvSource envSource, Expression op1, BinaryOperator operator, Expression op2) {
        super(envSource, operator.getOperandType(), op1, op2);
        this.operator = operator;
    }
    
    Object evaluate(Object v1, Object v2) {
        return operator.evaluate(v1, v2);
    }

    int getPriority() { return operator.getPriority(); }
    String getOperatorText() { return operator.getOperatorToken(); }
    int getLeftMinPriority() { return operator.getLeftMinPriority(); }
    int getRightMinPriority() { return operator.getRightMinPriority(); }
}