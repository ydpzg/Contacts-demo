package com.android.testcontact;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人的工具类, 查询，插入，删除
 */
public class ContactBaseUtils {

    public static String[] projection = new String[]{
            //获取联系人的ID
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            //获取联系人的姓名
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            //获取联系人的号码
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

    /**
     * 获取联系人数据
     *
     * @param context
     * @return
     */
    public static List<ContactBean> getContactsData(Context context) {
        Cursor cursor = null;
        String contactName;
        String contactNumber;
        ContentResolver resolver = context.getContentResolver();
        List<ContactBean> list = new ArrayList<>();
        //搜索字段
        try {
            cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    ContactBean bean = new ContactBean();
                    bean.setNumber(contactNumber.replaceAll(" ", "").trim());
                    bean.setName(contactName);
                    bean.setContactId(contactId);
                    list.add(bean);
                }
                Log.i("ContractUtils", "the size of contact list  is " + list.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    /**
     * 根据名字判断联系人是否存在
     *
     * @param context
     * @param name
     * @return
     */
    public static Boolean hasExistByName(Context context, String name) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        sb.append("=?");
        String selection = sb.toString();

        Cursor contactLookup = context.getContentResolver().query(uri, projection, selection, new String[]{name}, null);
        boolean result = false;
        if (contactLookup != null && contactLookup.getCount() > 0) {
            result = true;
        }
        if (contactLookup != null) {
            contactLookup.close();
        }

        return result;
    }

    /**
     * 根据手机号判断联系人是否存在
     *
     * @param context
     * @param number
     * @return
     */
    public static Boolean hasExistByNumber(Context context, String number) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.NUMBER);
        sb.append("=?");
        String selection = sb.toString();

        Cursor contactLookup = context.getContentResolver().query(uri, projection, selection, new String[]{number}, null);
        boolean result = false;
        if (contactLookup != null && contactLookup.getCount() > 0) {
            result = true;
        }
        if (contactLookup != null) {
            contactLookup.close();
        }

        return result;
    }

    /**
     * 根据手机号和名字判断联系人是否存在
     *
     * @param context
     * @param number
     * @param name
     * @return
     */
    public static Boolean hasExistByNumberAndName(Context context, String number, String name) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.NUMBER);
        sb.append("=?");
        sb.append(" and ");
        sb.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        sb.append("=?");
        String selection = sb.toString();

        Cursor contactLookup = context.getContentResolver().query(uri, projection, selection, new String[]{number, name}, null);
        boolean result = false;
        if (contactLookup != null && contactLookup.getCount() > 0) {
            result = true;
        }
        if (contactLookup != null) {
            contactLookup.close();
        }

        return result;
    }

    /**
     * 插入联系人列表
     *
     * @param context
     * @param list
     */
    public static void insertContact(Context context, List<ContactBean> list) {
        if (list == null) return;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex;
        for (int i = 0; i < list.size(); i++) {
            rawContactInsertIndex = ops.size();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, list.get(i).getName())
                    .withYieldAllowed(true)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, list.get(i).getNumber())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withYieldAllowed(true)
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据id删除联系人
     *
     * @param context
     * @param list
     */
    public static void deleteContactById(Context context, List<ContactBean> list) {
        if (list == null) return;
        ArrayList<ContentProviderOperation> ops = new ArrayList();

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        sb.append("=?");
        String selection = sb.toString();

        for (int i = 0; i < list.size(); i++) {
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selection, new String[]{String.valueOf(list.get(i).getContactId())})
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据手机号码删除联系人
     *
     * @param context
     * @param list
     */
    public static void deleteContactByNumber(Context context, List<ContactBean> list) {
        if (list == null) return;
        ArrayList<ContentProviderOperation> ops = new ArrayList();

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.NUMBER);
        sb.append("=?");
        String selection = sb.toString();

        for (int i = 0; i < list.size(); i++) {
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selection, new String[]{list.get(i).getNumber()})
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据名字删除联系人
     *
     * @param context
     * @param list
     */
    public static void deleteContactByName(Context context, List<ContactBean> list) {
        if (list == null) return;
        ArrayList<ContentProviderOperation> ops = new ArrayList();

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        sb.append("=?");
        String selection = sb.toString();

        for (int i = 0; i < list.size(); i++) {
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selection, new String[]{list.get(i).getName()})
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据手机号和名字删除联系人
     *
     * @param context
     * @param list
     */
    public static void deleteContactByNumberAndName(Context context, List<ContactBean> list) {
        if (list == null) return;
        ArrayList<ContentProviderOperation> ops = new ArrayList();

        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.CommonDataKinds.Phone.NUMBER);
        sb.append("=?");
        sb.append(" and ");
        sb.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        sb.append("=?");
        String selection = sb.toString();

        for (int i = 0; i < list.size(); i++) {
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selection, new String[]{list.get(i).getNumber(), list.get(i).getName()})
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


