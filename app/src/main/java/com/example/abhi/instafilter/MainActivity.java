package com.example.abhi.instafilter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.abhi.instafilter.Adapter.ViewPagerAdapter;
import com.example.abhi.instafilter.Interface.EditImageFragmentListener;
import com.example.abhi.instafilter.Interface.FiltersListFragmentListener;
import com.example.abhi.instafilter.Utils.BitmapUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity implements FiltersListFragmentListener,EditImageFragmentListener {

    public static final String pictureName = "Flash.jpg";
    public static final int PERMISSION_PICK_IMAGE = 1000;

    ImageView img_preview;
    TabLayout tabLayout;
    ViewPager viewPager;
    CoordinatorLayout coordinatorLayout;


    Bitmap originalBitmap,filterdBitmap,finalBitmap;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float constrantFinal = 1.0f;

    //Load native Image  filters lib
    static {
        System.loadLibrary("NativeImageProcessor");
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Instagram Filter");


        //view
        img_preview = (ImageView)findViewById(R.id.image_preview);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        loadImage();

        seUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void loadImage() {
        img_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                
                
                
                
                

                           Intent  intent = new  Intent();
                           intent.setType(("image/*"));
                           intent.setAction(Intent.ACTION_GET_CONTENT);
                           startActivityForResult(Intent.createChooser(intent, "Select Picture"), PERMISSION_PICK_IMAGE);

            }
        });

        originalBitmap = BitmapUtils.getBitMapFromAssets(this,pictureName,300,300);
        filterdBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        img_preview.setImageBitmap(originalBitmap);
    }

    private void seUpViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment,"FILTERS");
        adapter.addFragment(editImageFragment,"EDIT");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onConstrantChanged(float constrant) {

        constrantFinal = constrant;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(constrant));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {

        Bitmap bitmap = filterdBitmap.copy(Bitmap.Config.ARGB_8888,true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        myFilter.addSubFilter(new ContrastSubFilter(constrantFinal));


        finalBitmap = myFilter.processFilter(bitmap);


    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();
        filterdBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        img_preview.setImageBitmap(filter.processFilter(filterdBitmap));
        finalBitmap = filterdBitmap.copy(Bitmap.Config.ARGB_8888,true);

    }

    private void resetControl() {

        if (editImageFragment != null)
            editImageFragment.resetControls();
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        constrantFinal = 1.0f;
    }

    //ctrl + o


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open)
        {
            openImageFromGallery();
            return true;
        }

        if (id == R.id.action_save)
        {
            saveImageToGallery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted())
                        {
                            try {













                                final String path = BitmapUtils.insertImage(getContentResolver(),
                                        filterdBitmap,
                                        System.currentTimeMillis()+"_profile.jpg",
                                        null);
                                if (!TextUtils.isEmpty(path))
                                {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                            "Image save to Gallary",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("OPEN", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    openImage(path);
                                                }
                                            });
                                    snackbar.show();
                                }
                                else
                                {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                            "Unable to Save Image",
                                            Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
        .check();
    }

    private void openImage(String path) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse(path),"image/*");
//        startActivity(intent);

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.parse(path);
                intent.setDataAndType(data, "image/*");
                startActivity(intent);



        




    }

    private void openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted())
                        {
//                            Intent intent = new Intent(Intent.ACTION_PICK);
//                            intent.setType("image/*");
//                            startActivityForResult(intent,PERMISSION_PICK_IMAGE);

                            Intent  intent = new  Intent();
                            intent.setType(("image/*"));
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PERMISSION_PICK_IMAGE);                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                })
        .check();

    }

    //ctrl + o


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == PERMISSION_PICK_IMAGE)
        {


//            Bitmap bitmap = BitmapUtils.getBitmapFromgallery(this,data.getData(),800,800);



////                          if(resultCode == RESULT_OK){
//                                      Uri selectedImage = data.getData();
//                                      String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                                      Cursor cursor = getContentResolver().query(
//                                                         selectedImage, filePathColumn, null, null, null);
//                                      cursor.moveToFirst();
//
//                                      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                                      String filePath = cursor.getString(columnIndex);
//                                      cursor.close();
//
//
//                                      Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                                  }
                                                        try {
                                                            final Uri imageUri = data.getData();
                                                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                                            final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);


                                                            //clear bitkmap memory
                                                            originalBitmap.recycle();
                                                            finalBitmap.recycle();
                                                            filterdBitmap.recycle();

                                                            originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                            filterdBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                            finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                            img_preview.setImageBitmap(originalBitmap);
                                                            bitmap.recycle();

                                                            //Render Selected Image Thumbnail
                                                            filtersListFragment.displayThumbnail(originalBitmap);
                                                        }
                                                        catch (Exception e)
                                                        {}
        }
    }
}
