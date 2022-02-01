package com.akapps.sitesurvey.RecyclerViews;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Activities.Home_Page;
import com.akapps.sitesurvey.Activities.View_Project;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Classes.Project;
import com.akapps.sitesurvey.R;
import com.google.android.material.snackbar.Snackbar;

import io.realm.Realm;
import io.realm.RealmResults;

public class project_Homepage_Layout_Recyclerview extends RecyclerView.Adapter<project_Homepage_Layout_Recyclerview.MyViewHolder>{

    // project data
    private RealmResults<Project> projects;
    private Context context;
    private MaterialDialog projectStatusDialog;
    private ImageView markProjectComplete, markProjectIncomplete;
    private Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView project_Name;
        private TextView project_Created;
        private TextView project_Owner;
        private TextView project_Status;
        private LinearLayout project_Status_Indicator;
        private View view;

        public MyViewHolder(View v) {
            super(v);
            project_Name = v.findViewById(R.id.project_Name);
            project_Created = v.findViewById(R.id.project_Created);
            project_Owner = v.findViewById(R.id.project_Owner);
            project_Status = v.findViewById(R.id.project_Status);
            project_Status_Indicator = v.findViewById(R.id.project_Status_Indicator);
            view = v;
        }
    }

    public project_Homepage_Layout_Recyclerview(RealmResults<Project> projects, Realm realm) {
        this.projects = projects;
        this.realm = realm;
    }

    @Override
    public project_Homepage_Layout_Recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.project_layout_format, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // retrieves the current project data
        Project currentProject = projects.get(position);

        // populates TextViews with project info
        holder.project_Name .setText(currentProject.getProject_Name());
        holder.project_Created.setText(currentProject.getDate_Created());
        holder.project_Owner.setText(currentProject.getOwner_Name());

        // if project is completed, then the text and ImageView is set to
        // green to indicate so and gold-ish color if not
        if(currentProject.isCompleted()) {
            holder.project_Status.setText("Completed");
            holder.project_Status.setTextColor(context.getResources().getColor(R.color.green));
            holder.project_Status_Indicator.setBackgroundColor(context.getResources().getColor(R.color.green));
        }
        else{
            holder.project_Status.setText("Ongoing");
            holder.project_Status.setTextColor(context.getResources().getColor(R.color.goldish));
            holder.project_Status_Indicator.setBackgroundColor(context.getResources().getColor(R.color.goldish));
        }

        // on clicking project, it opens an activity with all user data and can edit project
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            context = view.getContext();
            Intent open_Project = new Intent(context, View_Project.class);
            open_Project.putExtra("project_Position", position);
            context.startActivity(open_Project);
            }
        });

        // On long click, user can set the status of project or delete it
        // Since the dialog layout is custom, the onClickListener for the ImageViews is not inside the dialog
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                projectStatusDialog = new MaterialDialog.Builder(context)
                            .title(R.string.project_status)
                            .titleColor(context.getResources().getColor(R.color.orange_red))
                            .contentGravity(GravityEnum.CENTER)
                            .customView(R.layout.project_edit, false)
                            .backgroundColor(context.getResources().getColor(R.color.black))
                            .positiveText(R.string.close_Dialog)
                            .canceledOnTouchOutside(false)
                            .autoDismiss(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // opens delete project dialog
                                new MaterialDialog.Builder(context)
                                        .title(R.string.delete_project)
                                        .titleColor(context.getResources().getColor(R.color.orange_red))
                                        .contentGravity(GravityEnum.CENTER)
                                        .content(R.string.confirm_Action)
                                        .contentColor(context.getResources().getColor(R.color.bluish))
                                        .backgroundColor(context.getResources().getColor(R.color.black))
                                        .positiveText(R.string.confirm_Entry)
                                        .canceledOnTouchOutside(false)
                                        .autoDismiss(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            // deletes project from database and updates it
                                            realm.beginTransaction();
                                            projects.deleteFromRealm(position);
                                            realm.commitTransaction();
                                            notifyItemRemoved(position);

                                            // shows empty view if there are no projects
                                            ((Home_Page) context).isListEmpty();
                                            dialog.dismiss();
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            }
                                        })
                                        .negativeText(R.string.close_Dialog)
                                        .positiveColor(context.getResources().getColor(R.color.green))
                                        .negativeColor(context.getResources().getColor(R.color.gray))
                                        .show();
                                dialog.dismiss();
                                }
                            })
                            .neutralText(R.string.delete_Dialog)
                            .positiveColor(context.getResources().getColor(R.color.gray))
                            .neutralColor(context.getResources().getColor(R.color.red))
                            .show();

                markProjectComplete = (ImageView) projectStatusDialog.findViewById(R.id.project_Done);
                markProjectIncomplete = (ImageView) projectStatusDialog.findViewById(R.id.project_Ongoing);

                // marks the project status as complete
                markProjectComplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    changeProjectStatus(position, true);
                    }
                });

                // marks the project as ongoing
                markProjectIncomplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    changeProjectStatus(position, false);
                    }
                });

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    // updates status of project and saves it
    private void changeProjectStatus(int position, boolean state){
        realm.beginTransaction();
        projects.get(position).setCompleted(state);
        realm.commitTransaction();

        notifyItemChanged(position);
        projectStatusDialog.dismiss();
    }
}