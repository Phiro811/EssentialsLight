package net.phiro;



import org.bukkit.Location;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageEvent;

import org.bukkit.event.player.PlayerMoveEvent;



public class TeleportationListener implements Listener {



    private final Main plugin;



    public TeleportationListener(Main plugin) {

        this.plugin = plugin;

    }



    @EventHandler

    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        TeleportationManager manager = plugin.getTeleportationManager();

        if (manager.isTeleporting(player.getUniqueId())) {

            Location from = event.getFrom();

            Location to = event.getTo();



// Wenn der Spieler sich signifikant bewegt

// Kleine Toleranz könnte hier hinzugefügt werden (z.B. from.distanceSquared(to) > 0.01)

// Für den Anfang ist dies aber ausreichend.

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {

                manager.cancelTeleport(player.getUniqueId());

            }

        }

    }



    @EventHandler

    public void onEntityDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();

            TeleportationManager manager = plugin.getTeleportationManager();

            if (manager.isTeleporting(player.getUniqueId())) {

                manager.cancelTeleport(player.getUniqueId());

            }

        }

    }

}