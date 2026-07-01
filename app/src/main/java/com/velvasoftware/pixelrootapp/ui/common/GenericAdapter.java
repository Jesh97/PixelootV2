package com.velvasoftware.pixelrootapp.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import java.util.List;

public class GenericAdapter<VB extends ViewBinding, T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder<VB>> {
    
    public interface BindingInflater<VB> {
        VB inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);
    }

    public interface Binder<VB, T> {
        void bind(VB binding, T data);
    }

    private final List<T> items;
    private final BindingInflater<VB> inflater;
    private final Binder<VB, T> binder;

    public GenericAdapter(List<T> items, BindingInflater<VB> inflater, Binder<VB, T> binder) {
        this.items = items;
        this.inflater = inflater;
        this.binder = binder;
    }

    @NonNull
    @Override
    public ViewHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VB binding = inflater.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder<VB> holder, int position) {
        binder.bind(holder.binding, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        public final VB binding;
        public ViewHolder(VB binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
