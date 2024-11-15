package me.sharyxxx.protectionstonesgui.CMD;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CMDHandler implements Listener {
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        PSPlayer psPlayer = PSPlayer.fromPlayer(player);
        List<PSRegion> regionList = psPlayer.getPSRegions(Bukkit.getWorld("world"), false);

        if (!command.startsWith("/dzialka") && !command.startsWith("/ps")) {
            return;
        }
        if(regionList.size() == 0) {
            player.sendMessage(ChatColor.RED + "Nie posiadasz działki! Wpisz " + ChatColor.YELLOW + "/dzialki");
            event.setCancelled(true);
            return;
        }
    }
}
