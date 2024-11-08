package my.insta.androrealm.Messages.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import my.insta.androrealm.Messages.MessageActivity;
import my.insta.androrealm.Messages.Model.Chat;
import my.insta.androrealm.R;
import my.insta.androrealm.models.Users;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context mContext;
    private List<Users> mUser;
    private boolean isChat;

    private FirebaseUser firebaseUser;
    private String lastMessage;

    public FriendsAdapter(Context mContext, List<Users> mUser, boolean isChat) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_single_layout, parent, false);
        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Users user = mUser.get(position);

        if (user != null) {
            holder.username.setText(user.getUsername() != null ? user.getUsername() : "Unknown User");

            if (user.getProfilePhoto() != null) {
                Glide.with(mContext).load(user.getProfilePhoto()).into(holder.profileimage);
            } else {
                holder.profileimage.setImageResource(R.drawable.defualt_insta_pic); // Placeholder image
            }

            if (isChat) {
                lastMessage(user.getUser_id(), holder.last_msg);
            } else {
                holder.last_msg.setText(user.getFullName() != null ? user.getFullName() : "No Name");
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getUser_id());
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUser != null ? mUser.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, last_msg;
        public CircleImageView profileimage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.FriendSingle_userName);
            last_msg = itemView.findViewById(R.id.FriendSingle_lastMsg);
            profileimage = itemView.findViewById(R.id.FriendSingle_user_img);
        }
    }

    // Check for the last message from/to a specific user
    private void lastMessage(final String userid, final TextView last_msg) {
        lastMessage = "default";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if ((chat.getReceiver() != null && chat.getReceiver().equals(firebaseUser.getUid()) &&
                                chat.getSender() != null && chat.getSender().equals(userid)) ||
                                (chat.getReceiver() != null && chat.getReceiver().equals(userid) &&
                                        chat.getSender() != null && chat.getSender().equals(firebaseUser.getUid()))) {
                            lastMessage = chat.getMessage();
                        }
                    }
                }

                if ("default".equals(lastMessage)) {
                    last_msg.setText("No Message");
                } else {
                    last_msg.setText(lastMessage);
                }

                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error if necessary
            }
        });
    }
}
