package com.groupproject.justeat.feature.staff;
import androidx.lifecycle.*;
import com.google.firebase.database.*;
import com.groupproject.justeat.base.BaseViewModel;
import com.groupproject.justeat.data.model.RestaurantTable;
import java.util.*;
/** Direct realtime Firebase table updates; database keys are distinct from displayed table numbers. */
public class StaffTableViewModel extends BaseViewModel {
    private final MutableLiveData<List<RestaurantTable>> tables = new MutableLiveData<>(Collections.emptyList());
    private final Map<RestaurantTable,String> keys = new IdentityHashMap<>();
    private DatabaseReference ref;
    private ValueEventListener listener;
    private boolean closed;
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;
    private DataSnapshot bookings;
    private final Map<String,String> names = new HashMap<>();
    private final Set<String> requested = new HashSet<>();
    private final MutableLiveData<Map<String,String>> bookingLabels = new MutableLiveData<>(Collections.emptyMap());
    public LiveData<Map<String,String>> getBookingLabels() { return bookingLabels; }
    private String value(DataSnapshot s,String key) { Object v=s.child(key).getValue(); return v==null?"":v.toString(); }
    private void publishBookings() {
        if (closed || bookings==null) return;
        Map<String,String> labels=new HashMap<>();
        for(DataSnapshot order:bookings.getChildren()) {
            if(!StaffOrderActionPolicy.isActive(value(order,"status"))) continue;
            String uid=value(order,"customerId");
            String label=names.getOrDefault(uid,uid)+" • "+value(order,"bookingDate")+" • "+value(order,"guestCount")+" guests";
            String table=value(order,"tableId");
            labels.put(table,labels.containsKey(table)?labels.get(table)+"\n"+label:label);
            if(!uid.isEmpty() && requested.add(uid)) FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener(s -> { String name=(value(s,"firstName")+" "+value(s,"lastName")).trim();
                    if(!name.isEmpty()) names.put(uid,name); publishBookings(); });
        }
        bookingLabels.setValue(labels);
    }
    public LiveData<List<RestaurantTable>> getTables() { return tables; }
    public void loadTables() {
        if(listener!=null) return;
        setLoading(true);
        StaffAccess.check(() -> {
            if(closed) return;
            ref=FirebaseDatabase.getInstance().getReference("tables");
            listener=new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    List<RestaurantTable> data=new ArrayList<>(); keys.clear();
                    try {
                        for(DataSnapshot child:snapshot.getChildren()) {
                            RestaurantTable t=child.getValue(RestaurantTable.class);
                            if(t!=null) { keys.put(t,child.getKey()); data.add(t); }
                        }
                        tables.setValue(data);
                    } catch(RuntimeException e) { setErrorMessage("Unable to read table data."); }
                    setLoading(false);
                }
                public void onCancelled(DatabaseError e) { fail(e.getMessage()); }
            };
            ref.addValueEventListener(listener);
            ordersRef=FirebaseDatabase.getInstance().getReference("Orders");
            ordersListener=new ValueEventListener() {
                public void onDataChange(DataSnapshot s) { bookings=s; publishBookings(); }
                public void onCancelled(DatabaseError e) { setErrorMessage("Booking details unavailable: "+e.getMessage()); }
            };
            ordersRef.addValueEventListener(ordersListener);
        },this::fail);
    }
    public void toggleAvailability(RestaurantTable table) {
        if(!"AVAILABLE".equalsIgnoreCase(table.getStatus()) && !"UNAVAILABLE".equalsIgnoreCase(table.getStatus())) return;
        updateStatus(table,"AVAILABLE".equalsIgnoreCase(table.getStatus())?"UNAVAILABLE":"AVAILABLE");
    }
    public void toggleOccupancy(RestaurantTable table) {
        if("AVAILABLE".equalsIgnoreCase(table.getStatus())) updateStatus(table,"OCCUPIED");
        else if("OCCUPIED".equalsIgnoreCase(table.getStatus()) || "RESERVED".equalsIgnoreCase(table.getStatus())) updateStatus(table,"AVAILABLE");
    }
    private void updateStatus(RestaurantTable table,String target) {
        String key=keys.get(table);
        if(key==null || Boolean.TRUE.equals(getLoading().getValue())) return;
        String expected=table.getStatus();
        setLoading(true);
        StaffAccess.check(() -> ref.child(key).child("status").runTransaction(new Transaction.Handler() {
            public Transaction.Result doTransaction(MutableData d) {
                String current=d.getValue(String.class);
                if(current==null) return Transaction.success(d);
                if(!expected.equalsIgnoreCase(current)) return Transaction.abort();
                d.setValue(target); return Transaction.success(d);
            }
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot s) {
                setLoading(false);
                if(e!=null || !committed || s==null || !target.equals(s.getValue(String.class))) fail("Table changed or update failed. Please try again.");
            }
        }),this::fail);
    }
    private void fail(String message) { setLoading(false); setErrorMessage(message); }
    public void consumeError() { setErrorMessage(null); }
    protected void onCleared() {
        closed=true;
        if(ref!=null && listener!=null) ref.removeEventListener(listener);
        if(ordersRef!=null && ordersListener!=null) ordersRef.removeEventListener(ordersListener);
    }
}
