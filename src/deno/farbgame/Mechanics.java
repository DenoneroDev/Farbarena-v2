package deno.farbgame;

import Hagbrain.GameMaster.Arena;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mechanics {

    List<Farbarena> arenen = new ArrayList<>();

    public Mechanics() {

        for(Arena arena : Main.arenen) {

            arenen.add((Farbarena) arena);
            String[] watcher = Main.config.getString(Main.GameType + "." + arena.getName() + ".watcher").split(";");
            ((Farbarena) arena).WatcherSpawn = new Location(Integer.parseInt(watcher[0]), Integer.parseInt(watcher[1]), Integer.parseInt(watcher[2]), Integer.parseInt(watcher[3]));
            ConfigSection CustomFloor = Main.config.getSection(Main.GameType + "." + arena.getName() + ".customFloor");
            ((Farbarena) arena).CustomFloorMap.put("von", new Location(Integer.parseInt(CustomFloor.getString("von").split(";")[0]), Integer.parseInt(CustomFloor.getString("von").split(";")[1]), Integer.parseInt(CustomFloor.getString("von").split(";")[2])));
            ((Farbarena) arena).CustomFloorMap.put("bis", new Location(Integer.parseInt(CustomFloor.getString("bis").split(";")[0]), Integer.parseInt(CustomFloor.getString("bis").split(";")[1]), Integer.parseInt(CustomFloor.getString("bis").split(";")[2])));
            ConfigSection floor = Main.config.getSection(Main.GameType + "." + arena.getName() + ".floor");
            ((Farbarena) arena).FloorMap.put("von", new Location(Integer.parseInt(floor.getString("von").split(";")[0]), Integer.parseInt(floor.getString("von").split(";")[1]), Integer.parseInt(floor.getString("von").split(";")[2])));
            ((Farbarena) arena).FloorMap.put("bis", new Location(Integer.parseInt(floor.getString("bis").split(";")[0]), Integer.parseInt(floor.getString("bis").split(";")[1]), Integer.parseInt(floor.getString("bis").split(";")[2])));
            ConfigSection FirstBoard = Main.config.getSection(Main.GameType + "." + arena.getName() + ".firstBoard");
            HashMap<String, Location> FirstBoardMap = new HashMap<>();
            FirstBoardMap.put("von", new Location(Integer.parseInt(FirstBoard.getString("von").split(";")[0]), Integer.parseInt(FirstBoard.getString("von").split(";")[1]), Integer.parseInt(FirstBoard.getString("von").split(";")[2])));
            FirstBoardMap.put("bis", new Location(Integer.parseInt(FirstBoard.getString("bis").split(";")[0]), Integer.parseInt(FirstBoard.getString("bis").split(";")[1]), Integer.parseInt(FirstBoard.getString("bis").split(";")[2])));
            ((Farbarena) arena).BoardMap.put("firstBoard", FirstBoardMap);
            ConfigSection SecondBoard = Main.config.getSection(Main.GameType + "." + arena.getName() + ".secondBoard");
            HashMap<String, Location> SecondBoardMap = new HashMap<>();
            SecondBoardMap.put("von", new Location(Integer.parseInt(SecondBoard.getString("von").split(";")[0]), Integer.parseInt(SecondBoard.getString("von").split(";")[1]), Integer.parseInt(SecondBoard.getString("von").split(";")[2])));
            SecondBoardMap.put("bis", new Location(Integer.parseInt(SecondBoard.getString("bis").split(";")[0]), Integer.parseInt(SecondBoard.getString("bis").split(";")[1]), Integer.parseInt(SecondBoard.getString("bis").split(";")[2])));
            ((Farbarena) arena).BoardMap.put("secondBoard", SecondBoardMap);
            ((Farbarena) arena).DefaultBoardBlock = Block.get(Integer.parseInt(Main.config.getString(Main.GameType + "." + arena.getName() + ".defaultBlockID").split(":")[0]), Integer.parseInt(Main.config.getString(Main.GameType + "." + arena.getName() + ".defaultBlockID").split(":")[1]));
            ConfigSection podest = Main.config.getSection(Main.GameType + "." + arena.getName() + ".podest");
            ((Farbarena) arena).PodestMap.put("von", new Location(Integer.parseInt(podest.getString("von").split(";")[0]), Integer.parseInt(podest.getString("von").split(";")[1]), Integer.parseInt(podest.getString("von").split(";")[2])));
            ((Farbarena) arena).PodestMap.put("bis", new Location(Integer.parseInt(podest.getString("bis").split(";")[0]), Integer.parseInt(podest.getString("bis").split(";")[1]), Integer.parseInt(podest.getString("bis").split(";")[2])));
        }

    }
    public Farbarena getArena(int count) {

        Farbarena a = null;

        for(Farbarena arena : arenen) {

            if(count >= arena.getMinSpieler() && !arena.getGestartet()) {

                if(a != null && a.getMinSpieler() > arena.getMinSpieler())
                    continue;
                a = arena;

            }

        }
        return a;

    }
    public int random(int max) {

        return (int) (Math.random() * max);

    }
    public void CopyPaste(Level level, SimpleAxisAlignedBB from, SimpleAxisAlignedBB to) {

        Block[] blocks = this.getLevelBlocks(level, from);
        Block[] AreaBlocks  = this.getLevelBlocks(level, to);

        for (int i = 0; i < blocks.length; i++) {

            Block block = blocks[i];
            Block AreaBlock = AreaBlocks[i];

            level.setBlock(AreaBlock, Block.get(block.getId(), block.getDamage()));

        }

    }
    public Block[] getLevelBlocks(Level level, AxisAlignedBB bb) {

        int minX = NukkitMath.floorDouble(Math.min(bb.getMinX(), bb.getMaxX()));
        int minY = NukkitMath.floorDouble(Math.min(bb.getMinY(), bb.getMaxY()));
        int minZ = NukkitMath.floorDouble(Math.min(bb.getMinZ(), bb.getMaxZ()));
        int maxX = NukkitMath.floorDouble(Math.max(bb.getMinX(), bb.getMaxX()));
        int maxY = NukkitMath.floorDouble(Math.max(bb.getMinY(), bb.getMaxY()));
        int maxZ = NukkitMath.floorDouble(Math.max(bb.getMinZ(), bb.getMaxZ()));
        List<Block> blocks = new ArrayList<>();
        Vector3 vec = new Vector3();

        for(int z = minZ; z <= maxZ; z++) {

            for(int x = minX; x <= maxX; x++) {

                for(int y = minY; y <= maxY; y++)
                    blocks.add(level.getBlock(vec.setComponents(x, y, z), false));

            }

        }
        return blocks.toArray(new Block[0]);

    }

    public String getColorCodeByBlockColor(int i) {

        String color = "f,8,5,b,e,a,d,8,7,3,8,9,8,2,4,0";
        return color.split(",")[i];

    }
    public String getColorNameByInt(int i) {

        String color = "Weiß,Orange,Magenta,Hellblau,Gelb,Hellgrün,Rosa,Dunkelgrau,Hellgrau,Türkis,Lila,Blau,Braun,Dunkelgrün,Rot,Schwarz";
        return color.split(",")[i];

    }

}
