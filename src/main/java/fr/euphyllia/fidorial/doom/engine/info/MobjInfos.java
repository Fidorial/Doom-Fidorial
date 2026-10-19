// Generated from id Software's linuxdoom-1.10 sources by gen_info.py - do not edit.
// Copyright (C) 1993-1996 by id Software, Inc. Licensed under the GNU GPL v2 or later.
package fr.euphyllia.fidorial.doom.engine.info;


import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTITEM;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTKILL;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPOFF;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_FLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_MISSILE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOBLOCKMAP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOBLOOD;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOGRAVITY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOSECTOR;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOTDMATCH;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_PICKUP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHOOTABLE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SOLID;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SPAWNCEILING;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SPECIAL;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_None;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_barexp;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgdth1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgsit1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bosdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bospit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bospn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_brsdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_brssit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bspact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bspdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bspsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_cacdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_cacsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_cybdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_cybsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dmact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dmpain;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_firsht;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_firxpl;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_keendt;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_keenpn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_kntdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_kntsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_mandth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_mansit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_mnpain;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pedth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pepain;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pesit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pistol;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_plasma;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pldeth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_plpain;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_podth1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_podth2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_popain;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posit1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posit2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_rlaunc;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_rxplod;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sgtatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sgtdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sgtsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_shotgn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skeact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skeatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skedth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skesit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sklatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_spidth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_spisit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_ssdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sssit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vilact;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vildth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vilsit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vipain;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_AMMO;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ARACH_PLAZ;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ARACH_PLEX;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ARM1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ARM2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BAR1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BBAR1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BEXP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGEXP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGLAND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFUG;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BIGTREE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BKEY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BLOOD1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BLOODYTWITCH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BLUETORCH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BON1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BON2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOS2_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BOSS_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BPAK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAINEYE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAINEYESEE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAINSTEM;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAIN_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAIN_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRBALL1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRBALLX1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BROK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSKULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_SIGHT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BSPI_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BTORCHSHRT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CANDELABRA;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CANDLESTIK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CELL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CELP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CLIP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_COLONGIBS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_COLU;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_COMMKEEN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CPOS_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CSAW;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CYBER_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DEADSTICK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_EVILEYE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_EXPLODE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATSHOT1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATSHOTX1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FATT_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FIRE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_FLOATSKULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_GIBS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_GREENTORCH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_GTORCHSHRT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGBNOBRAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGNOGUTS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGTLOOKDN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGTLOOKUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGTNOBRAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HANGTSKULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEADCANDLES;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEADONASTICK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEADSONSTICK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_DIE6;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEAD_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_HEARTCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_IFOG;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_KEENPAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_KEENSTND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_LAUN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_LIVESTICK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEAT2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEAT3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEAT4;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEAT5;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEDI;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MEGA;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MGUN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PAIN_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PINS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PINV;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASBALL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASEXP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_DIE7;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_XDIE9;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PMAP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_DIE5;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_POSS_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PSTR;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUFF1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PVIS;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_RBALL1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_RBALLX1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_REDTORCH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_RKEY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ROCK;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_ROCKET;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_RSKULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_RTORCHSHRT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_DIE6;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SBOX;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SHEL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SHOT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SHOT2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SHRTGRNCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SHRTREDCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_FIST1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_MISS1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKEL_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULLCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_DIE6;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SKULL_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SMALLPOOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SMOKE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SOUL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPAWN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPAWNFIRE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPID_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_DIE5;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SPOS_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SSWV_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_STALAGTITE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_STIM;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SUIT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TALLGRNCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TALLREDCOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TBALL1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TBALLX1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TECH2LAMP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TECHLAMP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TECHPILLAR;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TFOG;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TORCHTREE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TRACEEXP1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TRACER;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_DIE5;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_RAISE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_TROO_XDIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_DIE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_STND;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_YKEY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_YSKULL;

/**
 * The thing type table ({@code mobjinfo[]}).
 */
public final class MobjInfos {

    public static final MobjInfo[] MOBJINFO = {
            new MobjInfo(-1, S_PLAY, 100, S_PLAY_RUN1, sfx_None, 0, sfx_None, S_PLAY_PAIN, 255, sfx_plpain, S_NULL, S_PLAY_ATK1, S_PLAY_DIE1, S_PLAY_XDIE1, sfx_pldeth, 0, 16 * 65536, 56 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SHOOTABLE | MF_DROPOFF | MF_PICKUP | MF_NOTDMATCH, S_NULL),
            new MobjInfo(3004, S_POSS_STND, 20, S_POSS_RUN1, sfx_posit1, 8, sfx_pistol, S_POSS_PAIN, 200, sfx_popain, 0, S_POSS_ATK1, S_POSS_DIE1, S_POSS_XDIE1, sfx_podth1, 8, 20 * 65536, 56 * 65536, 100, 0, sfx_posact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_POSS_RAISE1),
            new MobjInfo(9, S_SPOS_STND, 30, S_SPOS_RUN1, sfx_posit2, 8, 0, S_SPOS_PAIN, 170, sfx_popain, 0, S_SPOS_ATK1, S_SPOS_DIE1, S_SPOS_XDIE1, sfx_podth2, 8, 20 * 65536, 56 * 65536, 100, 0, sfx_posact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_SPOS_RAISE1),
            new MobjInfo(64, S_VILE_STND, 700, S_VILE_RUN1, sfx_vilsit, 8, 0, S_VILE_PAIN, 10, sfx_vipain, 0, S_VILE_ATK1, S_VILE_DIE1, S_NULL, sfx_vildth, 15, 20 * 65536, 56 * 65536, 500, 0, sfx_vilact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_NULL),
            new MobjInfo(-1, S_FIRE1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(66, S_SKEL_STND, 300, S_SKEL_RUN1, sfx_skesit, 8, 0, S_SKEL_PAIN, 100, sfx_popain, S_SKEL_FIST1, S_SKEL_MISS1, S_SKEL_DIE1, S_NULL, sfx_skedth, 10, 20 * 65536, 56 * 65536, 500, 0, sfx_skeact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_SKEL_RAISE1),
            new MobjInfo(-1, S_TRACER, 1000, S_NULL, sfx_skeatk, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_TRACEEXP1, S_NULL, sfx_barexp, 10 * 65536, 11 * 65536, 8 * 65536, 100, 10, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_SMOKE1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(67, S_FATT_STND, 600, S_FATT_RUN1, sfx_mansit, 8, 0, S_FATT_PAIN, 80, sfx_mnpain, 0, S_FATT_ATK1, S_FATT_DIE1, S_NULL, sfx_mandth, 8, 48 * 65536, 64 * 65536, 1000, 0, sfx_posact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_FATT_RAISE1),
            new MobjInfo(-1, S_FATSHOT1, 1000, S_NULL, sfx_firsht, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_FATSHOTX1, S_NULL, sfx_firxpl, 20 * 65536, 6 * 65536, 8 * 65536, 100, 8, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(65, S_CPOS_STND, 70, S_CPOS_RUN1, sfx_posit2, 8, 0, S_CPOS_PAIN, 170, sfx_popain, 0, S_CPOS_ATK1, S_CPOS_DIE1, S_CPOS_XDIE1, sfx_podth2, 8, 20 * 65536, 56 * 65536, 100, 0, sfx_posact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_CPOS_RAISE1),
            new MobjInfo(3001, S_TROO_STND, 60, S_TROO_RUN1, sfx_bgsit1, 8, 0, S_TROO_PAIN, 200, sfx_popain, S_TROO_ATK1, S_TROO_ATK1, S_TROO_DIE1, S_TROO_XDIE1, sfx_bgdth1, 8, 20 * 65536, 56 * 65536, 100, 0, sfx_bgact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_TROO_RAISE1),
            new MobjInfo(3002, S_SARG_STND, 150, S_SARG_RUN1, sfx_sgtsit, 8, sfx_sgtatk, S_SARG_PAIN, 180, sfx_dmpain, S_SARG_ATK1, 0, S_SARG_DIE1, S_NULL, sfx_sgtdth, 10, 30 * 65536, 56 * 65536, 400, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_SARG_RAISE1),
            new MobjInfo(58, S_SARG_STND, 150, S_SARG_RUN1, sfx_sgtsit, 8, sfx_sgtatk, S_SARG_PAIN, 180, sfx_dmpain, S_SARG_ATK1, 0, S_SARG_DIE1, S_NULL, sfx_sgtdth, 10, 30 * 65536, 56 * 65536, 400, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_SHADOW | MF_COUNTKILL, S_SARG_RAISE1),
            new MobjInfo(3005, S_HEAD_STND, 400, S_HEAD_RUN1, sfx_cacsit, 8, 0, S_HEAD_PAIN, 128, sfx_dmpain, 0, S_HEAD_ATK1, S_HEAD_DIE1, S_NULL, sfx_cacdth, 8, 31 * 65536, 56 * 65536, 400, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_FLOAT | MF_NOGRAVITY | MF_COUNTKILL, S_HEAD_RAISE1),
            new MobjInfo(3003, S_BOSS_STND, 1000, S_BOSS_RUN1, sfx_brssit, 8, 0, S_BOSS_PAIN, 50, sfx_dmpain, S_BOSS_ATK1, S_BOSS_ATK1, S_BOSS_DIE1, S_NULL, sfx_brsdth, 8, 24 * 65536, 64 * 65536, 1000, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_BOSS_RAISE1),
            new MobjInfo(-1, S_BRBALL1, 1000, S_NULL, sfx_firsht, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_BRBALLX1, S_NULL, sfx_firxpl, 15 * 65536, 6 * 65536, 8 * 65536, 100, 8, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(69, S_BOS2_STND, 500, S_BOS2_RUN1, sfx_kntsit, 8, 0, S_BOS2_PAIN, 50, sfx_dmpain, S_BOS2_ATK1, S_BOS2_ATK1, S_BOS2_DIE1, S_NULL, sfx_kntdth, 8, 24 * 65536, 64 * 65536, 1000, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_BOS2_RAISE1),
            new MobjInfo(3006, S_SKULL_STND, 100, S_SKULL_RUN1, 0, 8, sfx_sklatk, S_SKULL_PAIN, 256, sfx_dmpain, 0, S_SKULL_ATK1, S_SKULL_DIE1, S_NULL, sfx_firxpl, 8, 16 * 65536, 56 * 65536, 50, 3, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_FLOAT | MF_NOGRAVITY, S_NULL),
            new MobjInfo(7, S_SPID_STND, 3000, S_SPID_RUN1, sfx_spisit, 8, sfx_shotgn, S_SPID_PAIN, 40, sfx_dmpain, 0, S_SPID_ATK1, S_SPID_DIE1, S_NULL, sfx_spidth, 12, 128 * 65536, 100 * 65536, 1000, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_NULL),
            new MobjInfo(68, S_BSPI_STND, 500, S_BSPI_SIGHT, sfx_bspsit, 8, 0, S_BSPI_PAIN, 128, sfx_dmpain, 0, S_BSPI_ATK1, S_BSPI_DIE1, S_NULL, sfx_bspdth, 12, 64 * 65536, 64 * 65536, 600, 0, sfx_bspact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_BSPI_RAISE1),
            new MobjInfo(16, S_CYBER_STND, 4000, S_CYBER_RUN1, sfx_cybsit, 8, 0, S_CYBER_PAIN, 20, sfx_dmpain, 0, S_CYBER_ATK1, S_CYBER_DIE1, S_NULL, sfx_cybdth, 16, 40 * 65536, 110 * 65536, 1000, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_NULL),
            new MobjInfo(71, S_PAIN_STND, 400, S_PAIN_RUN1, sfx_pesit, 8, 0, S_PAIN_PAIN, 128, sfx_pepain, 0, S_PAIN_ATK1, S_PAIN_DIE1, S_NULL, sfx_pedth, 8, 31 * 65536, 56 * 65536, 400, 0, sfx_dmact, MF_SOLID | MF_SHOOTABLE | MF_FLOAT | MF_NOGRAVITY | MF_COUNTKILL, S_PAIN_RAISE1),
            new MobjInfo(84, S_SSWV_STND, 50, S_SSWV_RUN1, sfx_sssit, 8, 0, S_SSWV_PAIN, 170, sfx_popain, 0, S_SSWV_ATK1, S_SSWV_DIE1, S_SSWV_XDIE1, sfx_ssdth, 8, 20 * 65536, 56 * 65536, 100, 0, sfx_posact, MF_SOLID | MF_SHOOTABLE | MF_COUNTKILL, S_SSWV_RAISE1),
            new MobjInfo(72, S_KEENSTND, 100, S_NULL, sfx_None, 8, sfx_None, S_KEENPAIN, 256, sfx_keenpn, S_NULL, S_NULL, S_COMMKEEN, S_NULL, sfx_keendt, 0, 16 * 65536, 72 * 65536, 10000000, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY | MF_SHOOTABLE | MF_COUNTKILL, S_NULL),
            new MobjInfo(88, S_BRAIN, 250, S_NULL, sfx_None, 8, sfx_None, S_BRAIN_PAIN, 255, sfx_bospn, S_NULL, S_NULL, S_BRAIN_DIE1, S_NULL, sfx_bosdth, 0, 16 * 65536, 16 * 65536, 10000000, 0, sfx_None, MF_SOLID | MF_SHOOTABLE, S_NULL),
            new MobjInfo(89, S_BRAINEYE, 1000, S_BRAINEYESEE, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 32 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOSECTOR, S_NULL),
            new MobjInfo(87, S_NULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 32 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOSECTOR, S_NULL),
            new MobjInfo(-1, S_SPAWN1, 1000, S_NULL, sfx_bospit, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_firxpl, 10 * 65536, 6 * 65536, 32 * 65536, 100, 3, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY | MF_NOCLIP, S_NULL),
            new MobjInfo(-1, S_SPAWNFIRE1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(2035, S_BAR1, 20, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_BEXP, S_NULL, sfx_barexp, 0, 10 * 65536, 42 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SHOOTABLE | MF_NOBLOOD, S_NULL),
            new MobjInfo(-1, S_TBALL1, 1000, S_NULL, sfx_firsht, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_TBALLX1, S_NULL, sfx_firxpl, 10 * 65536, 6 * 65536, 8 * 65536, 100, 3, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_RBALL1, 1000, S_NULL, sfx_firsht, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_RBALLX1, S_NULL, sfx_firxpl, 10 * 65536, 6 * 65536, 8 * 65536, 100, 5, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_ROCKET, 1000, S_NULL, sfx_rlaunc, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_EXPLODE1, S_NULL, sfx_barexp, 20 * 65536, 11 * 65536, 8 * 65536, 100, 20, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_PLASBALL, 1000, S_NULL, sfx_plasma, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_PLASEXP, S_NULL, sfx_firxpl, 25 * 65536, 13 * 65536, 8 * 65536, 100, 5, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_BFGSHOT, 1000, S_NULL, 0, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_BFGLAND, S_NULL, sfx_rxplod, 25 * 65536, 13 * 65536, 8 * 65536, 100, 100, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_ARACH_PLAZ, 1000, S_NULL, sfx_plasma, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_ARACH_PLEX, S_NULL, sfx_firxpl, 25 * 65536, 13 * 65536, 8 * 65536, 100, 5, sfx_None, MF_NOBLOCKMAP | MF_MISSILE | MF_DROPOFF | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_PUFF1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_BLOOD1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP, S_NULL),
            new MobjInfo(-1, S_TFOG, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(-1, S_IFOG, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(14, S_NULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOSECTOR, S_NULL),
            new MobjInfo(-1, S_BFGEXP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP | MF_NOGRAVITY, S_NULL),
            new MobjInfo(2018, S_ARM1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2019, S_ARM2, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2014, S_BON1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2015, S_BON2, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(5, S_BKEY, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(13, S_RKEY, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(6, S_YKEY, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(39, S_YSKULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(38, S_RSKULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(40, S_BSKULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_NOTDMATCH, S_NULL),
            new MobjInfo(2011, S_STIM, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2012, S_MEDI, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2013, S_SOUL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2022, S_PINV, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2023, S_PSTR, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2024, S_PINS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2025, S_SUIT, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2026, S_PMAP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2045, S_PVIS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(83, S_MEGA, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL | MF_COUNTITEM, S_NULL),
            new MobjInfo(2007, S_CLIP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2048, S_AMMO, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2010, S_ROCK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2046, S_BROK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2047, S_CELL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(17, S_CELP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2008, S_SHEL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2049, S_SBOX, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(8, S_BPAK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2006, S_BFUG, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2002, S_MGUN, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2005, S_CSAW, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2003, S_LAUN, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2004, S_PLAS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(2001, S_SHOT, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(82, S_SHOT2, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SPECIAL, S_NULL),
            new MobjInfo(85, S_TECHLAMP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(86, S_TECH2LAMP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(2028, S_COLU, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(30, S_TALLGRNCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(31, S_SHRTGRNCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(32, S_TALLREDCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(33, S_SHRTREDCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(37, S_SKULLCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(36, S_HEARTCOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(41, S_EVILEYE, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(42, S_FLOATSKULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(43, S_TORCHTREE, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(44, S_BLUETORCH, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(45, S_GREENTORCH, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(46, S_REDTORCH, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(55, S_BTORCHSHRT, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(56, S_GTORCHSHRT, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(57, S_RTORCHSHRT, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(47, S_STALAGTITE, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(48, S_TECHPILLAR, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(34, S_CANDLESTIK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(35, S_CANDELABRA, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(49, S_BLOODYTWITCH, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 68 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(50, S_MEAT2, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 84 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(51, S_MEAT3, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 84 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(52, S_MEAT4, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 68 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(53, S_MEAT5, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 52 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(59, S_MEAT2, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 84 * 65536, 100, 0, sfx_None, MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(60, S_MEAT4, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 68 * 65536, 100, 0, sfx_None, MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(61, S_MEAT3, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 52 * 65536, 100, 0, sfx_None, MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(62, S_MEAT5, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 52 * 65536, 100, 0, sfx_None, MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(63, S_BLOODYTWITCH, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 68 * 65536, 100, 0, sfx_None, MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(22, S_HEAD_DIE6, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(15, S_PLAY_DIE7, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(18, S_POSS_DIE5, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(21, S_SARG_DIE6, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(23, S_SKULL_DIE6, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(20, S_TROO_DIE5, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(19, S_SPOS_DIE5, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(10, S_PLAY_XDIE9, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(12, S_PLAY_XDIE9, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(28, S_HEADSONSTICK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(24, S_GIBS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, 0, S_NULL),
            new MobjInfo(27, S_HEADONASTICK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(29, S_HEADCANDLES, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(25, S_DEADSTICK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(26, S_LIVESTICK, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(54, S_BIGTREE, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 32 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(70, S_BBAR1, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 16 * 65536, 100, 0, sfx_None, MF_SOLID, S_NULL),
            new MobjInfo(73, S_HANGNOGUTS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 88 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(74, S_HANGBNOBRAIN, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 88 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(75, S_HANGTLOOKDN, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 64 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(76, S_HANGTSKULL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 64 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(77, S_HANGTLOOKUP, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 64 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(78, S_HANGTNOBRAIN, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 16 * 65536, 64 * 65536, 100, 0, sfx_None, MF_SOLID | MF_SPAWNCEILING | MF_NOGRAVITY, S_NULL),
            new MobjInfo(79, S_COLONGIBS, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP, S_NULL),
            new MobjInfo(80, S_SMALLPOOL, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP, S_NULL),
            new MobjInfo(81, S_BRAINSTEM, 1000, S_NULL, sfx_None, 8, sfx_None, S_NULL, 0, sfx_None, S_NULL, S_NULL, S_NULL, S_NULL, sfx_None, 0, 20 * 65536, 16 * 65536, 100, 0, sfx_None, MF_NOBLOCKMAP, S_NULL),
    };

    private MobjInfos() {
    }
}
