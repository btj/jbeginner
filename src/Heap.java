// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class Heap {
    Map counts = new HashMap();
    Map objects = new TreeMap();
    
    void clear() {
        counts = new HashMap();
        objects = new TreeMap();
        Explorer.instance.dataView.dataChanged();
    }
    
    int nextCount(JavaClass javaClass) {
        Integer countBox = (Integer) counts.get(javaClass);
        int count = countBox == null ? 0 : countBox.intValue();
        count++;
        counts.put(javaClass, new Integer(count));
        return count;
    }
    
    JavaObject createObject(JavaClass javaClass) {
        String className = javaClass.name;
        String prefix = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        String name = prefix + "#" + nextCount(javaClass);
        JavaObject object = new JavaObject(name, javaClass);
        objects.put(name, object);
        Explorer.instance.dataView.dataChanged();
        return object;
    }
}