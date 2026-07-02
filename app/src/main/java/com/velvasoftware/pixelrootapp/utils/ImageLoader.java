package com.velvasoftware.pixelrootapp.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.velvasoftware.pixelrootapp.R;

/**
 * Punto único para cargar cualquier imagen de la app (portadas de juegos, banners,
 * avatar de perfil, etc.) con un estilo visual consistente: esquinas redondeadas,
 * transición de aparición suave (fade), recorte centrado y manejo de error/placeholder.
 *
 * Antes cada Fragment repetía la misma cadena de Glide a mano; ahora todo pasa por acá,
 * así que si un día queremos cambiar el look (por ejemplo, más redondeado, o agregar un
 * efecto shimmer mientras carga), se cambia en un solo lugar.
 */
public class ImageLoader {

    private static final int CORNER_RADIUS_DP = 16;
    private static final int CORNER_RADIUS_SMALL_DP = 10;

    /** Portada de juego en una tarjeta (catálogo, home, carrito): esquinas redondeadas suaves. */
    public static void loadGameCover(ImageView imageView, String rawUrl) {
        int radiusPx = dpToPx(imageView, CORNER_RADIUS_DP);

        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .placeholder(R.drawable.bg_card_gaming)
                .error(R.drawable.bg_card_gaming);

        Glide.with(imageView.getContext())
                .load(ImageUrlUtils.toDirectImageUrl(rawUrl))
                .apply(options)
                .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(250))
                .into(imageView);
    }

    /** Imagen pequeña dentro de una fila (carrito, miniatura de galería): esquinas un poco menos redondeadas. */
    public static void loadThumbnail(ImageView imageView, String rawUrl) {
        int radiusPx = dpToPx(imageView, CORNER_RADIUS_SMALL_DP);

        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .placeholder(R.drawable.bg_card_gaming)
                .error(R.drawable.bg_card_gaming);

        Glide.with(imageView.getContext())
                .load(ImageUrlUtils.toDirectImageUrl(rawUrl))
                .apply(options)
                .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(200))
                .into(imageView);
    }

    /** Imagen principal grande del detalle de producto (banner ancho, sin recorte agresivo). */
    public static void loadMainBanner(ImageView imageView, String rawUrl) {
        Glide.with(imageView.getContext())
                .load(ImageUrlUtils.toDirectImageUrl(rawUrl))
                .centerCrop()
                .placeholder(R.drawable.bg_card_gaming)
                .error(R.drawable.bg_card_gaming)
                .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(300))
                .into(imageView);
    }

    /** Foto de perfil: circular. */
    public static void loadProfilePhoto(ImageView imageView, String rawUrl) {
        Glide.with(imageView.getContext())
                .load(ImageUrlUtils.toDirectImageUrl(rawUrl))
                .circleCrop()
                .placeholder(R.drawable.icon_default_profile)
                .error(R.drawable.icon_default_profile)
                .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(200))
                .into(imageView);
    }

    private static int dpToPx(ImageView view, int dp) {
        float density = view.getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}