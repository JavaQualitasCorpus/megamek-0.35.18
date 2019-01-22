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
 * Created on Sep 7, 2005
 *
 */
package megamek.common.weapons.infantry;

import megamek.common.AmmoType;
import megamek.common.TechConstants;

/**
 * @author Ben Grills
 */
public class InfantrySupportHeavyAutoGrenadeLauncherWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantrySupportHeavyAutoGrenadeLauncherWeapon() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "Infantry Heavy Auto Grenade Launcher";
        setInternalName(name);
        addLookupName("InfantryHeavyAutoGrenadeLauncher");
        ammoType = AmmoType.T_NA;
        cost = 4500;
        bv = 2.85;
        flags = flags.or(F_NO_FIRES).or(F_BALLISTIC).or(F_INF_ENCUMBER).or(F_INF_SUPPORT);
        infantryDamage = 1.16;
        infantryRange = 1;
        crew = 1;
    }
}