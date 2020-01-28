// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class StackVariable implements Variable {
    VarDecl decl;
    Object value;
    
    StackVariable(VarDecl decl, Object value) {
        this.decl = decl;
        this.value = value;
    }
    
    public JavaType getType() {
        return decl.type;
    }
    
    public Object getValue() {
        return value;
    }
}

class Scope {
    final List varList = new ArrayList();
    private Map varMap = new HashMap();
    
    Variable add(VarDecl decl, Object value) {
        StackVariable v = new StackVariable(decl, value);
        varList.add(v);
        varMap.put(decl, v);
        return v;
    }
    
    boolean containsVariable(VarDecl decl) {
        return varMap.containsKey(decl);
    }
    
    Object getValue(VarDecl decl) {
        return ((StackVariable) varMap.get(decl)).value;
    }

    void setValue(VarDecl decl, Object value) {
        ((StackVariable) varMap.get(decl)).value = value;
    }
}