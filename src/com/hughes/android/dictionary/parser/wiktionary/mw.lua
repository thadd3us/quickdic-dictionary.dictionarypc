-- http://www.mediawiki.org/wiki/Extension:Scribunto/Lua_reference_manual
-- http://en.wiktionary.org/wiki/Wiktionary:Scribunto

--require('org.luaj.vm2.lib.DebugLib')

mw = {}

function mw.loadData(name)
  return require(name)
end

function mw.getCurrentFrame()
  return {}
end

mw.title = {}

mw.title.CURRENT_TITLE = {}
mw.title.CURRENT_TITLE.text = ""  -- TODO: wrong
mw.title.CURRENT_TITLE.prefixedText = ""
mw.title.CURRENT_TITLE.prefixedText = ""
mw.title.CURRENT_TITLE.subpageText = ""
mw.title.CURRENT_TITLE.baseText = ""
mw.title.CURRENT_TITLE.nsText = ""
function mw.title.getCurrentTitle()
  return mw.title.CURRENT_TITLE
end


mw.ustring = {}
function mw.ustring.char(codepoint)
  return "_"  -- TODO: wrong
end
function mw.ustring.lower(s)
  return s  -- TODO: wrong
end
function mw.ustring.gsub(s, pattern, repl, n)
  return s  -- TODO: wrong
end


mw.language = {}
function mw.language.isKnownLanguageTag(code)
  return true
end