package sirs.spykid.guardian.model;

import java.security.Key;

import javax.crypto.SecretKey;

import sirs.spykid.util.Child;
import sirs.spykid.util.ChildId;

public class BeaconUser {

    private Key key;
    private ChildId child;

    public BeaconUser() {

    }

    public BeaconUser(Key key, ChildId childId) {
        this.key = key;
        this.child = child;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public ChildId getChild() {
        return child;
    }

    public void setChildId(ChildId childId) {
        this.child = child;
    }
}
