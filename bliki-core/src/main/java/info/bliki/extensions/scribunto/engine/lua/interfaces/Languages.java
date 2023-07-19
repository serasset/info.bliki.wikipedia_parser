package info.bliki.extensions.scribunto.engine.lua.interfaces;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class Languages {
    private Map<String,String> codes = new HashMap<>();
    {
        codes.put("en", "English");
        codes.put("ru", "русский");
    }

    /**
     * @param code string: The code of the language for which to get the name
     * @param inLanguage null|string: Code of language in which to return the name (null for autonyms)
     * @return string: Language name or empty
     */
    public String getName(String code, String inLanguage) {
        Locale l = Locale.forLanguageTag(code);
        String result;
        if (inLanguage == null) {
            // Autonym case
            result = l.getDisplayLanguage(l);
        } else {
            result = l.getDisplayLanguage(new Locale(inLanguage));
        }
        if (code.equals(result)) {
            result = l.getDisplayLanguage(Locale.ENGLISH);
        }
        return result;
    }


}
