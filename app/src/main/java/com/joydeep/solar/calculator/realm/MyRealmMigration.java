package com.joydeep.solar.calculator.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

class MyRealmMigration implements RealmMigration {

    private static final String TAG = "MyRealmMigration";

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
    }
}