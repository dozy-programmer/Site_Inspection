package com.akapps.sitesurvey.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Inverter;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.RecyclerViews.project_Inverters_Recyclerview;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Barcode_Scanner extends Fragment {

    // current project data
    private int project_Position;
    private RealmResults<Project> all_Projects;
    private RealmList<Inverter> all_Inverters;
    private boolean edit_Inverter;
    private int edit_Inverter_Position;

    // layout and activity info
    private LinearLayoutManager layoutManager_Inverters;
    private RecyclerView recyclerView_Inverters;
    private RecyclerView.Adapter adapter_Inverters;
    private FloatingActionButton add_Inverter;
    private Context context;
    private View view;

    // empty list layout
    private LinearLayout empty_Layout;
    private TextView empty_Text;

    // on-device database
    private Realm realm;

    public Barcode_Scanner() { }

    public static Barcode_Scanner newInstance(int project_Position) {
        Bundle args = new Bundle();
        args.putInt("position", project_Position);
        Barcode_Scanner f = new Barcode_Scanner();
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
            all_Inverters = all_Projects.get(project_Position).getInverter_Data();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_barcode__scanner, container, false);

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
        // initializing main layout
        recyclerView_Inverters = view.findViewById(R.id.projects_RecyclerView_Inverters);
        add_Inverter = view.findViewById(R.id.add_Inverter);

        layoutManager_Inverters = new LinearLayoutManager(context);
        recyclerView_Inverters.setHasFixedSize(true);
        recyclerView_Inverters.setLayoutManager(layoutManager_Inverters);

        // empty layout
        empty_Layout = view.findViewById(R.id.empty_Layout);
        empty_Text = view.findViewById(R.id.empty_Text);

        // opens camera to scan a barcode and returns the data
        add_Inverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode();
            }
        });

        // populates recyclerview
        populateAdapter(all_Inverters);

        // if recyclerview is empty, then empty view is shown
        isListEmpty();
    }

    // populates the recyclerview
    private void populateAdapter(RealmList<Inverter> project_Inverters) {
        adapter_Inverters = new project_Inverters_Recyclerview(project_Inverters, Barcode_Scanner.this, realm);
        recyclerView_Inverters.setAdapter(adapter_Inverters);
    }

    // opens camera to scan a barcode and returns the data
    private void scanBarcode(){
        // locks orientation so that the app does not crash
        // after opening the camera and changing the orientation
        if(Helper.getOrientation(context))
            Helper.setOrientation(getActivity(), getString(R.string.landscape));
        else
            Helper.setOrientation(getActivity(), getString(R.string.portrait));

        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setPrompt("SCANNING INVERTER");
        intentIntegrator.setBarcodeImageEnabled(false);
        IntentIntegrator.forSupportFragment(Barcode_Scanner.this).initiateScan();
    }

    // opens camera to scan a barcode and returns the data
    @SuppressLint("SourceLockedOrientationActivity")
    public void editScanBarcode(int inverterPosition){
        if(Helper.getOrientation(context))
            Helper.setOrientation(getActivity(), getString(R.string.landscape));
        else
            Helper.setOrientation(getActivity(), getString(R.string.portrait));

        edit_Inverter= true;
        this.edit_Inverter_Position = inverterPosition;

        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setPrompt("SCANNING INVERTER");
        intentIntegrator.setBarcodeImageEnabled(false);
        IntentIntegrator.forSupportFragment(Barcode_Scanner.this).initiateScan();
    }

    // if camera was closed after attempting to add a barcode for the first time, dialog sets EditText
    // to null, otherwise it just closes the dialog.
    // If the result contains data, then it is added to new inverter or to current inverter
    // depending on if the user if editing an inverter.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult Result = IntentIntegrator.parseActivityResult(requestCode , resultCode ,data);
        if(Result != null){
            if(Result.getContents() == null){
                Helper.showUserMessage(view, "Scanner Closed", Snackbar.LENGTH_SHORT);
                if(edit_Inverter)
                    Helper.setOrientation(getActivity(), "None");
                else
                    addBarcode("null");
            }
            else {
                if(edit_Inverter)
                    editBarcode(Result.getContents());
                else
                    addBarcode(Result.getContents());
            }
        }
        else
            super.onActivityResult(requestCode , resultCode , data);
    }

    // adds barcode and saves it to the database
    private void addBarcode(final String result){
        final Inverter currentInverter = new Inverter((all_Inverters.size()+1), result);

        new MaterialDialog.Builder(context)
            .title(getString(R.string.inverter_info))
            .titleColor(context.getResources().getColor(R.color.orange_red))
            .contentColor(context.getResources().getColor(R.color.bluish))
            .backgroundColor(context.getResources().getColor(R.color.black))
            .positiveText(R.string.confirm_Entry)
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .content("Name: Panel " + currentInverter.getNumberOfPanel() + "\n\nData: " +
                    currentInverter.getData())
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // updates data, unlocks orientation, and closes dialog
                    realm.beginTransaction();
                    all_Inverters.add(currentInverter);
                    realm.commitTransaction();
                    populateAdapter(all_Inverters);
                    isListEmpty();
                    Helper.setOrientation(getActivity(), "None");
                    dialog.dismiss();
                }
            })
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                    // closes dialog and unlocks orientation
                    dialog.dismiss();
                    Helper.setOrientation(getActivity(), "None");
                }
            })
            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // re-scans again
                    scanBarcode();
                    dialog.dismiss();
                }
            })
            .neutralText(getString(R.string.rescan))
            .negativeText(R.string.close_Dialog)
            .neutralColor(context.getResources().getColor(R.color.orange))
            .positiveColor(context.getResources().getColor(R.color.green))
            .negativeColor(context.getResources().getColor(R.color.gray))
            .show();
    }

    // edits barcode and saves it to the database
    private void editBarcode(final String inverterData){
        final Inverter currentInverter =  all_Inverters.get(edit_Inverter_Position);

        new MaterialDialog.Builder(context)
            .title(getString(R.string.edit_inverter))
            .titleColor(context.getResources().getColor(R.color.orange_red))
            .contentColor(context.getResources().getColor(R.color.bluish))
            .backgroundColor(context.getResources().getColor(R.color.black))
            .positiveText(R.string.confirm_Entry)
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .content("Name: Panel " + currentInverter.getNumberOfPanel() + "\n\n[NEW] Data: " + inverterData)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // updates data, unlocks orientation, and closes dialog
                    currentInverter.setData(inverterData);
                    populateAdapter(all_Inverters);
                    Helper.setOrientation(getActivity(), "None");
                    isListEmpty();
                    dialog.dismiss();
                }
            })
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                    // closes dialog
                    dialog.dismiss();
                }
            })
            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // re-scans again
                    editScanBarcode(edit_Inverter_Position);
                    dialog.dismiss();
                }
            })
            .negativeText(R.string.close_Dialog)
            .positiveColor(context.getResources().getColor(R.color.green))
            .negativeColor(context.getResources().getColor(R.color.gray))
            .neutralText(getString(R.string.rescan))
            .neutralColor(context.getResources().getColor(R.color.orange))
            .show();
    }

    // if recyclerview is empty, then empty view is shown
    public void isListEmpty(){
        Helper.isListEmpty(all_Inverters.size(), empty_Layout, null, empty_Text);
    }
}
