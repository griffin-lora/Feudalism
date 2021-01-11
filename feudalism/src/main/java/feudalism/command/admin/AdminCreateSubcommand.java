package feudalism.command.admin;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import feudalism.FeudalismException;
import feudalism.Util;
import feudalism.command.SubcommandBase;
import feudalism.object.GridCoord;
import feudalism.object.Realm;
import feudalism.object.User;

public class AdminCreateSubcommand extends SubcommandBase {
    @Override
    protected String[] getAliases() {
        return new String[]{ "create", "new" };
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) throws FeudalismException {
        if (args.length < 2) {
            throw new FeudalismException("Too few arguments");
        }
        String name = args[0];
        String ownerName = args[1];
        User owner = User.get(Util.getPlayerUuidByName(ownerName));
        GridCoord coord;
        if (args.length == 4) {
            int x = Integer.valueOf(args[2]);
            int z = Integer.valueOf(args[3]);
            coord = GridCoord.getFromGridPosition(x, z);
        } else {
            coord = GridCoord.getFromGridPosition(0, 0);
        }
        Realm realm = new Realm(owner, name, coord);
        System.out.println(realm);
    }
}
