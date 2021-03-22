import org.apache.logging.log4j.core.util.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightSet {
    
    TrafficLights plugin;
    
    private ItemFrame rf, yf, gf;
    private Location rl, yl, gl;
    private long rt, yt, gt, totalTime;
    private List<LightSet> syncC = new ArrayList<>();
    private List<LightSet> oppC = new ArrayList<>();
    LightSet parent;
    boolean depend, sync, oppCycle;
    int id;
    BukkitTask rTask, yTask, gTask, rOppTask, yOppTask, gOppTask;
    Runnable rTimer, yTimer, gTimer, rOppTimer, yOppTimer, gOppTimer;
    
    LightSet(Location redFrame, Location yellowFrame, Location greenFrame, int redTime, int yellowTime, int greenTime,
             int id, boolean dep, boolean syn, TrafficLights p){
        plugin = p;
        spawnFrame(redFrame, 0, true);
        spawnFrame(yellowFrame, 1, true);
        spawnFrame(greenFrame, 2, true);
        this.id = id;
        depend = dep;
        sync = syn;
        rOppTimer = () -> oppC.forEach(k -> k.setLightToColour(0));
        yOppTimer = () -> oppC.forEach(k -> k.setLightToColour(1));
        gOppTimer = () -> oppC.forEach(k -> k.setLightToColour(2));
        rTimer = () -> {setLightToColour(0); if(oppCycle && gOppTask == null){startOppCycles(); return;}
        if(oppCycle && !Bukkit.getScheduler().isQueued(gOppTask.getTaskId())) startOppCycles(); };
        yTimer = () -> setLightToColour(1);
        gTimer = () -> setLightToColour(2);
        new BukkitRunnable(){
            public void run(){
                setup(redTime, yellowTime, greenTime);
                if(!dep)
                    startCycles();
            }
        }.runTaskLater(p, 10);
    
    }
    
    Map<String, Object> saveToMap(){
        Map<String, Object> m = new HashMap<>();
        m.put("rf", rl); m.put("yf", yl); m.put("gf", gl);
        clearLights();
        if(parent != null)
            m.put("parent", parent.id);
        m.put("rt", rt); m.put("yt", yt); m.put("gt", gt);
//        if(parent != null)
        m.put("sync", sync);
        return m;
    }
    
    void spawnFrame(Location loc, int col, boolean load){
        if(loc.getWorld().getNearbyEntities(loc, 15, 10, 15).stream().noneMatch(e -> e.getType().equals(EntityType.PLAYER))
                && !load) return;
        try{
            if(load){
                if(col == 0){
                    rl = loc;
                    rf = loc.getWorld().spawn(rl.getBlock().getLocation(), ItemFrame.class);
                }
                else if(col == 1){
                    yl = loc;
                    yf = loc.getWorld().spawn(yl.getBlock().getLocation(), ItemFrame.class);
                }
                else{
                    gl = loc;
                    gf = loc.getWorld().spawn(gl.getBlock().getLocation(), ItemFrame.class);
                }
            } else{
                if(col == 0){
                    rf = loc.getWorld().spawn(rl.getBlock().getLocation(), ItemFrame.class);
                    rf.setItem(plugin.makeItem(null, Material.WOOL, 14));
                    rf.setCustomName("TL" + id);
                }else if(col == 1){
                    yf = loc.getWorld().spawn(yl.getBlock().getLocation(), ItemFrame.class);
                    yf.setItem(plugin.makeItem(null, Material.WOOL, 4));
                    yf.setCustomName("TL" + id);
                }else{
                    gf = loc.getWorld().spawn(gl.getBlock().getLocation(), ItemFrame.class);
                    gf.setItem(plugin.makeItem(null, Material.WOOL, 5));
                    gf.setCustomName("TL" + id);
                }
            }
        } catch (Exception e){
            new BukkitRunnable(){public void run(){
            ArmorStand a = loc.getWorld().spawn(loc, ArmorStand.class);
            new BukkitRunnable(){ public void run(){
            a.getNearbyEntities(0.1, 0.1, 0.1).stream().filter(k -> k instanceof ItemFrame).forEach(Entity::remove);
            a.remove();
            new BukkitRunnable(){
                public void run() {
                    if(col == 0){
                        rf = loc.getWorld().spawn(rl.getBlock().getLocation(), ItemFrame.class);
                        rf.setItem(plugin.makeItem(null, Material.WOOL, 14));
                        rf.setCustomName("TL" + id);
                    }
                    else if(col == 1){
                        yf = loc.getWorld().spawn(yl.getBlock().getLocation(), ItemFrame.class);
                        rf.setItem(plugin.makeItem(null, Material.WOOL, 4));
                        rf.setCustomName("TL" + id);
                    }
                    else{
                        gf = loc.getWorld().spawn(gl.getBlock().getLocation(), ItemFrame.class);
                        rf.setItem(plugin.makeItem(null, Material.WOOL, 5));
                        rf.setCustomName("TL" + id);
                    }
                }
            }.runTaskLater(plugin, 3);}}.runTaskLater(plugin, 3);}}.runTaskLater(plugin, 3);
        }
    }
    
    void deleteLight(){
        plugin.lights.remove(id);
        new ArrayList<>(syncC).forEach(this::removeChild);
        new ArrayList<>(oppC).forEach(this::removeChild);
        cancelCycles();
        clearLights();
        cancelOppCycles();
    }
    
    void retimeLights(int redTime, int yellowTime, int greenTime){
        cancelCycles();
        rt = redTime; yt = yellowTime; gt = greenTime;
        totalTime = rt + yt + gt;
        oppCycle = true;
        startCycles();
    }
    
    private void setup(int redTime, int yellowTime, int greenTime){
        rt = redTime; yt = yellowTime; gt = greenTime;
        totalTime = rt + yt + gt;
    }
    
    void addChild(LightSet l, boolean sync){ (sync ? syncC : oppC).add(l); l.cancelCycles(); l.depend = true; if(l.parent != null)
        l.parent.removeChild(l); l.parent = this; l.sync = sync; if(!sync && oppC.size() == 1) oppCycle = true;}
    
    void removeChild(LightSet l){ syncC.remove(l); oppC.remove(l); l.depend = false; l.startCycles(); l.parent = null;
        if(oppC.size() == 0) cancelOppCycles();}
    
    void cancelCycles(){
        if(rTask != null){
            Bukkit.getScheduler().cancelTask(rTask.getTaskId());
            Bukkit.getScheduler().cancelTask(yTask.getTaskId());
            Bukkit.getScheduler().cancelTask(gTask.getTaskId());
        }
        cancelOppCycles();
    }
    
    void startOppCycles(){
        gOppTask = Bukkit.getScheduler().runTaskTimer(plugin, gOppTimer, 0, totalTime);
        yOppTask = Bukkit.getScheduler().runTaskTimer(plugin, yOppTimer, rt - yt, totalTime);
        rOppTask = Bukkit.getScheduler().runTaskTimer(plugin, rOppTimer, rt, totalTime);
    }
    
    void cancelOppCycles(){
        oppCycle = false;
        if(rOppTask != null){
            Bukkit.getScheduler().cancelTask(rOppTask.getTaskId());
            Bukkit.getScheduler().cancelTask(yOppTask.getTaskId());
            Bukkit.getScheduler().cancelTask(gOppTask.getTaskId());
        }
    }
    
    void startCycles(){
        rTask = Bukkit.getScheduler().runTaskTimer(plugin, rTimer, 0, totalTime);
        gTask = Bukkit.getScheduler().runTaskTimer(plugin, gTimer, rt, totalTime);
        yTask = Bukkit.getScheduler().runTaskTimer(plugin, yTimer, rt + gt, totalTime);
    }
    
    void setLightToColour(int i){
        clearLights();
        if(i == 0)
            spawnFrame(rl, 0, false);
        else if(i == 1)
            spawnFrame(yl, 1, false);
        else
            spawnFrame(gl, 2, false);
        syncC.forEach(c -> c.setLightToColour(i));
    }
    
    private void clearLights(){
        rf.remove();
        yf.remove();
        gf.remove();
    }

}
