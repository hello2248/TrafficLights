import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TrafficLightsCommand implements CommandExecutor {
    
    TrafficLights plugin;
    
    TrafficLightsCommand(TrafficLights p){
        plugin = p;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(sender instanceof Player){
            if(plugin.perms.has((Player)sender, "trafficlights.configure")){
                ((Player) sender).getInventory().addItem(plugin.createLightItem);
                sender.sendMessage(plugin.createLightMsg);
            } else
                sender.sendMessage(plugin.cString("&cYou do not have permission!"));
        } else
            sender.sendMessage(plugin.cString("&cYou must be a player!"));
        
        return true;
    }
}
