package com.akapps.sitesurvey.RecyclerViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Fragment.Project_Photos;
import com.akapps.sitesurvey.R;
import com.bumptech.glide.Glide;
import com.stfalcon.imageviewer.StfalconImageViewer;

import io.realm.Realm;
import io.realm.RealmList;

public class project_Photos_Recyclerview extends RecyclerView.Adapter<project_Photos_Recyclerview.MyViewHolder>{

    // project data
    private RealmList<String> project_Photos;
    private Context context;
    private Project_Photos fragment;
    private Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView project_Photo;

        public MyViewHolder(View v) {
            super(v);
            project_Photo = v.findViewById(R.id.project_Photos);
        }
    }

    public project_Photos_Recyclerview(RealmList<String> project_Photos, Project_Photos fragment, Realm realm) {
        this.project_Photos = project_Photos;
        this.realm = realm;
        this.fragment = fragment;
    }

    @Override
    public project_Photos_Recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.photos_format, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // populates ImageView with image file using glide
        Glide.with(context).load(project_Photos.get(position))
                .placeholder(R.drawable.ic_empty_iocn)
                .into(holder.project_Photo);

        // if an ImageView is clicked, it will open that image in the default gallery app
        holder.project_Photo.setOnClickListener(v -> {
            // if project image  is clicked, it will open that image in fullscreen inside app
            new StfalconImageViewer.Builder<>(context, project_Photos, (imageView, image) ->
                    Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.ic_empty_iocn)
                            .into(imageView))
                    .withBackgroundColor(context.getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(position)
                    .withTransitionFrom(holder.project_Photo)
                    .show();
        });

        // on long click of ImageView, user can delete photo
        holder.project_Photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
            new MaterialDialog.Builder(context)
                    .title(R.string.delete_photo)
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .contentGravity(GravityEnum.CENTER)
                    .content(R.string.confirm_Action)
                    .contentColor(context.getResources().getColor(R.color.bluish))
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .onPositive((dialog, which) -> {
                    // deletes photo and saves to database
                    realm.beginTransaction();
                    project_Photos.remove(position);
                    realm.commitTransaction();

                    // updates recyclerview
                    notifyDataSetChanged();

                    // if empty, then empty view is shown
                    fragment.isListEmpty(project_Photos);
                    dialog.dismiss();
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .negativeText(R.string.close_Dialog)
                    .positiveColor(v.getResources().getColor(R.color.green))
                    .negativeColor(v.getResources().getColor(R.color.gray))
                    .show();
            return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return project_Photos.size();
    }
}

