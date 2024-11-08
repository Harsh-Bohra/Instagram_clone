package my.insta.androrealm.Messages.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import my.insta.androrealm.Messages.Adapter.FriendsAdapter;
import my.insta.androrealm.Messages.Model.Chat;
import my.insta.androrealm.Messages.Model.Chatlist;
import my.insta.androrealm.Messages.Notification.Token;
import my.insta.androrealm.R;
import my.insta.androrealm.models.Users;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendsAdapter userAdapter;
    private List<Users> mUsers;
    private FirebaseUser fuser;
    private DatabaseReference reference;
    private Set<String> usersSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.ChatsFragment_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersSet = new HashSet<>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (fuser != null) {
            reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    usersSet.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);

                        if (chat != null) {
                            if (chat.getSender().equals(fuser.getUid())) {
                                usersSet.add(chat.getReceiver());
                            }
                            if (chat.getReceiver().equals(fuser.getUid())) {
                                usersSet.add(chat.getSender());
                            }
                        }
                    }
                    readChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Log error if needed
                }
            });

            UpdateToken();
        } else {
            // Log error or handle null user scenario
        }

        return view;
    }

    private void UpdateToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String refreshToken = task.getResult();
                            Token token = new Token(refreshToken);
                            FirebaseDatabase.getInstance().getReference("Tokens")
                                    .child(firebaseUser.getUid())
                                    .setValue(token)
                                    .addOnFailureListener(e -> {
                                        // Log error or notify user if token update fails
                                    });
                        } else {
                            // Log or handle case where token fetching fails
                        }
                    });
        }
    }

    private void readChats() {
        mUsers = new ArrayList<>();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);

                    if (user != null && usersSet.contains(user.getUser_id())) {
                        mUsers.add(user);
                    }
                }

                userAdapter = new FriendsAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error if needed
            }
        });
    }
}
