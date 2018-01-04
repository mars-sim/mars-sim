package org.mars_sim.msp.ui.lua;


import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaTest {

	public LuaTest() {}

    public static void main(String[] args)  {

    	String script = "/lua/luatest.lua";
    	//URL url = LuaTest.class.getResource(script).toExternalForm();

    	// From luatest.lua
    	// function MyAdd( num1, num2 )
        //		return num1 + num2
        // end

        //run the lua script defining your function
        LuaValue _G = JsePlatform.standardGlobals();
        _G.get("dofile").call( LuaValue.valueOf(script));

        //call the function MyAdd with two parameters 5, and 5
        LuaValue addition = _G.get("MyAdd");
        LuaValue retvals = addition.call(LuaValue.valueOf(5), LuaValue.valueOf(5));

        //print out the result from the lua function
        System.out.println("The result from executing the script is :\n\n" + retvals.tojstring(1));
    }
}
