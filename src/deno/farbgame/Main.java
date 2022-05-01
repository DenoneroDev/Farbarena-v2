package deno.farbgame;

import Hagbrain.GameMaster.Arena;
import Hagbrain.GameMaster.Game;
import Hagbrain.GameMaster.GameMaster;
import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

public class Main extends PluginBase {

    public static Main plugin;
    public static Config config;
    public static final Game GameType = Game.Farbarena;
    public static List<Arena> arenen;
    public static Mechanics mechanics;

    @Override
    public void onLoad() {

        this.saveDefaultConfig();
        plugin = this;


    }
    @Override
    public void onEnable() {

        config = plugin.getConfig();

        ConfigSection ConfigArenen = config.getSection(GameType.getName());
        arenen = GameMaster.Spielfelder.getOrDefault(GameType, new ArrayList<>());
        ConfigArenen.forEach((feldname, config) -> arenen.add(new Farbarena((ConfigSection) config, feldname, GameType)));
        mechanics = new Mechanics();

        Entity.registerEntity("Farbmeister", Farbmeister.class);
        Entity ent = Entity.createEntity("Farbmeister", plugin.getServer().getDefaultLevel().getSpawnLocation().getChunk(), Entity.getDefaultNBT(plugin.getServer().getDefaultLevel().getSpawnLocation()));
        ent.spawnToAll();

        this.getLogger().info(TextFormat.colorize("&d" + this.getDescription().getName() + " Version &4" +
                this.getDescription().getVersion() + " &ageladen \t &dCopyrights by &c" + String.join(", ", this.getDescription().getAuthors())));

    }
    public static void log(Object o) {

        plugin.getLogger().info(o.toString());

    }

}
