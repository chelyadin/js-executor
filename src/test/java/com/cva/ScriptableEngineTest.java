package com.cva;

import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by zdv on 23.12.15.
 */
public class ScriptableEngineTest {

    @Test
    public void testEval() throws ScriptException, NoSuchMethodException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        assertNotNull(engine);
        Object result = engine.eval("var v = { a: 'string', b: { c: '2015-12-22', d: false, e: 1e6 } }; v;");
        Object JSON = engine.get("JSON");
        Invocable i = (Invocable) engine;
        String s = (String) i.invokeMethod(JSON, "stringify", result);
        System.out.print(s);
        assertEquals("Must match to stringified ", "{\"a\":\"string\",\"b\":{\"c\":\"2015-12-22\",\"d\":false,\"e\":1000000}}", s);
    }
}
