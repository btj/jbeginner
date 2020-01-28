// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

interface BinaryOperator {
    JavaType getOperandType();
    JavaType getResultType();
    String getOperatorToken();
    Object evaluate(Object op1, Object op2);
    int getPriority();
    int getLeftMinPriority();
    int getRightMinPriority();
}

class BinaryOperators {
    static final BinaryOperator addition = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.intType; }
        public String getOperatorToken() { return "+"; }
        public Object evaluate(Object v1, Object v2) {
            return new Integer(((Integer) v1).intValue() + ((Integer) v2).intValue());
        }
        public int getPriority() { return Priority.additive; }
        public int getLeftMinPriority() { return Priority.additive; }
        public int getRightMinPriority() { return Priority.additive + 1; }
    };
    
    static final BinaryOperator subtraction = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.intType; }
        public String getOperatorToken() { return "-"; }
        public Object evaluate(Object v1, Object v2) {
            return new Integer(((Integer) v1).intValue() - ((Integer) v2).intValue());
        }
        public int getPriority() { return Priority.additive; }
        public int getLeftMinPriority() { return Priority.additive; }
        public int getRightMinPriority() { return Priority.additive + 1; }
    };
    
    static final BinaryOperator multiplication = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.intType; }
        public String getOperatorToken() { return "*"; }
        public Object evaluate(Object v1, Object v2) {
            return new Integer(((Integer) v1).intValue() * ((Integer) v2).intValue());
        }
        public int getPriority() { return Priority.multiplicative; }
        public int getLeftMinPriority() { return Priority.multiplicative; }
        public int getRightMinPriority() { return Priority.multiplicative + 1; }
    };
    
    static final BinaryOperator division = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.intType; }
        public String getOperatorToken() { return "/"; }
        public Object evaluate(Object v1, Object v2) {
            int D = ((Integer) v1).intValue();
            int d = ((Integer) v2).intValue();
            if (d == 0)
                throw new JavaExecutionException("ArithmeticException: Division by zero");
            return new Integer(D / d);
        }
        public int getPriority() { return Priority.multiplicative; }
        public int getLeftMinPriority() { return Priority.multiplicative; }
        public int getRightMinPriority() { return Priority.multiplicative + 1; }
    };
    
    static final BinaryOperator lessThan = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.booleanType; }
        public String getOperatorToken() { return "<"; }
        public Object evaluate(Object v1, Object v2) {
            return ((Integer) v1).intValue() < ((Integer) v2).intValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        public int getPriority() { return Priority.relational; }
        public int getLeftMinPriority() { return Priority.relational + 1; }
        public int getRightMinPriority() { return Priority.relational + 1; }
    };
    
    static final BinaryOperator lessOrEqual = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.intType; }
        public JavaType getResultType() { return JavaType.booleanType; }
        public String getOperatorToken() { return "<="; }
        public Object evaluate(Object v1, Object v2) {
            return ((Integer) v1).intValue() <= ((Integer) v2).intValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        public int getPriority() { return Priority.relational; }
        public int getLeftMinPriority() { return Priority.relational + 1; }
        public int getRightMinPriority() { return Priority.relational + 1; }
    };

    static final BinaryOperator booleanAnd = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.booleanType; }
        public JavaType getResultType() { return JavaType.booleanType; }
        public String getOperatorToken() { return "&&"; }
        public Object evaluate(Object v1, Object v2) {
            return ((Boolean) v1).booleanValue() && ((Boolean) v2).booleanValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        public int getPriority() { return Priority.and; }
        public int getLeftMinPriority() { return Priority.and + 1; }
        public int getRightMinPriority() { return Priority.and + 1; }
    };

    static final BinaryOperator booleanOr = new BinaryOperator() {
        public JavaType getOperandType() { return JavaType.booleanType; }
        public JavaType getResultType() { return JavaType.booleanType; }
        public String getOperatorToken() { return "||"; }
        public Object evaluate(Object v1, Object v2) {
            return ((Boolean) v1).booleanValue() || ((Boolean) v2).booleanValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        public int getPriority() { return Priority.or; }
        public int getLeftMinPriority() { return Priority.or + 1; }
        public int getRightMinPriority() { return Priority.or + 1; }
    };
    
    static final BinaryOperator[] binaryOperators = {
        addition, subtraction, multiplication, division, lessThan, lessOrEqual,
        booleanAnd, booleanOr
    };
}
