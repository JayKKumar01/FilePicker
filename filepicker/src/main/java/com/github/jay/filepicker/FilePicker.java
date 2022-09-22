package com.github.jay.filepicker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilePicker implements View.OnClickListener {
    private File recent = Environment.getExternalStorageDirectory();
    private String[] extensions;
    private final Context context;

    private final View dialogView;
    private final ViewGroup parent;

    private RecyclerView recyclerView;
    private ConstraintLayout rootSD;
    private ConstraintLayout rootView;
    private ConstraintLayout storageView;
    private TextView titleText;
    private boolean initialized;
    private final int recViewItemView;
    private final File phone;
    private File sd;
    private final Modify modify = new Modify();
    private OnTouch onTouch;
    private SelectListener callbacks;
    private CancelListener cancelCallback;

    public FilePicker(Context context) {
        this.context = context;
        Activity activity = (Activity) context;
        this.parent = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.custom_dialog,parent,false);
        dialogView.setTranslationZ(Float.MAX_VALUE);
        initViews();
        //
        this.recViewItemView = R.layout.filemanager_recycle_item;
        List<String> paths = FileUtil.rootPaths(context);
        phone = new File(paths.get(0));
        sd = null;
        if (paths.size()>=2){
            sd = new File(paths.get(1));
        }
        else {
            rootSD.setVisibility(View.GONE);
        }
    }
    public void show(){
        dialogView.setVisibility(View.VISIBLE);
        if (!initialized) {
            parent.addView(dialogView);
            initialized = true;
        }

        //activity.setContentView(dialogView);
    }
    private void cancel() {
        dialogView.setVisibility(View.GONE);
        if (cancelCallback != null){
            cancelCallback.onCancelled();
        }
    }
    public void setTitle(String title){
        titleText.setText(title.toUpperCase());
    }
    public void setExtensions(String[] arr){
        extensions = arr;
    }




    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.root) {
            String str = "";
        } else if (id == R.id.rootPhone) {
            storageView.setVisibility(View.VISIBLE);
            rootView.setVisibility(View.GONE);
            modify.updateView(phone);
            recent = new File("null");
        } else if (id == R.id.rootSD) {
            storageView.setVisibility(View.VISIBLE);
            rootView.setVisibility(View.GONE);
            modify.updateView(sd);
            recent = new File("null");
        } else if (id == R.id.goBack) {
            modify.goBack();
        } else if (id == R.id.cancel) {
            cancel();
        }

    }



    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {

        this.recyclerView = dialogView.findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ImageView menu = dialogView.findViewById(R.id.imgMenu);
        ConstraintLayout root = dialogView.findViewById(R.id.root);
        ConstraintLayout rootPhone = dialogView.findViewById(R.id.rootPhone);
        this.rootSD = dialogView.findViewById(R.id.rootSD);
        this.rootView = dialogView.findViewById(R.id.rootView);
        this.storageView = dialogView.findViewById(R.id.storageView);
        ConstraintLayout goBack = dialogView.findViewById(R.id.goBack);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        this.titleText = dialogView.findViewById(R.id.titleText);


        root.setOnClickListener(this);
        rootPhone.setOnClickListener(this);
        menu.setOnClickListener(this);
        rootSD.setOnClickListener(this);
        goBack.setOnClickListener(this);
        cancel.setOnClickListener(this);


        onTouch = new OnTouch(context);
        rootPhone.setOnTouchListener(onTouch);
        rootSD.setOnTouchListener(onTouch);
        goBack.setOnTouchListener(onTouch);
        cancel.setOnTouchListener(onTouch);


    }

    public class Modify {
        public Modify() {
        }

        public void reset(View shouldVisible, View shouldGone){
            shouldGone.setVisibility(View.GONE);
            shouldVisible.setVisibility(View.VISIBLE);
        }
        @SuppressLint("NotifyDataSetChanged")
        public void goBack(){
            if (recent.listFiles() == null){
                reset(rootView, storageView);
                return;
            }
            File[] fileFolders = recent.listFiles();
            if(fileFolders != null) {
                Arrays.sort(fileFolders);
            }
            Adapter newAdapter = new Adapter(recent);
            recyclerView.setAdapter(newAdapter);
            newAdapter.notifyDataSetChanged();
            String str = recent.getPath();
            //Toast.makeText(context, ""+str, Toast.LENGTH_SHORT).show();
            if (str.contains("/")){
                String str2 = str.substring(str.lastIndexOf("/"));
                str = str.replace(str2,"");
            }
            recent = new File(str);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void updateView(File folder){
            if (folder.isDirectory()){
                File[] newFolder = folder.listFiles();
                if (newFolder == null){
                    Toast.makeText(context, "Access Denied!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newFolder.length == 0){
                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show();
                    //return;
                }
                Adapter newAdapter = new Adapter(folder);
                recyclerView.setAdapter(newAdapter);
                newAdapter.notifyDataSetChanged();
            }
            else{
                getPath(folder);
            }


        }
    }



    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
        File folder;
        List<String> fileAndFolder = new ArrayList<>();

        public Adapter(File folder) {
            this.folder = folder;
            this.fileAndFolder = sort(folder.listFiles());
        }

        List<String> sort(File[] folders){
            List<String> list = new ArrayList<>();
            if (folders == null){
                return null;
            }
            for (File file: folders){
                list.add(file.getPath());
            }
            Collections.sort(list,String.CASE_INSENSITIVE_ORDER);

            List<String> newList = new ArrayList<>();
            List<String> newFile = new ArrayList<>();
            for (String str: list){
                File file = new File(str);
                if (file.isDirectory()){
                    newList.add(str);
                }
                else {
                    if (extensions == null){
                        newFile.add(str);
                    }
                    else{
                        String name = file.getName();
                        for (String ext: extensions){
                            if (name.contains(ext)){
                                newFile.add(str);
                                break;
                            }
                        }
                    }

                }
            }
            newList.addAll(newFile);
            return newList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(recViewItemView,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String selectedStr = fileAndFolder.get(position);
            File selectedFile = new File(selectedStr);
            holder.fileText.setText(selectedFile.getName());
            if (selectedFile.isDirectory()){
                holder.iconView.setImageResource(R.drawable.folder);
            }
            else {
                holder.iconView.setImageResource(R.drawable.file);
            }
            holder.itemView.setOnTouchListener(onTouch);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    if (selectedFile.isDirectory() && selectedFile.listFiles() != null) {
                        recent = folder;
                    }
                    modify.updateView(selectedFile);

                }
            });

        }

        @Override
        public int getItemCount() {
            return fileAndFolder.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView fileText;
            ImageView iconView;

            public ViewHolder(View itemView) {
                super(itemView);
                fileText = itemView.findViewById(R.id.fileText);
                iconView = itemView.findViewById(R.id.iconView);
            }
        }
    }

    public class OnTouch extends View implements View.OnTouchListener {

        public OnTouch(Context context) {
            super(context);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                view.setBackground(context.getDrawable(R.drawable.bg_hover_dialogs));

            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.setBackgroundColor(Color.parseColor("#00000000"));

            }
            return super.onTouchEvent(motionEvent);
        }
    }
    public class AspectRatioDialogViews {

        public AspectRatioDialogViews() {
        }

        public void set(Activity activity){

            AspectRatioDialogViews aspectRatio = new AspectRatioDialogViews();
            for(int id: aspectRatio.IDs()) {
                View view = activity.findViewById(id);
                int width = view.getLayoutParams().width;
                int height = view.getLayoutParams().height;

                view.getLayoutParams().height = aspectRatio.value(height);
                view.getLayoutParams().width = aspectRatio.value(width);
            }
        }

        int value(int value) {
            int dpi = Resources.getSystem().getDisplayMetrics().densityDpi;
            int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

            return (int) (value * (double)deviceWidth/dpi * 0.3888888889);
        }

        int[] IDs(){
            return new int[] {
                    R.id.imgMenu,
                    R.id.imgPhone,
                    R.id.imgSD
            };
        }
    }




    public interface SelectListener {
        void onClicked(File file);
    }
    public void setOnclickListener(SelectListener callbacks){
        this.callbacks = callbacks;
    }
    public interface CancelListener{
        void onCancelled();
    }
    public void setOnCancelListener(CancelListener callbacks){
        this.cancelCallback = callbacks;
    }
    private void getPath(File folder) {
        dialogView.setVisibility(View.GONE);
        if (callbacks != null){
            callbacks.onClicked(folder);
        }
        //alertDialog.dismiss();
    }

}
