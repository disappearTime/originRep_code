import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class Test {

    public static void main(String[] args) {
        ScriptEngineManager factory = new ScriptEngineManager();
        for (ScriptEngineFactory available : factory.getEngineFactories()) {
            System.out.println(available.getEngineName());
        }
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String js;

        js = "var map = Array.prototype.map \n";
        js += "var names = [\"john\", \"jerry\", \"bob\"]\n";
        js += "var a = map.call(names, function(name) { return name.length() })\n";
        js += "print(a)";
        try {
            engine.eval(js);
        } catch (ScriptException e) {
            e.printStackTrace();
        }

    }
}
