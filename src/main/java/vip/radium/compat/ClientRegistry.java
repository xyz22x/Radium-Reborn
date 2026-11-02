package vip.radium.compat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public final class ClientRegistry {

    private static Map<String, Integer> saved;

    private ClientRegistry() {
    }

    public static void registerKeyBinding(KeyBinding key) {
        Minecraft mc = Minecraft.getMinecraft();
        String name = key.getKeyDescription();
        KeyBinding[] list = mc.gameSettings.keyBindings;

        for (int i = list.length - 1; i >= 0; i--) {
            KeyBinding k = list[i];
            if (k != key && name.equals(k.getKeyDescription()) && key.getKeyCategory().equals(k.getKeyCategory())) {
                KeyBinding[] newList = new KeyBinding[list.length - 1];
                int j = 0;
                for (int x = 0; x < list.length; x++) {
                    if (x != i) {
                        newList[j++] = list[x];
                    }
                }
                list = newList;
            }
        }

        mc.gameSettings.keyBindings = list;
        Integer code = saved().get("key_" + name);
        if (code != null) {
            key.setKeyCode(code);
        }

        boolean contains = false;
        for (KeyBinding k : mc.gameSettings.keyBindings) {
            if (k == key) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            KeyBinding[] newList = new KeyBinding[mc.gameSettings.keyBindings.length + 1];
            System.arraycopy(mc.gameSettings.keyBindings, 0, newList, 0, mc.gameSettings.keyBindings.length);
            newList[newList.length - 1] = key;
            mc.gameSettings.keyBindings = newList;
        }
    }

    private static Map<String, Integer> saved() {
        if (saved != null) {
            return saved;
        } else {
            saved = new HashMap<>();
            File file = new File(Minecraft.getMinecraft().mcDataDir, "options.txt");
            if (!file.isFile()) {
                return saved;
            } else {
                try {
                    for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                        int colon = line.lastIndexOf(58);
                        if (line.startsWith("key_") && colon > 4) {
                            try {
                                saved.put(line.substring(0, colon), Integer.parseInt(line.substring(colon + 1).trim()));
                            } catch (NumberFormatException var5) {
                            }
                        }
                    }
                } catch (RuntimeException | IOException var6) {
                }

                return saved;
            }
        }
    }
}
