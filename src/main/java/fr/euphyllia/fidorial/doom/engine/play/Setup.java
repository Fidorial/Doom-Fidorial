// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.BBox;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.MapThing;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Node;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Seg;
import fr.euphyllia.fidorial.doom.engine.Side;
import fr.euphyllia.fidorial.doom.engine.Subsector;
import fr.euphyllia.fidorial.doom.engine.Vertex;
import fr.euphyllia.fidorial.doom.engine.Wad;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;

import java.nio.ByteBuffer;

import static fr.euphyllia.fidorial.doom.engine.BBox.BOXBOTTOM;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXLEFT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXRIGHT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXTOP;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXRADIUS;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;

/**
 * Level loading ({@code p_setup.c}).
 */
public final class Setup {

    private static final int ML_THINGS = 1;
    private static final int ML_LINEDEFS = 2;
    private static final int ML_SIDEDEFS = 3;
    private static final int ML_VERTEXES = 4;
    private static final int ML_SEGS = 5;
    private static final int ML_SSECTORS = 6;
    private static final int ML_NODES = 7;
    private static final int ML_SECTORS = 8;
    private static final int ML_REJECT = 9;
    private static final int ML_BLOCKMAP = 10;

    private final Game g;

    public Setup(final Game g) {
        this.g = g;
    }

    /**
     * {@return the lump name of a map, such as {@code E1M1} or {@code MAP01}}
     *
     * @param commercial whether this is a Doom II style IWAD
     * @param episode    the episode
     * @param map        the map
     */
    public static String mapLumpName(final boolean commercial, final int episode, final int map) {
        if (commercial) {
            return map < 10 ? "MAP0" + map : "MAP" + map;
        }
        return "E" + episode + "M" + map;
    }

    private static ByteBuffer buf(final Wad wad, final int lump) {
        return wad.lumpBuffer(lump);
    }

    public void P_SetupLevel(final int episode, final int map, final int skill) {
        g.totalkills = g.totalitems = g.totalsecret = 0;
        g.wminfo.maxfrags = 0;
        g.wminfo.partime = 180;
        for (int i = 0; i < MAXPLAYERS; i++) {
            g.players[i].killcount = g.players[i].secretcount = g.players[i].itemcount = 0;
        }
        g.sound.S_Start();
        g.P_InitThinkers();
        final Wad wad = g.wad;
        final String lumpname = mapLumpName(g.gamemode == commercial, episode, map);
        final int lumpnum = wad.getNumForName(lumpname);
        g.leveltime = 0;
        final Level level = new Level();
        g.level = level;
        loadBlockMap(level, lumpnum + ML_BLOCKMAP);
        loadVertexes(level, lumpnum + ML_VERTEXES);
        loadSectors(level, lumpnum + ML_SECTORS);
        loadSideDefs(level, lumpnum + ML_SIDEDEFS);
        loadLineDefs(level, lumpnum + ML_LINEDEFS);
        loadSubsectors(level, lumpnum + ML_SSECTORS);
        loadNodes(level, lumpnum + ML_NODES);
        loadSegs(level, lumpnum + ML_SEGS);
        final int sectors = level.sectors.length;
        final int rejectSize = (sectors * sectors + 7) / 8;
        final byte[] reject = wad.readLump(lumpnum + ML_REJECT);
        level.rejectmatrix = reject.length >= rejectSize ? reject : java.util.Arrays.copyOf(reject, rejectSize);
        groupLines(level);
        g.bodyqueslot = 0;
        g.deathmatchCount = 0;
        loadThings(lumpnum + ML_THINGS);
        if (g.deathmatch != 0) {
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (g.playeringame[i]) {
                    g.players[i].mo = null;
                    g.G_DeathMatchSpawnPlayer(i);
                }
            }
        }
        g.mobjs.iquehead = g.mobjs.iquetail = 0;
        g.spec.P_SpawnSpecials();
    }

    private void loadVertexes(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 4;
        level.vertexes = new Vertex[n];
        for (int i = 0; i < n; i++) {
            level.vertexes[i] = new Vertex(b.getShort(i * 4) << FRACBITS, b.getShort(i * 4 + 2) << FRACBITS);
        }
    }

    private void loadSegs(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 12;
        level.segs = new Seg[n];
        for (int i = 0; i < n; i++) {
            final int o = i * 12;
            final Seg li = new Seg();
            li.v1 = level.vertexes[b.getShort(o) & 0xffff];
            li.v2 = level.vertexes[b.getShort(o + 2) & 0xffff];
            li.angle = b.getShort(o + 4) << 16;
            li.offset = b.getShort(o + 10) << 16;
            final int linedef = b.getShort(o + 6) & 0xffff;
            final Line ldef = level.lines[linedef];
            li.linedef = ldef;
            final int side = b.getShort(o + 8);
            final int sidenum = ldef.sidenum[side & 1];
            li.sidedef = level.sides[sidenum & 0xffff];
            li.frontsector = li.sidedef.sector;
            if ((ldef.flags & ML_TWOSIDED) != 0 && ldef.sidenum[(side & 1) ^ 1] != -1) {
                li.backsector = level.sides[ldef.sidenum[(side & 1) ^ 1] & 0xffff].sector;
            } else {
                li.backsector = null;
            }
            level.segs[i] = li;
        }
    }

    private void loadSubsectors(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 4;
        level.subsectors = new Subsector[n];
        for (int i = 0; i < n; i++) {
            final Subsector ss = new Subsector();
            ss.numlines = b.getShort(i * 4) & 0xffff;
            ss.firstline = b.getShort(i * 4 + 2) & 0xffff;
            level.subsectors[i] = ss;
        }
    }

    private void loadSectors(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final byte[] raw = g.wad.readLump(lump);
        final int n = raw.length / 26;
        final RenderData res = g.res;
        level.sectors = new Sector[n];
        for (int i = 0; i < n; i++) {
            final int o = i * 26;
            final Sector ss = new Sector(i);
            ss.floorheight = b.getShort(o) << FRACBITS;
            ss.ceilingheight = b.getShort(o + 2) << FRACBITS;
            ss.floorpic = (short) res.flatNumForName(Wad.readName(raw, o + 4));
            ss.ceilingpic = (short) res.flatNumForName(Wad.readName(raw, o + 12));
            ss.lightlevel = b.getShort(o + 20);
            ss.special = b.getShort(o + 22);
            ss.tag = b.getShort(o + 24);
            ss.thinglist = null;
            level.sectors[i] = ss;
        }
    }

    private void loadNodes(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 28;
        level.nodes = new Node[n];
        for (int i = 0; i < n; i++) {
            final int o = i * 28;
            final Node no = new Node();
            no.x = b.getShort(o) << FRACBITS;
            no.y = b.getShort(o + 2) << FRACBITS;
            no.dx = b.getShort(o + 4) << FRACBITS;
            no.dy = b.getShort(o + 6) << FRACBITS;
            for (int j = 0; j < 2; j++) {
                no.children[j] = b.getShort(o + 24 + j * 2) & 0xffff;
                for (int k = 0; k < 4; k++) {
                    no.bbox[j][k] = b.getShort(o + 8 + j * 8 + k * 2) << FRACBITS;
                }
            }
            level.nodes[i] = no;
        }
    }

    private void loadThings(final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 10;
        for (int i = 0; i < n; i++) {
            final int o = i * 10;
            final MapThing mt = new MapThing();
            mt.x = b.getShort(o);
            mt.y = b.getShort(o + 2);
            mt.angle = b.getShort(o + 4);
            mt.type = b.getShort(o + 6);
            mt.options = b.getShort(o + 8);
            boolean spawn = true;
            if (g.gamemode != commercial) {
                switch (mt.type) {
                    case 68, 64, 88, 89, 69, 67, 71, 65, 66, 84 -> spawn = false;
                    default -> {
                    }
                }
            }
            if (!spawn) {
                // vanilla stops reading things at the first Doom II only monster
                break;
            }
            g.mobjs.P_SpawnMapThing(mt);
        }
    }

    private void loadLineDefs(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int n = g.wad.lumpLength(lump) / 14;
        level.lines = new Line[n];
        for (int i = 0; i < n; i++) {
            final int o = i * 14;
            final Line ld = new Line(i);
            ld.flags = b.getShort(o + 4);
            ld.special = b.getShort(o + 6);
            ld.tag = b.getShort(o + 8);
            final Vertex v1 = ld.v1 = level.vertexes[b.getShort(o) & 0xffff];
            final Vertex v2 = ld.v2 = level.vertexes[b.getShort(o + 2) & 0xffff];
            ld.dx = v2.x - v1.x;
            ld.dy = v2.y - v1.y;
            if (ld.dx == 0) {
                ld.slopetype = Line.ST_VERTICAL;
            } else if (ld.dy == 0) {
                ld.slopetype = Line.ST_HORIZONTAL;
            } else {
                if (div(ld.dy, ld.dx) > 0) {
                    ld.slopetype = Line.ST_POSITIVE;
                } else {
                    ld.slopetype = Line.ST_NEGATIVE;
                }
            }
            if (v1.x < v2.x) {
                ld.bbox[BOXLEFT] = v1.x;
                ld.bbox[BOXRIGHT] = v2.x;
            } else {
                ld.bbox[BOXLEFT] = v2.x;
                ld.bbox[BOXRIGHT] = v1.x;
            }
            if (v1.y < v2.y) {
                ld.bbox[BOXBOTTOM] = v1.y;
                ld.bbox[BOXTOP] = v2.y;
            } else {
                ld.bbox[BOXBOTTOM] = v2.y;
                ld.bbox[BOXTOP] = v1.y;
            }
            ld.sidenum[0] = b.getShort(o + 10);
            ld.sidenum[1] = b.getShort(o + 12);
            if (ld.sidenum[0] != -1) {
                ld.frontsector = level.sides[ld.sidenum[0] & 0xffff].sector;
            } else {
                ld.frontsector = null;
            }
            if (ld.sidenum[1] != -1) {
                ld.backsector = level.sides[ld.sidenum[1] & 0xffff].sector;
            } else {
                ld.backsector = null;
            }
            level.lines[i] = ld;
        }
    }

    private void loadSideDefs(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final byte[] raw = g.wad.readLump(lump);
        final int n = raw.length / 30;
        final RenderData res = g.res;
        level.sides = new Side[n];
        for (int i = 0; i < n; i++) {
            final int o = i * 30;
            final Side sd = new Side();
            sd.textureoffset = b.getShort(o) << FRACBITS;
            sd.rowoffset = b.getShort(o + 2) << FRACBITS;
            sd.toptexture = (short) res.textureNumForName(Wad.readName(raw, o + 4));
            sd.bottomtexture = (short) res.textureNumForName(Wad.readName(raw, o + 12));
            sd.midtexture = (short) res.textureNumForName(Wad.readName(raw, o + 20));
            final int sec = b.getShort(o + 28) & 0xffff;
            sd.sector = level.sectors[Math.min(sec, level.sectors.length - 1)];
            level.sides[i] = sd;
        }
    }

    private void loadBlockMap(final Level level, final int lump) {
        final ByteBuffer b = buf(g.wad, lump);
        final int count = g.wad.lumpLength(lump) / 2;
        level.blockmaplump = new int[count];
        for (int i = 0; i < count; i++) {
            level.blockmaplump[i] = b.getShort(i * 2) & 0xffff;
        }
        level.bmaporgx = ((short) level.blockmaplump[0]) << FRACBITS;
        level.bmaporgy = ((short) level.blockmaplump[1]) << FRACBITS;
        level.bmapwidth = level.blockmaplump[2];
        level.bmapheight = level.blockmaplump[3];
        level.blocklinks = new Mobj[level.bmapwidth * level.bmapheight];
    }

    private void groupLines(final Level level) {
        for (final Subsector ss : level.subsectors) {
            final Seg seg = level.segs[ss.firstline];
            ss.sector = seg.sidedef.sector;
        }
        for (final Line li : level.lines) {
            li.frontsector.linecount++;
            if (li.backsector != null && li.backsector != li.frontsector) {
                li.backsector.linecount++;
            }
        }
        final int[] fill = new int[level.sectors.length];
        for (final Sector sector : level.sectors) {
            sector.lines = new Line[sector.linecount];
        }
        for (final Line li : level.lines) {
            final Sector front = li.frontsector;
            front.lines[fill[front.id]++] = li;
            if (li.backsector != null && li.backsector != front) {
                final Sector back = li.backsector;
                back.lines[fill[back.id]++] = li;
            }
        }
        final int[] bbox = new int[4];
        for (final Sector sector : level.sectors) {
            BBox.clear(bbox);
            for (final Line li : sector.lines) {
                BBox.add(bbox, li.v1.x, li.v1.y);
                BBox.add(bbox, li.v2.x, li.v2.y);
            }
            sector.soundorg.x = (bbox[BOXRIGHT] + bbox[BOXLEFT]) / 2;
            sector.soundorg.y = (bbox[BOXTOP] + bbox[BOXBOTTOM]) / 2;
            int block = (bbox[BOXTOP] - level.bmaporgy + MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block >= level.bmapheight ? level.bmapheight - 1 : block;
            sector.blockbox[BOXTOP] = block;
            block = (bbox[BOXBOTTOM] - level.bmaporgy - MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXBOTTOM] = block;
            block = (bbox[BOXRIGHT] - level.bmaporgx + MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block >= level.bmapwidth ? level.bmapwidth - 1 : block;
            sector.blockbox[BOXRIGHT] = block;
            block = (bbox[BOXLEFT] - level.bmaporgx - MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXLEFT] = block;
        }
    }
}
