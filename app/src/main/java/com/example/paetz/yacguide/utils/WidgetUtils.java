package com.example.paetz.yacguide.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.paetz.yacguide.database.Ascend;

import java.util.HashSet;
import java.util.Set;

public class WidgetUtils {

    public static final int tableFontSizeDp = 18;
    public static final int infoFontSizeDp = 16;
    public static final int textFontSizeDp = 14;

    public static RelativeLayout createCommonRowLayout(Context context, String textLeft, String textRight, int textSizeDp, View.OnClickListener onClickListener, int bgColor, int typeFace) {
        RelativeLayout layout = new RelativeLayout(context);
        layout.setBackgroundColor(bgColor);
        layout.setOnClickListener(onClickListener);

        TextView textViewLeft = _createTextView(context, textLeft, textSizeDp, typeFace, 20, 20, 20, 20);
        layout.addView(textViewLeft);
        textViewLeft.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        TextView textViewRight = _createTextView(context, textRight, textSizeDp, typeFace, 20, 20, 20, 20);
        layout.addView(textViewRight);
        textViewRight.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) textViewRight.getLayoutParams();
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) textViewLeft.getLayoutParams();
        paramsLeft.addRule(RelativeLayout.LEFT_OF, textViewRight.getId());

        return layout;
    }

    public static RelativeLayout createCommonRowLayout(Context context, String textLeft, String textRight, int textSizeDp, View.OnClickListener onClickListener, int bgColor, int typeFace, Integer... padding) {
        RelativeLayout layout = new RelativeLayout(context);
        layout.setBackgroundColor(bgColor);
        layout.setOnClickListener(onClickListener);

        if (padding.length != 4) {
            return layout;
        }

        TextView textViewLeft = _createTextView(context, textLeft, textSizeDp, typeFace, padding[0], padding[1], padding[2], padding[3]);
        layout.addView(textViewLeft);
        textViewLeft.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        TextView textViewRight = _createTextView(context, textRight, textSizeDp, typeFace, padding[0], padding[1], padding[2], padding[3]);
        layout.addView(textViewRight);
        textViewRight.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) textViewRight.getLayoutParams();
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) textViewLeft.getLayoutParams();
        paramsLeft.addRule(RelativeLayout.LEFT_OF, textViewRight.getId());

        return layout;
    }

    public static View createHorizontalLine(Context context, int size) {
        View lineView = new View(context);
        lineView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size));
        lineView.setBackgroundColor(Color.parseColor("#000000"));

        return lineView;
    }

    private static TextView _createTextView(Context context, String text, int textSizeDp, int typeFace, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        TextView textView = new TextView(context);
        textView.setId(View.generateViewId());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp);
        textView.setTypeface(textView.getTypeface(), typeFace);
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        return textView;
    }
}
