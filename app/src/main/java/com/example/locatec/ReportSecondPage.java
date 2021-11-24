package com.example.locatec;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class ReportSecondPage extends Fragment {
    Button goPrevBtn, submitBtn;
    TextInputLayout menu;
    AutoCompleteTextView menu_autocomplete;
    RadioButton addImageRBtn, removeImageRBtn;
    ImageView userAddImage;

    //데이터
    boolean isAddingImage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  (ViewGroup) inflater.inflate(
                R.layout.report_secondpage, container, false);

        bindingBtns(v);
        bindingMenu(v);
        bindingRadio(v);
        bindingImage(v);

        return v;
    }

    public void bindingBtns(View v) {
        goPrevBtn = (Button) v.findViewById(R.id.goPrevBtn);
        goPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ScreenSlidePagerActivity)ScreenSlidePagerActivity.viewPagerContext).goFirst();
            }
        });
        submitBtn = (Button) v.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 제출 후
                ((ScreenSlidePagerActivity)ScreenSlidePagerActivity.viewPagerContext).submit();
            }
        });
    }

    public void bindingMenu(View v) {
        menu = (TextInputLayout) v.findViewById(R.id.select_type);
        menu_autocomplete = (AutoCompleteTextView) v.findViewById(R.id.menu_autocomplete);
        List<String> items = Arrays.asList(getString(R.string.menu_item_smoking), getString(R.string.menu_item_trashcan));
        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.report_menu_item, items);
        menu_autocomplete.setAdapter(adapter);
        menu.setBoxStrokeWidth(0);
        menu.setBoxStrokeColor(0xffffffff);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            menu_autocomplete.setText(getString(R.string.menu_item_smoking), false);
        }
        menu_autocomplete.setEnabled(false);
    }

    public void bindingRadio(View v) {
        addImageRBtn = (RadioButton) v.findViewById(R.id.addImageButton);
        removeImageRBtn = (RadioButton) v.findViewById(R.id.removeImageButton);

        addImageRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddingImage = true;
                // 이미지 불러오기
                // 취소시, 그냥 아무것도 안함. 나중에 제출때 true이고, 이미지가 있을때만 제출
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK)   ;
                someActivityResultLauncher.launch(intent);
            }
        });
        removeImageRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddingImage = false;
                userAddImage.setImageResource(android.R.color.transparent);
            }
        });
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Glide.with(getContext()).load(result.getData().getData()).into(userAddImage);
                    }
                }
            });

    public void bindingImage(View v) {
        userAddImage = (ImageView)v.findViewById(R.id.userAddImage);
    }

    public String getMenuText() {
        return menu_autocomplete.getEditableText().toString();
    }



}
