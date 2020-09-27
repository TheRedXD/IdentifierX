package io.github.theredxd.identifierx;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class IdentifierX extends JavaPlugin implements Listener {
    boolean lockDown = false;
    boolean adminOnly = false;
    public String genPluginMsg(String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6"+msg);
    }
    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults();
        saveDefaultConfig(); // afk
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        System.out.println("[IdentifierX] The plugin has loaded!");
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    List<String> allowedIPS = this.getConfig().getStringList("allowed_ips");
    List<String> blockedUsers = new ArrayList<String>();
    public void updateConfig() {
        allowedIPS = this.getConfig().getStringList("allowed_ips");
    }
    @EventHandler
    public void onPreJoin(PlayerPreLoginEvent e) {
        if(adminOnly) {
            blockedUsers.add(e.getName());
            return;
        }
        if(!lockDown) return;
        System.out.println(e.getAddress().getHostAddress());
        if(e.getAddress().getHostAddress().equals("127.0.0.1")) return;
        else {
            if(allowedIPS.contains(e.getAddress().getHostAddress())) return;
            for(Player player: Bukkit.getOnlinePlayers()) {
                if(player.hasPermission("identifierx.seeip")) {
                    //player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6A player with the username &c"+e.getName()+"&6 has attempted to log in with the ip &c"+e.getAddress().getHostAddress()+"&6. To whitelist a IP, please run &c/identifierx add <ip>&6, or &nclick here&6."));
                    TextComponent text = new TextComponent();
                    text.setText(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6A player with the username &c"+e.getName()+"&6 has attempted to log in with the ip &c"+e.getAddress().getHostAddress()+"&6. To whitelist a IP, please run &c/identifierx add <ip>&6, or "));
                    TextComponent text2 = new TextComponent();
                    text2.setText(ChatColor.translateAlternateColorCodes('&', "&n&9click here&6."));
                    text2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/identifierx add "+e.getAddress().getHostAddress()));
                    text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&',"&bClick here in order to add the IP to the allowed IP list"))));
                    text.addExtra(text2);
                    player.spigot().sendMessage(text); //docs == documentation
                }
            }

            e.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8]\n &6Your IP is not whitelisted!")); // im playing with a ping pong ball cuz yes also mom back
        } //can u test this?
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("identifierx")) {
            if(args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6Missing argument: subcommand."));
                return true;
            }
            switch (args[0]) {
                case "add":
                    if(args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8[&cIdentifierX&8] &6Missing argument: IP"));
                        return true;
                    }
                    if(args[1].equals("127.0.0.1")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6You can't add localhost to the IP list!"));
                        return true;
                    }
                    if(allowedIPS.contains(args[1])) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6That IP is already in the IP list!"));
                        return true;
                    }
                    allowedIPS.add(args[1]);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6The IP (&c"+args[1]+"&6) was successfully added!"));
                    System.out.println(allowedIPS);
                getConfig().set("allowed_ips", allowedIPS);
                    try {
                        getConfig().save("config.yml");
                        saveConfig();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateConfig();
                    System.out.println(allowedIPS);
                    break;
                case "remove":
                    if(args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8[&cIdentifierX&8] &6Missing argument: IP"));
                        return true;
                    }
                    if(args[1].equals("127.0.0.1")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6You can't remove localhost from the IP list!"));
                        return true;
                    }
                    if(!allowedIPS.contains(args[1])) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6That IP is not in the IP list!"));
                        return true;
                    }
                    allowedIPS.remove(args[1]);
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player.getAddress().getHostString().equals(args[1])) {
                            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8]\n&6Your IP just got removed.\nIP: &9"+args[1]));
                        }
                    }
                    getConfig().set("allowed_ips", allowedIPS);
                    try {
                        getConfig().save("config.yml");
                        saveConfig();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8]&6 The IP (&c"+args[1]+"&6) was successfully removed!"));
                    break;
                case "list":
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8]&6 List of all added IPs:"));
                    for(String ip : allowedIPS) {
                        int number = allowedIPS.indexOf(ip);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6"+(number+1)+" - " + ip));
                    }
                    break;
                case "lockdown":
                    if(!sender.hasPermission("identifierx.command.lockdown")) {
                        sender.sendMessage(genPluginMsg("No perms!"));
                        return true;
                    }
                    if(lockDown) {
                        sender.sendMessage(genPluginMsg("Disabled lockdown!"));
                        lockDown = false;
                    } else {
                        sender.sendMessage(genPluginMsg("Enabled lockdown!"));
                        lockDown = true;
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            player.kickPlayer(genPluginMsg("Lockdown just got enabled! You can log back in if you have a verified IP!"));
                        }
                    }
                    break;
                case "adminonly":
                    if(sender.hasPermission("identifierx.adminonly.toggle")) {
                        if(adminOnly) {
                            sender.sendMessage(genPluginMsg("Disabled admin only mode"));
                            adminOnly = false;
                        } else {
                            sender.sendMessage(genPluginMsg("Enabled admin only mode"));
                            adminOnly = true;
                            for(Player player : Bukkit.getOnlinePlayers()) {
                                if(player.hasPermission("identifierx.adminonly.bypass")) return true;
                                else player.kickPlayer(genPluginMsg("Admin only mode has been enabled!"));
                            }
                        }
                        return true;
                    } else {
                        sender.sendMessage(genPluginMsg("No perms!"));
                        return true;
                    }
                case "secretcommand":
                    sender.sendMessage("yay u found the secret command!!!!!!!");
                    break;
                default:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cIdentifierX&8] &6"));
            }

        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equals("identifierx")) {
            if(args.length == 1 && args[0].equals("")) {
                List<String> returnArgs = new ArrayList<>();
                returnArgs.add("add");
                returnArgs.add("remove");
                returnArgs.add("list");
                returnArgs.add("lockdown");
                returnArgs.add("adminonly");
                return returnArgs;
            } else if(args[0].equals("remove")) {
                return allowedIPS;
            }
        }
        return new ArrayList<>();
    }
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent e) {
        if(blockedUsers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(blockedUsers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(!adminOnly) return;
        if(blockedUsers.contains(e.getPlayer().getName())) {
            if(e.getPlayer().hasPermission("identifierx.adminonly.bypass")) blockedUsers.remove(e.getPlayer().getName());
            else e.getPlayer().kickPlayer(genPluginMsg("Admin mode is currently enabled."));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(blockedUsers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(blockedUsers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            if(blockedUsers.contains(e.getEntity().getName())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!adminOnly) return;
        if(e.getPlayer().hasPermission("identifierx.adminonly.bypass")) blockedUsers.remove(e.getPlayer().getName());
        else {
            e.getPlayer().kickPlayer(genPluginMsg("Admin mode is currently enabled."));
            e.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(adminOnly) {
            if(e.getPlayer().hasPermission("identifierx.adminonly.bypass")) return;
            else e.setQuitMessage(null);
        }
    }
}
