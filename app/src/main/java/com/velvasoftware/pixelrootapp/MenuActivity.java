package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.velvasoftware.pixelrootapp.databinding.ActivityMenuBinding;

import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMenuBinding menuBinding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);

        menuBinding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(menuBinding.getRoot());

        setSupportActionBar(menuBinding.appBarMenuPixel.appToolbarPixel);

        DrawerLayout drawerLayout = menuBinding.appDrawerMenuPixel;
        NavigationView navigationView = menuBinding.appNavViewPixel;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.catalogFragment, R.id.ordersFragment,
                R.id.ticketListFragment, R.id.branchFragment, R.id.profileFragment,
                R.id.aboutUsFragment, R.id.categoryFragment, R.id.productFragment,
                R.id.cartFragment, R.id.incidenceFragment, R.id.searchFragment,
                R.id.productListFragment, R.id.productDetailFragment, R.id.checkoutFragment,
                R.id.mapsFragment, R.id.orderDetailFragment, R.id.createTicketFragment,
                R.id.ticketDetailFragment, R.id.ticketTimelineFragment, R.id.ticketChatFragment,
                R.id.refundFragment, R.id.notificationsFragment, R.id.settingsFragment
        ).setOpenableLayout(drawerLayout).build();

        navController = Navigation.findNavController(this, R.id.appFragmentContentPixel);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Listener personalizado para gestionar la navegación, evitar duplicados y limpiar el stack
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en el destino seleccionado, solo cerramos el menú
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == itemId) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (itemId == R.id.homeFragment) {
                // Si es Inicio, volvemos a la raíz limpiando el stack
                navController.popBackStack(R.id.homeFragment, false);
            } else {
                // Para los demás, navegamos evitando duplicar el fragmento en el stack
                NavOptions navOptions = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.homeFragment, false) // Mantiene Home como base
                        .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                        .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                        .build();

                try {
                    navController.navigate(itemId, null, navOptions);
                } catch (Exception e) {
                    // Fallback por si el ID no está en el grafo
                    return NavigationUI.onNavDestinationSelected(item, navController);
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        MaterialButton btnLogout = headerView.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    public boolean onSupportNavigateUp(){
        NavController navController = Navigation.findNavController(this, R.id.appFragmentContentPixel);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_notify_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            navController.navigate(R.id.notificationsFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.app_dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelLogout);
        MaterialButton btnAccept = dialogView.findViewById(R.id.btnAcceptLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAccept.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); 
        });

        dialog.show();
    }
}