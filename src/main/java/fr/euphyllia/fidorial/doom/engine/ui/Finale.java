// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import fr.euphyllia.fidorial.doom.engine.info.State;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.render.Video;

import static fr.euphyllia.fidorial.doom.engine.Defs.FF_FRAMEMASK;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_FINALE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_nothing;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_worlddone;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_plut;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_tnt;
import static fr.euphyllia.fidorial.doom.engine.Defs.retail;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_bunny;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_evil;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_read_m;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_victor;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_claw;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dshtgn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_firsht;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pistol;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_plasma;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_rlaunc;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sgtatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_shotgn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skeatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skepch;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skeswg;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sklatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vilatk;
import static fr.euphyllia.fidorial.doom.engine.Strings.C1TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.C2TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.C3TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.C4TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.C5TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.C6TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_ARACH;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_ARCH;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_BARON;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_CACO;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_CYBER;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_DEMON;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_HEAVY;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_HELL;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_HERO;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_IMP;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_LOST;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_MANCU;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_PAIN;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_REVEN;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_SHOTGUN;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_SPIDER;
import static fr.euphyllia.fidorial.doom.engine.Strings.CC_ZOMBIE;
import static fr.euphyllia.fidorial.doom.engine.Strings.E1TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.E2TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.E3TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.E4TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P1TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P2TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P3TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P4TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P5TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.P6TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T1TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T2TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T3TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T4TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T5TEXT;
import static fr.euphyllia.fidorial.doom.engine.Strings.T6TEXT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BABY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CHAINGUY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CYBORG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_FATSO;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_HEAD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_KNIGHT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PLAYER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_POSSESSED;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SERGEANT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SHOTGUY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SKULL;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SPIDER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TROOP;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_UNDEAD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_VILE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_ATK3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_ATK4;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_ATK4;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_ATK6;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_ATK5;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_ATK8;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_ATK3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_FIST2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_FIST4;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_MISS2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_ATK3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_ATK3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_ATK2;

/**
 * Episode end text screens, the bunny scroller and the Doom II cast call ({@code f_finale.c}).
 */
public final class Finale {

    private static final int TEXTSPEED = 3;
    private static final int TEXTWAIT = 250;

    private static final String[] CAST_NAMES = {
            CC_ZOMBIE, CC_SHOTGUN, CC_HEAVY, CC_IMP, CC_DEMON, CC_LOST, CC_CACO, CC_HELL, CC_BARON, CC_ARACH,
            CC_PAIN, CC_REVEN, CC_MANCU, CC_ARCH, CC_SPIDER, CC_CYBER, CC_HERO
    };
    private static final int[] CAST_TYPES = {
            MT_POSSESSED, MT_SHOTGUY, MT_CHAINGUY, MT_TROOP, MT_SERGEANT, MT_SKULL, MT_HEAD, MT_KNIGHT, MT_BRUISER,
            MT_BABY, MT_PAIN, MT_UNDEAD, MT_FATSO, MT_VILE, MT_SPIDER, MT_CYBORG, MT_PLAYER
    };

    private final Game g;
    private final RenderData res;
    private final Video video = new Video();
    public int finalestage;
    public int finalecount;
    private HuFont font;
    private String finaletext = "";
    private String finaleflat = "F_SKY1";

    private int castnum;
    private int casttics;
    private State caststate;
    private boolean castdeath;
    private int castframes;
    private int castonmelee;
    private boolean castattacking;
    private int laststage;

    public Finale(final Game g) {
        this.g = g;
        this.res = g.res;
    }

    private HuFont font() {
        if (font == null) {
            font = new HuFont(res);
        }
        return font;
    }

    public void F_StartFinale() {
        g.gameaction = ga_nothing;
        g.gamestate = GS_FINALE;
        if (g.gamemode == commercial) {
            g.sound.S_ChangeMusic(mus_read_m, true);
            final boolean tnt = g.gamemission == pack_tnt;
            final boolean plut = g.gamemission == pack_plut;
            switch (g.gamemap) {
                case 6 -> {
                    finaleflat = "SLIME16";
                    finaletext = tnt ? T1TEXT : plut ? P1TEXT : C1TEXT;
                }
                case 11 -> {
                    finaleflat = "RROCK14";
                    finaletext = tnt ? T2TEXT : plut ? P2TEXT : C2TEXT;
                }
                case 20 -> {
                    finaleflat = "RROCK07";
                    finaletext = tnt ? T3TEXT : plut ? P3TEXT : C3TEXT;
                }
                case 30 -> {
                    finaleflat = "RROCK17";
                    finaletext = tnt ? T4TEXT : plut ? P4TEXT : C4TEXT;
                }
                case 15 -> {
                    finaleflat = "RROCK13";
                    finaletext = tnt ? T5TEXT : plut ? P5TEXT : C5TEXT;
                }
                case 31 -> {
                    finaleflat = "RROCK19";
                    finaletext = tnt ? T6TEXT : plut ? P6TEXT : C6TEXT;
                }
                default -> {
                }
            }
        } else {
            g.sound.S_ChangeMusic(mus_victor, true);
            switch (g.gameepisode) {
                case 2 -> {
                    finaleflat = "SFLR6_1";
                    finaletext = E2TEXT;
                }
                case 3 -> {
                    finaleflat = "MFLR8_4";
                    finaletext = E3TEXT;
                }
                case 4 -> {
                    finaleflat = "MFLR8_3";
                    finaletext = E4TEXT;
                }
                default -> {
                    finaleflat = "FLOOR4_8";
                    finaletext = E1TEXT;
                }
            }
        }
        finaleflat = g.text(finaleflat);
        finaletext = g.text(finaletext);
        finalestage = 0;
        finalecount = 0;
    }

    /**
     * {@return whether the episode is over for good, on the last picture}
     */
    public boolean isFinished() {
        return g.gamemode != commercial && finalestage == 1;
    }

    public void F_Ticker() {
        if (g.gamemode == commercial && finalecount > 50) {
            int i;
            for (i = 0; i < MAXPLAYERS; i++) {
                if (g.playeringame[i] && g.players[i].cmd.buttons() != 0) {
                    break;
                }
            }
            if (i < MAXPLAYERS) {
                if (g.gamemap == 30) {
                    if (finalestage != 2) {
                        F_StartCast();
                    } else {
                        castDeathOnPress();
                    }
                } else {
                    g.gameaction = ga_worlddone;
                }
            }
        }
        finalecount++;
        if (finalestage == 2) {
            F_CastTicker();
            return;
        }
        if (g.gamemode == commercial) {
            return;
        }
        if (finalestage == 0 && finalecount > finaletext.length() * TEXTSPEED + TEXTWAIT) {
            finalecount = 0;
            finalestage = 1;
            if (g.gameepisode == 3) {
                g.sound.S_ChangeMusic(mus_bunny, false);
            }
        }
    }

    private void castDeathOnPress() {
        if (castdeath) {
            return;
        }
        castdeath = true;
        final MobjInfo info = g.mobjinfo[CAST_TYPES[castnum]];
        caststate = g.states[info.deathstate()];
        casttics = caststate.tics();
        castframes = 0;
        castattacking = false;
        if (info.deathsound() != 0) {
            g.sound.S_StartSound(null, info.deathsound());
        }
    }

    private void F_TextWrite(final byte[] screen) {
        final int flat = res.checkFlatNumForName(finaleflat);
        video.tileFlat(screen, flat >= 0 ? res.flats[flat] : new byte[4096]);
        int cx = 10;
        int cy = 10;
        int count = (finalecount - 10) / TEXTSPEED;
        if (count < 0) {
            count = 0;
        }
        final HuFont f = font();
        for (int i = 0; i < finaletext.length() && count > 0; i++, count--) {
            final char c = finaletext.charAt(i);
            if (c == '\n') {
                cx = 10;
                cy += 11;
                continue;
            }
            final Patch p = f.glyph(c);
            if (p == null) {
                cx += 4;
                continue;
            }
            if (cx + p.width > SCREENWIDTH) {
                break;
            }
            video.drawPatch(screen, cx, cy, p);
            cx += p.width;
        }
    }

    private void F_StartCast() {
        castnum = 0;
        caststate = g.states[g.mobjinfo[CAST_TYPES[castnum]].seestate()];
        casttics = caststate.tics();
        castdeath = false;
        finalestage = 2;
        castframes = 0;
        castonmelee = 0;
        castattacking = false;
        g.sound.S_ChangeMusic(mus_evil, true);
    }

    private void F_CastTicker() {
        if (--casttics > 0) {
            return;
        }
        boolean stopattack = false;
        if (caststate.tics() == -1 || caststate.nextstate() == S_NULL) {
            castnum++;
            castdeath = false;
            if (castnum >= CAST_TYPES.length) {
                castnum = 0;
            }
            final MobjInfo info = g.mobjinfo[CAST_TYPES[castnum]];
            if (info.seesound() != 0) {
                g.sound.S_StartSound(null, info.seesound());
            }
            caststate = g.states[info.seestate()];
            castframes = 0;
        } else {
            if (caststate.index() == S_PLAY_ATK1) {
                stopattack = true;
            } else {
                final int st = caststate.nextstate();
                caststate = g.states[st];
                castframes++;
                final int sfx = switch (st) {
                    case S_PLAY_ATK1 -> sfx_dshtgn;
                    case S_POSS_ATK2 -> sfx_pistol;
                    case S_SPOS_ATK2 -> sfx_shotgn;
                    case S_VILE_ATK2 -> sfx_vilatk;
                    case S_SKEL_FIST2 -> sfx_skeswg;
                    case S_SKEL_FIST4 -> sfx_skepch;
                    case S_SKEL_MISS2 -> sfx_skeatk;
                    case S_FATT_ATK8, S_FATT_ATK5, S_FATT_ATK2 -> sfx_firsht;
                    case S_CPOS_ATK2, S_CPOS_ATK3, S_CPOS_ATK4 -> sfx_shotgn;
                    case S_TROO_ATK3 -> sfx_claw;
                    case S_SARG_ATK2 -> sfx_sgtatk;
                    case S_BOSS_ATK2, S_BOS2_ATK2, S_HEAD_ATK2 -> sfx_firsht;
                    case S_SKULL_ATK2 -> sfx_sklatk;
                    case S_SPID_ATK2, S_SPID_ATK3 -> sfx_shotgn;
                    case S_BSPI_ATK2 -> sfx_plasma;
                    case S_CYBER_ATK2, S_CYBER_ATK4, S_CYBER_ATK6 -> sfx_rlaunc;
                    case S_PAIN_ATK3 -> sfx_sklatk;
                    default -> 0;
                };
                if (sfx != 0) {
                    g.sound.S_StartSound(null, sfx);
                }
            }
        }
        final MobjInfo info = g.mobjinfo[CAST_TYPES[castnum]];
        if (!stopattack && castframes == 12) {
            castattacking = true;
            caststate = g.states[castonmelee != 0 ? info.meleestate() : info.missilestate()];
            castonmelee ^= 1;
            if (caststate.index() == S_NULL) {
                caststate = g.states[castonmelee != 0 ? info.meleestate() : info.missilestate()];
            }
        }
        if (stopattack || (castattacking && (castframes == 24 || caststate.index() == info.seestate()))) {
            castattacking = false;
            castframes = 0;
            caststate = g.states[info.seestate()];
        }
        casttics = caststate.tics();
        if (casttics == -1) {
            casttics = 15;
        }
    }

    private void F_CastPrint(final byte[] screen, final String text) {
        final HuFont f = font();
        final int width = f.width(text);
        f.draw(video, screen, 160 - width / 2, 180, text);
    }

    private void F_CastDrawer(final byte[] screen) {
        video.drawPatch(screen, 0, 0, res.patch("BOSSBACK"));
        F_CastPrint(screen, g.text(CAST_NAMES[castnum]));
        final RenderData.SpriteDef sprdef = res.sprites[caststate.sprite()];
        final int frame = caststate.frame() & FF_FRAMEMASK;
        if (frame >= sprdef.frames().length) {
            return;
        }
        final RenderData.SpriteFrame sprframe = sprdef.frames()[frame];
        final int lump = sprframe.lump()[0];
        if (lump < 0 || lump >= res.spritePatches.length) {
            return;
        }
        final Patch patch = res.spritePatches[lump];
        if (sprframe.flip()[0]) {
            video.drawPatchFlipped(screen, 160, 170, patch);
        } else {
            video.drawPatch(screen, 160, 170, patch);
        }
    }

    private void F_BunnyScroll(final byte[] screen) {
        final Patch p1 = res.patch("PFUB2");
        final Patch p2 = res.patch("PFUB1");
        int scrolled = 320 - (finalecount - 230) / 2;
        if (scrolled > 320) {
            scrolled = 320;
        }
        if (scrolled < 0) {
            scrolled = 0;
        }
        java.util.Arrays.fill(screen, (byte) 0);
        for (int x = 0; x < SCREENWIDTH; x++) {
            final Patch src;
            final int col;
            if (x + scrolled < 320) {
                src = p1;
                col = x + scrolled;
            } else {
                src = p2;
                col = x + scrolled - 320;
            }
            if (col < src.width) {
                final Patch.Column column = src.columns[col];
                for (int p = 0; p < column.tops().length; p++) {
                    final byte[] post = column.posts()[p];
                    for (int k = 0; k < post.length; k++) {
                        final int y = column.tops()[p] + k;
                        if (y >= 0 && y < SCREENHEIGHT) {
                            screen[y * SCREENWIDTH + x] = post[k];
                        }
                    }
                }
            }
        }
        if (finalecount < 1130) {
            return;
        }
        if (finalecount < 1180) {
            video.drawPatch(screen, (SCREENWIDTH - 13 * 8) / 2, (SCREENHEIGHT - 8 * 8) / 2, res.patch("END0"));
            laststage = 0;
            return;
        }
        int stage = (finalecount - 1180) / 5;
        if (stage > 6) {
            stage = 6;
        }
        if (stage > laststage) {
            g.sound.S_StartSound(null, sfx_pistol);
            laststage = stage;
        }
        video.drawPatch(screen, (SCREENWIDTH - 13 * 8) / 2, (SCREENHEIGHT - 8 * 8) / 2, res.patch("END" + stage));
    }

    public void F_Drawer(final byte[] screen) {
        if (finalestage == 2) {
            F_CastDrawer(screen);
            return;
        }
        if (finalestage == 0) {
            F_TextWrite(screen);
            return;
        }
        switch (g.gameepisode) {
            case 1 -> video.drawPatch(screen, 0, 0, res.patch(g.gamemode == retail ? "CREDIT" : "HELP2"));
            case 2 -> video.drawPatch(screen, 0, 0, res.patch("VICTORY2"));
            case 3 -> F_BunnyScroll(screen);
            case 4 -> video.drawPatch(screen, 0, 0, res.patch("ENDPIC"));
            default -> {
            }
        }
    }
}
