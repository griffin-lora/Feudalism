package feudalism.command;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import feudalism.Chat;
import feudalism.Config;
import feudalism.FeudalismException;
import feudalism.Registry;
import feudalism.Util;
import feudalism.object.Confirmation;
import feudalism.object.GridCoord;
import feudalism.object.Realm;
import feudalism.object.User;

public class SelectCommands {
    public static final CommandElement[] elements = new CommandElement[] {
        new Claim(),
        new Set(),
        new Abandon()
    };
    
    public static class Abandon implements CommandElement {

        @Override
        public String[] getAliases() {
            return new String[] { "abandon" };
        }
    
        @Override
        public int getRequiredArgs() {
            return 0;
        }
    
        @Override
        public CommandElement[] getSubelements() {
            return new CommandElement[0];
        }
    
        @Override
        public void onExecute(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            Chat.sendMessage(sender, "Are you sure you want to abandon this realm?");
            new Confirmation(sender, () -> {
                Realm realm = (Realm) data.get(0);
                realm.removeOwner();
                Chat.sendMessage(sender, String.format("Successfully abandoned realm %s", realm.getName()));
            });
        }
    
        @Override
        public List<String> onTabComplete(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            return new ArrayList<>();
        }
        
    }

    private static class Claim implements CommandElement {

        @Override
        public String[] getAliases() {
            return new String[] { "claim" };
        }
    
        @Override
        public int getRequiredArgs() {
            return 0;
        }
    
        @Override
        public CommandElement[] getSubelements() {
            return new CommandElement[0];
        }
    
        @Override
        public void onExecute(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            Realm realm = (Realm) data.get(0);
            Player player = (Player) sender;
            User user = User.get(player);
            GridCoord coord = GridCoord.getFromLocation(player.getLocation());
            if (coord.hasOwner()) {
                throw new FeudalismException("You can not claim owned land");
            }
            if (!realm.hasDirectBorder(coord)) {
                throw new FeudalismException("You can not claim land that you do not border");
            }
            float claimPrice = Config.getFloat("realm.claim_price");
            if (!user.hasMoney(claimPrice)) {
                throw new FeudalismException(String.format("You need at least %s in your account to claim", claimPrice));
            }
            user.removeMoney(claimPrice);
            realm.addClaim(coord);
            Chat.sendMessage(sender, String.format("Successfully claimed %s, %s for %s", coord.getGridX(), coord.getGridZ(), claimPrice));
            coord.clean();
        }
    
        @Override
        public List<String> onTabComplete(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            return new ArrayList<>();
        }
        
    }
    
    private static class Set implements CommandElement {

        @Override
        public String[] getAliases() {
            return new String[] { "set" };
        }
    
        @Override
        public int getRequiredArgs() {
            return 1;
        }
    
        @Override
        public CommandElement[] getSubelements() {
            return new CommandElement[] {
                new Owner()
            };
        }

        @Override
        public void onExecute(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            CommandElement element = getSubelementWithAlias(args[0]);
            element.execute(sender, alias, Util.trimArgs(args, 1), data);
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
            if (args.length == 1) {
                return getSubaliases();
            }
            CommandElement element = getSubelementWithAlias(args[0]);
            return element.getTabComplete(sender, alias, Util.trimArgs(args, 1), data);
        }

        private class Owner implements CommandElement {

            @Override
            public String[] getAliases() {
                return new String[] { "owner" };
            }
        
            @Override
            public int getRequiredArgs() {
                return 1;
            }
        
            @Override
            public CommandElement[] getSubelements() {
                return new CommandElement[0];
            }
        
            @Override
            public void onExecute(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
                Realm realm = (Realm) data.get(0);
                UUID uuid = Util.getPlayerUuidByName(args[0]);
                User user = User.get(uuid);
                Chat.sendMessage(sender, String.format("Are you sure you want to change ownership of %s to %s", realm.getName(), args[0]));
                new Confirmation((Player) sender, () -> {
                    realm.setOwner(user);
                    Chat.sendMessage(sender, String.format("Changed ownership of %s over to %s", realm.getName(), args[0]));
                });
            }
        
            @Override
            public List<String> onTabComplete(CommandSender sender, String alias, String[] args, List<Object> data) throws FeudalismException {
                World world = Registry.getInstance().getWorld();
                List<String> names = new ArrayList<>();
                for (Player player : world.getPlayers()) {
                    names.add(player.getDisplayName());
                }
                return names;
            }
            
        }
        
    }
}
