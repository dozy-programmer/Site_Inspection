package com.akapps.sitesurvey.RecyclerViews;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akapps.sitesurvey.Classes.Inverter;
import com.akapps.sitesurvey.Fragment.Barcode_Scanner;
import com.akapps.sitesurvey.R;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class project_Inverters_Recyclerview extends RecyclerView.Adapter<project_Inverters_Recyclerview.MyViewHolder>{

    // project data
    private RealmList<Inverter> project_Inverters;
    private Context context;
    private Barcode_Scanner barcode_Scanner;
    private Realm realm;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView inverter_Name, inverter_Data;
        private ImageView edit_Inverter, delete_Inverter;

        public MyViewHolder(View v) {
            super(v);
            inverter_Name = v.findViewById(R.id.inverter_Number);
            inverter_Data = v.findViewById(R.id.inverter_Data);
            edit_Inverter = v.findViewById(R.id.edit_Inverter);
            delete_Inverter = v.findViewById(R.id.delete_Inverter);
        }
    }

    public project_Inverters_Recyclerview(RealmList<Inverter> project_Inverters, Barcode_Scanner barcode_Scanner, Realm realm) {
        this.project_Inverters = project_Inverters;
        this.barcode_Scanner = barcode_Scanner;
        this.realm = realm;
    }

    @Override
    public project_Inverters_Recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.inverters_format, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Inverter currentInverter = project_Inverters.get(position);

        holder.inverter_Name.setText("Panel " + currentInverter.getNumberOfPanel());
        holder.inverter_Data.setText(currentInverter.getData());

        // if the data string is null, is it set to red and green otherwise
        if(currentInverter.getData().equals("null"))
            holder.inverter_Data.setTextColor(context.getResources().getColor(R.color.red));
        else
            holder.inverter_Data.setTextColor(context.getResources().getColor(R.color.green));

        // opens delete inverter dialog
        holder.delete_Inverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new MaterialDialog.Builder(context)
                    .title(R.string.delete_Inverter)
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .contentGravity(GravityEnum.CENTER)
                    .content(R.string.confirm_Action)
                    .contentColor(context.getResources().getColor(R.color.bluish))
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .positiveText(R.string.delete_Dialog)
                    .canceledOnTouchOutside(false)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // deletes the inverter, saves data, and updates it to view
                        realm.beginTransaction();
                        project_Inverters.remove(position);
                        realm.commitTransaction();
                        barcode_Scanner.isListEmpty();
                        notifyItemRemoved(position);
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
                    .positiveColor(context.getResources().getColor(R.color.red))
                    .negativeColor(context.getResources().getColor(R.color.gray))
                    .show();
            }
        });

        // opens dialog to edit inverter
        holder.edit_Inverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String inverter_Data;

            // if the data that the user wants to edit is null, the edit-text
            // is set to empty so user doesn't have to spend time deleting it
            if(currentInverter.getData().equals("null"))
                inverter_Data = "";
            else
                inverter_Data = currentInverter.getData();

            new MaterialDialog.Builder(context)
                    .title(R.string.edit_inverter)
                    .titleColor(context.getResources().getColor(R.color.orange_red))
                    .backgroundColor(context.getResources().getColor(R.color.black))
                    .positiveText(R.string.confirm_Entry)
                    .canceledOnTouchOutside(true)
                    .autoDismiss(false)
                    .content("Name: Panel " + currentInverter.getNumberOfPanel())
                    .input("Enter inverter data", inverter_Data, false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // saves input by user and updates it to view
                        realm.beginTransaction();
                        currentInverter.setData(input.toString());
                        realm.commitTransaction();
                        notifyItemChanged(position);
                        dialog.dismiss();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {}
                    })
                    // if pressed, opens camera to scan bar-code
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick (@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                        barcode_Scanner.editScanBarcode(position);
                        dialog.dismiss();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new MaterialDialog.Builder(context)
                                .title(R.string.rename_panel)
                                .titleColor(context.getResources().getColor(R.color.orange_red))
                                .contentGravity(GravityEnum.CENTER)
                                .backgroundColor(context.getResources().getColor(R.color.black))
                                .positiveText(R.string.confirm_Entry)
                                .canceledOnTouchOutside(false)
                                .autoDismiss(false)
                                .inputType(InputType.TYPE_CLASS_NUMBER)
                                .inputRange(1, 3)
                                .input("Enter panel number", "", false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    RealmResults<Inverter> inverter_Exists = realm.where(Inverter.class)
                                            .equalTo("numberOfPanel", Integer.valueOf(input.toString()))
                                            .findAll();

                                    // makes sure that the name does not already exist
                                    if(inverter_Exists.size() == 0) {
                                        // saves input by user and updates it to view
                                        realm.beginTransaction();
                                        currentInverter.setNumberOfPanel(Integer.valueOf(input.toString()));
                                        realm.commitTransaction();
                                        notifyItemChanged(position);
                                        dialog.dismiss();
                                    }
                                    else
                                        dialog.getInputEditText().setError("Inverter name Exists");

                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                        }
                    })
                    .negativeText(R.string.rename)
                    .neutralText(R.string.rescan)
                    .negativeColor(context.getResources().getColor(R.color.gray))
                    .neutralColor(context.getResources().getColor(R.color.orange))
                    .positiveColor(context.getResources().getColor(R.color.green))
                    .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return project_Inverters.size();
    }
}
