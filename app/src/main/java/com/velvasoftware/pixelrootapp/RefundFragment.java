package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.velvasoftware.pixelrootapp.databinding.FragmentRefundBinding;

public class RefundFragment extends Fragment {

    private FragmentRefundBinding binding;

    public RefundFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRefundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupData();
        setupListeners();
    }

    private void setupData() {
        // En una app real, estos datos vendrían de los argumentos del fragmento
        binding.txtRefundAmount.setText(com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(59.99));
        binding.txtOrderTitle.setText("Neon Abyss II");
        binding.txtOrderId.setText("#ORD-99234");
        binding.txtRefundMethodTitle.setText("Créditos Pixel Root");
    }

    private void setupListeners() {
        binding.btnConfirmRefund.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Reembolso aceptado con éxito", Toast.LENGTH_SHORT).show();
            // Navegar de regreso al inicio o a la lista de tickets
            Navigation.findNavController(v).navigate(R.id.homeFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}