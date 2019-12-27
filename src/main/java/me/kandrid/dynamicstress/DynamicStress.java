package me.kandrid.dynamicstress;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.beans.EventHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class DynamicStress extends JavaPlugin {

    private static EventListener eventListener;

    private static CommandListener commandListener;

    private static HashMap<UUID, Double> heartRates = new HashMap<>();

    protected static HashMap<UUID, Double> getHeartRates() { return heartRates; }

    private static HashMap<UUID, HashSet<Integer>> mobsInSight = new HashMap<>();

    protected static HashMap<UUID, HashSet<Integer>> getMobsInSight() { return mobsInSight; }

    private static HashMap<UUID, Double> playerBeats = new HashMap<>();

    final static double maxDistance = 35;
    final static double baseHeartRate = 75;
    private static boolean itemTitleModify = false;
    protected static boolean debug = false;
    private static int raySteps = 0;

    private final HashSet<EntityType> otherHostiles = new HashSet<>(Arrays.asList(
            EntityType.SLIME,
            EntityType.MAGMA_CUBE,
            EntityType.GHAST,
            EntityType.SHULKER,
            EntityType.PHANTOM
    ));

    private final HashSet<EntityType> smallMobs = new HashSet<>(Arrays.asList(
            EntityType.CAVE_SPIDER,
            EntityType.ENDERMITE,
            EntityType.ENDERMITE,
            EntityType.GUARDIAN,
            EntityType.PHANTOM,
            EntityType.SHULKER,
            EntityType.SPIDER,
            EntityType.SILVERFISH,
            EntityType.VEX
    ));

    private final HashSet<Material> leaves = new HashSet<>(Arrays.asList(
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES
    ));

    @Override
    public void onEnable() {
        // Plugin startup logic

        eventListener = new EventListener();
        commandListener = new CommandListener();

        getCommand("ds_debug").setExecutor(commandListener);
        Bukkit.getPluginManager().registerEvents(eventListener, this);

        new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    getHeartRates().computeIfAbsent(player.getUniqueId(), k -> baseHeartRate);
                    getMobsInSight().computeIfAbsent(player.getUniqueId(), k -> new HashSet<Integer>());

                    double distance = maxDistance;
                    double normalDistance = maxDistance;
                    double newDistance = 0;
                    HashSet<Integer> mobIDs = new HashSet<>();
                    HashSet<Integer> entityIDs = new HashSet<>();

                    if (debug) {
                        for (int i = 0; i < 15; i++) {
                            player.sendRawMessage("");
                        }
                        raySteps = 0;
                    }

                    for (Entity entity : player.getNearbyEntities(maxDistance,maxDistance,maxDistance)) {
                        if (entity instanceof Monster || otherHostiles.contains(entity.getType())) {
                            entityIDs.add(entity.getEntityId());
                            newDistance = player.getLocation().distance(entity.getLocation());
                            if (newDistance < normalDistance) {
                                normalDistance = newDistance;
                            }
                            if (isInSight(player, (LivingEntity)entity)) {
                                mobIDs.add(entity.getEntityId());
                                if (!getMobsInSight().get(player.getUniqueId()).contains(entity.getEntityId())) {
                                    if (newDistance < distance) {
                                        distance = newDistance;
                                    }
                                }
                            }
                        }
                    }

                    if (debug) {
                        player.sendRawMessage("RaySteps:" + raySteps);
                    }

                    double finalDistance = normalDistance;
                    getMobsInSight().get(player.getUniqueId()).removeIf(mobID -> (!entityIDs.contains(mobID) || (!mobIDs.contains(mobID) && finalDistance > 7)));
                    getMobsInSight().get(player.getUniqueId()).addAll(mobIDs);

                    UpdateHeartRate(player, distance);
                    double doubleHeartRate = getHeartRates().get(player.getUniqueId());
                    HeartRateSymptoms(player, doubleHeartRate);
                    int heartRate = (int)Math.round(doubleHeartRate);

                    if (player.getInventory().getItemInOffHand().getType() == Material.OAK_BUTTON || player.getInventory().getItemInMainHand().getType() == Material.OAK_BUTTON) {
                        if (heartRate < 100) {
                            player.sendTitle("", "" + ChatColor.BOLD + ChatColor.GREEN + heartRate + ChatColor.DARK_GRAY + " BPM", 0, 11, 0);
                        } else if (heartRate < 150) {
                            player.sendTitle("", "" + ChatColor.BOLD + ChatColor.RED + heartRate + ChatColor.DARK_GRAY + " BPM", 0, 11, 0);
                        } else if (heartRate < 175) {
                            player.sendTitle("", "" + ChatColor.BOLD + ChatColor.DARK_RED + heartRate + ChatColor.DARK_GRAY + " BPM", 0, 11, 0);
                        } else {
                            player.sendTitle("", "" + ChatColor.BOLD + ChatColor.BLACK + heartRate + ChatColor.DARK_GRAY + " BPM", 0, 11, 0);
                        }
                    }
                    
                    HashMap<Player, Double> playerDistances = new HashMap<>();
                    String title = "";

                    for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                        if (!onlinePlayer.equals(player)) {
                            playerDistances.put(onlinePlayer, player.getLocation().distanceSquared(onlinePlayer.getLocation()));
                        }
                    }

                    if (playerDistances.size() == 0) {
                        title = ChatColor.DARK_AQUA + "No Players Detected";
                    } else {
                        for (int i = 0; i < 3 && playerDistances.size() > 0; i++) {
                            HashMap.Entry<Player, Double> min = null;

                            for (HashMap.Entry<Player, Double> entry : playerDistances.entrySet()) {
                                if (min == null || min.getValue() > entry.getValue()) {
                                    min = entry;
                                }
                            }

                            double otherHeartRate = getHeartRates().get(min.getKey().getUniqueId());

                            title += (ChatColor.DARK_AQUA + min.getKey().getDisplayName() + ": " + ChatColor.AQUA + (int) Math.round(otherHeartRate) + ChatColor.DARK_GRAY + " BPM ");
                            playerDistances.remove(min.getKey());
                        }
                    }

                    if (itemTitleModify) {
                        title = ChatColor.DARK_BLUE + "- " + title + ChatColor.DARK_BLUE + " -";
                        itemTitleModify = false;
                    } else {
                        itemTitleModify = true;
                    }

                    if (player.getInventory().getItemInMainHand().getType() == Material.OAK_BUTTON) {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        ItemMeta itemMeta = item.getItemMeta();
                        itemMeta.setDisplayName(title);
                        item.setItemMeta(itemMeta);
                    }
                }
            }

        }.runTaskTimer(this, 0L, 10L);

        new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    getHeartRates().computeIfAbsent(player.getUniqueId(), k -> baseHeartRate);
                    playerBeats.computeIfAbsent(player.getUniqueId(), k -> 0.0);
                    double heartBeat = getHeartRates().get(player.getUniqueId());
                    double progress = playerBeats.get(player.getUniqueId());
                    if (progress >= 60.0 / heartBeat) {
                        if (player.getInventory().contains(Material.OAK_BUTTON)) {
                            player.playNote(player.getLocation().toVector().add(new Vector(0, 80.0 - 0.4 * heartBeat,0)).toLocation(player.getWorld()), Instrument.BASS_DRUM, Note.natural(0, Note.Tone.A));
                        }
                        progress = 0.0;
                    } else {
                        progress += 0.1;
                    }
                    playerBeats.put(player.getUniqueId(), progress);
                }
            }

        }.runTaskTimer(this, 20L, 2L);
    }

    private double StartleBPM(double distance) {
        double result = (1200 / (distance + 5) - 0.1 * Math.pow(distance, 3) - 0.001 * Math.pow(distance, 5) - baseHeartRate * 0.6);
        return result > 0 ? result : 0;
    }

    private void UpdateHeartRate(Player player, double distance) {
        UUID playerID = player.getUniqueId();
        final HashSet<Integer> mobIDs = getMobsInSight().get(playerID);
        final double minHeartRate = baseHeartRate + mobIDs.size() * (baseHeartRate / 8.0);
        double current = getHeartRates().get(playerID);
        double startle = StartleBPM(distance);
        current += startle * Math.pow(1.0 - (current - baseHeartRate + 2) / (200.0 - baseHeartRate), 2);
        if (current < 150 && player.isSprinting()) {
            current += 1.5;
        }
        if (current - minHeartRate > 0 || current > 150) {
            current -= 0.5;
        } else {
            current += 2.5;
        }
        if (current > 200) {
            current = 200;
        }
        getHeartRates().put(playerID, current);
    }

    private void HeartRateSymptoms(Player player, double heartRate) {
        if (heartRate >= 175) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 2));
        }
        if (heartRate >= 160) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0));
        }
        if (heartRate >= 150) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 0));
        }
    }

    private boolean isInSight(Player player, LivingEntity entity) {
        boolean small = smallMobs.contains(entity.getType());
        Location eLocation = entity.getLocation();
        eLocation.setY(eLocation.getY() + (small ? 0.5 : 1.5));
        Location pLocation = player.getLocation().clone();
        pLocation.setY(pLocation.getY() + (player.isSneaking() ? 1.25 : 1.6));
        final double eDistance = eLocation.distance(pLocation);
        final double fov = 60;
        final double precision = 1;
        final Vector eVector = new Vector((eLocation.getX() - pLocation.getX()) / eDistance, (eLocation.getY() - pLocation.getY()) / eDistance, (eLocation.getZ() - pLocation.getZ()) / eDistance);
        final Vector pVector = pLocation.getDirection();
        final double angle = Math.toDegrees(pVector.angle(eVector));
        final double angleDown = Math.toDegrees(eVector.angle(new Vector(0, -1, 0)));

        if (angle <= fov) {
            Vector step;
            Material m1, m2;
            HashSet<Location> steps = new HashSet<>();

            for (double i = 0; i < maxDistance; i += precision) {
                if (debug) {
                    raySteps++;
                }

                if (i >= eDistance) {
                    if (debug) {
                        for (Location loc : steps) {
                            player.spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
                        }
                        player.sendRawMessage(entity.getType() + "-" + entity.getEntityId() + " D:" + Math.round(player.getLocation().distance(entity.getLocation()) * 100.0) / 100.0 + " A1:" + Math.round(angle * 100.0) / 100.0 + " A2:" + Math.round(angleDown * 100.0) / 100.0);
                    }
                    return true;
                }

                step = pLocation.toVector().add(eVector.clone().multiply(i));
                if (i > 1 && debug) {
                    steps.add(step.toLocation(player.getWorld()));
                }

                m1 = player.getWorld().getBlockAt(step.getBlockX(), step.getBlockY(), step.getBlockZ()).getType();
                m2 = small ? m1 : player.getWorld().getBlockAt(step.getBlockX(), step.getBlockY() - 1, step.getBlockZ()).getType();

                if ((m1.isOccluding() || leaves.contains(m1)) && (small || (m2.isOccluding() || leaves.contains(m2) || angleDown < 45))) {
                    if (debug) {
                        for (Location loc : steps) {
                            player.spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
                        }
                    }
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
