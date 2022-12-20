package com.example.ocr;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Entities.Product;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    Button captureButton, copyButton;
    TextView textData;
    private static final int REQUEST_CAMERA_CODE = 100;
    Bitmap bitmap;
    TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureButton = findViewById(R.id.captureButton);
        copyButton = findViewById(R.id.copyButton);
        table = findViewById(R.id.Table);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scannedText = textData.getText().toString();
               CopyToClipboard(scannedText);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri uriResult = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriResult);
                    GetTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void GetTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> text =
                recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        List<Product> productList = new ArrayList<>();
                        List<Text.TextBlock> textBlocks = visionText.getTextBlocks();
                        List<Text.Line> textLine = textBlocks.get(0).getLines();
                        List<Text.Element> allElements = new ArrayList<>();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            for (Text.Line Line : block.getLines()) {
                                for (Text.Element element : Line.getElements()) {
                                    allElements.add(element);
                                }
                            }
                        }
                        while(!allElements.isEmpty()) {
                            List<Text.Element> lineElements = allElements.stream()
                                    .filter(p -> p.getCornerPoints()[0].y <= allElements.get(0).getCornerPoints()[0].y + 70 && p.getCornerPoints()[0].y >= allElements.get(0).getCornerPoints()[0].y - 70)
                                    .collect(Collectors.toList());
                            Collections.sort(lineElements, (e1, e2) -> e1.getCornerPoints()[0].x - e2.getCornerPoints()[0].x);
                            allElements.removeIf(p -> p.getCornerPoints()[0].y <= allElements.get(0).getCornerPoints()[0].y + 70 && p.getCornerPoints()[0].y >= allElements.get(0).getCornerPoints()[0].y - 70);
                            Product product = new Product();
                            StringBuilder name = new StringBuilder();
                            StringBuilder price = new StringBuilder();
                            for (Text.Element element : lineElements) {
                                if (element.getCornerPoints()[0].x < 1000) {
                                    name.append(element.getText());
                                } else {
                                    price.append(element.getText());
                                }
                                Log.d("product",  "name: " + name.toString() + "price: " + price.toString());
                                //stringBuilder.append(element.getCornerPoints()[0]);
                                //stringBuilder.append(" ");
                            }
                            product.setName(name.toString());
                            product.setPrice(price.toString());
                            productList.add(product);
                        }
                        //textData.setText(stringBuilder.toString());
                        AddProductsToTable(productList);
                    });
        captureButton.setText("Retake");
        copyButton.setVisibility(View.VISIBLE);
    }

    private void AddProductsToTable(List<Product> products){
       for(Product product : products){
           TableRow row = new TableRow(this);
           TextView name = new TextView(this);
           TextView price = new TextView(this);
           name.setText(product.getName());
           price.setText(product.getPrice());
           row.addView(name);
           row.addView(price);
           table.addView(row);
       }
    }


    private void CopyToClipboard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied data", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "copied to Clipboard!", Toast.LENGTH_SHORT).show();
    }
}