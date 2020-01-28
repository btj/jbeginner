// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class TypeWrapper {
    JavaType type;
    
    TypeWrapper(JavaType type) {
        this.type = type;
    }
    
    public String toString() {
        return type.getTypeText();
    }
}

class Util {
    private static HashSet reservedWords = new HashSet(Arrays.asList(new String[] {
        "abstract", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else",
        "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "try",
        "void", "volatile", "while", "assert"}));
    
    static boolean isReservedWord(String text) {
        return reservedWords.contains(text);
    }
    
    static String getNewKey(Map map, String prefix) {
        int i = 1;
        String key;
        while (map.containsKey(key = prefix + i))
            i++;
        return key;
    }
    
    static boolean isJavaIdentifier(String text) {
        if (!Character.isJavaIdentifierStart(text.charAt(0)))
            return false;
        for (int i = 1; i < text.length(); i++)
            if (!Character.isJavaIdentifierPart(text.charAt(i)))
                return false;
        return true;
    }
    
    static Vector getTypeWrappers() {
        Vector types = new Vector();
        for (int i = 0; i < JavaType.primitiveTypes.length; i++)
            types.add(new TypeWrapper(JavaType.primitiveTypes[i]));
        Iterator iter = Machine.instance.program.classes.values().iterator();
        while (iter.hasNext()) {
            JavaClass javaClass = (JavaClass) iter.next();
            types.add(new TypeWrapper(javaClass));
        }
        return types;
    }
}