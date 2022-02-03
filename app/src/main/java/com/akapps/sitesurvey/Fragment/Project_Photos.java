package com.akapps.sitesurvey.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.RecyclerViews.project_Photos_Recyclerview;
import com.github.dhaval2404.imagepicker.ImagePicker;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Project_Photos extends Fragment{

    // current project data
    private RealmResults<Project> all_Projects;
    private RealmList<String> project_Photos;
    private RealmList<String> installation_Photos;
    private int project_Position;
    private boolean type_Photos_Layout;
    private int numberOfColumns;
    private boolean orientation;

    // layout and activity info
    private RecyclerView recyclerView_Photos;
    private RecyclerView.Adapter adapter_Photos;
    private TextView currentLayout;
    private Context context;
    private View view;

    // empty list layout
    private LinearLayout empty_Layout;
    private TextView empty_Text;
    private TextView add_Photos;

    // on-device database
    private Realm realm;

    public Project_Photos() { }

    public static Project_Photos newInstance(int project_Position) {
        Bundle args = new Bundle();
        args.putInt("position", project_Position);
        Project_Photos f = new Project_Photos();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Realm.init(context);
        realm = Realm.getDefaultInstance();

        orientation = Helper.getOrientation(context);

        // retrieves data from  using realm db
        if (getArguments() != null) {
            project_Position = getArguments().getInt("position");
            all_Projects = realm.where(Project.class).findAll();
            project_Photos = all_Projects.get(project_Position).getProject_Photo_Locations();
            installation_Photos = all_Projects.get(project_Position).getInstallation_Photo_Locations();
        }

        // if orientation changes, then type_Photos_Layout is set to the value it was beforehand
        if (savedInstanceState != null) {
            type_Photos_Layout = savedInstanceState.getBoolean("photos layout");
        }
    }

    // when orientation changes, then type_Photos_Layout is saved
    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("photos layout", type_Photos_Layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_project__photos, container, false);

        initializeLayout();
        // populates menu
        setHasOptionsMenu(true);

        return view;
    }

    // closes realm db when activity or is closed
    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void initializeLayout() {
        // empty layout
        empty_Layout = view.findViewById(R.id.empty_Layout);
        empty_Text = view.findViewById(R.id.empty_Text);
        add_Photos = view.findViewById(R.id.add_Project);
        numberOfColumns = getColumnSize(orientation);

        // initializing main layout
        recyclerView_Photos = view.findViewById(R.id.projects_RecyclerView_Photos);
        currentLayout = view.findViewById(R.id.current_Layout);

        recyclerView_Photos.setHasFixedSize(true);
        recyclerView_Photos.setItemViewCacheSize(20);
        recyclerView_Photos.setDrawingCacheEnabled(true);
        recyclerView_Photos.setLayoutManager(new GridLayoutManager(context, numberOfColumns));

        // opens camera to take pictures and will go continuously until back button is pressed
        add_Photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        updatePhotoLayout();
    }

    private void openCamera(){
        ImagePicker.with(this)
                .maxResultSize(814, 814)
                .compress(1024)
                .saveDir(context.getExternalFilesDir(null))
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            String photo_Location = uri.getPath();

            // if false, then it is saved to project photos and recyclerview is updated
            if(!type_Photos_Layout) {
                realm.beginTransaction();
                project_Photos.add(photo_Location);
                realm.commitTransaction();

                if(project_Photos.size()==0)
                    populateAdapter(project_Photos);
                else
                    adapter_Photos.notifyItemInserted(project_Photos.size()-1);

                // is project_Photos is empty, then empty view is set
                isListEmpty(project_Photos);
            }
            else {
                // if true, then it is saved to installation photos and recyclerview is updated
                realm.beginTransaction();
                installation_Photos.add(photo_Location);
                realm.commitTransaction();

                if (installation_Photos.size() == 0)
                    populateAdapter(installation_Photos);
                else
                    adapter_Photos.notifyItemInserted(installation_Photos.size() - 1);

                // is project_Photos is empty, then empty view is set
                isListEmpty(installation_Photos);
            }
        }
        else if (resultCode== ImagePicker.RESULT_ERROR) {}
    }

    private void updatePhotoLayout(){
        // if false, then the current pictures being viewed is project photos
        if(!type_Photos_Layout) {
            populateAdapter(project_Photos);
            currentLayout.setText("Project Photos");
            isListEmpty(project_Photos);
        }
        // else, then the current pictures being viewed is installation photos
        else {
            populateAdapter(installation_Photos);
            currentLayout.setText("Installation Photos");
            isListEmpty(installation_Photos);
        }
    }

    // populates the recyclerview
    private void populateAdapter(RealmList<String> project_Photos) {
        adapter_Photos = new project_Photos_Recyclerview(project_Photos, Project_Photos.this, realm);
        recyclerView_Photos.setAdapter(adapter_Photos);
    }

    // populates menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_switch_column_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // opens camera to take pictures and will go continuously until back button is pressed
        if (id == R.id.action_Add) {
            openCamera();
        }
        // switches between project photos and installation photos by repopulating recyclerview
        else if (id == R.id.action_Switch) {
            if(type_Photos_Layout) {
                currentLayout.setText("Project Photos");
                type_Photos_Layout = false;
                populateAdapter(project_Photos);
                isListEmpty(project_Photos);
            }
            else {
                currentLayout.setText("Installation Photos");
                type_Photos_Layout = true;
                populateAdapter(installation_Photos);
                isListEmpty(installation_Photos);
            }
        }
        // opens dialog to let user change the number of columns of the images
        else if (id == R.id.action_ChangeColumns) {
            String rangeMessage;

            if(orientation)
                rangeMessage = "Pick a number between 5-8";
            else
                rangeMessage = "Pick a number between 2-5";

            new MaterialDialog.Builder(context)
                    .title(getString(R.string.column_size))
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .contentColor(getResources().getColor(R.color.bluish))
                    .content(rangeMessage)
                    .contentGravity(GravityEnum.CENTER)
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input("Enter number of columns", "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        EditText editText = dialog.getInputEditText();
                        int inputNum = Integer.valueOf(input.toString());

                        // if in landscape mode, then user can choose higher number of columns
                        if(orientation && inputNum>4 && inputNum<9) {
                            setColumnSize(inputNum);
                            recyclerView_Photos.setLayoutManager(new GridLayoutManager(context, inputNum));
                            recyclerView_Photos.setAdapter(adapter_Photos);
                            dialog.dismiss();
                        }
                        else if (!orientation && inputNum>1 && inputNum<6){
                            setColumnSize(inputNum);
                            recyclerView_Photos.setLayoutManager(new GridLayoutManager(context, inputNum));
                            recyclerView_Photos.setAdapter(adapter_Photos);
                            dialog.dismiss();
                        }
                        else {
                            if(orientation)
                                editText.setError("Range is 5-8");
                            else
                                editText.setError("Range is 3-5");
                        }
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {}
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                        dialog.dismiss();
                        }
                    })
                    .negativeText(R.string.close_Dialog)
                    .positiveColor(getResources().getColor(R.color.green))
                    .negativeColor(getResources().getColor(R.color.gray))
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    // sets the size of columns for current orientation
    private void setColumnSize(int columnSize) {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.prefs), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if(Helper.getOrientation(context))
            editor.putInt(getString(R.string.colSize_Land), columnSize);
        else
            editor.putInt(getString(R.string.colSize), columnSize);

        editor.apply();
    }

    // returns column size -- default value for portrait is 3 and 5 for landscape
    private int getColumnSize(boolean orientation){
        SharedPreferences preference = context.getSharedPreferences(getString(R.string.prefs), Activity.MODE_PRIVATE);
        int columnSize;

        if(orientation)
            columnSize = preference.getInt(getString(R.string.colSize_Land),-1);
        else
            columnSize = preference.getInt(getString(R.string.colSize),-1);
        if(columnSize < 3) {
            if(orientation)
                return 5;
            else
                return 3;
        }
        return columnSize;
    }


    // if recyclerview is empty, then empty view is shown
    public void isListEmpty(RealmList<String> project_Photos){
        Helper.isListEmpty(project_Photos.size(), empty_Layout, null, empty_Text);
    }
}
