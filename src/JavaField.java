// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class JavaField {
    JavaType type;
    String name;
    
    JavaField(JavaType type, String name) {
        this.type = type;
        this.name = name;
    }
    
    
    void addLine(LineSink sink) {
        sink.addLine("    " + type.getTypeText() + " " + name + ";");
    }
    
    public String toString() {
        return name;
    }
}