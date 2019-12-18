package me.kandrid.dynamicstress;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;

public final class DynamicStress extends JavaPlugin {

    //private HashMap<Player, Integer> heartRates = new HashMap<>();
    final double maxDistance = 35;

    @Override
    public void onEnable() {
        // Plugin startup logic

        new BukkitRunnable() {

            @Override
            public void run() {
                int monsters = 0;

                for (Player player : getServer().getOnlinePlayers()) {
                    //if (!heartRates.containsKey(player)) {
                    //    heartRates.put(player, 75);
                    //}

                    for (int i = 0; i < 10; i++) {
                        player.sendRawMessage("\n");
                    }

                    for (Entity entity : player.getNearbyEntities(maxDistance,maxDistance,maxDistance)) {
                        if (entity instanceof Monster || entity.getType() == EntityType.SLIME || entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.GHAST || entity.getType() == EntityType.SHULKER) {
                            if (isInSight(player, entity)) {
                                monsters++;
                            }
                        }
                    }

                    player.sendRawMessage("Hostile Mobs:" + monsters);
                }

            }

        }.runTaskTimer(this, 0L, 10L);

    }

    boolean isInSight(Player player, Entity entity) {
        boolean small = (entity.getType() == EntityType.CAVE_SPIDER || entity.getType() == EntityType.ENDERMITE || entity.getType() == EntityType.GUARDIAN || entity.getType() == EntityType.PHANTOM || entity.getType() == EntityType.SHULKER || entity.getType() == EntityType.SPIDER || entity.getType() == EntityType.SILVERFISH || entity.getType() == EntityType.VEX);
        Location eLocation = entity.getLocation();
        eLocation.setY(eLocation.getY() + (small ? 0.5 : 1.5));
        Location pLocation = player.getLocation().clone();
        pLocation.setY(pLocation.getY() + 1.5);
        final double eDistance = eLocation.distance(pLocation);
        final double fov = 60;
        final double precision = 1;
        final Vector eVector = new Vector((eLocation.getX() - pLocation.getX()) / eDistance, (eLocation.getY() - pLocation.getY()) / eDistance, (eLocation.getZ() - pLocation.getZ()) / eDistance);
        final Vector pVector = pLocation.getDirection();
        final double angle = Math.toDegrees(pVector.angle(eVector));

        if (angle <= fov) {
            for (double i = 0; i < maxDistance; i += precision) {
                if (i >= eDistance) {
                    player.sendRawMessage("Type:" + entity.getName() + " ID:" + entity.getEntityId() + " Dist:" + Math.round(eDistance * 10.0) / 10.0 + " Angle:" + Math.round(angle));
                    return true;
                }

                Vector step = pLocation.toVector().add(eVector.clone().multiply(i));
                Material m1 = player.getWorld().getBlockAt((int)Math.floor(step.getX()), (int)Math.floor(step.getY()), (int)Math.floor(step.getZ())).getType();
                Material m2 = small ? m1 : player.getWorld().getBlockAt((int)Math.floor(step.getX()), (int)Math.floor(step.getY() - 1), (int)Math.floor(step.getZ())).getType();

                if ((m1 != Material.LARGE_FERN && m1 != Material.FERN && m1 != Material.AIR && m1 != Material.CAVE_AIR && m1 != Material.VOID_AIR && m1 != Material.TALL_GRASS && m1 != Material.GRASS && m1 != Material.TALL_SEAGRASS && m1 != Material.WATER && m1 != Material.GLASS && m1 != Material.GLASS_PANE && m1 != Material.VINE) && (small || (m2 != Material.AIR && m2 != Material.CAVE_AIR && m2 != Material.VOID_AIR && m2 != Material.TALL_GRASS && m2 != Material.GRASS && m2 != Material.TALL_SEAGRASS && m2 != Material.WATER && m2 != Material.GLASS && m2 != Material.GLASS_PANE &&  m2 != Material.VINE && m2 != Material.LARGE_FERN && m2 != Material.FERN))) {
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
