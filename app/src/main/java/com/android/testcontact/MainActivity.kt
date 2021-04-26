package com.android.testcontact

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions


/**
 * 需要权限,使用了权限库permissionsdispatcher
 */
@RuntimePermissions
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun readAction(v: View) {
        readContactWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun readContact() {
        val list = ContactBaseUtils.getContactsData(this)
        list.forEach {
            Log.i("ContractUtils", "${it.contactId}, ${it.name}, ${it.number}")
        }

        //val hasExist = ContactBaseUtils.hasExistByNumber(this, "123456789")
        //Log.i("hasExist", hasExist.toString())
    }

    fun writeAction(v: View) {
        writeContactWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_CONTACTS)
    fun writeContact() {
        ContactBaseUtils.insertContact(this, listOf(ContactBean().apply {
            number = "123456789"
            name = "john"
        }, ContactBean().apply {
            number = "9999999"
            name = "john"
        }, ContactBean().apply {
            number = "123456789"
            name = "yyy"
        }))
    }

    fun delAction(v: View) {
        delContactWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_CONTACTS)
    fun delContact() {
        ContactBaseUtils.deleteContactByNumberAndName(this, listOf(ContactBean().apply {
            number = "123456789"
            name = "john"
        }))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }
}
