package info.bliki.extensions.scribunto.engine.lua.interfaces;

import info.bliki.extensions.scribunto.engine.lua.CompiledScriptCache;
import info.bliki.extensions.scribunto.engine.lua.ScribuntoLuaEngine;
import info.bliki.wiki.model.IWikiModel;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

public class MwTitleTestEngine extends ScribuntoLuaEngine {

  public MwTitleTestEngine(IWikiModel model,
      CompiledScriptCache cache) {
    super(model, cache, true);
    final LuaValue title = getGlobals().get("mw").get("title");
    assert ! title.isnil();
    title.set("testPageId", LuaString.valueOf("test"));
    model.setPageName("Main Page");
  }
}
