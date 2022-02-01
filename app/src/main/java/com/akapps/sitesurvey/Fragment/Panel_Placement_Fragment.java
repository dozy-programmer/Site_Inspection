package com.akapps.sitesurvey.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Classes.Camera;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.RecyclerViews.project_Panels_Recyclerview;
import com.google.android.material.snackbar.Snackbar;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Panel_Placement_Fragment extends Fragment{

    // current project data
    private RealmResults<Project> all_Projects;
    private RealmList<Boolean> project_Panels;
    private int project_Position;
    private boolean editMode;

    // layout and activity info
    private RecyclerView recyclerView_Panels;
    private RecyclerView.Adapter adapter_Panels;
    private Context context;
    private View view;

    // dialogs
    private int numberOfColumns, numOfRows;
    private EditText inputColumns, inputRows;
    private MaterialDialog inputColsRows;

    // compass dialog
    private ImageView north, south, east, west, northeast, northwest, southwest, southeast;
    private MaterialDialog inputNorthFacingArrow;
    private ImageView northFacingArrow;
    private boolean refresh_Layout;

    // empty list layout
    private LinearLayout empty_Layout;
    private TextView empty_Text, add_Panels;

    // on-device database
    private Realm realm;

    public Panel_Placement_Fragment() { }

    public static Panel_Placement_Fragment newInstance(int project_Position) {
        Bundle args = new Bundle();
        args.putInt("position", project_Position);
        Panel_Placement_Fragment f = new Panel_Placement_Fragment();
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
            project_Panels = all_Projects.get(project_Position).getPanel_Data();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_panel__placement, container, false);

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
        if(inputColsRows!=null)
            inputColsRows.cancel();
        if(inputNorthFacingArrow!=null)
            inputNorthFacingArrow.cancel();
    }

    @SuppressLint("RestrictedApi")
    private void initializeLayout() {
        // initializing main layout
        recyclerView_Panels = view.findViewById(R.id.projects_RecyclerView_Panels);
        northFacingArrow = view.findViewById(R.id.northFacingArrow);

        recyclerView_Panels.setHasFixedSize(true);
        recyclerView_Panels.setItemViewCacheSize(20);
        recyclerView_Panels.setDrawingCacheEnabled(true);
        recyclerView_Panels.setNestedScrollingEnabled(false);

        // empty layout
        empty_Layout = view.findViewById(R.id.empty_Layout);
        empty_Text = view.findViewById(R.id.empty_Text);
        add_Panels = view.findViewById(R.id.edit_Panels);

        // opens dialog for user to set number of rows and columns of panels
        add_Panels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPanelsMode();
            }
        });

        // on pressing button, user can edit direction of arrow
        northFacingArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCompassDialog();
            }
        });

        showLayout();
    }

    private void showLayout(){
        // retrieves user preference on number of columns and rows
        SharedPreferences preference = context.getSharedPreferences(all_Projects.get(project_Position).getProject_Id(), Activity.MODE_PRIVATE);
        numberOfColumns = preference.getInt("Column_Size_Panels",0);
        numOfRows = preference.getInt("Rows_Size_Panels",0);
        editMode = preference.getBoolean("status", false);

        // shows layout if number of columns and number of rows set by user is greater than 0
        if(numberOfColumns != 0 && numOfRows != 0) {
            boolean isNorthArrowSet = all_Projects.get(project_Position).getNorthFacingArrow();
            // sets the number of columns of recyclerview
            recyclerView_Panels.setLayoutManager(new GridLayoutManager(context, numberOfColumns));

            // shows the north facing arrow if user set one
            if(!isNorthArrowSet)
                northFacingArrow.setVisibility(View.GONE);
            else{
                northFacingArrow.setVisibility(View.VISIBLE);
                northFacingArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_compass_icon));
                northFacingArrow.setRotation(all_Projects.get(project_Position).getNorthFacingArrowRotation());
            }
            // shows screenshot icon based on status of edit mode
            getActivity().invalidateOptionsMenu();
            // populates recyclerview
            populateAdapter(project_Panels, editMode);
        }

        // if recyclerview is empty, then empty view is shown
        Helper.isListEmpty(project_Panels.size(), empty_Layout, null, empty_Text);
    }

    // populates the recyclerview
    private void populateAdapter(RealmList<Boolean> project_Panels, boolean editMode) {
        adapter_Panels = new project_Panels_Recyclerview(project_Panels, editMode, realm);
        recyclerView_Panels.setAdapter(adapter_Panels);
    }

    // populates menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_confirm_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);

        MenuItem screenshotIcon = menu.findItem(R.id.action_Screenshot);
        MenuItem confirmIcon = menu.findItem(R.id.action_Confirm);
        if(editMode) {
            screenshotIcon.setVisible(false);
            confirmIcon.setVisible(true);
        }
        else {
            screenshotIcon.setVisible(true);
            confirmIcon.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_Screenshot){
            screenShotDialog();
        }
        else if (id == R.id.action_Confirm) {
            new MaterialDialog.Builder(context)
                .title(getString(R.string.panel_info))
                .titleColor(context.getResources().getColor(R.color.orange_red))
                .contentGravity(GravityEnum.CENTER)
                .content(getString(R.string.panel_placement_confirmation))
                .contentColor(context.getResources().getColor(R.color.bluish))
                .backgroundColor(context.getResources().getColor(R.color.black))
                .positiveText(R.string.confirm_Entry)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    try {
                        if(editMode && !isPanelPlacementEmpty()) {
                            // sets edit mode to false, repopulates layout, and opens
                            // dialog to set north facing arrow
                            setEditMode(false);
                            showLayout();
                            // opens dialog for user to choose north direction
                            openCompassDialog();
                            dialog.dismiss();
                        }
                        else if(!isPanelPlacementEmpty()){
                            // no changes where made and user is notified
                            Helper.showUserMessage(view, getString(R.string.panel_no_changes), Snackbar.LENGTH_LONG);
                            dialog.dismiss();
                        }
                        else{
                            // no panel selected and user is notified
                            Helper.showUserMessage(view, getString(R.string.empty6), Snackbar.LENGTH_LONG);
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        Helper.showUserMessage(view, getString(R.string.panel_error), Snackbar.LENGTH_LONG);
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
                .negativeColor(getResources().getColor(R.color.gray))
                .show();
        }
        else  if (id == R.id.action_Edit) {
            // opens edit dialog
            editPanelsMode();
        }

        return super.onOptionsItemSelected(item);
    }

    // initializes column and row input
    private void initializeDialogInput(){
        inputColumns = (EditText) inputColsRows.findViewById(R.id.column_input);
        inputRows = (EditText) inputColsRows.findViewById(R.id.row_input);
    }

    // initializes direction ImageViews
    private void initializeCompassDialogInput(){
        north = (ImageView) inputNorthFacingArrow.findViewById(R.id.north_compass);
        northeast = (ImageView) inputNorthFacingArrow.findViewById(R.id.northeast_compass);
        northwest = (ImageView) inputNorthFacingArrow.findViewById(R.id.northwest_compass);
        south = (ImageView) inputNorthFacingArrow.findViewById(R.id.south_compass);
        southeast = (ImageView) inputNorthFacingArrow.findViewById(R.id.southeast_compass);
        southwest = (ImageView) inputNorthFacingArrow.findViewById(R.id.southwest_compass);
        east = (ImageView) inputNorthFacingArrow.findViewById(R.id.east_compass);
        west = (ImageView) inputNorthFacingArrow.findViewById(R.id.west_compass);
    }

    // saves column and rows size and updates it in activity
    private void setColumnSize(int columnSize, int rows) {
        SharedPreferences sp = context.getSharedPreferences(all_Projects.get(project_Position).getProject_Id(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("Column_Size_Panels", columnSize);
        editor.putInt("Rows_Size_Panels", rows);
        editor.apply();

        this.numberOfColumns = columnSize;
        this.numOfRows = rows;
    }

    // whichever image is selected is set to be lighter and all the other images is set to
    // their default color
    private void setCurrentSelectedImage(int position){
        int removeSelection = 250;
        int setSelection = 50;

        north.setImageAlpha(removeSelection);
        northwest.setImageAlpha(removeSelection);
        northeast.setImageAlpha(removeSelection);
        southwest.setImageAlpha(removeSelection);
        south.setImageAlpha(removeSelection);
        southeast.setImageAlpha(removeSelection);
        east.setImageAlpha(removeSelection);
        west.setImageAlpha(removeSelection);

        switch (position){
            case 0:
                northwest.setImageAlpha(setSelection);
                break;
            case 1:
                north.setImageAlpha(setSelection);
                break;
            case 2:
                northeast.setImageAlpha(setSelection);
                break;
            case 3:
                west.setImageAlpha(setSelection);
                break;
            case 4:
                east.setImageAlpha(setSelection);
                break;
            case 5:
                southwest.setImageAlpha(setSelection);
                break;
            case 6:
                south.setImageAlpha(setSelection);
                break;
            case 7:
                southeast.setImageAlpha(setSelection);
                break;
            default:
                break;

        }
    }

    private void openCompassDialog(){
        inputNorthFacingArrow = new MaterialDialog.Builder(context)
                .title(getString(R.string.north_facing_select))
                .titleColor(context.getResources().getColor(R.color.orange_red))
                .backgroundColor(context.getResources().getColor(R.color.black))
                .contentColor(getResources().getColor(R.color.bluish))
                .customView(R.layout.north_direction_dialog_layout, false)
                .contentGravity(GravityEnum.CENTER)
                .positiveText(R.string.confirm_Entry)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // floating action button on the bottom right of screen is made visible
                    // with chosen arrow direction on it
                    northFacingArrow.setVisibility(View.VISIBLE);
                    // refreshes layout to reflect changes
                    showLayout();

                    // takes a screen shot if there are panels selected by the user
                    if(project_Panels.size()!=0)
                        screenShot();
                    else
                        Helper.showUserMessage(view, "Empty", Snackbar.LENGTH_LONG);

                    dialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // gets rid of floating action button
                    northFacingArrow.setVisibility(View.INVISIBLE);
                    realm.beginTransaction();
                    all_Projects.get(project_Position).setNorthFacingArrow(false);
                    realm.commitTransaction();
                    // refreshes layout to reflect changes
                    showLayout();

                    // if user is not currently editing, then a screenshot is taken
                    if(!editMode)
                        screenShot();

                    dialog.dismiss();
                    }
                })
                .neutralText(getString(R.string.none))
                .neutralColor(getResources().getColor(R.color.goldish))
                .positiveColor(getResources().getColor(R.color.green))
                .show();

        initializeCompassDialogInput();
        selectedIconOnClickListener();
    }

    // returns true if there are no panels selected
    private boolean isPanelPlacementEmpty(){
        for(int i=0; i<project_Panels.size(); i++){
            if(project_Panels.get(i))
                return false;
        }
        return true;
    }

    // if user selects edit mode, then that preference is saved until confirming placement
    private void setEditMode(boolean status) {
        SharedPreferences sp = context.getSharedPreferences(all_Projects.get(project_Position).getProject_Id(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("status", status);
        this.editMode = status;
        editor.apply();
    }

    // notifies user that screen shot was taken and allows them to view it
    private void screenshotTakenDialog(final String screenShotFile){
        Snackbar.make(view, "Screenshot Saved!", Snackbar.LENGTH_LONG).setAction(getString(R.string.view), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScreenShot(screenShotFile);
            }
        }).show();
    }

    // opens last taken screen shot in default gallery app
    private void openScreenShot(final String screenShotFile){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(screenShotFile), "image/*");
        getActivity().startActivity(intent);
    }

    private void screenShotDialog(){
        // checks to see if there are panels selected and that
        // user is not in edit mode
        if(!isPanelPlacementEmpty() && !editMode) {
            new MaterialDialog.Builder(context)
                    .title(getString(R.string.screen_shot))
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .contentGravity(GravityEnum.CENTER)
                    .content(getString(R.string.screen_shot_question))
                    .contentColor(context.getResources().getColor(R.color.bluish))
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .positiveText(getString(R.string.retake))
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // takes screen shot of panels
                            screenShot();
                            dialog.dismiss();
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
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // opens last screen shot taken
                            String screenShotPath = all_Projects.get(project_Position).getPanel_Placement_Screenshot_Path();
                            if(!screenShotPath.isEmpty()) {
                                openScreenShot(all_Projects.get(project_Position).getPanel_Placement_Screenshot_Path());
                                dialog.dismiss();
                            }
                            else
                                Helper.showUserMessage(view, getString(R.string.no_screenshots), Snackbar.LENGTH_LONG);
                        }
                    })
                    .neutralText(getString(R.string.view))
                    .neutralColor(getResources().getColor(R.color.goldish))
                    .negativeText(R.string.close_Dialog)
                    .positiveColor(getResources().getColor(R.color.green))
                    .negativeColor(getResources().getColor(R.color.gray))
                    .show();
        }
        // user is in edit mode, therefore they are notified to confirm panel placement
        else if(!isPanelPlacementEmpty())
            Helper.showUserMessage(view, "Confirm placement to take screenshot", Snackbar.LENGTH_LONG);
        else
            // no selected panels, user is notified
            Helper.showUserMessage(view, "Empty", Snackbar.LENGTH_LONG);
    }

    // takes screenshot of the current fragment, crops it to get rid of white space above and
    // below panels and adds the fab button with the north facing arrow on the top right
    private void screenShot () {
        // top most row that contains panels
        int first_Rows = findPanelHorizontalBounds(true);
        // bottom most row that contains panels
        int last_Rows = findPanelHorizontalBounds(false);
        //  left most column that contains panels
        int left_Row = findPanelVerticalBounds(true);
        //  right most column that contains panels
        int right_Row = findPanelVerticalBounds(false);
        try {
            // scrolls to top of screen
            recyclerView_Panels.scrollToPosition(0);
            // sets background of recyclerview to R.color.background
            recyclerView_Panels.setBackgroundColor(getResources().getColor(R.color.background));
            recyclerView_Panels.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView_Panels.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            // creates bitmap of the whole recyclerview
            Bitmap grid = Bitmap.createBitmap(recyclerView_Panels.getWidth(), recyclerView_Panels.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            recyclerView_Panels.draw(new Canvas(grid));

            // determines each row size
            int eachRowSize = grid.getHeight() / numOfRows;
            // determines each column size
            int eachColSize = grid.getWidth() / numberOfColumns;
            // using the first and last row, the height of desired bitmap is determined
            int desired_Height = (eachRowSize * last_Rows) - (eachRowSize * first_Rows);
            // using the first right and left column, the width of desired bitmap is determined
            final int desired_Width = (eachColSize * right_Row) - (eachColSize * left_Row);

            // creates the cropped bitmap desired
            grid = Bitmap.createBitmap(grid, eachColSize * left_Row, eachRowSize * first_Rows, desired_Width, desired_Height);

            // sets the fab size to be 10% of the width of cropped bitmap
            int fab_Size = (int) (desired_Width * 0.1);
            northFacingArrow.getLayoutParams().height = fab_Size;
            northFacingArrow.getLayoutParams().width = fab_Size;
            northFacingArrow.requestLayout();

            final Bitmap fab;
            final Bitmap fullBitmap;
            final Canvas tempCanvas;

            // if a north-arrow fab button was set, then it takes that fab button, rotates it
            // to what the direction the user wanted, and adds it to a new bitmap that contains
            // the north facing arrow on the top right and the cropped bitmap of panels under it
            if (northFacingArrow.getVisibility() == View.VISIBLE) {

                // refresh_Layout variable allows the northFacingArrow Image to be updated
                // by recalling this method with the new size of northFacingArrow
                if(refresh_Layout == false) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refresh_Layout = true;
                            screenShot();
                        }
                    }, 1000);
                }

                Matrix matrix = new Matrix();
                int fabHeight, fabWidth;
                matrix.preRotate(all_Projects.get(project_Position).getNorthFacingArrowRotation());

                // gets the size of the fab button, sets the default to 144
                if (northFacingArrow.getWidth() == 0 || northFacingArrow.getHeight() == 0)
                    fabHeight = fabWidth = 56;
                else
                    fabHeight = fabWidth = fab_Size;

                // creates fab button bitmap
                fab = Bitmap.createBitmap(fabWidth, fabHeight, Bitmap.Config.ARGB_8888);
                tempCanvas = new Canvas(fab);
                // rotates bitmap
                tempCanvas.rotate(all_Projects.get(project_Position).getNorthFacingArrowRotation(), fabWidth / 2, fabHeight / 2);
                northFacingArrow.draw(tempCanvas);

                int width, height;
                height = grid.getHeight() + fabHeight;
                width = grid.getWidth();
                // creates a bitmap with the fab button on the top right and leaves
                // a space of 50 under it, then the crop image starts from there
                fullBitmap = Bitmap.createBitmap(width, height + 50, Bitmap.Config.ARGB_8888);
                Canvas comboImage = new Canvas(fullBitmap);
                comboImage.drawBitmap(fab, grid.getWidth() - fab.getWidth(), 0f, null);
                comboImage.drawBitmap(grid, 0f, fabHeight + 50, null);
            } else {
                // the full bitmap is the cropped bitmap if no north-arrow button was set
                fullBitmap = grid;
                refresh_Layout = true;
            }

            // saves the bitmap on device and returns a string of the location
            final String screenShotFile = Camera.saveImage(view, context, fullBitmap, all_Projects.get(project_Position).getProject_Id() + getString(R.string.panel_placement_path), false);

            // device location saved in database
            realm.beginTransaction();
            all_Projects.get(project_Position).setPanel_Placement_Screenshot_Path(screenShotFile);
            realm.commitTransaction();

            // notifies user that the screenshot was taken
            if(refresh_Layout) {
                screenshotTakenDialog(screenShotFile);
                refresh_Layout = false;
            }
        } catch (Resources.NotFoundException e) {
            Helper.showUserMessage(view, getString(R.string.screenshot_error), Snackbar.LENGTH_LONG);
        }
    }

    // multiples the number of columns and rows setby the user
    // and creates an RealmList (ArrayList) of that size
    private RealmList<Boolean> createMatrixOfPanels(int sizeOfPanels){
        RealmList<Boolean> project_Panels = new RealmList<>();
        for(int i=0; i<sizeOfPanels; i++){
            boolean panel = false;
            project_Panels.add(panel);
        }
        return project_Panels;
    }

    // determines which icon arrow was selected in dialog and saves that information
    private void selectedIconOnClickListener(){
        northwest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(0);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(-30);
                realm.commitTransaction();
            }
        });

        north.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(1);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(0);
                realm.commitTransaction();
            }
        });

        northeast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(2);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(30);
                realm.commitTransaction();
            }
        });

        west.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(3);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(-90);
                realm.commitTransaction();
            }
        });

        east.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(4);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(90);
                realm.commitTransaction();
            }
        });

        southwest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(5);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(-135);
                realm.commitTransaction();
            }
        });

        south.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(6);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(180);
                realm.commitTransaction();
            }
        });

        southeast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentSelectedImage(7);
                realm.beginTransaction();
                all_Projects.get(project_Position).setNorthFacingArrow(true);
                all_Projects.get(project_Position).setNorthFacingArrowRotation(135);
                realm.commitTransaction();
            }
        });
    }

    // opens dialog and lets user enter number of columns and rows
    private void editPanelsMode(){
        inputColsRows = new MaterialDialog.Builder(context)
                .title(getString(R.string.size_of_panels))
                .titleColor(context.getResources().getColor(R.color.orange_red))
                .backgroundColor(context.getResources().getColor(R.color.black))
                .contentColor(getResources().getColor(R.color.bluish))
                .customView(R.layout.add_column_row_dialog_format, false)
                .contentGravity(GravityEnum.CENTER)
                .positiveText(R.string.confirm_Entry)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    initializeDialogInput();

                    // makes sure input length is greater than 0
                    if (inputColumns.getText().toString().length() > 0 && inputRows.getText().toString().length() > 0) {
                        int columns = Integer.valueOf(inputColumns.getText().toString());
                        int rows = Integer.valueOf(inputRows.getText().toString());

                        // saves columns and rows size inputted
                        if (columns > 1 && columns < 21) {
                            if (rows > 0 && rows < 41) {
                                setColumnSize(columns, rows);
                                realm.beginTransaction();
                                all_Projects.get(project_Position).setPanel_Data(createMatrixOfPanels(columns * rows));

                                // if an fab button is visible, set it to invisible and update
                                // data so that the user can set a new north facing arrow
                                if(northFacingArrow.getVisibility() == View.VISIBLE)
                                    all_Projects.get(project_Position).setNorthFacingArrow(false);
                                realm.commitTransaction();

                                // sets edit mode to true and re-initializes layout to reflect changes
                                setEditMode(true);
                                showLayout();

                                dialog.dismiss();
                            }
                            else
                                inputRows.setError(getString(R.string.input_incorrect));
                        }
                        else if(columns == 0 && rows == 0){
                            // sets the panel data to empty, saves it, and shows empty view
                            realm.beginTransaction();
                            all_Projects.get(project_Position).setPanel_Data(new RealmList<Boolean>());
                            if(northFacingArrow.getVisibility() == View.VISIBLE)
                                all_Projects.get(project_Position).setNorthFacingArrow(false);
                            realm.commitTransaction();

                            // refreshes layout to reflect changes
                            showLayout();
                            dialog.dismiss();
                            // displays message to user that the panels are deleted
                            Helper.showUserMessage(view, getString(R.string.panel_data_deleted), Snackbar.LENGTH_LONG);
                        }
                        else
                            inputColumns.setError(getString(R.string.input_incorrect));
                    }
                    else {
                        // catches empty input
                        if(inputColumns.getText().toString().length()==0)
                            inputColumns.setError("Input empty");
                        else
                            inputRows.setError("Input Empty");
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
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // sets edit mode to true and re-initializes layout to reflect changes
                    setEditMode(true);
                    showLayout();
                    dialog.dismiss();
                }
            })
            .neutralText(getString(R.string.edit))
            .neutralColor(getResources().getColor(R.color.goldish))
            .negativeText(R.string.close_Dialog)
            .positiveColor(getResources().getColor(R.color.green))
            .negativeColor(getResources().getColor(R.color.gray))
            .show();
    }

    // finds the top-most row of panels if status is true and the bottom-most row otherwise
    public int findPanelHorizontalBounds(boolean status) {
        int desiredRow=0;
        for (int i = 0; i < project_Panels.size(); i++) {
            if (project_Panels.get(i)) {
                desiredRow = i / numberOfColumns;
                if(status)
                    return desiredRow; // finds top
            }
        }
        return ++desiredRow; // finds bottom
    }

    // finds the left-most column of panels if status is true and the right-most column otherwise
    public int findPanelVerticalBounds(boolean status) {
        int desiredColumn=0;
        for (int column= 0; column < numberOfColumns; column++) {
            for(int row= column; row < project_Panels.size(); row+=numberOfColumns) {
                if (project_Panels.get(row)) {
                    desiredColumn = column;
                    if (status)
                        return desiredColumn;
                    else
                        break;
                }
            }
        }
        return ++desiredColumn;
    }

}
