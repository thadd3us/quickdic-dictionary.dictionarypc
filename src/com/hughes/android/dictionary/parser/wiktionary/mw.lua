-- http://www.mediawiki.org/wiki/Extension:Scribunto/Lua_reference_manual
-- http://en.wiktionary.org/wiki/Wiktionary:Scribunto

mw = {}

function mw.loadData(name)
  return require(name)
end


mw.ustring = {}

function mw.ustring.char(codepoint)
  return "_"
end
