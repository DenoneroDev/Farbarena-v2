package deno.farbgame;

import Hagbrain.API;
import Hagbrain.Entity.EntityNPC;
import Hagbrain.GameMaster.GameMaster;
import Hagbrain.GameMaster.GamePlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Farbmeister extends EntityNPC {

    public static Farbarena arena;

    public Farbmeister(FullChunk chunk, CompoundTag nbt) {

        super(chunk, nbt);

    }
    public synchronized static void hit(Player p) {

        GamePlayer gp = GameMaster.gamePlayerList.get(p.getName());

        if(gp != null && !gp.spiel().getName().equals(Main.GameType.getName())) {

            API.sendPlayerMessageAction(gp.getPlayer(), "&6Du bist schon im Spiel &e" + gp.spiel().getName() + " &6eingetragen.\n&aDu musst dich dort zunächst austragen!");
            return;

        }

        if(GameMaster.gamePlayerList.containsKey(p.getName())) {

            GameMaster.gamePlayerList.remove(p.getName());
            p.sendTip(TextFormat.colorize("&cDu wurdes aus der Warteliste von &4" + Main.GameType + " &centfernt!"));
            return;

        }

        Gamer GP = new Gamer(p, Main.GameType);

        GameMaster.gamePlayerList.put(p.getName(), GP);
        p.sendTip(TextFormat.colorize("&bDu wurdest in der Warteliste von &3" + Main.GameType + " &beingetragen!"));

        arena = Main.mechanics.getArena(GameMaster.getPlayerCount(Main.GameType, true));

        if(arena != null)
            arena.checkStart();

    }
    @Override
    public synchronized boolean attack(EntityDamageEvent e) {

        Entity entity = ((EntityDamageByEntityEvent) e).getDamager();

        if (!(entity instanceof Player))
            return false;

        hit((Player) ((EntityDamageByEntityEvent) e).getDamager());
        return true;

    }
    @Override
    public void initEntity() {

        if (!this.namedTag.contains("EntityName")) {

            this.namedTag.putString("EntityName", TextFormat.colorize("&4F&aa&2r&bb&5m&6e&9i&ds&4t&ae&2r"));
            this.namedTag.putBoolean("isBaby", false);

        }
        this.ItemInHand = Item.get(Block.WOOL, 2);
        this.skin = LoadSkin("Farbenmeister");

        super.initEntity();

    }
    public UUID getUniqueId() {

        if (this.uuid == null) {

            this.uuid = Utils.dataToUUID(String.valueOf(this.getId()).getBytes(StandardCharsets.UTF_8), this.skin.getSkinData().data, this.getNameTag().getBytes(StandardCharsets.UTF_8));

        }

        return this.uuid;
    }
}
