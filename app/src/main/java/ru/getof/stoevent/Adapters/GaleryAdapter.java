package ru.getof.stoevent.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import ru.getof.stoevent.R;

public class GaleryAdapter extends RecyclerView.Adapter<GaleryAdapter.GaleryViewHolder> {

    private final int TYPE_IMAGE_YES = 0;
    private final int TYPE_IMAGE_ADD = 1;
    private View viewG;

    private Context imgContext;
    private List<String> imagesEvents;
    private OnItemClickListener listener;

    public GaleryAdapter(Context imgContext, List<String> imagesEvents) {
        this.imgContext = imgContext;
        this.imagesEvents = imagesEvents;
    }

    public interface OnItemClickListener{
        void onItemClick(int id, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GaleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_IMAGE_YES:
                viewG = LayoutInflater.from(imgContext).inflate(R.layout.item_images, parent, false);
                break;
            case TYPE_IMAGE_ADD:
                viewG = LayoutInflater.from(imgContext).inflate(R.layout.item_image_add, parent, false);
                break;
        }
        return new GaleryViewHolder(viewG);
    }

    @Override
    public void onBindViewHolder(@NonNull GaleryViewHolder holder, int position) {

        if (position != imagesEvents.size()){
            Glide
                    .with(imgContext)
                    .load(imagesEvents.get(position))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.clickImage);
        }

    }

    @Override
    public int getItemCount() {
        return imagesEvents.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == imagesEvents.size()) return TYPE_IMAGE_ADD;
        return TYPE_IMAGE_YES;
    }

    public class GaleryViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout clickLayoutAdd, clickLayoutClose;
        ImageView clickImage;

        public GaleryViewHolder(@NonNull View itemView) {
            super(itemView);
            clickLayoutAdd = itemView.findViewById(R.id.layout_image__add);
            clickLayoutClose = itemView.findViewById(R.id.layout_close);
            clickImage = itemView.findViewById(R.id.imagePhoto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(v.getId(), position);
                        }
                    }
                }
            });


            if (clickLayoutAdd != null){
                clickLayoutAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(v.getId(), position);
                            }
                        }
                    }
                });
            }

            if (clickLayoutClose != null && clickImage != null){
                clickLayoutClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(v.getId(), position);
                            }
                        }
                    }
                });

                clickImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(v.getId(), position);
                            }
                        }
                    }
                });
            }


        }
    }
}
