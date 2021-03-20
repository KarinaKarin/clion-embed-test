package com.intellij.intern.y21.clion.stubs;

import java.util.function.Supplier;

public class EmbeddedBundle {
    public static Supplier<String> messagePointer(String s) {
        return ()->s;
    }

    public static String message(String s) {
        return s;
    }
}
