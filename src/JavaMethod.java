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

abstract class JavaMethodBase implements EnvSource, StaticScope {
    JavaClass javaClass;
    VarDecl thisVariable;
    List parameters = new ArrayList();
    StatementList statements;

    JavaMethodBase(JavaClass javaClass, boolean isStatic, List parameters) {
        this.javaClass = javaClass;
        if (!isStatic)
            thisVariable = new VarDecl(javaClass, "this");
        this.parameters = parameters;
        statements = new StatementList(this, false);
    }
    
    public Map getEnv() {
        Map environment = new TreeMap();
        if (thisVariable != null)
            environment.put("this", thisVariable);
        for (int i = 0; i < parameters.size(); i++) {
            VarDecl decl = (VarDecl) parameters.get(i);
            environment.put(decl.name, decl);
        }
        return environment;
    }
    
    public boolean declares(String name) {
        for (int i = 0; i < parameters.size(); i++) {
            VarDecl parameter = (VarDecl) parameters.get(i);
            if (parameter.name.equals(name))
                return true;
        }
        return statements.declares(name);
    }
    
    boolean isStatic() {
        return thisVariable == null;
    }
    
    abstract Object getResult();

    void doReturn() {
        Object result = getResult();
        ReturnContinuation cont = Machine.instance.top().returnContinuation;
        Machine.instance.stack.pop();
        Explorer.instance.dataView.dataChanged();
        cont.run(result);
    }
    
    void addParameterListText(StringBuffer buffer) {
        buffer.append('(');
        boolean first = true;
        Iterator iter = parameters.iterator();
        while (iter.hasNext()) {
            if (first)
                first = false;
            else
                buffer.append(", ");
            VarDecl var = (VarDecl) iter.next();
            var.appendDeclarationText(buffer);
        }
        buffer.append(')');
    }
    
    void invoke(Object target, Object[] arguments, ReturnContinuation cont) {
        final JavaStackFrame frame = new JavaStackFrame();
        frame.javaMethodBase = this;
        frame.returnContinuation = cont;
        Scope scope = new Scope();
        if (thisVariable != null)
            scope.add(thisVariable, target);
        for (int i = 0; i < parameters.size(); i++) {
            scope.add((VarDecl) parameters.get(i), arguments[i]);
        }
        frame.scopeStack.push(scope);
        Machine.instance.stack.push(frame);
        Explorer.instance.dataView.dataChanged();
        statements.execute(new Runnable() {
            public void run() {
                frame.nextStatement = null;
                frame.continuation = null;
            }
        });
    }
}

class JavaConstructor extends JavaMethodBase {
    JavaConstructor(JavaClass javaClass, List parameters) {
        super(javaClass, false, parameters);
    }
    
    Object getResult() {
        return null;
    }
    
    void addLines(LineSink sink) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    ");
        buffer.append(javaClass.name);
        addParameterListText(buffer);
        buffer.append(" {");

        ArrayList actions = new ArrayList();        
        actions.add(new Action("Create Object", new Runnable() {
            public void run() {
                createObject();
            }
        }));

        statements.addHeaderActions(actions);        
        sink.addLine(new Line(buffer.toString(), actions));
        statements.addLines("        ", sink);
        sink.addLine("    }", this);
    }
    
    void createObject() {
        Object[] arguments;
        if (parameters.size() > 0) {
            arguments = ArgumentsDialog.show(parameters);
        } else {
            arguments = new Object[] {};
        }
        if (arguments != null) {
            JavaObject object = Machine.instance.heap.createObject(javaClass);
            invoke(object, arguments, new ReturnContinuation() {
                public void run(Object result) {
                }
            });
			Explorer.instance.executionLocationChanged();
		}
    }
    
    public String toString() {
        return "new " + javaClass.name;
    }
}

class JavaMethod extends JavaMethodBase {
    JavaType returnType;
    String name;
    ExpressionContainer returnExpression;
    
    JavaMethod(JavaClass javaClass, boolean isStatic, JavaType returnType, String name, List parameters) {
        super(javaClass, isStatic, parameters);
        this.returnType = returnType;
        this.name = name;
        if (returnType != null)
            returnExpression = new ExpressionContainer(statements, returnType, new Literal(returnType.getDefaultValue()));
    }
    
    Object getResult() {
        return returnExpression != null ? returnExpression.get().evaluate() : null;
    }
    
    void invoke(JavaObject target) {
        Object[] arguments;
        if (parameters.size() > 0) {
            arguments = ArgumentsDialog.show(parameters);
        } else {
            arguments = new Object[] {};
        }
        if (arguments != null) {
            invoke(target, arguments, new ReturnContinuation() {
                public void run(Object result) {
                    if (returnType != null) {
                        Explorer.instance.repaint();
                        JOptionPane.showMessageDialog(Explorer.instance, "Result: " + String.valueOf(result), "Invoke Method", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
			Explorer.instance.executionLocationChanged();
        }
    }
    
    
    void addLines(LineSink sink) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    ");
        if (isStatic())
            buffer.append("static ");
        String returnTypeText = returnType == null ? "void" : returnType.getTypeText();
        buffer.append(returnTypeText);
        buffer.append(' ');
        buffer.append(name);
        addParameterListText(buffer);
        buffer.append("{");
        
        List actions = new ArrayList();
        if (isStatic()) {
            actions.add(new Action("Invoke", new Runnable() {
                public void run() {
                    invoke(null);
                }
            }));
        }
        
        statements.addHeaderActions(actions);
        sink.addLine(new Line(buffer.toString(), actions));
        statements.addLines("        ", sink);
        if (returnType != null) {
            LineBuilder builder = new LineBuilder();
            builder.append("        return ");
            returnExpression.addText(builder);
            builder.append(";");
            sink.addLine(builder.toLine(), this);
            sink.addLine("    }");
        } else {
            sink.addLine("    }", this);
        }
    }
    
    public String toString() {
        return name + " in " + javaClass.name;
    }
}