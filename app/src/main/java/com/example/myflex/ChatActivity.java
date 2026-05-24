package com.example.myflex;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myflex.databinding.ActivityChatBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ChatActivity — real-time 1-to-1 messaging between a patient and their therapist.
 *
 * HOW TO OPEN FROM MAINACTIVITY (patient side — no extras needed):
 *   startActivity(new Intent(this, ChatActivity.class));
 *   ChatActivity will automatically find the first available therapist.
 *
 * HOW TO OPEN FROM THERAPIST DASHBOARD (pass target patient):
 *   Intent i = new Intent(this, ChatActivity.class);
 *   i.putExtra("other_user_id",   patient.getUserId());
 *   i.putExtra("other_user_name", patient.getName());
 *   i.putExtra("other_user_role", "patient");
 *   startActivity(i);
 *
 * Firestore path:
 *   chats/{uid_a}_{uid_b}/messages/{messageId}
 *   where uid_a < uid_b (alphabetically sorted to avoid duplicates)
 */
public class ChatActivity extends AppCompatActivity {

    // ── Fields ─────────────────────────────────────────────────────
    private ActivityChatBinding   binding;
    private FirebaseFirestore     db;
    private SessionManager        sm;
    private ChatMessageAdapter    adapter;
    private ListenerRegistration  listener;

    private final List<ChatMessage> messageList = new ArrayList<>();

    // Current logged-in user
    private String myId;
    private String myName;
    private String myRole;

    // The other party
    private String otherId;
    private String otherName;
    private String otherRole;

    // Deterministic room ID
    private String chatRoomId;

    // ── Lifecycle ─────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        sm = new SessionManager(this);

        // Load current user from Firebase Auth
        if (com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .getCurrentUser() != null) {

            myId = com.google.firebase.auth.FirebaseAuth
                    .getInstance()
                    .getCurrentUser()
                    .getUid();
        }

        myName = sm.getName();
        myRole = sm.getRole();

        // Load the other party from intent
        otherId   = getIntent().getStringExtra("other_user_id");
        otherName = getIntent().getStringExtra("other_user_name");
        otherRole = getIntent().getStringExtra("other_user_role");

        // If no target specified, auto-find the assigned therapist
        if (otherId == null || otherId.isEmpty()) {
            autoFindOtherParty();
        } else {
            initUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    // ── Auto-find therapist (for patient) or patient (for therapist) ──
    private void autoFindOtherParty() {

        // إذا المستخدم therapist
        if ("therapist".equalsIgnoreCase(myRole)) {

            db.collection("users")
                    .whereEqualTo("assignedTherapistId", myId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {

                        if (query.isEmpty()) {
                            showNoPartyUI("patient");
                            return;
                        }

                        var doc = query.getDocuments().get(0);

                        otherId = doc.getId();
                        otherName = doc.getString("name");
                        otherRole = "patient";

                        initUI();
                    });

        } else {

            // إذا المستخدم patient
            db.collection("users")
                    .document(myId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (!doc.exists()) {
                            showNoPartyUI("therapist");
                            return;
                        }

                        otherId = doc.getString("assignedTherapistId");

                        if (otherId == null || otherId.isEmpty()) {
                            showNoPartyUI("therapist");
                            return;
                        }

                        db.collection("users")
                                .document(otherId)
                                .get()
                                .addOnSuccessListener(tDoc -> {

                                    otherName = tDoc.getString("name");
                                    otherRole = "therapist";

                                    initUI();
                                });
                    });
        }
    }

    private void showNoPartyUI(String targetRole) {
        binding.tvOtherName.setText("No " + targetRole + " found");
        binding.tvOtherStatus.setText("Please contact your clinic");
        binding.layoutInput.setVisibility(View.GONE);
        showEmpty("No " + targetRole + " has been assigned yet.\nPlease contact your clinic.");
    }

    // ── Init UI after both IDs are ready ──────────────────────────
    private void initUI() {
        // Build room ID (alphabetically sorted to avoid duplicates)
        String a = (myId    != null) ? myId    : "guest";
        String b = (otherId != null) ? otherId : "support";
        chatRoomId = a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
        Toast.makeText(this,
                "ROOM: " + chatRoomId,
                Toast.LENGTH_LONG).show();

        // Header
        String displayName = (otherName != null && !otherName.isEmpty()) ? otherName : "Therapist";
        binding.tvOtherName.setText(displayName);
        binding.tvOtherStatus.setText(
                "therapist".equalsIgnoreCase(otherRole) ? "🩺 Physical Therapist" : "👤 Patient"
        );
        binding.tvOtherAvatar.setText(getInitials(displayName));

        // RecyclerView
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(llm);
        adapter = new ChatMessageAdapter(messageList, a);
        binding.rvMessages.setAdapter(adapter);

        // Send button starts disabled until user types something
        binding.btnSend.setEnabled(false);
        binding.btnSend.setAlpha(0.5f);

        // Watch input field to enable/disable send button
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                binding.btnSend.setEnabled(hasText);
                binding.btnSend.setAlpha(hasText ? 1f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Listeners
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSend.setOnClickListener(v -> sendMessage());

        // Start real-time message listener
        listenForMessages();
    }

    // ── Real-time listener ────────────────────────────────────────
    private void listenForMessages() {
        listener = db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                            if (msg != null) {
                                messageList.add(msg);
                                adapter.notifyItemInserted(messageList.size() - 1);
                                binding.rvMessages.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }

                    binding.tvEmpty.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ── Send message ──────────────────────────────────────────────
    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        binding.etMessage.setText("");

        String msgId = UUID.randomUUID().toString();
        long   now   = System.currentTimeMillis();

        // Message document
        Map<String, Object> msg = new HashMap<>();
        msg.put("messageId",  msgId);
        msg.put("senderId",   myId   != null ? myId   : "guest");
        msg.put("senderName", myName != null ? myName : "User");
        msg.put("senderRole", myRole != null ? myRole : "patient");
        msg.put("text",       text);
        msg.put("timestamp",  now);
        msg.put("isRead",     false);

        // Chat room metadata (for future inbox)
        Map<String, Object> roomMeta = new HashMap<>();
        roomMeta.put("lastMessage",  text);
        roomMeta.put("lastTime",     now);
        roomMeta.put("patientId",    "patient".equalsIgnoreCase(myRole) ? myId : otherId);
        roomMeta.put("therapistId",  "therapist".equalsIgnoreCase(myRole) ? myId : otherId);
        roomMeta.put("participants", Arrays.asList(
                myId    != null ? myId    : "guest",
                otherId != null ? otherId : "support"
        ));

        // Write to Firestore: room metadata first, then the message
        db.collection("chats")
                .document(chatRoomId)
                .set(roomMeta)
                .addOnSuccessListener(unused ->
                        db.collection("chats")
                                .document(chatRoomId)
                                .collection("messages")
                                .document(msgId)
                                .set(msg)
                                .addOnFailureListener(ex ->
                                        Toast.makeText(this,
                                                "Send failed: " + ex.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                )
                )
                .addOnFailureListener(ex ->
                        Toast.makeText(this,
                                "Error: " + ex.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void showEmpty(String message) {
        binding.tvEmpty.setText(message);
        binding.tvEmpty.setVisibility(View.VISIBLE);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        }
        return sb.toString().toUpperCase();
    }
}