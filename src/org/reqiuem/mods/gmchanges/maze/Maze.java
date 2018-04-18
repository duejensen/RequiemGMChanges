// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.maze;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import org.reqiuem.mods.gmchanges.events.CreateMazeHedgeEvent;
import org.reqiuem.mods.gmchanges.events.GrowMazeHedgeEvent;
import org.reqiuem.mods.gmchanges.events.ShortEventDispatcher;
import org.reqiuem.mods.gmchanges.utils.StdRandom;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Maze
{
    private static Logger logger;
    private int mazeSize;
    private boolean[][] north;
    private boolean[][] east;
    private boolean[][] south;
    private boolean[][] west;
    private boolean[][] visited;
    private boolean done;
    private StructureConstantsEnum fenceType;
    private int offsetX;
    private int offsetY;
    private float fenceQl;
    
    static {
        Maze.logger = Logger.getLogger(Maze.class.getName());
    }
    
    public Maze(final int startX, final int startY, final int size, final StructureConstantsEnum fenceType) {
        this.done = false;
        this.offsetX = 0;
        this.offsetY = 0;
        this.fenceQl = 85.152f;
        this.mazeSize = size;
        this.offsetX = startX;
        this.offsetY = startY;
        this.fenceType = fenceType;
    }
    
    public void create(final boolean animateCreation, final boolean animateGrowth) {
        this.init();
        this.generate();
        this.draw(animateCreation, animateGrowth);
    }
    
    private void init() {
        this.visited = new boolean[this.mazeSize + 2][this.mazeSize + 2];
        for (int x = 0; x < this.mazeSize + 2; ++x) {
            this.visited[x][0] = true;
            this.visited[x][this.mazeSize + 1] = true;
        }
        for (int y = 0; y < this.mazeSize + 2; ++y) {
            this.visited[0][y] = true;
            this.visited[this.mazeSize + 1][y] = true;
        }
        this.north = new boolean[this.mazeSize + 2][this.mazeSize + 2];
        this.east = new boolean[this.mazeSize + 2][this.mazeSize + 2];
        this.south = new boolean[this.mazeSize + 2][this.mazeSize + 2];
        this.west = new boolean[this.mazeSize + 2][this.mazeSize + 2];
        for (int x = 0; x < this.mazeSize + 2; ++x) {
            for (int y2 = 0; y2 < this.mazeSize + 2; ++y2) {
                this.north[x][y2] = true;
                this.east[x][y2] = true;
                this.south[x][y2] = true;
                this.west[x][y2] = true;
            }
        }
    }
    
    private void generate(final int x, final int y) {
        this.visited[x][y] = true;
    Label_0219:
        while (!this.visited[x][y + 1] || !this.visited[x + 1][y] || !this.visited[x][y - 1] || !this.visited[x - 1][y]) {
            double r;
            do {
                r = StdRandom.uniform(4);
                if (r == 0.0 && !this.visited[x][y + 1]) {
                    this.north[x][y] = false;
                    this.south[x][y + 1] = false;
                    this.generate(x, y + 1);
                    continue Label_0219;
                }
                if (r == 1.0 && !this.visited[x + 1][y]) {
                    this.east[x][y] = false;
                    this.west[x + 1][y] = false;
                    this.generate(x + 1, y);
                    continue Label_0219;
                }
                if (r == 2.0 && !this.visited[x][y - 1]) {
                    this.south[x][y] = false;
                    this.north[x][y - 1] = false;
                    this.generate(x, y - 1);
                    continue Label_0219;
                }
            } while (r != 3.0 || this.visited[x - 1][y]);
            this.west[x][y] = false;
            this.east[x - 1][y] = false;
            this.generate(x - 1, y);
        }
    }
    
    private void generate() {
        this.generate(1, 1);
    }
    
    public void clear() {
        for (int x = 0; x < this.mazeSize; ++x) {
            for (int y = 0; y < this.mazeSize; ++y) {
                this.clearHedges(this.offsetX + x - this.mazeSize / 2, this.offsetY + y - this.mazeSize / 2);
            }
        }
    }
    
    private void clearHedges(final int x, final int y) {
        Fence f = MethodsStructure.getFenceAtTileBorderOrNull(x, y, Tiles.TileBorderDirection.DIR_HORIZ, 0, true);
        if (f != null && f.getQualityLevel() == this.fenceQl) {
            f.destroy();
        }
        f = MethodsStructure.getFenceAtTileBorderOrNull(x, y, Tiles.TileBorderDirection.DIR_DOWN, 0, true);
        if (f != null && f.getQualityLevel() == this.fenceQl) {
            f.destroy();
        }
    }
    
    private void draw(final boolean animateCreation, final boolean animateGrowth) {
        int delay = 0;
        try {
            for (int x = 1; x <= this.mazeSize; ++x) {
                for (int yIter = 1; yIter <= this.mazeSize; ++yIter) {
                    final int y = this.mazeSize - yIter;
                    delay = Math.max(Math.abs(x - this.mazeSize / 2) * 150, Math.abs(y - this.mazeSize / 2) * 150);
                    if (x != 1 && y == this.mazeSize - 1) {
                        if (animateCreation) {
                            this.drawDelayedHedge(Tiles.TileBorderDirection.DIR_HORIZ, x, y + 1, delay, animateGrowth);
                        }
                        else {
                            this.createHedge(Tiles.TileBorderDirection.DIR_HORIZ, x, y + 1, animateGrowth);
                        }
                    }
                    if (x == this.mazeSize) {
                        if (animateCreation) {
                            this.drawDelayedHedge(Tiles.TileBorderDirection.DIR_DOWN, x + 1, y, delay, animateGrowth);
                        }
                        else {
                            this.createHedge(Tiles.TileBorderDirection.DIR_DOWN, x + 1, y, animateGrowth);
                        }
                    }
                    if (this.north[x][yIter]) {
                        if (animateCreation) {
                            this.drawDelayedHedge(Tiles.TileBorderDirection.DIR_HORIZ, x, y, delay, animateGrowth);
                        }
                        else {
                            this.createHedge(Tiles.TileBorderDirection.DIR_HORIZ, x, y, animateGrowth);
                        }
                    }
                    if (this.west[x][yIter]) {
                        if (animateCreation) {
                            this.drawDelayedHedge(Tiles.TileBorderDirection.DIR_DOWN, x, y, delay, animateGrowth);
                        }
                        else {
                            this.createHedge(Tiles.TileBorderDirection.DIR_DOWN, x, y, animateGrowth);
                        }
                    }
                    if (Server.rand.nextInt(this.mazeSize) == 1) {
                        SoundPlayer.playSound("sound.forest.branchsnap", x, y, true, 0.0f);
                    }
                }
            }
        }
        catch (NoSuchZoneException | IOException ex2) {
            final Exception e = ex2;
            Maze.logger.log(Level.SEVERE, "Could not create maze", e);
        }
    }
    
    private void drawDelayedHedge(final Tiles.TileBorderDirection border, final int x, final int y, final int fromNow, final boolean animateGrowth) {
        ShortEventDispatcher.add(new CreateMazeHedgeEvent(fromNow, this, border, x, y, animateGrowth));
    }
    
    private void drawDelayedHedge(final Tiles.TileBorderDirection border, final int x, final int y, final int minDelay, final int maxDelay, final boolean animateGrowth) {
        ShortEventDispatcher.add(new CreateMazeHedgeEvent(minDelay + Server.rand.nextInt(maxDelay - minDelay), this, border, x, y, animateGrowth));
    }
    
    private boolean isFenceAllowed(final Tiles.TileBorderDirection border, final int x, final int y) {
        int diffx = 0;
        int diffy = 0;
        if (border == Tiles.TileBorderDirection.DIR_DOWN) {
            diffx = -1;
        }
        else {
            diffy = -1;
        }
        final int tile = Server.surfaceMesh.getTile(x, y);
        final byte type = Tiles.decodeType(tile);
        final int tile2 = Server.surfaceMesh.getTile(x + diffx, y + diffy);
        final byte type2 = Tiles.decodeType(tile2);
        return this.hasFence(border, x, y) && !Terraforming.isCornerUnderWater(x, y, true) && this.isTileGrowHedge(type) && this.isTileGrowHedge(type2) && !Zones.containsVillage(x, y, true) && !Zones.containsVillage(x + diffx, y + diffy, true);
    }
    
    private boolean isTileGrowHedge(final byte type) {
        return type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MOSS.id || Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_REED.id || type == Tiles.Tile.TILE_KELP.id || type == Tiles.Tile.TILE_LAWN.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id;
    }
    
    private boolean hasFence(final Tiles.TileBorderDirection tbDir, final int x, final int y) {
        final int startX = this.offsetX - this.mazeSize / 2;
        final int startY = this.offsetY - this.mazeSize / 2;
        final Fence f = MethodsStructure.getFenceAtTileBorderOrNull(startX + x, startY + y, tbDir, 0, true);
        return f != null;
    }
    
    public boolean createHedge(final Tiles.TileBorderDirection tbDir, final int x, final int y, final boolean animateGrowth) throws NoSuchZoneException, IOException {
        if (animateGrowth && this.fenceType != StructureConstantsEnum.HEDGE_FLOWER3_HIGH) {
            throw new RuntimeException("Can only animate growth on flower 3 (Camellia) hedges");
        }
        final int startX = this.offsetX - this.mazeSize / 2;
        final int startY = this.offsetY - this.mazeSize / 2;
        final int layer = 0;
        final Zone zone = Zones.getZone(startX + x, startY + y, true);
        final DbFence fence = new DbFence( animateGrowth ? StructureConstantsEnum.HEDGE_FLOWER3_LOW : this.fenceType, startX + x, startY + y, 0, 1.0f, tbDir, zone.getId(), layer);
       
        
        if (this.fenceType != StructureConstantsEnum.FENCE_MAGIC_STONE) {
            fence.setHasNoDecay(true);
        }
        fence.setState(fence.getFinishState());
        fence.setQualityLevel(this.fenceQl);
        fence.improveOrigQualityLevel(this.fenceQl);
        fence.save();
        zone.addFence(fence);
        if (animateGrowth) {
            ShortEventDispatcher.add(new GrowMazeHedgeEvent(150, fence, (byte)112));
            ShortEventDispatcher.add(new GrowMazeHedgeEvent(300, fence, (byte)113));
        }
        return true;
    }
}
