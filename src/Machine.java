// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.util.*;

class Machine {
    static final Machine instance = new Machine();
    
    JavaProgram program = new JavaProgram();
    Heap heap = new Heap();
    Stack stack = new Stack();
	int selectedStackFrameIndex;
    
    JavaStackFrame top() {
        return (JavaStackFrame) stack.peek();
    }

	JavaStackFrame getSelectedStackFrame() {
		return (JavaStackFrame) stack.get(selectedStackFrameIndex);
	}

	boolean isSelectedStackFrameActive() {
		return selectedStackFrameIndex == stack.size() - 1;
	}

	void selectTopStackFrame() {
		selectedStackFrameIndex = stack.size() - 1;
	}
}