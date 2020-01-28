// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

interface ReturnContinuation {
    void run(Object result);
}

class JavaStackFrame {
    JavaMethodBase javaMethodBase;
    Stack scopeStack = new Stack();
    Statement nextStatement;
    Runnable continuation;
    ReturnContinuation returnContinuation;

    void pushNewScope() {
        scopeStack.push(new Scope());
        Explorer.instance.dataView.dataChanged();
    }
    
    void popScope() {
        scopeStack.pop();
        Explorer.instance.dataView.dataChanged();
    }

    int getVariableCount() {
        int count = 0;
        for (int i = 0; i < scopeStack.size(); i++) {
            count += ((Scope) scopeStack.get(i)).varList.size();
        }
        return count;
    }
    
    Object getValue(VarDecl var) {
        int i = scopeStack.size() - 1;
        while (0 <= i) {
            Scope scope = (Scope) scopeStack.get(i);
            if (scope.containsVariable(var)) {
                return scope.getValue(var);
            }
            i--;
        }
        throw new RuntimeException("Local variable lookup failed.");
    }
    
    void setValue(VarDecl var, Object value) {
        int i = scopeStack.size() - 1;
        while (0 <= i) {
            Scope scope = (Scope) scopeStack.get(i);
            if (scope.containsVariable(var)) {
                scope.setValue(var, value);
                return;
            }
            i--;
        }
        throw new RuntimeException("Local variable lookup failed.");
    }
    
    void addVariable(VarDecl var, Object value) {
        ((Scope) scopeStack.peek()).add(var, value);
    }
}