package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;
import com.bumptech.glide.Glide;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityStaffDetailsBinding;

/** Reads Customer's MenuItem snapshots, keeping their original keys and fields intact. */
public class StaffOrderDetailsActivity extends BaseActivity<ActivityStaffDetailsBinding> {
    private DatabaseReference root;
    private ValueEventListener orderListener, itemsListener;
    private DataSnapshot order, items;
    private String id;
    private boolean busy;
    private String customerName;
    private String customerUid;
    private String tableName, tableKey;
    protected ActivityStaffDetailsBinding inflateBinding(LayoutInflater inflater) { return ActivityStaffDetailsBinding.inflate(inflater); }
    public void onCreate(Bundle state) {
        setTheme(com.groupproject.justeat.R.style.Theme_JustEat_Staff);
        super.onCreate(state);
        id = getIntent().getStringExtra("orderId");
        if (id == null || id.isEmpty() || id.matches(".*[.#$\\[\\]/].*")) { finish(); return; }
        getBinding().btnStaffDetailsBack.setOnClickListener(v -> finish());
        text("Loading order…");
        StaffAccess.check(() -> {
            if (isFinishing() || isDestroyed()) return;
            root = FirebaseDatabase.getInstance().getReference();
            orderListener = listen(root.child("Orders").child(id), true);
            itemsListener = listen(root.child("OrderItems").child(id), false);
        }, this::error);
    }
    private ValueEventListener listen(DatabaseReference ref, boolean isOrder) {
        ValueEventListener listener = new ValueEventListener() {
            public void onDataChange(DataSnapshot s) {
                if (isOrder) { order=s; loadCustomer(); loadTable(); } else items=s;
                render();
            }
            public void onCancelled(DatabaseError e) { error(e.getMessage()); }
        };
        ref.addValueEventListener(listener);
        return listener;
    }
    private void loadTable() {
        String key=value(order,"tableId");
        if (!StaffBookingPolicy.validKey(key) || key.equals(tableKey)) return;
        tableKey=key; tableName=null;
        root.child("tables").child(key).child("tableNumber").get().addOnSuccessListener(s -> {
            if (key.equals(tableKey)) { tableName=s.getValue(String.class); render(); }
        });
    }
    private void loadCustomer() {
        String uid=value(order,"customerId");
        if (uid.isEmpty() || uid.equals(customerUid)) return;
        customerUid=uid; customerName=null;
        root.child("users").child(uid).get().addOnSuccessListener(s -> {
            if (!uid.equals(customerUid)) return;
            String name=(value(s,"firstName")+" "+value(s,"lastName")).trim();
            customerName=name.isEmpty()?uid:name; render();
        });
    }
    private String value(DataSnapshot s, String key) {
        Object v = s == null ? null : s.child(key).getValue();
        return v == null ? "" : v.toString();
    }
    private void text(String message) { StaffUi.text(getBinding().content,message,16,false); }
    private void button(LinearLayout parent, String title, boolean enabled, boolean secondary, Runnable action) {
        // Explicit style on the button keeps disabled states and ripple from Material Components.
        MaterialButton b = new MaterialButton(this);
        b.setText(title); b.setAllCaps(false); b.setEnabled(enabled && !busy);
        b.setMinHeight(StaffUi.dp(this,48)); b.setCornerRadius(StaffUi.dp(this,12));
        if(secondary) {
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(com.groupproject.justeat.R.color.staff_error_surface)));
            b.setTextColor(getColor(com.groupproject.justeat.R.color.staff_error));
            b.setAlpha(enabled && !busy ? 1f : .45f);
        }
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=StaffUi.dp(this,8); parent.addView(b,params);
        b.setOnClickListener(v -> action.run());
    }
    private void render() {
        if (isFinishing() || isDestroyed()) return;
        LinearLayout content=getBinding().content;
        content.removeAllViews();
        if (order == null || items == null) { text("Loading order…"); return; }
        if (!order.exists()) { text("This order no longer exists."); return; }
        String status = value(order,"status");
        LinearLayout summary=StaffUi.card(content);
        StaffUi.text(summary,"Order #"+id,18,true);
        TextView badge=new TextView(this); StaffUi.badge(badge,status);
        LinearLayout.LayoutParams badgeParams=new LinearLayout.LayoutParams(-2,-2);
        badgeParams.bottomMargin=StaffUi.dp(this,16); summary.addView(badge,badgeParams);
        StaffUi.text(summary,customerName==null?value(order,"customerId"):customerName,18,true);
        StaffUi.text(summary,"Table "+(tableName == null ? value(order,"tableId") : tableName)+"  ·  "+value(order,"guestCount")+" guests",16,false);
        StaffUi.text(summary,value(order,"bookingDate")+"\nTime: "+
            (value(order,"bookingTime").isEmpty()?"Not provided in booking":value(order,"bookingTime")),14,false);
        StaffUi.text(content,"Meals",20,true);
        boolean active = StaffOrderActionPolicy.isActive(status);
        boolean allServed = true;
        int servedCount=0;
        for (DataSnapshot item : items.getChildren()) {
            LinearLayout meal=StaffUi.card(content);
            String name = value(item,"mealName");
            if (name.isEmpty()) name = value(item,"menuItemName");
            if (name.isEmpty()) name = "Meal";
            StaffUi.text(meal,name,18,true);
            StaffUi.text(meal,"$"+value(item,"price"),16,false);
            String image = value(item,"imageUrl");
            if (!image.isEmpty()) {
                ImageView imageView = new ImageView(this); imageView.setContentDescription(name);
                meal.addView(imageView,new LinearLayout.LayoutParams(-1,StaffUi.dp(this,144)));
                Glide.with(this).load(image).centerInside().into(imageView);
            }
            boolean served = Boolean.TRUE.equals(order.child("servedItems").child(item.getKey()).getValue(Boolean.class));
            allServed &= served; if(served) servedCount++;
            String key = item.getKey();
            button(meal,served ? "Served" : "Serve",active && !served,false,() -> serve(key));
        }
        if (!items.hasChildren()) text("No meals in this booking.");
        text(servedCount+" of "+items.getChildrenCount()+" meals served");
        button(content,"Complete order",StaffOrderActionPolicy.canComplete(status,allServed),false,() -> changeStatus("COMPLETED"));
        button(content,"Cancel order",active,true,() -> new androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("Cancel this order?").setNegativeButton("Keep order", null)
            .setPositiveButton("Cancel order", (d,w) -> changeStatus("CANCELLED")).show());
    }
    private void serve(String key) {
        busy=true; render();
        StaffAccess.check(() -> root.child("Orders").child(id).runTransaction(new Transaction.Handler() {
            public Transaction.Result doTransaction(MutableData data) {
                if (data.getValue()==null) return Transaction.success(data);
                if (!StaffOrderActionPolicy.isActive(data.child("status").getValue(String.class))) return Transaction.abort();
                data.child("servedItems").child(key).setValue(true);
                return Transaction.success(data);
            }
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot s) {
                busy=false; render();
                if(e!=null || !committed || s==null || !Boolean.TRUE.equals(s.child("servedItems").child(key).getValue(Boolean.class)))
                    error(e==null?"Order changed. Cannot serve this meal.":e.getMessage());
            }
        }, false), this::error);
    }
    private void changeStatus(String target) {
        busy=true; render();
        StaffBookingUpdates.changeStatus(id, target, message -> {
            if (isFinishing() || isDestroyed()) return;
            busy=false;
            if (message != null) error(message);
            else render();
        });
    }

    private void error(String message) {
        if(isFinishing() || isDestroyed()) return;
        busy=false; Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        if (root == null) { getBinding().content.removeAllViews(); text(message);  }
        else render();
    }
    protected void onDestroy() {
        if(root!=null) {
            if(orderListener!=null) root.child("Orders").child(id).removeEventListener(orderListener);
            if(itemsListener!=null) root.child("OrderItems").child(id).removeEventListener(itemsListener);
        }
        super.onDestroy();
    }
}
