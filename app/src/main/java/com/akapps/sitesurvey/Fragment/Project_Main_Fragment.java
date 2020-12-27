package com.akapps.sitesurvey.Fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Activities.Home_Page;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.RecyclerViews.project_Notes_Recyclerview;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Project_Main_Fragment extends Fragment {

    // current project data
    private RealmResults<Project> all_Projects;
    private RealmList<String> all_Notes;
    private RealmList<Integer> checklist;
    private Project current_Project;
    private String FILE_PROVIDER_AUTHORITY;
    private boolean show_Notes;
    private int project_Position;

    // layout and activity info
    private TextView project_Owner, project_Created, project_Address;
    private ImageView showNotes;
    private RecyclerView recyclerView_Notes;
    private LinearLayoutManager layoutManager_Notes;
    private RecyclerView.Adapter adapter_Notes;
    private FloatingActionButton add_Notes;
    private Context context;
    private View view;

    // dialog layout
    private MaterialDialog edit_Project_Dialog;
    private EditText project_Name_Input;
    private EditText project_Owner_Input;
    private EditText project_Address_Input;
    private EditText project_Street_Input;
    private EditText project_City_Input;
    private EditText project_Zipcode_Input;
    private ImageView navigate;

    // on-device database
    private Realm realm;

    public Project_Main_Fragment() { }

    public static Project_Main_Fragment newInstance(int project_Position) {
        Bundle args = new Bundle();
        args.putInt("position", project_Position);
        Project_Main_Fragment f = new Project_Main_Fragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Realm.init(context);
        realm = Realm.getDefaultInstance();

        // retrieves data from  using realm db
        if (getArguments() != null) {
            project_Position = getArguments().getInt("position");
            all_Projects = realm.where(Project.class).findAll();
            current_Project = all_Projects.get(project_Position);
            all_Notes = all_Projects.get(project_Position).getProject_Notes();
            checklist = all_Projects.get(project_Position).getChecklist();
        }

        FILE_PROVIDER_AUTHORITY = getResources().getString(R.string.file_Provider);

        // if orientation changes, then show_Notes is set to the value it was beforehand
        if (savedInstanceState != null)
            show_Notes = savedInstanceState.getBoolean("show notes");
    }

    // when orientation changes, then show_Notes is saved
    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("show notes", show_Notes);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.project_main_fragment, container, false);

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

    private void initializeLayout(){
        // sets the title of the activity to project name
        getActivity().setTitle(current_Project.getProject_Name());

        // initializing main layout
        project_Owner = view.findViewById(R.id.project_Owner);
        project_Created = view.findViewById(R.id.project_Created);
        project_Address = view.findViewById(R.id.project_Address);
        recyclerView_Notes = view.findViewById(R.id.projects_RecyclerView_Notes);
        showNotes = view.findViewById(R.id.drop_Down_Button_Notes);
        add_Notes = view.findViewById(R.id.add_Inverter);
        navigate = view.findViewById(R.id.navigate);

        layoutManager_Notes = new LinearLayoutManager(context);
        recyclerView_Notes.setHasFixedSize(true);
        recyclerView_Notes.setLayoutManager(layoutManager_Notes);
        recyclerView_Notes.setNestedScrollingEnabled(false);

        // populates user data here
        project_Owner.setText(current_Project.getOwner_Name());
        project_Created.setText(current_Project.getDate_Created());
        project_Address.setText(current_Project.getProject_Address());

        // shows recyclerview and populates it
        if(show_Notes){
            recyclerView_Notes.setVisibility(View.VISIBLE);
            populateAdapter(all_Notes, realm);
        }

        // opens google maps with the location of the current project
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.co.in/maps?q=" + current_Project.getProject_Address()));
            if(intent.resolveActivity(getActivity().getPackageManager())!=null)
                startActivity(intent);
            }
        });

        // opens the recyclerview of notes
        showNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(all_Notes.size()==0)
                Helper.showUserMessage(view, "No notes", Snackbar.LENGTH_LONG);
            else
                showNotesState(true);
            }
        });

        add_Notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
            new MaterialDialog.Builder(context)
                    .title(getString(R.string.add_note))
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .contentGravity(GravityEnum.CENTER)
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .input("Enter note", "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            // saves inputted note to database
                            realm.beginTransaction();
                            all_Notes.add(input.toString());
                            realm.commitTransaction();

                            // if recyclerview is open, then new note is updated
                            // if not, then it is opened and updated
                            if(show_Notes)
                                adapter_Notes.notifyItemInserted(all_Notes.size());
                            else
                                showNotesState(true);

                            Helper.showUserMessage(view,  "Note added", Snackbar.LENGTH_SHORT);
                            dialog.dismiss();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
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
                    .negativeColor(getResources().getColor(R.color.gray))
                    .show();
            }
        });

    }

    // populates the recyclerview
    private void populateAdapter(RealmList<String> project_Notes, Realm realm) {
        adapter_Notes = new project_Notes_Recyclerview(project_Notes, realm);
        recyclerView_Notes.setAdapter(adapter_Notes);
    }

    // populates menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_email_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // opens dialog with all project data for user to edit
        if (id == R.id.action_Edit) {
            editProjectDialog();
        }
        // if notes is showing, close it first and then start the email process
        else if (id == R.id.action_Email) {
            if(show_Notes) {
                showNotesState(false);
            }
            emailProject();
        }
        // opens dialog to show checklist of what is completed in project
        else if (id == R.id.action_Checklist) {
            showChecklist();
        }

        return super.onOptionsItemSelected(item);
    }

    // initializes edit project dialog
    private void initializeDialog(){
        project_Name_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_Name_EditText);
        project_Owner_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_Owner_EditText);
        project_Address_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_Address_EditText);
        project_Street_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_Street_EditText);
        project_City_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_City_EditText);
        project_Zipcode_Input = (EditText) edit_Project_Dialog.findViewById(R.id.project_Zipcode_EditText);

        // populates dialog
        project_Name_Input.setText(current_Project.getProject_Name());
        project_Owner_Input.setText(current_Project.getOwner_Name());
        project_Address_Input.setText(String.valueOf(current_Project.getStreet_Number()));
        project_Street_Input.setText(current_Project.getStreet_Name());
        project_City_Input.setText(current_Project.getCity());
        project_Zipcode_Input.setText(String.valueOf(current_Project.getZipcode()));
    }

    // creates a zipped folder for project photos, a zipped folder for installation photos, attaches image
    // of the panel placement, and creates a formatted project information body.
    private void emailProject() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");

        // creates a zipped project photos folder and retrieves the location string
        String zippedPhotos = zipPhotos(current_Project.getProject_Photo_Locations(),
                current_Project.getProject_Name() + "_Survey_Photos");
        // creates a zipped installation photos folder and retrieves the location string
        String zippedInstallationPhotos = zipPhotos(current_Project.getInstallation_Photo_Locations(),
                current_Project.getProject_Name() + "_Installation_Photos");

        // attaching to intent to send folders
        ArrayList<Uri> uris = new ArrayList<>();

        // creates file pertaining to
        File fileProject = new File(zippedPhotos);
        File fileInstallation = new File(zippedInstallationPhotos);

        File panelPlacement;
        if(current_Project.getPanel_Placement_Screenshot_Path().contains(getString(R.string.panel_placement_path))) {
            panelPlacement = new File(current_Project.getPanel_Placement_Screenshot_Path());
            uris.add(FileProvider.getUriForFile(
                    getActivity(),
                    FILE_PROVIDER_AUTHORITY,
                    panelPlacement));
        }
        // only attaches to email if there are project photos
        if(current_Project.getProject_Photo_Locations().size()>0) {
            uris.add(FileProvider.getUriForFile(
                    getActivity(),
                    FILE_PROVIDER_AUTHORITY,
                    fileProject));
        }
        // only attaches to email if there are installation photos
        if(current_Project.getInstallation_Photo_Locations().size()>0) {
            uris.add(FileProvider.getUriForFile(
                    getActivity(),
                    FILE_PROVIDER_AUTHORITY,
                    fileInstallation));
        }

        // creates a formatted body of project data
        String emailBody = getProjectData();

        // adds email subject and email body to intent
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, current_Project.getOwner_Name() + " " + getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        startActivity(emailIntent);
    }

    // returns project information, project notes, and panel data in formatted
    private String getProjectData(){
        String allProjectData = current_Project.toString();
        allProjectData+="\n\nNotes:\n\n";
        StringBuilder all_Notes_Concat= new StringBuilder();
        StringBuilder inverter_Data= new StringBuilder();
        StringTokenizer tokens;

        if(all_Notes.size()==0)
            allProjectData+= "--No Notes--";
        else {
            for (int i = 0; i < all_Notes.size(); i++) {
                all_Notes_Concat.append(all_Notes.get(i)).append("\n");
            }
        }

        allProjectData+= all_Notes_Concat + "\n\nPanels and Inverter Data:\n\n";

        if(current_Project.getInverter_Data().size()==0)
            allProjectData+= "--No Data--";
        else {
            for (int i = 0; i < current_Project.getInverter_Data().size(); i++) {
                inverter_Data.append("Panel "+ current_Project.getInverter_Data().get(i).getNumberOfPanel()).
                        append(":\t").append(current_Project.getInverter_Data().get(i).getData()).append("\n");
            }
            allProjectData+= inverter_Data;
        }

        return allProjectData;
    }

    // depending on state, the recyclerview of notes will be set to invisible/gone or will be set to visible
    public void showNotesState(boolean state){
        if(!state){
            ObjectAnimator.ofFloat(showNotes, "rotation", 180f, 0f).setDuration(400).start();
            show_Notes = false;
            recyclerView_Notes.setVisibility(View.GONE);
        }
        else if(!show_Notes) {
            ObjectAnimator.ofFloat(showNotes, "rotation", 0f, -180f).setDuration(400).start();
            show_Notes = true;
            recyclerView_Notes.setVisibility(View.VISIBLE);
            populateAdapter(all_Notes, realm);
        }
        else {
            ObjectAnimator.ofFloat(showNotes, "rotation", 180f, 0f).setDuration(400).start();
            show_Notes = false;
            recyclerView_Notes.setVisibility(View.GONE);
        }
    }

    private void editProjectDialog(){
        edit_Project_Dialog = new MaterialDialog.Builder(context)
                .title(getString(R.string.edit_project))
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
                    String p_Name = project_Name_Input.getText().toString();
                    String p_Owner = project_Owner_Input.getText().toString();
                    String p_Address = project_Address_Input.getText().toString();
                    String p_Street = project_Street_Input.getText().toString();
                    String p_City = project_City_Input.getText().toString();
                    String p_Zipcode = project_Zipcode_Input.getText().toString();
                    if (p_Name.equals(""))
                        project_Name_Input.setError("Empty!");
                    else if (p_Owner.equals(""))
                        project_Owner_Input.setError("Empty!");
                    else if (p_Address.equals(""))
                        project_Address_Input.setError("Empty!");
                    else if (p_Street.equals(""))
                        project_Street_Input.setError("Empty!");
                    else if (p_City.equals(""))
                        project_City_Input.setError("Empty");
                    else if (p_Zipcode.equals(""))
                        project_Zipcode_Input.setError("Empty!");
                    else {

                        RealmResults<Project> user_Exists = realm.where(Project.class)
                                .equalTo("project_Name", p_Name)
                                .findAll();

                        // new project name could be unique, or it could still
                        // be the same one
                        if(user_Exists.size() <=1) {

                            // if none of the input fields is empty, then the project is
                            // updates and the new information is saved to database
                            realm.beginTransaction();
                            current_Project.setProject_Name(p_Name);
                            current_Project.setOwner_Name(p_Owner);
                            current_Project.setStreet_Number(Integer.valueOf(p_Address));
                            current_Project.setStreet_Name(p_Street);
                            current_Project.setCity(p_City);
                            current_Project.setZipcode(Integer.valueOf(p_Zipcode));
                            realm.commitTransaction();

                            dialog.dismiss();
                            Helper.showUserMessage(view, p_Name + " has been edited!", Snackbar.LENGTH_SHORT);

                            // updates data
                            getActivity().setTitle(current_Project.getProject_Name());
                            project_Owner.setText(current_Project.getOwner_Name());
                            project_Address.setText(current_Project.getProject_Address());

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
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                    edit_Project_Dialog.dismiss();
                    new MaterialDialog.Builder(context)
                            .title(getString(R.string.delete_project))
                            .titleColor(getResources().getColor(R.color.orange_red))
                            .contentGravity(GravityEnum.CENTER)
                            .content(R.string.confirm_Action)
                            .contentColor(getResources().getColor(R.color.bluish))
                            .backgroundColor(getResources().getColor(R.color.black))
                            .positiveText(R.string.confirm_Entry)
                            .canceledOnTouchOutside(false)
                            .autoDismiss(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // deletes project and saves the new data
                                    realm.beginTransaction();
                                    all_Projects.deleteFromRealm(project_Position);
                                    realm.commitTransaction();

                                    // since project was delete, it no longer exits
                                    // so user is sent to home activity
                                    Intent homepage = new Intent(context, Home_Page.class);
                                    startActivity(homepage);
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
                            .negativeColor(getResources().getColor(R.color.gray))
                            .show();
                    }
                })
                .negativeText(R.string.close_Dialog)
                .neutralText(R.string.delete_Dialog)
                .neutralColor(getResources().getColor(R.color.red))
                .positiveColor(getResources().getColor(R.color.green))
                .negativeColor(getResources().getColor(R.color.gray))
                .show();

        // dialog initializing
        initializeDialog();
    }

    // opens a dialog to show what is currently completed in the project
    private void showChecklist(){
        Integer[] checked;

        // retrieves position of items checked
        if(checklist.size()!=0){
            checked = new Integer[checklist.size()];
            for (int i = 0; i < checklist.size(); i++)
                checked[i] = checklist.get(i);
        }
        // nothing is checked
        else
            checked = new Integer[0];

        new MaterialDialog.Builder(context)
                .title(R.string.checklist_title)
                .backgroundColor(getResources().getColor(R.color.black))
                .positiveText(R.string.confirm_Entry)
                .items(R.array.checklist)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .negativeText(R.string.close_Dialog)
                .neutralText(R.string.select_all)
                .titleColor(getResources().getColor(R.color.orange_red))
                .positiveColor(getResources().getColor(R.color.dark_green))
                .negativeColor(getResources().getColor(R.color.gray))
                .neutralColor(getResources().getColor(R.color.orange))
                .itemsCallbackMultiChoice(checked, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        saveChecklist(which);
                        dialog.dismiss();
                        return false;
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (dialog.getSelectedIndices().length == getResources().getStringArray(R.array.checklist).length) {
                                dialog.clearSelectedIndices(false);
                            } else {
                                dialog.selectAllIndices(false);
                            }
                        }
                    })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss(); // dismisses dialog
                    }
                })
                .show();
    }

    // saves the status of the checklist
    private void saveChecklist(Integer[] selected){
        boolean isProjectCompleted = false;

        realm.beginTransaction();
        checklist.clear();
        realm.commitTransaction();

        for(int i=0; i<selected.length; i++){
            realm.beginTransaction();
            checklist.add(selected[i]);
            realm.commitTransaction();
        }

        if(selected.length == getResources().getStringArray(R.array.checklist).length)
            isProjectCompleted = true;

        realm.beginTransaction();
        current_Project.setCompleted(isProjectCompleted);
        realm.commitTransaction();
    }

    // creates a zip folder
    public String createZipFolder(String folderName){
        String zipPath = "";
        File storageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + getString(R.string.app_path) + folderName);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return storageDir.getAbsolutePath();
    }

    // places all the photos in a zip file and returns a string of the file path
    public String zipPhotos(RealmList<String> files, String folderName) {

        String zipFolder = createZipFolder(folderName);
        String zippath = zipFolder + ".zip";
        File zipFile = new File(zippath);

        int BUFFER = 1024;

        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];

            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zipFile.getAbsolutePath();
    }
}
