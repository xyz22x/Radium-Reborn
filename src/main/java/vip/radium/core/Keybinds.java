package vip.radium.core;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import vip.radium.module.Module;
import vip.radium.module.ModuleManager;

public final class Keybinds {

    private static final Map<Module, KeyBinding> TOGGLES = new LinkedHashMap<>();

    private Keybinds() {
    }

    static void register(ModuleManager modules) {
        for (Module m : modules.getModules()) {
            KeyBinding toggle = new KeyBinding("Toggle " + m.getLabel(), 0, "key.category.radium");
            TOGGLES.put(m, toggle);
        }
        KeyBinding.resetKeyBindingArrayAndHash();
    }

    public static KeyBinding toggleOf(Module m) {
        return TOGGLES.get(m);
    }

    static void handle(Minecraft mc) {
        for (Map.Entry<Module, KeyBinding> e : TOGGLES.entrySet()) {
            while (e.getValue().isPressed()) {
                Module m = e.getKey();
                m.setEnabled(!m.isEnabled());
            }
        }
    }
}
