import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreakListener implements Listener {

    @EventHandler
    public void onHangingBreak(HangingBreakEvent e){
        if(e.getEntity().getCustomName() == null)
            return;
        if(!e.getEntity().getCustomName().substring(0, 2).equals("TL"))
            return;
        if(!e.getCause().equals(HangingBreakEvent.RemoveCause.DEFAULT) || !e.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS))
            e.setCancelled(true);
    }

}
