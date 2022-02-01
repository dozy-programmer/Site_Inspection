package com.akapps.sitesurvey.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.RecyclerViews.project_Homepage_Layout_Recyclerview;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.MaterialDialog;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;
import io.realm.Realm;
import io.realm.RealmResults;

public class Home_Page extends AppCompatActivity {

    // data
    private RealmResults<Project> all_Projects;

    // layout and activity info
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private FloatingActionButton add_Project;
    private Context context;

    // dialog layout
    private MaterialDialog add_Project_Dialog;
    private EditText project_Name_Input;
    private EditText project_Owner_Input;
    private EditText project_Address_Input;
    private EditText project_Street_Input;
    private EditText project_City_Input;
    private EditText project_Zipcode_Input;

    // empty list layout
    private LinearLayout empty_Layout;
    private LinearLayout app_background;
    private TextView empty_Text;

    // on-device database
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        Realm.init(context);
        realm = Realm.getDefaultInstance();

        // layout is initialized
        initializeLayout();

        // retrieves data from  using realm db
        all_Projects = realm.where(Project.class).findAll();

        // populates adapter
        adapter = new project_Homepage_Layout_Recyclerview(all_Projects, realm);
        recyclerView.setAdapter(adapter);
        // if recyclerview is empty, then empty view is shown
        isListEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.action_Info){
            showAppInfoDialog(); // displays app version number and company
        }
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // updates status of project
        adapter.notifyDataSetChanged();
    }

    private void initializeLayout(){
        setContentView(R.layout.homepage_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initializing main layout
        add_Project = findViewById(R.id.add_Project);
        recyclerView = findViewById(R.id.projects_RecyclerView);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // empty layout
        empty_Layout = findViewById(R.id.empty_Layout);
        empty_Text = findViewById(R.id.empty_Text);

        // app background image
        app_background = findViewById(R.id.main_background);

        // handles the add project button by opening a dialog
        // and makes sure that none of the fields are empty on exit
        add_Project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                add_Project_Dialog = new MaterialDialog.Builder(context)
                    .title(getString(R.string.project_info))
                    .titleColor(getResources().getColor(R.color.orange_red))
                    .titleGravity(GravityEnum.CENTER)
                    .contentGravity(GravityEnum.CENTER)
                    .customView(R.layout.add_project_dialog_format, true)
                    .backgroundColor(getResources().getColor(R.color.black))
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String project_Name = project_Name_Input.getText().toString();
                        String project_Owner = project_Owner_Input.getText().toString();
                        String project_Address = project_Address_Input.getText().toString();
                        String project_Street = project_Street_Input.getText().toString();
                        String project_City = project_City_Input.getText().toString();
                        String project_Zipcode = project_Zipcode_Input.getText().toString();
                        if (project_Name.equals(""))
                            project_Name_Input.setError("Empty!");
                        else if (project_Owner.equals(""))
                            project_Owner_Input.setError("Empty!");
                        else {
                            if(!project_Name.toLowerCase().contains("project"))
                                project_Name+= " Project";

                            RealmResults<Project> user_Exists = realm.where(Project.class)
                                    .equalTo("project_Name", project_Name)
                                    .findAll();

                            if(user_Exists.size() == 0) {
                                boolean addressEmpty = Helper.isDialogAddressEmpty(project_Address, project_Street, project_City, project_Zipcode);
                                Project newProject;
                                if(!addressEmpty) {
                                    newProject = new Project(project_Name + "_" + project_Street, DateFormat.getDateInstance().format(new Date()),
                                            project_Name, project_Owner, Integer.valueOf(project_Address), project_Street,
                                            project_City, Integer.valueOf(project_Zipcode));
                                }
                                else{
                                    newProject = new Project(project_Name + "_" + project_Street, DateFormat.getDateInstance().format(new Date()),
                                            project_Name, project_Owner);
                                }

                                realm.beginTransaction();
                                realm.insertOrUpdate(newProject);
                                realm.commitTransaction();

                                // if recyclerview is empty, then empty view is shown
                                isListEmpty();

                                Helper.showUserMessage(view, project_Name + " is added!", Snackbar.LENGTH_SHORT);
                                adapter.notifyItemInserted(all_Projects.size());
                                dialog.dismiss();
                            }
                            else
                                project_Name_Input.setError("Project Exists");
                        }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                                dialog.dismiss();
                            }
                    })
                    .negativeText(R.string.close_Dialog)
                    .positiveColor(getResources().getColor(R.color.green))
                    .negativeColor(getResources().getColor(R.color.red))
                    .show();

            // dialog initializing
            initializeDialog();
            }
        });
    }

    private void initializeDialog(){
        project_Name_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_Name_EditText);
        project_Owner_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_Owner_EditText);
        project_Address_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_Address_EditText);
        project_Street_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_Street_EditText);
        project_City_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_City_EditText);
        project_Zipcode_Input = (EditText) add_Project_Dialog.findViewById(R.id.project_Zipcode_EditText);
    }

    // shows app information
    private void showAppInfoDialog(){
        add_Project_Dialog = new MaterialDialog.Builder(context)
            .title(getString(R.string.app_info))
            .titleColor(getResources().getColor(R.color.orange_red))
            .contentGravity(GravityEnum.CENTER)
            .content(getString(R.string.app_company) + Html.fromHtml(getString(R.string.c_symbol)))
            .contentColor(getResources().getColor(R.color.green))
            .backgroundColor(getResources().getColor(R.color.black))
            .positiveText(R.string.close_Dialog)
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .neutralText("OPEN IN SETTINGS")
            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    openAppInSettings();
                }
            })
            .positiveColor(getResources().getColor(R.color.gray))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .show();
    }

    // if recyclerview is empty, then empty view is shown
    public void isListEmpty(){
        Helper.isListEmpty(all_Projects.size(), empty_Layout, app_background, empty_Text);
    }

    private void openAppInSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
