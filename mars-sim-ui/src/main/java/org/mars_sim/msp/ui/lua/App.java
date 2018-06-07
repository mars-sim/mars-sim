package org.mars_sim.msp.ui.lua;

//import org.luaj.vm2.Globals;
//import org.luaj.vm2.LuaValue;
//import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Sample source file for a maven project that depends on luaj.

public class App
{
    public static void main( String[] args )
    {
		String script1 = "print('Hello, Mars!')";
		String script2 = "print('Running version', _VERSION)";


		// create an environment to run in
		Globals globals = JsePlatform.standardGlobals();

		// Use the convenience function on the globals to load a chunk.
		LuaValue chunk = globals.load(script1, "maven-exmaple");

		// Use any of the "call()" or "invoke()" functions directly on the chunk.
		chunk.call();
    }
}
 */