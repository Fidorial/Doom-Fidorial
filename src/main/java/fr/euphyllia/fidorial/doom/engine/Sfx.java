// Generated from id Software's linuxdoom-1.10 sources by gen_info.py - do not edit.
// Copyright (C) 1993-1996 by id Software, Inc. Licensed under the GNU GPL v2 or later.
package fr.euphyllia.fidorial.doom.engine;


/**
 * Sound effect numbers and their {@code sfxinfo_t} data.
 */
public final class Sfx {

    public static final int sfx_None = 0;
    public static final int sfx_pistol = 1;
    public static final int sfx_shotgn = 2;
    public static final int sfx_sgcock = 3;
    public static final int sfx_dshtgn = 4;
    public static final int sfx_dbopn = 5;
    public static final int sfx_dbcls = 6;
    public static final int sfx_dbload = 7;
    public static final int sfx_plasma = 8;
    public static final int sfx_bfg = 9;
    public static final int sfx_sawup = 10;
    public static final int sfx_sawidl = 11;
    public static final int sfx_sawful = 12;
    public static final int sfx_sawhit = 13;
    public static final int sfx_rlaunc = 14;
    public static final int sfx_rxplod = 15;
    public static final int sfx_firsht = 16;
    public static final int sfx_firxpl = 17;
    public static final int sfx_pstart = 18;
    public static final int sfx_pstop = 19;
    public static final int sfx_doropn = 20;
    public static final int sfx_dorcls = 21;
    public static final int sfx_stnmov = 22;
    public static final int sfx_swtchn = 23;
    public static final int sfx_swtchx = 24;
    public static final int sfx_plpain = 25;
    public static final int sfx_dmpain = 26;
    public static final int sfx_popain = 27;
    public static final int sfx_vipain = 28;
    public static final int sfx_mnpain = 29;
    public static final int sfx_pepain = 30;
    public static final int sfx_slop = 31;
    public static final int sfx_itemup = 32;
    public static final int sfx_wpnup = 33;
    public static final int sfx_oof = 34;
    public static final int sfx_telept = 35;
    public static final int sfx_posit1 = 36;
    public static final int sfx_posit2 = 37;
    public static final int sfx_posit3 = 38;
    public static final int sfx_bgsit1 = 39;
    public static final int sfx_bgsit2 = 40;
    public static final int sfx_sgtsit = 41;
    public static final int sfx_cacsit = 42;
    public static final int sfx_brssit = 43;
    public static final int sfx_cybsit = 44;
    public static final int sfx_spisit = 45;
    public static final int sfx_bspsit = 46;
    public static final int sfx_kntsit = 47;
    public static final int sfx_vilsit = 48;
    public static final int sfx_mansit = 49;
    public static final int sfx_pesit = 50;
    public static final int sfx_sklatk = 51;
    public static final int sfx_sgtatk = 52;
    public static final int sfx_skepch = 53;
    public static final int sfx_vilatk = 54;
    public static final int sfx_claw = 55;
    public static final int sfx_skeswg = 56;
    public static final int sfx_pldeth = 57;
    public static final int sfx_pdiehi = 58;
    public static final int sfx_podth1 = 59;
    public static final int sfx_podth2 = 60;
    public static final int sfx_podth3 = 61;
    public static final int sfx_bgdth1 = 62;
    public static final int sfx_bgdth2 = 63;
    public static final int sfx_sgtdth = 64;
    public static final int sfx_cacdth = 65;
    public static final int sfx_skldth = 66;
    public static final int sfx_brsdth = 67;
    public static final int sfx_cybdth = 68;
    public static final int sfx_spidth = 69;
    public static final int sfx_bspdth = 70;
    public static final int sfx_vildth = 71;
    public static final int sfx_kntdth = 72;
    public static final int sfx_pedth = 73;
    public static final int sfx_skedth = 74;
    public static final int sfx_posact = 75;
    public static final int sfx_bgact = 76;
    public static final int sfx_dmact = 77;
    public static final int sfx_bspact = 78;
    public static final int sfx_bspwlk = 79;
    public static final int sfx_vilact = 80;
    public static final int sfx_noway = 81;
    public static final int sfx_barexp = 82;
    public static final int sfx_punch = 83;
    public static final int sfx_hoof = 84;
    public static final int sfx_metal = 85;
    public static final int sfx_chgun = 86;
    public static final int sfx_tink = 87;
    public static final int sfx_bdopn = 88;
    public static final int sfx_bdcls = 89;
    public static final int sfx_itmbk = 90;
    public static final int sfx_flame = 91;
    public static final int sfx_flamst = 92;
    public static final int sfx_getpow = 93;
    public static final int sfx_bospit = 94;
    public static final int sfx_boscub = 95;
    public static final int sfx_bossit = 96;
    public static final int sfx_bospn = 97;
    public static final int sfx_bosdth = 98;
    public static final int sfx_manatk = 99;
    public static final int sfx_mandth = 100;
    public static final int sfx_sssit = 101;
    public static final int sfx_ssdth = 102;
    public static final int sfx_keenpn = 103;
    public static final int sfx_keendt = 104;
    public static final int sfx_skeact = 105;
    public static final int sfx_skesit = 106;
    public static final int sfx_skeatk = 107;
    public static final int sfx_radio = 108;
    public static final int NUMSFX = 109;

    /**
     * Lump name suffix ({@code DS<name>}).
     */
    public static final String[] NAME = {"none", "pistol", "shotgn", "sgcock", "dshtgn", "dbopn", "dbcls", "dbload", "plasma", "bfg", "sawup", "sawidl", "sawful", "sawhit", "rlaunc", "rxplod", "firsht", "firxpl", "pstart", "pstop", "doropn", "dorcls", "stnmov", "swtchn", "swtchx", "plpain", "dmpain", "popain", "vipain", "mnpain", "pepain", "slop", "itemup", "wpnup", "oof", "telept", "posit1", "posit2", "posit3", "bgsit1", "bgsit2", "sgtsit", "cacsit", "brssit", "cybsit", "spisit", "bspsit", "kntsit", "vilsit", "mansit", "pesit", "sklatk", "sgtatk", "skepch", "vilatk", "claw", "skeswg", "pldeth", "pdiehi", "podth1", "podth2", "podth3", "bgdth1", "bgdth2", "sgtdth", "cacdth", "skldth", "brsdth", "cybdth", "spidth", "bspdth", "vildth", "kntdth", "pedth", "skedth", "posact", "bgact", "dmact", "bspact", "bspwlk", "vilact", "noway", "barexp", "punch", "hoof", "metal", "chgun", "tink", "bdopn", "bdcls", "itmbk", "flame", "flamst", "getpow", "bospit", "boscub", "bossit", "bospn", "bosdth", "manatk", "mandth", "sssit", "ssdth", "keenpn", "keendt", "skeact", "skesit", "skeatk", "radio"};
    /**
     * Whether only one instance may play at a time.
     */
    public static final boolean[] SINGULARITY = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    /**
     * Channel priority, lower is more important.
     */
    public static final int[] PRIORITY = {0, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 118, 64, 64, 64, 70, 70, 70, 100, 100, 100, 100, 119, 78, 78, 96, 96, 96, 96, 96, 96, 78, 78, 78, 96, 32, 98, 98, 98, 98, 98, 98, 98, 94, 92, 90, 90, 90, 90, 90, 90, 70, 70, 70, 70, 70, 70, 32, 32, 70, 70, 70, 70, 70, 70, 70, 70, 32, 32, 32, 32, 32, 32, 32, 32, 120, 120, 120, 100, 100, 100, 78, 60, 64, 70, 70, 64, 60, 100, 100, 100, 32, 32, 60, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 60};
    /**
     * Sound this one is an alias of, or -1.
     */
    public static final int[] LINK = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, sfx_pistol, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    /**
     * Pitch override for linked sounds, or -1.
     */
    public static final int[] PITCH = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 150, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    /**
     * Volume override for linked sounds, or -1.
     */
    public static final int[] VOLUME = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private Sfx() {
    }
}
