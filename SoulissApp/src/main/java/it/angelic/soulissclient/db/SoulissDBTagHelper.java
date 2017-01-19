package it.angelic.soulissclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.SoulissTypicalFactory;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBTagHelper extends SoulissDBHelper {

    public SoulissDBTagHelper(Context context) {
        super(context);
    }

    // Database fields


    public static synchronized SQLiteDatabase getDatabase() {
        return database;
    }


    public List<SoulissTag> getTags(Context context) {
        List<SoulissTag> comments = new ArrayList<>();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                null, null, null, null, SoulissDB.COLUMN_TAG_ORDER +", "+SoulissDB.COLUMN_TAG_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTag dto = new SoulissTag();
            dto.setTagId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID)));
            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setTagOrder(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ORDER)));
            dto.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            Log.i(Constants.TAG, "retrieving TAG:" + dto.getTagId()+" ORDER:"+dto.getTagOrder());
            dto.setAssignedTypicals(getTagTypicals(dto));
            comments.add(dto);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    public SoulissTag getTag(long tagId) throws SQLDataException {
        SoulissTag dto = new SoulissTag();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                SoulissDB.COLUMN_TAG_ID + " = " + tagId, null, null, null, null);
        if(cursor.isLast())
            throw new SQLDataException("Non Existing TagId:"+tagId);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dto.setTagOrder(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ORDER)));
            dto.setTagId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID)));
            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            Log.i(Constants.TAG, "retrieving TAG:" + dto.getTagId() + " ORDER:" + dto.getTagOrder());
            dto.setAssignedTypicals(getTagTypicals(dto));
            cursor.moveToNext();
        }
        cursor.close();
        return dto;
    }

    public int countFavourites() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS_TYPICALS + " where "
                + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " =  " + SoulissDB.FAVOURITES_TAG_ID, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTypicalTags() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS_TYPICALS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTags() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countScenes() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_SCENES, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTriggers() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TRIGGERS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public List<SoulissTypical> getFavouriteTypicals() {

        SoulissTag fake = new SoulissTag();
        fake.setTagId(0);
        return getTagTypicals(fake);
    }

    /**
     * Crea un nuovo scenario vuoto
     *
     * @param tagIN
     * @return
     */
    public long createOrUpdateTag(SoulissTag tagIN) {
        ContentValues values = new ContentValues();
        long ret = -1;
        if (tagIN != null) {
            values.put(SoulissDB.COLUMN_TAG_NAME, tagIN.getName());
            values.put(SoulissDB.COLUMN_TAG_ICONID, tagIN.getIconResourceId());
            values.put(SoulissDB.COLUMN_TAG_ORDER, tagIN.getTagOrder());
            values.put(SoulissDB.COLUMN_TAG_IMGPTH, tagIN.getImagePath());

            ret = database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + tagIN.getTagId(),
                    null);
            Log.i(Constants.TAG, "UPD TAG " + tagIN.getTagId() + " just updated rows:" + ret);

            List<SoulissTypical> typs = tagIN.getAssignedTypicals();
            for (SoulissTypical nowT : typs) {
                createOrUpdateTagTypicalNode(nowT, tagIN, 0);
                Log.i(Constants.TAG, "INSERTED TAG->TYP" + nowT.toString() + " TO " + tagIN.getNiceName());
            }
            return ret;
        } else {//brand new
            values.put(SoulissDB.COLUMN_TAG_ICONID, 0);
            // Inserisco e risetto il nome e l'ordine
            ret = (int) database.insert(SoulissDB.TABLE_TAGS, null, values);
            values.put(SoulissDB.COLUMN_TAG_NAME,
                    SoulissApp.getAppContext().getResources().getString(R.string.tag) + " " + ret);
            values.put(SoulissDB.COLUMN_TAG_ORDER, ret);
            database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + ret, null);
            return ret;
        }

    }



    /**
     * relazione coi typ
     *
     * @param nodeIN
     * @return
     */
    public int createOrUpdateTagTypicalNode(SoulissTypical nodeIN, SoulissTag toAssoc, Integer priority) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_TAG_TYP_NODE_ID, nodeIN.getNodeId());
        values.put(SoulissDB.COLUMN_TAG_TYP_SLOT, nodeIN.getSlot());
        values.put(SoulissDB.COLUMN_TAG_TYP_TAG_ID, toAssoc.getTagId());
        values.put(SoulissDB.COLUMN_TAG_TYP_PRIORITY, priority);
        int upd = database.update(SoulissDB.TABLE_TAGS_TYPICALS, values, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot() + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toAssoc.getTagId(),
                null);
        if (upd == 0) {
            long insertId = database.insert(SoulissDB.TABLE_TAGS_TYPICALS, null, values);
        }

        return upd;
    }

    /**
     * relazione coi typ
     *
     * @param nodeIN
     * @return
     */
    public int deleteTagTypicalNode(SoulissTypical nodeIN, SoulissTag toAssoc) {

        int upd = database.delete(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot()
                + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toAssoc.getTagId(), null);
        Log.w(Constants.TAG, "DELETE TAG->TYP" + nodeIN.toString() + " TO " + toAssoc.getNiceName());
        return upd;
    }
    public List<SoulissTag> getTagsByTypicals(SoulissTypical parent) {

        List<SoulissTag> comments = new ArrayList<>();
        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS + " a "
                + " WHERE a." + SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + parent.getNodeId()
                + " AND a." + SoulissDB.COLUMN_TAG_TYP_SLOT + " =  " + parent.getSlot();
        Cursor cursor = database.rawQuery(MY_QUERY, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int tagId = cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_TYP_TAG_ID));
            try {
                SoulissTag newTag = getTag(tagId);
                if (!comments.contains(newTag))
                    comments.add(newTag);
            } catch (SQLDataException e) {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<SoulissTypical> getTagTypicals(SoulissTag parent) {

        List<SoulissTypical> comments = new ArrayList<>();
        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS + " a "
                + " INNER JOIN " + SoulissDB.TABLE_TYPICALS + " b "
                + " ON a." + SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = b." + SoulissDB.COLUMN_TYPICAL_NODE_ID
                + " AND a." + SoulissDB.COLUMN_TAG_TYP_SLOT + " = b." + SoulissDB.COLUMN_TYPICAL_SLOT
                + " WHERE a." + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + parent.getTagId();
        Cursor cursor = database.rawQuery(MY_QUERY, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissNode par = getSoulissNode(dto.getNodeId());
            SoulissTypical newTyp = SoulissTypicalFactory.getTypical(dto.getTypical(), par, dto, opts);
            //hack dto ID, could be different if parent is massive
            newTyp.getTypicalDTO().setNodeId(dto.getNodeId());

            //Se e` qui, e` taggato
            if (parent.getTagId() == 0)
                newTyp.getTypicalDTO().setFavourite(true);
            newTyp.getTypicalDTO().setTagged(true);
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }
}
