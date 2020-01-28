// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import javax.swing.*;

interface ErrorContinuation {
    void success(Object value);
    void error(String message);
}

class SimpleErrorContinuation implements ErrorContinuation {
    Object value;
    String message;
    
    public void success(Object value) {
        this.value = value;
    }
    
    public void error(String message) {
        this.message = message;
    }
}

interface LiteralEditor {
    JComponent getJComponent();
    void tryGetValue(ErrorContinuation cont);
}

class BooleanEditor implements LiteralEditor {
    JComboBox box = new JComboBox(new Object[] {Boolean.FALSE, Boolean.TRUE});
    
    BooleanEditor(boolean value) {
        box.setSelectedIndex(value ? 1 : 0);
    }
    
    public JComponent getJComponent() {
        return box;
    }
    
    public void tryGetValue(ErrorContinuation cont) {
        cont.success(box.getSelectedItem());
    }
}

class IntEditor implements LiteralEditor {
    JTextField field = new JTextField(11);
    
    IntEditor(int value) {
        field.setText(String.valueOf(value));
        field.selectAll();
    }
    
    public JComponent getJComponent() {
        return field;
    }
    
    public void tryGetValue(ErrorContinuation cont) {
        try {
            int value = Integer.parseInt(field.getText());
            cont.success(new Integer(value));
        } catch (NumberFormatException e) {
            cont.error("Not a number.");
        }
    }
}

abstract class JavaType {
    abstract String getTypeText();
    abstract Object getDefaultValue();
    abstract LiteralEditor getLiteralEditor(Object value);
	abstract Object[] getAllLiteralValues();
	abstract Object[] getSomeLiteralValues();
    LiteralEditor getValueEditor(Object value) {
        return getLiteralEditor(value);
    }
    
    static final JavaType booleanType = new JavaType() {
        String getTypeText() {
            return "boolean";
        }
        Object getDefaultValue() {
            return Boolean.FALSE;
        }
        LiteralEditor getLiteralEditor(Object value) {
            return new BooleanEditor(((Boolean) value).booleanValue());
        }
		final Object[] literalValues = {Boolean.FALSE, Boolean.TRUE};
		Object[] getAllLiteralValues() { return literalValues; }
		Object[] getSomeLiteralValues() { return literalValues; }
    };

    static final JavaType intType = new JavaType() {
        String getTypeText() {
            return "int";
        }
        Object getDefaultValue() {
            return new Integer(0);
        }
        void tryParse(String text, ErrorContinuation cont) {
            try {
                int i = Integer.parseInt(text);
                cont.success(new Integer(i));
            } catch (NumberFormatException e) {
                cont.error("Not a number.");
            }
        }
		final Object[] someLiteralValues = {new Integer(1), new Integer(2)};
		Object[] getAllLiteralValues() { return null; }
		Object[] getSomeLiteralValues() { return someLiteralValues; }
        LiteralEditor getLiteralEditor(Object value) {
            return new IntEditor(((Integer) value).intValue());
        }
    };
    
    static final JavaType[] primitiveTypes = {intType, booleanType};
}