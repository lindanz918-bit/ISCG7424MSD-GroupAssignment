package com.groupproject.justeat.feature.staff;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.groupproject.justeat.R;

/** Staff-only presentation helpers. No data access or role-wide theme mutations. */
final class StaffUi {
    private StaffUi() {}

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static void badge(TextView view, String status) {
        String label = status == null || status.isEmpty() ? "UNKNOWN" : status;
        boolean pending = "PENDING".equalsIgnoreCase(label) || "RESERVED".equalsIgnoreCase(label);
        boolean negative =
                "REJECTED".equalsIgnoreCase(label)
                        || "CANCELLED".equalsIgnoreCase(label)
                        || "UNAVAILABLE".equalsIgnoreCase(label);
        Context c = view.getContext();
        int foreground =
                pending
                        ? R.color.staff_pending
                        : negative ? R.color.staff_error : R.color.staff_primary;
        int background =
                pending
                        ? R.color.staff_pending_surface
                        : negative ? R.color.staff_error_surface : R.color.staff_soft;
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(c.getColor(background));
        shape.setCornerRadius(dp(c, 16));
        view.setBackground(shape);
        view.setTextColor(c.getColor(foreground));
        view.setText(label);
        view.setTextSize(12);
        view.setTypeface(null, Typeface.BOLD);
        view.setPadding(dp(c, 12), dp(c, 6), dp(c, 12), dp(c, 6));
    }

    static TextView text(LinearLayout parent, String value, int size, boolean bold) {
        Context c = parent.getContext();
        TextView text = new TextView(c);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(c.getColor(bold ? R.color.staff_text : R.color.staff_muted));
        if (bold) text.setTypeface(null, Typeface.BOLD);
        text.setLineSpacing(dp(c, 4), 1);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(c, 12);
        parent.addView(text, p);
        return text;
    }

    static LinearLayout card(LinearLayout parent) {
        Context c = parent.getContext();
        MaterialCardView card = new MaterialCardView(c);
        card.setRadius(dp(c, 18));
        card.setCardElevation(dp(c, 1));
        card.setCardBackgroundColor(c.getColor(R.color.staff_surface));
        card.setStrokeWidth(dp(c, 1));
        card.setStrokeColor(c.getColor(R.color.staff_border));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(c, 16);
        parent.addView(card, p);
        LinearLayout content = new LinearLayout(c);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(c, 16), dp(c, 16), dp(c, 16), dp(c, 16));
        card.addView(content);
        return content;
    }
}
