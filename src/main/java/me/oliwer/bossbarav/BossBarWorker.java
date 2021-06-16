package me.oliwer.bossbarav;

import me.oliwer.bossbar.api.BarEntity;
import me.oliwer.bossbarav.impl.BossBar_1_8_R1;
import me.oliwer.bossbarav.impl.BossBar_1_8_R2;
import me.oliwer.bossbarav.impl.BossBarNewer;
import me.oliwer.bossbarav.impl.BossBar_1_8_R3;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Method;

/**
 * This class is the bossbar worker - is handles stuff such as reflection based interactions, creation etc.
 */
public final class BossBarWorker {
    /**
     * {@link Method} this method is the instance creator.
     */
    private static final Method INSTANCE_CREATOR;

    /** No instantiation needed. **/
    private BossBarWorker() {
        throw new IllegalStateException("BossBarWorker is not to be instantiated");
    }

    /**
     * Create a new boss bar by location, health percentage and text.
     *\
     * @param healthPercentage {@link Float} the health percentage of this bossbar.
     * @param text             {@link String} the wanted text to be displayed.
     * @return {@link BarEntity}
     */
    public static BarEntity<?, ?> create(World world, float healthPercentage, String text) {
        try { return (BarEntity<?, ?>) INSTANCE_CREATOR.invoke(null, world, healthPercentage, text); }
        catch (Exception ex) { return null; }
    }

    /* Initialize the NMS version constant. */
    static {
        // necessity
        final String unformatted = Bukkit.getServer().getClass().getPackage().getName();
        final String version = unformatted.substring(unformatted.lastIndexOf('.') + 1);
        final Class<? extends BarEntity<?, ?>> type;

        // fetch correct bar type
        switch (version) {
            case "v1_8_R1": {
                type = BossBar_1_8_R1.class;
                break;
            }

            case "v1_8_R2": {
                type = BossBar_1_8_R2.class;
                break;
            }

            case "v1_8_R3": {
                type = BossBar_1_8_R3.class;
                break;
            }

            default: type = BossBarNewer.class;
        }

        // define instancer method
        Method method = null;

        // assign above variable with said declared method
        try {
            method = type.getDeclaredMethod("create", World.class, float.class, String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        // initialize INSTANCER
        INSTANCE_CREATOR = method;
    }
}