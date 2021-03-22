import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TrafficLights extends JavaPlugin {
    
    Permission perms;
    
    ItemStack createLightItem, deleteLightItem, retimeLightItem, syncLightItem, setOppositeLightItem;
    String createLightMsg, deleteLightMsg, retimeLightMsg, syncLightMsg, oppositeLightMsg, cancelledMessage, retimedMsg,
            createdLightMsg, deletedLightMsg, redTimeMsg, yellowTimeMsg, greenTimeMsg, syncedLightMsg, opposisedLightMsg;
    Inventory switchToolMenu;
    FileConfiguration config;
    
    Map<Integer, LightSet> lights = new HashMap<>();
    
    @Override
    public void onEnable(){
        if(!setupPermissions()){
            getServer().getConsoleSender().sendMessage(cString("&cUnable to connect to permissions! Shutting Down Server..."));
            Bukkit.shutdown();
        }
        
        saveDefaultConfig();
        config = getConfig();
        
        for(String k : config.getKeys(false)){
            ConfigurationSection c = config.getConfigurationSection(k);
            lights.put(Integer.parseInt(k), new LightSet(((Location)c.get("rf")).getBlock().getLocation(),
                    ((Location)c.get("yf")).getBlock().getLocation(), ((Location)c.get("gf")).getBlock().getLocation(),
                    c.getInt("rt"), c.getInt("yt"), c.getInt("gt"), Integer.parseInt(k), c.contains("parent"), c.getBoolean("sync")
                    , this));
        }
        new BukkitRunnable(){
            public void run(){
                for(String k : config.getKeys(false)){
                    if(config.contains(k + ".parent")){
                        LightSet l = lights.get(Integer.parseInt(k));
                        lights.get(config.getInt(k + ".parent")).addChild(l, config.getBoolean(k + ".sync"));
                    }
                }
            }
        }.runTaskLater(this, 3);
        
        
        createLightItem = makeItem("&a&lTL - Create A Light", Material.INK_SACK, 10);
        deleteLightItem = makeItem("&c&lTL - Delete A Light", Material.INK_SACK, 1);
        retimeLightItem = makeItem("&7&lTL - Retime A Light", Material.INK_SACK, 8);
        syncLightItem = makeItem("&f&lTL - Synchronise Two Lights", Material.INK_SACK, 15);
        setOppositeLightItem = makeItem("&8&lTL - Set Two Lights Opposite", Material.INK_SACK, 0);
        
        switchToolMenu = Bukkit.createInventory(null, 45, "Select A Tool");
        switchToolMenu.setItem(4, syncLightItem.clone()); setItemLore(syncLightItem);
        switchToolMenu.setItem(20, setOppositeLightItem.clone()); setItemLore(setOppositeLightItem);
        switchToolMenu.setItem(22, createLightItem.clone()); setItemLore(createLightItem);
        switchToolMenu.setItem(24, retimeLightItem.clone()); setItemLore(retimeLightItem);
        switchToolMenu.setItem(40, deleteLightItem.clone()); setItemLore(deleteLightItem);
        
        createLightMsg = cString("&9Right click the center item frame to create a light");
        deleteLightMsg = cString("&9Right click the light to delete it");
        retimeLightMsg = cString("&9Right click the light to retime it");
        syncLightMsg = cString("&9Right click the light that the timings will be copied from, " +
                "and then right click the light that will be synchronised with the first light");
        oppositeLightMsg = cString("&9Right click the light that the timings will be opposite of, " +
                "and then click the light that will be coordinated with the first light");
        createdLightMsg = cString("&aLight created!");
        deletedLightMsg = cString("&cLight deleted!");
        cancelledMessage = cString("&cPrevious configuration cancelled!");
        redTimeMsg = cString("&9Enter the time that the light will spend on red");
        yellowTimeMsg = cString("&9Enter the time that the light will spend on yellow");
        greenTimeMsg = cString("&9Enter the time that the light will spend on green");
        retimedMsg = cString("&9Light retimed successfully!");
        syncedLightMsg = cString("&9Successfully synchronised lights!");
        opposisedLightMsg = cString("&9Successfully coordinated lights!");
        
        getCommand("trafficlights").setExecutor(new TrafficLightsCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerIntEntListener(this), this);
        getServer().getPluginManager().registerEvents(new InvClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerIntListener(this), this);
        getServer().getPluginManager().registerEvents(new HangingBreakListener(), this);
    }
    
    @Override
    public void onDisable(){
        Set<String> ks = config.getKeys(false);
        ks.forEach(k -> config.set(k, null));
        lights.keySet().forEach(k -> config.set(Integer.toString(k), lights.get(k).saveToMap()));
        saveConfig();
        getServer().getConsoleSender().sendMessage(cString("&aTrafficLights saved successfully!"));
    }
    
    void createNewLight(ItemFrame rf, ItemFrame yf, ItemFrame gf, int rt, int yt, int gt){
        int i = 0;
        while(lights.containsKey(i)){ i++; }
        lights.put(i, new LightSet(rf.getLocation(), yf.getLocation(), gf.getLocation(), rt, yt, gt, i, false, false, this));
    }
    
    ItemStack makeItem(String name, Material mat, int dura){
        ItemStack i = new ItemStack(mat);
        i.setDurability((short)dura);
        ItemMeta m = i.getItemMeta();
        if(name != null)
            m.setDisplayName(cString(name));
        i.setItemMeta(m);
        return i;
    }
    
    ItemStack setItemLore(ItemStack i){
        List<String> lore = new ArrayList<>();
        lore.add(cString("&7 - Shift right click to switch tools"));
        ItemMeta m = i.getItemMeta();
        m.setLore(lore);
        i.setItemMeta(m);
        return i;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    String cString(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
}
