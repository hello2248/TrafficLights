import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InvClickListener implements Listener {
    
    TrafficLights plugin;
    
    InvClickListener(TrafficLights p){
        plugin = p;
    }
    
    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        if(!e.getInventory().equals(plugin.switchToolMenu))
            return;
        e.setCancelled(true);
        if(e.getCurrentItem() != null){
            Player p = (Player)e.getWhoClicked();
            p.getInventory().setItemInMainHand(plugin.setItemLore(e.getCurrentItem().clone()));
            switch(e.getCurrentItem().getDurability()){
                case 0: p.sendMessage(plugin.oppositeLightMsg); break;
                case 15: p.sendMessage(plugin.syncLightMsg); break;
                case 1: p.sendMessage(plugin.deleteLightMsg); break;
                case 8: p.sendMessage(plugin.retimeLightMsg); break;
                case 10: p.sendMessage(plugin.createLightMsg);
            }
            new BukkitRunnable(){
                public void run(){
                    p.closeInventory();
                }
            }.runTaskLater(plugin, 1);
        }
    }
    
}
