import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class PlayerIntEntListener implements Listener {

    TrafficLights plugin;
    Player input;
    boolean retime;
    int bId;
    int brt, byt, bgt;
    ItemFrame brf, byf, bgf;
    
    PlayerIntEntListener(TrafficLights p){
        plugin = p; bId = -1;
    }
    
    @EventHandler
    public void onPlayerEntInt(PlayerInteractEntityEvent e){
        if(!(e.getRightClicked() instanceof ItemFrame))
            return;
        if(e.getPlayer().getInventory().getItemInMainHand().equals(plugin.createLightItem)){
            e.setCancelled(true);
            ItemFrame mainF = (ItemFrame) e.getRightClicked();
            List<Entity> ents = mainF.getNearbyEntities(1, 1, 1);
            ents.removeIf(l -> !(l instanceof ItemFrame));
            ents.removeIf(l -> l.getLocation().subtract(mainF.getLocation()).toVector().length() > 1.1);
            Player p = e.getPlayer();
            if(ents.size() > 2){
                p.sendMessage(plugin.cString("&cToo many item frames nearby!"));
                return;
            } else if(ents.size() < 2){
                p.sendMessage(plugin.cString("&cNot enough item frames nearby!"));
                return;
            }
            if(!ents.get(0).getLocation().subtract(mainF.getLocation()).subtract(ents.get(0).getLocation()
                    .subtract(mainF.getLocation()).multiply(2)).equals(
                    ents.get(1).getLocation().subtract(mainF.getLocation()))){
                p.sendMessage(plugin.cString("&cItem frames do not line up!"));
                return;
            }
            if(ents.get(0).getCustomName() != null || ents.get(1).getCustomName() != null || mainF.getCustomName() != null){
                p.sendMessage(plugin.cString("&cAn item frame is already in use!"));
                return;
            }
            if(ents.get(0).getLocation().subtract(ents.get(1).getLocation()).getY() == 0){
                Vector v;
                switch(mainF.getAttachedFace()){
                    case EAST: v = new Vector(0, 0, 1); break;
                    case WEST: v = new Vector(0, 0, -1); break;
                    case NORTH: v = new Vector(1, 0, 0); break;
                    default: v = new Vector(-1, 0, 0);
                }
                if(mainF.getLocation().subtract(ents.get(0).getLocation()).toVector().equals(v)){
                    brf = (ItemFrame)ents.get(0);
                    bgf = (ItemFrame)ents.get(1);
                } else{
                    brf = (ItemFrame)ents.get(1);
                    bgf = (ItemFrame)ents.get(0);
                }
            } else{
                if(ents.get(0).getLocation().getY() > ents.get(1).getLocation().getY()){
                    brf = (ItemFrame) ents.get(0);
                    bgf = (ItemFrame) ents.get(1);
                } else{
                    brf = (ItemFrame) ents.get(1);
                    bgf = (ItemFrame) ents.get(0);
                }
            }
            byf = mainF;
            
            if(input != null){
                input.sendMessage(plugin.cancelledMessage);
            }
            if(!brf.isValid() || !byf.isValid() || !bgf.isValid()){
                e.getPlayer().sendMessage(plugin.cancelledMessage);
                return;
            }
            brt = -1; byt = -1; bgt = -1;
            input = p; retime = false; p.sendMessage(plugin.redTimeMsg);
            return;
        }
        if(e.getRightClicked().getCustomName() == null || !e.getRightClicked().getCustomName().substring(0, 2).equals("TL")){
            return;
        }
        e.setCancelled(true);
        ItemStack inHand = e.getPlayer().getInventory().getItemInMainHand();
        int id = Integer.parseInt(e.getRightClicked().getCustomName().substring(2));
        if(inHand.equals(plugin.deleteLightItem)){
            if(input != null){
                input.sendMessage(plugin.cancelledMessage);
            }
            plugin.lights.get(id).deleteLight();
            e.getPlayer().sendMessage(plugin.deletedLightMsg);
            return;
        }
        if(inHand.equals(plugin.retimeLightItem)){
            if(input != null){
                input.sendMessage(plugin.cancelledMessage);
            }
            brt = -1; byt = -1; bgt = -1;
            input = e.getPlayer(); retime = true; bId = id;
            input.sendMessage(plugin.redTimeMsg);
            return;
        }
        if(inHand.equals(plugin.syncLightItem)){
            if(bId == -1){
                bId = id;
                e.getPlayer().sendMessage(plugin.cString("&9Select the second light"));
            }else if(bId == id){
                bId = -1;
                e.getPlayer().sendMessage(plugin.cancelledMessage);
            }else {
                if(plugin.lights.get(bId).parent != null && plugin.lights.get(bId).parent.id == id){
                    e.getPlayer().sendMessage(plugin.cString("&cCannot associate with parent"));
                    bId = -1;
                    return;
                }
                if(plugin.lights.get(id).depend)
                    plugin.lights.get(id).parent.removeChild(plugin.lights.get(id));
                plugin.lights.get(bId).addChild(plugin.lights.get(id), true);
                e.getPlayer().sendMessage(plugin.syncedLightMsg);
                bId = -1;
            }
            return;
        }
        if(inHand.equals(plugin.setOppositeLightItem)){
            if(bId == -1){
                bId = id;
                e.getPlayer().sendMessage(plugin.cString("&9Select the second light"));
            }else if(bId == id){
                bId = -1;
                e.getPlayer().sendMessage(plugin.cancelledMessage);
            }else {
                if(plugin.lights.get(bId).parent != null && plugin.lights.get(bId).parent.id == id){
                    e.getPlayer().sendMessage(plugin.cString("&cCannot associate with parent"));
                    bId = -1;
                    return;
                }
                if(plugin.lights.get(id).depend)
                    plugin.lights.get(id).parent.removeChild(plugin.lights.get(id));
                plugin.lights.get(bId).addChild(plugin.lights.get(id), false);
                e.getPlayer().sendMessage(plugin.opposisedLightMsg);
                bId = -1;
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if(!e.getPlayer().equals(input))
            return;
        e.setCancelled(true);
        String m = e.getMessage();
        if(m.equalsIgnoreCase("cancel")){
            input.sendMessage(plugin.cancelledMessage); input = null; return;
        }
        try{
            Double.parseDouble(m);
        } catch(NumberFormatException c){
            input.sendMessage(plugin.cString("&cThat is not a valid number!"));
            return;
        }
        double t = Double.parseDouble(m);
        if(t <= 0.1) {input.sendMessage(plugin.cString("&cThat time is too short!")); return;}
        if(t > 60) {input.sendMessage(plugin.cString("&cThat time is too long!")); return;}
        if(brt == -1) {brt = (int)Math.round(t * 20); input.sendMessage(plugin.yellowTimeMsg); return;}
        if(byt == -1) {byt = (int)Math.round(t * 20); input.sendMessage(plugin.greenTimeMsg); return;}
        if(bgt == -1) {bgt = (int)Math.round(t * 20);
        input = null; if(!retime) {
            plugin.createNewLight(brf, byf, bgf, brt, byt, bgt);
                e.getPlayer().sendMessage(plugin.createdLightMsg);
            } else{
                plugin.lights.get(bId).retimeLights(brt, byt, bgt);
                e.getPlayer().sendMessage(plugin.retimedMsg);
            }
            bId = -1;
            return;
        }
    }
    
    @EventHandler
    public void onEntDmgEnt(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof ItemFrame))
            return;
        if(e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().substring(0, 2).equals("TL"))
            return;
        e.setCancelled(true);
    }
    
}
