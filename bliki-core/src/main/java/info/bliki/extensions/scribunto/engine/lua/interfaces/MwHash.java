package info.bliki.extensions.scribunto.engine.lua.interfaces;

import static info.bliki.extensions.scribunto.engine.lua.ScribuntoLuaEngine.toLuaString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class MwHash implements MwInterface {

    @Override
    public String name() {
        return "mw.hash";
    }

    @Override
    public LuaTable getInterface() {
        LuaTable iface = new LuaTable();
        iface.set("listAlgorithms", listAlgorithms());
        iface.set("hashValue", hashValue());
        return iface;
    }


    private static final String[] algos = new String[]{"SHA-256"};
    private LuaValue listAlgorithms() {
        return new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable list = new LuaTable();
                for (int i = 0; i < algos.length; i++) {
                    LuaString algo = toLuaString(algos[i]);
                    list.set(i+1,algo);
                }
                return list;
            }
        };
    }

    private LuaValue hashValue() {
        return new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue algo, LuaValue value) {
                LuaString strval = value.checkstring();
                try {
                    MessageDigest digest = MessageDigest.getInstance(algo.tojstring());
                    byte[] hash = digest.digest(strval.m_bytes);
                    return LuaString.valueOf(hash);
                } catch (NoSuchAlgorithmException e) {
                    throw new LuaError("Unknown hashing algorithm: " + algo.tojstring());
                }
            }
        };
    }

    @Override
    public LuaValue getSetupOptions() {
        return new LuaTable();
    }
}
