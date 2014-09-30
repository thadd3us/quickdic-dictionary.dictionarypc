package com.hughes.android.dictionary.parser.wiktionary;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class LuaEnvironment {
    
    static final Logger LOG = Logger.getLogger("LuaEnvironment");

    Globals globals;
    LuaValue lua_package;
    LuaValue lua_package_loaded;
    LuaValue customPackages;
    
    LuaTable frame;
    
    public LuaEnvironment() {
        globals = JsePlatform.standardGlobals();
        lua_package = globals.get("package");
        lua_package_loaded = lua_package.get("loaded");
        
        customPackages = globals.load("customPackages = {}; return customPackages").call();
        globals.load("function CustomPackageLoader(modname) return load(customPackages[modname], modname)(); end").call();
        globals.load("function FindCustomPackageLoader(modname) if customPackages[modname] then return CustomPackageLoader; end return nil; end").call();
        
        frame = new LuaTable();
        frame.set("getParent", globals.load("return function (self) return self; end").call());
        
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
