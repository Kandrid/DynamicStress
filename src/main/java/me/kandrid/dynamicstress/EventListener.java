package me.kandrid.dynamicstress;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DynamicStress.getHeartRates().put(event.getPlayer().getUniqueId(), DynamicStress.baseHeartRate);
        DynamicStress.getMobsInSight().put(event.getPlayer().getUniqueId(), new HashSet<Integer>());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        DynamicStress.getHeartRates().remove(event.getPlayer().getUniqueId());
        DynamicStress.getMobsInSight().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            UUID playerID = event.getEntity().getUniqueId();
            final double heartRate = DynamicStress.getHeartRates().get(playerID);
            if (heartRate < 150) {
                DynamicStress.getHeartRates().put(playerID, heartRate + 10);
            }
        }
    }

}
