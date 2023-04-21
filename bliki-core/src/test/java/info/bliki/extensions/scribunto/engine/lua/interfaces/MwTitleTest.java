package info.bliki.extensions.scribunto.engine.lua.interfaces;

import info.bliki.extensions.scribunto.engine.lua.CompiledScriptCache;
import info.bliki.extensions.scribunto.engine.lua.LuaTestBase;
import info.bliki.extensions.scribunto.engine.lua.ScribuntoLuaEngine;
import info.bliki.wiki.filter.ParsedPageName;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.model.WikiModelContentException;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MwTitleTest extends LuaTestBase {
    @Override
    public String getLuaTest() {
        return "TitleLibraryTests.lua";
    }

    @Override
    protected ScribuntoLuaEngine getScribuntoLuaEngine() {
        return new MwTitleTestEngine(getWikiModel(), CompiledScriptCache.DONT_CACHE);
    }


    @Override
    protected WikiModel getWikiModel() {
        Configuration conf = new Configuration();
        conf.addInterwikiLink("interwikiprefix", "//test.wikipedia.org/wiki/$1");

        return new WikiModel(conf,"${image}", "${title}"){
            @Override
            public String getRawWikiContent(ParsedPageName parsedPagename, Map<String, String> templateParameters)
                    throws WikiModelContentException {
                if (parsedPagename.pagename.equals("ScribuntoTestPage"))
                    return "{{int:mainpage}}<includeonly>...</includeonly><noinclude>...</noinclude>";
                return super.getRawWikiContent(parsedPagename, templateParameters);
            }
        };
    }

    List<String> ignored = Arrays.asList(
            "title.new space normalization",
            "title.new with invalid title",
            "title.new with existing pageid",
            ".protectionLevels",
            ".cascadingProtection",
            ".partialUrl",
            ".fullUrl",
            ".localUrl",
            ".canonicalUrl",
            ".redirectTarget",
            "too many expensive functions"
    );
    @Override
    public boolean isIgnored(Description testDescription) {
        return ignored.stream().anyMatch(n -> testDescription.getDisplayName().startsWith(n));
    }
}
