package ru.getof.stoevent.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.getof.stoevent.Model.ClientsForRV;
import ru.getof.stoevent.Model.EventModel;
import ru.getof.stoevent.Model.EventModelSort;
import ru.getof.stoevent.R;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private Context mContext;
    private ArrayList<EventModelSort> eventModels;
    private ArrayList<ClientsForRV> clientsForRVS;
    private OnItemClickListener listener;

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_events,parent,false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        for (int i=0;i<clientsForRVS.size();i++){
            if (clientsForRVS.get(i).getUid().equals(eventModels.get(position).getClientId())){
                holder.client.setText(clientsForRVS.get(i).getName());
            }
        }
        holder.date.setText(eventModels.get(position).getDate());
        holder.time.setText(eventModels.get(position).getTime());
        holder.desc.setText(eventModels.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return eventModels.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public EventsAdapter(Context mContext, ArrayList<EventModelSort> eventModels, ArrayList<ClientsForRV> clientsForRVS) {
        this.mContext = mContext;
        this.eventModels = eventModels;
        this.clientsForRVS = clientsForRVS;
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView date,time,client,desc;
        CardView itemCV;

        EventViewHolder(@NonNull final View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.item_date_event);
            time = itemView.findViewById(R.id.item_time_event);
            client = itemView.findViewById(R.id.item_client_event);
            desc = itemView.findViewById(R.id.item_desc_event);
            itemCV = itemView.findViewById(R.id.item_event);


            itemCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });


        }
    }
}
