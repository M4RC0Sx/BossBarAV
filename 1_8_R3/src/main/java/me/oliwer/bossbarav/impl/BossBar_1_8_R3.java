package me.oliwer.bossbarav.impl;

import me.oliwer.bossbar.api.BarEntity;
import me.oliwer.bossbar.api.BarUpdate;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static net.minecraft.server.v1_8_R3.MathHelper.floor;

public final class BossBar_1_8_R3 extends BarEntity<EntityWither, DataWatcher> {
    public BossBar_1_8_R3(World world, float healthPercentage, String text) {
        super(new EntityWither(((CraftWorld) world).getHandle()));

        // assign to update
        this.latestUpdate.text = text;
        this.latestUpdate.healthPercentage = healthPercentage;

        // set the location
        this.entity.setLocation(0, 0, 0, 0, 0);

        // create and assign data watcher
        this.watcher = this.createWatcher();
    }

    @Override
    public void show(Player player) {
        // make sure player is not already seeing this bar
        if (this.isShown(player)) {
            return;
        }

        // create the packet
        final PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(this.entity);

        // set said watcher to packet if present
        if (this.watcher != null) {
            try {
                final Field livingField = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("l");
                livingField.setAccessible(true);
                livingField.set(packet, this.watcher);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to get declared field of PacketPlayOutSpawnEntityLiving");
            }
        }

        // send packet
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        // handle show functionality at last
        super.show(player);
    }

    @Override
    public void hide(Player player) {
        // make sure the player is not already hidden
        if (!this.isShown(player)) {
            return;
        }

        // create and send packet
        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.entity.getId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        // handle hide functionality at last
        super.hide(player);
    }

    @Override
    public void update(Consumer<BarUpdate> action) {
        // create new update and consume the modification(s)
        action.accept(latestUpdate);

        // get the update and assign watcher
        this.watcher = this.createWatcher();

        // build and send packet to all
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entity.getId(), this.watcher, true);
        this.shown.forEach(it -> ((CraftPlayer) it).getHandle().playerConnection.sendPacket(packet));
    }

    @Override
    protected DataWatcher createWatcher() {
        final DataWatcher dataWatcher = new DataWatcher(this.entity);
        final BarUpdate update = this.latestUpdate;

        dataWatcher.a(0, (byte) (1 << 5));
        dataWatcher.a(6, (update.healthPercentage * 200) / 100);
        dataWatcher.a(10, update.text);
        dataWatcher.a(2, update.text);
        dataWatcher.a(11, (byte) 1);
        dataWatcher.a(3, (byte) 1);
        dataWatcher.a(20, 750);

        return dataWatcher;
    }

    @Override
    public void tick() {
        // update
        for (final Player player : this.shown) {
            // create location
            final Vector directional = player.getEyeLocation().getDirection().multiply(30);
            final Location old = player.getLocation();
            final Location location = new Location(old.getWorld(), old.getX(), old.getY(), old.getZ(), old.getYaw(), old.getPitch())
                .add(directional);

            // send packet
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(
                this.entity.getId(),
                floor(location.getX() * 32.0D),
                floor(location.getY() * 32.0D),
                floor(location.getZ() * 32.0D),
                (byte) ((int) (location.getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (location.getPitch() * 256.0F / 360.0F)),
                this.entity.onGround
            ));
        }
    }

    public static BarEntity<?, ?> create(World world, float healthPercentage, String text) {
        return new BossBar_1_8_R3(world, healthPercentage, text);
    }
}