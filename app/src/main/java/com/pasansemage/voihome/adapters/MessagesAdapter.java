package com.pasansemage.voihome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pasansemage.voihome.R;
import com.pasansemage.voihome.models.Message;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;

    int ITEM_SEND = 1;
    int ITEM_RECIEVE = 2;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.user_chat_layout, parent, false );
            return new SenderViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.voihome_chat_layout, parent, false );
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
             Message msg = messages.get(position);
             if(holder.getClass() == SenderViewHolder.class) {
                 SenderViewHolder viewHolder = (SenderViewHolder) holder;
                 viewHolder.txtMessage.setText(msg.getMessage());
             }else{
                 RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                 viewHolder.txtMessage.setText(msg.getMessage());
             }
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderNumber() == ITEM_SEND){
            return ITEM_SEND;
        }else {
            return ITEM_RECIEVE;
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }



    class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView txtMessage;
        
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.usermessage);
            
        }
    }

    class RecieverViewHolder extends RecyclerView.ViewHolder{

        TextView txtMessage;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.voimessage);
        }
    }
}
