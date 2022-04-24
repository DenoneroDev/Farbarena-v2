package deno.farbgame;

import Hagbrain.GameMaster.Game;
import Hagbrain.GameMaster.GamePlayer;
import cn.nukkit.Player;

public class Gamer extends GamePlayer {

    public boolean watcher = false;

    public Gamer(Player p, Game gameType) {

        super(p, gameType);

    }

}
