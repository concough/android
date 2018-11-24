package com.concough.android.models;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.internal.Table;

/**
 * Created by abolfazl on 1/21/18.
 */

public class ModelMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema sessionSchema = realm.getSchema();

        if (oldVersion <= 0) {
            RealmObjectSchema sessionObjSchema = sessionSchema.get("EntranceModel");
            sessionObjSchema.removePrimaryKey();
            sessionObjSchema.addField("pUniqueId", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("pUniqueId", obj.getString("username") + "-" + obj.getString("uniqueId"));

                        }
                    });
            sessionObjSchema.addPrimaryKey("pUniqueId");
        }

        if (oldVersion <= 2) {
            RealmObjectSchema sessionObjSchema = sessionSchema.get("EntranceModel");
            sessionObjSchema.addField("month", int.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("month", 0);
                        }
                    });
        }

        if (oldVersion <= 6) {
//            RealmObjectSchema lessonExamModel = sessionSchema.create("EntranceLessonExamModel")
//                    .addField("uniqueId", String.class, FieldAttribute.PRIMARY_KEY)
//                    .addField("username", String.class)
//                    .addField("entranceUniqueId", String.class)
//                    .addField("lessonTitle", String.class)
//                    .addField("lessonOrder", int.class)
//                    .addField("bookletOrder", int.class)
//                    .addField("startedDate", Date.class)
//                    .addField("finishedDate", Date.class)
//                    .addField("withTime", boolean.class)
//                    .addField("questionCount", int.class)
//                    .addField("trueAnswer", int.class)
//                    .addField("falseAnswer", int.class)
//                    .addField("noAnswer", int.class)
//                    .addField("created", Date.class)
//                    .addField("examDuration", int.class)
//                    .addField("examData", String.class)
//                    .addField("percentage", double.class);

//            RealmObjectSchema questionExamStatModel = sessionSchema.create("EntranceQuestionExamStatModel")
//                    .addField("username", String.class)
//                    .addField("entranceUniqueId", String.class)
//                    .addField("questionNo", int.class)
//                    .addField("totalCount", int.class)
//                    .addField("trueCount", int.class)
//                    .addField("falseCount", int.class)
//                    .addField("emptyCount", int.class)
//                    .addField("created", Date.class)
//                    .addField("updated", Date.class)
//                    .addField("statData", String.class);
//
//            RealmObjectSchema questionCommentModel = sessionSchema.create("EntranceQuestionCommentModel")
//                    .addField("uniqueId", String.class, FieldAttribute.PRIMARY_KEY)
//                    .addField("username", String.class)
//                    .addField("entranceUniqueId", String.class)
//                    .addField("created", Date.class)
//                    .addField("commentType", String.class)
//                    .addField("commentData", String.class)
//                    .addField("question", EntranceQuestionModel.class);
        }
    }
}
