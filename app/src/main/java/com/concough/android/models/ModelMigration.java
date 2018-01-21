package com.concough.android.models;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by abolfazl on 1/21/18.
 */

public class ModelMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema sessionSchema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema sessionObjSchema = sessionSchema.get("EntranceModel");
            sessionObjSchema.removePrimaryKey();
            sessionObjSchema.addField("pUniqueId", String.class, FieldAttribute.PRIMARY_KEY)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("pUinqueId", obj.getString("username") + "-" + obj.getString("uniqueId"));
                        }
                    });

            oldVersion++;
        }
    }
}
