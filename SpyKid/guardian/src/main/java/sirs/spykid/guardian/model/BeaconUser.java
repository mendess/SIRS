package sirs.spykid.guardian.model;

import sirs.spykid.util.ChildId;
import sirs.spykid.util.SharedKey;

public class BeaconUser {

    private SharedKey key;
    private ChildId child;

    public BeaconUser(SharedKey key, ChildId childId) {
        this.key = key;
        this.child = childId;
    }

    public SharedKey getKey() {
        return key;
    }

    public ChildId getChild() {
        return child;
    }
}
