package com.ronal.nearestprinting.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ronal.nearestprinting.R;
import com.ronal.nearestprinting.model.Service;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ServicesAdapter extends FirestoreRecyclerAdapter<Service, ServicesAdapter.ViewHolder> {

    private Context context;

    public ServicesAdapter(@NonNull FirestoreRecyclerOptions<Service> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int i, @NonNull Service service) {
        Locale locale = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        int price = Integer.parseInt(service.getServicePrice());

        holder.serviceName.setText(service.getServiceName());
        holder.minimalOrder.setText(
                context.getString(R.string.service_minimal, service.getMinimalOrder()
                , service.getServiceSellUnitType())
        );
        holder.servicePrice.setText(
                format.format((double) price)
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_list_item, parent, false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.serviceName)
        AppCompatTextView serviceName;

        @BindView(R.id.serviceMinimalOrder)
        AppCompatTextView minimalOrder;

        @BindView(R.id.servicePrice)
        AppCompatTextView servicePrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
