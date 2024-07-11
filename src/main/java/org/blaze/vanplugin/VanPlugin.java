package org.blaze.vanplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

import java.util.EnumSet;
import java.util.Set;

import java.util.*;

public final class VanPlugin extends JavaPlugin implements Listener  {

    private final Map<String, Location> homes = new HashMap<>();
    private final HashMap<UUID, Long> lastDamageTime = new HashMap<>();

    private static final Set<Material> LOG_TYPES = EnumSet.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM
            // Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.BAMBOO_BLOCK
    );

    private static final Set<Material> LEAF_TYPES = EnumSet.of(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE

    );

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("van-credits").setExecutor(new VanCmd());
        this.getCommand("van-features").setExecutor(new VanCmd());
        this.getCommand("home").setExecutor((new VanCmd()));
        this.getCommand("sethome").setExecutor((new VanCmd()));
        this.getCommand("luatopia").setExecutor((new VanCmd()));
        this.getCommand("dannylandia").setExecutor((new VanCmd()));

        getServer().getPluginManager().registerEvents(this, this);

        System.out.println("[]======[Enabling Van QoL plugin]=====[]");
        System.out.println("|  Hello there!!");
        System.out.println("|  Blaze276 says hello!!");
        System.out.println("|  Powershot300 says hi!!");
        System.out.println("|");
        System.out.println("| project github:");
        System.out.println("|  https://coming.soon/Blaze276/Van");
        System.out.println("|");
        System.out.println("| Contributors:");
        System.out.println("|  Blaze276: https://github.com/Blaze276");
        System.out.println("|  Powershot300: https://github.com/101Corp");
        System.out.println("|");
        System.out.println("|  THANKS FOR USING <3!");
        System.out.println("|");
        System.out.println("[]====================================[]");
    }
    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Iterator var2 = event.blockList().iterator();

        while(var2.hasNext()) {
            Block block = (Block)var2.next();
            if (block.getType() != Material.AIR) {
                FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getBlockData());
                fallingBlock.setVelocity(new Vector(0, 1, 0));
                block.setType(Material.AIR);
            }
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.WOLF) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.BEEF, 3));
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (LOG_TYPES.contains(block.getType())) {
            Set<Block> treeBlocks = new HashSet<>();
            markTreeBlocks(block, treeBlocks);
            breakTree(treeBlocks);
            decayLeavesAroundTree(treeBlocks);
        }
    }

    private void markTreeBlocks(Block block, Set<Block> treeBlocks) {
        if (LOG_TYPES.contains(block.getType()) && treeBlocks.add(block)) {
            for (BlockFace face : BlockFace.values()) {
                Block relative = block.getRelative(face);
                markTreeBlocks(relative, treeBlocks);
            }
        }
    }

    private void breakTree(Set<Block> treeBlocks) {
        for (Block logBlock : treeBlocks) {
            logBlock.breakNaturally();
        }
    }

    private void decayLeavesAroundTree(Set<Block> treeBlocks) {
        for (Block logBlock : treeBlocks) {
            for (BlockFace face : BlockFace.values()) {
                Block relative = logBlock.getRelative(face);
                if (LEAF_TYPES.contains(relative.getType())) {
                    decayLeaf(relative, treeBlocks);
                }
            }
        }
    }

    private void decayLeaf(Block leaf, Set<Block> treeBlocks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (LEAF_TYPES.contains(leaf.getType())) {
                    leaf.breakNaturally();
                    for (BlockFace face : BlockFace.values()) {
                        Block relative = leaf.getRelative(face);
                        if (LEAF_TYPES.contains(relative.getType()) && isAdjacentToLog(relative, treeBlocks)) {
                            decayLeaf(relative, treeBlocks);
                        }
                    }
                }
            }
        }.runTaskLater(this, 1L);  // 1 tick delay to simulate natural decay
    }

    private boolean isAdjacentToLog(Block block, Set<Block> treeBlocks) {
        for (BlockFace face : BlockFace.values()) {
            Block relative = block.getRelative(face);
            if (treeBlocks.contains(relative)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    private final HashMap<UUID, ArmorStand> sittingPlayers = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType().toString().endsWith("_STAIRS")) {
            Player player = event.getPlayer();
            BlockData blockData = event.getClickedBlock().getBlockData();

            if (blockData instanceof Stairs) {
                Stairs stairs = (Stairs) blockData;
                float yaw = 0;

                switch (stairs.getFacing()) {
                    case NORTH:
                        yaw = 180 + 180; // 180 degrees added
                        break;
                    case SOUTH:
                        yaw = 180; // 180 degrees added
                        break;
                    case WEST:
                        yaw = 90 + 180; // 180 degrees added
                        break;
                    case EAST:
                        yaw = 270 + 180; // 180 degrees added
                        break;
                    default:
                        break;
                }

                    // Adjust the height of the armor stand to match the sitting position on stairs
                    ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(
                            event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), // Adjust Y-coordinate as needed
                            EntityType.ARMOR_STAND
                    );
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setMarker(true);
                    armorStand.setSmall(true);
                    armorStand.setRotation(yaw, 0);

                    // Make the player sit on the ArmorStand
                    armorStand.addPassenger(player);
                    sittingPlayers.put(player.getUniqueId(), armorStand);
                }
            }
        }


    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (sittingPlayers.containsKey(player.getUniqueId())) {
                ArmorStand armorStand = sittingPlayers.get(player.getUniqueId());
                armorStand.remove();
                sittingPlayers.remove(player.getUniqueId());
            }
        }
    }

    public class VanCmd implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
            if (cmd.getName().equalsIgnoreCase("van-credits")) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[]==========[Van QoL plugin]==========[]");
                sender.sendMessage(ChatColor.AQUA + "|  Created by:" + ChatColor.DARK_PURPLE + " Blaze276");
                sender.sendMessage(ChatColor.AQUA + "|  With help from:" + ChatColor.GREEN + " Powershot300");
                sender.sendMessage(ChatColor.AQUA + "|");
                sender.sendMessage(ChatColor.AQUA + "|" + ChatColor.DARK_AQUA + ChatColor.BOLD + " Github:");
                sender.sendMessage(ChatColor.AQUA + "|  https://github.com/Blaze276/Van-Plugin");
                sender.sendMessage(ChatColor.AQUA + "|");
                sender.sendMessage(ChatColor.AQUA + "|" + ChatColor.BOLD + " Contributors:");
                sender.sendMessage(ChatColor.AQUA + "|  Blaze276: https://github.com/Blaze276");
                sender.sendMessage(ChatColor.AQUA + "|  Powershot300: https://github.com/101Corp");
                sender.sendMessage(ChatColor.AQUA + "|");
                sender.sendMessage(ChatColor.AQUA + "|" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "  THANKS FOR USING <3!");
                sender.sendMessage(ChatColor.AQUA + "|");
                sender.sendMessage(ChatColor.DARK_AQUA + "[]====================================[]");

            }
            if (cmd.getName().equalsIgnoreCase("van-features")) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[]=========[Van QoL Features]=========[]");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Better Explosions");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Edible Dogs (requested by Lunatomic)");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Fast Leaf Decay");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Instant tree mining");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Home and sethome teleports (Going home 2.0)");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  Sit on stairs");
                sender.sendMessage(ChatColor.DARK_AQUA + "|  More coming soon!!");
                sender.sendMessage(ChatColor.DARK_AQUA + "[]====================================[]");
            }
            if (cmd.getName().equalsIgnoreCase("home")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                        if (canTeleportHome(player)) {
                            teleportHome(player);
                            player.sendMessage("Teleported to home!");
                        } else {
                            player.sendMessage("You cannot use /home for 30 seconds after taking damage.");
                        }
                    return true;
                } else {
                    sender.sendMessage("my question is how are you running this?");
                }
            }
            if (cmd.getName().equalsIgnoreCase("sethome")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    setHome(player);
                    player.sendMessage("Home set!");
                    return true;
                } else {
                    sender.sendMessage("my question is how are you running this?");
                }
            }
            if (cmd.getName().equalsIgnoreCase("luatopia")) {
                if (canTeleportHome((Player) sender)) {
                    Player player = (Player) sender;
                    Location location = new Location(player.getWorld(), -1617, 64, -215);
                    player.teleport(location);
                    player.sendMessage("Teleported to LuaTopia!!");
                } else {
                    sender.sendMessage("You cannot use /luatopia for 30 seconds after taking damage.");
                }
                return true;
            }
            if (cmd.getName().equalsIgnoreCase("dannylandia")) {
                if (canTeleportHome((Player) sender)) {
                    Player player = (Player) sender;
                    Location location = new Location(player.getWorld(), -1413, 73, -9);
                    player.teleport(location);
                    player.sendMessage("Teleported to Dannylandia!!");
                } else {
                    sender.sendMessage("You cannot use /dannylandia for 30 seconds after taking damage.");
                }
                return true;
            }
            return false;
        }

        private void setHome(Player player) {
            homes.put(player.getUniqueId().toString(), player.getLocation());
            String homes = "plugins/VanPlugin/homes.txt";
            String data = "plugins/VanPlugin/data.txt";
            String homeToAppend = player.getUniqueId()+"\n";
            String dataToAppend = player.getLocation()+"\n";

            try {
                // Ensure the directory exists
                Files.createDirectories(Paths.get("plugins/VanPlugin"));

                // write to homes.txt
                FileWriter writer = new FileWriter(homes, true);
                writer.write(homeToAppend);
                writer.close();

                // write to data.txt
                FileWriter writeData = new FileWriter(data, true);
                writeData.write(dataToAppend);
                writeData.close();

            } catch (IOException e) {
                getLogger().severe("Could not write to file: " + e.getMessage());
            }
        }
    }

        private boolean hasHome(Player player) {
            return homes.containsKey(player.getUniqueId().toString());

        }

        private void teleportHome(Player player) {
                String homes = "plugins/VanPlugin/homes.txt";
                String data = "plugins/VanPlugin/data.txt";

                String searchText = player.getUniqueId().toString();
                int lineNumber = getLatestLineNumber(homes, searchText);
                if (lineNumber != -1) {
                    System.out.println("The most recent occurrence of \"" + searchText + "\" is at line: " + lineNumber);
                }
                String lineContent = getLine(data, lineNumber);
                if (lineContent != null) {
                    System.out.println("Content of line " + lineNumber + ": " + lineContent);
                } else {
                    System.out.println("Line " + lineNumber + " not found in the file.");
                }

                assert lineContent != null;
                assert !lineContent.equals("Line not found");
                Location location = parseLocation(lineContent);

                // Teleport a player to the parsed location
                // plName.sendMessage(location.toString());
                player.teleport(location);

        }

        private boolean canTeleportHome(Player player) {
            UUID playerUUID = player.getUniqueId();
            if (lastDamageTime.containsKey(playerUUID)) {
                long lastDamage = lastDamageTime.get(playerUUID);
                return (System.currentTimeMillis() - lastDamage) >= 30000;
            }
            return true;
        }
    public static Location parseLocation(String locationString) {
        // Example input: Location{world=CraftWorld{name=world},x=223.35677520485368,y=70.0,z=-113.07492264553957,pitch=27.308031,yaw=1.9686122}
        String[] parts = locationString.replace("Location{", "").replace("}", "").split(",");

        String worldName = parts[0].split("=")[2].replace("CraftWorld{name=", "").replace("}", "").trim();
        double x = Double.parseDouble(parts[1].split("=")[1]);
        double y = Double.parseDouble(parts[2].split("=")[1]);
        double z = Double.parseDouble(parts[3].split("=")[1]);
        float pitch = Float.parseFloat(parts[4].split("=")[1]);
        float yaw = Float.parseFloat(parts[5].split("=")[1]);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldName);
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
    public static int getLatestLineNumber(String filePath, String searchText) {
        int latestLineNumber = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains(searchText)) {
                    latestLineNumber = lineNumber;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latestLineNumber;
    }
    public static String getLine(String filePath, int lineNumber) {
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            for (int i = 0; i < lineNumber; i++) {
                line = reader.readLine();
                if (line == null) {
                    return "Line not found";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }




    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("[]=====[Disabling Van QoL plugin]=====[]");
        System.out.println("|  Goodbye!!");
        System.out.println("|  Blaze276 was here!!");
        System.out.println("|  Powershot300 was here too!!");
        System.out.println("|");
        System.out.println("| project github:");
        System.out.println("|  https://github.com/Blaze276/Van-Plugin");
        System.out.println("|");
        System.out.println("| Contributors:");
        System.out.println("|  Blaze276: https://github.com/Blaze276");
        System.out.println("|  Powershot300: https://github.com/101Corp");
        System.out.println("|");
        System.out.println("|  THANKS FOR USING <3!");
        System.out.println("|");
        System.out.println("[]====================================[]");
    }
}
