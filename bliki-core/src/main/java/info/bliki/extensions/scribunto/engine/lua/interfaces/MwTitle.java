package info.bliki.extensions.scribunto.engine.lua.interfaces;

import info.bliki.wiki.filter.ParsedPageName;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.WikiModelContentException;
import info.bliki.wiki.namespaces.INamespace;
import info.bliki.wiki.namespaces.INamespace.INamespaceValue;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.HashMap;

import static info.bliki.extensions.scribunto.engine.lua.ScribuntoLuaEngine.toLuaString;
import static info.bliki.wiki.namespaces.INamespace.NamespaceCode.MEDIA_NAMESPACE_KEY;
import static org.luaj.vm2.LuaValue.*;

// TitleLibrary.php
public class MwTitle implements MwInterface {

    private final IWikiModel wikiModel;

    public MwTitle(IWikiModel wikiModel) {
        assert (wikiModel != null);
        this.wikiModel = wikiModel;
    }

    @Override
    public String name() {
        return "mw.title.reusable";
    }

    @Override
    public LuaTable getInterface() {
        LuaTable table = new LuaTable();
        table.set("newTitle", newTitle());
        table.set("makeTitle", makeTitle());
        table.set("getExpensiveData", getExpensiveData());
        table.set("getUrl", getUrl());
        table.set("getContent", getContent());
        table.set("fileExists", fileExists());
        table.set("getFileInfo", getFileInfo());
        table.set("protectionLevels", protectionLevels());
        table.set("cascadingProtection", cascadingProtection());
        table.set("getCurrentTitle", getCurrentTitle());
        table.set("recordVaryFlag", recordVaryFlag());
        return table;
    }

    @Override
    public LuaValue getSetupOptions() {
        LuaTable table = new LuaTable();
        table.set("thisTitle", title(wikiModel.getNamespaceName(), wikiModel.getPageName()));
        table.set("NS_MEDIA", MEDIA_NAMESPACE_KEY.code);
        return table;
    }

    private LuaValue getFileInfo() {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return LuaValue.NIL;
            }
        };
    }

    private LuaValue getExpensiveData() {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return new LuaTable();
            }
        };
    }

    private LuaValue getUrl() {
        return new LibFunction() {
            /**
             *  $text, $which, $query = null, $proto = null
             */
            @Override
            public Varargs invoke(Varargs args) {
                return LuaValue.EMPTYSTRING;
            }
        };
    }

    private LuaValue fileExists() {
        return new OneArgFunction() {
            /**
             * @param page
             * @return Whether the file exists. For File- and Media-namespace titles, this is
             * expensive. It will also be recorded as an image usage for File- and Media-namespace titles.
             */
            @Override
            public LuaValue call(LuaValue page) {
                return NIL;
            }
        };
    }

    private LuaValue protectionLevels() {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue action) {
                return new LuaTable();
            }
        };
    }

    private LuaValue cascadingProtection() {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue action) {
                LuaTable table = new LuaTable();
                table.set("restrictions", new LuaTable());
                return table;
            }
        };
    }

    private LuaValue getContent() {
        return new OneArgFunction() {
            /**
             * @param page the title of the page to fetch
             * @return the (unparsed) content of the page, or nil if there is no page.
             * The page will be recorded as a transclusion.
             */
            @Override
            public LuaValue call(LuaValue page) {
                // TODO: provide a handler to stub out arbitrary content
                if (page.isnil()) return  EMPTYSTRING;
                if (! page.isstring()) return EMPTYSTRING;
                ParsedPageName pagename = ParsedPageName.parsePageName(wikiModel, page.tojstring(),
                        wikiModel.getNamespace().getMain(), false, true);
                String content = null;
                try {
                    content = wikiModel.getRawWikiContent(pagename, new HashMap<String, String>());
                } catch (WikiModelContentException e) {
                    throw new RuntimeException(e);
                }
                // return an empty string to improve compatibility with modules which
                // don't bother to perform nil checks.
                return null == content ? NIL : LuaString.valueOf(content);
            }
        };
    }

    private LuaValue newTitle() {
        return new TwoArgFunction() {
            /**
             * Handler for title.new
             *
             * @param text_or_id       string|int Title or page_id to fetch
             * @param defaultNamespace string|int Namespace name or number to use if $text_or_id doesn't override
             * @return array Lua data
             */
            @Override
            public LuaValue call(LuaValue text_or_id, LuaValue defaultNamespace) {
                // "-" is considered as a number by Lua... so check the type rather than the value
                if (text_or_id.type() == TNUMBER) {
                    // no database lookup
                    return NIL;
                } else if (text_or_id.isstring()) {
                    if (isValidTitle(text_or_id, defaultNamespace)) {
                        Title title = new Title(text_or_id.tojstring(), defaultNamespace);
                        return title.toTitle();
                    } else {
                        return NIL;
                    }
                } else {
                    return NIL;
                }
            }
        };
    }

    private class Title {

        String interwiki = null;
        String namespaceText = null;
        INamespaceValue namespace;
        String pagename = null;
        String fragment = null;
        boolean valid;

        public Title(String fullPagename, LuaValue defaultNamespace) {
            this(fullPagename,
                    defaultNamespace.isnil()
                            ? wikiModel.getNamespace().getMain()
                            : defaultNamespace.isint()
                            ? wikiModel.getNamespace().getNamespaceByNumber(defaultNamespace.toint())
                            : wikiModel.getNamespace().getNamespace(defaultNamespace.tojstring()));
        }

        public Title(String fullPagename, int defaultNamespace) {
            this(fullPagename,
                    wikiModel.getNamespace().getNamespaceByNumber(defaultNamespace));
        }

        public Title(String fullPagename, String defaultNamespace) {
            this(fullPagename,
                    wikiModel.getNamespace().getNamespace(defaultNamespace));
        }

        public Title(String fullPagename, INamespaceValue defaultNamespace) {
            if (fullPagename.length() > 0 && fullPagename.charAt(0) == ':') {
                if (fullPagename.length() > 1 && fullPagename.charAt(1) == ':') {
                    // double "::" are not parsed as template/transclusion
                    pagename = fullPagename;
                    namespace = defaultNamespace;
                    namespaceText = defaultNamespace.getCanonicalName();
                    valid = false;
                    return;
                }
                fullPagename = fullPagename.substring(1);
            }

            // parse away any "#label" markers which are not supported
            int hashIndex = fullPagename.indexOf('#');
            if (hashIndex != (-1)) {
                fragment = fullPagename.substring(hashIndex + 1);
                fullPagename = fullPagename.substring(0, hashIndex);
            }

            final int colon1 = fullPagename.indexOf(':');
            final int colon2 = (-1 == colon1) ? -1 : fullPagename.indexOf(':', colon1 + 1);

            String maybeInterwikiStr0 = null;
            String maybeNamespaceStr0 = null;
            String maybePagename = null;
            if (colon1 > 0 && colon2 > 0) {
                maybeInterwikiStr0 = fullPagename.substring(0, colon1);
                maybeNamespaceStr0 = fullPagename.substring(colon1 + 1, colon2);
                maybePagename = fullPagename.substring(colon2 + 1);
            } else if (colon1 > 0) {
                maybeNamespaceStr0 = fullPagename.substring(0, colon1);
                maybePagename = fullPagename.substring(colon1 + 1);
            } else {
                maybePagename = fullPagename;
            }
            pagename = fullPagename;
            if (null != maybeInterwikiStr0) {
                maybeInterwikiStr0 = normalize(maybeInterwikiStr0);
                maybeNamespaceStr0 = normalize(maybeNamespaceStr0);
                if (wikiModel.isInterWiki(maybeInterwikiStr0)) {
                    interwiki = maybeInterwikiStr0;
                    pagename = maybeNamespaceStr0 + ":" + maybePagename;
                } else if (wikiModel.isNamespace(maybeInterwikiStr0)) {
                    pagename = maybeNamespaceStr0 + ":" + maybePagename;
                    namespaceText = maybeInterwikiStr0;
                }
            } else if (null != maybeNamespaceStr0) {
                maybeNamespaceStr0 = normalize(maybeNamespaceStr0);
                if (wikiModel.isNamespace(maybeNamespaceStr0)) {
                    pagename = maybePagename;
                    namespaceText = maybeNamespaceStr0;
                }
            }
            if (null == namespaceText) {
                namespace = defaultNamespace;
                namespaceText = namespace.getPrimaryText();
            } else {
                INamespace.INamespaceValue maybeNamespace = wikiModel.getNamespace().getNamespace(namespaceText);
                if (maybeNamespace != null) {
                    namespace = maybeNamespace;
                    valid = true;
                } else {
                    namespace = defaultNamespace;
                    namespaceText = namespace.getPrimaryText();
                    pagename = fullPagename;
                }
            }
            namespaceText = normalize(namespaceText);
            fragment = normalize(fragment);
            pagename = normalize(pagename);
        }

        LuaValue toTitle() {
            return title(
                    (null == namespaceText) ? EMPTYSTRING : LuaString.valueOf(namespaceText),
                    LuaString.valueOf(pagename),
                    fragment == null ? LuaValue.NIL : LuaString.valueOf(fragment),
                    interwiki == null ? LuaValue.NIL : LuaString.valueOf(interwiki)
                    );
        }
    }

    private String normalize(String name) {
        if (null == name) return null;
        name = name.replace("_", " ");
        name = name.trim();
        name = name.replaceAll(" +", " ");
        return name;
    }

    private boolean isValidTitle(LuaValue title, LuaValue defaultNamespace) {
        // To be complete, this method would have to replicate the logic in
        // MediaWikiTitleCodec.php#splitTitleString
        //
        // https://github.com/wikimedia/mediawiki/blob/c13fee87d42bdd6fdf6764edb6f6475c14c27749/includes/title/MediaWikiTitleCodec.php#L252
        return !title.checkjstring().trim().isEmpty();
    }

    private LuaValue makeTitle() {
        return new LibFunction() {
            /**
             * Creates a title object with title title in namespace namespace, optionally with the specified
             * fragment and interwiki prefix. namespace may be any key found in mw.site.namespaces.
             *
             * @param $ns        string|int Namespace
             * @param $text      string Title text
             * @param $fragment  string URI fragment
             * @param $interwiki string Interwiki code
             * @return if the resulting title is not valid, returns nil.
             */
            @Override
            public Varargs invoke(Varargs args) {
                LuaValue ns = args.arg(1);
                LuaValue title = args.arg(2);
                LuaValue fragment = args.arg(3);
                LuaValue interwiki = args.arg(4);

                if (isValidTitle(title, ns)) {
                    return title(ns, title, fragment, interwiki);
                } else {
                    return NIL;
                }
            }
        };
    }

    /**
     * Return the current title object, as given by the model.
     *
     * @return if the resulting title is not valid, returns nil.
     */
    private LuaValue getCurrentTitle() {
        return new ZeroArgFunction() {
            /**
             * @return the title of the current page as given by the model.
             */
            @Override
            public LuaValue call() {
                return title(wikiModel.getNamespaceName(), wikiModel.getPageName());
            }
        };
    }

    private LuaValue recordVaryFlag() {
        return new TwoArgFunction() {
            /**
             * Record a ParserOutput flag when the current title is accessed
             *
             * @param text
             * @param flag
             * @return array
             */
            @Override
            public LuaValue call(LuaValue text, LuaValue flag) {
                return new LuaTable();
            }
        };
    }

    private LuaValue title(String namespace, String pageName) {
        return title(
                toLuaString(namespace != null ? namespace : ""),
                toLuaString(pageName != null ? normalize(pageName) : ""),
                LuaValue.NIL,
                LuaValue.NIL
        );
    }

    private LuaValue title(LuaValue ns, LuaValue title, LuaValue fragment,
                           LuaValue interwiki) {
        // Standardize namespace (as ns is either an integer or a string)
        INamespace.INamespaceValue nsValue = null;
        String nsText = null;
        if (!ns.isnil()) {
            if (ns.isint()) {
                nsValue = wikiModel.getNamespace().getNamespaceByNumber(ns.toint());
                nsText = nsValue.getPrimaryText();
            } else if (ns.isstring()) {
                nsText = ns.tojstring();
                nsValue = wikiModel.getNamespace().getNamespace(nsText);
            }
        }
        if (null == nsValue) {
            nsValue = wikiModel.getNamespace().getMain();
            nsText = "";
        }

        LuaTable table = new LuaTable();
        table.set("isLocal", interwiki.isnil() ? TRUE : FALSE);
        table.set("isRedirect", EMPTYSTRING);
        table.set("subjectNsText", EMPTYSTRING);  // unused a priori (will be taken from the ns value)
        table.set("interwiki", interwiki.isnil() ? EMPTYSTRING : interwiki);
        table.set("namespace", LuaValue.valueOf(nsValue.getCode().code)); // should be the namespace id
        table.set("nsText", nsText == null ? LuaValue.EMPTYSTRING : LuaString.valueOf(nsText));
        table.set("text", title);
        table.set("id", title);
        table.set("fragment", fragment.isnil() ? EMPTYSTRING : fragment);
        table.set("contentModel", EMPTYSTRING);
        table.set("thePartialUrl", EMPTYSTRING);

        return table;
    }
}
