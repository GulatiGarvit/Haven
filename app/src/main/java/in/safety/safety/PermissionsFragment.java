package in.safety.safety;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PermissionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PermissionsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int GPS_REQUEST_CODE = 101;
    private static final int CAM_REQUEST_CODE = 102;
    private static final int PERMISSION_OVERLAY = 104;
    private static final int AUTOSTART = 105;
    private static final int STORAGE_REQ_CODE = 103;
    private SharedPreferences permissionManager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;

    private ImageView permissionIllustration;
    private TextView permissionHeader, permissionBody;
    private Button allowButton, denyButton;

    public void setOnPermissionGrantedListener(onPermissionsGrantedListener listener) {
        this.listener = listener;
    }

    public interface onPermissionsGrantedListener
    {
        void granted();
    }
    private onPermissionsGrantedListener listener;

    public PermissionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PermissionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PermissionsFragment newInstance(String param1, String param2) {
        PermissionsFragment fragment = new PermissionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_permissions, container, false);
        permissionManager = getActivity().getSharedPreferences("permissionManager", Context.MODE_PRIVATE);
        if(permissionManager.getBoolean("askedForPermissions", false))
        {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
        permissionIllustration = view.findViewById(R.id.permission_illustration);
        permissionHeader = view.findViewById(R.id.permission_title);
        permissionBody = view.findViewById(R.id.permission_body);
        allowButton = view.findViewById(R.id.allow_permission);
        denyButton = view.findViewById(R.id.deny_permission);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "The app needs appropriate permissions to function properly.", Toast.LENGTH_SHORT).show();
            }
        });
        getPermission(0);
        return view;
    }

    private void getPermission(int i) {
        switch (i)
        {
            case 0:
                getGPS();
                break;
            case 1:
                getCamera();
                break;
            case 2:
                getStorage();
                break;
            case 3:
                getSystemOverlay();
                break;
            case 4:
                getAutostart();
                break;
        }
    }

    private void getStorage() {
        fadeOut();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                permissionIllustration.setImageDrawable(getResources().getDrawable(R.drawable.illustration_storage));
                permissionHeader.setText(getResources().getString(R.string.storage_header));
                permissionBody.setText(getResources().getString(R.string.storage_body));
                fadeIn(0, true);
            }
        }, 600);
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQ_CODE);
            }
        });
    }

    private void getAutostart() {
        //this will open auto start screen where user can enable permission for your app
        Intent intent1 = new Intent();
        intent1.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        startActivityForResult(intent1, AUTOSTART);
    }

    private void getSystemOverlay() {
        fadeOut();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    permissionHeader.setText("Draw over apps");
                    permissionBody.setText("Please allow us to draw over other apps. This ensures us to provide you the best level of security in case of emergencies and avoid unwanted apps occupying your screen when you are in danger.");
                    fadeIn(0, false);
                }
            },600);
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Settings.canDrawOverlays(getActivity())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                        startActivityForResult(intent, PERMISSION_OVERLAY);
                    }
                    else
                    {
                        String manufacturer = "xiaomi";
                        if (manufacturer.equalsIgnoreCase(Build.MANUFACTURER)) {
                            getPermission(4);
                        } else {
                            permissionManager.edit().putBoolean("askedForPermissions", true).apply();
                            listener.granted();
                        }
                    }
                }
            });
        }
        else
        {
            permissionManager.edit().putBoolean("askedForPermissions", true).apply();
            listener.granted();
        }
    }

    private void getCamera() {
        fadeOut();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                permissionIllustration.setImageDrawable(getResources().getDrawable(R.drawable.illustration_cam));
                permissionHeader.setText(getResources().getString(R.string.cam_header));
                permissionBody.setText(getResources().getString(R.string.cam_body));
                fadeIn(0, true);
            }
        }, 600);
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAM_REQUEST_CODE);
            }
        });
    }

    private void fadeOut()
    {
        YoYo.with(Techniques.FadeOut)
                .duration(600)
                .playOn(permissionIllustration);
        YoYo.with(Techniques.FadeOut)
                .duration(600)
                .playOn(permissionHeader);
        YoYo.with(Techniques.FadeOut)
                .duration(600)
                .playOn(permissionBody);
    }

    private void fadeIn(long delay, boolean image)
    {
        if(image)
            YoYo.with(Techniques.FadeIn)
                    .duration(600)
                    .delay(delay)
                    .playOn(permissionIllustration);
        YoYo.with(Techniques.FadeIn)
                .duration(600)
                .delay(delay)
                .playOn(permissionHeader);
        YoYo.with(Techniques.FadeIn)
                .duration(600)
                .delay(delay)
                .playOn(permissionBody);
    }

    private void getGPS()
    {
        permissionIllustration.setImageDrawable(getResources().getDrawable(R.drawable.illustration_gps));
        permissionHeader.setText(getResources().getString(R.string.gps_header));
        permissionBody.setText(getResources().getString(R.string.gps_body));
        fadeIn(0, true);
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
                } else
                {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 101:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionManager.edit().putBoolean("gps", true).apply();
                    getPermission(1);
                }
                else {
                    permissionManager.edit().putBoolean("gps", false).apply();
                    denyButton.callOnClick();
                }
                break;
            case 102:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionManager.edit().putBoolean("camera", true).apply();
                    getPermission(2);
                }
                else {
                    permissionManager.edit().putBoolean("camera", false).apply();
                    denyButton.callOnClick();
                }
                break;
            case 103:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionManager.edit().putBoolean("storage", true).apply();
                    getPermission(3);
                }
                else {
                    permissionManager.edit().putBoolean("storage", false).apply();
                    denyButton.callOnClick();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERMISSION_OVERLAY)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!Settings.canDrawOverlays(getActivity()))
                    Snackbar.make(view, "Please grant the permission", Snackbar.LENGTH_SHORT).show();
                else {
                    String manufacturer = "xiaomi";
                    if (manufacturer.equalsIgnoreCase(Build.MANUFACTURER)) {
                        getPermission(4);
                    } else {
                        permissionManager.edit().putBoolean("askedForPermissions", true).apply();
                        listener.granted();
                    }
                }
            }
        }
        else if(requestCode == AUTOSTART)
        {
            permissionManager.edit().putBoolean("askedForPermissions", true).apply();
            listener.granted();
        }
    }
}