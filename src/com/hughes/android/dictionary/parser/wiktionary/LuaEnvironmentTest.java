package com.hughes.android.dictionary.parser.wiktionary;

import static org.junit.Assert.*;

import org.junit.Test;
import org.luaj.vm2.LuaValue;

import java.util.logging.Logger;

public class LuaEnvironmentTest {
    
    static final Logger LOG = Logger.getLogger("LuaEnvironmentTest");


    @Test
    public void test() {
        final String moduleSource = 
            "local export = {}\n" + 
            "function export.myFunction(frame)\n" + 
            "    return frame .. \"_hello\"\n" +
            "end\n" +
            "return export"
        ;
                             
        LuaEnvironment luaEnvironment = new LuaEnvironment();
        luaEnvironment.preloadModule("hello", moduleSource);
        
        LuaValue m = luaEnvironment.globals.load("s = ''; for k,v in pairs(package.loaded) do s = s .. k .. ' '; end return s;", "myscript").call();
        LOG.info(m.tojstring());
        
        m = luaEnvironment.globals.load("s = ''; for k,v in pairs(package.preload) do s = s .. k .. ' '; end return s;", "myscript").call();
        LOG.info(m.tojstring());

        
        m = luaEnvironment.globals.load("h = require(\"hello\").myFunction('asdf'); return h").call();
        LOG.info(m.tojstring());
        assertEquals("asdf_hello", m.tojstring());
    }

}
