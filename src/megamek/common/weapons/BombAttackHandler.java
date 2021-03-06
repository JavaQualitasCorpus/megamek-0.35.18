/**
 * MegaMek - Copyright (C) 2004,2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
/*
 * Created on Sep 23, 2004
 *
 */
package megamek.common.weapons;

import java.util.Vector;

import megamek.common.Aero;
import megamek.common.BombType;
import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.HitData;
import megamek.common.IGame;
import megamek.common.Mounted;
import megamek.common.Report;
import megamek.common.TargetRoll;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.server.Server;

/**
 * @author Jay Lawson
 */
public class BombAttackHandler extends WeaponHandler {


    /**
     *
     */
    private static final long serialVersionUID = -2997052348538688888L;
    /**
     * @param toHit
     * @param waa
     * @param g
     */
    public BombAttackHandler(ToHitData toHit, WeaponAttackAction waa, IGame g,
            Server s) {
        super(toHit, waa, g, s);
        generalDamageType = HitData.DAMAGE_NONE;
    }

    /**
     * Does this attack use the cluster hit table?
     * necessary to determine how Aero damage should be applied
     */
    @Override
    protected boolean usesClusterTable() {
        return true;
    }

    @Override
    protected void useAmmo() {
        int[] payload = waa.getBombPayload();
        if(!(ae instanceof Aero) || (null == payload)) {
            return;
        }
        for(int type = 0; type < payload.length; type++) {
            for(int i = 0; i < payload[type]; i++) {
                //find the first mounted bomb of this type and drop it
                for(Mounted bomb : ae.getBombs()) {
                    if(!bomb.isDestroyed() && (bomb.getShotsLeft() > 0)
                           && (((BombType)bomb.getType()).getBombType() == type)) {
                        bomb.setShotsLeft(0);
                        break;
                    }
                }
            }
        }
        super.useAmmo();
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.weapons.AttackHandler#handle(int, java.util.Vector)
     */
    @Override
    public boolean handle(IGame.Phase phase, Vector<Report> vPhaseReport) {
        //Report weapon attack and its to-hit value.
        Report r = new Report(3120);
        r.indent();
        r.newlines = 0;
        r.subject = subjectId;
        if (wtype != null) {
            r.add(wtype.getName());
        } else {
            r.add("Error: From Nowhwere");
        }

        r.add(target.getDisplayName(), true);
        vPhaseReport.addElement(r);
        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(3135);
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
            return false;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_FAIL) {
            r = new Report(3140);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(3145);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getDesc());
            vPhaseReport.addElement(r);
        } else {
            // roll to hit
            r = new Report(3150);
            r.newlines = 0;
            r.subject = subjectId;
            r.add(toHit.getValue());
            vPhaseReport.addElement(r);
        }

        // dice have been rolled, thanks
        r = new Report(3155);
        r.newlines = 0;
        r.subject = subjectId;
        r.add(roll);
        vPhaseReport.addElement(r);

        // do we hit?
        bMissed = roll < toHit.getValue();
        // Set Margin of Success/Failure.
        toHit.setMoS(roll-Math.max(2,toHit.getValue()));

        Coords coords = target.getPosition();
        if (!bMissed) {
            r = new Report(3190);
            r.subject = subjectId;
            r.add(coords.getBoardNum());
            vPhaseReport.addElement(r);
        } else {
            r = new Report(3196);
            r.subject = subjectId;
            r.add(coords.getBoardNum());
            vPhaseReport.addElement(r);
        }

        //now go through the payload and drop the bombs one at a time
        int[] payload = waa.getBombPayload();
        Coords drop = coords;
        for(int type = 0; type < payload.length; type++) {
            for(int i = 0; i < payload[type]; i++) {
                drop = coords;
                //each bomb can scatter a different direction
                if(!bMissed) {
                    r = new Report(6697);
                    r.indent(1);
                    r.add(BombType.getBombName(type));
                    r.subject = subjectId;
                    r.newlines = 1;
                    vPhaseReport.add(r);
                } else {
                    drop = Compute.scatterDiveBombs(coords);
                    if (game.getBoard().contains(drop)) {
                        // misses and scatters to another hex
                        r = new Report(6698);
                        r.indent(1);
                        r.subject = subjectId;
                        r.newlines = 1;
                        r.add(BombType.getBombName(type));
                        r.add(drop.getBoardNum());
                        vPhaseReport.addElement(r);
                    } else {
                        // misses and scatters off-board
                        r = new Report(6699);
                        r.indent(1);
                        r.subject = subjectId;
                        r.newlines = 1;
                        r.add(BombType.getBombName(type));
                        vPhaseReport.addElement(r);
                        continue;
                    }
                }
                if(type == BombType.B_INFERNO) {
                    server.deliverBombInferno(drop, ae, subjectId, vPhaseReport);
                } else if (type == BombType.B_THUNDER) {
                    server.deliverThunderMinefield(drop, subjectId, 20, ae.getId());
                } else {

                    server.deliverBombDamage(drop, type, subjectId, ae, vPhaseReport);
                }
            }
        }

        return false;
    }
}
