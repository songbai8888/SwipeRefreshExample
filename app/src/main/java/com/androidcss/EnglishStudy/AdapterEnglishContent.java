package com.androidcss.EnglishStudy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

public class AdapterEnglishContent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private int selectedPos = 0;
    public List<DataEnglishContent> data= Collections.emptyList();
    DataEnglishContent current;
    int currentPos=0;

    // create constructor to innitilize context and data sent from MainActivity
    public AdapterEnglishContent(Context context, List<DataEnglishContent> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.container_item, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        DataEnglishContent current=data.get(position);
        myHolder.textFishName.setText(current.english_paper_title);
        myHolder.textType.setText("Category: " + current.english_paper_dir);
        if(current.is_downloaded ){
            myHolder.textPrice.setText("audio");
            myHolder.textPrice.setTextColor(Color.GREEN);
        }else if( current.english_paper_media.length() == 0 || current.english_paper_media == null){
            myHolder.textPrice.setText("text");
            myHolder.textPrice.setTextColor(Color.GREEN);
        }else {
            myHolder.textPrice.setText("audio");
            myHolder.textPrice.setTextColor(Color.RED);
        }
        switch(current.english_type_id){
            case 1:
            case 2:
                myHolder.ivFish.setImageResource(R.drawable.audio);
                break;
            case 3:
                myHolder.ivFish.setImageResource(R.drawable.news);
                break;
            default:
                myHolder.ivFish.setImageResource(R.drawable.others);
        }
        myHolder.itemView.setSelected(selectedPos == position);

        // load image into imageview using glide
//        Glide.with(context).load("http://192.168.1.7/test/images/" + current.fishImage)
//                .placeholder(R.drawable.ic_img_error)
//                .error(R.drawable.ic_img_error)
//                .into(myHolder.ivFish);

    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        TextView textFishName;
        ImageView ivFish;
        TextView textSize;
        TextView textType;
        TextView textPrice;

        // create constructor to get widget reference
        public MyHolder(final View itemView) {
            super(itemView);
            textFishName= (TextView) itemView.findViewById(R.id.textFishName);
            ivFish= (ImageView) itemView.findViewById(R.id.ivFish);
            textSize = (TextView) itemView.findViewById(R.id.textSize);
            textType = (TextView) itemView.findViewById(R.id.textType);
            textPrice = (TextView) itemView.findViewById(R.id.textPrice);

            textPrice.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int pos = getAdapterPosition();
                    //Toast.makeText(itemView.getContext(), data.get(pos).english_paper_title, Toast.LENGTH_SHORT).show();

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int pos = getAdapterPosition();
                    MainActivity.currentSelected = pos;
                    //Toast.makeText(itemView.getContext(), data.get(pos).english_paper_title, Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(itemView.getContext(),StudyActivity.class);
                    itemView.getContext().startActivity(myIntent);
                }
            });
        }

    }

}
