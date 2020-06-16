package com.nemanja.arduino.model.dto;

public class Mods {

    private final Mod[] mods = new Mod[5];

    public Mods() {
        mods[0] = new Mod(0D, 0D, 0D, 0D, 0, 0);
        mods[1] = new Mod(20D, 22D, 90D, 95D, 4000, 6000);
        mods[2] = new Mod(14D, 18D, 90D, 95D, 4000, 6000);
        mods[3] = new Mod(16D, 18D, 90D, 95D, 1100, 1500);
        mods[4] = new Mod(0D, 0D, 0D, 0D, 0, 0);
    }

    public Mod getMod(int mod) {
        if ((mod >= 0) && (mod < 5)) {
            return mods[mod];
        } else {
            return mods[4];
        }

    }

}
