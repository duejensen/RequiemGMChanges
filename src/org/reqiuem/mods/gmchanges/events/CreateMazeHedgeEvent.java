// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.events;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.zones.NoSuchZoneException;
import org.reqiuem.mods.gmchanges.maze.Maze;

import java.io.IOException;

public class CreateMazeHedgeEvent extends EventOnce
{
    private int tileX;
    private int tileY;
    private Tiles.TileBorderDirection border;
    private Maze maze;
    private boolean animateGrowth;
    
    public CreateMazeHedgeEvent(final int fromNow, final Maze maze, final Tiles.TileBorderDirection border, final int tileX, final int tileY, final boolean animateGrowth) {
        super(fromNow, Unit.MILLISECONDS);
        this.tileX = 0;
        this.tileY = 0;
        this.border = null;
        this.maze = null;
        this.animateGrowth = false;
        this.maze = maze;
        this.tileX = tileX;
        this.tileY = tileY;
        this.border = border;
        this.animateGrowth = animateGrowth;
    }
    
    @Override
    public boolean invoke() {
        try {
            this.maze.createHedge(this.border, this.tileX, this.tileY, this.animateGrowth);
        }
        catch (NoSuchZoneException | IOException ex2) {
            final Exception e = ex2;
            e.printStackTrace();
        }
        return true;
    }
}
