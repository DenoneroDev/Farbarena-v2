package deno.farbgame;

import Hagbrain.GameMaster.Arena;
import Hagbrain.GameMaster.Game;
import Hagbrain.GameMaster.GameMaster;
import Hagbrain.GameMaster.GamePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Farbarena extends Arena {

    private TaskHandler CheckStartTask;
    private TaskHandler RoundTask;
    public Location WatcherSpawn;
    private final List<String> watchers = new ArrayList<>();
    private final String[] GameOverMessages = {"{player} hat verloren D:", "{player} ist runtergefallen D:", "{player} ist ausgerutscht D:", "{player} hat es versucht D:", "{player} muss mehr üben D:", "{player} muss mehr skill besitzen D:"};
    private final String[] WinnerMessages = {"{player} hat gewonnen :D", "{player} ist der Gewinner :D", "{player} ist Episch :D", "{player} hat Skill :D", "{player} hat alles geputzt :D", "{player} ist einfach Legendär :D"};
    public HashMap<String, Location> CustomFloorMap = new HashMap<>();
    public HashMap<String, Location> FloorMap = new HashMap<>();
    private final List<Integer> PlacedBlocks = new ArrayList<>();
    private int time = 6;
    private boolean FirstRound = true;
    private int round = 0;
    String mode = Main.config.getString("game.mode");
    int minGamerCount = 2;
    HashMap<String, HashMap<String, Location>> BoardMap = new HashMap<>();
    Block DefaultBoardBlock;
    HashMap<String, Location> PodestMap = new HashMap<>();

    public Farbarena(ConfigSection Daten, String name, Game spiel) {

        super(Daten, name, spiel);

        if(mode.equalsIgnoreCase("developer"))
            minGamerCount = 1;

    }
    public void checkStart() {

        if(GameMaster.getPlayerCount(Main.GameType, true) >= getMinSpieler()) {

            if(CheckStartTask == null || CheckStartTask.isCancelled()) {

                CheckStartTask = Main.plugin.getServer().getScheduler().scheduleDelayedTask(Main.plugin, ()-> {

                    if(Main.mechanics.getArena(GameMaster.getPlayerCount(Main.GameType, true)) == this) {

                        if(GameMaster.getPlayerCount(Main.GameType, true) >= getMinSpieler()) {

                            for(GamePlayer gp : GameMaster.gamePlayerList.values()) {

                                if(gp.spiel() == Main.GameType && gp.wartend()) {

                                    AddPlayerToArena(gp.getPlayer().getName());
                                    gp.getPlayer().getFoodData().setLevel(20);
                                    gp.getPlayer().setFoodEnabled(false);

                                }

                            }
                            setGestartet(true);
                            StartGame();
                            return;

                        }
                        for(GamePlayer gp : GameMaster.gamePlayerList.values()) {

                            if(gp.spiel() == Main.GameType && gp.wartend())
                                gp.getPlayer().sendTip(TextFormat.colorize("&4Das Spiel &c" + Main.GameType + " &4konnte nicht gestartet werden, nur &c" + GameMaster.getPlayerCount(Main.GameType, true) + " &4Spieler"));

                        }

                    }

                }, 10 * (int) Main.plugin.getServer().getTicksPerSecond());

                for(GamePlayer gp : GameMaster.gamePlayerList.values()) {

                    if(gp.spiel() == Main.GameType && gp.wartend())
                        gp.getPlayer().sendTip(TextFormat.colorize("&6Das Spiel &e" + Main.GameType + " &6beginnt in wenigen Sekunden!"));

                }

            }

        }

    }
    public synchronized void GameOver(Gamer gp) {

        gp.getPlayer().setFoodEnabled(true);
        GameMaster.gamePlayerList.remove(gp.getPlayer().getName());

        if(!gp.getPlayer().isOnline()) {

            this.DeletePlayerFromArena(gp.getPlayer().getName());
            this.sendMessage("&4<leave>&r " + gp.getNameTag() + " &4hat das Spiel verlassen!", false);

            if(getGamerCount() <= minGamerCount)
                end();
            return;

        }

        int place = getGamerCount();

        watchers.add(gp.getPlayer().getName());
        gp.watcher = true;
        gp.getPlayer().teleport(WatcherSpawn);
        gp.getPlayer().getInventory().clearAll();


        this.sendMessage("&6#" + place + "&r " + GameOverMessages[Main.mechanics.random( GameOverMessages.length - 1)].replace("{player}", gp.getNameTag() + "&4"), false);

        if(getGamerCount() <= minGamerCount)
            end();

    }
    public void pastePodest() {

        Location FloorMin = FloorMap.get("von");
        Location FloorMax = FloorMap.get("bis");
        Location PodestMin = PodestMap.get("von");
        Location PodestMax = PodestMap.get("bis");

        Location FloorMinStart = new Location(((FloorMin.x + FloorMax.x)/2) - (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMin.y + 1, (FloorMin.z + FloorMax.z)/2 - (Math.abs((PodestMin.z - PodestMax.z)) / 2));
        Location FloorMaxStart = new Location(((FloorMin.x + FloorMax.x)/2) + (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMinStart.y + (PodestMax.y - PodestMin.y), (FloorMin.z + FloorMax.z)/2 + (Math.abs((PodestMin.z - PodestMax.z)) / 2));
        SimpleAxisAlignedBB from = new SimpleAxisAlignedBB(PodestMin, PodestMax);
        SimpleAxisAlignedBB to = new SimpleAxisAlignedBB(FloorMinStart, FloorMaxStart);

        Main.mechanics.CopyPaste(getLevel(), from, to);

        checkRanks();

        }
    public void checkRanks() {

        Location FloorMin = FloorMap.get("von");
        Location FloorMax = FloorMap.get("bis");
        Location PodestMin = PodestMap.get("von");
        Location PodestMax = PodestMap.get("bis");
        Location FloorMinStart = new Location(((FloorMin.x + FloorMax.x)/2) - (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMin.y + 1, (FloorMin.z + FloorMax.z)/2 - (Math.abs((PodestMin.z - PodestMax.z)) / 2));
        Location FloorMaxStart = new Location(((FloorMin.x + FloorMax.x)/2) + (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMinStart.y + (PodestMax.y - PodestMin.y), (FloorMin.z + FloorMax.z)/2 + (Math.abs((PodestMin.z - PodestMax.z)) / 2));

        int x;
        int y;
        int z;

        for(y = (int) FloorMinStart.y; y <= FloorMaxStart.y; y++) {

            for(x = (int) FloorMinStart.x; x <= FloorMaxStart.x; x++) {

                for( z = (int) FloorMinStart.z; z <= FloorMaxStart.z; z++) {

                    Location loc = new Location(x, y, z);

                    if(getLevel().getBlock(loc).getId() == Block.WOODEN_PRESSURE_PLATE) {

                        getLevel().setBlock(loc, Block.get(Block.AIR));
                        PodestMap.put("third", loc);

                    }
                    if(getLevel().getBlock(loc).getId() == Block.HEAVY_WEIGHTED_PRESSURE_PLATE) {

                        getLevel().setBlock(loc, Block.get(Block.AIR));
                        PodestMap.put("second", loc);

                    }
                    if(getLevel().getBlock(loc).getId() == Block.LIGHT_WEIGHTED_PRESSURE_PLATE) {

                        getLevel().setBlock(loc, Block.get(Block.AIR));
                        PodestMap.put("first", loc);

                    }

                }
                z = (int) FloorMinStart.z;

            }
            x = (int) FloorMinStart.x;

        }
        teleportPodest();

    }
    public void removePodest() {

        Location FloorMin = FloorMap.get("von");
        Location FloorMax = FloorMap.get("bis");
        Location PodestMin = PodestMap.get("von");
        Location PodestMax = PodestMap.get("bis");
        Location FloorMinStart = new Location(((FloorMin.x + FloorMax.x)/2) - (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMin.y + 1, (FloorMin.z + FloorMax.z)/2 - (Math.abs((PodestMin.z - PodestMax.z)) / 2));
        Location FloorMaxStart = new Location(((FloorMin.x + FloorMax.x)/2) + (Math.abs((PodestMin.x - PodestMax.x)) / 2), FloorMinStart.y + (PodestMax.y - PodestMin.y), (FloorMin.z + FloorMax.z)/2 + (Math.abs((PodestMin.z - PodestMax.z)) / 2));

        int x;
        int y;
        int z;

        for(y = (int) FloorMinStart.y; y <= FloorMaxStart.y; y++) {

            for(x = (int) FloorMinStart.x; x <= FloorMaxStart.x; x++) {

                for( z = (int) FloorMinStart.z; z <= FloorMaxStart.z; z++) {

                    Location loc = new Location(x, y, z);
                    getLevel().setBlock(loc, Block.get(Block.AIR));

                }
                z = (int) FloorMinStart.z;

            }
            x = (int) FloorMinStart.x;

        }

    }
    public void teleportPodest() {

        Player winner = (mode.equalsIgnoreCase("developer")) ? Server.getInstance().getPlayer(watchers.get(0)) : getGamers().get(0);
        Player second = (mode.equalsIgnoreCase("developer")) ? null : Server.getInstance().getPlayer(watchers.get(watchers.size() - 1));
        Player third = (watchers.size() >= 2) ? Server.getInstance().getPlayer(watchers.get(watchers.size() - 2)) : null;

        winner.teleport(PodestMap.get("first").add(0.5, 0, 0.5));

        if(second != null)
            second.teleport(PodestMap.get("second").add(0.5, 0, 0.5));
        if(third != null)
            third.teleport(PodestMap.get("third").add(0.5, 0, 0.5));

    }
    public void end() {

        if(RoundTask != null)
            RoundTask.cancel();

        String WinnerName = (mode.equalsIgnoreCase("developer")) ? "DenoneroDev" : getGamers().get(0).getNameTag();

        sendMessage("&6#1&r " + WinnerMessages[Main.mechanics.random( WinnerMessages.length - 1)].replace("{player}", WinnerName + "&d"), false);
        getGamers().forEach(gamer -> gamer.getInventory().clearAll());
        watchers.forEach(WatcherName -> {

            Player watcher = Server.getInstance().getPlayer(WatcherName);
            watcher.getInventory().clearAll();

        });

        Main.plugin.getServer().getScheduler().scheduleAsyncTask(Main.plugin, new AsyncTask() {

            @Override
            public void onRun() {

                SimpleAxisAlignedBB CustomFloor = new SimpleAxisAlignedBB(CustomFloorMap.get("von"), CustomFloorMap.get("bis"));
                SimpleAxisAlignedBB floor = new SimpleAxisAlignedBB(FloorMap.get("von"), FloorMap.get("bis"));

                Main.mechanics.CopyPaste(getLevel(), CustomFloor, floor);
                pastePodest();
                setBoardColor(16);

                try {

                    Thread.sleep(500);

                } catch(Exception ignore) {}

                //TODO Podest teleportieren
                PlaySound(Sound.FIREWORK_BLAST, 1, false);

                try {

                    Thread.sleep(20000);

                } catch(Exception ignore) {}

                setGestartet(false);
                getGamers().forEach(gamer -> Farbmeister.hit(gamer.getPlayer()));
                watchers.forEach(WatcherName-> {

                    Player p = Server.getInstance().getPlayer(WatcherName);
                    if(p.isOnline())
                        Farbmeister.hit(p);

                });
                getGamers().forEach(gamer -> {

                    DeletePlayerFromArena(gamer.getName());

                    Gamer gp = new Gamer(gamer, Main.GameType);
                    gp.verlasseArena();


                });
                watchers.forEach(WatcherName-> {

                    DeletePlayerFromArena(WatcherName);

                    Gamer gp = new Gamer(Server.getInstance().getPlayer(WatcherName), Main.GameType);
                    gp.verlasseArena();


                });
                watchers.clear();
                getGamers().clear();

                removePodest();
                time = 6;
                FirstRound = true;
                round = 0;
                watchers.clear();

            }

        });

    }
    public void sendMessage(String msg, boolean onlyGamer) {

        sendMessage(MessageTypes.CHAT, msg, onlyGamer);

    }
    public void sendMessage(MessageTypes type, String msg, boolean onlyGamer) {

        for(String name : this.getSpieler()) {

            Player p = Server.getInstance().getPlayer(name);

            if(onlyGamer && this.isWatcher(p.getName()))
                return;
            if(type.getType() == MessageTypes.CHAT.getType())
                p.sendMessage(TextFormat.colorize(msg));
            if(type.getType() == MessageTypes.ACTION_BAR.getType() && getGamerCount() >= minGamerCount)
                p.sendTip(TextFormat.colorize(msg));
            if(type.getType() == MessageTypes.INVISIBLE_ACTION_BAR.getType() && getGamerCount() >= minGamerCount) {

                TextPacket pk = new TextPacket();

                pk.type = TextPacket.TYPE_JUKEBOX_POPUP;

                if(p.getGamemode() == 1) {

                    pk.message = TextFormat.colorize(msg + "\n\n ");

                } else {

                    pk.message = TextFormat.colorize(msg);

                }

                p.dataPacket(pk);

            }

        }

    }
    public boolean isWatcher(String name) {

        return watchers.contains(name);

    }
    public Level getLevel() {

        return this.getStartPosition().level;

    }
    public void MaskFloor() {

        if(getGamerCount() >= minGamerCount) {

            List<Integer> colors = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
            Location min = FloorMap.get("von");
            Location max = FloorMap.get("bis");

            CompletableFuture.runAsync(() -> {

                double minZ = Math.min(FloorMap.get("von").z, FloorMap.get("bis").z);
                int BlockCount = 0;
                int random = 0;
                int lastBlock = 0;
                int lastRow = 0;

                while(minZ <= max.z) {

                    double minX = Math.min(FloorMap.get("von").x, FloorMap.get("bis").x);

                    while(minX <= max.x) {

                        Location loc = new Location(minX, min.y, minZ);

                        if(BlockCount == 0) {

                            if(getLevel().getBlock(loc.add(-1)).getId() == Block.WOOL) {

                                if(!colors.contains(lastBlock))
                                    colors.add(lastBlock);
                                if(!colors.contains(lastRow))
                                    colors.add(lastRow);

                                lastBlock = getLevel().getBlock(loc.add(-1)).getDamage();
                                if(colors.contains(lastBlock))
                                    colors.remove(Integer.valueOf(lastBlock));

                            }
                            if(getLevel().getBlock(loc.add(0, 0, -1)).getId() == Block.WOOL) {

                                lastRow = getLevel().getBlock(loc.add(0, 0, -1)).getDamage();

                                if(colors.contains(lastRow))
                                    colors.remove(Integer.valueOf(lastRow));

                            }

                            random = Main.mechanics.random(colors.size());

                            PlacedBlocks.add(colors.get(random));


                        }

                        getLevel().setBlock(loc, Block.get(Block.WOOL, colors.get(random)));
                        getLevel().setBlock(loc.add(0, 0, 1), Block.get(Block.WOOL, colors.get(random)));
                        getLevel().setBlock(loc.add(0, 0, 2), Block.get(Block.WOOL, colors.get(random)));

                        BlockCount++;

                        if(BlockCount == 3)
                            BlockCount = 0;
                        minX++;

                    }
                    minZ = minZ + 3;
                }

                StartRound();

            });

        }

    }
    public void PlaySound(Sound s, int p, boolean onlyGamer) {

        for(String name : this.getSpieler()) {

            Player player = Server.getInstance().getPlayer(name);

            if(onlyGamer && this.isWatcher(player.getName()))
                return;
            getLevel().addSound(player, s, 1, p);

        }

    }
    public int getGamerCount() {

        int count = 0;
        for(String name : this.getSpieler()) {

            if(!watchers.contains(name))
                count++;

        }
        return count;

    }
    public List<Player> getGamers() {

        List<Player> gamers = new ArrayList<>();
        for(String name : this.getSpieler()) {

            if(!watchers.contains(name))
                gamers.add(Server.getInstance().getPlayer(name));

        }
        return gamers;

    }
    public void showColor(int color) {

        if(getGamerCount() >= minGamerCount) {

            setBoardColor(color);

            for(String name : this.getSpieler()) {

                if(!watchers.contains(name)) {

                    Player p = Server.getInstance().getPlayer(name);

                    for(int i = 0; i < 9; i++) {

                        p.getInventory().setItem(i, Item.get(Block.WOOL, color));

                    }

                }

            }

        }

    }
    public void setBoardColor(int c) {

        if(getGamerCount() >= minGamerCount || c == 16) {

            for(int i = 0; i < 2; i++) {

                String[] BoardTypes = {"first", "second"};
                Block b = (c == 16) ? DefaultBoardBlock : Block.get(Block.WOOL, c);

                Location min = BoardMap.get(BoardTypes[i] + "Board").get("von");
                Location max = BoardMap.get(BoardTypes[i] + "Board").get("bis");

                for(int y = (int) min.y; y <= max.y; y++) {

                    if(min.x == max.x) {

                        for(int z = (int) min.z; z <= max.z; z++) {

                            Location loc = new Location(min.x, y, z);
                            getLevel().setBlock(loc, b);

                        }

                    } else {

                        for(int x = (int) min.x; x <= max.x; x++) {

                            Location loc = new Location(x, y, min.z);
                            getLevel().setBlock(loc, b);

                        }

                    }

                }

            }

        }

    }
    public void clearColor() {

        for(String name : this.getSpieler()) {

            if(!watchers.contains(name)) {
                
                Player p = Server.getInstance().getPlayer(name);
                p.getInventory().clearAll();

            }

        }

    }
    public void pickColor(int c) {

        if(getGamerCount() >= minGamerCount) {

            Location min = FloorMap.get("von");
            Location max = FloorMap.get("bis");

            CompletableFuture.runAsync(() -> {

                double FirstX = min.x;
                double SecondX = max.x;

                while(FirstX <= SecondX) {

                    double FirstZ = min.z;
                    double SecondZ = max.z;

                    while(FirstZ <= SecondZ) {

                        Block b = getLevel().getBlock((int) FirstX, (int) min.y, (int) FirstZ);

                        if(b.getDamage() != c) {

                            getLevel().setBlock(b, Block.get(Block.AIR));
                            getLevel().setBlock(b.add(1), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(2), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(0, 0, 1), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(0, 0, 2), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(1, 0, 1), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(1, 0, 2), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(2, 0, 1), Block.get(Block.AIR));
                            getLevel().setBlock(b.add(2, 0, 2), Block.get(Block.AIR));

                        }
                        FirstZ = FirstZ + 3;

                    }
                    FirstX = FirstX + 3;

                }

            });

        }

    }
    @Override
    public void StartGame() {

        Main.plugin.getServer().getScheduler().scheduleAsyncTask(Main.plugin, new AsyncTask() {

            @Override
            public void onRun() {

                for(int i = 5; i > 0; i--) {

                    if(getGamerCount() < minGamerCount) {

                        i = 0;
                        return;

                    }

                    sendMessage(MessageTypes.ACTION_BAR, "&6Das Spiel beginnt in &e" + i + " &6Sekunden", true);

                    try {

                        Thread.sleep(1000);

                    } catch(Exception ignore) {}

                }

                if(getGamerCount() < minGamerCount)
                    return;

                MaskFloor();
                PlaySound(Sound.NOTE_BELL, 1, true);

                try {

                    Thread.sleep(500);

                } catch (Exception ignore) {}

            }

        });

    }
    public void StartRound() {

        RoundTask = Server.getInstance().getScheduler().scheduleAsyncTask(Main.plugin, new AsyncTask() {

            @Override
            public void onRun() {

                int color = PlacedBlocks.get(Main.mechanics.random(PlacedBlocks.size() - 1));
                String VisualTimer = "▇▆▅▄▃▂";

                for(int i = time; i > 0; i--) {

                    StringBuilder VT = new StringBuilder();

                    for(int j = i; j > 0; j--)
                        VT.append(VisualTimer.charAt(j - 1));



                    if(!FirstRound) {

                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignore) {}

                    }

                    if(round == 3 && time != 2) {

                        round = 0;
                        time--;

                    }

                    if(i == 6) PlaySound(Sound.NOTE_DIDGERIDOO, 3, true);
                    if(i == 5) PlaySound(Sound.NOTE_BASS, 3, true);
                    if(i == 4) PlaySound(Sound.NOTE_DIDGERIDOO, 2, true);
                    if(i == 3) PlaySound(Sound.NOTE_BASS, 2, true);
                    if(i == 2) PlaySound(Sound.NOTE_DIDGERIDOO, 1, true);
                    if(i == 1) PlaySound(Sound.NOTE_BASS, 1, true);

                    sendMessage(MessageTypes.INVISIBLE_ACTION_BAR,"&f" + getGamerCount() + "&7/&8" + 10, false);
                    showColor(color);
                    sendMessage(MessageTypes.ACTION_BAR, "&" + Main.mechanics.getColorCodeByBlockColor(color) + VT + " " + Main.mechanics.getColorNameByInt(color) + " " + VT.reverse(), false);

                    if(FirstRound) {

                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignore) {}

                    }

                }
                PlacedBlocks.clear();
                if(!FirstRound) {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignore) {}

                }
                pickColor(color);
                PlaySound(Sound.NOTE_SNARE, 1, true);
                clearColor();

                for(int i = 3; i > 0; i--) {

                    sendMessage(MessageTypes.INVISIBLE_ACTION_BAR,"&f" + getGamerCount() + "&7/&8" + 10, false);
                    sendMessage(MessageTypes.ACTION_BAR, "&l&4✘ Stop ✘", false);

                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignore) {}

                }
                for(int i = 2; i > 0; i--) {

                    sendMessage(MessageTypes.INVISIBLE_ACTION_BAR,"&f" + getGamerCount() + "&7/&8" + 10, false);
                    sendMessage(MessageTypes.ACTION_BAR, "&l&aWarte...", false);

                    try {

                        Thread.sleep(1000);

                    } catch (Exception ignore) {}

                    sendMessage(MessageTypes.ACTION_BAR, "&l&aWarte...", false);

                }

                if(getGamerCount() >= minGamerCount) {

                    MaskFloor();
                    round++;
                    FirstRound = false;
                    PlaySound(Sound.NOTE_BELL, 1, true);

                }

            }

        });

    }

    @Override
    public void Verlasse(GamePlayer gp) {

        GameOver((Gamer) gp);

    }

    @Override
    public boolean Died(GamePlayer gamePlayer) {
        return false;
    }

    @Override
    public boolean Bewege(GamePlayer gp) {

        if(!IsInArena(gp.getPlayer())) {

            GameOver((Gamer) gp);
            return true;

        }
        return true;

    }

    @Override
    public boolean Teleportiere(GamePlayer gamePlayer) {
        return false;
    }

    @Override
    public boolean Angriff(GamePlayer gamePlayer, Entity entity, double v) {
        return false;
    }

    @Override
    public boolean Verteidigen(Entity entity, GamePlayer gamePlayer, double v) {
        return false;
    }

    @Override
    public boolean Interact(GamePlayer gamePlayer, Item item, Block block) {
        return false;
    }

    @Override
    public boolean InteractEntity(GamePlayer gamePlayer, Item item, Entity entity) {
        return false;
    }

    @Override
    public boolean BreakBlock(Player player, Block block, Item item) {
        return false;
    }

    @Override
    public boolean DropItem(GamePlayer gamePlayer, Item item) {
        return false;
    }
}
