package com.hughes.android.dictionary.parser.wiktionary;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class LuaEnvironment {
    
    static final Logger LOG = Logger.getLogger("LuaEnvironment");

    Globals globals;
    LuaValue lua_package;
    LuaValue lua_package_loaded;
    LuaValue customPackages;
    
    LuaTable parentFrame;
    LuaTable frame;
    
    public LuaEnvironment() {
        globals = JsePlatform.debugGlobals();
        lua_package = globals.get("package");
        lua_package_loaded = lua_package.get("loaded");
        
        customPackages = globals.load("customPackages = {}; return customPackages").call();
        globals.load("function CustomPackageLoader(modname) return load(customPackages[modname], modname)(); end").call();
        globals.load("function FindCustomPackageLoader(modname) if customPackages[modname] then return CustomPackageLoader; end return nil; end").call();
        
        parentFrame = (LuaTable) globals.load("parentFrame = {}; return parentFrame").call();

        frame = (LuaTable) globals.load("frame = {}; return frame").call();

        // TODO: this isn't right, the parent of an #invoke should be the template's frame!
        frame.set("getParent", globals.load("return function (self) return parentFrame; end").call());
    }
    
    public static void setFrameArgs(final LuaTable frame, 
            final List<String> positionArgs, 
            final Map<String, String> namedArgs) {
        // for k,v in pairs(tab) do tab[k]=nil end
        LuaTable args = new LuaTable();
        for (int i = 0; i < positionArgs.size(); ++i) {
            args.set(i + 1, positionArgs.get(i));
        }
        for (final Map.Entry<String, String> namedArg : namedArgs.entrySet()) {
            args.set(namedArg.getKey(), namedArg.getValue());
        }
        frame.set("args", args);
    }
    
    Pattern ASCII = Pattern.compile("[^\\p{ASCII}]");
    
    void preloadModule(String moduleName, String moduleText) {
//        moduleText = moduleText.replaceAll("“", ".");
//        moduleText = moduleText.replaceAll("”", ".");
        moduleText = ASCII.matcher(moduleText).replaceAll("_");
        try {
            customPackages.set(moduleName, moduleText);
            globals.load(String.format("package.preload[\"%s\"] = CustomPackageLoader", moduleName)).call();
        } catch (LuaError luaError) {
            LOG.log(Level.WARNING, "Failure loading module: " + moduleName + ", " + moduleText, luaError);
        }
    }
}
