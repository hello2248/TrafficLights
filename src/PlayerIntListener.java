import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerIntListener implements Listener {
    
    TrafficLights plugin;
    
    PlayerIntListener(TrafficLights p){
        plugin = p;
    }
    
    @EventHandler
    public void onPlayerInt(PlayerInteractEvent e){
        if(!e.getPlayer().isSneaking())
            return;
        if(e.getItem() == null)
            return;
        if(!e.getItem().hasItemMeta())
            return;
        if(!e.getItem().getItemMeta().getDisplayName().substring(4, 6).equals("TL"))
            return;
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            e.getPlayer().openInventory(plugin.switchToolMenu);
    }
    
    
    
}
