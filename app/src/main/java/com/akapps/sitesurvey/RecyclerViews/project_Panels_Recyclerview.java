package com.akapps.sitesurvey.RecyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.sitesurvey.R;
import io.realm.Realm;
import io.realm.RealmList;

public class project_Panels_Recyclerview extends RecyclerView.Adapter<project_Panels_Recyclerview.MyViewHolder>{

    // project data
    private RealmList<Boolean> project_Panels;
    private Context context;
    private boolean editMode;
    private Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView panel_Image;

        public MyViewHolder(View v) {
            super(v);
            panel_Image = v.findViewById(R.id.panel_Image);
        }
    }

    public project_Panels_Recyclerview(RealmList<Boolean> project_Panels, boolean editMode, Realm realm) {
        this.project_Panels = project_Panels;
        this.editMode = editMode;
        this.realm = realm;
    }

    @Override
    public project_Panels_Recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.panel_format, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // if a position is selected, then it is set to a solar panel image
        if(project_Panels.get(position)){
            holder.panel_Image.setImageDrawable(context.getDrawable(R.drawable.ic_solar_panel_icon));
        }
        // if a position is selected, then it is set to a solar panel image. But it is set to
        // invisible so that all the grid items are the same size
        else if(!editMode) {
            holder.panel_Image.setImageDrawable(context.getDrawable(R.drawable.ic_solar_panel_icon));
            holder.panel_Image.setVisibility(View.INVISIBLE);
        }
        // if not selected, it each grid box will have a blue outline for user to know
        // where they can put the panels
        else if(editMode)
            holder.panel_Image.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

        // if user clicks on a position in the grid, it will save that position and if it is
        // pressed again, position is unselected. Selected positions is where panels will go.
        holder.panel_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(editMode) {
                if (project_Panels.get(position)) {
                    realm.beginTransaction();
                    project_Panels.set(position, false);
                    realm.commitTransaction();
                    notifyItemChanged(position);
                } else {
                    realm.beginTransaction();
                    project_Panels.set(position, true);
                    realm.commitTransaction();
                    notifyItemChanged(position);
                }
            }
            }
        });

    }

    @Override
    public int getItemCount() {
        return project_Panels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

