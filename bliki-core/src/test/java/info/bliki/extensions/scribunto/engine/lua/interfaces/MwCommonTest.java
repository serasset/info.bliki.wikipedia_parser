package info.bliki.extensions.scribunto.engine.lua.interfaces;

import info.bliki.extensions.scribunto.engine.lua.LuaTestBase;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MwCommonTest extends LuaTestBase {
    @Override
    public String getLuaTest() {
        return "CommonTests.lua";
    }

    @Override public boolean isIgnored(Description testDescription) {
        final String methodName = testDescription.getMethodName();

        return super.isIgnored(testDescription) ||
                methodName.contains("setfenv") ||
                methodName.contains("getfenv");
    }

    @Override
    public Set<String> ignoredTests() {
        return new HashSet<>(Arrays.asList(
            "string is not string metatable",
            "clone table then modify",
            "mw.loadJsonData, getter (true)",
            "mw.loadJsonData, getter (false)",
            "mw.loadJsonData, getter (num)",
            "mw.loadJsonData, getter (str)",
            "mw.loadJsonData, getter (table.2)",
            "mw.loadJsonData, pairs",
            "mw.loadJsonData, ipairs",
            "mw.loadJsonData, setmetatable",
            "mw.loadJsonData, setter (1)",
            "mw.loadJsonData, setter (2)",
            "mw.loadJsonData, setter (3)",
            "mw.loadJsonData, rawset",
            "mw.loadJsonData, bad title (1)",
            "mw.loadJsonData, bad title (2)",
            "mw.loadJsonData, bad title (3)",
            "mw.loadJsonData, bad title (4)",
            "mw.addWarning, bad type"
        ));
    }



}
