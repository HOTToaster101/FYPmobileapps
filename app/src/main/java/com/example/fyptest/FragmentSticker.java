package com.example.fyptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentSticker extends Fragment {

    GridView list;
    ArrayList<Sticker> stickerlist;
    String path;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_sticker, container, false);

        path = getContext().getExternalFilesDir(null) + File.separator;
        stickerlist = new ArrayList<>();
        //configure the recycle list to show all the stickers
        list = v.findViewById(R.id.gv_sticker);
        int stickerCount = getStickerCount();
        for(int i = 0; i < stickerCount; i++){
            FileInputStream fileInputStream = null;
            Bitmap b = null;
            String info = Integer.toString(i);
            String date = "Unavailable";
            Sticker s;
            try {
                File fbitmap = new File(path + i + "grabcut.webp");
                File f = new File(path + i + "grabcut.txt");
                if(f.exists()){
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    if(br.readLine().equals(Integer.toString(i))){
                        info = br.readLine();
                        date = br.readLine();
                    }
                }
                if(fbitmap.exists()){
                    b = BitmapFactory.decodeStream(new FileInputStream(fbitmap));
                    b = Bitmap.createScaledBitmap(b, 300, 300, false);
                }else{
                    b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            s = new Sticker(i, date, b);
            s.setName(info);
            stickerlist.add(s);
        }
        StickerAdapter adapter = new StickerAdapter(this.getContext(), stickerlist);
        list.setAdapter(adapter);

        return v;
    }

    public int getStickerCount(){
        String state = Environment.getExternalStorageState();
        int count = 0;
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(path + "config.txt");
            //file.delete();
            if (file.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line = br.readLine();
                    count = Integer.parseInt(line);

                    br.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }
            }
        }
        System.out.println("the current count is " + count);
        return count;
    }

}
