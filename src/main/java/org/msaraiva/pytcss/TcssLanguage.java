package org.msaraiva.pytcss;

import com.intellij.lang.Language;

public class TcssLanguage extends Language {
    public static final TcssLanguage INSTANCE = new TcssLanguage();

    private TcssLanguage() {
        super("TCSS");
    }
}
