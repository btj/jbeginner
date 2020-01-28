// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class JavaProgram {
    Map classes = new TreeMap();
    
    void clear() {
        classes = new TreeMap();
    }
    
    void addLines(LineSink sink) {
        Iterator iter = classes.entrySet().iterator();
        boolean first = true;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (first) {
                first = false;
            } else {
                sink.addLine("");
            }
            JavaClass javaClass = (JavaClass) entry.getValue();
            javaClass.addLines(sink);
        }
    }
}