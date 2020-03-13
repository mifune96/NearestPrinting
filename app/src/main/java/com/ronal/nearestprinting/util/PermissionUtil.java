package com.ronal.nearestprinting.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

    /*
    *   Class untuk meminta permissions kepada user
     */

public class PermissionUtil {

    private Context context;
    private Activity activity;

    PermissionResultCallback permissionResultCallback;

    private ArrayList<String> permissionList = new ArrayList<>();
    private ArrayList<String> listPermissionNeeded = new ArrayList<>();

    private String dialogContent = "";
    private int REQUEST_CODE;

    public PermissionUtil(Context context){
        this.context = context;
        this.activity = (Activity) context;

        permissionResultCallback = (PermissionResultCallback) context;
    }

    public PermissionUtil(Context context, PermissionResultCallback permissionResultCallback) {
        this.context = context;
        this.activity = (Activity) context;

        this.permissionResultCallback = permissionResultCallback;
    }

    /**
     * Check the API Level & Permission
     *
     * @param permissions
     * @param dialogContent
     * @param REQUEST_CODE
     */

    public void CheckPermission(ArrayList<String> permissions, String dialogContent,
                                int REQUEST_CODE){
        this.permissionList = permissions;
        this.dialogContent = dialogContent;
        this.REQUEST_CODE = REQUEST_CODE;

        if (Build.VERSION.SDK_INT >= 23){
            if (checkAndPermissions(permissions, REQUEST_CODE)){
                permissionResultCallback.PermissionGranted(REQUEST_CODE);
                Log.i("All Permissions", "granted");
                Log.i("proceed", "to callback");
            }
        } else {
            permissionResultCallback.PermissionGranted(REQUEST_CODE);
            Log.i("All Permissions", "granted");
            Log.i("proceed", "to callback");
        }
    }

    /**
     * Check and request the permissions
     *
     * @param permissions
     * @param request_code
     * @return
     */

    private boolean checkAndPermissions(ArrayList<String> permissions, int request_code) {
        if (permissions.size() > 0){
            listPermissionNeeded = new ArrayList<>();

            for (int i = 0; i < permissions.size(); i++){
                int hasPermission = ContextCompat.checkSelfPermission(activity, permissions.get(i));

                if (hasPermission != PackageManager.PERMISSION_GRANTED){
                    listPermissionNeeded.add(permissions.get(i));
                }
            }

            if (!listPermissionNeeded.isEmpty()){
                ActivityCompat.requestPermissions(activity, listPermissionNeeded.toArray(new String[
                        listPermissionNeeded.size()]), request_code);
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param requestCode
     * @param permission
     * @param grantResults
     */

    public void onRequestPermissionResult(int requestCode, String[] permission, int[] grantResults){
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                Map<String, Integer> perms = new HashMap<>();

                for (int i = 0; i < permission.length; i++) {
                    perms.put(permission[i], grantResults[i]);
                }

                final ArrayList<String> pending_permissions = new ArrayList<>();

                for (int i = 0; i < listPermissionNeeded.size(); i++) {
                    if (perms.get(listPermissionNeeded.get(i)) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                listPermissionNeeded.get(i))) {
                            pending_permissions.add(listPermissionNeeded.get(i));
                        } else {
                            Log.i("Go to settings", "and enable permissions");
                            permissionResultCallback.NeverAskAgain(REQUEST_CODE);
                            Toast.makeText(activity, "Go to setting and enable permissions"
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                if (pending_permissions.size() > 0) {
                    showMessageOKCancel(dialogContent,
                            (dialog, which) -> {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        CheckPermission(permissionList, dialogContent, REQUEST_CODE);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Log.i("permission", "not fully given");
                                        if (permissionList.size() == pending_permissions.size()) {
                                            permissionResultCallback.PermissionDenied(REQUEST_CODE);
                                        } else {
                                            permissionResultCallback.PartialPermissionGranted(REQUEST_CODE,
                                                    pending_permissions);
                                        }
                                        break;
                                }
                            });
                } else {
                    Log.i("all", "permission granted");
                    Log.i("proceed", "to next step");
                    permissionResultCallback.PermissionGranted(REQUEST_CODE);
                }
            }
        }
    }

    /**
     *
     *
     * @param dialogContent
     * @param onClickListener
     */

    private void showMessageOKCancel(String dialogContent, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(activity)
                .setMessage(dialogContent)
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .create().show();
    }


    public interface PermissionResultCallback{
        void PermissionGranted(int REQUEST_CODE);
        void PartialPermissionGranted(int REQUEST_CODE, ArrayList<String> GRANTED_PERMISSIONS);
        void PermissionDenied(int REQUEST_CODE);
        void NeverAskAgain(int REQUEST_CODE);
    }
}
