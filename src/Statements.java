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

interface EnvSource {
    Map getEnv();
}

interface StaticScope {
    boolean declares(String name);
}

abstract class Statement implements EnvSource, StaticScope {
    final EnvSource envSource;
    final StaticScope staticScope;

    Statement(EnvSource envSource, StaticScope staticScope) {
        this.envSource = envSource;
        this.staticScope = staticScope;
    }
    
    abstract void addLines(String indent, LineSink sink);
    abstract void execute();
    abstract VarDecl getDecl();
    
    public Map getEnv() {
        Map env = envSource.getEnv();
        VarDecl decl = getDecl();
        if (decl != null)
            env.put(decl.name, decl);
        return env;
    }
    
    public boolean declares(String name) {
        VarDecl decl = getDecl();
        return decl == null ? false : decl.name.equals(name);
    }
}

interface StatementContinuation {
    public void run(Statement statement);
}

interface StatementSink {
    public void add(String label, Statement statement);
}

interface StatementFactory {
    String getName();
    void addStatements(Map environment, EnvSource envSource, StaticScope staticScope, StatementSink sink);
}

class StatementFactories {
    static StatementFactory fieldAssign = new StatementFactory() {
        public String getName() { return "Field Assignment"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            Iterator targetIter = env.values().iterator();
            while (targetIter.hasNext()) {
                VarDecl target = (VarDecl) targetIter.next();
                if (target.type instanceof JavaClass) {
                    JavaClass javaClass = (JavaClass) target.type;
                    
                    Iterator fieldIter = javaClass.fields.values().iterator();
                    while (fieldIter.hasNext()) {
                        JavaField field = (JavaField) fieldIter.next();
                        Expression rhs = new Literal(field.type.getDefaultValue());
                        Statement stmt = new FieldAssignStatement(envSource, staticScope, target, field, rhs);
                        String label = target.name + "." + field.name + " = " + rhs.toString() + ";";
                        sink.add(label, stmt);
                    }
                }
            }
        }
    };
    
    static StatementFactory localDeclaration = new StatementFactory() {
        public String getName() { return "Local Variable Declaration"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            String lhsName = Util.getNewKey(env, "local");
            Vector typeWrappers = Util.getTypeWrappers();
            for (int i = 0; i < typeWrappers.size(); i++) {
                JavaType type = ((TypeWrapper) typeWrappers.get(i)).type;
                VarDecl decl = new VarDecl(type, lhsName);
                Literal rhs = new Literal(type.getDefaultValue());
                Statement stmt = new AssignmentStatement(envSource, staticScope, true, decl, rhs);
                String label = decl.getDeclarationText() + " = " + rhs + ";";
                sink.add(label, stmt);
            }
        }
    };

    static StatementFactory assignment = new StatementFactory() {
        public String getName() { return "Local Variable Assignment"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            Iterator iter = env.values().iterator();
            while (iter.hasNext()) {
                VarDecl decl = (VarDecl) iter.next();
                if (!decl.name.equals("this")) {
                    Literal rhs = new Literal(decl.type.getDefaultValue());
                    Statement stmt = new AssignmentStatement(envSource, staticScope, false, decl, rhs);
                    String label = decl.name + " = " + rhs + ";";
                    sink.add(label, stmt);
                }
            }
        }
    };
    
    static StatementFactory invocation = new StatementFactory() {
        public String getName() { return "Invocation"; }
        void addStatement(EnvSource envSource, StaticScope staticScope, String lhsName, JavaMethod method, VarDecl target, StatementSink sink) {
            VarDecl lhs;
            if (method.returnType == null)
                lhs = null;
            else {
                lhs = new VarDecl(method.returnType, lhsName);
            }
            List argExprs = new ArrayList();
            for (int i = 0; i < method.parameters.size(); i++) {
                VarDecl param = (VarDecl) method.parameters.get(i);
                argExprs.add(new Literal(param.type.getDefaultValue()));
            }
            Statement stmt = new InvocationStatement(envSource, staticScope, lhs, target, method, argExprs);
            String lhsText = lhs == null ? "" : lhs.getDeclarationText() + " = ";
            String targetText = target == null ? method.javaClass.name : target.name;
            String label = lhsText + targetText + "." + method.name + "(...);";
            sink.add(label, stmt);
        }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            String lhsName = Util.getNewKey(env, "local");
            
            // Instance method invocations
            Iterator targetIter = env.values().iterator();
            while (targetIter.hasNext()) {
                VarDecl target = (VarDecl) targetIter.next();
                if (target.type instanceof JavaClass) {
                    JavaClass javaClass = (JavaClass) target.type;
                    Iterator methodIter = javaClass.methods.values().iterator();
                    while (methodIter.hasNext()) {
                        JavaMethod method = (JavaMethod) methodIter.next();
                        
                        if (!method.isStatic()) {
                            addStatement(envSource, staticScope, lhsName, method, target, sink);
                        }
                    }
                }
            }
            
            // Static method invocations
            Iterator classIter = Machine.instance.program.classes.values().iterator();
            while (classIter.hasNext()) {
                JavaClass javaClass = (JavaClass) classIter.next();
                Iterator methodIter = javaClass.methods.values().iterator();
                while (methodIter.hasNext()) {
                    JavaMethod method = (JavaMethod) methodIter.next();
                    if (method.isStatic()) {
                        addStatement(envSource, staticScope, lhsName, method, null, sink);
                    }
                }
            }
        }
    };
    
    static StatementFactory creation = new StatementFactory() {
        public String getName() { return "Creation"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            String lhsName = Util.getNewKey(env, "local");
            Iterator iter = Machine.instance.program.classes.values().iterator();
            while (iter.hasNext()) {
                JavaClass javaClass = (JavaClass) iter.next();
                Iterator ctorIter = javaClass.constructors.values().iterator();
                while (ctorIter.hasNext()) {
                    JavaConstructor constructor = (JavaConstructor) ctorIter.next();
                    VarDecl lhs = new VarDecl(javaClass, lhsName);
                    List argExprs = new ArrayList();
                    for (int i = 0; i < constructor.parameters.size(); i++) {
                        VarDecl param = (VarDecl) constructor.parameters.get(i);
                        argExprs.add(new Literal(param.type.getDefaultValue()));
                    }
                    Statement stmt = new CreationStatement(envSource, staticScope, lhs, javaClass, constructor, argExprs);
                    String label = lhs.getDeclarationText() + " = new " + javaClass.name + "(...);";
                    sink.add(label, stmt);
                }
            }
        }
    };
    
    static StatementFactory ifStatement = new StatementFactory() {
        public String getName() { return "If"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            Statement stmt = new IfStatement(envSource, staticScope, new Literal(Boolean.FALSE));
            sink.add("if (false) {} else {}", stmt);
        }
    };
    
    static StatementFactory whileStatement = new StatementFactory() {
        public String getName() { return "While"; }
        public void addStatements(Map env, EnvSource envSource, StaticScope staticScope, StatementSink sink) {
            Statement stmt = new WhileStatement(envSource, staticScope, new Literal(Boolean.FALSE));
            sink.add("while (false) {}", stmt);
        }
    };
    
    static StatementFactory[] factories = {
        localDeclaration, assignment, fieldAssign,
        invocation, creation, ifStatement, whileStatement
    };
}

class StatementList implements EnvSource, StaticScope {
    final List statements = new ArrayList();
    final EnvSource envSource;
    final boolean ownScope;
    
    StatementList(EnvSource envSource, boolean ownScope) {
        this.envSource = envSource;
        this.ownScope = ownScope;
    }
    
    void addLines(String indent, LineSink sink) {
        Iterator iter = statements.iterator();
        while (iter.hasNext()) {
            Statement statement = (Statement) iter.next();
            statement.addLines(indent, sink);
        }
    }
    
    public Map getEnv() {
        if (statements.size() == 0)
            return envSource.getEnv();
        else
            return ((EnvSource) statements.get(statements.size() - 1)).getEnv();
    }
    
    public boolean declares(String name) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = (Statement) statements.get(i);
            if (statement.declares(name))
                return true;
        }
        return false;
    }

    void execute(final Runnable continuation) {
        final JavaStackFrame frame = Machine.instance.top();
        if (ownScope)
            frame.pushNewScope();
        new Runnable() {
            int index;
            
            public void run() {
                if (index == statements.size()) {
                    if (ownScope)
                        frame.popScope();
                    continuation.run();
                } else {
                    frame.nextStatement = (Statement) statements.get(index);
                    index++;
                    frame.continuation = this;
                }
            }
        }.run();
    }
    
    void addHeaderActions(List actions) {
        actions.add(StatementSwingUtil.createAddStatementAction(envSource, this, statements));
        if (statements.size() > 0)
            actions.add(StatementSwingUtil.createRemoveStatementAction(statements));
    }
}

class FieldAssignStatement extends Statement {
    VarDecl target;
    JavaField field;
    ExpressionContainer rhs;
    
    FieldAssignStatement(EnvSource envSource, StaticScope staticScope, VarDecl target, JavaField field, Expression rhs) {
        super(envSource, staticScope);
        this.target = target;
        this.field = field;
        this.rhs = new ExpressionContainer(envSource, field.type, rhs);
    }
    
    void addLines(String indent, LineSink sink) {
        LineBuilder builder = new LineBuilder();
        builder.append(indent + target + "." + field.name + " = ");
        rhs.addText(builder);
        builder.append(";");
        sink.addLine(builder.toLine(), this);
    }
    
    void execute() {
        JavaStackFrame frame = Machine.instance.top();
        JavaObject targetObject = (JavaObject) frame.getValue(target);
        Expression.checkNotNull(targetObject);
        targetObject.setFieldValue(field, rhs.get().evaluate());
        frame.continuation.run();
    }
    
    VarDecl getDecl() { return null; }
}

// In time, we will want to have declarations without initializers.
// It is not good to force people to initialize variables prematurely.

class AssignmentStatement extends Statement {
    boolean isDeclaration;
    VarDecl lhs;
    ExpressionContainer rhs;
    
    AssignmentStatement(EnvSource envSource, StaticScope staticScope, boolean isDeclaration, VarDecl lhs, Expression rhs) {
        super(envSource, staticScope);
        this.isDeclaration = isDeclaration;
        this.lhs = lhs;
        this.rhs = new ExpressionContainer(envSource, lhs.type, rhs);
    }
    
    void addLines(String indent, LineSink sink) {
        LineBuilder builder = new LineBuilder();
        builder.append(indent);
        if (isDeclaration)
            lhs.addDeclarationText(builder, envSource, staticScope);
        else
            builder.append(lhs.toString());
        builder.append(" = ");
        rhs.addText(builder);
        builder.append(";");
        sink.addLine(builder.toLine(), this);
    }
    
    void execute() {
        JavaStackFrame frame = Machine.instance.top();
        Object value = rhs.get().evaluate();
        if (isDeclaration)
            frame.addVariable(lhs, value);
        else
            frame.setValue(lhs, value);
        frame.continuation.run();
    }
    
    VarDecl getDecl() { return isDeclaration ? lhs : null; }
}

class InvocationStatement extends Statement {
    VarDecl lhs;
    VarDecl target;
    JavaMethod method;
    List argumentExpressionContainers;

    InvocationStatement(EnvSource envSource, StaticScope staticScope, VarDecl lhs, VarDecl target, JavaMethod method, List argumentExpressions) {
        super(envSource, staticScope);
        this.lhs = lhs;
        this.target = target;
        this.method = method;
        this.argumentExpressionContainers = ExpressionContainer.createArgumentExpressionContainers(envSource, method.parameters, argumentExpressions);
    }
    
    void execute() {
        final JavaStackFrame frame = Machine.instance.top();
        Object object;
        if (target == null)
            object = null;
        else 
        {
            object = frame.getValue(target);
            Expression.checkNotNull(object);
        }
        Object[] arguments = new Object[argumentExpressionContainers.size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = ((ExpressionContainer) argumentExpressionContainers.get(i)).get().evaluate();
        }
        method.invoke(object, arguments, new ReturnContinuation() {
            public void run(Object result) {
                if (lhs != null)
                    frame.addVariable(lhs, result);
                frame.continuation.run();
            }
        });
    }

    void addLines(String indent, LineSink sink) {
        LineBuilder builder = new LineBuilder();
        builder.append(indent);
        if (lhs != null) {
            lhs.addDeclarationText(builder, envSource, staticScope);
            builder.append(" = ");
        }
        builder.append(target == null ? method.javaClass.name : target.toString());
        builder.append(".");
        builder.append(method.name);
        builder.append("(");
        for (int i = 0; i < argumentExpressionContainers.size(); i++) {
            ExpressionContainer c = (ExpressionContainer) argumentExpressionContainers.get(i);
            if (i > 0)
                builder.append(", ");
            c.addText(builder);
        }
        builder.append(");");
        sink.addLine(builder.toLine(), this);
    }
    
    VarDecl getDecl() { return lhs; }
}

class CreationStatement extends Statement {
    VarDecl lhs;
    JavaClass javaClass;
    JavaConstructor constructor;
    List argumentExpressionContainers;
    
    CreationStatement(EnvSource envSource, StaticScope staticScope, VarDecl lhs, JavaClass javaClass, JavaConstructor constructor, List argumentExpressions) {
        super(envSource, staticScope);
        this.lhs = lhs;
        this.javaClass = javaClass;
        this.constructor = constructor;
        this.argumentExpressionContainers = ExpressionContainer.createArgumentExpressionContainers(envSource, constructor.parameters, argumentExpressions);
    }
    
    void execute() {
        final JavaObject object = Machine.instance.heap.createObject(javaClass);
        final JavaStackFrame frame = Machine.instance.top();
        ReturnContinuation cont = new ReturnContinuation() {
            public void run(Object result) {
                if (lhs != null) {
                    frame.addVariable(lhs, object);
                }
                frame.continuation.run();
            }
        };
        if (constructor == null) {
            cont.run(null);
        } else {
            Object[] arguments = new Object[argumentExpressionContainers.size()];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = ((ExpressionContainer) argumentExpressionContainers.get(i)).get().evaluate();
            }
            constructor.invoke(object, arguments, cont);
        }
    }
    
    void addLines(String indent, LineSink sink) {
        LineBuilder builder = new LineBuilder();
        builder.append(indent);
        if (lhs != null) {
            lhs.addDeclarationText(builder, envSource, staticScope);
            builder.append(" = ");
        }
        builder.append("new ");
        builder.append(javaClass.name);
        builder.append("(");
        for (int i = 0; i < argumentExpressionContainers.size(); i++) {
            ExpressionContainer c = (ExpressionContainer) argumentExpressionContainers.get(i);
            if (i > 0)
                builder.append(", ");
            c.addText(builder);
        }
        builder.append(");");
        sink.addLine(builder.toLine(), this);
    }
    
    VarDecl getDecl() { return lhs; }
}

class IfStatement extends Statement {
    ExpressionContainer condition;
    StatementList trueBranch;
    StatementList falseBranch;
    
    IfStatement(EnvSource envSource, StaticScope staticScope, Expression condition) {
        super(envSource, staticScope);
        this.condition = new ExpressionContainer(envSource, JavaType.booleanType, condition);
        this.trueBranch = new StatementList(envSource, true);
        this.falseBranch = new StatementList(envSource, true);
    }
    
    void execute() {
        StatementList branch = ((Boolean) condition.get().evaluate()).booleanValue() ? trueBranch : falseBranch;
        branch.execute(Machine.instance.top().continuation);
    }
    
    void addLines(String indent, LineSink sink) {
        {
            LineBuilder builder = new LineBuilder();
            builder.append(indent + "if (");
            condition.addText(builder);
            builder.append(") {");
            Line line = builder.toLine();
            ArrayList actions = new ArrayList();
            trueBranch.addHeaderActions(actions);
            line.setActions(actions);
            sink.addLine(line, this);
        }
        trueBranch.addLines(indent + "    ", sink);
        if (falseBranch == null) {
            sink.addLine(indent + "}");
        } else {
            {
                ArrayList actions = new ArrayList();
                falseBranch.addHeaderActions(actions);
                sink.addLine(new Line(indent + "} else {", actions));
            }
            falseBranch.addLines(indent + "    ", sink);
            sink.addLine(indent + "}");
        }
    }
    
    VarDecl getDecl() { return null; }
    
    public boolean declares(String name) {
        return trueBranch.declares(name) || falseBranch != null && falseBranch.declares(name);
    }
}

class WhileStatement extends Statement {
    ExpressionContainer condition;
    StatementList body;
    
    WhileStatement(EnvSource envSource, StaticScope staticScope, Expression condition) {
        super(envSource, staticScope);
        this.condition = new ExpressionContainer(envSource, JavaType.booleanType, condition);
        this.body = new StatementList(envSource, true);
    }
    
    void execute() {
        final Runnable cont = Machine.instance.top().continuation;
        new Runnable() {
            public void run() {
                if (((Boolean) condition.get().evaluate()).booleanValue()) {
                    body.execute(this);
                } else {
                    cont.run();
                }
            }
        }.run();
    }
    
    void addLines(String indent, LineSink sink) {
        LineBuilder builder = new LineBuilder();
        builder.append(indent + "while (");
        condition.addText(builder);
        builder.append(") {");
        Line line = builder.toLine();
        ArrayList actions = new ArrayList();
        body.addHeaderActions(actions);
        line.setActions(actions);
        sink.addLine(line, this);
        body.addLines(indent + "    ", sink);
        sink.addLine(indent + "}");
    }
    
    VarDecl getDecl() { return null; }
    
    public boolean declares(String name) {
        return body.declares(name);
    }
}

