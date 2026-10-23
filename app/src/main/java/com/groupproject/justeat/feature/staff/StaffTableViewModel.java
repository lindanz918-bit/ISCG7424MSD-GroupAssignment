package com.groupproject.justeat.feature.staff;
import androidx.lifecycle.*;
import com.google.firebase.database.*;
import com.groupproject.justeat.base.BaseViewModel;
import java.util.*;
/** Direct realtime Firebase table updates; database keys are distinct from displayed table numbers. */
public class StaffTableViewModel extends BaseViewModel {
    private final MutableLiveData<List<StaffTableState>> tables = new MutableLiveData<>(Collections.emptyList());
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
            if(!StaffBookingPolicy.holdsDay(value(order,"status"))) continue;
            String uid=value(order,"customerId");
            String label=names.getOrDefault(uid,uid)+" • "+value(order,"bookingDate")+" • "+value(order,"guestCount")+" guests • "+value(order,"status");
            String table=value(order,"tableId");
            labels.put(table,labels.containsKey(table)?labels.get(table)+"\n"+label:label);
            if(!uid.isEmpty() && requested.add(uid)) FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener(s -> { String name=(value(s,"firstName")+" "+value(s,"lastName")).trim();
                    if(!name.isEmpty()) names.put(uid,name); publishBookings(); });
        }
        bookingLabels.setValue(labels);
    }
    public LiveData<List<StaffTableState>> getTables() { return tables; }
    public void loadTables() {
        if(listener!=null) return;
        setLoading(true);
        StaffAccess.check(() -> {
            if(closed) return;
            ref=FirebaseDatabase.getInstance().getReference("tables");
            listener=new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    List<StaffTableState> data=new ArrayList<>();
                    try {
                        for(DataSnapshot child:snapshot.getChildren()) {
                            data.add(new StaffTableState(child));
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
    public void toggleAvailability(StaffTableState table) {
        if (!table.isMigrated()) { fail("Migrate this table before editing its state."); return; }
        updateField(table, "isEnabled", table.enabled, !table.enabled);
    }
    public void toggleOccupancy(StaffTableState table) {
        if (!table.isMigrated()) { fail("Migrate this table before editing its state."); return; }
        updateField(table, "occupancyStatus", table.occupancy,
                "AVAILABLE".equals(table.occupancy) ? "OCCUPIED" : "AVAILABLE");
    }
    private void updateField(StaffTableState table, String field, Object expected, Object target) {
        if (Boolean.TRUE.equals(getLoading().getValue())) return;
        setLoading(true);
        StaffAccess.check(() -> ref.child(table.key).runTransaction(new Transaction.Handler() {
            public Transaction.Result doTransaction(MutableData data) {
                if (data.getValue() == null) return Transaction.success(data);
                if (!expected.equals(data.child(field).getValue())) return Transaction.abort();
                data.child(field).setValue(target);
                data.child("updatedAt").setValue(ServerValue.TIMESTAMP);
                // Never touch legacy status, Orders or date reservation slots here.
                return Transaction.success(data);
            }
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot s) {
                setLoading(false);
                if (e != null || !committed || s == null || !target.equals(s.child(field).getValue()))
                    fail("Table changed or update denied. Please refresh.");
            }
        }, false), this::fail);
    }
    private void fail(String message) { setLoading(false); setErrorMessage(message); }
    public void consumeError() { setErrorMessage(null); }
    protected void onCleared() {
        closed=true;
        if(ref!=null && listener!=null) ref.removeEventListener(listener);
        if(ordersRef!=null && ordersListener!=null) ordersRef.removeEventListener(ordersListener);
    }
}
