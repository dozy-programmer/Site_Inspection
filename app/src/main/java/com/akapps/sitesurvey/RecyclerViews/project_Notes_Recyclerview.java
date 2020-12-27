package com.akapps.sitesurvey.RecyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.R;
import io.realm.Realm;
import io.realm.RealmList;

public class project_Notes_Recyclerview extends RecyclerView.Adapter<project_Notes_Recyclerview.MyViewHolder>{

    // project data
    private RealmList<String> project_Notes;
    private Context context;
    private Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView project_Note;

        public MyViewHolder(View v) {
            super(v);
            project_Note = v.findViewById(R.id.note_Textview);
        }
    }

    public project_Notes_Recyclerview(RealmList<String> project_Notes, Realm realm) {
        this.project_Notes = project_Notes;
        this.realm = realm;
    }

    @Override
    public project_Notes_Recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_format, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final String currentNote = project_Notes.get(position);

        // populates note into the recyclerview
        holder.project_Note.setText(currentNote);

        // color of every second note is blue, otherwise it is white
        if(position%2==0)
            holder.project_Note.setBackgroundColor(context.getResources().getColor(R.color.ultra_white));
        else
            holder.project_Note.setBackgroundColor(context.getResources().getColor(R.color.light_blue));

        // user can edit or delete note on long press on note
        holder.project_Note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            new MaterialDialog.Builder(context)
                    .title(R.string.edit_note)
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .contentGravity(GravityEnum.CENTER)
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .contentColor(context.getResources().getColor(R.color.bluish))
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .input("Enter note", currentNote, false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // saves edited note in database and updates it in recyclerview
                        realm.beginTransaction();
                        project_Notes.set(position, input.toString());
                        realm.commitTransaction();
                        notifyItemChanged(position);
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
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new MaterialDialog.Builder(context)
                                .title(R.string.delete_note)
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
                                        // deletes note and updates it in recyclerview
                                        realm.beginTransaction();
                                        project_Notes.remove(position);
                                        realm.commitTransaction();
                                        notifyDataSetChanged();
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
                                .positiveColor(context.getResources().getColor(R.color.green))
                                .negativeColor(context.getResources().getColor(R.color.gray))
                                .show();
                        dialog.dismiss();
                        }
                    })
                    .neutralText(R.string.delete_Dialog)
                    .negativeText(R.string.close_Dialog)
                    .positiveColor(v.getResources().getColor(R.color.green))
                    .negativeColor(v.getResources().getColor(R.color.gray))
                    .neutralColor(v.getResources().getColor(R.color.red))
                    .show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return project_Notes.size();
    }
}
