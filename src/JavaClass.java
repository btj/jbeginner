// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;
import javax.swing.*;

class LiteralReferenceEditor implements LiteralEditor {
    JLabel label = new JLabel("null");
    
    public JComponent getJComponent() {
        return label;
    }
    
    public void tryGetValue(ErrorContinuation cont) {
        cont.success(null);
    }
}

class ReferenceEditor implements LiteralEditor {
    JavaClass javaClass;
    JComboBox box;
    
    ReferenceEditor(JavaClass javaClass, Object value) {
        Vector data = new Vector();
        data.add("null");
        Iterator iter = Machine.instance.heap.objects.values().iterator();
        while (iter.hasNext()) {
            JavaObject object = (JavaObject) iter.next();
            if (object.javaClass == javaClass)
                data.add(object);
        }
        box = new JComboBox(data);
        box.setSelectedItem(value == null ? "null" : value);
    }
    
    public JComponent getJComponent() { return box; }
    
    public void tryGetValue(ErrorContinuation cont) {
        Object value = box.getSelectedItem();
        cont.success(value == "null" ? null : value);
    }
}

class JavaClass extends JavaType {
    String name;
    Map fields = new TreeMap();
    Map constructors = new TreeMap();
    Map methods = new TreeMap();
    
    int screenWidth = 150;
    int screenHeight;
    
    JavaClass(String name) {
        this.name = name;
    }
    
    void addLines(LineSink sink) {
        List actions = new ArrayList();
        actions.add(new Action("Add Field...", new Runnable() {
            public void run() {
                AddFieldDialog.showAddFieldDialog(JavaClass.this);
            }
        }));
        if (constructors.size() == 0) {
            actions.add(new Action("Add Constructor...", new Runnable() {
                public void run() {
                    AddConstructorDialog.showAddConstructorDialog(JavaClass.this);
                }
            }));
        }
        actions.add(new Action("Add Method...", new Runnable() {
            public void run() {
                AddMethodDialog.showAddMethodDialog(JavaClass.this);
            }
        }));
//        if (constructors.size() == 0) {
//            actions.add(new Action("Create Object", new Runnable() {
//                public void run() {
//                    Machine.instance.heap.createObject(JavaClass.this);
//                }
//            }));
//        }
        sink.addLine(new Line("class " + name + " {", actions));
        boolean first = true;
        {
            Iterator iter = fields.entrySet().iterator();
            while (iter.hasNext()) {
                first = false;
                Map.Entry entry = (Map.Entry) iter.next();
                JavaField field = (JavaField) entry.getValue();
                field.addLine(sink);
            }
        }
        {
            Iterator iter = constructors.values().iterator();
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    sink.addLine("");
                }
        
                JavaConstructor constructor = (JavaConstructor) iter.next();
                constructor.addLines(sink);
            }
        }
        {
            Iterator iter = methods.values().iterator();
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    sink.addLine("");
                }
        
                JavaMethod method = (JavaMethod) iter.next();
                method.addLines(sink);
            }
        }
        
        sink.addLine("}");
    }
    
    String getTypeText() {
        return name;
    }
    
    Object getDefaultValue() {
        return null;
    }
    
	static final Object[] literalValues = {null};
	Object[] getAllLiteralValues() { return literalValues; }
	Object[] getSomeLiteralValues() { return literalValues; }
    LiteralEditor getLiteralEditor(Object value) {
        return new LiteralReferenceEditor();
    }
    
    LiteralEditor getValueEditor(Object value) {
        return new ReferenceEditor(this, value);
    }
}