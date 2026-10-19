// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.PspDef;
import fr.euphyllia.fidorial.doom.engine.info.Action;

/**
 * Calls the action function a state names, as the {@code actionf_t} pointers do in C.
 */
public final class Actions {

    private final Game g;

    public Actions(final Game g) {
        this.g = g;
    }

    /**
     * Runs a map object action ({@code acp1}).
     */
    public void callMobj(final Action action, final Mobj mo) {
        switch (action) {
            case A_BFGSpray -> g.pspr.A_BFGSpray(mo);
            case A_Explode -> g.enemy.A_Explode(mo);
            case A_Pain -> g.enemy.A_Pain(mo);
            case A_PlayerScream -> g.enemy.A_PlayerScream(mo);
            case A_Fall -> g.enemy.A_Fall(mo);
            case A_XScream -> g.enemy.A_XScream(mo);
            case A_Look -> g.enemy.A_Look(mo);
            case A_Chase -> g.enemy.A_Chase(mo);
            case A_FaceTarget -> g.enemy.A_FaceTarget(mo);
            case A_PosAttack -> g.enemy.A_PosAttack(mo);
            case A_Scream -> g.enemy.A_Scream(mo);
            case A_SPosAttack -> g.enemy.A_SPosAttack(mo);
            case A_VileChase -> g.enemy.A_VileChase(mo);
            case A_VileStart -> g.enemy.A_VileStart(mo);
            case A_VileTarget -> g.enemy.A_VileTarget(mo);
            case A_VileAttack -> g.enemy.A_VileAttack(mo);
            case A_StartFire -> g.enemy.A_StartFire(mo);
            case A_Fire -> g.enemy.A_Fire(mo);
            case A_FireCrackle -> g.enemy.A_FireCrackle(mo);
            case A_Tracer -> g.enemy.A_Tracer(mo);
            case A_SkelWhoosh -> g.enemy.A_SkelWhoosh(mo);
            case A_SkelFist -> g.enemy.A_SkelFist(mo);
            case A_SkelMissile -> g.enemy.A_SkelMissile(mo);
            case A_FatRaise -> g.enemy.A_FatRaise(mo);
            case A_FatAttack1 -> g.enemy.A_FatAttack1(mo);
            case A_FatAttack2 -> g.enemy.A_FatAttack2(mo);
            case A_FatAttack3 -> g.enemy.A_FatAttack3(mo);
            case A_BossDeath -> g.enemy.A_BossDeath(mo);
            case A_CPosAttack -> g.enemy.A_CPosAttack(mo);
            case A_CPosRefire -> g.enemy.A_CPosRefire(mo);
            case A_TroopAttack -> g.enemy.A_TroopAttack(mo);
            case A_SargAttack -> g.enemy.A_SargAttack(mo);
            case A_HeadAttack -> g.enemy.A_HeadAttack(mo);
            case A_BruisAttack -> g.enemy.A_BruisAttack(mo);
            case A_SkullAttack -> g.enemy.A_SkullAttack(mo);
            case A_Metal -> g.enemy.A_Metal(mo);
            case A_SpidRefire -> g.enemy.A_SpidRefire(mo);
            case A_BabyMetal -> g.enemy.A_BabyMetal(mo);
            case A_BspiAttack -> g.enemy.A_BspiAttack(mo);
            case A_Hoof -> g.enemy.A_Hoof(mo);
            case A_CyberAttack -> g.enemy.A_CyberAttack(mo);
            case A_PainAttack -> g.enemy.A_PainAttack(mo);
            case A_PainDie -> g.enemy.A_PainDie(mo);
            case A_KeenDie -> g.enemy.A_KeenDie(mo);
            case A_BrainPain -> g.enemy.A_BrainPain(mo);
            case A_BrainScream -> g.enemy.A_BrainScream(mo);
            case A_BrainDie -> g.enemy.A_BrainDie(mo);
            case A_BrainAwake -> g.enemy.A_BrainAwake(mo);
            case A_BrainSpit -> g.enemy.A_BrainSpit(mo);
            case A_SpawnSound -> g.enemy.A_SpawnSound(mo);
            case A_SpawnFly -> g.enemy.A_SpawnFly(mo);
            case A_BrainExplode -> g.enemy.A_BrainExplode(mo);
            default -> {
                // a weapon action on a map object: vanilla would pass garbage, skip it
            }
        }
    }

    /**
     * Runs a player sprite action ({@code acp2}).
     */
    public void callPlayer(final Action action, final Player player, final PspDef psp) {
        switch (action) {
            case A_Light0 -> g.pspr.A_Light0(player, psp);
            case A_WeaponReady -> g.pspr.A_WeaponReady(player, psp);
            case A_Lower -> g.pspr.A_Lower(player, psp);
            case A_Raise -> g.pspr.A_Raise(player, psp);
            case A_Punch -> g.pspr.A_Punch(player, psp);
            case A_ReFire -> g.pspr.A_ReFire(player, psp);
            case A_FirePistol -> g.pspr.A_FirePistol(player, psp);
            case A_Light1 -> g.pspr.A_Light1(player, psp);
            case A_FireShotgun -> g.pspr.A_FireShotgun(player, psp);
            case A_Light2 -> g.pspr.A_Light2(player, psp);
            case A_FireShotgun2 -> g.pspr.A_FireShotgun2(player, psp);
            case A_CheckReload -> g.pspr.A_CheckReload(player, psp);
            case A_OpenShotgun2 -> g.enemy.A_OpenShotgun2(player, psp);
            case A_LoadShotgun2 -> g.enemy.A_LoadShotgun2(player, psp);
            case A_CloseShotgun2 -> g.enemy.A_CloseShotgun2(player, psp);
            case A_FireCGun -> g.pspr.A_FireCGun(player, psp);
            case A_GunFlash -> g.pspr.A_GunFlash(player, psp);
            case A_FireMissile -> g.pspr.A_FireMissile(player, psp);
            case A_Saw -> g.pspr.A_Saw(player, psp);
            case A_FirePlasma -> g.pspr.A_FirePlasma(player, psp);
            case A_BFGsound -> g.pspr.A_BFGsound(player, psp);
            case A_FireBFG -> g.pspr.A_FireBFG(player, psp);
            default -> {
            }
        }
    }
}
