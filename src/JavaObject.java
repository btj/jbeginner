// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

interface Variable {
    JavaType getType();
    Object getValue();
}

class HeapVariable implements Variable {
    JavaField field;
    Object value;
    
    HeapVariable(JavaField field) {
        this.field = field;
        value = field.type.getDefaultValue();
    }
    
    public JavaType getType() {
        return field.type;
    }
    
    public Object getValue() {
        return value;
    }
}

class JavaObject {
    String name;
    JavaClass javaClass;
    Map fieldValues = new TreeMap();
    
    boolean positioned;
    int x;
    int y;
    
    JavaObject(String name, JavaClass javaClass) {
        this.name = name;
        this.javaClass = javaClass;
        Iterator iter = javaClass.fields.values().iterator();
        while (iter.hasNext()) {
            JavaField field = (JavaField) iter.next();
            fieldValues.put(field.name, new HeapVariable(field));
        }
    }
    
    Object getFieldValue(JavaField field) {
        return ((HeapVariable) fieldValues.get(field.name)).value;
    }
    
    void setFieldValue(JavaField field, Object value) {
        ((HeapVariable) fieldValues.get(field.name)).value = value;
    }
    
    public String toString() {
        return name;
    }
}